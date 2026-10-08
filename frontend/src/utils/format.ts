const signedPercentFormat = new Intl.NumberFormat('sv-SE', {
  signDisplay: 'exceptZero',
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
});

const signedSekFormat = new Intl.NumberFormat('sv-SE', {
  signDisplay: 'exceptZero',
  maximumFractionDigits: 0,
});

const decimalFormat = new Intl.NumberFormat('sv-SE', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
});

const sekFormat = new Intl.NumberFormat('sv-SE', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
});

export const formatSignedPercent = (value: number) => `${signedPercentFormat.format(value)}%`;

export const formatSignedSek = (value: number) => `${signedSekFormat.format(value)} SEK`;

export const formatDecimal = (value: number) => decimalFormat.format(value);

export const formatSek = (value: number) => `${sekFormat.format(value)} SEK`;
