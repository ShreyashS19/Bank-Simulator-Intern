package com.bank.simulator.controller;

import com.bank.simulator.model.Account;
import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.model.Customer;
import com.bank.simulator.model.Transaction;
import com.bank.simulator.model.User;
import com.bank.simulator.service.AccountService;
import com.bank.simulator.service.CustomerService;
import com.bank.simulator.service.TransactionService;
import com.bank.simulator.service.UserService;
import com.bank.simulator.service.impl.AccountServiceImpl;
import com.bank.simulator.service.impl.CustomerServiceImpl;
import com.bank.simulator.service.impl.TransactionServiceImpl;
import com.bank.simulator.service.impl.UserServiceImpl;
import com.bank.simulator.validation.TransactionValidator;
import com.bank.simulator.validation.ValidationResult;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import com.bank.simulator.service.ExcelGeneratorService;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    private final TransactionService transactionService = new TransactionServiceImpl();
    private final TransactionValidator transactionValidator = new TransactionValidator();
    private final AccountService accountService = new AccountServiceImpl();
    private final CustomerService customerService = new CustomerServiceImpl();
    private final UserService userService = new UserServiceImpl(); 

    @POST
    @Path("/createTransaction")
    public Response createTransaction(Transaction transaction) {
        try {
            System.out.println("\n=== TRANSACTION CREATION REQUEST ===");

            if (transaction == null) {
                System.err.println("VALIDATION FAILED: Transaction object is null");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Transaction data is required"))
                    .build();
            }

            if (transaction.getPin() == null || transaction.getPin().trim().isEmpty()) {
                System.err.println("VALIDATION FAILED: Customer PIN missing");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Customer PIN is required"))
                    .build();
            }

            if (!transaction.getPin().matches("^[0-9]{6}$")) {
                System.err.println("PIN VALIDATION FAILED: Invalid format");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("PIN must be exactly 6 digits"))
                    .build();
            }

            if (transaction.getTransactionType() == null || transaction.getTransactionType().trim().isEmpty()) {
                transaction.setTransactionType("ONLINE");
                System.out.println("Transaction type not provided, defaulting to ONLINE");
            }

            if (transaction.getSenderAccountNumber() == null || transaction.getSenderAccountNumber().trim().isEmpty()) {
                System.err.println("VALIDATION FAILED: Sender account number missing");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Sender account number is required"))
                    .build();
            }

            if (transaction.getReceiverAccountNumber() == null || transaction.getReceiverAccountNumber().trim().isEmpty()) {
                System.err.println("VALIDATION FAILED: Receiver account number missing");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Receiver account number is required"))
                    .build();
            }

            if (transaction.getSenderAccountNumber().equals(transaction.getReceiverAccountNumber())) {
                System.err.println("SAME ACCOUNT ERROR");
                System.err.println("Sender: " + transaction.getSenderAccountNumber());
                System.err.println("Receiver: " + transaction.getReceiverAccountNumber());
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Sender and receiver account numbers must be different"))
                    .build();
            }

            Account senderAccount = accountService.getAccountByAccountNumber(transaction.getSenderAccountNumber());
            
            if (senderAccount == null) {
                System.err.println("SENDER ACCOUNT NOT FOUND");
                System.err.println("Account Number: " + transaction.getSenderAccountNumber());
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Sender account not found"))
                    .build();
            }

            if (senderAccount.getStatus() == null || !senderAccount.getStatus().equalsIgnoreCase("ACTIVE")) {
                System.err.println("SENDER ACCOUNT DEACTIVATED");
                System.err.println("Account Number: " + transaction.getSenderAccountNumber());
                System.err.println("Account Status: " + senderAccount.getStatus());
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(ApiResponse.error("Your account is deactivated. Please contact admin or report an issue."))
                        .build();
            }

            Account receiverAccount = accountService.getAccountByAccountNumber(transaction.getReceiverAccountNumber());
            
            if (receiverAccount == null) {
                System.err.println("RECEIVER ACCOUNT NOT FOUND");
                System.err.println("Account Number: " + transaction.getReceiverAccountNumber());
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Receiver account not found"))
                    .build();
            }

            if (receiverAccount.getStatus() == null || !receiverAccount.getStatus().equalsIgnoreCase("ACTIVE")) {
                System.err.println("RECEIVER ACCOUNT DEACTIVATED");
                System.err.println("Account Number: " + transaction.getReceiverAccountNumber());
                System.err.println("Account Status: " + receiverAccount.getStatus());
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(ApiResponse.error("Receiver account is deactivated. Transaction cannot be processed."))
                        .build();
            }

            Customer customer = customerService.getCustomerById(senderAccount.getCustomerId());
            
            if (customer == null) {
                System.err.println("CUSTOMER NOT FOUND");
                System.err.println("Customer ID: " + senderAccount.getCustomerId());
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Customer not found"))
                    .build();
            }

            try {
                System.out.println("=== CHECKING USER ACCOUNT STATUS ===");
                User senderUser = userService.getUserByEmail(customer.getEmail());
                
                if (senderUser != null) {
                    if (!senderUser.isActive()) {
                        System.err.println("USER ACCOUNT DEACTIVATED");
                        System.err.println("Email: " + customer.getEmail());
                        System.err.println("User ID: " + senderUser.getId());
                        return Response.status(Response.Status.FORBIDDEN)
                                .entity(ApiResponse.error("Your account is deactivated. Please contact admin or report an issue."))
                                .build();
                    }
                    System.out.println("User account is active");
                } else {
                    System.out.println("Warning: User record not found for customer email: " + customer.getEmail());
                }
            } catch (Exception userCheckEx) {
                System.err.println("Warning: Could not verify user active status: " + userCheckEx.getMessage());
            }

            String storedPin = customer.getCustomerPin();
            String enteredPin = transaction.getPin();

            System.out.println("PIN Validation:");
            System.out.println("- Customer ID: " + customer.getCustomerId());
            System.out.println("- Customer Name: " + customer.getName());
            System.out.println("- Entered PIN: " + enteredPin);
            System.out.println("- Stored PIN: " + storedPin);

            if (!enteredPin.equals(storedPin)) {
                System.err.println("INVALID PIN");
                System.err.println("Entered: " + enteredPin + ", Expected: " + storedPin);
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Invalid PIN"))
                    .build();
            }

            System.out.println("PIN validation successful");

            ValidationResult validationResult = transactionValidator.validateTransactionForCreation(transaction);

            if (!validationResult.isValid()) {
                System.err.println("TRANSACTION VALIDATION FAILED");
                System.err.println("Errors: " + validationResult.getAllErrorMessages());

                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(validationResult.getFirstErrorMessage()))
                    .build();
            }

            String transactionId = transactionService.createTransaction(transaction);

            if (transactionId != null && transactionId.startsWith("TXN_")) {
                System.out.println("TRANSACTION SUCCESSFUL");
                System.out.println("Transaction ID: " + transactionId);
                return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Transaction created successfully", transactionId))
                    .build();
            } 
            else if ("INSUFFICIENT_BALANCE".equals(transactionId)) {
                System.err.println("INSUFFICIENT BALANCE");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Insufficient balance for this transaction"))
                    .build();
            } 
            else {
                System.err.println("TRANSACTION CREATION FAILED");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to create transaction"))
                    .build();
            }

        } catch (Exception e) {
            System.err.println("EXCEPTION IN TRANSACTION CREATION");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/getTransactionsByAccountNumber/{accountNumber}")
    public Response getTransactionsByAccountNumber(@PathParam("accountNumber") String accountNumber) {
        try {
            System.out.println("\n=== GET TRANSACTIONS REQUEST ===");
            System.out.println("Account Number: " + accountNumber);

            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                System.err.println("VALIDATION FAILED: Account number missing");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number is required"))
                    .build();
            }

            List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);

            if (transactions.isEmpty()) {
                System.out.println("NO TRANSACTIONS FOUND");
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("No transactions found for account number: " + accountNumber))
                    .build();
            }

            System.out.println("TRANSACTIONS RETRIEVED SUCCESSFULLY");
            System.out.println("Total Transactions: " + transactions.size());

            return Response.ok(ApiResponse.success("Transactions retrieved successfully", transactions))
                .build();

        } catch (Exception e) {
            System.err.println("EXCEPTION IN FETCHING TRANSACTIONS");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllTransactions() {
        try {
            System.out.println("\n=== GET ALL TRANSACTIONS REQUEST ===");
            
            List<Transaction> transactions = transactionService.getAllTransactions();
            
            System.out.println("TRANSACTIONS RETRIEVED SUCCESSFULLY");
            System.out.println("Total Transactions: " + transactions.size());
            
            return Response.ok(ApiResponse.success("Transactions retrieved successfully", transactions))
                .build();
                
        } catch (Exception e) {
            System.err.println("EXCEPTION IN FETCHING ALL TRANSACTIONS");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/download/all")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response downloadAllTransactionsExcel() {
        try {
            System.out.println("\n=== DOWNLOAD ALL TRANSACTIONS REQUEST ===");
            
            List<Transaction> transactions = transactionService.getAllTransactions();
            
            if (transactions.isEmpty()) {
                System.out.println("No transactions found");
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("No transactions available to download"))
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            
            System.out.println("Transactions to export: " + transactions.size());
            
            ExcelGeneratorService excelService = new ExcelGeneratorService();
            ByteArrayOutputStream excelStream = excelService.generateTransactionsExcel(transactions);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "all_transactions_" + timestamp + ".xlsx";
            
            System.out.println("Generated file: " + filename);
            System.out.println("File size: " + excelStream.size() + " bytes");
            
            return Response.ok(excelStream.toByteArray())
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .build();
            
        } catch (Exception e) {
            System.err.println("Error generating Excel file: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to generate Excel file: " + e.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @GET
    @Path("/download/{accountNumber}")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response downloadTransactionsByAccount(@PathParam("accountNumber") String accountNumber) {
        System.out.println("\n=== DOWNLOAD TRANSACTIONS BY ACCOUNT REQUEST ===");
        System.out.println("Account Number: " + accountNumber);
        
        try {
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                System.err.println("Invalid account number provided");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Account number is required"))
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            
            List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
            
            if (transactions == null || transactions.isEmpty()) {
                System.err.println("No transactions found for account: " + accountNumber);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("No transactions found for account number: " + accountNumber))
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            
            System.out.println("Found " + transactions.size() + " transactions for account: " + accountNumber);
            
            ExcelGeneratorService excelService = new ExcelGeneratorService();
            ByteArrayOutputStream excelStream = excelService.generateTransactionsExcel(transactions);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "transactions_" + accountNumber + "_" + timestamp + ".xlsx";
            
            System.out.println("Excel file generated successfully: " + filename);
            System.out.println("File size: " + excelStream.size() + " bytes");
            
            return Response.ok(excelStream.toByteArray())
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .build();
                    
        } catch (Exception e) {
            System.err.println("Error generating Excel file for account " + accountNumber);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to generate Excel file: " + e.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @DELETE
    @Path("/{transactionId}")
    public Response deleteTransaction(@PathParam("transactionId") String transactionId) {
        try {
            System.out.println("\n=== DELETE TRANSACTION REQUEST ===");
            System.out.println("Transaction ID: " + transactionId);
            
            if (transactionId == null || transactionId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Transaction ID is required"))
                        .build();
            }
           
            String checkQuery = "SELECT COUNT(*) FROM Transaction WHERE transaction_id = ?";
            try (java.sql.Connection conn = com.bank.simulator.config.DBConfig.getConnection();
                 java.sql.PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                
                checkStmt.setString(1, transactionId);
                java.sql.ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next() && rs.getInt(1) == 0) {
                    System.err.println("Transaction not found");
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity(ApiResponse.error("Transaction not found"))
                            .build();
                }
            }
            
            String deleteQuery = "DELETE FROM Transaction WHERE transaction_id = ?";
            try (java.sql.Connection conn = com.bank.simulator.config.DBConfig.getConnection();
                 java.sql.PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                
                deleteStmt.setString(1, transactionId);
                int result = deleteStmt.executeUpdate();
                
                if (result > 0) {
                    System.out.println("Transaction deleted successfully");
                    return Response.ok()
                            .entity(ApiResponse.success("Transaction deleted successfully"))
                            .build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(ApiResponse.error("Failed to delete transaction"))
                            .build();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to delete transaction. Please try again."))
                    .build();
        }
    }
}
