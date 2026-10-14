interface SummaryRow {
  label: string;
  value: string;
  tone?: 'positive' | 'negative' | 'neutral';
}

interface SummaryListProps {
  title?: string;
  rows: SummaryRow[];
  className?: string;
}

const toneClass: Record<NonNullable<SummaryRow['tone']>, string> = {
  positive: 'text-green-600',
  negative: 'text-red-600',
  neutral: 'text-neutral-900',
};

export const SummaryList = ({ title = 'Total innehav', rows, className }: SummaryListProps) => {
  return (
    <div
      className={`rounded-2xl border border-neutral-200 bg-white p-5${
        className ? ` ${className}` : ''
      }`}
    >
      <h3 className="text-lg font-bold text-neutral-900 text-left m-4">{title}</h3>

      <dl className="mx-4 mb-4 flex flex-col divide-y divide-neutral-200">
        {rows.map((row) => (
          <div key={row.label} className="flex items-center justify-between py-3 text-sm first:pt-0 last:pb-0">
            <dt className="text-neutral-600">{row.label}</dt>
            <dd className={`font-semibold ${toneClass[row.tone ?? 'neutral']}`}>{row.value}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
};
