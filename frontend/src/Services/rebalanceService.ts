export interface RebalanceTrade {
  holdingId: number
  ticker: string
  category: string
  side: 'BUY' | 'SELL'
  quantity: number
  price: number
  value: number
}

// Matches backend RebalancePreviewDTO.
export interface RebalancePreview {
  accountId: number
  totalValue: number
  currentAktierPct: number
  currentFonderPct: number
  targetAktierPct: number
  targetFonderPct: number
  drifted: boolean
  trades: RebalanceTrade[]
  warning: string | null
}

const API_URL = import.meta.env.VITE_API_URL ?? ''

const readError = async (response: Response, fallback: string) => {
  try {
    const body = await response.json()
    return body?.detail ?? fallback
  } catch {
    return fallback
  }
}

// Calculates the trades for a target split without changing anything.
export const previewRebalance = async (
  accountId: number,
  targetAktierPct: number,
  signal?: AbortSignal,
): Promise<RebalancePreview> => {
  const params = new URLSearchParams({
    accountId: String(accountId),
    targetAktierPct: String(targetAktierPct),
  })
  const response = await fetch(`${API_URL}/api/rebalance/preview?${params}`, {
    method: 'GET',
    credentials: 'include',
    signal,
  })

  if (!response.ok) {
    throw new Error(await readError(response, 'Kunde inte förhandsgranska rebalanseringen.'))
  }

  return response.json()
}

// Carries out the rebalance the user has chosen.
export const applyRebalance = async (
  accountId: number,
  targetAktierPct: number,
): Promise<RebalancePreview> => {
  const response = await fetch(`${API_URL}/api/rebalance/apply`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ accountId, targetAktierPct }),
  })

  if (!response.ok) {
    throw new Error(await readError(response, 'Kunde inte genomföra rebalanseringen.'))
  }

  return response.json()
}
