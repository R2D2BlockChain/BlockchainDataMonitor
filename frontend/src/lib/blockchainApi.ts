import type { BasicBlockInfo, DetailedBlockInfo } from "../types/blockchain"

interface RecentBlocksApiResponse {
  latestBlockNumber: number
  basicBlocks: BasicBlockInfo[]
  detailedBlocks: DetailedBlockInfo[]
}

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL ?? "http://localhost:8080"

export async function fetchRecentBlocks() {
  const response = await fetch(
    `${BACKEND_URL}/api/blocks/recent?basic=100&detailed=10`,
  )

  if (!response.ok) {
    throw new Error(`Backend request failed with status ${response.status}`)
  }

  const data = (await response.json()) as RecentBlocksApiResponse
  if (!data.basicBlocks || !data.detailedBlocks) {
    throw new Error("Backend response has invalid format")
  }

  return data
}
