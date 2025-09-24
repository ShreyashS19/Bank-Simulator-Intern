package com.bank.simulator.service;

import com.bank.simulator.model.Transaction;
import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    // Core CRUD operations
    String createTransaction(Transaction transaction);
    Transaction getTransactionById(String transactionId);
    List<Transaction> getTransactionsByAccountId(String accountId);
    
    // Account and balance operations
    boolean isAccountExists(String accountId);
    BigDecimal getAccountBalance(String accountId);
    String getAccountBalanceDetails(String accountId);
    
    // Transfer operations
    String createAccountTransfer(String fromAccountId, String toAccountId, 
                               BigDecimal amount, String transactionMode, String description);
    
    // Debug utilities
    void debugPrintAccountTransactions(String accountId);
    
    // ID generation
    String generateTransactionId();
}
