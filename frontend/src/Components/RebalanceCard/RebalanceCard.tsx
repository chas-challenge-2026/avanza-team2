import { AllocationRing } from '../AllocationRing/AllocationRing';
import { Button } from '../Button/Button';
import { formatDecimal } from '../../utils/format';

interface AllocationSlice {
  label: string;
  percent: number;
}

interface RebalanceCardProps {
  current: AllocationSlice[];
  target: AllocationSlice[];
  /** Percentage points of drift on any single slice before a rebalance is suggested. */
  thresholdPct?: number;
  onRebalance?: () => void;
  className?: string;
}

interface Drift {
  label: string;
  currentPct: number;
  targetPct: number;
  diffPct: number;
}

const biggestDrift = (current: AllocationSlice[], target: AllocationSlice[]): Drift | null => {
  let biggest: Drift | null = null;

  for (const targetSlice of target) {
    const currentSlice = current.find((slice) => slice.label === targetSlice.label);
    const currentPct = currentSlice?.percent ?? 0;
    const diffPct = currentPct - targetSlice.percent;

    if (!biggest || Math.abs(diffPct) > Math.abs(biggest.diffPct)) {
      biggest = { label: targetSlice.label, currentPct, targetPct: targetSlice.percent, diffPct };
    }
  }

  return biggest;
};

export const RebalanceCard = ({
  current,
  target,
  thresholdPct = 5,
  onRebalance,
  className,
}: RebalanceCardProps) => {
  const drift = biggestDrift(current, target);
  const needsRebalance = drift !== null && Math.abs(drift.diffPct) > thresholdPct;

  const message = needsRebalance && drift
    ? `Din andel ${drift.label.toLowerCase()} har ${
        drift.diffPct > 0 ? 'ökat' : 'minskat'
      } till ${drift.currentPct}% (mål ${drift.targetPct}%), en avvikelse på ${formatDecimal(
        Math.abs(drift.diffPct),
      )} procentenheter. Vill du balansera om?`
    : 'Din portfölj matchar ditt mål. Ingen ombalansering behövs just nu.';

  return (
    <div
      className={`rounded-2xl border border-neutral-200 bg-white p-5${
        className ? ` ${className}` : ''
      }`}
    >
      {/* 1. Ny rubrik */}
      <h3 className="text-lg font-bold text-neutral-900 text-left m-4">Fördelning & Mål</h3>

      <div className="m-4 grid grid-cols-2 gap-4">
        <AllocationRing title="Din Portfölj" slices={current} />
        <AllocationRing title="Ditt Mål" slices={target} />
      </div>

      {/* 2. Alert-ruta för texten */}
      {needsRebalance ? (
        <div className="m-4 p-4 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 text-sm text-left flex items-start gap-3">
          <span className="text-amber-600 font-bold text-base leading-none">⚠️</span>
          <div>
            <p className="font-bold text-amber-900 mb-0.5">Avvikelse upptäckt</p>
            <p className="text-amber-800 leading-relaxed">{message}</p>
          </div>
        </div>
      ) : (
        <div className="m-4 p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-900 text-sm text-left flex items-center gap-3">
          <span className="text-emerald-600 font-bold text-base leading-none">✓</span>
          <p className="text-emerald-800 font-medium">{message}</p>
        </div>
      )}

      {needsRebalance && (
        <div className="m-4 flex justify-center">
          <Button
            label="Balansera om portföljen"
            onClick={onRebalance ?? (() => {})}
            pill
          />
        </div>
      )}
    </div>
  );
};