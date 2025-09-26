package com.bank.simulator.service.impl;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Account;
import com.bank.simulator.service.AccountService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountServiceImpl implements AccountService {
    private static final AtomicInteger accountCounter = new AtomicInteger(1);
    private static final AtomicInteger customerCounter = new AtomicInteger(1);

    @Override
    public String createAccount(Account account) {
        String accountId = generateAccountId();
        account.setAccountId(accountId);
        
        System.out.println("=== ACCOUNT CREATION DEBUG STARTED ===");
        System.out.println("Generated Account ID: " + accountId);
        System.out.println("Aadhar Number to validate: " + account.getAadharNumber());
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("IFSC Code: " + account.getIfscCode());
        
        // Step 1: Find customer by aadhar number and auto-generate/link customerId
        String customerInfo = findAndLinkCustomerByAadhar(account.getAadharNumber());
        
        if (customerInfo.startsWith("ERROR:")) {
            System.err.println("Error: " + customerInfo);
            return customerInfo; // Return error code
        }
        
        // Set the found/generated customer ID
        account.setCustomerId(customerInfo);
        System.out.println("Auto-linked Customer ID: " + customerInfo);
        
        // Step 2: Get customer's phone number for linking
        String customerPhone = getCustomerPhoneByCustomerId(customerInfo);
        if (customerPhone == null) {
            System.err.println("Error: Could not retrieve customer phone number");
            return "ERROR_CUSTOMER_PHONE";
        }
        
        // Step 3: Auto-set phone number linked to customer's phone
        account.setPhoneNumberLinked(customerPhone);
        System.out.println("Auto-linked Phone Number: " + customerPhone);
        
        // Step 4: Check if account number already exists
        if (isAccountNumberExists(account.getAccountNumber())) {
            System.err.println("Error: Duplicate account creation attempted");
            System.err.println("Account Number: " + account.getAccountNumber() + " already exists");
            return "ACCOUNT_NUMBER_EXISTS";
        }
        
        // Step 5: Insert account with new structure
        String query = """
            INSERT INTO Account (account_id, customer_id, account_number, aadhar_number, 
                               ifsc_code, phone_number_linked, amount, bank_name, 
                               name_on_account, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            System.out.println("=== INSERTING ACCOUNT RECORD ===");
            
            stmt.setString(1, account.getAccountId());
            stmt.setString(2, account.getCustomerId());
            stmt.setString(3, account.getAccountNumber());
            stmt.setString(4, account.getAadharNumber());
            stmt.setString(5, account.getIfscCode());
            stmt.setString(6, account.getPhoneNumberLinked());
            stmt.setBigDecimal(7, account.getAmount());
            stmt.setString(8, account.getBankName());
            stmt.setString(9, account.getNameOnAccount());
            stmt.setString(10, account.getStatus());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("\n"); // Empty line for readability
                System.out.println("Account created successfully for Customer ID: " + account.getCustomerId());
                System.out.println("=== ACCOUNT CREATION SUCCESS DETAILS ===");
                System.out.println("Account ID: " + accountId);
                System.out.println("Customer ID: " + account.getCustomerId());
                System.out.println("Account Number: " + account.getAccountNumber());
                System.out.println("Phone Linked: " + account.getPhoneNumberLinked());
                System.out.println("Amount: " + account.getAmount());
                System.out.println("Status: " + account.getStatus());
                System.out.println("Bank Name: " + account.getBankName());
                System.out.println("Name on Account: " + account.getNameOnAccount());
                System.out.println("IFSC Code: " + account.getIfscCode());
                System.out.println("=== END ACCOUNT CREATION ===");
                System.out.println("\n"); // Empty line after success
                return accountId;
            } else {
                System.err.println("Error: Account creation failed - database insertion returned 0 rows");
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("Error: Database error during account creation");
            System.err.println("SQL Error: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find customer by aadhar number and return customer ID
     * If customer doesn't exist, return appropriate error
     */
    private String findAndLinkCustomerByAadhar(String aadharNumber) {
        String query = "SELECT customer_id FROM Customer WHERE aadhar_number = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, aadharNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String customerId = rs.getString("customer_id");
                System.out.println("✓ Found existing customer with Aadhar: " + aadharNumber);
                System.out.println("✓ Customer ID: " + customerId);
                return customerId;
            } else {
                System.err.println("✗ Aadhar number not found in Customer database");
                System.err.println("✗ Aadhar: " + aadharNumber);
                return "ERROR: Aadhar number is not linked with any customer";
            }
            
        } catch (SQLException e) {
            System.err.println("Error: Database error while validating Aadhar number");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: Database error during Aadhar validation";
        }
    }

    /**
     * Get customer's phone number by customer ID
     */
    private String getCustomerPhoneByCustomerId(String customerId) {
        String query = "SELECT phone_number FROM Customer WHERE customer_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("phone_number");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting customer phone: " + e.getMessage());
        }
        
        return null;
    }

    @Override
    public Account getAccountById(String accountId) {
        String query = """
            SELECT a.*, c.phone_number as customer_phone 
            FROM Account a 
            JOIN Customer c ON a.customer_id = c.customer_id 
            WHERE a.account_id = ?
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Account account = new Account();
                account.setAccountId(rs.getString("account_id"));
                account.setCustomerId(rs.getString("customer_id"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setAadharNumber(rs.getString("aadhar_number"));
                account.setIfscCode(rs.getString("ifsc_code"));
                account.setPhoneNumberLinked(rs.getString("phone_number_linked"));
                account.setAmount(rs.getBigDecimal("amount"));
                account.setBankName(rs.getString("bank_name"));
                account.setNameOnAccount(rs.getString("name_on_account"));
                account.setStatus(rs.getString("status"));
                account.setCreated(rs.getTimestamp("created").toLocalDateTime());
                account.setModified(rs.getTimestamp("modified").toLocalDateTime());
                
                System.out.println("=== ACCOUNT RETRIEVED ===");
                System.out.println("Account ID: " + accountId);
                
                return account;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving account: " + accountId);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Account getAccountByCustomerId(String customerId) {
        String query = """
            SELECT a.*, c.phone_number as customer_phone 
            FROM Account a 
            JOIN Customer c ON a.customer_id = c.customer_id 
            WHERE a.customer_id = ?
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Account account = new Account();
                account.setAccountId(rs.getString("account_id"));
                account.setCustomerId(rs.getString("customer_id"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setAadharNumber(rs.getString("aadhar_number"));
                account.setIfscCode(rs.getString("ifsc_code"));
                account.setPhoneNumberLinked(rs.getString("phone_number_linked"));
                account.setAmount(rs.getBigDecimal("amount"));
                account.setBankName(rs.getString("bank_name"));
                account.setNameOnAccount(rs.getString("name_on_account"));
                account.setStatus(rs.getString("status"));
                account.setCreated(rs.getTimestamp("created").toLocalDateTime());
                account.setModified(rs.getTimestamp("modified").toLocalDateTime());
                
                return account;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving account by customer ID: " + customerId);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Account getAccountByAccountNumber(String accountNumber) {
        String query = """
            SELECT a.*, c.phone_number as customer_phone 
            FROM Account a 
            JOIN Customer c ON a.customer_id = c.customer_id 
            WHERE a.account_number = ?
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Account account = new Account();
                account.setAccountId(rs.getString("account_id"));
                account.setCustomerId(rs.getString("customer_id"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setAadharNumber(rs.getString("aadhar_number"));
                account.setIfscCode(rs.getString("ifsc_code"));
                account.setPhoneNumberLinked(rs.getString("phone_number_linked"));
                account.setAmount(rs.getBigDecimal("amount"));
                account.setBankName(rs.getString("bank_name"));
                account.setNameOnAccount(rs.getString("name_on_account"));
                account.setStatus(rs.getString("status"));
                account.setCreated(rs.getTimestamp("created").toLocalDateTime());
                account.setModified(rs.getTimestamp("modified").toLocalDateTime());
                
                return account;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving account by account number: " + accountNumber);
            e.printStackTrace();
        }
        return null;
    }

    @Override
public boolean updateAccount(String accountId, Account account) {
    System.out.println("=== ACCOUNT UPDATE DEBUG STARTED ===");
    System.out.println("Account ID: " + accountId);
    System.out.println("Aadhar Number: " + account.getAadharNumber());
    
    // Step 1: Get customer ID based on aadhar number
    String customerId = findCustomerIdByAadhar(account.getAadharNumber());
    if (customerId == null) {
        System.err.println("Error: Customer not found for Aadhar: " + account.getAadharNumber());
        return false;
    }
    
    // Step 2: Auto-set phone number linked to customer's phone
    String customerPhone = getCustomerPhoneByCustomerId(customerId);
    if (customerPhone == null) {
        System.err.println("Error: Could not retrieve customer phone number");
        return false;
    }
    
    account.setPhoneNumberLinked(customerPhone);
    System.out.println("Auto-linked Customer ID: " + customerId);
    System.out.println("Auto-linked Phone Number: " + customerPhone);
    
    // Step 3: Update account (account_number and customer_id remain fixed)
    String query = """
        UPDATE Account SET aadhar_number = ?, ifsc_code = ?, phone_number_linked = ?, 
                         amount = ?, bank_name = ?, name_on_account = ?, status = ? 
        WHERE account_id = ?
    """;
    
    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        System.out.println("=== UPDATING ACCOUNT RECORD ===");
        
        stmt.setString(1, account.getAadharNumber());
        stmt.setString(2, account.getIfscCode());
        stmt.setString(3, account.getPhoneNumberLinked()); // Now properly set
        stmt.setBigDecimal(4, account.getAmount());
        stmt.setString(5, account.getBankName());
        stmt.setString(6, account.getNameOnAccount());
        stmt.setString(7, account.getStatus());
        stmt.setString(8, accountId);
        
        int result = stmt.executeUpdate();
        
        if (result > 0) {
            System.out.println("\n"); // Empty line for readability
            System.out.println("=== ACCOUNT UPDATED SUCCESSFULLY ===");
            System.out.println("Account ID: " + accountId);
            System.out.println("Customer ID: " + customerId);
            System.out.println("Phone Linked: " + account.getPhoneNumberLinked());
            System.out.println("Amount: " + account.getAmount());
            System.out.println("Bank Name: " + account.getBankName());
            System.out.println("Name on Account: " + account.getNameOnAccount());
            System.out.println("Status: " + account.getStatus());
            System.out.println("Updated fields: Aadhar, IFSC, Phone, Amount, Bank Name, Name on Account, Status");
            System.out.println("=== END ACCOUNT UPDATE ===");
            System.out.println("\n"); // Empty line after success
        } else {
            System.err.println("Error: Account update failed - no rows affected");
        }
        
        return result > 0;
    } catch (SQLException e) {
        System.err.println("Error: Account update failed");
        System.err.println("SQL Error: " + e.getMessage());
        System.err.println("Error Code: " + e.getErrorCode());
        e.printStackTrace();
        return false;
    }
}

/**
 * Find customer ID by aadhar number (helper method for updates)
 */
private String findCustomerIdByAadhar(String aadharNumber) {
    String query = "SELECT customer_id FROM Customer WHERE aadhar_number = ?";
    
    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, aadharNumber);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            String customerId = rs.getString("customer_id");
            System.out.println("✓ Found customer for Aadhar " + aadharNumber + ": " + customerId);
            return customerId;
        } else {
            System.out.println("✗ No customer found for Aadhar: " + aadharNumber);
            return null;
        }
        
    } catch (SQLException e) {
        System.err.println("Error finding customer by Aadhar: " + e.getMessage());
        return null;
    }
}

    @Override
public boolean deleteAccount(String accountId) {
    // Hard delete - permanently remove from database
    System.out.println("===  DELETING ACCOUNT ===");
    System.out.println("Account ID: " + accountId);
    
    // First check if account exists
    if (!accountExists(accountId)) {
        System.err.println("Error: Account not found: " + accountId);
        return false;
    }
    
    // Delete from Transaction table first (due to foreign key constraint)
    String deleteTransactionsQuery = "DELETE FROM Transaction WHERE account_id = ?";
    
    // Delete from Account table
    String deleteAccountQuery = "DELETE FROM Account WHERE account_id = ?";
    
    try (Connection conn = DBConfig.getConnection()) {
        
        // Start transaction
        conn.setAutoCommit(false);
        
        try {
            // Step 1: Delete all transactions for this account
            try (PreparedStatement transactionStmt = conn.prepareStatement(deleteTransactionsQuery)) {
                transactionStmt.setString(1, accountId);
                int transactionsDeleted = transactionStmt.executeUpdate();
                System.out.println("Deleted " + transactionsDeleted + " transactions for account: " + accountId);
            }
            
            // Step 2: Delete the account
            try (PreparedStatement accountStmt = conn.prepareStatement(deleteAccountQuery)) {
                accountStmt.setString(1, accountId);
                int result = accountStmt.executeUpdate();
                
                if (result > 0) {
                    // Commit transaction
                    conn.commit();
                    
                    System.out.println("\n"); // Empty line for readability
                    System.out.println("=== ACCOUNT  DELETED SUCCESSFULLY ===");
                    System.out.println("Account ID: " + accountId);
                    System.out.println("Account permanently removed from database");
                    System.out.println("All related transactions also deleted");
                    System.out.println("=== END ACCOUNT DELETION ===");
                    System.out.println("\n"); // Empty line after success
                    
                    return true;
                } else {
                    // Rollback if account deletion failed
                    conn.rollback();
                    System.err.println("Error: Account deletion failed - no rows affected");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            // Rollback on any error
            conn.rollback();
            System.err.println("Error during deletion transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
    } catch (SQLException e) {
        System.err.println("Error: Account deletion failed");
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

/**
 * Helper method to check if account exists
 */
private boolean accountExists(String accountId) {
    String query = "SELECT COUNT(*) FROM Account WHERE account_id = ?";
    
    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, accountId);
        ResultSet rs = stmt.executeQuery();
        
        return rs.next() && rs.getInt(1) > 0;
        
    } catch (SQLException e) {
        System.err.println("Error checking account existence: " + e.getMessage());
        return false;
    }
}

    @Override
    public boolean isAccountNumberExists(String accountNumber) {
        String query = "SELECT COUNT(*) FROM Account WHERE account_number = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                if (exists) {
                    System.out.println("✗ Account number already exists: " + accountNumber);
                } else {
                    System.out.println("✓ Account number is unique: " + accountNumber);
                }
                return exists;
            }
        } catch (SQLException e) {
            System.err.println("Error checking account number uniqueness: " + accountNumber);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isCustomerExists(String customerId) {
        String query = "SELECT COUNT(*) FROM Customer WHERE customer_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                return exists;
            }
        } catch (SQLException e) {
            System.err.println("Error checking customer existence: " + customerId);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getCustomerPhoneNumber(String customerId) {
        return getCustomerPhoneByCustomerId(customerId);
    }

    @Override
    public boolean isPhoneNumberLinkedToCustomer(String customerId, String phoneNumber) {
        String customerPhone = getCustomerPhoneNumber(customerId);
        return customerPhone != null && customerPhone.equals(phoneNumber);
    }

    @Override
    public void debugPrintAccountDetails(String accountId) {
        System.out.println("=== ACCOUNT DETAILS DEBUG ===");
        
        String query = """
            SELECT a.*, c.name as customer_name, c.phone_number as customer_phone
            FROM Account a 
            JOIN Customer c ON a.customer_id = c.customer_id 
            WHERE a.account_id = ?
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("Account ID: " + rs.getString("account_id"));
                System.out.println("Customer ID: " + rs.getString("customer_id"));
                System.out.println("Customer Name: " + rs.getString("customer_name"));
                System.out.println("Account Number: " + rs.getString("account_number"));
                System.out.println("Aadhar Number: " + rs.getString("aadhar_number"));
                System.out.println("IFSC Code: " + rs.getString("ifsc_code"));
                System.out.println("Customer Phone: " + rs.getString("customer_phone"));
                System.out.println("Linked Phone: " + rs.getString("phone_number_linked"));
                System.out.println("Amount: " + rs.getBigDecimal("amount"));
                System.out.println("Bank Name: " + rs.getString("bank_name"));
                System.out.println("Name on Account: " + rs.getString("name_on_account"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Created: " + rs.getTimestamp("created"));
                System.out.println("Modified: " + rs.getTimestamp("modified"));
            } else {
                System.out.println("Account not found: " + accountId);
            }
        } catch (SQLException e) {
            System.err.println("Error in debug print: " + e.getMessage());
        }
        
        System.out.println("=== END ACCOUNT DETAILS ===");
    }

    @Override
    public String generateAccountId() {
        return "ACC_" + accountCounter.getAndIncrement();
    }
}
