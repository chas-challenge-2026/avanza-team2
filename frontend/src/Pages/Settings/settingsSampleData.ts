import type { AllocationSlice } from '../Analyses/analysesSampleData.ts';

export const portfolioName: string = 'Min portfölj';

export type CurrencyCode = 'SEK' | 'USD' ;
export const defaultCurrency: CurrencyCode = 'SEK';

export const targetDistribution: AllocationSlice[] = [
  { label: 'Aktier', percent: 50 },
  { label: 'Räntor', percent: 35 },
  { label: 'Övrigt', percent: 15 },
];

export const deviationAlert = {
  thresholdPercent: 5,
};

export interface LinkedAccountsSummary {
  count: number;
}

export const linkedAccounts: LinkedAccountsSummary = {
  count: 2,
};
