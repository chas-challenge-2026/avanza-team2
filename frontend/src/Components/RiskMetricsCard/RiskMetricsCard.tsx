import { useState } from 'react'

interface RiskMetricItemProps {
  label: string
  sublabel: string
  value: string
  explanation: string
}

const RiskMetricItem = ({ label, sublabel, value, explanation }: RiskMetricItemProps) => {
  const [showOverlay, setShowOverlay] = useState(false)

  return (
    <div className="relative flex flex-col justify-between p-3.5 rounded-xl bg-neutral-50 border border-neutral-200/80 text-left">
      <div>
        <div className="flex items-center justify-between mb-1">
          <span className="text-xs font-bold text-neutral-900">{label}</span>
          <button
            type="button"
            onClick={() => setShowOverlay(!showOverlay)}
            className="text-neutral-500 hover:text-neutral-800 text-[11px] font-bold rounded-full w-4 h-4 flex items-center justify-center border border-neutral-300 bg-white shadow-sm"
            title="Visa förklaring"
          >
            ?
          </button>
        </div>
        <span className="block text-[11px] text-neutral-500 mb-2">{sublabel}</span>
      </div>

      <span className="text-lg font-extrabold text-neutral-900">{value}</span>

      {/* Overlay som öppnas vid klick på '?' */}
      {showOverlay && (
        <div className="absolute inset-0 bg-neutral-900/95 text-white p-3 rounded-xl text-xs z-10 flex flex-col justify-between">
          <p className="leading-relaxed">{explanation}</p>
          <button
            type="button"
            onClick={() => setShowOverlay(false)}
            className="self-end text-[11px] text-neutral-300 underline mt-1"
          >
            Stäng
          </button>
        </div>
      )}
    </div>
  )
}

interface RiskMetricsCardProps {
  volatility: number
  maxDrawdown: number
  sharpeRatio: number
  periodLabel?: string
}

export const RiskMetricsCard = ({
  volatility,
  maxDrawdown,
  sharpeRatio,
  periodLabel = '5 år',
}: RiskMetricsCardProps) => {
  return (
    <div className="rounded-2xl border border-neutral-200 bg-white p-5 text-left shadow-sm">
      <h3 className="text-base font-bold text-neutral-900 mb-4">
        Risk & Svängningar ({periodLabel})
      </h3>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <RiskMetricItem
          label="Svängningar"
          sublabel="Volatilitet"
          value={`${volatility.toString().replace('.', ',')}%`}
          explanation="Visar hur mycket din portfölj brukar svänga upp och ner i värde. Högre siffra innebär mer svängningar."
        />
        <RiskMetricItem
          label="Största fall"
          sublabel="Max Drawdown"
          value={`${maxDrawdown.toString().replace('.', ',')}%`}
          explanation="Det mesta din portfölj har fallit i värde från sin högsta punkt under valda perioden."
        />
        <RiskMetricItem
          label="Riskeffektivitet"
          sublabel="Sharpe-kvot"
          value={sharpeRatio.toString().replace('.', ',')}
          explanation="Mäter hur mycket avkastning du fått i förhållande till risken du tagit. Ett värde över 1,0 anses bra."
        />
      </div>
    </div>
  )
}