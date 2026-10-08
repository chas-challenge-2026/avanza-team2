import { useEffect, useState } from 'react'
import { Title } from '../../Components/Title/Title.tsx'
import { StatCard } from '../../Components/StatCard/StatCard.tsx'
import { WariningBanner } from '../../Components/WariningBanner/WariningBanner.tsx'
import { AccountsTable } from '../../Components/AccountsTable/AccountsTable.tsx'
import { DonutChart } from '../../Components/DonutChart/DonutChart.tsx'
import { RecentActivity } from '../../Components/RecentActivity/RecentActivity.tsx'

const API_URL = import.meta.env.VITE_API_URL ?? ''
const POLL_INTERVAL_MS = 24 * 60 * 60 * 1000

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
  recentAlerts: {
    id: number
    user: number
    message: string
    dismissed: boolean
    createdAt: string
  }[]
  anyDrift: boolean
  usdToSek: number
}

interface AlertsResponse {
  storedAlerts: {
    content: {
      id: number
      user: number
      message: string
      dismissed: boolean
      createdAt: string
    }[]
  }
  liveAlerts: {
    alertType: string
    message: string
    dismissed: boolean
    createdAt: string
  }[]
  driftThreshold: number
}

const numberFormatter = new Intl.NumberFormat('sv-SE', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

const allocationColors: Record<string, string> = {
  ISK: '#059669',
  KF: '#2563eb',
  Depa: '#d97706',
}

const formatAccountType = (accountType: string) =>
  accountType === 'Depa' ? 'Depå' : accountType

const formatDate = (value: string) => {
  if (value === 'Nu') {
    return 'Nu'
  }

  const date = new Date(value.replace(' ', 'T'))

  return Number.isNaN(date.getTime())
    ? value
    : date.toLocaleDateString('sv-SE')
}

export const Overview = () => {
  const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null)
  const [alerts, setAlerts] = useState<AlertsResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    let timeoutId: number | undefined

    const loadPortfolio = async () => {
      try {
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

        try {
          const alertsResponse = await fetch(`${API_URL}/api/alerts`, {
            credentials: 'include',
            signal: controller.signal,
          })

          if (alertsResponse.ok) {
            const alertsData = (await alertsResponse.json()) as AlertsResponse
            setAlerts(alertsData)
          }
        } catch {
          if (!controller.signal.aborted) {
            console.warn('Kunde inte hämta alerts.')
          }
        }
      } catch {
        if (!controller.signal.aborted) {
          setError(
            'Kunde inte hämta portföljen. Kontrollera anslutningen och försök igen.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false)
          timeoutId = window.setTimeout(
            () => void loadPortfolio(),
            POLL_INTERVAL_MS,
          )
        }
      }
    }

    void loadPortfolio()

    return () => {
      controller.abort()
      if (timeoutId !== undefined) {
        window.clearTimeout(timeoutId)
      }
    }
  }, [])

  const formatSek = (value: number) => `${numberFormatter.format(value)} SEK`

  if (isLoading) {
    return (
      <div className="p-6" role="status">
        <Title>Översikt</Title>
        <p className="mx-auto mt-6 max-w-4xl text-sm text-neutral-600">
          Hämtar portfölj...
        </p>
      </div>
    )
  }

  if (!portfolio) {
    return (
      <div className="p-6" role="alert">
        <Title>Översikt</Title>
        <p className="mx-auto mt-6 max-w-4xl text-sm text-red-700">
          {error ?? 'Portföljdata saknas.'}
        </p>
      </div>
    )
  }

  const investedValue = portfolio.enrichedHoldings.reduce(
    (total, holding) => {
      if (holding.valueSek === null || holding.unrealizedReturn === null) {
        return total
      }

      return total + holding.valueSek - holding.unrealizedReturn
    },
    0,
  )

  const driftedAccountTypes = portfolio.allocationRows
    .filter((row) => row.overThreshold)
    .map((row) => formatAccountType(row.accountType))

  const accounts = portfolio.accountSummary.map((account) => ({
    name: account.accountName,
    type: formatAccountType(account.accountType),
    value: numberFormatter.format(account.totalValueSek),
  }))

  const allocation = portfolio.allocationRows
    .filter((row) => row.accountType !== 'Pension')
    .map((row) => ({
      label: formatAccountType(row.accountType),
      value: row.actual,
      color: allocationColors[row.accountType] ?? '#64748b',
    }))

  const liveActivities =
    alerts?.liveAlerts
      .filter((alert) => !alert.dismissed)
      .map((alert) => ({
        label: alert.message,
        date: formatDate(alert.createdAt),
        icon: 'fa-bell',
        color: '#d97706',
        category: 'Övrigt' as const,
      })) ?? []

  const storedActivities =
    alerts?.storedAlerts.content
      .filter((alert) => !alert.dismissed)
      .map((alert) => ({
        label: alert.message,
        date: formatDate(alert.createdAt),
        icon: 'fa-bell',
        color: '#d97706',
        category: 'Övrigt' as const,
      })) ?? []

  const activity = [...liveActivities, ...storedActivities].slice(0, 3)

  return (
    <div className="p-6">
      <Title>Översikt</Title>

      <div className="mx-auto mt-6 flex max-w-4xl flex-col gap-4">
        {error && (
          <p className="text-sm text-red-700" role="status">
            {error} Visar senast hämtade data.
          </p>
        )}

        <StatCard
          tone="accent"
          title="Totalt värde"
          value={formatSek(portfolio.totalPortfolioValue)}
        />

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <StatCard title="Totalt investerat" value={formatSek(investedValue)} />
          <StatCard title="USD/SEK" value={numberFormatter.format(portfolio.usdToSek)} />
        </div>

        {portfolio.anyDrift && (
          <WariningBanner
            message={
              driftedAccountTypes.length > 0
                ? `${driftedAccountTypes.join(', ')} avviker mer än tillåtet från målfördelningen.`
                : 'En eller flera kontotyper avviker mer än tillåtet från målfördelningen.'
            }
          />
        )}

        <AccountsTable accounts={accounts} />

        <DonutChart
          title="Fördelning per kontotyp"
          data={allocation}
          centerLabel={`${numberFormatter.format(
            allocation.reduce((total, slice) => total + slice.value, 0),
          )}%`}
        />

        <RecentActivity title="Senaste aviseringar" items={activity} />
      </div>
    </div>
  )
}