import { Cell, Pie, PieChart, ResponsiveContainer } from 'recharts'

interface AllocationSlice {
  label: string
  value: number
  color: string
}

interface SpreadRow {
  label: string
  value: number
}

interface DistributionSpreadCardProps {
  title?: string
  allocation: AllocationSlice[]
  spread: SpreadRow[]
  className?: string
}

// TODO: mock data — backend has no asset-type/country/sector fields on Holdings yet.
// Replace allocation/spread props with real data once that's added.
export const DistributionSpreadCard = ({
  title = 'Fördelning & Spridning',
  allocation,
  spread,
  className,
}: DistributionSpreadCardProps) => {
  const centerLabel = allocation.map((slice) => Math.round(slice.value)).join(' / ')

  return (
    <div
      className={`rounded-2xl border border-neutral-200 bg-white p-5${
        className ? ` ${className}` : ''
      }`}
    >
      <h3 className="mb-4 text-left text-lg font-bold text-neutral-900">{title}</h3>

      <div className="flex items-center gap-6">
        <div className="relative h-32 w-32 flex-shrink-0">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={allocation}
                dataKey="value"
                nameKey="label"
                innerRadius="72%"
                outerRadius="100%"
                paddingAngle={2}
                isAnimationActive={false}
                stroke="none"
              >
                {allocation.map((slice) => (
                  <Cell key={slice.label} fill={slice.color} />
                ))}
              </Pie>
            </PieChart>
          </ResponsiveContainer>
          <div className="pointer-events-none absolute inset-0 flex items-center justify-center text-base font-bold text-neutral-900">
            {centerLabel}
          </div>
        </div>

        <ul className="flex flex-1 flex-col gap-3">
          {allocation.map((slice) => (
            <li key={slice.label} className="flex items-center justify-between gap-4 text-sm">
              <span className="flex items-center gap-2 text-neutral-700">
                <span
                  className="h-2.5 w-2.5 rounded-full"
                  style={{ backgroundColor: slice.color }}
                />
                {slice.label}
              </span>
              <span className="font-semibold text-neutral-900">{Math.round(slice.value)}%</span>
            </li>
          ))}
        </ul>
      </div>

      <div className="mt-5 flex flex-col gap-4 border-t border-neutral-100 pt-4">
        {spread.map((row) => (
          <div key={row.label}>
            <div className="mb-1.5 flex items-center justify-between text-sm">
              <span className="text-neutral-500">{row.label}</span>
              <span className="font-semibold text-neutral-900">{Math.round(row.value)}%</span>
            </div>
            <div className="h-1.5 w-full overflow-hidden rounded-full bg-neutral-100">
              <div
                className="h-full rounded-full bg-neutral-800"
                style={{ width: `${Math.min(100, Math.max(0, row.value))}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
