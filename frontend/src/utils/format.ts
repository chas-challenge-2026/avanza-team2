const signedPercentFormat = new Intl.NumberFormat('sv-SE', {
  signDisplay: 'exceptZero',
  minimumFractionDigits: 1,
  maximumFractionDigits: 2,
});

const signedSekFormat = new Intl.NumberFormat('sv-SE', {
  signDisplay: 'exceptZero',
  maximumFractionDigits: 2,
});

const decimalFormat = new Intl.NumberFormat('sv-SE', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 2,
});

const sekFormat = new Intl.NumberFormat('sv-SE', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

export const formatSignedPercent = (value: number) => `${signedPercentFormat.format(value)}%`;

export const formatSignedSek = (value: number) => `${signedSekFormat.format(value)} SEK`;

export const formatDecimal = (value: number) => decimalFormat.format(value);

export const formatSek = (value: number) => `${sekFormat.format(value)} SEK`;

export const getInitials = (name: string) => {
  const parts = name.trim().split(/\s+/).filter(Boolean);

  if (parts.length === 0) {
    return '';
  }

  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase();
  }

  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
};
