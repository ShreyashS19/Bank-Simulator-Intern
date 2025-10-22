package com.bank.simulator.service;

import com.bank.simulator.model.Account;
import java.util.List;

public interface AccountService {
   
    String createAccount(Account account);
    Account getAccountById(String accountId);
    Account getAccountByCustomerId(String customerId);
    Account getAccountByAccountNumber(String accountNumber);
    boolean updateAccount(String accountId, Account account);
    boolean deleteAccount(String accountId);  
    
    // Validation and utility methods
    boolean isAccountNumberExists(String accountNumber);
   // boolean isCustomerExists(String customerId);
    String getCustomerPhoneNumber(String customerId);
    //boolean isPhoneNumberLinkedToCustomer(String customerId, String phoneNumber);
    
 //   void debugPrintAccountDetails(String accountId);
    
    
    String generateAccountId();
     List<Account> getAllAccounts();
}
