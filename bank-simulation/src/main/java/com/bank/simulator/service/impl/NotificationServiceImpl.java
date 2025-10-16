package com.bank.simulator.service.impl;

import com.bank.simulator.service.NotificationService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class NotificationServiceImpl implements NotificationService {

    private final Properties emailProperties;
    private final boolean emailEnabled;
    private final String fromEmail;
    private final String username;
    private final String password;
    private final boolean requiresAuth;

    public NotificationServiceImpl() {
        emailProperties = new Properties();
        
        Properties appProps = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                appProps.load(input);
            } else {
                System.err.println("application.properties not found!");
            }
        } catch (IOException e) {
            System.err.println("Could not load application.properties for email config");
            e.printStackTrace();
        }

        String smtpHost = appProps.getProperty("email.smtp.host", "smtp.gmail.com");
        String smtpPort = appProps.getProperty("email.smtp.port", "587");
        String smtpAuth = appProps.getProperty("email.smtp.auth", "true");
        String smtpStartTls = appProps.getProperty("email.smtp.starttls.enable", "true");
        String smtpSslTrust = appProps.getProperty("email.smtp.ssl.trust", "smtp.gmail.com");
        
        emailProperties.put("mail.smtp.host", smtpHost);
        emailProperties.put("mail.smtp.port", smtpPort);
        emailProperties.put("mail.smtp.auth", smtpAuth);
        emailProperties.put("mail.smtp.starttls.enable", smtpStartTls);
        emailProperties.put("mail.smtp.ssl.trust", smtpSslTrust);
        emailProperties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        this.fromEmail = appProps.getProperty("email.from", "noreply@banksimulator.com");
        this.username = appProps.getProperty("email.username", "");
        this.password = appProps.getProperty("email.password", "");
        this.requiresAuth = Boolean.parseBoolean(smtpAuth);
        this.emailEnabled = Boolean.parseBoolean(appProps.getProperty("email.enabled", "true"));

        System.out.println("\n=== EMAIL SERVICE INITIALIZED ===");
        System.out.println("SMTP Host: " + smtpHost);
        System.out.println("SMTP Port: " + smtpPort);
        System.out.println("Authentication: " + smtpAuth);
        System.out.println("TLS Enabled: " + smtpStartTls);
        System.out.println("Email From: " + fromEmail);
        System.out.println("Email Enabled: " + emailEnabled);
        System.out.println("=====================================\n");
    }

    @Override
    public void sendTransactionNotificationToSender(
            String senderEmail,
            String senderName,
            String senderBankName,
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            String transactionId
    ) {
        if (!emailEnabled) {
            System.out.println("Email notifications are disabled. Skipping sender email.");
            return;
        }

        String subject = "Transaction Alert  Amount Debited";
        String body = buildSenderEmailTemplate(
            senderName, 
            senderBankName,
            senderAccountNumber,
            receiverAccountNumber,
            amount, 
            transactionId
        );

        sendNotification(senderEmail, subject, body);
    }

    @Override
    public void sendTransactionNotificationToReceiver(
            String receiverEmail,
            String receiverName,
            String receiverBankName,
            String receiverAccountNumber,
            String senderAccountNumber,
            BigDecimal amount,
            String transactionId
    ) {
        if (!emailEnabled) {
            System.out.println("Email notifications are disabled. Skipping receiver email.");
            return;
        }

        String subject = "Transaction Alert  Amount Credited";
        String body = buildReceiverEmailTemplate(
            receiverName,
            receiverBankName,
            receiverAccountNumber,
            senderAccountNumber,
            amount,
            transactionId
        );

        sendNotification(receiverEmail, subject, body);
    }

    @Override
    public boolean sendNotification(String toEmail, String subject, String body) {
        try {
            System.out.println("\n=== SENDING REAL EMAIL ===");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: " + subject);
            System.out.println("From: " + fromEmail);

            Session session;
            
            if (requiresAuth) {
                session = Session.getInstance(emailProperties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
            } else {
                session = Session.getInstance(emailProperties);
            }

            session.setDebug(false);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println(" Email sent successfully to: " + toEmail);
            System.out.println("========================\n");
            
            return true;

        } catch (MessagingException e) {
            System.err.println("\n!!! FAILED TO SEND EMAIL !!!");
            System.err.println("Recipient: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================\n");
            return false;
        }
    }

  
private String buildSenderEmailTemplate(
        String senderName, 
        String bankName,
        String senderAccount,
        String receiverAccount, 
        BigDecimal amount,
        String transactionId  
) {
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
    String transactionDate = now.format(formatter);

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f5f5f5;
                    margin: 0;
                    padding: 20px;
                }
                .email-container {
                    max-width: 600px;
                    margin: 0 auto;
                    background-color: #ffffff;
                    border-radius: 8px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    overflow: hidden;
                }
                .email-header {
                    background-color: #1976D2;
                    color: white;
                    padding: 20px;
                    text-align: center;
                }
                .email-header h1 {
                    margin: 0;
                    font-size: 24px;
                }
                .email-body {
                    padding: 30px;
                    color: #333333;
                    line-height: 1.8;
                }
                .greeting {
                    font-size: 16px;
                    margin-bottom: 20px;
                }
                .message {
                    font-size: 15px;
                    margin-bottom: 25px;
                }
                .details-section {
                    background-color: #f9f9f9;
                    border-left: 4px solid #1976D2;
                    padding: 20px;
                    margin: 20px 0;
                }
                .details-title {
                    font-weight: bold;
                    font-size: 16px;
                    margin-bottom: 15px;
                    color: #1976D2;
                }
                .detail-row {
                    display: flex;
                    justify-content: space-between;
                    padding: 8px 0;
                    border-bottom: 1px solid #e0e0e0;
                }
                .detail-row:last-child {
                    border-bottom: none;
                }
                .detail-label {
                    font-weight: bold;
                    color: #666666;
                }
                .detail-value {
                    color: #333333;
                    text-align: right;
                }
                .amount-highlight {
                    color: #d32f2f;
                    font-weight: bold;
                    font-size: 18px;
                }
                .closing {
                    margin-top: 30px;
                    font-size: 15px;
                }
                .signature {
                    margin-top: 20px;
                    font-weight: bold;
                }
                .email-footer {
                    background-color: #f5f5f5;
                    padding: 20px;
                    text-align: center;
                    font-size: 12px;
                    color: #999999;
                    border-top: 1px solid #e0e0e0;
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="email-header">
                    <h1>%s</h1>
                </div>
                <div class="email-body">
                    <div class="greeting">Hi %s,</div>
                    
                    <div class="message">
                        Your account has been debited with <span class="amount-highlight">₹%s</span> for a transaction to account %s.
                    </div>
                    
                    <div class="details-section">
                        <div class="details-title">Transaction Details:</div>
                        <div class="detail-row">
                            <span class="detail-label">Bank:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Sender Account:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Receiver Account:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Amount:</span>
                            <span class="detail-value amount-highlight">₹%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Date:</span>
                            <span class="detail-value">%s</span>
                        </div>
                    </div>
                    
                    <div class="closing">
                        Thank you for banking with %s.
                    </div>
                    
                    <div class="signature">
                        Best regards,<br>
                        %s Team
                    </div>
                </div>
                <div class="email-footer">
                    <p>This is an automated message. Please do not reply to this email.</p>
                    <p>&copy; 2025 %s. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
            bankName,              
            senderName,            
            amount.toString(),    
            receiverAccount,       
            bankName,             
            senderAccount,        
            receiverAccount,       
            amount.toString(),     
            transactionDate,     
            bankName,             
            bankName,              
            bankName               
        );
}


private String buildReceiverEmailTemplate(
        String receiverName,
        String bankName,
        String receiverAccount,
        String senderAccount,
        BigDecimal amount,
        String transactionId  
) {
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
    String transactionDate = now.format(formatter);

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f5f5f5;
                    margin: 0;
                    padding: 20px;
                }
                .email-container {
                    max-width: 600px;
                    margin: 0 auto;
                    background-color: #ffffff;
                    border-radius: 8px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    overflow: hidden;
                }
                .email-header {
                    background-color: #4CAF50;
                    color: white;
                    padding: 20px;
                    text-align: center;
                }
                .email-header h1 {
                    margin: 0;
                    font-size: 24px;
                }
                .email-body {
                    padding: 30px;
                    color: #333333;
                    line-height: 1.8;
                }
                .greeting {
                    font-size: 16px;
                    margin-bottom: 20px;
                }
                .message {
                    font-size: 15px;
                    margin-bottom: 25px;
                }
                .details-section {
                    background-color: #f9f9f9;
                    border-left: 4px solid #4CAF50;
                    padding: 20px;
                    margin: 20px 0;
                }
                .details-title {
                    font-weight: bold;
                    font-size: 16px;
                    margin-bottom: 15px;
                    color: #4CAF50;
                }
                .detail-row {
                    display: flex;
                    justify-content: space-between;
                    padding: 8px 0;
                    border-bottom: 1px solid #e0e0e0;
                }
                .detail-row:last-child {
                    border-bottom: none;
                }
                .detail-label {
                    font-weight: bold;
                    color: #666666;
                }
                .detail-value {
                    color: #333333;
                    text-align: right;
                }
                .amount-highlight {
                    color: #4CAF50;
                    font-weight: bold;
                    font-size: 18px;
                }
                .closing {
                    margin-top: 30px;
                    font-size: 15px;
                }
                .signature {
                    margin-top: 20px;
                    font-weight: bold;
                }
                .email-footer {
                    background-color: #f5f5f5;
                    padding: 20px;
                    text-align: center;
                    font-size: 12px;
                    color: #999999;
                    border-top: 1px solid #e0e0e0;
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="email-header">
                    <h1>%s</h1>
                </div>
                <div class="email-body">
                    <div class="greeting">Hi %s,</div>
                    
                    <div class="message">
                        Your account has been credited with <span class="amount-highlight">₹%s</span> from account %s.
                    </div>
                    
                    <div class="details-section">
                        <div class="details-title">Transaction Details:</div>
                        <div class="detail-row">
                            <span class="detail-label">Bank:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Sender Account:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Receiver Account:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Amount:</span>
                            <span class="detail-value amount-highlight">₹%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Date:</span>
                            <span class="detail-value">%s</span>
                        </div>
                    </div>
                    
                    <div class="closing">
                        Thank you for banking with %s.
                    </div>
                    
                    <div class="signature">
                        Best regards,<br>
                        %s Team
                    </div>
                </div>
                <div class="email-footer">
                    <p>This is an automated message. Please do not reply to this email.</p>
                    <p>&copy; 2025 %s. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
            bankName,              
            receiverName,          
            amount.toString(),     
            senderAccount,         
            bankName,              
            senderAccount,        
            receiverAccount,       
            amount.toString(),     
            transactionDate,       
            bankName,              
            bankName,              
            bankName              
        );
}

}
