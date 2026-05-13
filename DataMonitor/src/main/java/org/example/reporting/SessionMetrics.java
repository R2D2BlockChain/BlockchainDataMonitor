package org.example.reporting;

import org.example.api.RecentBlocksResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Liczniki zbierane w trakcie działania serwera; zapisywane do pliku przy zamknięciu JVM.
 */
public final class SessionMetrics {

    private final AtomicLong blocksProcessed = new AtomicLong();
    private final AtomicLong transactionsProcessed = new AtomicLong();
    private final AtomicLong gasUsedTotal = new AtomicLong();

    public void recordSuccessfulRecentBlocks(
            List<RecentBlocksResponse.BasicBlockInfo> basicBlocks,
            List<RecentBlocksResponse.DetailedBlockInfo> detailedBlocks
    ) {
        long blocks = (long) basicBlocks.size() + detailedBlocks.size();
        long gas = 0L;
        for (RecentBlocksResponse.BasicBlockInfo b : basicBlocks) {
            gas += b.getGasUsed();
        }
        long tx = 0L;
        for (RecentBlocksResponse.BasicBlockInfo b : basicBlocks) {
            tx += b.getTransactionCount();
        }
        blocksProcessed.addAndGet(blocks);
        transactionsProcessed.addAndGet(tx);
        gasUsedTotal.addAndGet(gas);
    }

    public long getBlocksProcessed() {
        return blocksProcessed.get();
    }

    public long getTransactionsProcessed() {
        return transactionsProcessed.get();
    }

    public long getGasUsedTotal() {
        return gasUsedTotal.get();
    }
}
