import { useId } from 'react';
import { Area, AreaChart, ReferenceDot, ResponsiveContainer, XAxis, YAxis } from 'recharts';
import { formatDecimal, formatSignedPercent, formatSignedSek } from '../../utils/format';

interface PerformancePoint {
  date: string;
  portfolioPct: number;
  indexPct: number;
}

interface PerformanceCardProps {
  points: PerformancePoint[];
  startValue: number;
  title?: string;
  indexName?: string;
  className?: string;
}

const POSITIVE_COLOR = '#00c281';
const NEGATIVE_COLOR = '#ef4444';

const describeDifference = (diff: number) => {
  if (diff > 0) {
    return `Du slår index med ${formatDecimal(diff)} procentenheter.`;
  }
  if (diff < 0) {
    return `Du ligger ${formatDecimal(-diff)} procentenheter efter index.`;
  }
  return 'Du följer index.';
};

export const PerformanceCard = ({
  points,
  startValue,
  title = 'Portföljutveckling',
  indexName = 'OMXS30',
  className,
}: PerformanceCardProps) => {
  const gradientId = useId();
  const last = points[points.length - 1];
  const cardClass = `rounded-2xl border border-neutral-200 bg-white p-5${
    className ? ` ${className}` : ''
  }`;

  if (!last) {
    return (
      <div className={cardClass}>
        <div className="m-4 text-left">
          <h3 className="text-lg font-bold text-neutral-900">{title}</h3>
          <p className="mt-2 text-sm text-neutral-500">Ingen data för vald period.</p>
        </div>
      </div>
    );
  }

  const positive = last.portfolioPct >= 0;
  const color = positive ? POSITIVE_COLOR : NEGATIVE_COLOR;
  const gainSek = (startValue * last.portfolioPct) / 100;
  const headline = `${formatSignedPercent(last.portfolioPct)} (${formatSignedSek(gainSek)})`;
  const diff = Math.round((last.portfolioPct - last.indexPct) * 10) / 10;
  const indexSummary = `Jämförelseindex (${indexName}): ${formatSignedPercent(last.indexPct)}. ${describeDifference(diff)}`;

  return (
    <div className={cardClass}>
      <div className="m-4 text-left">
        <h3 className="text-lg font-bold text-neutral-900">{title}</h3>
        <p className={`mt-1 text-2xl font-bold ${positive ? 'text-green-600' : 'text-red-600'}`}>
          {headline}
        </p>

        <div role="img" aria-label={`${title}: ${headline}. ${indexSummary}`} className="mt-4 h-40">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart
              data={points}
              margin={{ top: 10, right: 8, bottom: 4, left: 4 }}
              accessibilityLayer={false}
            >
              <defs>
                <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor={color} stopOpacity={0.3} />
                  <stop offset="100%" stopColor={color} stopOpacity={0} />
                </linearGradient>
              </defs>
              <XAxis dataKey="date" hide />
              <YAxis hide domain={['dataMin', 'dataMax']} />
              <Area
                type="monotone"
                dataKey="portfolioPct"
                stroke={color}
                strokeWidth={2}
                fill={`url(#${gradientId})`}
                baseValue="dataMin"
                dot={false}
                isAnimationActive={false}
              />
              <ReferenceDot
                x={last.date}
                y={last.portfolioPct}
                r={5}
                fill={color}
                stroke="#ffffff"
                strokeWidth={2}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <p className="mt-3 text-sm text-neutral-500">{indexSummary}</p>
      </div>
    </div>
  );
};
