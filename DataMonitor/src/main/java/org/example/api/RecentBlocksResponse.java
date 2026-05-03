package org.example.api;

import java.util.List;

public class RecentBlocksResponse {
    private final long latestBlockNumber;
    private final List<BasicBlockInfo> basicBlocks;
    private final List<DetailedBlockInfo> detailedBlocks;

    public RecentBlocksResponse(
            long latestBlockNumber,
            List<BasicBlockInfo> basicBlocks,
            List<DetailedBlockInfo> detailedBlocks
    ) {
        this.latestBlockNumber = latestBlockNumber;
        this.basicBlocks = basicBlocks;
        this.detailedBlocks = detailedBlocks;
    }

    public long getLatestBlockNumber() {
        return latestBlockNumber;
    }

    public List<BasicBlockInfo> getBasicBlocks() {
        return basicBlocks;
    }

    public List<DetailedBlockInfo> getDetailedBlocks() {
        return detailedBlocks;
    }

    public static class BasicBlockInfo {
        private final long number;
        private final String hash;
        private final long timestamp;
        private final int transactionCount;
        private final long gasUsed;

        public BasicBlockInfo(long number, String hash, long timestamp, int transactionCount, long gasUsed) {
            this.number = number;
            this.hash = hash;
            this.timestamp = timestamp;
            this.transactionCount = transactionCount;
            this.gasUsed = gasUsed;
        }

        public long getNumber() {
            return number;
        }

        public String getHash() {
            return hash;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public int getTransactionCount() {
            return transactionCount;
        }

        public long getGasUsed() {
            return gasUsed;
        }
    }

    public static class DetailedTransactionInfo {
        private final String hash;
        private final String from;
        private final String to;
        private final String valueWei;
        private final long gas;
        private final String gasPriceWei;

        public DetailedTransactionInfo(
                String hash,
                String from,
                String to,
                String valueWei,
                long gas,
                String gasPriceWei
        ) {
            this.hash = hash;
            this.from = from;
            this.to = to;
            this.valueWei = valueWei;
            this.gas = gas;
            this.gasPriceWei = gasPriceWei;
        }

        public String getHash() {
            return hash;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getValueWei() {
            return valueWei;
        }

        public long getGas() {
            return gas;
        }

        public String getGasPriceWei() {
            return gasPriceWei;
        }
    }

    public static class DetailedBlockInfo extends BasicBlockInfo {
        private final String parentHash;
        private final String miner;
        private final String nonce;
        private final long size;
        private final long gasLimit;
        private final String baseFeePerGasWei;
        private final List<DetailedTransactionInfo> transactions;

        public DetailedBlockInfo(
                long number,
                String hash,
                long timestamp,
                int transactionCount,
                long gasUsed,
                String parentHash,
                String miner,
                String nonce,
                long size,
                long gasLimit,
                String baseFeePerGasWei,
                List<DetailedTransactionInfo> transactions
        ) {
            super(number, hash, timestamp, transactionCount, gasUsed);
            this.parentHash = parentHash;
            this.miner = miner;
            this.nonce = nonce;
            this.size = size;
            this.gasLimit = gasLimit;
            this.baseFeePerGasWei = baseFeePerGasWei;
            this.transactions = transactions;
        }

        public String getParentHash() {
            return parentHash;
        }

        public String getMiner() {
            return miner;
        }

        public String getNonce() {
            return nonce;
        }

        public long getSize() {
            return size;
        }

        public long getGasLimit() {
            return gasLimit;
        }

        public String getBaseFeePerGasWei() {
            return baseFeePerGasWei;
        }

        public List<DetailedTransactionInfo> getTransactions() {
            return transactions;
        }
    }
}
