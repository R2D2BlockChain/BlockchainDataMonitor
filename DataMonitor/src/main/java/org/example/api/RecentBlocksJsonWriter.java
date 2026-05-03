package org.example.api;

import java.util.List;

public final class RecentBlocksJsonWriter {
    private RecentBlocksJsonWriter() {
    }

    public static String toJson(RecentBlocksResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"latestBlockNumber\":").append(response.getLatestBlockNumber()).append(",");
        sb.append("\"basicBlocks\":");
        appendBasicBlocks(sb, response.getBasicBlocks());
        sb.append(",");
        sb.append("\"detailedBlocks\":");
        appendDetailedBlocks(sb, response.getDetailedBlocks());
        sb.append("}");
        return sb.toString();
    }

    private static void appendBasicBlocks(StringBuilder sb, List<RecentBlocksResponse.BasicBlockInfo> blocks) {
        sb.append("[");
        for (int i = 0; i < blocks.size(); i++) {
            RecentBlocksResponse.BasicBlockInfo block = blocks.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{");
            sb.append("\"number\":").append(block.getNumber()).append(",");
            sb.append("\"hash\":\"").append(escape(block.getHash())).append("\",");
            sb.append("\"timestamp\":").append(block.getTimestamp()).append(",");
            sb.append("\"transactionCount\":").append(block.getTransactionCount()).append(",");
            sb.append("\"gasUsed\":").append(block.getGasUsed());
            sb.append("}");
        }
        sb.append("]");
    }

    private static void appendDetailedBlocks(StringBuilder sb, List<RecentBlocksResponse.DetailedBlockInfo> blocks) {
        sb.append("[");
        for (int i = 0; i < blocks.size(); i++) {
            RecentBlocksResponse.DetailedBlockInfo block = blocks.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{");
            sb.append("\"number\":").append(block.getNumber()).append(",");
            sb.append("\"hash\":\"").append(escape(block.getHash())).append("\",");
            sb.append("\"timestamp\":").append(block.getTimestamp()).append(",");
            sb.append("\"transactionCount\":").append(block.getTransactionCount()).append(",");
            sb.append("\"gasUsed\":").append(block.getGasUsed()).append(",");
            sb.append("\"parentHash\":\"").append(escape(block.getParentHash())).append("\",");
            sb.append("\"miner\":\"").append(escape(block.getMiner())).append("\",");
            sb.append("\"nonce\":\"").append(escape(block.getNonce())).append("\",");
            sb.append("\"size\":").append(block.getSize()).append(",");
            sb.append("\"gasLimit\":").append(block.getGasLimit()).append(",");

            String baseFee = block.getBaseFeePerGasWei();
            if (baseFee == null) {
                sb.append("\"baseFeePerGasWei\":null,");
            } else {
                sb.append("\"baseFeePerGasWei\":\"").append(escape(baseFee)).append("\",");
            }

            sb.append("\"transactions\":");
            appendTransactions(sb, block.getTransactions());
            sb.append("}");
        }
        sb.append("]");
    }

    private static void appendTransactions(StringBuilder sb, List<RecentBlocksResponse.DetailedTransactionInfo> txs) {
        sb.append("[");
        for (int i = 0; i < txs.size(); i++) {
            RecentBlocksResponse.DetailedTransactionInfo tx = txs.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{");
            sb.append("\"hash\":\"").append(escape(tx.getHash())).append("\",");
            sb.append("\"from\":\"").append(escape(tx.getFrom())).append("\",");
            if (tx.getTo() == null) {
                sb.append("\"to\":null,");
            } else {
                sb.append("\"to\":\"").append(escape(tx.getTo())).append("\",");
            }
            sb.append("\"valueWei\":\"").append(escape(tx.getValueWei())).append("\",");
            sb.append("\"gas\":").append(tx.getGas()).append(",");
            sb.append("\"gasPriceWei\":\"").append(escape(tx.getGasPriceWei())).append("\"");
            sb.append("}");
        }
        sb.append("]");
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
