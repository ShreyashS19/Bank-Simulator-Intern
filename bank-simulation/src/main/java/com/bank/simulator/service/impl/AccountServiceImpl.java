package com.bank.simulator.service.impl;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Account;
import com.bank.simulator.service.AccountService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountServiceImpl implements AccountService {
    private static final AtomicInteger accountCounter = new AtomicInteger(1);

    @Override
    public String createAccount(Account account) {
        String accountId = generateAccountId();
        account.setAccountId(accountId);
        
        System.out.println("=== ACCOUNT CREATION DEBUG STARTED ===");
        System.out.println("Generated Account ID: " + accountId);
        System.out.println("Customer ID: " + account.getCustomerId());
        System.out.println("Phone Number Linked: " + account.getPhoneNumberLinked());
        System.out.println("Account Number: " + account.getAccountNumber());
        
        // First, validate that customer exists and get their phone number
        String customerPhoneQuery = "SELECT phone_number FROM Customer WHERE customer_id = ?";
        String customerPhoneNumber = null;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(customerPhoneQuery)) {
            
            stmt.setString(1, account.getCustomerId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                customerPhoneNumber = rs.getString("phone_number");
                System.out.println("=== CUSTOMER PHONE VALIDATION ===");
                System.out.println("Customer exists with phone: " + customerPhoneNumber);
                System.out.println("Account phone linked: " + account.getPhoneNumberLinked());
                
                // Validate that phoneNumberLinked matches customer's phone number
                if (!customerPhoneNumber.equals(account.getPhoneNumberLinked())) {
                    System.err.println("=== PHONE NUMBER MISMATCH ERROR ===");
                    System.err.println("Customer phone: " + customerPhoneNumber);
                    System.err.println("Linked phone: " + account.getPhoneNumberLinked());
                    System.err.println("ERROR: Phone number is not linked to this customer");
                    return "PHONE_NOT_LINKED"; // Special error code
                }
                
                System.out.println("✓ Phone number validation passed");
            } else {
                System.err.println("=== CUSTOMER NOT FOUND ERROR ===");
                System.err.println("Customer ID does not exist: " + account.getCustomerId());
                return "CUSTOMER_NOT_EXISTS"; // Special error code
            }
        } catch (SQLException e) {
            System.err.println("=== DATABASE ERROR IN CUSTOMER VALIDATION ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        // Check if account number already exists
        if (isAccountNumberExists(account.getAccountNumber())) {
            System.err.println("=== DUPLICATE ACCOUNT NUMBER ERROR ===");
            System.err.println("Account number already exists: " + account.getAccountNumber());
            return "ACCOUNT_NUMBER_EXISTS"; // Special error code
        }
        
        String query = """
            INSERT INTO Account (account_id, customer_id, balance, account_type, 
                               account_name, account_number, phone_number_linked, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            System.out.println("=== INSERTING ACCOUNT RECORD ===");
            System.out.println("SQL Query: " + query);
            
            stmt.setString(1, account.getAccountId());
            stmt.setString(2, account.getCustomerId());
            stmt.setBigDecimal(3, account.getBalance());
            stmt.setString(4, account.getAccountType());
            stmt.setString(5, account.getAccountName());
            stmt.setString(6, account.getAccountNumber());
            stmt.setString(7, account.getPhoneNumberLinked());
            stmt.setString(8, account.getStatus());
            
            System.out.println("Parameters set:");
            System.out.println("1. Account ID: " + account.getAccountId());
            System.out.println("2. Customer ID: " + account.getCustomerId());
            System.out.println("3. Balance: " + account.getBalance());
            System.out.println("4. Account Type: " + account.getAccountType());
            System.out.println("5. Account Name: " + account.getAccountName());
            System.out.println("6. Account Number: " + account.getAccountNumber());
            System.out.println("7. Phone Linked: " + account.getPhoneNumberLinked());
            System.out.println("8. Status: " + account.getStatus());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("=== ACCOUNT CREATED SUCCESSFULLY ===");
                System.out.println("Account ID: " + accountId);
                System.out.println("Customer Phone: " + customerPhoneNumber);
                System.out.println("Linked Phone: " + account.getPhoneNumberLinked());
                System.out.println("Rows inserted: " + result);
                return accountId;
            } else {
                System.err.println("=== ACCOUNT INSERTION FAILED ===");
                System.err.println("No rows were inserted");
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("=== SQL ERROR IN ACCOUNT CREATION ===");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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
                account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                account.setModifiedAt(rs.getTimestamp("modified_at").toLocalDateTime());
                account.setBalance(rs.getBigDecimal("balance"));
                account.setAccountType(rs.getString("account_type"));
                account.setAccountName(rs.getString("account_name"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setPhoneNumberLinked(rs.getString("phone_number_linked"));
                account.setStatus(rs.getString("status"));
                
                System.out.println("=== ACCOUNT RETRIEVED ===");
                System.out.println("Account ID: " + accountId);
                System.out.println("Customer Phone: " + rs.getString("customer_phone"));
                System.out.println("Linked Phone: " + account.getPhoneNumberLinked());
                
                return account;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving account: " + accountId);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateAccount(String accountId, Account account) {
        // First validate phone number linking for updates too
        String customerPhoneQuery = "SELECT phone_number FROM Customer WHERE customer_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(customerPhoneQuery)) {
            
            stmt.setString(1, account.getCustomerId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String customerPhoneNumber = rs.getString("phone_number");
                
                System.out.println("=== ACCOUNT UPDATE PHONE VALIDATION ===");
                System.out.println("Account ID: " + accountId);
                System.out.println("Customer phone: " + customerPhoneNumber);
                System.out.println("Linked phone: " + account.getPhoneNumberLinked());
                
                // Validate phone number linking
                if (!customerPhoneNumber.equals(account.getPhoneNumberLinked())) {
                    System.err.println("=== UPDATE FAILED: PHONE NOT LINKED ===");
                    System.err.println("Customer phone: " + customerPhoneNumber);
                    System.err.println("Linked phone: " + account.getPhoneNumberLinked());
                    return false;
                }
            } else {
                System.err.println("Customer not found for update: " + account.getCustomerId());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error validating customer for update");
            e.printStackTrace();
            return false;
        }
        
        String query = """
            UPDATE Account SET customer_id = ?, balance = ?, account_type = ?, 
                             account_name = ?, account_number = ?, phone_number_linked = ?, 
                             status = ? WHERE account_id = ?
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, account.getCustomerId());
            stmt.setBigDecimal(2, account.getBalance());
            stmt.setString(3, account.getAccountType());
            stmt.setString(4, account.getAccountName());
            stmt.setString(5, account.getAccountNumber());
            stmt.setString(6, account.getPhoneNumberLinked());
            stmt.setString(7, account.getStatus());
            stmt.setString(8, accountId);
            
            int result = stmt.executeUpdate();
            
            System.out.println("=== ACCOUNT UPDATE RESULT ===");
            System.out.println("Account ID: " + accountId);
            System.out.println("Rows updated: " + result);
            
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating account: " + accountId);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteAccount(String accountId) {
        String query = "DELETE FROM Account WHERE account_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            System.out.println("=== DELETING ACCOUNT ===");
            System.out.println("Account ID: " + accountId);
            
            stmt.setString(1, accountId);
            int result = stmt.executeUpdate();
            
            System.out.println("Delete result - Rows affected: " + result);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + accountId);
            e.printStackTrace();
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
                System.out.println("Account number " + accountNumber + " exists: " + exists);
                return exists;
            }
        } catch (SQLException e) {
            System.err.println("Error checking account number: " + accountNumber);
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
                System.out.println("Customer " + customerId + " exists: " + exists);
                return exists;
            }
        } catch (SQLException e) {
            System.err.println("Error checking customer existence: " + customerId);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * New method: Get customer's phone number
     */
    public String getCustomerPhoneNumber(String customerId) {
        String query = "SELECT phone_number FROM Customer WHERE customer_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String phone = rs.getString("phone_number");
                System.out.println("Customer " + customerId + " phone: " + phone);
                return phone;
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer phone: " + customerId);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * New method: Validate phone number linking
     */
    public boolean isPhoneNumberLinkedToCustomer(String customerId, String phoneNumber) {
        String customerPhone = getCustomerPhoneNumber(customerId);
        boolean isLinked = customerPhone != null && customerPhone.equals(phoneNumber);
        
        System.out.println("=== PHONE LINK VALIDATION ===");
        System.out.println("Customer ID: " + customerId);
        System.out.println("Customer Phone: " + customerPhone);
        System.out.println("Provided Phone: " + phoneNumber);
        System.out.println("Is Linked: " + isLinked);
        
        return isLinked;
    }

    @Override
    public String generateAccountId() {
        return "ACC_" + accountCounter.getAndIncrement();
    }

    /**
     * Debug method: Print account details with customer info
     */
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
                System.out.println("Customer Phone: " + rs.getString("customer_phone"));
                System.out.println("Linked Phone: " + rs.getString("phone_number_linked"));
                System.out.println("Account Number: " + rs.getString("account_number"));
                System.out.println("Account Type: " + rs.getString("account_type"));
                System.out.println("Balance: " + rs.getBigDecimal("balance"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Created: " + rs.getTimestamp("created_at"));
                System.out.println("Modified: " + rs.getTimestamp("modified_at"));
                
                // Check if phones match
                String customerPhone = rs.getString("customer_phone");
                String linkedPhone = rs.getString("phone_number_linked");
                boolean matches = customerPhone.equals(linkedPhone);
                System.out.println("Phone Numbers Match: " + matches);
            } else {
                System.out.println("Account not found: " + accountId);
            }
        } catch (SQLException e) {
            System.err.println("Error in debug print: " + e.getMessage());
        }
        
        System.out.println("=== END ACCOUNT DETAILS ===");
    }
}
