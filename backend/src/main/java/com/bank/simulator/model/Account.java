package com.bank.simulator.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private String accountId;
    private String customerId;
    private String accountNumber;
    private String aadharNumber;
    private String ifscCode;
    private String phoneNumberLinked;
    private BigDecimal amount = BigDecimal.valueOf(600.00);  
    private String bankName;
    private String nameOnAccount;
    private String status = "ACTIVE";  
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modified;

    
    public Account() {}

    public Account(String accountId, String customerId, String accountNumber, String aadharNumber,
                   String ifscCode, String phoneNumberLinked, BigDecimal amount, String bankName,
                   String nameOnAccount, String status, LocalDateTime created, LocalDateTime modified) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.aadharNumber = aadharNumber;
        this.ifscCode = ifscCode;
        this.phoneNumberLinked = phoneNumberLinked;
        this.amount = amount;
        this.bankName = bankName;
        this.nameOnAccount = nameOnAccount;
        this.status = status;
        this.created = created;
        this.modified = modified;
    }


    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getPhoneNumberLinked() { return phoneNumberLinked; }
    public void setPhoneNumberLinked(String phoneNumberLinked) { this.phoneNumberLinked = phoneNumberLinked; }

    public BigDecimal getAmount() { return amount; }
    // public void setAmount(BigDecimal amount) { 
    //     // Ensure minimum balance of 600
    //     if (amount != null && amount.compareTo(BigDecimal.valueOf(600)) >= 0) {
    //         this.amount = amount;
    //     } else {
    //         this.amount = BigDecimal.valueOf(600.00);
    //     }
    // }
    public void setAmount(BigDecimal amount) {
    this.amount = amount;
}

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getNameOnAccount() { return nameOnAccount; }
    public void setNameOnAccount(String nameOnAccount) { this.nameOnAccount = nameOnAccount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    public LocalDateTime getModified() { return modified; }
    public void setModified(LocalDateTime modified) { this.modified = modified; }

    @Override
    public String toString() {
        return "Account{" +
                "accountId='" + accountId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", aadharNumber='" + aadharNumber + '\'' +
                ", ifscCode='" + ifscCode + '\'' +
                ", phoneNumberLinked='" + phoneNumberLinked + '\'' +
                ", amount=" + amount +
                ", bankName='" + bankName + '\'' +
                ", nameOnAccount='" + nameOnAccount + '\'' +
                ", status='" + status + '\'' +
                ", created=" + created +
                ", modified=" + modified +
                '}';
    }
}
