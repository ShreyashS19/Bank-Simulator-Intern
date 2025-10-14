package com.bank.simulator.validation;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class TransactionValidatorTest {

    @Test
    void testValidSenderAccountNumber() {
        assertTrue(isValidAccountNumber("1234567890123456"));
    }

    @Test
    void testInvalidSenderAccountNumber_TooShort() {
        assertFalse(isValidAccountNumber("12345"));
    }

    @Test
    void testInvalidSenderAccountNumber_TooLong() {
        assertFalse(isValidAccountNumber("12345678901234567890123456"));
    }

    @Test
    void testInvalidSenderAccountNumber_WithLetters() {
        assertFalse(isValidAccountNumber("123ABC789"));
    }

    @Test
    void testValidReceiverAccountNumber() {
        assertTrue(isValidAccountNumber("9876543210987654"));
    }

    @Test
    void testSameAccountNumbers() {
        assertFalse(isDifferentAccounts("1234567890123456", "1234567890123456"));
    }

    @Test
    void testDifferentAccountNumbers() {
        assertTrue(isDifferentAccounts("1234567890123456", "9876543210987654"));
    }

    @Test
    void testValidTransactionAmount() {
        assertTrue(isValidAmount(BigDecimal.valueOf(1000)));
    }

    @Test
    void testInvalidAmount_Zero() {
        assertFalse(isValidAmount(BigDecimal.ZERO));
    }

    @Test
    void testInvalidAmount_Negative() {
        assertFalse(isValidAmount(BigDecimal.valueOf(-100)));
    }

    @Test
    void testInvalidAmount_MoreThanTwoDecimals() {
        assertFalse(isValidAmount(new BigDecimal("100.123")));
    }

    @Test
    void testValidTransactionType() {
        assertTrue(isValidTransactionType("ONLINE"));
    }

    @Test
    void testInvalidTransactionType() {
        assertFalse(isValidTransactionType("CASH"));
    }

    @Test
    void testEmptyTransactionType() {
        assertFalse(isValidTransactionType(""));
    }

    @Test
    void testNullTransactionType() {
        assertFalse(isValidTransactionType(null));
    }

    // Helper methods
    private boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && accountNumber.matches("^[0-9]{10,25}$");
    }

    private boolean isDifferentAccounts(String sender, String receiver) {
        return sender != null && receiver != null && !sender.equals(receiver);
    }

    private boolean isValidAmount(BigDecimal amount) {
        if (amount == null) return false;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        if (amount.scale() > 2) return false;
        return true;
    }

    private boolean isValidTransactionType(String type) {
        return type != null && !type.trim().isEmpty() && "ONLINE".equalsIgnoreCase(type);
    }
}
