package org.example.service;

import org.example.api.RecentBlocksResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

class BlockServiceTest {

    private Web3j web3j;
    private BlockService blockService;

    @BeforeEach
    void setUp() {
        web3j = mock(Web3j.class);
        blockService = new BlockService(web3j);
    }

    @Test
    void shouldReturnLatestBlockNumberFromWeb3j() throws IOException, InterruptedException {
        @SuppressWarnings("unchecked")
        Request<?, EthBlockNumber> request = (Request<?, EthBlockNumber>) mock(Request.class);
        EthBlockNumber ethBlockNumber = mock(EthBlockNumber.class);

        doReturn(request).when(web3j).ethBlockNumber();
        when(request.send()).thenReturn(ethBlockNumber);
        when(ethBlockNumber.getBlockNumber()).thenReturn(BigInteger.valueOf(12345));

        BigInteger result = blockService.getLatestBlock();

        assertEquals(BigInteger.valueOf(12345), result);
    }

    @Test
    void shouldReadBasicBlockFields() throws IOException, InterruptedException {
        EthBlock.Block block = mock(EthBlock.Block.class);
        when(block.getNumber()).thenReturn(BigInteger.valueOf(100));
        when(block.getHash()).thenReturn("0xabc123");

        assertEquals("100", blockService.getBlockNumber(block));
        assertEquals("0xabc123", blockService.getBlockHash(block));
    }

    @Test
    void shouldReturnTransactionCountFromBlock() throws IOException, InterruptedException {
        EthBlock.Block block = mock(EthBlock.Block.class);
        List<EthBlock.TransactionResult> transactions = List.of(
                mock(EthBlock.TransactionResult.class),
                mock(EthBlock.TransactionResult.class),
                mock(EthBlock.TransactionResult.class)
        );
        when(block.getTransactions()).thenReturn(transactions);

        String result = blockService.getBlockTransactionCount(block);

        assertEquals("3", result);
    }

    @Test
    void shouldReturnZeroTransactionCountForEmptyList() throws IOException, InterruptedException {
        EthBlock.Block block = mock(EthBlock.Block.class);
        when(block.getTransactions()).thenReturn(List.of());

        String result = blockService.getBlockTransactionCount(block);

        assertEquals("0", result);
    }

    @Test
    void shouldReturnTransactionsListFromBlock() throws IOException, InterruptedException {
        EthBlock.Block block = mock(EthBlock.Block.class);
        List<EthBlock.TransactionResult> transactions = List.of(mock(EthBlock.TransactionResult.class));
        when(block.getTransactions()).thenReturn(transactions);

        List<EthBlock.TransactionResult> result = blockService.getTransactionsList(block);

        assertEquals(transactions, result);
    }

    @Test
    void shouldReturnSingleTransactionByIndex() throws IOException, InterruptedException {
        EthBlock.TransactionObject tx = mock(EthBlock.TransactionObject.class);
        List<EthBlock.TransactionResult> transactions = List.of(tx);

        EthBlock.TransactionObject result = blockService.getSingleTransaction(transactions, 0);

        assertEquals(tx, result);
    }

    @Test
    void shouldThrowForInvalidTransactionIndex() {
        EthBlock.TransactionObject tx = mock(EthBlock.TransactionObject.class);
        List<EthBlock.TransactionResult> transactions = List.of(tx);

        assertThrows(IndexOutOfBoundsException.class, () -> blockService.getSingleTransaction(transactions, 10));
    }

    @Test
    void shouldThrowWhenTransactionHasUnexpectedType() {
        EthBlock.TransactionResult<String> invalid = mock(EthBlock.TransactionResult.class);
        List<EthBlock.TransactionResult> transactions = List.of(invalid);

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
        EthBlock.TransactionObject tx = mock(EthBlock.TransactionObject.class);
        when(tx.getHash()).thenReturn("0xhash");
        when(tx.getFrom()).thenReturn("0xfrom");
        when(tx.getTo()).thenReturn("0xto");
        when(tx.getValue()).thenReturn(BigInteger.valueOf(50));
        when(tx.getGas()).thenReturn(BigInteger.valueOf(21000));

        assertEquals("0xhash", blockService.getSingleTransactionHash(tx));
        assertEquals("0xfrom", blockService.getSingleTransactionSender(tx));
        assertEquals("0xto", blockService.getSingleTransactionReceiver(tx));
        assertEquals("50", blockService.getSingleTransactionValue(tx));
        assertEquals("21000", blockService.getSingleTransactionGas(tx));
    }

    @Test
    void shouldBuildRecentBlocksResponseForBasicAndDetailedBlocks() throws IOException, InterruptedException {
        mockLatestBlock(200);

        EthBlock.TransactionObject tx = mockTransaction("0xtx", "0xfrom", "0xto", 12, 21000, 50);
        EthBlock.Block block200Basic = mockBlock(200, "0xblock200", List.of(), null);
        EthBlock.Block block199Basic = mockBlock(199, "0xblock199", List.of(), null);
        EthBlock.Block block200Detailed = mockBlock(200, "0xblock200", List.of(tx), BigInteger.valueOf(1000));

        mockBlockRequest(false, block200Basic, block199Basic);
        mockBlockRequest(true, block200Detailed);

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
        mockLatestBlock(300);
        EthBlock.Block basicBlock = mockBlock(300, "0xblock300", List.of(), null);
        EthBlock.Block detailedBlock = mockBlock(300, "0xblock300", List.of(), null);

        mockBlockRequest(false, basicBlock);
        mockBlockRequest(true, detailedBlock);

        RecentBlocksResponse response = blockService.getRecentBlocks(0, -5);

        assertEquals(1, response.getBasicBlocks().size());
        assertEquals(1, response.getDetailedBlocks().size());
        assertEquals(300, response.getBasicBlocks().get(0).getNumber());
        assertNull(response.getDetailedBlocks().get(0).getBaseFeePerGasWei());
    }

    @Test
    void shouldNotCreateMoreDetailedBlocksThanBasicBlocks() throws IOException, InterruptedException {
        mockLatestBlock(400);
        EthBlock.Block basicBlock = mockBlock(400, "0xblock400", List.of(), null);
        EthBlock.Block detailedBlock = mockBlock(400, "0xblock400", List.of(), null);

        mockBlockRequest(false, basicBlock);
        mockBlockRequest(true, detailedBlock);

        RecentBlocksResponse response = blockService.getRecentBlocks(1, 10);

        assertEquals(1, response.getBasicBlocks().size());
        assertEquals(1, response.getDetailedBlocks().size());
    }

    @Test
    void shouldSkipHashOnlyTransactionsInDetailedBlock() throws IOException, InterruptedException {
        mockLatestBlock(500);

        @SuppressWarnings("unchecked")
        EthBlock.TransactionResult<String> hashOnlyTransaction = mock(EthBlock.TransactionResult.class);
        EthBlock.TransactionObject normalTransaction = mockTransaction("0xnormal", "0xfrom", "0xto", 15, 22000, 60);

        EthBlock.Block basicBlock = mockBlock(500, "0xblock500", List.of(), null);
        EthBlock.Block detailedBlock = mockBlock(
                500,
                "0xblock500",
                List.of(hashOnlyTransaction, normalTransaction),
                null
        );

        mockBlockRequest(false, basicBlock);
        mockBlockRequest(true, detailedBlock);

        RecentBlocksResponse response = blockService.getRecentBlocks(1, 1);

        assertEquals(2, response.getDetailedBlocks().get(0).getTransactionCount());
        assertEquals(1, response.getDetailedBlocks().get(0).getTransactions().size());
        assertEquals("0xnormal", response.getDetailedBlocks().get(0).getTransactions().get(0).getHash());
    }

    @Test
    void shouldPassIOExceptionFromBlockchainClient() throws IOException {
        @SuppressWarnings("unchecked")
        Request<?, EthBlockNumber> request = (Request<?, EthBlockNumber>) mock(Request.class);

        doReturn(request).when(web3j).ethBlockNumber();
        when(request.send()).thenThrow(new IOException("rpc problem"));

        assertThrows(IOException.class, () -> blockService.getRecentBlocks(1, 1));
    }

    private void mockLatestBlock(long number) throws IOException {
        @SuppressWarnings("unchecked")
        Request<?, EthBlockNumber> request = (Request<?, EthBlockNumber>) mock(Request.class);
        EthBlockNumber ethBlockNumber = mock(EthBlockNumber.class);

        doReturn(request).when(web3j).ethBlockNumber();
        when(request.send()).thenReturn(ethBlockNumber);
        when(ethBlockNumber.getBlockNumber()).thenReturn(BigInteger.valueOf(number));
    }

    private void mockBlockRequest(boolean fullTransactionObjects, EthBlock.Block... blocks) throws IOException {
        @SuppressWarnings("unchecked")
        Request<?, EthBlock> request = (Request<?, EthBlock>) mock(Request.class);
        EthBlock[] responses = new EthBlock[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            responses[i] = mock(EthBlock.class);
            when(responses[i].getBlock()).thenReturn(blocks[i]);
        }

        doReturn(request).when(web3j).ethGetBlockByNumber(
                any(DefaultBlockParameter.class),
                eq(fullTransactionObjects)
        );
        if (responses.length == 1) {
            when(request.send()).thenReturn(responses[0]);
        } else {
            EthBlock[] nextResponses = Arrays.copyOfRange(responses, 1, responses.length);
            when(request.send()).thenReturn(responses[0], nextResponses);
        }
    }

    private EthBlock.Block mockBlock(
            long number,
            String hash,
            List<EthBlock.TransactionResult> transactions,
            BigInteger baseFee
    ) {
        EthBlock.Block block = mock(EthBlock.Block.class);

        when(block.getNumber()).thenReturn(BigInteger.valueOf(number));
        when(block.getHash()).thenReturn(hash);
        when(block.getTimestamp()).thenReturn(BigInteger.valueOf(1710000000L + number));
        when(block.getTransactions()).thenReturn(transactions);
        when(block.getGasUsed()).thenReturn(BigInteger.valueOf(100000 + number));
        when(block.getParentHash()).thenReturn("0xparent" + number);
        when(block.getMiner()).thenReturn("0xminer");
        when(block.getNonceRaw()).thenReturn("0xnonce");
        when(block.getSize()).thenReturn(BigInteger.valueOf(900 + number));
        when(block.getGasLimit()).thenReturn(BigInteger.valueOf(30000000));
        when(block.getBaseFeePerGas()).thenReturn(baseFee);

        return block;
    }

    private EthBlock.TransactionObject mockTransaction(
            String hash,
            String from,
            String to,
            long value,
            long gas,
            long gasPrice
    ) {
        EthBlock.TransactionObject tx = mock(EthBlock.TransactionObject.class);

        when(tx.getHash()).thenReturn(hash);
        when(tx.getFrom()).thenReturn(from);
        when(tx.getTo()).thenReturn(to);
        when(tx.getValue()).thenReturn(BigInteger.valueOf(value));
        when(tx.getGas()).thenReturn(BigInteger.valueOf(gas));
        when(tx.getGasPrice()).thenReturn(BigInteger.valueOf(gasPrice));

        return tx;
    }
}
