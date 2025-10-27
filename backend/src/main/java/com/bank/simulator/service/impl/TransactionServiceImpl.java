package com.bank.simulator.service.impl;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Transaction;
import com.bank.simulator.service.TransactionService;
import com.bank.simulator.service.NotificationService;
import java.sql.Statement;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionServiceImpl implements TransactionService {
    
    private final NotificationService notificationService = new NotificationServiceImpl();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicInteger dailyCounter = new AtomicInteger(1);
    private static volatile String lastDate = "";

    @Override
    public String generateTransactionId() {
        String currentDate = LocalDateTime.now().format(DATE_FORMATTER);
        
        synchronized (TransactionServiceImpl.class) {
            if (!currentDate.equals(lastDate)) {
                lastDate = currentDate;
                int maxCounter = getMaxDailyCounterFromDB(currentDate);
                dailyCounter.set(maxCounter + 1);
                System.out.println("=== NEW DAY DETECTED OR SERVER RESTART ===");
                System.out.println("Date: " + currentDate);
                System.out.println("Starting counter at: " + dailyCounter.get());
            }
            
            int counter = dailyCounter.getAndIncrement();
            String transactionId = String.format("TXN_%s%03d", currentDate, counter);
            
            System.out.println("Generated Transaction ID: " + transactionId);
            return transactionId;
        }
    }

    private int getMaxDailyCounterFromDB(String dateStr) {
        String query = "SELECT transaction_id FROM Transaction WHERE transaction_id LIKE ? ORDER BY transaction_id DESC LIMIT 1";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, "TXN_" + dateStr + "%");
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String lastTxnId = rs.getString("transaction_id");
                String counterPart = lastTxnId.substring(13);
                int maxCounter = Integer.parseInt(counterPart);
                
                System.out.println("Loaded max transaction counter from database:");
                System.out.println("  Last Transaction ID: " + lastTxnId);
                System.out.println("  Max Counter: " + maxCounter);
                
                return maxCounter;
            } else {
                System.out.println("No transactions found for date: " + dateStr);
                return 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Warning: Could not load max transaction counter from database");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Starting counter from 0 for date: " + dateStr);
            return 0;
        }
    }

    @Override
    public String createTransaction(Transaction transaction) {
        System.out.println("\n");
        System.out.println("=== TRANSACTION CREATION STARTED ===");
        System.out.println("Sender Account Number: " + transaction.getSenderAccountNumber());
        System.out.println("Receiver Account Number: " + transaction.getReceiverAccountNumber());
        System.out.println("Amount: " + transaction.getAmount());
        System.out.println("Transaction Type: " + transaction.getTransactionType());
        System.out.println("Description: " + (transaction.getDescription() != null ? transaction.getDescription() : "NULL"));

        Connection conn = null;
        try {
            conn = DBConfig.getConnection();
            conn.setAutoCommit(false);

            String senderAccountId = getAccountIdByAccountNumber(conn, transaction.getSenderAccountNumber());
            String receiverAccountId = getAccountIdByAccountNumber(conn, transaction.getReceiverAccountNumber());

            if (senderAccountId == null) {
                System.err.println("ERROR: Sender account not found");
                return null;
            }

            if (receiverAccountId == null) {
                System.err.println("ERROR: Receiver account not found");
                return null;
            }

            transaction.setAccountId(senderAccountId);

            BigDecimal senderBalance = getAccountBalance(conn, senderAccountId);
            System.out.println("Sender Current Balance: " + senderBalance);

            if (senderBalance.compareTo(transaction.getAmount()) < 0) {
                System.err.println("ERROR: Insufficient balance");
                System.err.println("Available: " + senderBalance + ", Required: " + transaction.getAmount());
                conn.rollback();
                return "INSUFFICIENT_BALANCE";
            }

            String transactionId = generateTransactionId();
            transaction.setTransactionId(transactionId);
            transaction.setCreatedDate(LocalDateTime.now());

            String insertQuery = "INSERT INTO Transaction (transaction_id, account_id, sender_account_number, " +
                               "receiver_account_number, amount, transaction_type, description, created_date) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, transaction.getTransactionId());
                stmt.setString(2, transaction.getAccountId());
                stmt.setString(3, transaction.getSenderAccountNumber());
                stmt.setString(4, transaction.getReceiverAccountNumber());
                stmt.setBigDecimal(5, transaction.getAmount());
                stmt.setString(6, transaction.getTransactionType());
                stmt.setString(7, transaction.getDescription());
                stmt.setTimestamp(8, Timestamp.valueOf(transaction.getCreatedDate()));

                int result = stmt.executeUpdate();
                System.out.println("Transaction record inserted: " + result + " rows");
            }

            String deductQuery = "UPDATE Account SET amount = amount - ? WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deductQuery)) {
                stmt.setBigDecimal(1, transaction.getAmount());
                stmt.setString(2, senderAccountId);
                int result = stmt.executeUpdate();
                System.out.println("Sender balance updated: " + result + " rows");
            }

            String addQuery = "UPDATE Account SET amount = amount + ? WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(addQuery)) {
                stmt.setBigDecimal(1, transaction.getAmount());
                stmt.setString(2, receiverAccountId);
                int result = stmt.executeUpdate();
                System.out.println("Receiver balance updated: " + result + " rows");
            }

            BigDecimal senderNewBalance = getAccountBalance(conn, senderAccountId);
            BigDecimal receiverNewBalance = getAccountBalance(conn, receiverAccountId);
            
            System.out.println("\n");
            System.out.println("=== BALANCE UPDATE SUMMARY ===");
            System.out.println("Sender Previous Balance: " + senderBalance);
            System.out.println("Sender New Balance: " + senderNewBalance);
            System.out.println("Receiver New Balance: " + receiverNewBalance);

            conn.commit();
            System.out.println("\n");
            System.out.println("=== TRANSACTION COMPLETED SUCCESSFULLY ===");
            System.out.println("Transaction ID: " + transactionId);

            try {
                System.out.println("\n=== INITIATING EMAIL NOTIFICATIONS ===");
                sendTransactionEmails(
                    conn,
                    senderAccountId,
                    receiverAccountId,
                    transaction.getSenderAccountNumber(),
                    transaction.getReceiverAccountNumber(),
                    transaction.getAmount(),
                    transactionId
                );
            } catch (Exception emailEx) {
                System.err.println("\nEMAIL NOTIFICATION FAILED (Transaction was successful)");
                System.err.println("Error: " + emailEx.getMessage());
                emailEx.printStackTrace();
            }

            return transactionId;

        } catch (SQLException e) {
            System.out.println("\n");
            System.err.println("=== TRANSACTION FAILED ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back successfully");
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            return null;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    private void sendTransactionEmails(
            Connection conn,
            String senderAccountId,
            String receiverAccountId,
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            String transactionId
    ) throws SQLException {

        String senderQuery = "SELECT c.name, c.email, a.bank_name, a.account_number FROM Customer c " +
                            "JOIN Account a ON c.customer_id = a.customer_id " +
                            "WHERE a.account_id = ?";
        
        String senderName = null;
        String senderEmail = null;
        String senderBankName = null;
        String senderAccNum = null;
        
        try (PreparedStatement stmt = conn.prepareStatement(senderQuery)) {
            stmt.setString(1, senderAccountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                senderName = rs.getString("name");
                senderEmail = rs.getString("email");
                senderBankName = rs.getString("bank_name");
                senderAccNum = rs.getString("account_number");
            }
        }

        String receiverQuery = "SELECT c.name, c.email, a.bank_name, a.account_number FROM Customer c " +
                              "JOIN Account a ON c.customer_id = a.customer_id " +
                              "WHERE a.account_id = ?";
        
        String receiverName = null;
        String receiverEmail = null;
        String receiverBankName = null;
        String receiverAccNum = null;
        
        try (PreparedStatement stmt = conn.prepareStatement(receiverQuery)) {
            stmt.setString(1, receiverAccountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                receiverName = rs.getString("name");
                receiverEmail = rs.getString("email");
                receiverBankName = rs.getString("bank_name");
                receiverAccNum = rs.getString("account_number");
            }
        }

        System.out.println("--- Email Details ---");
        System.out.println("Sender: " + senderName + " <" + senderEmail + ">");
        System.out.println("Sender Bank: " + senderBankName);
        System.out.println("Receiver: " + receiverName + " <" + receiverEmail + ">");
        System.out.println("Receiver Bank: " + receiverBankName);

        if (senderEmail != null && senderName != null && !senderEmail.trim().isEmpty()) {
            System.out.println("\nSending DEBIT notification to sender: " + senderEmail);
            try {
                notificationService.sendTransactionNotificationToSender(
                    senderEmail,
                    senderName,
                    senderBankName != null ? senderBankName : "Bank",  
                    senderAccNum != null ? senderAccNum : senderAccountNumber,
                    receiverAccountNumber,
                    amount,
                    transactionId
                );
                System.out.println("Sender email sent successfully");
            } catch (Exception e) {
                System.err.println("Failed to send email to sender: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Sender email not found or invalid. Skipping sender notification.");
        }

        if (receiverEmail != null && receiverName != null && !receiverEmail.trim().isEmpty()) {
            System.out.println("\nSending CREDIT notification to receiver: " + receiverEmail);
            try {
                notificationService.sendTransactionNotificationToReceiver(
                    receiverEmail,
                    receiverName,
                    receiverBankName != null ? receiverBankName : "Bank",  
                    receiverAccNum != null ? receiverAccNum : receiverAccountNumber,
                    senderAccountNumber,
                    amount,
                    transactionId
                );
                System.out.println("Receiver email sent successfully");
            } catch (Exception e) {
                System.err.println("Failed to send email to receiver: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Receiver email not found or invalid. Skipping receiver notification.");
        }

        System.out.println("=== EMAIL NOTIFICATIONS COMPLETED ===\n");
    }

    @Override
    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        System.out.println("\n");
        System.out.println("=== FETCHING TRANSACTIONS FOR ACCOUNT NUMBER: " + accountNumber + " ===");
        
        List<Transaction> transactions = new ArrayList<>();
        
        String query = "SELECT t.* FROM Account a " +
                      "JOIN Transaction t ON a.account_id = t.account_id " +
                      "WHERE t.sender_account_number = ? OR t.receiver_account_number = ? " +
                      "ORDER BY t.created_date DESC";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountNumber);
            stmt.setString(2, accountNumber);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                
                transaction.setTransactionId(rs.getString("transaction_id"));
                transaction.setSenderAccountNumber(rs.getString("sender_account_number"));
                transaction.setReceiverAccountNumber(rs.getString("receiver_account_number"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setDescription(rs.getString("description"));
                transaction.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
                
                transactions.add(transaction);
            }
            
            System.out.println("Found " + transactions.size() + " transactions");
            
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }

    private String getAccountIdByAccountNumber(Connection conn, String accountNumber) throws SQLException {
        String query = "SELECT account_id FROM Account WHERE account_number = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("account_id");
            }
        }
        
        return null;
    }

    private BigDecimal getAccountBalance(Connection conn, String accountId) throws SQLException {
        String query = "SELECT amount FROM Account WHERE account_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("amount");
            }
        }
        
        return BigDecimal.ZERO;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        System.out.println("\n=== FETCHING ALL TRANSACTIONS ===");
        
        List<Transaction> transactions = new ArrayList<>();
        
        String query = "SELECT * FROM Transaction ORDER BY created_date DESC";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                
                transaction.setTransactionId(rs.getString("transaction_id"));
                transaction.setSenderAccountNumber(rs.getString("sender_account_number"));
                transaction.setReceiverAccountNumber(rs.getString("receiver_account_number"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setDescription(rs.getString("description"));
                transaction.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
                
                transactions.add(transaction);
            }
            
            System.out.println("Total transactions fetched: " + transactions.size());
            
        } catch (SQLException e) {
            System.err.println("Error fetching all transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
}
