package com.bank.simulator.service;

import com.bank.simulator.model.Account;

public interface AccountService {
    String createAccount(Account account);
    Account getAccountById(String accountId);
    boolean updateAccount(String accountId, Account account);
    boolean deleteAccount(String accountId);
    boolean isAccountNumberExists(String accountNumber);
    boolean isCustomerExists(String customerId);
    String generateAccountId();
}
