export function AppHeader() {
  return (
    <header className="space-y-3">
      <p className="inline-flex rounded-full border border-sky-400/30 bg-sky-400/10 px-3 py-1 text-xs font-medium uppercase tracking-wider text-sky-300">
        Blockchain Data Monitor
      </p>
      <h1 className="text-3xl font-semibold tracking-tight text-slate-100 md:text-4xl">
        Monitor ostatnich blokow Ethereum
      </h1>
      <p className="max-w-2xl text-sm text-slate-400 md:text-base">
        Widok live: ostatnie 100 blokow z podstawowymi polami i ostatnie 10
        ze szczegolowymi informacjami oraz lista transakcji.
      </p>
    </header>
  )
}
