package com.bank.simulator.validation;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionValidator {

    /**
     * Main validation method for transaction creation
     */
    public ValidationResult validateTransactionForCreation(Transaction transaction) {
        System.out.println("=== TRANSACTION CREATION VALIDATION STARTED ===");
        System.out.println("Account ID: " + transaction.getAccountId());
        System.out.println("Amount: " + transaction.getTransactionAmount());
        System.out.println("Type: " + transaction.getTransactionType());
        System.out.println("Mode: " + transaction.getTransactionMode());
        
        ValidationResult result = new ValidationResult();
        
        // Basic field validations
        ValidationResult accountIdValidation = validateAccountId(transaction.getAccountId());
        if (!accountIdValidation.isValid()) {
            result.addError(accountIdValidation.getFirstErrorMessage());
        }
        
        ValidationResult amountValidation = validateTransactionAmount(transaction.getTransactionAmount());
        if (!amountValidation.isValid()) {
            result.addError(amountValidation.getFirstErrorMessage());
        }
        
        ValidationResult typeValidation = validateTransactionType(transaction.getTransactionType());
        if (!typeValidation.isValid()) {
            result.addError(typeValidation.getFirstErrorMessage());
        }
        
        ValidationResult modeValidation = validateTransactionMode(transaction.getTransactionMode());
        if (!modeValidation.isValid()) {
            result.addError(modeValidation.getFirstErrorMessage());
        }
        
        // Database-dependent validations
        if (result.isValid()) {
            System.out.println("=== BASIC VALIDATIONS PASSED - CHECKING DATABASE CONSTRAINTS ===");
            
            ValidationResult accountExistsValidation = validateAccountExists(transaction.getAccountId());
            if (!accountExistsValidation.isValid()) {
                result.addError(accountExistsValidation.getFirstErrorMessage(), "ACCOUNT_NOT_EXISTS");
            } else {
                // Only check balance for debit transactions and if account exists
                if ("debited".equals(transaction.getTransactionType())) {
                    ValidationResult balanceValidation = validateSufficientBalance(
                        transaction.getAccountId(), transaction.getTransactionAmount());
                    if (!balanceValidation.isValid()) {
                        result.addError("Transaction failed: Insufficient balance", "INSUFFICIENT_BALANCE");
                    }
                }
            }
        }
        
        System.out.println("=== TRANSACTION VALIDATION RESULT ===");
        System.out.println("Valid: " + result.isValid());
        if (!result.isValid()) {
            System.out.println("Errors: " + result.getAllErrorMessages());
            System.out.println("Error Code: " + result.getErrorCode());
        }
        
        return result;
    }

    /**
     * Validate account ID format and requirement
     */
    public ValidationResult validateAccountId(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return ValidationResult.failure("Account ID is required");
        }
        
        // Optional: Validate format if you want to enforce ACC_<number> format
        if (!accountId.matches("^ACC_[0-9]+$")) {
            return ValidationResult.failure("Account ID format is invalid");
        }
        
        return ValidationResult.success();
    }

    /**
     * Validate transaction amount
     */
    public ValidationResult validateTransactionAmount(BigDecimal amount) {
        if (amount == null) {
            return ValidationResult.failure("Transaction amount is required");
        }
        
        if (amount.signum() <= 0) {
            return ValidationResult.failure("Transaction amount must be greater than zero");
        }
        
        // Additional amount validations
        if (amount.compareTo(new BigDecimal("1000000")) > 0) {
            return ValidationResult.failure("Transaction amount cannot exceed ₹10,00,000");
        }
        
        if (amount.scale() > 2) {
            return ValidationResult.failure("Transaction amount cannot have more than 2 decimal places");
        }
        
        return ValidationResult.success();
    }

    /**
     * Validate transaction type
     */
    public ValidationResult validateTransactionType(String transactionType) {
        if (transactionType == null || transactionType.trim().isEmpty()) {
            return ValidationResult.failure("Transaction type is required");
        }
        
        if (!"debited".equals(transactionType) && !"credited".equals(transactionType)) {
            return ValidationResult.failure("Transaction type must be 'debited' or 'credited'");
        }
        
        return ValidationResult.success();
    }

    /**
     * Validate transaction mode
     */
    public ValidationResult validateTransactionMode(String transactionMode) {
        if (transactionMode == null || transactionMode.trim().isEmpty()) {
            return ValidationResult.failure("Transaction mode is required");
        }
        
        String[] validModes = {
            "UPI", "debit", "credit card", "net banking", "ATM", "cash", 
            "cheque", "NEFT", "RTGS", "IMPS", "bank transfer"
        };
        
        for (String validMode : validModes) {
            if (validMode.equalsIgnoreCase(transactionMode)) {
                return ValidationResult.success();
            }
        }
        
        return ValidationResult.failure("Invalid transaction mode");
    }

    /**
     * Check if account exists in database
     */
    public ValidationResult validateAccountExists(String accountId) {
        String query = "SELECT COUNT(*) FROM Account WHERE account_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("✓ Account exists: " + accountId);
                return ValidationResult.success();
            } else {
                System.out.println("✗ Account not found: " + accountId);
                return ValidationResult.failure("Account does not exist");
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking account existence: " + e.getMessage());
            return ValidationResult.failure("Database error while validating account");
        }
    }

    /**
     * Validate sufficient balance for debit transactions
     */
    public ValidationResult validateSufficientBalance(String accountId, BigDecimal transactionAmount) {
        String query = "SELECT balance FROM Account WHERE account_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal currentBalance = rs.getBigDecimal("balance");
                
                System.out.println("=== BALANCE VALIDATION ===");
                System.out.println("Account ID: " + accountId);
                System.out.println("Current Balance: " + currentBalance);
                System.out.println("Transaction Amount: " + transactionAmount);
                
                if (currentBalance.compareTo(transactionAmount) >= 0) {
                    System.out.println("✓ Sufficient balance available");
                    return ValidationResult.success();
                } else {
                    BigDecimal shortage = transactionAmount.subtract(currentBalance);
                    System.out.println("✗ Insufficient balance");
                    System.out.println("Shortage: " + shortage);
                    
                    return ValidationResult.failure("Insufficient balance");
                }
            } else {
                return ValidationResult.failure("Account not found during balance validation");
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking account balance: " + e.getMessage());
            return ValidationResult.failure("Database error while validating balance");
        }
    }
}
