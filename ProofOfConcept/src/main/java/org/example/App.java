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
        BlockchainClient client = new BlockchainClient(url);
        Web3j web3j = client.getWeb3j();


        // Business Logic
        BlockService blockService = new BlockService(web3j);

        // Reporting
        ConsoleReporter reporter = new ConsoleReporter();

        // Wykonanie
        List<EthBlock.Block> blocks = blockService.getLatestBlocks(1);



        reporter.printBlocks(blocks);
    }
}


/*import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;
import java.math.BigInteger;

public class App {

    public static void main(String[] args) throws IOException {

        String url = "https://eth-sepolia.g.alchemy.com/v2/3AM57GszO0MV8S8e6WVyQ";

        Web3j web3j = Web3j.build(new HttpService(url));

        // sprawdzenie połączenia
        BigInteger latestBlock = web3j.ethBlockNumber().send().getBlockNumber();
        System.out.println("Latest block: " + latestBlock);

        // pobranie 100 bloków
        for (int i = 0; i < 100; i++) {
            BigInteger blockNumber = latestBlock.subtract(BigInteger.valueOf(i));

            EthBlock blockResponse = web3j
                    .ethGetBlockByNumber(
                            org.web3j.protocol.core.DefaultBlockParameter.valueOf(blockNumber),
                            true
                    )
                    .send();

            EthBlock.Block block = blockResponse.getBlock();

            System.out.println("Block: " + block.getNumber());
            System.out.println("Hash: " + block.getHash());
            System.out.println("Transactions: " + block.getTransactions().size());
            System.out.println("----------------------------");
        }
    }
}*/