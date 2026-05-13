import type { BasicBlockInfo, DetailedBlockInfo } from "../types/blockchain"

interface RecentBlocksApiResponse {
  latestBlockNumber: number
  basicBlocks: BasicBlockInfo[]
  detailedBlocks: DetailedBlockInfo[]
}

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL ?? "http://localhost:8080"

/** HTTP 429 — zawiera `retryAfterSeconds` z API (nagłówek lub JSON). */
export class RateLimitedError extends Error {
  readonly retryAfterSeconds: number

  constructor(retryAfterSeconds: number) {
    const s = Math.max(1, Math.floor(retryAfterSeconds))
    super(`Zbyt wiele żądań. Spróbuj ponownie za ${s} s.`)
    this.name = "RateLimitedError"
    this.retryAfterSeconds = s
  }
}

function parseRetryAfterSeconds(response: Response, body: unknown): number {
  const headerRaw = response.headers.get("Retry-After")
  if (headerRaw != null && headerRaw.trim() !== "") {
    const n = parseInt(headerRaw, 10)
    if (Number.isFinite(n) && n > 0) return n
  }
  if (
    body != null &&
    typeof body === "object" &&
    "retryAfterSeconds" in body &&
    typeof (body as { retryAfterSeconds: unknown }).retryAfterSeconds ===
      "number"
  ) {
    const n = (body as { retryAfterSeconds: number }).retryAfterSeconds
    if (Number.isFinite(n) && n > 0) return Math.floor(n)
  }
  return 60
}

export async function fetchRecentBlocks() {
  const response = await fetch(
    `${BACKEND_URL}/api/blocks/recent?basic=100&detailed=10`,
  )

  if (response.status === 429) {
    let body: unknown = null
    try {
      body = await response.json()
    } catch {
      /* ignore */
    }
    throw new RateLimitedError(parseRetryAfterSeconds(response, body))
  }

  if (!response.ok) {
    let detail = ""
    try {
      const errBody = (await response.json()) as { error?: string }
      if (typeof errBody?.error === "string") detail = `: ${errBody.error}`
    } catch {
      /* ignore */
    }
    throw new Error(
      `Żądanie do backendu nie powiodło się (status ${response.status})${detail}`,
    )
  }

  const data = (await response.json()) as RecentBlocksApiResponse
  if (!data.basicBlocks || !data.detailedBlocks) {
    throw new Error("Backend response has invalid format")
  }

  return data
}
