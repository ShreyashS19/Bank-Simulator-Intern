package com.bank.simulator.validation;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class AccountValidator {

    private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[1-9][0-9]{9}$");
    private static final Pattern AADHAR_PATTERN = Pattern.compile("^[0-9]{12}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[0-9]{10,25}$");

    
    public ValidationResult validateAccountForCreation(Account account) {
        System.out.println("=== ACCOUNT CREATION VALIDATION STARTED ===");
        System.out.println("Aadhar Number: " + account.getAadharNumber());
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("IFSC Code: " + account.getIfscCode());
        
        ValidationResult result = new ValidationResult();
        
        if (account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
            System.err.println("Error: Required field missing (account number)");
            result.addError("Account number is required");
        }
        
        if (account.getAadharNumber() == null || account.getAadharNumber().trim().isEmpty()) {
            System.err.println("Error: Required field missing (aadhar number)");
            result.addError("Aadhar number is required");
        }
        
        if (account.getIfscCode() == null || account.getIfscCode().trim().isEmpty()) {
            System.err.println("Error: Required field missing (IFSC code)");
            result.addError("IFSC code is required");
        }
        
        if (account.getBankName() == null || account.getBankName().trim().isEmpty()) {
            System.err.println("Error: Required field missing (bank name)");
            result.addError("Bank name is required");
        }
        
        if (account.getNameOnAccount() == null || account.getNameOnAccount().trim().isEmpty()) {
            System.err.println("Error: Required field missing (name on account)");
            result.addError("Name on account is required");
        }
        
        ValidationResult accountNumberValidation = validateAccountNumber(account.getAccountNumber());
        if (!accountNumberValidation.isValid()) {
            System.err.println("Error: Invalid account number format");
            result.addError(accountNumberValidation.getFirstErrorMessage());
        }
        
        ValidationResult aadharValidation = validateAadharNumber(account.getAadharNumber());
        if (!aadharValidation.isValid()) {
            System.err.println("Error: Invalid aadhar number format");
            result.addError(aadharValidation.getFirstErrorMessage());
        }
        
        ValidationResult ifscValidation = validateIfscCode(account.getIfscCode());
        if (!ifscValidation.isValid()) {
            System.err.println("Error: Invalid IFSC code format");
            result.addError(ifscValidation.getFirstErrorMessage());
        }
        
        ValidationResult amountValidation = validateAmount(account.getAmount());
        if (!amountValidation.isValid()) {
            System.err.println("Error: Invalid amount");
            result.addError(amountValidation.getFirstErrorMessage());
        }
        
        ValidationResult bankNameValidation = validateBankName(account.getBankName());
        if (!bankNameValidation.isValid()) {
            System.err.println("Error: Invalid bank name");
            result.addError(bankNameValidation.getFirstErrorMessage());
        }
        
        ValidationResult nameOnAccountValidation = validateNameOnAccount(account.getNameOnAccount());
        if (!nameOnAccountValidation.isValid()) {
            System.err.println("Error: Invalid name on account");
            result.addError(nameOnAccountValidation.getFirstErrorMessage());
        }
        
        if (result.isValid()) {
            System.out.println("=== BASIC VALIDATIONS PASSED - CHECKING DATABASE CONSTRAINTS ===");
            
            ValidationResult aadharExistsValidation = validateAadharExistsInCustomer(account.getAadharNumber());
            if (!aadharExistsValidation.isValid()) {
                System.err.println("Error: Aadhar number not found");
                result.addError("Aadhar number is not linked with any customer", "AADHAR_NOT_FOUND");
            }
            
            ValidationResult accountNumberUniqueValidation = validateAccountNumberUniqueness(account.getAccountNumber());
            if (!accountNumberUniqueValidation.isValid()) {
                System.err.println("Error: Duplicate account creation attempted");
                result.addError(accountNumberUniqueValidation.getFirstErrorMessage(), "ACCOUNT_NUMBER_EXISTS");
            }
        }
        
        System.out.println("=== ACCOUNT VALIDATION RESULT ===");
        System.out.println("Valid: " + result.isValid());
        if (!result.isValid()) {
            System.err.println("Validation Errors: " + result.getAllErrorMessages());
            System.err.println("Error Code: " + result.getErrorCode());
        }
        
        return result;
    }

    public ValidationResult validateAccountForUpdate(String accountId, Account account) {
        System.out.println("=== ACCOUNT UPDATE VALIDATION STARTED ===");
        System.out.println("Account ID: " + accountId);
        System.out.println("Aadhar Number: " + account.getAadharNumber());
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("IFSC Code: " + account.getIfscCode());
        
        ValidationResult result = new ValidationResult();
        
        if (!accountExists(accountId)) {
            result.addError("Account not found with ID: " + accountId);
            return result;
        }
        
        if (account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
            System.err.println("Error: Required field missing (account number)");
            result.addError("Account number is required");
        }
        
        if (account.getAadharNumber() == null || account.getAadharNumber().trim().isEmpty()) {
            System.err.println("Error: Required field missing (aadhar number)");
            result.addError("Aadhar number is required");
        }
        
        if (account.getIfscCode() == null || account.getIfscCode().trim().isEmpty()) {
            System.err.println("Error: Required field missing (IFSC code)");
            result.addError("IFSC code is required");
        }
        
        if (account.getBankName() == null || account.getBankName().trim().isEmpty()) {
            System.err.println("Error: Required field missing (bank name)");
            result.addError("Bank name is required");
        }
        
        if (account.getNameOnAccount() == null || account.getNameOnAccount().trim().isEmpty()) {
            System.err.println("Error: Required field missing (name on account)");
            result.addError("Name on account is required");
        }
        
        ValidationResult accountNumberValidation = validateAccountNumber(account.getAccountNumber());
        if (!accountNumberValidation.isValid()) {
            System.err.println("Error: Invalid account number format");
            result.addError(accountNumberValidation.getFirstErrorMessage());
        }
        
        ValidationResult aadharValidation = validateAadharNumber(account.getAadharNumber());
        if (!aadharValidation.isValid()) {
            System.err.println("Error: Invalid aadhar number format");
            result.addError(aadharValidation.getFirstErrorMessage());
        }
        
        ValidationResult ifscValidation = validateIfscCode(account.getIfscCode());
        if (!ifscValidation.isValid()) {
            System.err.println("Error: Invalid IFSC code format");
            result.addError(ifscValidation.getFirstErrorMessage());
        }
        
        ValidationResult amountValidation = validateAmount(account.getAmount());
        if (!amountValidation.isValid()) {
            System.err.println("Error: Invalid amount");
            result.addError(amountValidation.getFirstErrorMessage());
        }
        
        ValidationResult bankNameValidation = validateBankName(account.getBankName());
        if (!bankNameValidation.isValid()) {
            System.err.println("Error: Invalid bank name");
            result.addError(bankNameValidation.getFirstErrorMessage());
        }
        
        ValidationResult nameOnAccountValidation = validateNameOnAccount(account.getNameOnAccount());
        if (!nameOnAccountValidation.isValid()) {
            System.err.println("Error: Invalid name on account");
            result.addError(nameOnAccountValidation.getFirstErrorMessage());
        }
        
        if (result.isValid()) {
            System.out.println("=== BASIC UPDATE VALIDATIONS PASSED - CHECKING DATABASE CONSTRAINTS ===");
            
            ValidationResult aadharExistsValidation = validateAadharExistsInCustomer(account.getAadharNumber());
            if (!aadharExistsValidation.isValid()) {
                System.err.println("Error: Aadhar number not found for update");
                result.addError("Aadhar number is not linked with any customer", "AADHAR_NOT_FOUND");
            }
            
            ValidationResult accountNumberUniqueValidation = validateAccountNumberUniquenessForUpdate(accountId, account.getAccountNumber());
            if (!accountNumberUniqueValidation.isValid()) {
                System.err.println("Error: Duplicate account number in update");
                result.addError(accountNumberUniqueValidation.getFirstErrorMessage(), "ACCOUNT_NUMBER_EXISTS");
            }
        }
        
        System.out.println("=== ACCOUNT UPDATE VALIDATION RESULT ===");
        System.out.println("Valid: " + result.isValid());
        if (!result.isValid()) {
            System.err.println("Update Validation Errors: " + result.getAllErrorMessages());
            System.err.println("Error Code: " + result.getErrorCode());
        }
        
        return result;
    }

    public ValidationResult validateAadharExistsInCustomer(String aadharNumber) {
        String query = "SELECT COUNT(*) FROM Customer WHERE aadhar_number = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, aadharNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println(" Aadhar number found in Customer Module: " + aadharNumber);
                return ValidationResult.success();
            } else {
                System.out.println(" Aadhar number not found in Customer Module: " + aadharNumber);
                return ValidationResult.failure("Aadhar number is not linked with any customer");
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking aadhar existence: " + e.getMessage());
            return ValidationResult.failure("Database error while validating aadhar number");
        }
    }

    // public ValidationResult validateAccountNumber(String accountNumber) {
    //     if (accountNumber == null || accountNumber.trim().isEmpty()) {
    //         return ValidationResult.failure("Account number is required");
    //     }
        
    //     if (accountNumber.length() < 10 || accountNumber.length() > 25) {
    //         return ValidationResult.failure("Account number must be between 10-25 characters");
    //     }
        
    //     return ValidationResult.success();
    // }
public ValidationResult validateAccountNumber(String accountNumber) {
    if (accountNumber == null || accountNumber.trim().isEmpty()) {
        return ValidationResult.failure("Account number is required");
    }
    
    accountNumber = accountNumber.trim();
    
    if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
        return ValidationResult.failure("Account number must be 10-25 digits and contain only numbers (0-9). No letters, spaces, or special characters allowed.");
    }
    
    return ValidationResult.success();
}

   
public ValidationResult validateAccountNumberFormat(String accountNumber) {
    if (accountNumber == null || accountNumber.trim().isEmpty()) {
        return ValidationResult.failure("Account number is required");
    }
    
    accountNumber = accountNumber.trim();
    
    if (accountNumber.contains(" ")) {
        return ValidationResult.failure("Account number cannot contain spaces. Please enter digits only.");
    }
    
    if (accountNumber.matches(".*[a-zA-Z].*")) {
        return ValidationResult.failure("Account number cannot contain letters. Please enter digits only.");
    }
    
    if (accountNumber.matches(".*[^0-9].*")) {
        return ValidationResult.failure("Account number can only contain numeric digits (0-9). Special characters like '-', '+', '.' are not allowed.");
    }
    
   
    if (accountNumber.length() < 10) {
        return ValidationResult.failure("Account number is too short. It must be at least 10 digits.");
    }
    
    if (accountNumber.length() > 25) {
        return ValidationResult.failure("Account number is too long. It cannot exceed 25 digits.");
    }
    
    return ValidationResult.success();
}
 
    public ValidationResult validateAadharNumber(String aadharNumber) {
        if (aadharNumber == null || aadharNumber.trim().isEmpty()) {
            return ValidationResult.failure("Aadhar number is required");
        }
        
        String cleanAadhar = aadharNumber.replaceAll("[^0-9]", "");
        
        if (cleanAadhar.length() != 12) {
            return ValidationResult.failure("Aadhar number must be exactly 12 digits");
        }
        
        if (!AADHAR_PATTERN.matcher(cleanAadhar).matches()) {
            return ValidationResult.failure("Aadhar number format is invalid");
        }
        
        return ValidationResult.success();
    }

    public ValidationResult validateIfscCode(String ifscCode) {
        if (ifscCode == null || ifscCode.trim().isEmpty()) {
            return ValidationResult.failure("IFSC code is required");
        }
        
        if (ifscCode.length() != 11) {
            return ValidationResult.failure("IFSC code must be exactly 11 characters");
        }
        
        if (!IFSC_PATTERN.matcher(ifscCode.toUpperCase()).matches()) {
            return ValidationResult.failure("IFSC code format is invalid. Format: ABCD0123456");
        }
        
        return ValidationResult.success();
    }

    public ValidationResult validateAmount(BigDecimal amount) {
        System.out.println("\n");
        System.out.println("=== AMOUNT VALIDATION DEBUG ===");
        System.out.println("Amount received: " + amount);
        System.out.println("Amount class: " + (amount != null ? amount.getClass() : "null"));
        
        if (amount == null) {
            System.out.println("Amount is null - will be set to default 0 after validation");
            return ValidationResult.success(); 
        }
        
        System.out.println("Comparing amount " + amount + " with minimum 0");
        BigDecimal minimumBalance = BigDecimal.valueOf(0);
        int comparison = amount.compareTo(minimumBalance);
        
        System.out.println("Comparison result: " + comparison + " (negative means amount < 0)");
        
        if (comparison < 0) {
            System.out.println(" VALIDATION FAILED - Amount " + amount + " is less than minimum " + minimumBalance);
            System.err.println("Error: Amount below minimum balance");
            return ValidationResult.failure("Amount must have a minimum balance of 0");
        }
        
        if (amount.scale() > 2) {
            System.out.println(" VALIDATION FAILED - Amount has more than 2 decimal places");
            return ValidationResult.failure("Amount cannot have more than 2 decimal places");
        }
        
        System.out.println(" Amount validation passed: " + amount);
        return ValidationResult.success();
    }

   
    public ValidationResult validateBankName(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) {
            return ValidationResult.failure("Bank name is required");
        }
        
        if (bankName.trim().length() < 2) {
            return ValidationResult.failure("Bank name must be at least 2 characters");
        }
        
        if (bankName.length() > 100) {
            return ValidationResult.failure("Bank name cannot exceed 100 characters");
        }
        
        return ValidationResult.success();
    }

    
    public ValidationResult validateNameOnAccount(String nameOnAccount) {
        if (nameOnAccount == null || nameOnAccount.trim().isEmpty()) {
            return ValidationResult.failure("Name on account is required");
        }
        
        if (nameOnAccount.trim().length() < 2) {
            return ValidationResult.failure("Name on account must be at least 2 characters");
        }
        
        if (nameOnAccount.length() > 100) {
            return ValidationResult.failure("Name on account cannot exceed 100 characters");
        }
        
        return ValidationResult.success();
    }

 
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
            
            System.out.println(" Account number is unique: " + accountNumber);
            return ValidationResult.success();
            
        } catch (SQLException e) {
            System.err.println("Error checking account number uniqueness: " + e.getMessage());
            return ValidationResult.failure("Database error while checking account number");
        }
    }

    
    public ValidationResult validateAccountNumberUniquenessForUpdate(String currentAccountId, String accountNumber) {
        String query = "SELECT COUNT(*) FROM Account WHERE account_number = ? AND account_id != ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountNumber);
            stmt.setString(2, currentAccountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("=== DUPLICATE ACCOUNT NUMBER (OTHER ACCOUNT) ===");
                System.out.println("Account Number: " + accountNumber);
                return ValidationResult.failure("Account number already exists with another account");
            }
            
            System.out.println(" Account number is unique for update: " + accountNumber);
            return ValidationResult.success();
            
        } catch (SQLException e) {
            System.err.println("Error checking account number uniqueness for update: " + e.getMessage());
            return ValidationResult.failure("Database error while checking account number");
        }
    }

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
