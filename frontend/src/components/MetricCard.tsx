import type { NetworkMetric } from "../types/blockchain"

const statusStyles: Record<NetworkMetric["status"], string> = {
  healthy: "bg-emerald-400/10 text-emerald-300 border-emerald-400/30",
  delayed: "bg-amber-400/10 text-amber-300 border-amber-400/30",
  offline: "bg-rose-400/10 text-rose-300 border-rose-400/30",
}

export function MetricCard({ label, value, status }: NetworkMetric) {
  return (
    <article className="rounded-xl border border-slate-800 bg-slate-900/70 p-5 shadow-sm shadow-black/30">
      <div className="mb-3 flex items-center justify-between gap-3">
        <h2 className="text-sm font-medium text-slate-300">{label}</h2>
        <span
          className={`rounded-full border px-2.5 py-0.5 text-xs font-medium ${statusStyles[status]}`}
        >
          {status}
        </span>
      </div>
      <p className="text-2xl font-semibold text-slate-100">{value}</p>
    </article>
  )
}
