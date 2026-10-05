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
      <h3 className="text-lg font-bold text-neutral-900 text-left m-4">Rebalanseringshjälp</h3>

      <div className="m-4 grid grid-cols-2 gap-4">
        <AllocationRing title="Din Portfölj" slices={current} />
        <AllocationRing title="Ditt Mål" slices={target} />
      </div>

      <p className="m-4 text-left text-sm text-neutral-600">{message}</p>

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
