import { Title } from '../../Components/Title/Title.tsx'
import { StatCard } from '../../Components/StatCard/StatCard.tsx'
import { SparklineChart } from '../../Components/SparklineChart/SparklineChart.tsx'
import { WariningBanner } from '../../Components/WariningBanner/WariningBanner.tsx'
import { AccountsTable } from '../../Components/AccountsTable/AccountsTable.tsx'
import { DonutChart } from '../../Components/DonutChart/DonutChart.tsx'
import { RecentActivity } from '../../Components/RecentActivity/RecentActivity.tsx'

const sampleTrend = [10, 12, 11, 14, 13, 16, 15, 18, 17, 20, 19, 23]

const sampleAccounts = [
  { name: 'Anna ISK', type: 'ISK', value: '161 824,7' },
  { name: 'Anna KF', type: 'KF', value: '38 620,0' },
  { name: 'Anna Depå', type: 'Depå', value: '63 840,0' },
]

const sampleAllocation = [
  { label: 'Aktier', value: 60, color: '#00c281' },
  { label: 'Räntor', value: 25, color: '#3b82f6' },
  { label: 'Övrigt', value: 15, color: '#8b5cf6' },
]

const sampleActivity = [
  { label: 'Köp av Volvo B', date: '2024-08-26', icon: 'fa-arrow-trend-up', color: '#3b82f6', category: 'Aktiehandel' as const },
  { label: 'Utdelning Ericsson B', date: '2024-08-24', icon: 'fa-sack-dollar', color: '#8b5cf6', category: 'Utdelningar' as const },
  { label: 'Rebalansering', date: '2024-08-20', icon: 'fa-rotate', color: '#3b82f6', category: 'Övrigt' as const },
]

export const Overview = () => {
  return (
    <div className="p-6">
      <Title>Översikt</Title>

      <div className="mt-6 flex max-w-4xl flex-col gap-4 mx-auto">
        <StatCard
          tone="accent"
          title="Totalt värde"
          value="264 284,7 SEK"
          delta={{ label: '1,23% idag', positive: true }}
          chart={<SparklineChart data={sampleTrend} />}
        />

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <StatCard title="Tillgängligt för köp" value="38 620,0 SEK" />
          <StatCard title="Totalt investerat" value="225 664,7 SEK" />
        </div>

        <WariningBanner message="En eller fler kontotyper avviker mer än 5% från mål fördelning." />

        <AccountsTable accounts={sampleAccounts} />

        <DonutChart data={sampleAllocation} />

        <RecentActivity items={sampleActivity} />
      </div>
    </div>
  )
}
