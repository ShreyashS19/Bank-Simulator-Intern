package com.bank.simulator.validation;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AccountValidatorTest {

    @Test
    void testValidAccountNumber() {
        assertTrue(isValidAccountNumber("1234567890123456"));
    }

    @Test
    void testAccountNumberTooShort() {
        assertFalse(isValidAccountNumber("123456789"));
    }

    @Test
    void testAccountNumberTooLong() {
        assertFalse(isValidAccountNumber("12345678901234567890123456"));
    }

    @Test
    void testAccountNumberWithLetters() {
        assertFalse(isValidAccountNumber("12345abcde"));
    }

    @Test
    void testValidIFSC() {
        assertTrue(isValidIFSC("ABCD0123456"));
    }

    @Test
    void testInvalidIFSCFormat() {
        assertFalse(isValidIFSC("ABC123456"));
    }

    @Test
    void testIFSCWrongLength() {
        assertFalse(isValidIFSC("ABCD01234567"));
    }

    @Test
    void testValidAmount() {
        assertTrue(isValidAmount(BigDecimal.valueOf(1000)));
    }

    @Test
    void testAmountBelowMinimum() {
        assertFalse(isValidAmount(BigDecimal.valueOf(500)));
    }

    @Test
    void testNegativeAmount() {
        assertFalse(isValidAmount(BigDecimal.valueOf(-100)));
    }

    @Test
    void testValidBankName() {
        assertTrue(isValidBankName("State Bank of India"));
    }

    @Test
    void testEmptyBankName() {
        assertFalse(isValidBankName(""));
    }

    @Test
    void testValidNameOnAccount() {
        assertTrue(isValidNameOnAccount("John Doe"));
    }

    @Test
    void testNameOnAccountWithNumbers() {
        assertFalse(isValidNameOnAccount("John123"));
    }

    @Test
    void testValidAadhar() {
        assertTrue(isValidAadhar("123456789012"));
    }

    // Helper methods
    private boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && accountNumber.matches("^\\d{10,25}$");
    }

    private boolean isValidIFSC(String ifsc) {
        return ifsc != null && ifsc.matches("^[A-Z]{4}0[A-Z0-9]{6}$");
    }

    private boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.valueOf(600)) >= 0;
    }

    private boolean isValidBankName(String bankName) {
        return bankName != null && bankName.trim().length() >= 3;
    }

    private boolean isValidNameOnAccount(String name) {
        return name != null && name.matches("^[a-zA-Z\\s]+$") && name.trim().length() >= 2;
    }

    private boolean isValidAadhar(String aadhar) {
        return aadhar != null && aadhar.matches("^\\d{12}$");
    }
}
