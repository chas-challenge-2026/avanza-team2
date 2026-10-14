export type PeriodId = '1m' | '3m' | '1y';

export interface PerformancePoint {
  date: string;
  portfolioPct: number;
  indexPct: number;
}

export interface PerformancePeriod {
  id: PeriodId;
  label: string;
  startValue: number;
  points: PerformancePoint[];
}

export const defaultPeriodId: PeriodId = '1y';

// Cumulative % since the period start, so portfolio and OMXS30 both begin at 0.
export const performancePeriods: PerformancePeriod[] = [
  {
    id: '1m',
    label: 'Senaste månaden',
    startValue: 256338,
    points: [
      { date: '2026-08-24', portfolioPct: 0, indexPct: 0 },
      { date: '2026-08-31', portfolioPct: 0.9, indexPct: 0.2 },
      { date: '2026-09-07', portfolioPct: 0.4, indexPct: -0.2 },
      { date: '2026-09-14', portfolioPct: 1.8, indexPct: 0.5 },
      { date: '2026-09-21', portfolioPct: 2.3, indexPct: 0.6 },
      { date: '2026-09-24', portfolioPct: 3.1, indexPct: 0.9 },
    ],
  },
  {
    id: '3m',
    label: 'Senaste 3 månaderna',
    startValue: 248388,
    points: [
      { date: '2026-06-24', portfolioPct: 0, indexPct: 0 },
      { date: '2026-07-08', portfolioPct: 1.2, indexPct: 0.4 },
      { date: '2026-07-22', portfolioPct: 0.5, indexPct: -0.3 },
      { date: '2026-08-05', portfolioPct: 2.8, indexPct: 0.8 },
      { date: '2026-08-19', portfolioPct: 2.1, indexPct: 0.5 },
      { date: '2026-09-02', portfolioPct: 4.0, indexPct: 1.4 },
      { date: '2026-09-16', portfolioPct: 5.1, indexPct: 1.7 },
      { date: '2026-09-24', portfolioPct: 6.4, indexPct: 2.1 },
    ],
  },
  {
    id: '1y',
    label: 'Senaste året',
    startValue: 231423,
    points: [
      { date: '2025-09-24', portfolioPct: 0, indexPct: 0 },
      { date: '2025-10-24', portfolioPct: 1.4, indexPct: 0.6 },
      { date: '2025-11-24', portfolioPct: 0.6, indexPct: -0.9 },
      { date: '2025-12-24', portfolioPct: 2.9, indexPct: 0.4 },
      { date: '2026-01-24', portfolioPct: 2.2, indexPct: 1.2 },
      { date: '2026-02-24', portfolioPct: 4.1, indexPct: 0.2 },
      { date: '2026-03-24', portfolioPct: 3.4, indexPct: 1.9 },
      { date: '2026-04-24', portfolioPct: 6.0, indexPct: 2.6 },
      { date: '2026-05-24', portfolioPct: 5.1, indexPct: 2.1 },
      { date: '2026-06-24', portfolioPct: 8.3, indexPct: 3.4 },
      { date: '2026-07-24', portfolioPct: 10.7, indexPct: 4.1 },
      { date: '2026-08-24', portfolioPct: 9.8, indexPct: 4.9 },
      { date: '2026-09-24', portfolioPct: 14.2, indexPct: 5.8 },
    ],
  },
];

export interface AllocationSlice {
  label: string;
  percent: number;
}

export const currentAllocation: AllocationSlice[] = [
  { label: 'Aktier', percent: 60 },
  { label: 'Räntor', percent: 40 },
];

export const targetAllocation: AllocationSlice[] = [
  { label: 'Aktier', percent: 50 },
  { label: 'Räntor', percent: 50 },
];

export interface HoldingsSummary {
  totalValue: number;
  totalInvested: number;
}

// Same totals as the Overview sample data.
export const holdingsSummary: HoldingsSummary = {
  totalValue: 264284.7,
  totalInvested: 225664.7,
};
