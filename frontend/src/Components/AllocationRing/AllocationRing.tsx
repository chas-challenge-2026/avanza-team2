import { Cell, Pie, PieChart, ResponsiveContainer } from 'recharts';

interface AllocationSlice {
  label: string;
  percent: number;
}

interface AllocationRingProps {
  title: string;
  slices: AllocationSlice[];
  className?: string;
}

const SLICE_COLORS = ['#00c281', '#e5e7eb', '#3b82f6', '#8b5cf6'];

const colorFor = (index: number) => SLICE_COLORS[index % SLICE_COLORS.length];

export const AllocationRing = ({ title, slices, className }: AllocationRingProps) => {
  const caption = slices.map((slice) => `${slice.label} ${slice.percent}%`).join(' / ');

  return (
    <div
      className={`flex min-w-0 flex-col items-center text-center${className ? ` ${className}` : ''}`}
    >
      <p className="text-base font-semibold text-neutral-900">{title}</p>

      <div aria-hidden="true" className="mt-3 flex w-full flex-col items-center">
        <div className="w-full max-w-36">
          <ResponsiveContainer width="100%" aspect={1}>
            <PieChart accessibilityLayer={false}>
              <Pie
                data={slices}
                dataKey="percent"
                nameKey="label"
                innerRadius="70%"
                outerRadius="100%"
                startAngle={90}
                endAngle={-270}
                stroke="none"
                rootTabIndex={-1}
                isAnimationActive={false}
              >
                {slices.map((slice, index) => (
                  <Cell key={slice.label} fill={colorFor(index)} />
                ))}
              </Pie>
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="mt-3 flex h-1.5 w-24 max-w-full overflow-hidden rounded-full">
          {slices.map((slice, index) => (
            <div
              key={slice.label}
              style={{ width: `${slice.percent}%`, backgroundColor: colorFor(index) }}
            />
          ))}
        </div>
      </div>

      <p className="mt-3 text-sm text-neutral-700">{caption}</p>
    </div>
  );
};
