// Represents one account type and its target allocation.
export interface TargetAllocation {
  id: number
  accountType: string
  targetPercentage: number
}

// Defines the request body expected by the backend.
interface UpdateTargetAllocationsRequest {
  allocations: {
    accountType: string
    targetPercentage: number
  }[]
}

// Uses the existing API base URL and session-based authentication.
const API_URL = import.meta.env.VITE_API_URL ?? ''

// Fetches the target allocations for the authenticated user.
export const getTargetAllocations = async (): Promise<TargetAllocation[]> => {
  const response = await fetch(`${API_URL}/api/target-allocations`, {
    method: 'GET',
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to load target allocations.')
  }

  return response.json()
}

// Saves all target allocations for the authenticated user.
export const updateTargetAllocations = async (
  allocations: UpdateTargetAllocationsRequest['allocations'],
): Promise<TargetAllocation[]> => {
  const response = await fetch(`${API_URL}/api/target-allocations`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ allocations }),
  })

  if (!response.ok) {
    throw new Error('Failed to save target allocations.')
  }

  return response.json()
}