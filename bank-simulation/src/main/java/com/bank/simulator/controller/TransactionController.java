package com.bank.simulator.controller;

import com.bank.simulator.model.Account;
import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.model.Customer;
import com.bank.simulator.model.Transaction;
import com.bank.simulator.service.AccountService;
import com.bank.simulator.service.CustomerService;
import com.bank.simulator.service.TransactionService;
import com.bank.simulator.service.impl.AccountServiceImpl;
import com.bank.simulator.service.impl.CustomerServiceImpl;
import com.bank.simulator.service.impl.TransactionServiceImpl;
import com.bank.simulator.validation.TransactionValidator;
import com.bank.simulator.validation.ValidationResult;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    private final TransactionService transactionService = new TransactionServiceImpl();
    private final TransactionValidator transactionValidator = new TransactionValidator();
    private final AccountService accountService = new AccountServiceImpl();
    private final CustomerService customerService = new CustomerServiceImpl();

    @POST
    @Path("/createTransaction")
    public Response createTransaction(Transaction transaction) {
        try {
            System.out.println("\n=== TRANSACTION CREATION REQUEST ===");

            // STEP 1: Auto-fill transaction type if empty
            if (transaction.getTransactionType() == null || transaction.getTransactionType().trim().isEmpty()) {
                transaction.setTransactionType("ONLINE");
                System.out.println("Transaction type not provided, defaulting to: ONLINE");
            }

            // STEP 2: Basic field validation
            if (transaction.getSenderAccountNumber() == null || transaction.getSenderAccountNumber().trim().isEmpty()) {
                System.err.println("=== VALIDATION FAILED: Sender account number missing ===");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Sender account number is required"))
                    .build();
            }

            if (transaction.getReceiverAccountNumber() == null || transaction.getReceiverAccountNumber().trim().isEmpty()) {
                System.err.println("=== VALIDATION FAILED: Receiver account number missing ===");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Receiver account number is required"))
                    .build();
            }

            // STEP 3: Check if sender and receiver are the same (BEFORE PIN validation)
            if (transaction.getSenderAccountNumber().equals(transaction.getReceiverAccountNumber())) {
                System.err.println("=== SAME ACCOUNT ERROR ===");
                System.err.println("Sender: " + transaction.getSenderAccountNumber());
                System.err.println("Receiver: " + transaction.getReceiverAccountNumber());
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Sender and receiver account numbers must be different"))
                    .build();
            }

            // STEP 4: Validate PIN format (must be 6 digits)
            if (transaction.getPin() == null || transaction.getPin().trim().isEmpty()) {
                System.err.println("=== PIN VALIDATION FAILED: PIN not provided ===");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("PIN is required for transaction"))
                    .build();
            }

            if (!transaction.getPin().matches("^[0-9]{6}$")) {
                System.err.println("=== PIN VALIDATION FAILED: Invalid format ===");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("PIN must be exactly 6 digits"))
                    .build();
            }

            // STEP 5: Get sender's account
            Account senderAccount = accountService.getAccountByAccountNumber(transaction.getSenderAccountNumber());
            
            if (senderAccount == null) {
                System.err.println("=== SENDER ACCOUNT NOT FOUND ===");
                System.err.println("Account Number: " + transaction.getSenderAccountNumber());
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Sender account not found"))
                    .build();
            }

            // STEP 6: Get customer details using customer_id from account
            Customer customer = customerService.getCustomerById(senderAccount.getCustomerId());
            
            if (customer == null) {
                System.err.println("=== CUSTOMER NOT FOUND ===");
                System.err.println("Customer ID: " + senderAccount.getCustomerId());
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Customer not found"))
                    .build();
            }

            // STEP 7: Validate PIN - compare entered PIN with stored customerPin
            String storedPin = customer.getCustomerPin();
            String enteredPin = transaction.getPin();

            System.out.println("PIN Validation:");
            System.out.println("- Customer ID: " + customer.getCustomerId());
            System.out.println("- Customer Name: " + customer.getName());
            System.out.println("- Entered PIN: " + enteredPin);
            System.out.println("- Stored PIN: " + storedPin);

            if (!enteredPin.equals(storedPin)) {
                System.err.println("=== INVALID PIN ===");
                System.err.println("Entered: " + enteredPin + ", Expected: " + storedPin);
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Invalid PIN"))
                    .build();
            }

            System.out.println("✓ PIN validation successful");

            // STEP 8: Proceed with regular transaction validation
            ValidationResult validationResult = transactionValidator.validateTransactionForCreation(transaction);

            if (!validationResult.isValid()) {
                System.err.println("=== TRANSACTION VALIDATION FAILED ===");
                System.err.println("Errors: " + validationResult.getAllErrorMessages());

                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(validationResult.getFirstErrorMessage()))
                    .build();
            }

            // STEP 9: Create transaction
            String transactionId = transactionService.createTransaction(transaction);

            if (transactionId != null && transactionId.startsWith("TXN_")) {
                System.out.println("=== TRANSACTION SUCCESSFUL ===");
                System.out.println("Transaction ID: " + transactionId);
                return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Transaction created successfully", transactionId))
                    .build();
            } else if ("INSUFFICIENT_BALANCE".equals(transactionId)) {
                System.err.println("=== INSUFFICIENT BALANCE ===");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Insufficient balance for this transaction"))
                    .build();
            } else {
                System.err.println("=== TRANSACTION CREATION FAILED ===");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to create transaction"))
                    .build();
            }

        } catch (Exception e) {
            System.err.println("=== EXCEPTION IN TRANSACTION CREATION ===");
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
                System.err.println("=== VALIDATION FAILED: Account number missing ===");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number is required"))
                    .build();
            }

            List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);

            if (transactions.isEmpty()) {
                System.out.println("=== NO TRANSACTIONS FOUND ===");
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("No transactions found for account number: " + accountNumber))
                    .build();
            }

            System.out.println("=== TRANSACTIONS RETRIEVED SUCCESSFULLY ===");
            System.out.println("Total Transactions: " + transactions.size());

            return Response.ok(ApiResponse.success("Transactions retrieved successfully", transactions))
                .build();

        } catch (Exception e) {
            System.err.println("=== EXCEPTION IN FETCHING TRANSACTIONS ===");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }
}
