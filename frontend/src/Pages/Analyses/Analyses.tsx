import { useState } from 'react'
import { Title } from '../../Components/Title/Title.tsx'
import { DropdownBtn } from '../../Components/DropdownBtn/DropdownBtn.tsx'
import { PerformanceCard } from '../../Components/PerformanceCard/PerformanceCard.tsx'
import { RebalanceCard } from '../../Components/RebalanceCard/RebalanceCard.tsx'
import { SummaryList } from '../../Components/SummaryList/SummaryList.tsx'
import { formatSek, formatSignedPercent, formatSignedSek } from '../../utils/format.ts'
import {
  currentAllocation,
  defaultPeriodId,
  holdingsSummary,
  performancePeriods,
  targetAllocation,
  type PeriodId,
} from './analysesSampleData.ts'

const periodOptions = performancePeriods.map(({ id, label }) => ({ label, value: id }))

export const Analyses = () => {
  const [periodId, setPeriodId] = useState<PeriodId>(defaultPeriodId)
  const period = performancePeriods.find((p) => p.id === periodId) ?? performancePeriods[0]
  const gainSek = holdingsSummary.totalValue - holdingsSummary.totalInvested
  const gainPercent = (gainSek / holdingsSummary.totalInvested) * 100

  const handlePeriodChange = (value: string) => {
    const selected = performancePeriods.find((p) => p.id === value)
    if (selected) {
      setPeriodId(selected.id)
    }
  }

  return (
    <div className="p-6">
      <Title>Portföljanalys</Title>

      <div className="mt-6 flex max-w-4xl flex-col gap-4 mx-auto">
        <DropdownBtn
          options={periodOptions}
          value={periodId}
          onChange={handlePeriodChange}
          className="self-start"
        />

        <PerformanceCard points={period.points} startValue={period.startValue} />

        <RebalanceCard current={currentAllocation} target={targetAllocation} />

        <SummaryList
          rows={[
            { label: 'Värde totalt', value: formatSek(holdingsSummary.totalValue) },
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
