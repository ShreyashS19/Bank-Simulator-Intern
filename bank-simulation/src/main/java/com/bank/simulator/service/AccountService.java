package com.bank.simulator.service;

import com.bank.simulator.model.Account;
import java.math.BigDecimal;

public interface AccountService {
    // Core CRUD operations
    String createAccount(Account account);
    Account getAccountById(String accountId);
    boolean updateAccount(String accountId, Account account);
    boolean deleteAccount(String accountId);
    
    // Validation and utility methods
    boolean isAccountNumberExists(String accountNumber);
    boolean isCustomerExists(String customerId);
    String getCustomerPhoneNumber(String customerId);
    boolean isPhoneNumberLinkedToCustomer(String customerId, String phoneNumber);
    
    // Debug and utility methods
    void debugPrintAccountDetails(String accountId);
    
    // ID generation
    String generateAccountId();
}
