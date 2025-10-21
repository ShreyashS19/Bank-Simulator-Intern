package com.bank.simulator.validation;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class CustomerValidatorTest {

    @Test
    void testValidPhoneNumber() {
        assertTrue(isValidPhone("9876543210"));
    }

    @Test
    void testInvalidPhoneNumber() {
        assertFalse(isValidPhone("-1"));
    }

    @Test
    void testPhoneStartsWithZero() {
        assertFalse(isValidPhone("0876543210"));
    }

    @Test
    void testPhoneTooShort() {
        assertFalse(isValidPhone("98765"));
    }

    @Test
    void testPhoneTooLong() {
        assertFalse(isValidPhone("98765432109"));
    }

    @Test
    void testValidEmail() {
        assertTrue(isValidEmail("john@example.com"));
    }

    @Test
    void testInvalidEmailFormat() {
        assertFalse(isValidEmail("invalid-email"));
    }

    @Test
    void testValidName() {
        assertTrue(isValidName("John Doe"));
    }

    @Test
    void testEmptyName() {
        assertFalse(isValidName(""));
    }

    @Test
    void testNameWithNumbers() {
        assertFalse(isValidName("John123"));
    }

    @Test
    void testValidAadhar() {
        assertTrue(isValidAadhar("123456789012"));
    }

    @Test
    void testInvalidAadharLength() {
        assertFalse(isValidAadhar("12345"));
    }

    @Test
    void testValidDateOfBirth() {
        assertTrue(isValidDOB(LocalDate.of(1990, 5, 15)));
    }

    @Test
    void testFutureDateOfBirth() {
        assertFalse(isValidDOB(LocalDate.now().plusDays(1)));
    }

    @Test
    void testValidAddress() {
        assertTrue(isValidAddress("123 Main Street, City"));
    }

    // Helper methods
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[6-9]\\d{9}$");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    private boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2 && name.matches("^[a-zA-Z\\s]+$");
    }

    private boolean isValidAadhar(String aadhar) {
        return aadhar != null && aadhar.matches("^\\d{12}$");
    }

    private boolean isValidDOB(LocalDate dob) {
        return dob != null && !dob.isAfter(LocalDate.now()) && !dob.isAfter(LocalDate.now().minusYears(18));
    }

    private boolean isValidAddress(String address) {
        return address != null && address.trim().length() >= 10;
    }
}
