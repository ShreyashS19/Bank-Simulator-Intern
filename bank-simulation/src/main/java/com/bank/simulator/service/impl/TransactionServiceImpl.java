package com.bank.simulator.service.impl;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Transaction;
import com.bank.simulator.service.TransactionService;

import java.math.BigDecimal;
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
        
        String insertQuery = """
            INSERT INTO Transaction (transaction_id, account_id, transaction_amount, 
                                   transaction_type, transaction_mode, receiver_details, sender_details) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        String updateBalanceQuery = """
            UPDATE Account SET balance = balance + ? WHERE account_id = ?
        """;
        
        String checkBalanceQuery = """
            SELECT balance FROM Account WHERE account_id = ?
        """;
        
        Connection conn = null;
        try {
            conn = DBConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Check current balance BEFORE transaction
            BigDecimal balanceBefore = BigDecimal.ZERO;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkBalanceQuery)) {
                checkStmt.setString(1, transaction.getAccountId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    balanceBefore = rs.getBigDecimal("balance");
                    System.out.println("=== BEFORE TRANSACTION DEBUG ===");
                    System.out.println("Account ID: " + transaction.getAccountId());
                    System.out.println("Current Balance: " + balanceBefore);
                    System.out.println("Transaction Type: " + transaction.getTransactionType());
                    System.out.println("Transaction Amount: " + transaction.getTransactionAmount());
                }
            }
            
            // For debit transactions, check sufficient balance
            if ("debited".equals(transaction.getTransactionType())) {
                if (balanceBefore.compareTo(transaction.getTransactionAmount()) < 0) {
                    conn.rollback();
                    System.err.println("=== INSUFFICIENT BALANCE ERROR ===");
                    System.err.println("Current Balance: " + balanceBefore);
                    System.err.println("Requested Amount: " + transaction.getTransactionAmount());
                    System.err.println("Shortage: " + transaction.getTransactionAmount().subtract(balanceBefore));
                    return null;
                }
            }
            
            // Insert transaction
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, transaction.getTransactionId());
                insertStmt.setString(2, transaction.getAccountId());
                insertStmt.setBigDecimal(3, transaction.getTransactionAmount());
                insertStmt.setString(4, transaction.getTransactionType());
                insertStmt.setString(5, transaction.getTransactionMode());
                insertStmt.setString(6, transaction.getReceiverDetails());
                insertStmt.setString(7, transaction.getSenderDetails());
                insertStmt.executeUpdate();
                
                System.out.println("=== TRANSACTION INSERTED ===");
                System.out.println("Transaction ID: " + transactionId);
                System.out.println("Account ID: " + transaction.getAccountId());
                System.out.println("Amount: " + transaction.getTransactionAmount());
                System.out.println("Type: " + transaction.getTransactionType());
            }
            
            // Calculate amount to add/subtract
            BigDecimal amountToUpdate = "credited".equals(transaction.getTransactionType()) 
                ? transaction.getTransactionAmount() 
                : transaction.getTransactionAmount().negate();
                
            System.out.println("=== BALANCE UPDATE DEBUG ===");
            System.out.println("Amount to update balance: " + amountToUpdate);
            System.out.println("Update operation: balance = balance + (" + amountToUpdate + ")");
            
            // Update balance
            try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceQuery)) {
                updateStmt.setBigDecimal(1, amountToUpdate);
                updateStmt.setString(2, transaction.getAccountId());
                int rowsUpdated = updateStmt.executeUpdate();
                
                System.out.println("Balance update executed - Rows affected: " + rowsUpdated);
                
                if (rowsUpdated == 0) {
                    System.err.println("WARNING: No rows updated! Account might not exist.");
                }
            }
            
            // Check balance AFTER transaction
            try (PreparedStatement checkStmt = conn.prepareStatement(checkBalanceQuery)) {
                checkStmt.setString(1, transaction.getAccountId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    BigDecimal balanceAfter = rs.getBigDecimal("balance");
                    BigDecimal actualChange = balanceAfter.subtract(balanceBefore);
                    
                    System.out.println("=== AFTER TRANSACTION DEBUG ===");
                    System.out.println("Previous Balance: " + balanceBefore);
                    System.out.println("New Balance: " + balanceAfter);
                    System.out.println("Actual Change: " + actualChange);
                    System.out.println("Expected Change: " + amountToUpdate);
                    
                    if (!actualChange.equals(amountToUpdate)) {
                        System.err.println("WARNING: Balance change mismatch!");
                        System.err.println("Expected: " + amountToUpdate + ", Actual: " + actualChange);
                    }
                }
            }
            
            conn.commit();
            System.out.println("=== TRANSACTION COMMITTED SUCCESSFULLY ===");
            System.out.println("Transaction ID: " + transactionId);
            return transactionId;
            
        } catch (SQLException e) {
            System.err.println("=== SQL ERROR IN TRANSACTION ===");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Message: " + e.getMessage());
            e.printStackTrace();
            
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back successfully");
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace();
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

    /**
     * Enhanced Solution - Account-to-Account Transfer
     * Transfers money from one account to another with proper debit/credit transactions
     */
    public String createAccountTransfer(String fromAccountId, String toAccountId, 
                                       BigDecimal amount, String transactionMode, String description) {
        
        System.out.println("=== ACCOUNT-TO-ACCOUNT TRANSFER STARTED ===");
        System.out.println("From Account: " + fromAccountId);
        System.out.println("To Account: " + toAccountId);
        System.out.println("Amount: " + amount);
        System.out.println("Mode: " + transactionMode);
        
        Connection conn = null;
        String debitTransactionId = null;
        String creditTransactionId = null;
        
        try {
            conn = DBConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Check if both accounts exist
            if (!isAccountExists(fromAccountId)) {
                System.err.println("ERROR: Sender account does not exist: " + fromAccountId);
                return null;
            }
            
            if (!isAccountExists(toAccountId)) {
                System.err.println("ERROR: Receiver account does not exist: " + toAccountId);
                return null;
            }
            
            // Check sender balance
            String checkBalanceQuery = "SELECT balance FROM Account WHERE account_id = ?";
            BigDecimal senderBalance = BigDecimal.ZERO;
            BigDecimal receiverBalanceBefore = BigDecimal.ZERO;
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkBalanceQuery)) {
                // Check sender balance
                checkStmt.setString(1, fromAccountId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    senderBalance = rs.getBigDecimal("balance");
                    System.out.println("Sender current balance: " + senderBalance);
                    
                    if (senderBalance.compareTo(amount) < 0) {
                        conn.rollback();
                        System.err.println("INSUFFICIENT BALANCE FOR TRANSFER");
                        System.err.println("Available: " + senderBalance + ", Required: " + amount);
                        return null;
                    }
                }
                
                // Check receiver balance
                checkStmt.setString(1, toAccountId);
                rs = checkStmt.executeQuery();
                if (rs.next()) {
                    receiverBalanceBefore = rs.getBigDecimal("balance");
                    System.out.println("Receiver current balance: " + receiverBalanceBefore);
                }
            }
            
            // Generate transaction IDs
            debitTransactionId = generateTransactionId();
            creditTransactionId = generateTransactionId();
            
            System.out.println("Generated Debit Transaction ID: " + debitTransactionId);
            System.out.println("Generated Credit Transaction ID: " + creditTransactionId);
            
            // Insert transactions
            String insertQuery = """
                INSERT INTO Transaction (transaction_id, account_id, transaction_amount, 
                                       transaction_type, transaction_mode, receiver_details, sender_details) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            
            // Insert debit transaction for sender
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, debitTransactionId);
                stmt.setString(2, fromAccountId);
                stmt.setBigDecimal(3, amount);
                stmt.setString(4, "debited");
                stmt.setString(5, transactionMode);
                stmt.setString(6, "Transfer to " + toAccountId + " - " + description);
                stmt.setString(7, "Account " + fromAccountId);
                stmt.executeUpdate();
                System.out.println("Debit transaction inserted for sender");
            }
            
            // Insert credit transaction for receiver
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, creditTransactionId);
                stmt.setString(2, toAccountId);
                stmt.setBigDecimal(3, amount);
                stmt.setString(4, "credited");
                stmt.setString(5, transactionMode);
                stmt.setString(6, "Transfer from " + fromAccountId + " - " + description);
                stmt.setString(7, "Account " + fromAccountId);
                stmt.executeUpdate();
                System.out.println("Credit transaction inserted for receiver");
            }
            
            // Update sender balance (subtract)
            String updateQuery = "UPDATE Account SET balance = balance - ? WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setBigDecimal(1, amount);
                stmt.setString(2, fromAccountId);
                int rowsUpdated = stmt.executeUpdate();
                System.out.println("Sender balance updated - Rows affected: " + rowsUpdated);
            }
            
            // Update receiver balance (add)
            updateQuery = "UPDATE Account SET balance = balance + ? WHERE account_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setBigDecimal(1, amount);
                stmt.setString(2, toAccountId);
                int rowsUpdated = stmt.executeUpdate();
                System.out.println("Receiver balance updated - Rows affected: " + rowsUpdated);
            }
            
            // Verify balances after transfer
            try (PreparedStatement checkStmt = conn.prepareStatement(checkBalanceQuery)) {
                // Check sender final balance
                checkStmt.setString(1, fromAccountId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    BigDecimal senderFinalBalance = rs.getBigDecimal("balance");
                    System.out.println("=== SENDER ACCOUNT FINAL ===");
                    System.out.println("Previous Balance: " + senderBalance);
                    System.out.println("Final Balance: " + senderFinalBalance);
                    System.out.println("Change: " + senderFinalBalance.subtract(senderBalance));
                }
                
                // Check receiver final balance
                checkStmt.setString(1, toAccountId);
                rs = checkStmt.executeQuery();
                if (rs.next()) {
                    BigDecimal receiverFinalBalance = rs.getBigDecimal("balance");
                    System.out.println("=== RECEIVER ACCOUNT FINAL ===");
                    System.out.println("Previous Balance: " + receiverBalanceBefore);
                    System.out.println("Final Balance: " + receiverFinalBalance);
                    System.out.println("Change: " + receiverFinalBalance.subtract(receiverBalanceBefore));
                }
            }
            
            conn.commit();
            System.out.println("=== ACCOUNT TRANSFER COMPLETED SUCCESSFULLY ===");
            System.out.println("Debit Transaction ID: " + debitTransactionId);
            System.out.println("Credit Transaction ID: " + creditTransactionId);
            
            return debitTransactionId; // Return the debit transaction ID as primary reference
            
        } catch (SQLException e) {
            System.err.println("=== ERROR IN ACCOUNT TRANSFER ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Account transfer rolled back successfully");
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace();
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

    /**
     * Get current balance for an account
     */
    public BigDecimal getAccountBalance(String accountId) {
        String query = "SELECT balance FROM Account WHERE account_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal balance = rs.getBigDecimal("balance");
                System.out.println("Account " + accountId + " current balance: " + balance);
                return balance;
            }
        } catch (SQLException e) {
            System.err.println("Error getting account balance for " + accountId);
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Get account balance with account details
     */
    public String getAccountBalanceDetails(String accountId) {
        String query = """
            SELECT a.account_id, a.account_name, a.balance, c.name as customer_name 
            FROM Account a 
            JOIN Customer c ON a.customer_id = c.customer_id 
            WHERE a.account_id = ?
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String details = String.format(
                    "Account: %s | Customer: %s | Account Name: %s | Balance: %s",
                    rs.getString("account_id"),
                    rs.getString("customer_name"),
                    rs.getString("account_name"),
                    rs.getBigDecimal("balance")
                );
                System.out.println("Account details: " + details);
                return details;
            }
        } catch (SQLException e) {
            System.err.println("Error getting account details for " + accountId);
            e.printStackTrace();
        }
        return "Account not found: " + accountId;
    }

    @Override
    public boolean isAccountExists(String accountId) {
        String query = "SELECT COUNT(*) FROM Account WHERE account_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                System.out.println("Account " + accountId + " exists: " + exists);
                return exists;
            }
        } catch (SQLException e) {
            System.err.println("Error checking account existence for " + accountId);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String generateTransactionId() {
        return "TXN_" + transactionCounter.getAndIncrement();
    }

    /**
     * Debug method to print all transactions for an account
     */
    public void debugPrintAccountTransactions(String accountId) {
        System.out.println("=== ALL TRANSACTIONS FOR ACCOUNT " + accountId + " ===");
        List<Transaction> transactions = getTransactionsByAccountId(accountId);
        
        if (transactions.isEmpty()) {
            System.out.println("No transactions found for account: " + accountId);
        } else {
            for (Transaction txn : transactions) {
                System.out.printf("ID: %s | Type: %s | Amount: %s | Time: %s | Mode: %s%n",
                    txn.getTransactionId(),
                    txn.getTransactionType(),
                    txn.getTransactionAmount(),
                    txn.getTransactionTime(),
                    txn.getTransactionMode()
                );
            }
        }
        
        // Print current balance
        BigDecimal balance = getAccountBalance(accountId);
        System.out.println("Current Balance: " + balance);
        System.out.println("=== END TRANSACTION LIST ===");
    }
}
