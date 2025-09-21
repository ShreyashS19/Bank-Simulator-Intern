package com.bank.simulator.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private String transactionId;
    private String accountId;
    private BigDecimal transactionAmount;
    private String transactionType; // debited/credited
    private LocalDateTime transactionTime;
    private String transactionMode; // debit, UPI, credit card
    private String receiverDetails;
    private String senderDetails;

    public Transaction() {}

    public Transaction(String transactionId, String accountId, BigDecimal transactionAmount,
                      String transactionType, LocalDateTime transactionTime, String transactionMode,
                      String receiverDetails, String senderDetails) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionAmount = transactionAmount;
        this.transactionType = transactionType;
        this.transactionTime = transactionTime;
        this.transactionMode = transactionMode;
        this.receiverDetails = receiverDetails;
        this.senderDetails = senderDetails;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }

    public String getTransactionMode() { return transactionMode; }
    public void setTransactionMode(String transactionMode) { this.transactionMode = transactionMode; }

    public String getReceiverDetails() { return receiverDetails; }
    public void setReceiverDetails(String receiverDetails) { this.receiverDetails = receiverDetails; }

    public String getSenderDetails() { return senderDetails; }
    public void setSenderDetails(String senderDetails) { this.senderDetails = senderDetails; }
}
