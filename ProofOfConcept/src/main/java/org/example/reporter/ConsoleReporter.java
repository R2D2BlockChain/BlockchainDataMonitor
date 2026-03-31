package org.example.reporter;

import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.List;


public class ConsoleReporter {

    public void printBlocks(List<EthBlock.Block> blocks) {

        for (EthBlock.Block block : blocks) {
            System.out.println("Block: " + block.getNumber());
            System.out.println("Hash: " + block.getHash());
            System.out.println("Transactions: " + block.getTransactions().size());
            System.out.println("Bleble: " + block.getSize());
            System.out.println("GasUsed: " + block.getGasUsed());
            System.out.println("Timestamp: " + block.getTimestamp());
            System.out.println("Difficulty: " + block.getDifficulty());
            System.out.println("Miner: " + block.getMiner());

            List<EthBlock.TransactionResult> transactionResults = block.getTransactions();

            for (int i=0; i<10; i++) {
                EthBlock.TransactionObject temp = (EthBlock.TransactionObject) transactionResults.get(i);

                System.out.println(temp.getFrom());
                System.out.println(temp.getTo());
                System.out.println(temp.getValue());
                System.out.println(temp.getGas());
            }
            System.out.println("----------------------------");
        }
    }
}