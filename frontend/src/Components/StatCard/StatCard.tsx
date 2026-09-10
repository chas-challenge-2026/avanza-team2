import type { ReactNode } from 'react';

interface StatCardDelta {
  label: string;
  positive?: boolean;
}

interface StatCardProps {
  title: string;
  value: string;
  delta?: StatCardDelta;
  chart?: ReactNode;
  tone?: 'default' | 'accent';
  className?: string;
}

export const StatCard = ({
  title,
  value,
  delta,
  chart,
  tone = 'default',
  className,
}: StatCardProps) => {
  const toneStyles =
    tone === 'accent'
      ? 'bg-[#eafaf3] border-[#d3f3e6]'
      : 'bg-white border-neutral-200';

  return (
    <div
      className={`flex items-center justify-between gap-4 rounded-2xl border p-5${
        className ? ` ${className}` : ''
      } ${toneStyles}`}
    >
      <div className="flex flex-col gap-2 m-4 text-left">
        <span className="text-sm font-bold text-neutral-500">{title}</span>
        <span className="text-2xl font-bold text-black">{value}</span>
        {delta && (
          <span
            className={`flex items-center gap-1 text-sm font-medium ${
              delta.positive ? 'text-green-600' : 'text-red-600'
            }`}
          >
            <i
              className={`fa-solid fa-arrow-up-long${
                delta.positive ? '' : ' rotate-180'
              }`}
            ></i>
            {delta.label}
          </span>
        )}
      </div>

      {chart && <div className="h-16 w-28 flex-shrink-0 px-4">{chart}</div>}
    </div>
  );
};
