package com.bank.simulator.validation;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionValidator {

    public ValidationResult validateTransactionForCreation(Transaction transaction) {
        System.out.println("=== TRANSACTION VALIDATION STARTED ===");
        
        ValidationResult result = new ValidationResult();

        if (transaction == null) {
            return ValidationResult.failure("Transaction data is required");
        }

        ValidationResult senderValidation = validateSenderAccountNumber(transaction.getSenderAccountNumber());
        if (!senderValidation.isValid()) {
            result.addError(senderValidation.getFirstErrorMessage());
        }

        ValidationResult receiverValidation = validateReceiverAccountNumber(transaction.getReceiverAccountNumber());
        if (!receiverValidation.isValid()) {
            result.addError(receiverValidation.getFirstErrorMessage());
        }

        ValidationResult amountValidation = validateTransactionAmount(transaction.getAmount());
        if (!amountValidation.isValid()) {
            result.addError(amountValidation.getFirstErrorMessage());
        }

        ValidationResult typeValidation = validateTransactionType(transaction.getTransactionType());
        if (!typeValidation.isValid()) {
            result.addError(typeValidation.getFirstErrorMessage());
        }

        if (result.isValid()) {
            ValidationResult sameAccountValidation = validateDifferentAccounts(
                transaction.getSenderAccountNumber(), 
                transaction.getReceiverAccountNumber()
            );
            if (!sameAccountValidation.isValid()) {
                result.addError(sameAccountValidation.getFirstErrorMessage(), "SAME_ACCOUNT");
                return result;
            }

            ValidationResult senderExistsValidation = validateAccountExists(transaction.getSenderAccountNumber());
            if (!senderExistsValidation.isValid()) {
                result.addError("Sender account does not exist", "SENDER_NOT_FOUND");
                return result;
            }

            ValidationResult receiverExistsValidation = validateAccountExists(transaction.getReceiverAccountNumber());
            if (!receiverExistsValidation.isValid()) {
                result.addError("Receiver account does not exist", "RECEIVER_NOT_FOUND");
                return result;
            }

            ValidationResult balanceValidation = validateSufficientBalance(
                transaction.getSenderAccountNumber(), 
                transaction.getAmount()
            );
            if (!balanceValidation.isValid()) {
                result.addError(balanceValidation.getFirstErrorMessage(), "INSUFFICIENT_BALANCE");
            }
        }

        System.out.println("=== VALIDATION RESULT ===");
        System.out.println("Valid: " + result.isValid());
        if (!result.isValid()) {
            System.out.println("Errors: " + result.getAllErrorMessages());
        }

        return result;
    }

    private ValidationResult validateSenderAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return ValidationResult.failure("Sender account number is required");
        }

        if (!accountNumber.matches("^[0-9]{10,25}$")) {
            return ValidationResult.failure("Sender account number must be 10-25 digits");
        }

        return ValidationResult.success();
    }

    private ValidationResult validateReceiverAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return ValidationResult.failure("Receiver account number is required");
        }

        if (!accountNumber.matches("^[0-9]{10,25}$")) {
            return ValidationResult.failure("Receiver account number must be 10-25 digits");
        }

        return ValidationResult.success();
    }

    private ValidationResult validateDifferentAccounts(String senderAccountNumber, String receiverAccountNumber) {
        if (senderAccountNumber != null && senderAccountNumber.equals(receiverAccountNumber)) {
            return ValidationResult.failure("Sender and receiver account numbers must be different");
        }
        return ValidationResult.success();
    }

    private ValidationResult validateTransactionAmount(BigDecimal amount) {
        if (amount == null) {
            return ValidationResult.failure("Transaction amount is required");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.failure("Transaction amount must be greater than zero");
        }

        if (amount.scale() > 2) {
            return ValidationResult.failure("Transaction amount cannot have more than 2 decimal places");
        }

        return ValidationResult.success();
    }

    private ValidationResult validateTransactionType(String transactionType) {
        if (transactionType == null || transactionType.trim().isEmpty()) {
            return ValidationResult.failure("Transaction type is required");
        }

        if (!"ONLINE".equalsIgnoreCase(transactionType)) {
            return ValidationResult.failure("Transaction type must be 'ONLINE'");
        }

        return ValidationResult.success();
    }

    private ValidationResult validateAccountExists(String accountNumber) {
        String query = "SELECT COUNT(*) FROM Account WHERE account_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return ValidationResult.success();
            } else {
                return ValidationResult.failure("Account number does not exist: " + accountNumber);
            }

        } catch (SQLException e) {
            System.err.println("Error validating account existence: " + e.getMessage());
            return ValidationResult.failure("Database error while validating account existence");
        }
    }

    private ValidationResult validateSufficientBalance(String accountNumber, BigDecimal requiredAmount) {
        String query = "SELECT amount FROM Account WHERE account_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                BigDecimal currentBalance = rs.getBigDecimal("amount");
                System.out.println("Current Balance: " + currentBalance + ", Required: " + requiredAmount);

                if (currentBalance.compareTo(requiredAmount) < 0) {
                    return ValidationResult.failure(
                        "Insufficient balance. Available: " + currentBalance + ", Required: " + requiredAmount
                    );
                }

                return ValidationResult.success();
            } else {
                return ValidationResult.failure("Account not found");
            }

        } catch (SQLException e) {
            System.err.println("Error checking balance: " + e.getMessage());
            return ValidationResult.failure("Database error while checking balance");
        }
    }
}
