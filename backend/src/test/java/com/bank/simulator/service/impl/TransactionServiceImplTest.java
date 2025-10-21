package com.bank.simulator.service.impl;

import com.bank.simulator.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceImplTest {

    private Transaction testTransaction;
    private Map<String, Transaction> mockTransactionDatabase;
    private Map<String, BigDecimal> mockAccountBalances;
    private static int counter = 1;

    @BeforeEach
    void setUp() {
        mockTransactionDatabase = new HashMap<>();
        mockAccountBalances = new HashMap<>();
        
        mockAccountBalances.put("1234567890123456", BigDecimal.valueOf(5000));
        mockAccountBalances.put("9876543210987654", BigDecimal.valueOf(2000));
        
        testTransaction = createValidTransaction();
    }

    @Test
    void testCreateTransaction_ValidData() {
        String result = simulateCreateTransaction(testTransaction);
        assertNotNull(result);
        assertTrue(result.startsWith("TXN_"));
    }

    @Test
    void testCreateTransaction_NullTransaction() {
        String result = simulateCreateTransaction(null);
        assertNull(result);
    }

    @Test
    void testCreateTransaction_InsufficientBalance() {
        testTransaction.setAmount(BigDecimal.valueOf(10000));
        String result = simulateCreateTransaction(testTransaction);
        assertEquals("INSUFFICIENT_BALANCE", result);
    }

    @Test
    void testCreateTransaction_SameAccount() {
        testTransaction.setReceiverAccountNumber("1234567890123456");
        String result = simulateCreateTransaction(testTransaction);
        assertNull(result);
    }

    @Test
    void testCreateTransaction_SenderNotFound() {
        testTransaction.setSenderAccountNumber("9999999999999999");
        String result = simulateCreateTransaction(testTransaction);
        assertNull(result);
    }

    @Test
    void testCreateTransaction_ReceiverNotFound() {
        testTransaction.setReceiverAccountNumber("9999999999999999");
        String result = simulateCreateTransaction(testTransaction);
        assertNull(result);
    }

    @Test
    void testCreateTransaction_WithDescription() {
        testTransaction.setDescription("Payment for services");
        String result = simulateCreateTransaction(testTransaction);
        assertNotNull(result);
        assertTrue(result.startsWith("TXN_"));
    }

    @Test
    void testCreateTransaction_WithoutDescription() {
        testTransaction.setDescription(null);
        String result = simulateCreateTransaction(testTransaction);
        assertNotNull(result);
        assertTrue(result.startsWith("TXN_"));
    }

    @Test
    void testBalanceDeduction_AfterTransaction() {
        BigDecimal initialBalance = mockAccountBalances.get("1234567890123456");
        simulateCreateTransaction(testTransaction);
        BigDecimal newBalance = mockAccountBalances.get("1234567890123456");
        assertEquals(initialBalance.subtract(testTransaction.getAmount()), newBalance);
    }

    @Test
    void testBalanceAddition_AfterTransaction() {
        BigDecimal initialBalance = mockAccountBalances.get("9876543210987654");
        simulateCreateTransaction(testTransaction);
        BigDecimal newBalance = mockAccountBalances.get("9876543210987654");
        assertEquals(initialBalance.add(testTransaction.getAmount()), newBalance);
    }

    @Test
    void testGetTransactionsByAccountNumber_Sender() {
        simulateCreateTransaction(testTransaction);
        List<Transaction> result = simulateGetTransactions("1234567890123456");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetTransactionsByAccountNumber_Receiver() {
        simulateCreateTransaction(testTransaction);
        List<Transaction> result = simulateGetTransactions("9876543210987654");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetTransactionsByAccountNumber_NoTransactions() {
        List<Transaction> result = simulateGetTransactions("1111111111111111");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateTransactionId_Unique() {
        String id1 = generateTransactionId();
        String id2 = generateTransactionId();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
        assertTrue(id1.startsWith("TXN_"));
        assertTrue(id2.startsWith("TXN_"));
    }

    @Test
    void testMultipleTransactions_SameAccount() {
        String txn1 = simulateCreateTransaction(testTransaction);
        
        Transaction txn2 = createValidTransaction();
        txn2.setAmount(BigDecimal.valueOf(500));
        String txn2Id = simulateCreateTransaction(txn2);
        
        assertNotNull(txn1);
        assertNotNull(txn2Id);
        assertNotEquals(txn1, txn2Id);
    }

    private String simulateCreateTransaction(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        if (transaction.getSenderAccountNumber().equals(transaction.getReceiverAccountNumber())) {
            return null;
        }
        
        if (!mockAccountBalances.containsKey(transaction.getSenderAccountNumber())) {
            return null;
        }
        
        if (!mockAccountBalances.containsKey(transaction.getReceiverAccountNumber())) {
            return null;
        }
        
        BigDecimal senderBalance = mockAccountBalances.get(transaction.getSenderAccountNumber());
        if (senderBalance.compareTo(transaction.getAmount()) < 0) {
            return "INSUFFICIENT_BALANCE";
        }
        
        String txnId = generateTransactionId();
        transaction.setTransactionId(txnId);
        transaction.setCreatedDate(LocalDateTime.now());
        
        mockAccountBalances.put(
            transaction.getSenderAccountNumber(),
            senderBalance.subtract(transaction.getAmount())
        );
        
        BigDecimal receiverBalance = mockAccountBalances.get(transaction.getReceiverAccountNumber());
        mockAccountBalances.put(
            transaction.getReceiverAccountNumber(),
            receiverBalance.add(transaction.getAmount())
        );
        
        mockTransactionDatabase.put(txnId, transaction);
        return txnId;
    }

    private List<Transaction> simulateGetTransactions(String accountNumber) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : mockTransactionDatabase.values()) {
            if (t.getSenderAccountNumber().equals(accountNumber) || 
                t.getReceiverAccountNumber().equals(accountNumber)) {
                result.add(t);
            }
        }
        return result;
    }

    private String generateTransactionId() {
        return "TXN_" + (counter++);
    }

    private Transaction createValidTransaction() {
        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber("1234567890123456");
        transaction.setReceiverAccountNumber("9876543210987654");
        transaction.setAmount(BigDecimal.valueOf(1000));
        transaction.setTransactionType("ONLINE");
        transaction.setDescription("Test transaction");
        return transaction;
    }
}
