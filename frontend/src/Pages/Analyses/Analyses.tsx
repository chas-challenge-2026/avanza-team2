import { useEffect, useState } from 'react'
import { Title } from '../../Components/Title/Title.tsx'
import { DropdownBtn } from '../../Components/DropdownBtn/DropdownBtn.tsx'
import { PerformanceCard } from '../../Components/PerformanceCard/PerformanceCard.tsx'
import { RebalanceCard } from '../../Components/RebalanceCard/RebalanceCard.tsx'
import { SummaryList } from '../../Components/SummaryList/SummaryList.tsx'
import {
  formatSek,
  formatSignedPercent,
  formatSignedSek,
} from '../../utils/format.ts'

import {
  defaultPeriodId,
  performancePeriods,
  type PeriodId,
} from './analysesSampleData.ts'

// Use the same API configuration as the rest of the frontend.
const API_URL = import.meta.env.VITE_API_URL ?? ''

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

// Define a single historical performance point returned by the backend.
interface HistoricalPerformancePoint {
  date: string
  portfolioPct: number
  indexPct: number | null
}

// Define the complete historical performance response.
interface HistoricalPerformanceResponse {
  startValue: number
  points: HistoricalPerformancePoint[]
  unavailableTickers: string[]
  warning: string | null
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

  // Store the current portfolio data received from the backend.
  const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null)

  // Store the historical performance data for the selected period.
  const [performance, setPerformance] =
    useState<HistoricalPerformanceResponse | null>(null)

  // Track portfolio loading and errors separately from historical performance.
  const [isLoading, setIsLoading] = useState(true)
  const [isPerformanceLoading, setIsPerformanceLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [performanceError, setPerformanceError] = useState<string | null>(null)

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

  // Fetch historical performance whenever the selected period changes.
  useEffect(() => {
    const controller = new AbortController()

    const loadPerformance = async () => {
      setIsPerformanceLoading(true)
      setPerformanceError(null)
      setPerformance(null)

      try {
        // Request historical performance for the selected time period.
        const response = await fetch(
          `${API_URL}/api/portfolio/performance?period=${encodeURIComponent(periodId)}`,
          {
            credentials: 'include',
            signal: controller.signal,
          },
        )

        if (!response.ok) {
          throw new Error(
            `Historical performance request failed: ${response.status}`,
          )
        }

        const data =
          (await response.json()) as HistoricalPerformanceResponse

        setPerformance(data)
      } catch {
        // Ignore errors caused by cancelling an outdated request.
        if (!controller.signal.aborted) {
          setPerformanceError(
            'Kunde inte hämta historisk utveckling. Försök igen senare.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsPerformanceLoading(false)
        }
      }
    }

    void loadPerformance()

    // Cancel the previous request when the period changes or the page unmounts.
    return () => {
      controller.abort()
    }
  }, [periodId])

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
    portfolio?.allocationRows.map((row) => ({
      label: formatAccountType(row.accountType),
      percent: row.actual,
    })) ?? []

  // Map the user's target allocation returned by the backend.
  const targetAllocation: AllocationSlice[] =
    portfolio?.allocationRows.map((row) => ({
      label: formatAccountType(row.accountType),
      percent: row.target,
    })) ?? []

  return (
    <div className="p-6">
      <Title>Portföljanalys</Title>

      <div className="mx-auto mt-6 flex max-w-4xl flex-col gap-4">
        <DropdownBtn
          options={periodOptions}
          value={periodId}
          onChange={handlePeriodChange}
          className="self-start"
        />

        {/* Show historical performance from the backend instead of sample data. */}
        {isPerformanceLoading ? (
          <div
            className="rounded-2xl border border-neutral-200 bg-white p-5"
            role="status"
          >
            <p className="text-sm text-neutral-600">
              Hämtar historisk utveckling...
            </p>
          </div>
        ) : performanceError ? (
          <div
            className="rounded-2xl border border-neutral-200 bg-white p-5"
            role="alert"
          >
            <p className="text-sm text-red-700">{performanceError}</p>
          </div>
        ) : performance ? (
          <>
            <PerformanceCard
              points={performance.points}
              startValue={performance.startValue}
            />

            {/* Explain when historical data is incomplete or unavailable. */}
            {performance.warning && (
              <p className="text-sm text-amber-700" role="status">
                {performance.warning}
              </p>
            )}

            {/* Show which holdings lack historical price data, if any. */}
            {performance.unavailableTickers.length > 0 && (
              <p className="text-sm text-neutral-600">
                Historisk kursdata saknas för:{' '}
                {performance.unavailableTickers.join(', ')}.
              </p>
            )}
          </>
        ) : (
          <div className="rounded-2xl border border-neutral-200 bg-white p-5">
            <p className="text-sm text-neutral-500">
              Ingen historisk data tillgänglig.
            </p>
          </div>
        )}

        {/* Show the existing rebalance card with live allocation data. */}
        {portfolio && (
          <RebalanceCard
            current={currentAllocation}
            target={targetAllocation}
          />
        )}

        {/* Display loading, errors, or live portfolio information. */}
        {isLoading ? (
          <p className="text-sm text-neutral-600" role="status">
            Hämtar portföljdata...
          </p>
        ) : error ? (
          <p className="text-sm text-red-700" role="alert">
            {error}
          </p>
        ) : portfolio ? (
          <SummaryList
            rows={[
              {
                label: 'Värde totalt',
                value: formatSek(portfolio.totalPortfolioValue),
              },
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
        ) : null}
      </div>
    </div>
  )
}