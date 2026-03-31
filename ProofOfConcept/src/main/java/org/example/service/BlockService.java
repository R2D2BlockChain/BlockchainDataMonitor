package org.example.service;

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

    public List<EthBlock.Block> getLatestBlocks(int count) throws IOException, InterruptedException {
        List<EthBlock.Block> blocks = new ArrayList<>();

        BigInteger latestBlock = web3j.ethBlockNumber().send().getBlockNumber();


        for (int i = 0; i < count; i++) {
            BigInteger blockNumber = latestBlock.subtract(BigInteger.valueOf(i));


            EthBlock blockResponse = web3j
                    .ethGetBlockByNumber(
                            org.web3j.protocol.core.DefaultBlockParameter.valueOf(blockNumber),
                            true
                    )
                    .send();

            EthBlock.Block block = blockResponse.getBlock();

            if (block != null) {
                blocks.add(block);
            }

            Thread.sleep(200);
        }

        return blocks;
    }
}