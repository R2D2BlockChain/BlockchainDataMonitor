package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}
