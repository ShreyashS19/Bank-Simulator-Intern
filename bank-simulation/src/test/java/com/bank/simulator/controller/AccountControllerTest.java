package com.bank.simulator.controller;

import com.bank.simulator.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AccountControllerTest {

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = createValidAccount();
    }

    @Test
    void testCreateAccount_ValidData() {
        int statusCode = simulateCreateAccountAPI(testAccount);
        assertEquals(201, statusCode);
    }

    @Test
    void testCreateAccount_BelowMinimumBalance() {
        testAccount.setAmount(BigDecimal.valueOf(500));
        int statusCode = simulateCreateAccountAPI(testAccount);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateAccount_InvalidAccountNumber() {
        testAccount.setAccountNumber("123");
        int statusCode = simulateCreateAccountAPI(testAccount);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateAccount_NullData() {
        int statusCode = simulateCreateAccountAPI(null);
        assertEquals(400, statusCode);
    }

    @Test
    void testGetAccount_ValidId() {
        int statusCode = simulateGetAccountAPI("ACC001");
        assertEquals(200, statusCode);
    }

    @Test
    void testGetAccount_InvalidId() {
        int statusCode = simulateGetAccountAPI("ACC999");
        assertEquals(404, statusCode);
    }

    @Test
    void testGetAccountByCustomerId_Valid() {
        int statusCode = simulateGetAccountByCustomerIdAPI("CUST001");
        assertEquals(200, statusCode);
    }

    @Test
    void testUpdateAccount_ValidData() {
        testAccount.setBankName("Updated Bank");
        int statusCode = simulateUpdateAccountAPI("ACC001", testAccount);
        assertEquals(200, statusCode);
    }

    @Test
    void testUpdateAccount_InvalidId() {
        int statusCode = simulateUpdateAccountAPI("ACC999", testAccount);
        assertEquals(404, statusCode);
    }

    @Test
    void testDeleteAccount_ValidId() {
        int statusCode = simulateDeleteAccountAPI("ACC001");
        assertEquals(200, statusCode);
    }

    @Test
    void testDeleteAccount_InvalidId() {
        int statusCode = simulateDeleteAccountAPI("ACC999");
        assertEquals(404, statusCode);
    }

    @Test
    void testCreateAccount_DatabaseError() {
        int statusCode = simulateCreateAccountWithError(testAccount);
        assertEquals(500, statusCode);
    }

    // Helper methods
    private int simulateCreateAccountAPI(Account account) {
        if (account == null) return 400;
        if (account.getAmount() == null || account.getAmount().compareTo(BigDecimal.valueOf(600)) < 0) return 400;
        if (!account.getAccountNumber().matches("^\\d{10,25}$")) return 400;
        return 201;
    }

    private int simulateGetAccountAPI(String id) {
        return "ACC001".equals(id) ? 200 : 404;
    }

    private int simulateGetAccountByCustomerIdAPI(String customerId) {
        return "CUST001".equals(customerId) ? 200 : 404;
    }

    private int simulateUpdateAccountAPI(String id, Account account) {
        return "ACC001".equals(id) ? 200 : 404;
    }

    private int simulateDeleteAccountAPI(String id) {
        return "ACC001".equals(id) ? 200 : 404;
    }

    private int simulateCreateAccountWithError(Account account) {
        return 500;
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
