package com.bank.simulator.service;

import com.bank.simulator.model.Transaction;
import java.util.List;

public interface TransactionService {
    String createTransaction(Transaction transaction);
    Transaction getTransactionById(String transactionId);
    List<Transaction> getTransactionsByAccountId(String accountId);
    boolean isAccountExists(String accountId);
    String generateTransactionId();
}
