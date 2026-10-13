import { useState } from 'react'
import { Title } from '../../Components/Title/Title.tsx'
import { SettingsSection } from '../../Components/SettingsSection/SettingsSection.tsx'
import { SettingsRow } from '../../Components/SettingsRow/SettingsRow.tsx'
import { Toggle } from '../../Components/Toggle/Toggle.tsx'
import { DropdownBtn } from '../../Components/DropdownBtn/DropdownBtn.tsx'
import {
  portfolioName,
  defaultCurrency,
  targetDistribution,
  deviationAlert,
  notificationPreferences,
  type CurrencyCode,
} from './settingsSampleData.ts'

const currencyOptions = [
  { label: 'SEK', value: 'SEK' },
  { label: 'USD', value: 'USD' },
  { label: 'EUR', value: 'EUR' },
]

const targetDistributionSummary = targetDistribution
  .map((slice) => `${slice.label} ${slice.percent}%`)
  .join(', ')

export const Settings = () => {
  const [currency, setCurrency] = useState<CurrencyCode>(defaultCurrency)
  const [deviationEnabled, setDeviationEnabled] = useState(deviationAlert.enabled)
  const [generalNotifications, setGeneralNotifications] = useState(
    notificationPreferences.generalNotifications,
  )
  const [dividendNotifications, setDividendNotifications] = useState(
    notificationPreferences.dividendNotifications,
  )
  const [quarterlyReports, setQuarterlyReports] = useState(
    notificationPreferences.quarterlyReports,
  )

  return (
    <div className="p-6">
      <Title>Anpassa Min Portfölj</Title>

      <div className="mt-6 flex max-w-2xl flex-col gap-6 mx-auto">
        <SettingsSection title="Allmänt">
          <SettingsRow
            label="Portföljnamn"
            control={
              <span className="flex items-center gap-2 text-sm text-neutral-700">
                {portfolioName}
                <i className="fa-solid fa-pen text-xs text-neutral-400" />
              </span>
            }
          />
          <SettingsRow
            label="Standardvaluta"
            control={
              <DropdownBtn
                options={currencyOptions}
                value={currency}
                onChange={(value) => setCurrency(value as CurrencyCode)}
              />
            }
          />
        </SettingsSection>

        <SettingsSection title="Portföljmål (Rebalansering)">
          <SettingsRow
            label="Redigera målfördelning"
            subtext={targetDistributionSummary}
            control={<i className="fa-solid fa-chevron-right text-xs text-neutral-400" />}
            onClick={() => {}}
          />
          <SettingsRow
            label="Notis vid avvikelse"
            subtext={`Varna om fördelning avviker mer än ${deviationAlert.thresholdPercent}%`}
            control={<Toggle checked={deviationEnabled} onChange={setDeviationEnabled} />}
          />
        </SettingsSection>

        <SettingsSection title="Aviseringar">
          <SettingsRow
            label="Generella notiser"
            control={<Toggle checked={generalNotifications} onChange={setGeneralNotifications} />}
          />
          <SettingsRow
            label="Utdelningsnotiser"
            control={<Toggle checked={dividendNotifications} onChange={setDividendNotifications} />}
          />
          <SettingsRow
            label="Kvartalsrapporter"
            control={<Toggle checked={quarterlyReports} onChange={setQuarterlyReports} />}
          />
        </SettingsSection>

        <SettingsSection title="Konto & Säkerhet">
          <SettingsRow
            label="Kopplade konton"
            control={<i className="fa-solid fa-chevron-right text-xs text-neutral-400" />}
            onClick={() => {}}
          />
          <SettingsRow
            label="Säkerhet"
            control={<i className="fa-solid fa-chevron-right text-xs text-neutral-400" />}
            onClick={() => {}}
          />
        </SettingsSection>

        <button type="button" className="self-start px-1 text-left text-sm font-medium text-red-600 hover:text-red-700">
          Logga ut
        </button>
      </div>
    </div>
  )
}
