package org.example.service;

import org.example.api.RecentBlocksResponse;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
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

    public RecentBlocksResponse getRecentBlocks(int basicCount, int detailedCount) throws IOException, InterruptedException {
        int safeBasicCount = Math.max(1, basicCount);
        int safeDetailedCount = Math.max(1, detailedCount);
        int actualDetailedCount = Math.min(safeBasicCount, safeDetailedCount);

        BigInteger latest = getLatestBlock();
        List<RecentBlocksResponse.BasicBlockInfo> basicBlocks = new ArrayList<RecentBlocksResponse.BasicBlockInfo>();
        List<RecentBlocksResponse.DetailedBlockInfo> detailedBlocks = new ArrayList<RecentBlocksResponse.DetailedBlockInfo>();

        for (int i = 0; i < safeBasicCount; i++) {
            BigInteger blockNumber = latest.subtract(BigInteger.valueOf(i));
            EthBlock.Block basicBlock = web3j.ethGetBlockByNumber(
                    org.web3j.protocol.core.DefaultBlockParameter.valueOf(blockNumber),
                    false
            ).send().getBlock();
            basicBlocks.add(mapBasicBlock(basicBlock));
        }

        for (int i = 0; i < actualDetailedCount; i++) {
            BigInteger blockNumber = latest.subtract(BigInteger.valueOf(i));
            EthBlock.Block detailedBlock = web3j.ethGetBlockByNumber(
                    org.web3j.protocol.core.DefaultBlockParameter.valueOf(blockNumber),
                    true
            ).send().getBlock();
            detailedBlocks.add(mapDetailedBlock(detailedBlock));
        }

        return new RecentBlocksResponse(latest.longValue(), basicBlocks, detailedBlocks);
    }

    private RecentBlocksResponse.BasicBlockInfo mapBasicBlock(EthBlock.Block block) throws IOException, InterruptedException {
        return new RecentBlocksResponse.BasicBlockInfo(
                Long.parseLong(getBlockNumber(block)),
                getBlockHash(block),
                block.getTimestamp().longValue(),
                Integer.parseInt(getBlockTransactionCount(block)),
                block.getGasUsed().longValue()
        );
    }

    private RecentBlocksResponse.DetailedBlockInfo mapDetailedBlock(EthBlock.Block block) throws IOException, InterruptedException {
        List<RecentBlocksResponse.DetailedTransactionInfo> transactions =
                new ArrayList<RecentBlocksResponse.DetailedTransactionInfo>();

        List<EthBlock.TransactionResult> transactionResults = getTransactionsList(block);
        for (int i = 0; i < transactionResults.size(); i++) {
            try {
                EthBlock.TransactionObject tx = getSingleTransaction(transactionResults, i);
                transactions.add(
                        new RecentBlocksResponse.DetailedTransactionInfo(
                                getSingleTransactionHash(tx),
                                getSingleTransactionSender(tx),
                                getSingleTransactionReceiver(tx),
                                getSingleTransactionValue(tx),
                                Long.parseLong(getSingleTransactionGas(tx)),
                                tx.getGasPrice().toString()
                        )
                );
            } catch (ClassCastException ignored) {
                // Ignore non-object transactions.
            }
        }

        String baseFee;

        if (block.getBaseFeePerGas() == null) {
            baseFee = null;
        } else {
            baseFee = block.getBaseFeePerGas().toString();
        }

        return new RecentBlocksResponse.DetailedBlockInfo(
                Long.parseLong(getBlockNumber(block)),
                getBlockHash(block),
                block.getTimestamp().longValue(),
                Integer.parseInt(getBlockTransactionCount(block)),
                block.getGasUsed().longValue(),
                block.getParentHash(),
                block.getMiner(),
                block.getNonceRaw(),
                block.getSize().longValue(),
                block.getGasLimit().longValue(),
                baseFee,
                transactions
        );
    }

}