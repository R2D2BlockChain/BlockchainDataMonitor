export type NetworkStatus = "healthy" | "delayed" | "offline"

export interface NetworkMetric {
  label: string
  value: string
  status: NetworkStatus
}

export interface BasicBlockInfo {
  number: number
  hash: string
  timestamp: number
  transactionCount: number
  gasUsed: number
}

export interface DetailedTransactionInfo {
  hash: string
  from: string
  to: string | null
  valueWei: string
  gas: number
  gasPriceWei: string
}

export interface DetailedBlockInfo extends BasicBlockInfo {
  parentHash: string
  miner: string
  nonce: string
  size: number
  gasLimit: number
  baseFeePerGasWei: string | null
  transactions: DetailedTransactionInfo[]
}
