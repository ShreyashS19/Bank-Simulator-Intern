package com.bank.simulator.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private String accountId;
    private String customerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private BigDecimal balance = BigDecimal.valueOf(50.00);
    private String accountType;
    private String accountName;
    private String accountNumber;
    private String phoneNumberLinked;
    private String status;

    public Account() {}

    public Account(String accountId, String customerId, LocalDateTime createdAt, LocalDateTime modifiedAt,
                   BigDecimal balance, String accountType, String accountName, String accountNumber,
                   String phoneNumberLinked, String status) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.balance = balance;
        this.accountType = accountType;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.phoneNumberLinked = phoneNumberLinked;
        this.status = status;
    }

    // Getters and Setters
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(LocalDateTime modifiedAt) { this.modifiedAt = modifiedAt; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getPhoneNumberLinked() { return phoneNumberLinked; }
    public void setPhoneNumberLinked(String phoneNumberLinked) { this.phoneNumberLinked = phoneNumberLinked; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
