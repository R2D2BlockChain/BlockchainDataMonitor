import { useEffect, useMemo, useState } from "react"
import { AppHeader } from "../components/AppHeader"
import { fetchRecentBlocks } from "../lib/blockchainApi"
import type { BasicBlockInfo, DetailedBlockInfo } from "../types/blockchain"

interface DashboardState {
  latestBlockNumber: number
  basicBlocks: BasicBlockInfo[]
  detailedBlocks: DetailedBlockInfo[]
}

function shortHash(hash: string): string {
  return `${hash.slice(0, 10)}...${hash.slice(-8)}`
}

function formatDate(timestamp: number): string {
  return new Date(timestamp * 1000).toLocaleString()
}

export function DashboardPage() {
  const [data, setData] = useState<DashboardState | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadBlocks() {
      try {
        setLoading(true)
        setError(null)
        const result = await fetchRecentBlocks()
        setData(result)
      } catch (loadError) {
        const message =
          loadError instanceof Error
            ? loadError.message
            : "Nieznany blad podczas pobierania blokow"
        setError(message)
      } finally {
        setLoading(false)
      }
    }

    void loadBlocks()
  }, [])

  const stats = useMemo(() => {
    if (!data) return null
    const totalTransactions = data.basicBlocks.reduce(
      (acc, block) => acc + block.transactionCount,
      0,
    )
    return {
      latestBlock: data.latestBlockNumber,
      totalTransactions,
      averageTxCount: Math.round(totalTransactions / data.basicBlocks.length),
    }
  }, [data])

  return (
    <main className="min-h-screen bg-slate-950 px-6 py-10 text-slate-100 md:px-10">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-8">
        <AppHeader />

        {loading && (
          <section className="rounded-xl border border-slate-800 bg-slate-900/60 p-5 text-slate-300">
            Ladowanie ostatnich 100 blokow...
          </section>
        )}

        {error && (
          <section className="rounded-xl border border-rose-500/40 bg-rose-500/10 p-5 text-rose-200">
            Nie udalo sie pobrac danych z RPC: {error}
          </section>
        )}

        {stats && (
          <section className="grid gap-4 md:grid-cols-3">
            <article className="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
              <p className="text-xs uppercase tracking-wide text-slate-400">
                Najnowszy blok
              </p>
              <p className="mt-1 text-xl font-semibold text-slate-100">
                #{stats.latestBlock.toLocaleString()}
              </p>
            </article>
            <article className="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
              <p className="text-xs uppercase tracking-wide text-slate-400">
                Suma transakcji (100 blokow)
              </p>
              <p className="mt-1 text-xl font-semibold text-slate-100">
                {stats.totalTransactions.toLocaleString()}
              </p>
            </article>
            <article className="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
              <p className="text-xs uppercase tracking-wide text-slate-400">
                Srednia TX / blok
              </p>
              <p className="mt-1 text-xl font-semibold text-slate-100">
                {stats.averageTxCount.toLocaleString()}
              </p>
            </article>
          </section>
        )}

        {data && (
          <section className="space-y-3 rounded-xl border border-slate-800 bg-slate-900/60 p-4">
            <h2 className="text-lg font-semibold text-slate-100">
              Ostatnie 100 blokow (podstawowe informacje)
            </h2>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[880px] text-left text-sm">
                <thead className="text-slate-400">
                  <tr className="border-b border-slate-800">
                    <th className="px-2 py-2 font-medium">Blok</th>
                    <th className="px-2 py-2 font-medium">Hash</th>
                    <th className="px-2 py-2 font-medium">Timestamp</th>
                    <th className="px-2 py-2 font-medium">Liczba TX</th>
                    <th className="px-2 py-2 font-medium">Gas used</th>
                  </tr>
                </thead>
                <tbody>
                  {data.basicBlocks.map((block) => (
                    <tr key={block.hash} className="border-b border-slate-900/90">
                      <td className="px-2 py-2 text-slate-200">
                        #{block.number.toLocaleString()}
                      </td>
                      <td className="px-2 py-2 font-mono text-xs text-slate-300">
                        {shortHash(block.hash)}
                      </td>
                      <td className="px-2 py-2 text-slate-300">
                        {formatDate(block.timestamp)}
                      </td>
                      <td className="px-2 py-2 text-slate-300">
                        {block.transactionCount.toLocaleString()}
                      </td>
                      <td className="px-2 py-2 text-slate-300">
                        {block.gasUsed.toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {data && (
          <section className="space-y-3 rounded-xl border border-slate-800 bg-slate-900/60 p-4">
            <h2 className="text-lg font-semibold text-slate-100">
              Ostatnie 10 blokow (szczegolowe informacje)
            </h2>
            <div className="space-y-3">
              {data.detailedBlocks.map((block) => (
                <details
                  key={block.hash}
                  className="rounded-lg border border-slate-800 bg-slate-950/70 p-4"
                >
                  <summary className="cursor-pointer text-sm font-medium text-slate-100">
                    Blok #{block.number.toLocaleString()} | TX:{" "}
                    {block.transactionCount.toLocaleString()} | Hash:{" "}
                    {shortHash(block.hash)}
                  </summary>
                  <div className="mt-3 grid gap-2 text-sm text-slate-300 md:grid-cols-2">
                    <p>
                      <span className="text-slate-400">Parent hash:</span>{" "}
                      <span className="font-mono text-xs">
                        {shortHash(block.parentHash)}
                      </span>
                    </p>
                    <p>
                      <span className="text-slate-400">Miner:</span>{" "}
                      <span className="font-mono text-xs">
                        {shortHash(block.miner)}
                      </span>
                    </p>
                    <p>
                      <span className="text-slate-400">Nonce:</span>{" "}
                      <span className="font-mono text-xs">{block.nonce}</span>
                    </p>
                    <p>
                      <span className="text-slate-400">Size:</span>{" "}
                      {block.size.toLocaleString()}
                    </p>
                    <p>
                      <span className="text-slate-400">Gas limit:</span>{" "}
                      {block.gasLimit.toLocaleString()}
                    </p>
                    <p>
                      <span className="text-slate-400">Base fee:</span>{" "}
                      <span className="font-mono text-xs">
                        {block.baseFeePerGasWei ?? "brak"}
                      </span>
                    </p>
                  </div>

                  <div className="mt-3 overflow-x-auto">
                    <p className="mb-2 text-xs uppercase tracking-wide text-slate-400">
                      Transakcje w bloku
                    </p>
                    <table className="w-full min-w-[780px] text-left text-xs">
                      <thead className="text-slate-500">
                        <tr className="border-b border-slate-800">
                          <th className="px-2 py-1.5 font-medium">Hash</th>
                          <th className="px-2 py-1.5 font-medium">From</th>
                          <th className="px-2 py-1.5 font-medium">To</th>
                          <th className="px-2 py-1.5 font-medium">Value (wei)</th>
                          <th className="px-2 py-1.5 font-medium">Gas</th>
                          <th className="px-2 py-1.5 font-medium">Gas price</th>
                        </tr>
                      </thead>
                      <tbody>
                        {block.transactions.map((tx) => (
                          <tr key={tx.hash} className="border-b border-slate-900/80">
                            <td className="px-2 py-1.5 font-mono text-slate-300">
                              {shortHash(tx.hash)}
                            </td>
                            <td className="px-2 py-1.5 font-mono text-slate-300">
                              {shortHash(tx.from)}
                            </td>
                            <td className="px-2 py-1.5 font-mono text-slate-300">
                              {tx.to ? shortHash(tx.to) : "contract creation"}
                            </td>
                            <td className="px-2 py-1.5 font-mono text-slate-300">
                              {tx.valueWei}
                            </td>
                            <td className="px-2 py-1.5 text-slate-300">
                              {tx.gas.toLocaleString()}
                            </td>
                            <td className="px-2 py-1.5 font-mono text-slate-300">
                              {tx.gasPriceWei}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </details>
              ))}
            </div>
          </section>
        )}
      </div>
    </main>
  )
}
