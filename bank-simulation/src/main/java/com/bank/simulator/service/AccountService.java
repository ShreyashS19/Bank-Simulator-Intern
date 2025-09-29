package com.bank.simulator.service;

import com.bank.simulator.model.Account;

public interface AccountService {
    // Core CRUD operations
    String createAccount(Account account);
    Account getAccountById(String accountId);
    Account getAccountByCustomerId(String customerId);
    Account getAccountByAccountNumber(String accountNumber);
    boolean updateAccount(String accountId, Account account);
    boolean deleteAccount(String accountId);  // Soft delete
    
    // Validation and utility methods
    boolean isAccountNumberExists(String accountNumber);
   // boolean isCustomerExists(String customerId);
    String getCustomerPhoneNumber(String customerId);
    //boolean isPhoneNumberLinkedToCustomer(String customerId, String phoneNumber);
    
    // Debug and utility methods
 //   void debugPrintAccountDetails(String accountId);
    
    // ID generation
    String generateAccountId();
}
