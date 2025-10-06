package com.bank.simulator.service;

import com.bank.simulator.model.Transaction;
import java.util.List;

public interface TransactionService {
    
    String createTransaction(Transaction transaction);
    
    List<Transaction> getTransactionsByAccountNumber(String accountNumber);
    
    String generateTransactionId();
}
