import { AllocationRing } from "../AllocationRing/AllocationRing";
import { Button } from "../Button/Button";
import { formatDecimal } from "../../utils/format";

interface AllocationSlice {
  label: string;
  percent: number;
}

interface RebalanceCardProps {
  current: AllocationSlice[];
  target: AllocationSlice[];
  thresholdPct?: number;
  showDriftAlert?: boolean;
  onRebalance?: () => void;
  className?: string;
}

interface Drift {
  label: string;
  currentPct: number;
  targetPct: number;
  diffPct: number;
}

// Format percentages consistently using Swedish number formatting.
const percentageFormatter = new Intl.NumberFormat("sv-SE", {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

const formatPercentage = (value: number) =>
  `${percentageFormatter.format(value)}%`;

// Format deviations with a sign and a percentage symbol.
const formatDifference = (value: number) => {
  if (Math.abs(value) < 0.005) {
    return "0%";
  }

  const sign = value > 0 ? "+" : "−";

  return `${sign}${formatDecimal(Math.abs(value))}%`;
};

// Find the account with the largest deviation from its target.
const biggestDrift = (
  current: AllocationSlice[],
  target: AllocationSlice[],
): Drift | null => {
  let biggest: Drift | null = null;

  for (const targetSlice of target) {
    const currentSlice = current.find(
      (slice) => slice.label === targetSlice.label,
    );

    const currentPct = currentSlice?.percent ?? 0;
    const diffPct = currentPct - targetSlice.percent;

    if (!biggest || Math.abs(diffPct) > Math.abs(biggest.diffPct)) {
      biggest = {
        label: targetSlice.label,
        currentPct,
        targetPct: targetSlice.percent,
        diffPct,
      };
    }
  }

  return biggest;
};

export const RebalanceCard = ({
  current,
  target,
  thresholdPct = 5,
  showDriftAlert = true,
  onRebalance,
  className,
}: RebalanceCardProps) => {
  const drift = biggestDrift(current, target);

  const needsRebalance =
    drift !== null && Math.abs(drift.diffPct) > thresholdPct;

  // Build rows from the target allocation to preserve a consistent order.
  const allocationRows: Drift[] = target.map((targetSlice) => {
    const currentSlice = current.find(
      (slice) => slice.label === targetSlice.label,
    );

    const currentPct = currentSlice?.percent ?? 0;

    return {
      label: targetSlice.label,
      currentPct,
      targetPct: targetSlice.percent,
      diffPct: currentPct - targetSlice.percent,
    };
  });

  const message =
    needsRebalance && drift
      ? `Största avvikelsen gäller ${drift.label}. Din nuvarande andel är ${formatPercentage(
          drift.currentPct,
        )}, jämfört med målet på ${formatPercentage(
          drift.targetPct,
        )}. Skillnaden är ${formatDecimal(
          Math.abs(drift.diffPct),
        )} procentenheter.`
      : "Ingen enskild avvikelse överstiger den angivna gränsen just nu.";

  return (
    <div
      className={`rounded-2xl border border-neutral-200 bg-white p-5 ${
        className ?? ""
      }`}
    >
      {/* 1. Ny rubrik */}
      <h3 className="text-lg font-bold text-neutral-900 text-left m-4">Fördelning & Mål</h3>
      <h3 className="mb-6 text-lg font-bold text-neutral-900">
        Rebalanseringshjälp
      </h3>

      {/* Keep the existing portfolio and target donut charts. */}
      <div className="mb-8 grid grid-cols-2 gap-4">
        <AllocationRing
          title="Din portfölj"
          slices={current}
          showCaption={false}
        />

        <AllocationRing title="Ditt mål" slices={target} showCaption={false} />
      </div>

      {/* 2. Alert-ruta för texten */}
      {showDriftAlert && (
        needsRebalance ? (
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
        )
      )}
      {/* Same width and inner padding as the boxes below, so edges line up. */}
      <div className="mx-auto w-full max-w-5xl overflow-x-auto px-4">
        <table className="w-full table-fixed border-collapse text-sm">
          <colgroup>
            <col style={{ width: "31%" }} />
            <col style={{ width: "31%" }} />
            <col style={{ width: "19%" }} />
            <col style={{ width: "19%" }} />
          </colgroup>

          <thead>
            <tr className="border-b border-neutral-200 text-neutral-500">
              <th className="py-3 pr-2 text-left font-medium">Konto</th>

              <th className="px-2 py-3 text-left font-medium">Nuvarande</th>

              <th className="px-2 py-3 text-left font-medium">Mål</th>

              <th className="px-2 py-3 text-right font-medium">Avvikelse</th>
            </tr>
          </thead>

          <tbody>
            {allocationRows.map((row) => {
              const isOverweight = row.diffPct > 0.005;
              const isUnderweight = row.diffPct < -0.005;

              return (
                <tr
                  key={row.label}
                  className="border-b border-neutral-100 last:border-0"
                >
                  <td className="py-3 pr-2 text-left font-medium text-neutral-900">
                    {row.label}
                  </td>

                  <td className="px-2 py-3 text-left tabular-nums text-neutral-700">
                    {formatPercentage(row.currentPct)}
                  </td>

                  <td className="px-2 py-3 text-left tabular-nums text-neutral-700">
                    {formatPercentage(row.targetPct)}
                  </td>

                  <td
                    className={`px-2 py-3 text-right font-medium tabular-nums ${
                      isOverweight
                        ? "text-amber-700"
                        : isUnderweight
                          ? "text-blue-700"
                          : "text-neutral-500"
                    }`}
                  >
                    {formatDifference(row.diffPct)}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Explain how to interpret the allocation deviations. */}
      <div className="mx-auto mt-5 w-full max-w-5xl rounded-xl bg-neutral-50 p-4">
        <h4 className="mb-2 text-sm font-semibold text-neutral-900">
          Så tolkar du avvikelsen
        </h4>

        <p className="text-sm leading-6 text-neutral-600">
          Avvikelsen visar skillnaden mellan din nuvarande fördelning och ditt
          mål. Exempelvis är skillnaden mellan 24,8% och 15% 9,8 procentenheter.
        </p>

        <div className="mt-3 flex flex-wrap justify-center gap-x-5 gap-y-2 text-sm">
          <div className="flex items-center gap-2">
            <span
              className="h-2.5 w-2.5 shrink-0 rounded-full bg-blue-700"
              aria-hidden="true"
            />

            <span className="text-neutral-700">
              Under mål: andelen är lägre än önskat.
            </span>
          </div>

          <div className="flex items-center gap-2">
            <span
              className="h-2.5 w-2.5 shrink-0 rounded-full bg-amber-700"
              aria-hidden="true"
            />

            <span className="text-neutral-700">
              Över mål: andelen är högre än önskat.
            </span>
          </div>

          <div className="flex items-center gap-2">
            <span
              className="h-2.5 w-2.5 shrink-0 rounded-full bg-neutral-400"
              aria-hidden="true"
            />

            <span className="text-neutral-700">
              I linje med mål: ingen betydande avvikelse.
            </span>
          </div>
        </div>
      </div>

      {/* Keep the action below the allocation details without repeating the alert text. */}
      {needsRebalance && (
        <div className="mx-auto mt-4 flex w-full max-w-5xl justify-center">
            <Button
              label="Balansera om portföljen"
              onClick={onRebalance ?? (() => {})}
              pill
              className="px-6 py-3"
            />
        </div>
      )}
    </div>
  );
};