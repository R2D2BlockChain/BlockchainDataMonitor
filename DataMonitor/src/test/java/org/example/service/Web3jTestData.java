package org.example.service;

import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.math.BigInteger;
import java.util.List;

final class Web3jTestData {

    private Web3jTestData() {
    }

    static EthBlockNumber ethBlockNumber(long number) {
        EthBlockNumber response = new EthBlockNumber();
        response.setResult(hex(number));
        return response;
    }

    static EthBlock ethBlock(EthBlock.Block block) {
        EthBlock response = new EthBlock();
        response.setResult(block);
        return response;
    }

    static EthBlock.Block block(
            long number,
            String hash,
            List<EthBlock.TransactionResult> transactions,
            BigInteger baseFee
    ) {
        EthBlock.Block block = new EthBlock.Block();
        block.setNumber(hex(number));
        block.setHash(hash);
        block.setTimestamp(hex(1710000000L + number));
        block.setTransactions(transactions);
        block.setGasUsed(hex(100000 + number));
        block.setParentHash("0xparent" + number);
        block.setMiner("0xminer");
        block.setNonce("0xnonce");
        block.setSize(hex(900 + number));
        block.setGasLimit(hex(30000000));
        block.setBaseFeePerGas(baseFee == null ? null : hex(baseFee.longValue()));
        return block;
    }

    static EthBlock.TransactionObject transaction(
            String hash,
            String from,
            String to,
            long value,
            long gas,
            long gasPrice
    ) {
        EthBlock.TransactionObject tx = new EthBlock.TransactionObject();
        tx.setHash(hash);
        tx.setFrom(from);
        tx.setTo(to);
        tx.setValue(hex(value));
        tx.setGas(hex(gas));
        tx.setGasPrice(hex(gasPrice));
        return tx;
    }

    static EthBlock.TransactionHash transactionHash(String hash) {
        return new EthBlock.TransactionHash(hash);
    }

    private static String hex(long value) {
        return "0x" + Long.toHexString(value);
    }
}
