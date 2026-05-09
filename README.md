# BlockchainDataMonitor

Prosty monitor blokow Ethereum: backend w Java pobiera dane blockchain, frontend w React wyswietla je w dashboardzie.

## Architektura

- `DataMonitor` - backend Java (Web3j + HTTP API)
- `frontend` - UI React + TypeScript + Tailwind

Backend pobiera dane z RPC i udostepnia endpoint:

- `GET /api/blocks/recent?basic=100&detailed=10`

Frontend nie laczy sie bezposrednio z blockchainem, tylko pobiera dane z backendu.

## Wymagania

- Java 17+ (lub zgodna z Twoja konfiguracja projektu)
- Maven (`mvn`) dostepny w PATH
- Node.js 20+ i npm

## Konfiguracja

### Backend (Java)

Opcjonalne zmienne srodowiskowe:

- `ETH_RPC_URL` - endpoint RPC Ethereum  
  Domyslnie: `https://eth-mainnet.g.alchemy.com/v2/3AM57GszO0MV8S8e6WVyQ`
- `API_PORT` - port API backendu  
  Domyslnie: `8080`

### Frontend (React)

Skopiuj przyklad i ustaw URL backendu:

```bash
cd frontend
cp .env.example .env.local
```

Domyslnie:

- `VITE_BACKEND_URL=http://localhost:8080`

## Uruchomienie

Uruchom backend:

```bash
cd DataMonitor
mvn compile exec:java -Dexec.mainClass=org.example.App
```

W PowerShell uzyj wersji z `--%` (zatrzymuje parsowanie argumentow przez shell):

```powershell
cd DataMonitor
mvn --% compile exec:java -Dexec.mainClass=org.example.App
```

W drugim terminalu uruchom frontend:

```bash
cd frontend
npm install
npm run dev
```

## Szybka weryfikacja

Health check backendu:

```bash
curl http://localhost:8080/health
```

Pobranie blokow z backendu:

```bash
curl "http://localhost:8080/api/blocks/recent?basic=100&detailed=10"
```

Frontend lokalnie:

- `http://localhost:5173`

## Co zwraca API

`/api/blocks/recent` zwraca:

- `latestBlockNumber`
- `basicBlocks` (ostatnie N blokow, podstawowe pola)
- `detailedBlocks` (ostatnie M blokow ze szczegolami i transakcjami)