package com.bank.simulator.service;

import java.math.BigDecimal;

public interface NotificationService {
    
    
    void sendTransactionNotificationToSender(
        String senderEmail, 
        String senderName,
        String senderBankName,  
        String senderAccountNumber, 
        String receiverAccountNumber,
        BigDecimal amount,
        String transactionId
    );
    
    
    void sendTransactionNotificationToReceiver(
        String receiverEmail,
        String receiverName,
        String receiverBankName, 
        String receiverAccountNumber, 
        String senderAccountNumber,
        BigDecimal amount,
        String transactionId
    );
    
    boolean sendNotification(String to, String subject, String body);
}
