package com.bank.simulator.validation;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountValidator {

    /**
     * Main validation method for account creation
     */
    public ValidationResult validateAccountForCreation(Account account) {
        System.out.println("=== ACCOUNT CREATION VALIDATION STARTED ===");
        System.out.println("Customer ID: " + account.getCustomerId());
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Phone Linked: " + account.getPhoneNumberLinked());
        
        ValidationResult result = new ValidationResult();
        
        // Basic field validations
        ValidationResult customerIdValidation = validateCustomerId(account.getCustomerId());
        if (!customerIdValidation.isValid()) {
            result.addError(customerIdValidation.getFirstErrorMessage());
        }
        
        ValidationResult phoneLinkedValidation = validatePhoneNumberLinked(account.getPhoneNumberLinked());
        if (!phoneLinkedValidation.isValid()) {
            result.addError(phoneLinkedValidation.getFirstErrorMessage());
        }
        
        ValidationResult accountNumberValidation = validateAccountNumber(account.getAccountNumber());
        if (!accountNumberValidation.isValid()) {
            result.addError(accountNumberValidation.getFirstErrorMessage());
        }
        
        ValidationResult accountTypeValidation = validateAccountType(account.getAccountType());
        if (!accountTypeValidation.isValid()) {
            result.addError(accountTypeValidation.getFirstErrorMessage());
        }
        
        ValidationResult accountNameValidation = validateAccountName(account.getAccountName());
        if (!accountNameValidation.isValid()) {
            result.addError(accountNameValidation.getFirstErrorMessage());
        }
        
        ValidationResult statusValidation = validateStatus(account.getStatus());
        if (!statusValidation.isValid()) {
            result.addError(statusValidation.getFirstErrorMessage());
        }
        
        ValidationResult balanceValidation = validateBalance(account.getBalance());
        if (!balanceValidation.isValid()) {
            result.addError(balanceValidation.getFirstErrorMessage());
        }
        
        // Database-dependent validations
        if (result.isValid()) {
            System.out.println("=== BASIC VALIDATIONS PASSED - CHECKING DATABASE CONSTRAINTS ===");
            
            ValidationResult customerExistsValidation = validateCustomerExists(account.getCustomerId());
            if (!customerExistsValidation.isValid()) {
                result.addError(customerExistsValidation.getFirstErrorMessage(), "CUSTOMER_NOT_EXISTS");
                return result; // Return early if customer doesn't exist
            }
            
            ValidationResult accountNumberUniqueValidation = validateAccountNumberUniqueness(account.getAccountNumber());
            if (!accountNumberUniqueValidation.isValid()) {
                result.addError(accountNumberUniqueValidation.getFirstErrorMessage(), "ACCOUNT_NUMBER_EXISTS");
            }
            
            ValidationResult phoneLinkValidation = validatePhoneNumberLinkedToCustomer(
                account.getCustomerId(), account.getPhoneNumberLinked());
            if (!phoneLinkValidation.isValid()) {
                result.addError(phoneLinkValidation.getFirstErrorMessage(), "PHONE_NOT_LINKED");
            }
        }
        
        System.out.println("=== ACCOUNT VALIDATION RESULT ===");
        System.out.println("Valid: " + result.isValid());
        if (!result.isValid()) {
            System.out.println("Errors: " + result.getAllErrorMessages());
            System.out.println("Error Code: " + result.getErrorCode());
        }
        
        return result;
    }

    /**
     * Validation for account updates
     */
    public ValidationResult validateAccountForUpdate(String accountId, Account account) {
        System.out.println("=== ACCOUNT UPDATE VALIDATION STARTED ===");
        System.out.println("Account ID: " + accountId);
        
        ValidationResult result = new ValidationResult();
        
        // Check if account exists
        if (!accountExists(accountId)) {
            result.addError("Account not found with ID: " + accountId);
            return result;
        }
        
        // Run creation validations (might need to modify for updates)
        ValidationResult basicValidation = validateAccountForCreation(account);
        if (!basicValidation.isValid()) {
            result.addError(basicValidation.getAllErrorMessages());
        }
        
        return result;
    }

    /**
     * Validate customer ID format and requirement
     */
    public ValidationResult validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return ValidationResult.failure("Customer ID is required");
        }
        
        // Optional: Validate format if you want to enforce CUST_<number> format
        if (!customerId.matches("^CUST_[0-9]+$")) {
            return ValidationResult.failure("Customer ID format is invalid");
        }
        
        return ValidationResult.success();
    }

    /**
     * Validate phone number linked (reusing customer validation logic)
     */
    public ValidationResult validatePhoneNumberLinked(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return ValidationResult.failure("Phone number linked is required");
        }
        
        // Reuse customer phone validation
        CustomerValidator customerValidator = new CustomerValidator();
        return customerValidator.validatePhoneNumberFormat(phoneNumber);
    }

    /**
     * Validate account number
     */
    public ValidationResult validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return ValidationResult.failure("Account number is required");
        }
        
        if (accountNumber.length() < 10 || accountNumber.length() > 20) {
            return ValidationResult.failure("Account number must be between 10-20 characters");
        }
        
        return ValidationResult.success();
    }

    /**
     * Validate account type
     */
    public ValidationResult validateAccountType(String accountType) {
        if (accountType == null || accountType.trim().isEmpty()) {
            return ValidationResult.failure("Account type is required");
        }
        
        String[] validTypes = {"Savings", "Current", "Fixed Deposit", "Recurring Deposit"};
        for (String validType : validTypes) {
            if (validType.equalsIgnoreCase(accountType)) {
                return ValidationResult.success();
            }
        }
        
        return ValidationResult.failure("Invalid account type");
    }

    /**
     * Validate account name
     */
    public ValidationResult validateAccountName(String accountName) {
        if (accountName == null || accountName.trim().isEmpty()) {
            return ValidationResult.failure("Account name is required");
        }
        
        if (accountName.trim().length() < 3) {
            return ValidationResult.failure("Account name must be at least 3 characters");
        }
        
        if (accountName.length() > 100) {
            return ValidationResult.failure("Account name cannot exceed 100 characters");
        }
        
        return ValidationResult.success();
    }

    /**
     * Validate account status
     */
    public ValidationResult validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return ValidationResult.failure("Account status is required");
        }
        
        String[] validStatuses = {"Active", "Inactive", "Suspended", "Closed"};
        for (String validStatus : validStatuses) {
            if (validStatus.equalsIgnoreCase(status)) {
                return ValidationResult.success();
            }
        }
        
        return ValidationResult.failure("Invalid account status");
    }

    /**
     * Validate account balance
     */
    public ValidationResult validateBalance(BigDecimal balance) {
        if (balance == null) {
            // Balance can be null, will default to 50
            return ValidationResult.success();
        }
        
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            return ValidationResult.failure("Account balance cannot be negative");
        }
        
        return ValidationResult.success();
    }

    /**
     * Check if customer exists in database
     */
    public ValidationResult validateCustomerExists(String customerId) {
        String query = "SELECT COUNT(*) FROM Customer WHERE customer_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("✓ Customer exists: " + customerId);
                return ValidationResult.success();
            } else {
                System.out.println("✗ Customer not found: " + customerId);
                return ValidationResult.failure("Customer does not exist. Please provide a valid customer ID.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking customer existence: " + e.getMessage());
            return ValidationResult.failure("Database error while validating customer");
        }
    }

    /**
     * Check account number uniqueness
     */
    public ValidationResult validateAccountNumberUniqueness(String accountNumber) {
        String query = "SELECT COUNT(*) FROM Account WHERE account_number = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("=== DUPLICATE ACCOUNT NUMBER ===");
                System.out.println("Account Number: " + accountNumber);
                return ValidationResult.failure("Account number already exists. Please use a unique account number.");
            }
            
            System.out.println("✓ Account number is unique: " + accountNumber);
            return ValidationResult.success();
            
        } catch (SQLException e) {
            System.err.println("Error checking account number uniqueness: " + e.getMessage());
            return ValidationResult.failure("Database error while checking account number");
        }
    }

    /**
     * Validate phone number is linked to customer
     */
    public ValidationResult validatePhoneNumberLinkedToCustomer(String customerId, String phoneNumber) {
        String query = "SELECT phone_number FROM Customer WHERE customer_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String customerPhone = rs.getString("phone_number");
                
                System.out.println("=== PHONE LINKING VALIDATION ===");
                System.out.println("Customer ID: " + customerId);
                System.out.println("Customer Phone: " + customerPhone);
                System.out.println("Account Phone Linked: " + phoneNumber);
                
                if (customerPhone.equals(phoneNumber)) {
                    System.out.println("✓ Phone number is correctly linked");
                    return ValidationResult.success();
                } else {
                    System.out.println("✗ Phone number mismatch");
                    return ValidationResult.failure("Phone number is not linked to this customer. Please use the customer's registered phone number.");
                }
            } else {
                return ValidationResult.failure("Customer not found during phone linking validation");
            }
            
        } catch (SQLException e) {
            System.err.println("Error validating phone linking: " + e.getMessage());
            return ValidationResult.failure("Database error while validating phone linking");
        }
    }

    /**
     * Check if account exists
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
}
