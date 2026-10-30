import { useEffect, useState } from 'react'
import { DropdownBtn } from '../../Components/DropdownBtn/DropdownBtn.tsx'
import { RebalanceCard } from '../../Components/RebalanceCard/RebalanceCard.tsx'
import { RiskMetricsCard } from '../../Components/RiskMetricsCard/RiskMetricsCard.tsx'
import { SummaryList } from '../../Components/SummaryList/SummaryList.tsx'
import {
  formatSek,
  formatSignedPercent,
  formatSignedSek,
} from '../../utils/format.ts'
import { areDriftNotificationsEnabled } from '../../utils/driftNotificationPreference.ts'

// Use the same API configuration as the rest of the frontend.
const API_URL = import.meta.env.VITE_API_URL ?? ''
const HISTORY_IMPORT_INTERVAL_MS = 24 * 60 * 60 * 1000
const HISTORY_IMPORT_LAST_ATTEMPT_KEY = 'avanza.historical-import.last-attempt'

type PeriodId = '1m' | '3m' | '1y'

const performancePeriods: { id: PeriodId; label: string; days: number }[] = [
  { id: '1m', label: 'Senaste månaden', days: 30 },
  { id: '3m', label: 'Senaste 3 månaderna', days: 90 },
  { id: '1y', label: 'Senaste året', days: 365 },
]

const defaultPeriodId: PeriodId = '1y'

interface BacktestResponse {
  totalReturn: number
  annualizedReturn: number
  maxDrawdown: number
  sharpeRatio: number
  tickers: string[]
  dates: string[]
}

let historyImportInFlight: Promise<void> | null = null

const importHistoryOnceDaily = (): Promise<void> => {
  if (historyImportInFlight) {
    return historyImportInFlight
  }

  const lastAttempt = Number(
    window.localStorage.getItem(HISTORY_IMPORT_LAST_ATTEMPT_KEY),
  )
  if (lastAttempt > 0 && Date.now() - lastAttempt < HISTORY_IMPORT_INTERVAL_MS) {
    return Promise.resolve()
  }

  window.localStorage.setItem(HISTORY_IMPORT_LAST_ATTEMPT_KEY, String(Date.now()))
  historyImportInFlight = fetch(`${API_URL}/api/market/history/import`, {
    method: 'POST',
    credentials: 'include',
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`Historical import failed: ${response.status}`)
      }
    })
    .finally(() => {
      historyImportInFlight = null
    })

  return historyImportInFlight
}

// Define the portfolio response returned by the existing portfolio endpoint.
interface PortfolioResponse {
  accountSummary: {
    id: number
    accountType: string
    accountName: string
    currency: string
    totalValueSek: number
  }[]

  enrichedHoldings: {
    id: number
    ticker: string
    instrumentName: string
    quantity: number
    currentPrice: number | null
    valueSek: number | null
    unrealizedReturn: number | null
    unrealizedReturnPct: number | null
    sharpe: number | null
    displayCurrency: string
  }[]

  allocationRows: {
    accountType: string
    actual: number
    target: number
    drift: number
    overThreshold: boolean
  }[]

  totalPortfolioValue: number
}

// Define the allocation format expected by RebalanceCard.
interface AllocationSlice {
  label: string
  percent: number
}

const periodOptions = performancePeriods.map(({ id, label }) => ({
  label,
  value: id,
}))

const selectedPeriodLabel = (periodId: PeriodId) =>
  performancePeriods.find((period) => period.id === periodId)?.label ?? ''

// Convert backend account types into readable Swedish labels.
const formatAccountType = (accountType: string) => {
  switch (accountType) {
    case 'Depa':
      return 'Depå'
    case 'Pension':
      return 'Pension'
    default:
      return accountType
  }
}

export const Analyses = () => {
  const [periodId, setPeriodId] = useState<PeriodId>(defaultPeriodId)
  const [historyImportReady, setHistoryImportReady] = useState(false)
  const [historyImportError, setHistoryImportError] = useState<string | null>(null)

  // Store the current portfolio data received from the backend.
  const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null)

  // Store the native backtest result for the selected period.
  const [backtest, setBacktest] = useState<BacktestResponse | null>(null)

  // Track portfolio loading and errors separately from historical performance.
  const [isLoading, setIsLoading] = useState(true)
  const [isBacktestLoading, setIsBacktestLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [backtestError, setBacktestError] = useState<string | null>(null)

  // Fetch the current portfolio once when the page is opened.
  useEffect(() => {
    const controller = new AbortController()

    const loadPortfolio = async () => {
      try {
        // Include the session cookie when requesting the authenticated portfolio.
        const response = await fetch(`${API_URL}/api/portfolio`, {
          credentials: 'include',
          signal: controller.signal,
        })

        if (!response.ok) {
          throw new Error(`Portfolio request failed: ${response.status}`)
        }

        const data = (await response.json()) as PortfolioResponse

        setPortfolio(data)
        setError(null)
      } catch {
        // Ignore errors caused by cancelling the request.
        if (!controller.signal.aborted) {
          setError(
            'Kunde inte hämta portföljen. Kontrollera anslutningen och försök igen.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      }
    }

    void loadPortfolio()

    // Cancel the request if the component unmounts.
    return () => {
      controller.abort()
    }
  }, [])

  // Attempt one historical import per rolling 24 hours for this browser.
  useEffect(() => {
    let active = true

    const loadHistory = async () => {
      try {
        await importHistoryOnceDaily()
      } catch {
        if (active) {
          setHistoryImportError(
            'Historisk import misslyckades. Ett nytt försök tillåts om 24 timmar.',
          )
        }
      } finally {
        if (active) {
          setHistoryImportReady(true)
        }
      }
    }

    void loadHistory()

    return () => {
      active = false
    }
  }, [])

  // Run the native backtest after the import attempt and whenever the period changes.
  useEffect(() => {
    if (!historyImportReady || isLoading || !portfolio) {
      return
    }

    const controller = new AbortController()
    const tickers = [...new Set(portfolio.enrichedHoldings.map((holding) => holding.ticker))]
    const selectedPeriod = performancePeriods.find((period) => period.id === periodId)
    const periodDays = selectedPeriod?.days ?? 365
    const end = new Date()
    end.setUTCDate(end.getUTCDate() - 1)
    const start = new Date(end.getTime())
    start.setUTCDate(start.getUTCDate() - periodDays + 1)
    const endDate = end.toISOString().slice(0, 10)
    const startDate = start.toISOString().slice(0, 10)

    const loadBacktest = async () => {
      setIsBacktestLoading(true)
      setBacktestError(null)
      setBacktest(null)

      try {
        const response = await fetch(`${API_URL}/api/market/backtest`, {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          signal: controller.signal,
          body: JSON.stringify({
            tickers,
            startDate,
            endDate,
            strategy: 'BUY_HOLD',
          }),
        })

        if (!response.ok) {
          throw new Error(`Backtest request failed: ${response.status}`)
        }

        setBacktest((await response.json()) as BacktestResponse)
      } catch {
        if (!controller.signal.aborted) {
          setBacktestError(
            'Kunde inte köra backtest. Kontrollera att historiska priser finns för perioden.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsBacktestLoading(false)
        }
      }
    }

    void loadBacktest()

    return () => {
      controller.abort()
    }
  }, [historyImportReady, isLoading, periodId, portfolio])

  const handlePeriodChange = (value: string) => {
    const selected = performancePeriods.find((p) => p.id === value)

    if (selected) {
      setPeriodId(selected.id)
    }
  }

  // Calculate the invested amount using holdings with complete data.
  const investedValue =
    portfolio?.enrichedHoldings.reduce((total, holding) => {
      // Skip holdings without both the current value and unrealized return.
      if (
        holding.valueSek === null ||
        holding.unrealizedReturn === null
      ) {
        return total
      }

      // Calculate the original invested amount for this holding.
      return total + holding.valueSek - holding.unrealizedReturn
    }, 0) ?? 0

  // Calculate the total unrealized return using the same holdings.
  const gainSek =
    portfolio?.enrichedHoldings.reduce((total, holding) => {
      // Use the same data requirements as the invested value calculation.
      if (
        holding.valueSek === null ||
        holding.unrealizedReturn === null
      ) {
        return total
      }

      return total + holding.unrealizedReturn
    }, 0) ?? 0

  // Calculate the return percentage based on the invested amount.
  const gainPercent =
    investedValue > 0 ? (gainSek / investedValue) * 100 : 0

  // Map the actual account allocation returned by the backend.
  const currentAllocation: AllocationSlice[] =
    portfolio?.allocationRows
      .filter((row) => row.accountType !== 'Pension')
      .map((row) => ({
        label: formatAccountType(row.accountType),
        percent: row.actual,
      })) ?? []

  // Map the user's target allocation returned by the backend.
  const targetAllocation: AllocationSlice[] =
    portfolio?.allocationRows
      .filter((row) => row.accountType !== 'Pension')
      .map((row) => ({
        label: formatAccountType(row.accountType),
        percent: row.target,
      })) ?? []

  return (
    <div className="p-6">
      <div className="mx-auto mt-6 flex max-w-4xl flex-col gap-4">
        <DropdownBtn
          options={periodOptions}
          value={periodId}
          onChange={handlePeriodChange}
          className="self-start"
        />

        {error && (
          <p className="text-sm text-red-700" role="alert">
            {error}
          </p>
        )}

        {historyImportError && (
          <p className="text-sm text-amber-700" role="status">
            {historyImportError}
          </p>
        )}

        {isBacktestLoading ? (
          <div className="rounded-2xl border border-neutral-200 bg-white p-5" role="status">
            <p className="text-sm text-neutral-600">Hämtar backtestresultat...</p>
          </div>
        ) : backtestError ? (
          <p className="text-sm text-red-700" role="alert">{backtestError}</p>
        ) : backtest ? (
          <>
            <SummaryList
              title={`Backtestresultat · ${selectedPeriodLabel(periodId)}`}
              rows={[
                {
                  label: 'Total avkastning',
                  value: formatSignedPercent(backtest.totalReturn * 100),
                  tone: backtest.totalReturn >= 0 ? 'positive' : 'negative',
                },
                {
                  label: 'Annualiserad avkastning',
                  value: formatSignedPercent(backtest.annualizedReturn * 100),
                  tone: backtest.annualizedReturn >= 0 ? 'positive' : 'negative',
                },
              ]}
            />
            <RiskMetricsCard
              volatility={null}
              maxDrawdown={-Math.abs(backtest.maxDrawdown * 100)}
              sharpeRatio={backtest.sharpeRatio}
              periodLabel={selectedPeriodLabel(periodId)}
            />
            <p className="text-sm text-neutral-600">
              Tickers: {backtest.tickers.join(', ')}. Period:{' '}
              {backtest.dates[0]}–{backtest.dates[backtest.dates.length - 1]}.
            </p>
            <p className="text-sm text-orange-400" role="note">
              Historiska priser har konverterats till SEK med ECB-kursen för respektive handelsdag.
            </p>
          </>
        ) : (
          <p className="text-sm text-neutral-500" role="status">
            Inga backtestresultat tillgängliga för perioden.
          </p>
        )}

        <RebalanceCard
          current={currentAllocation}
          target={targetAllocation}
          showDriftAlert={areDriftNotificationsEnabled()}
        />

        <SummaryList
          title="Portfölj just nu"
          rows={[
            { label: 'Värde totalt', value: formatSek(portfolio?.totalPortfolioValue ?? 0) },
            {
              label: 'Avkastning (SEK)',
              value: formatSignedSek(gainSek),
              tone: gainSek >= 0 ? 'positive' : 'negative',
            },
            {
              label: 'Avkastning (%)',
              value: formatSignedPercent(gainPercent),
              tone: gainPercent >= 0 ? 'positive' : 'negative',
            },
          ]}
        />
      </div>
    </div>
  )
}