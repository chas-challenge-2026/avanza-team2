import type { AllocationSlice } from '../Analyses/analysesSampleData.ts';

export const portfolioName: string = 'Min portfölj';

export type CurrencyCode = 'SEK' | 'USD' | 'EUR';
export const defaultCurrency: CurrencyCode = 'SEK';

export const targetDistribution: AllocationSlice[] = [
  { label: 'Aktier', percent: 50 },
  { label: 'Räntor', percent: 35 },
  { label: 'Övrigt', percent: 15 },
];

export interface NotificationDeviation {
  enabled: boolean;
  thresholdPercent: number;
}

export const deviationAlert: NotificationDeviation = {
  enabled: true,
  thresholdPercent: 5,
};

export interface NotificationPreferences {
  generalNotifications: boolean;
  dividendNotifications: boolean;
  quarterlyReports: boolean;
}

export const notificationPreferences: NotificationPreferences = {
  generalNotifications: true,
  dividendNotifications: true,
  quarterlyReports: true,
};

export interface LinkedAccountsSummary {
  count: number;
}

export const linkedAccounts: LinkedAccountsSummary = {
  count: 2,
};
