package com.bank.simulator.controller;

import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.model.Transaction;
import com.bank.simulator.service.TransactionService;
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

    @POST
    @Path("/createTransaction")
    public Response createTransaction(Transaction transaction) {
        try {
            System.out.println("=== TRANSACTION CREATION REQUEST ===");

            ValidationResult validationResult = transactionValidator.validateTransactionForCreation(transaction);

            if (!validationResult.isValid()) {
                System.err.println("=== TRANSACTION VALIDATION FAILED ===");
                System.err.println("Errors: " + validationResult.getAllErrorMessages());

                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(validationResult.getFirstErrorMessage()))
                    .build();
            }

            String transactionId = transactionService.createTransaction(transaction);

            if (transactionId != null && transactionId.startsWith("TXN_")) {
                return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Transaction created successfully", transactionId))
                    .build();
            } else if ("INSUFFICIENT_BALANCE".equals(transactionId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Insufficient balance for this transaction"))
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to create transaction"))
                    .build();
            }

        } catch (Exception e) {
            System.err.println("Exception in transaction creation: " + e.getMessage());
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
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number is required"))
                    .build();
            }

            List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);

            if (transactions.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("No transactions found for account number: " + accountNumber))
                    .build();
            }

            return Response.ok(ApiResponse.success("Transactions retrieved successfully", transactions))
                .build();

        } catch (Exception e) {
            System.err.println("Exception in fetching transactions: " + e.getMessage());
            e.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }
}
