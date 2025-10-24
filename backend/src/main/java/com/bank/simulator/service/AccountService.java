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
    boolean isAccountNumberExists(String accountNumber);
    String getCustomerPhoneNumber(String customerId);
    String generateAccountId();
     List<Account> getAllAccounts();
}
