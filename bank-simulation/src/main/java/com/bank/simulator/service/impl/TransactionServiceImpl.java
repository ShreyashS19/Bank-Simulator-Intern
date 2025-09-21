package com.bank.simulator.service.impl;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Transaction;
import com.bank.simulator.service.TransactionService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionServiceImpl implements TransactionService {
    private static final AtomicInteger transactionCounter = new AtomicInteger(1);

    @Override
    public String createTransaction(Transaction transaction) {
        String transactionId = generateTransactionId();
        transaction.setTransactionId(transactionId);
        
        String query = """
            INSERT INTO Transaction (transaction_id, account_id, transaction_amount, 
                                   transaction_type, transaction_mode, receiver_details, sender_details) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, transaction.getTransactionId());
            stmt.setString(2, transaction.getAccountId());
            stmt.setBigDecimal(3, transaction.getTransactionAmount());
            stmt.setString(4, transaction.getTransactionType());
            stmt.setString(5, transaction.getTransactionMode());
            stmt.setString(6, transaction.getReceiverDetails());
            stmt.setString(7, transaction.getSenderDetails());
            
            int result = stmt.executeUpdate();
            return result > 0 ? transactionId : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Transaction getTransactionById(String transactionId) {
        String query = "SELECT * FROM Transaction WHERE transaction_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setTransactionId(rs.getString("transaction_id"));
                transaction.setAccountId(rs.getString("account_id"));
                transaction.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setTransactionTime(rs.getTimestamp("transaction_time").toLocalDateTime());
                transaction.setTransactionMode(rs.getString("transaction_mode"));
                transaction.setReceiverDetails(rs.getString("receiver_details"));
                transaction.setSenderDetails(rs.getString("sender_details"));
                return transaction;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(String accountId) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM Transaction WHERE account_id = ? ORDER BY transaction_time DESC";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setTransactionId(rs.getString("transaction_id"));
                transaction.setAccountId(rs.getString("account_id"));
                transaction.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setTransactionTime(rs.getTimestamp("transaction_time").toLocalDateTime());
                transaction.setTransactionMode(rs.getString("transaction_mode"));
                transaction.setReceiverDetails(rs.getString("receiver_details"));
                transaction.setSenderDetails(rs.getString("sender_details"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public boolean isAccountExists(String accountId) {
        String query = "SELECT COUNT(*) FROM Account WHERE account_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String generateTransactionId() {
        return "TXN_" + transactionCounter.getAndIncrement();
    }
}
