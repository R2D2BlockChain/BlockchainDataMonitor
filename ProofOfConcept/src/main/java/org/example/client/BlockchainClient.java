package org.example.client;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class BlockchainClient {

    private final Web3j web3j;

    public BlockchainClient(String url) {
        this.web3j = Web3j.build(new HttpService(url));
    }

    public Web3j getWeb3j() {
        return web3j;
    }
}
