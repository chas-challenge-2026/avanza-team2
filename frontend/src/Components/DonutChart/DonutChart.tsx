import { Cell, Pie, PieChart, ResponsiveContainer } from 'recharts';

interface AllocationSlice {
  label: string;
  value: number;
  color: string;
}

interface DonutChartProps {
  title?: string;
  data: AllocationSlice[];
  centerLabel?: string;
  className?: string;
}

export const DonutChart = ({
  title = 'Aktiv fördelning',
  data,
  centerLabel = '100%',
  className,
}: DonutChartProps) => {
  return (
    <div
      className={`rounded-2xl border border-neutral-200 bg-white p-5${
        className ? ` ${className}` : ''
      }`}
    >
      <h3 className="text-lg font-bold text-neutral-900 text-left m-4">{title}</h3>

      <div className="m-4 flex items-center gap-6">
        <div className="relative h-36 w-36 flex-shrink-0">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={data}
                dataKey="value"
                nameKey="label"
                innerRadius="70%"
                outerRadius="100%"
                paddingAngle={2}
                isAnimationActive={false}
                stroke="none"
              >
                {data.map((slice) => (
                  <Cell key={slice.label} fill={slice.color} />
                ))}
              </Pie>
            </PieChart>
          </ResponsiveContainer>
          <div className="pointer-events-none absolute inset-0 flex items-center justify-center text-lg font-bold text-neutral-900">
            {centerLabel}
          </div>
        </div>

        <ul className="flex flex-1 flex-col gap-3">
          {data.map((slice) => (
            <li key={slice.label} className="flex items-center justify-between gap-4 text-sm">
              <span className="flex items-center gap-2 text-neutral-700">
                <span
                  className="h-2.5 w-2.5 rounded-full"
                  style={{ backgroundColor: slice.color }}
                />
                {slice.label}
              </span>
              <span className="font-semibold text-neutral-900">{slice.value}%</span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};
