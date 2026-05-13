package org.example.service;

import org.example.api.RecentBlocksResponse;
import org.example.reporting.SessionMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.example.service.Web3jTestData.block;
import static org.example.service.Web3jTestData.ethBlock;
import static org.example.service.Web3jTestData.ethBlockNumber;
import static org.example.service.Web3jTestData.transaction;
import static org.example.service.Web3jTestData.transactionHash;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlockServiceTest {

    private final Queue<Response<?>> rpcResponses = new ArrayDeque<Response<?>>();
    private Web3jService web3jService;
    private BlockService blockService;

    @BeforeEach
    void setUp() throws IOException {
        web3jService = mock(Web3jService.class);
        when(web3jService.send(any(Request.class), any())).thenAnswer(invocation -> {
            Class<?> responseType = invocation.getArgument(1);
            return responseType.cast(rpcResponses.remove());
        });
        blockService = new BlockService(Web3j.build(web3jService), new SessionMetrics());
    }

    @Test
    void shouldReturnLatestBlockNumberFromWeb3j() throws IOException, InterruptedException {
        rpcResponses.add(ethBlockNumber(12345));

        BigInteger result = blockService.getLatestBlock();

        assertEquals(BigInteger.valueOf(12345), result);
    }

    @Test
    void shouldReadBasicBlockFields() throws IOException, InterruptedException {
        EthBlock.Block block = block(100, "0xabc123", List.of(), null);

        assertEquals("100", blockService.getBlockNumber(block));
        assertEquals("0xabc123", blockService.getBlockHash(block));
    }

    @Test
    void shouldReturnTransactionCountFromBlock() throws IOException, InterruptedException {
        EthBlock.Block block = block(
                100,
                "0xabc123",
                List.of(transactionHash("0x1"), transactionHash("0x2"), transactionHash("0x3")),
                null
        );

        String result = blockService.getBlockTransactionCount(block);

        assertEquals("3", result);
    }

    @Test
    void shouldReturnZeroTransactionCountForEmptyList() throws IOException, InterruptedException {
        EthBlock.Block block = block(100, "0xabc123", List.of(), null);

        String result = blockService.getBlockTransactionCount(block);

        assertEquals("0", result);
    }

    @Test
    void shouldReturnTransactionsListFromBlock() throws IOException, InterruptedException {
        List<EthBlock.TransactionResult> transactions = List.of(transactionHash("0x1"));
        EthBlock.Block block = block(100, "0xabc123", transactions, null);

        List<EthBlock.TransactionResult> result = blockService.getTransactionsList(block);

        assertEquals(transactions, result);
    }

    @Test
    void shouldReturnSingleTransactionByIndex() throws IOException, InterruptedException {
        EthBlock.TransactionObject tx = transaction("0xhash", "0xfrom", "0xto", 50, 21000, 10);
        List<EthBlock.TransactionResult> transactions = List.of(tx);

        EthBlock.TransactionObject result = blockService.getSingleTransaction(transactions, 0);

        assertEquals(tx, result);
    }

    @Test
    void shouldThrowForInvalidTransactionIndex() {
        EthBlock.TransactionObject tx = transaction("0xhash", "0xfrom", "0xto", 50, 21000, 10);
        List<EthBlock.TransactionResult> transactions = List.of(tx);

        assertThrows(IndexOutOfBoundsException.class, () -> blockService.getSingleTransaction(transactions, 10));
    }

    @Test
    void shouldThrowWhenTransactionHasUnexpectedType() {
        List<EthBlock.TransactionResult> transactions = List.of(transactionHash("0xhash"));

        assertThrows(ClassCastException.class, () -> blockService.getSingleTransaction(transactions, 0));
    }

    @Test
    void shouldThrowForNullBlockInput() {
        assertThrows(NullPointerException.class, () -> blockService.getTransactionsList(null));
    }

    @Test
    void shouldThrowForNullTransactionInput() {
        assertThrows(NullPointerException.class, () -> blockService.getSingleTransactionHash(null));
    }

    @Test
    void shouldReturnSingleTransactionFieldsAsStrings() throws IOException, InterruptedException {
        EthBlock.TransactionObject tx = transaction("0xhash", "0xfrom", "0xto", 50, 21000, 10);

        assertEquals("0xhash", blockService.getSingleTransactionHash(tx));
        assertEquals("0xfrom", blockService.getSingleTransactionSender(tx));
        assertEquals("0xto", blockService.getSingleTransactionReceiver(tx));
        assertEquals("50", blockService.getSingleTransactionValue(tx));
        assertEquals("21000", blockService.getSingleTransactionGas(tx));
    }

    @Test
    void shouldBuildRecentBlocksResponseForBasicAndDetailedBlocks() throws IOException, InterruptedException {
        EthBlock.TransactionObject tx = transaction("0xtx", "0xfrom", "0xto", 12, 21000, 50);
        EthBlock.Block block200Basic = block(200, "0xblock200", List.of(), null);
        EthBlock.Block block199Basic = block(199, "0xblock199", List.of(), null);
        EthBlock.Block block200Detailed = block(200, "0xblock200", List.of(tx), BigInteger.valueOf(1000));

        rpcResponses.add(ethBlockNumber(200));
        rpcResponses.add(ethBlock(block200Basic));
        rpcResponses.add(ethBlock(block199Basic));
        rpcResponses.add(ethBlock(block200Detailed));

        RecentBlocksResponse response = blockService.getRecentBlocks(2, 1);

        assertEquals(200, response.getLatestBlockNumber());
        assertEquals(2, response.getBasicBlocks().size());
        assertEquals(1, response.getDetailedBlocks().size());
        assertEquals(199, response.getBasicBlocks().get(1).getNumber());
        assertEquals("0xblock200", response.getDetailedBlocks().get(0).getHash());
        assertEquals("1000", response.getDetailedBlocks().get(0).getBaseFeePerGasWei());
        assertEquals("0xtx", response.getDetailedBlocks().get(0).getTransactions().get(0).getHash());
    }

    @Test
    void shouldUseAtLeastOneBlockWhenCountsAreTooSmall() throws IOException, InterruptedException {
        EthBlock.Block basicBlock = block(300, "0xblock300", List.of(), null);
        EthBlock.Block detailedBlock = block(300, "0xblock300", List.of(), null);

        rpcResponses.add(ethBlockNumber(300));
        rpcResponses.add(ethBlock(basicBlock));
        rpcResponses.add(ethBlock(detailedBlock));

        RecentBlocksResponse response = blockService.getRecentBlocks(0, -5);

        assertEquals(1, response.getBasicBlocks().size());
        assertEquals(1, response.getDetailedBlocks().size());
        assertEquals(300, response.getBasicBlocks().get(0).getNumber());
        assertNull(response.getDetailedBlocks().get(0).getBaseFeePerGasWei());
    }

    @Test
    void shouldNotCreateMoreDetailedBlocksThanBasicBlocks() throws IOException, InterruptedException {
        EthBlock.Block basicBlock = block(400, "0xblock400", List.of(), null);
        EthBlock.Block detailedBlock = block(400, "0xblock400", List.of(), null);

        rpcResponses.add(ethBlockNumber(400));
        rpcResponses.add(ethBlock(basicBlock));
        rpcResponses.add(ethBlock(detailedBlock));

        RecentBlocksResponse response = blockService.getRecentBlocks(1, 10);

        assertEquals(1, response.getBasicBlocks().size());
        assertEquals(1, response.getDetailedBlocks().size());
    }

    @Test
    void shouldSkipHashOnlyTransactionsInDetailedBlock() throws IOException, InterruptedException {
        EthBlock.TransactionHash hashOnlyTransaction = transactionHash("0xhashOnly");
        EthBlock.TransactionObject normalTransaction = transaction("0xnormal", "0xfrom", "0xto", 15, 22000, 60);

        EthBlock.Block basicBlock = block(500, "0xblock500", List.of(), null);
        EthBlock.Block detailedBlock = block(
                500,
                "0xblock500",
                List.of(hashOnlyTransaction, normalTransaction),
                null
        );

        rpcResponses.add(ethBlockNumber(500));
        rpcResponses.add(ethBlock(basicBlock));
        rpcResponses.add(ethBlock(detailedBlock));

        RecentBlocksResponse response = blockService.getRecentBlocks(1, 1);

        assertEquals(2, response.getDetailedBlocks().get(0).getTransactionCount());
        assertEquals(1, response.getDetailedBlocks().get(0).getTransactions().size());
        assertEquals("0xnormal", response.getDetailedBlocks().get(0).getTransactions().get(0).getHash());
    }

    @Test
    void shouldPassIOExceptionFromBlockchainClient() throws IOException {
        when(web3jService.send(any(Request.class), any())).thenThrow(new IOException("rpc problem"));

        assertThrows(IOException.class, () -> blockService.getRecentBlocks(1, 1));
    }
}
