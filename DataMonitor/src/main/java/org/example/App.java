package org.example;


import org.example.client.BlockchainClient;
import org.example.service.BlockService;
import org.example.reporter.ConsoleReporter;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException, InterruptedException {

        String url = "https://eth-mainnet.g.alchemy.com/v2/3AM57GszO0MV8S8e6WVyQ";

        // Access Layer
//        BlockchainClient client = new BlockchainClient(url);
//        Web3j web3j = client.getWeb3j();
//
//
//        // Business Logic
//        BlockService blockService = new BlockService(web3j);
//
//        // Reporting
//        ConsoleReporter reporter = new ConsoleReporter();
//
//        // Wykonanie
//        List<EthBlock.Block> blocks = blockService.getLatestBlocks(1);
//
//
//
//        reporter.printBlocks(blocks);
    }
}