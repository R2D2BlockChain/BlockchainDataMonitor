package org.example.service;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class BlockService {

    private final Web3j web3j;

    public BlockService(Web3j web3j) {
        this.web3j = web3j;
    }

    public BigInteger getLatestBlock() throws IOException, InterruptedException {
        return web3j.ethBlockNumber().send().getBlockNumber();
    }

    public String getBlockNumber(EthBlock.Block block) throws IOException, InterruptedException {
        return block.getNumber().toString();
    }

    public String getBlockHash(EthBlock.Block block) throws IOException, InterruptedException {
        return block.getHash();
    }

    public String getBlockTransactionCount(EthBlock.Block block) throws IOException, InterruptedException {
        return String.valueOf(block.getTransactions().size());
    }

    public List<EthBlock.TransactionResult> getTransactionsList(EthBlock.Block block) throws IOException, InterruptedException{
        return block.getTransactions();
    }

    public EthBlock.TransactionObject getSingleTransaction(List<EthBlock.TransactionResult> transactionResults, int index) throws IOException, InterruptedException{
        return (EthBlock.TransactionObject) transactionResults.get(index);
    }

    public String getSingleTransactionHash (EthBlock.TransactionObject transaction)  throws IOException, InterruptedException{
        return transaction.getHash();
    }

    public String getSingleTransactionSender(EthBlock.TransactionObject transaction)  throws IOException, InterruptedException{
        return transaction.getFrom();
    }

    public String getSingleTransactionReceiver(EthBlock.TransactionObject transaction)  throws IOException, InterruptedException{
        return transaction.getTo();
    }

    public String getSingleTransactionValue(EthBlock.TransactionObject transaction)  throws IOException, InterruptedException{
        return transaction.getValue().toString();
    }

    public String getSingleTransactionGas(EthBlock.TransactionObject transaction)  throws IOException, InterruptedException{
        return transaction.getGas().toString();
    }


}