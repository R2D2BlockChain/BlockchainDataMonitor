package org.example.client;

import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BlockchainClientTest {

    @Test
    void shouldCreateWeb3jInstanceForGivenUrl() {
        BlockchainClient client = new BlockchainClient("http://localhost:8545");

        Web3j web3j = client.getWeb3j();

        assertNotNull(web3j);
    }
}
