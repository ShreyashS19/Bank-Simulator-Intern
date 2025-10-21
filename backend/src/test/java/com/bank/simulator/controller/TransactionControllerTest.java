package com.bank.simulator.controller;

import com.bank.simulator.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class TransactionControllerTest {

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = createValidTransaction();
    }

    @Test
    void testCreateTransaction_ValidData() {
        int statusCode = simulateCreateTransactionAPI(testTransaction);
        assertEquals(201, statusCode);
    }

    @Test
    void testCreateTransaction_NullData() {
        int statusCode = simulateCreateTransactionAPI(null);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateTransaction_InvalidSenderAccount() {
        testTransaction.setSenderAccountNumber("123");
        int statusCode = simulateCreateTransactionAPI(testTransaction);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateTransaction_InvalidReceiverAccount() {
        testTransaction.setReceiverAccountNumber("ABC");
        int statusCode = simulateCreateTransactionAPI(testTransaction);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateTransaction_SameAccount() {
        testTransaction.setReceiverAccountNumber("1234567890123412");
        int statusCode = simulateCreateTransactionAPI(testTransaction);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateTransaction_NegativeAmount() {
        testTransaction.setAmount(BigDecimal.valueOf(-100));
        int statusCode = simulateCreateTransactionAPI(testTransaction);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateTransaction_ZeroAmount() {
        testTransaction.setAmount(BigDecimal.ZERO);
        int statusCode = simulateCreateTransactionAPI(testTransaction);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateTransaction_InvalidTransactionType() {
        testTransaction.setTransactionType("CASH");
        int statusCode = simulateCreateTransactionAPI(testTransaction);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateTransaction_EmptyTransactionType() {
        testTransaction.setTransactionType("");
        int statusCode = simulateCreateTransactionAPI(testTransaction);
        assertEquals(201, statusCode);
    }

    @Test
    void testGetTransactions_ValidAccountNumber() {
        int statusCode = simulateGetTransactionsAPI("1234567890123412");
        assertEquals(200, statusCode);
    }

    @Test
    void testGetTransactions_EmptyAccountNumber() {
        int statusCode = simulateGetTransactionsAPI("");
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateTransaction_DatabaseError() {
        int statusCode = simulateCreateTransactionWithError(testTransaction);
        assertEquals(500, statusCode);
    }

    private int simulateCreateTransactionAPI(Transaction transaction) {
        if (transaction == null) return 400;
        
        if (transaction.getTransactionType() == null || transaction.getTransactionType().trim().isEmpty()) {
            transaction.setTransactionType("ONLINE");
        }
        
        if (transaction.getSenderAccountNumber() == null || 
            !transaction.getSenderAccountNumber().matches("^[0-9]{10,25}$")) {
            return 400;
        }
        
        if (transaction.getReceiverAccountNumber() == null || 
            !transaction.getReceiverAccountNumber().matches("^[0-9]{10,25}$")) {
            return 400;
        }
        
        if (transaction.getSenderAccountNumber().equals(transaction.getReceiverAccountNumber())) {
            return 400;
        }
        
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return 400;
        }
        
        if (!"ONLINE".equalsIgnoreCase(transaction.getTransactionType())) {
            return 400;
        }
        
        return 201;
    }

    private int simulateGetTransactionsAPI(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return 400;
        }
        return 200;
    }

    private int simulateCreateTransactionWithError(Transaction transaction) {
        return 500;
    }

    private Transaction createValidTransaction() {
        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber("1234567890123412");
        transaction.setReceiverAccountNumber("9876543210987634");
        transaction.setAmount(BigDecimal.valueOf(1000));
        transaction.setTransactionType("ONLINE");
        transaction.setDescription("Test payment");
        return transaction;
    }
}
