package org.example.reporter;

import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.List;

public class ConsoleReporter {

    public void printBlocks(List<EthBlock.Block> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            System.out.println("Brak blokow do wyswietlenia.");
            return;
        }

        for (EthBlock.Block block : blocks) {
            if (block == null) {
                continue;
            }
            System.out.printf(
                    "Block #%s | hash=%s | txCount=%d%n",
                    block.getNumber(),
                    block.getHash(),
                    block.getTransactions().size()
            );
        }
    }
}
