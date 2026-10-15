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

// Use the same API configuration as the Overview page.
const API_URL = import.meta.env.VITE_API_URL ?? ''

// Define the portfolio response returned by the backend.
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

  // Store the portfolio data received from the backend.
  const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null)

  // Track loading and API errors.
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Keep the existing historical performance data for now.
  const period =
    performancePeriods.find((p) => p.id === periodId) ??
    performancePeriods[0]

  useEffect(() => {
    const controller = new AbortController()

    const loadPortfolio = async () => {
      try {
        // Fetch the authenticated user's portfolio.
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

    // Cancel the request when the component unmounts.
    return () => {
      controller.abort()
    }
  }, [])

  const handlePeriodChange = (value: string) => {
    const selected = performancePeriods.find((p) => p.id === value)

    if (selected) {
      setPeriodId(selected.id)
    }
  }

  // Calculate the invested amount from holdings with complete data.
  const investedValue =
    portfolio?.enrichedHoldings.reduce((total, holding) => {
      if (
        holding.valueSek === null ||
        holding.unrealizedReturn === null
      ) {
        return total
      }

      return total + holding.valueSek - holding.unrealizedReturn
    }, 0) ?? 0

  // Calculate the total unrealized return from the available holdings.
  const gainSek =
    portfolio?.enrichedHoldings.reduce((total, holding) => {
      if (holding.unrealizedReturn === null) {
        return total
      }

      return total + holding.unrealizedReturn
    }, 0) ?? 0

  // Calculate the return percentage using the invested amount.
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

        {/* Historical performance remains unchanged for now. */}
        <PerformanceCard
          points={period.points}
          startValue={period.startValue}
        />

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