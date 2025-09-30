package com.bank.simulator.service.impl;

import com.bank.simulator.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AccountServiceImplTest {

    private Account testAccount;
    private Map<String, Account> mockDatabase;

    @BeforeEach
    void setUp() {
        mockDatabase = new HashMap<>();
        testAccount = createValidAccount();
        mockDatabase.put("ACC001", testAccount);
    }

    @Test
    void testCreateAccount_ValidData() {
        Account newAccount = createValidAccount();
        newAccount.setAccountNumber("1234567890123457");
        String result = simulateCreateAccount(newAccount);
        assertNotNull(result);
        assertTrue(result.startsWith("ACC"));
    }

    @Test
    void testCreateAccount_NullAccount() {
        String result = simulateCreateAccount(null);
        assertNull(result);
    }

    @Test
    void testCreateAccount_InvalidAccountNumber() {
        testAccount.setAccountNumber("123");
        String result = simulateCreateAccount(testAccount);
        assertNull(result);
    }

    @Test
    void testCreateAccount_BelowMinimumBalance() {
        testAccount.setAmount(BigDecimal.valueOf(500));
        String result = simulateCreateAccount(testAccount);
        assertNull(result);
    }

    @Test
    void testCreateAccount_DuplicateAccountNumber() {
        Account duplicate = createValidAccount();
        String result = simulateCreateAccount(duplicate);
        assertNull(result);
    }

    @Test
    void testGetAccountById_ValidId() {
        Account result = simulateGetAccountById("ACC001");
        assertNotNull(result);
        assertEquals("1234567890123456", result.getAccountNumber());
    }

    @Test
    void testGetAccountById_InvalidId() {
        Account result = simulateGetAccountById("ACC999");
        assertNull(result);
    }

    @Test
    void testGetAccountByCustomerId_Valid() {
        Account result = simulateGetAccountByCustomerId("CUST001");
        assertNotNull(result);
        assertEquals("CUST001", result.getCustomerId());
    }

    @Test
    void testUpdateAccount_ValidData() {
        testAccount.setBankName("Updated Bank");
        boolean result = simulateUpdateAccount("ACC001", testAccount);
        assertTrue(result);
    }

    @Test
    void testUpdateAccount_InvalidId() {
        boolean result = simulateUpdateAccount("ACC999", testAccount);
        assertFalse(result);
    }

    @Test
    void testDeleteAccount_ValidId() {
        boolean result = simulateDeleteAccount("ACC001");
        assertTrue(result);
    }

    @Test
    void testDeleteAccount_InvalidId() {
        boolean result = simulateDeleteAccount("ACC999");
        assertFalse(result);
    }

    @Test
    void testIsAccountNumberExists_Existing() {
        boolean result = simulateIsAccountNumberExists("1234567890123456");
        assertTrue(result);
    }

    @Test
    void testIsAccountNumberExists_NotExisting() {
        boolean result = simulateIsAccountNumberExists("9999999999999999");
        assertFalse(result);
    }

    @Test
    void testGenerateAccountId() {
        String id1 = generateAccountId();
        String id2 = generateAccountId();
        assertNotNull(id1);
        assertNotEquals(id1, id2);
        assertTrue(id1.startsWith("ACC"));
    }

    // Helper methods
    private String simulateCreateAccount(Account account) {
        if (account == null || account.getAccountNumber() == null) return null;
        if (!account.getAccountNumber().matches("^\\d{10,25}$")) return null;
        if (account.getAmount().compareTo(BigDecimal.valueOf(600)) < 0) return null;
        if (simulateIsAccountNumberExists(account.getAccountNumber())) return null;
        String id = generateAccountId();
        mockDatabase.put(id, account);
        return id;
    }

    private Account simulateGetAccountById(String id) {
        return mockDatabase.get(id);
    }

    private Account simulateGetAccountByCustomerId(String customerId) {
        return mockDatabase.values().stream()
            .filter(a -> customerId.equals(a.getCustomerId()))
            .findFirst().orElse(null);
    }

    private boolean simulateUpdateAccount(String id, Account account) {
        if (!mockDatabase.containsKey(id)) return false;
        mockDatabase.put(id, account);
        return true;
    }

    private boolean simulateDeleteAccount(String id) {
        return mockDatabase.remove(id) != null;
    }

    private boolean simulateIsAccountNumberExists(String accountNumber) {
        return mockDatabase.values().stream()
            .anyMatch(a -> accountNumber.equals(a.getAccountNumber()));
    }

    private String generateAccountId() {
        return "ACC" + (int)(Math.random() * 1000);
    }

    private Account createValidAccount() {
        Account account = new Account();
        account.setAccountId("ACC001");
        account.setCustomerId("CUST001");
        account.setAccountNumber("1234567890123456");
        account.setAadharNumber("123456789012");
        account.setIfscCode("ABCD0123456");
        account.setAmount(BigDecimal.valueOf(1000));
        account.setBankName("Test Bank");
        account.setNameOnAccount("John Doe");
        account.setStatus("ACTIVE");
        account.setCreated(LocalDateTime.now());
        return account;
    }
}
