package com.bank.simulator.controller;

import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.model.Transaction;
import com.bank.simulator.service.TransactionService;  // Use interface
import com.bank.simulator.service.impl.TransactionServiceImpl;
import com.bank.simulator.validation.TransactionValidator;
import com.bank.simulator.validation.ValidationResult;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;

@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {
    
    // Use interface reference
    private final TransactionService transactionService = new TransactionServiceImpl();
    private final TransactionValidator transactionValidator = new TransactionValidator();

    @POST
    @Path("/add")
    public Response createTransaction(Transaction transaction) {
        try {
            System.out.println("=== TRANSACTION CREATION REQUEST ===");
            
            // Use validator
            ValidationResult validationResult = transactionValidator.validateTransactionForCreation(transaction);
            
            if (!validationResult.isValid()) {
                System.out.println("=== TRANSACTION VALIDATION FAILED ===");
                System.out.println("Errors: " + validationResult.getAllErrorMessages());
                System.out.println("Error Code: " + validationResult.getErrorCode());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(validationResult.getFirstErrorMessage()))
                    .build();
            }

            String transactionId = transactionService.createTransaction(transaction);
            if (transactionId != null) {
                return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Transaction created successfully", transactionId))
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
    
    // Add new transfer endpoint
    @POST
    @Path("/transfer")
    public Response createAccountTransfer(
        @QueryParam("fromAccount") String fromAccount,
        @QueryParam("toAccount") String toAccount,
        @QueryParam("amount") BigDecimal amount,
        @QueryParam("mode") String mode,
        @QueryParam("description") String description) {
        
        try {
            String transactionId = transactionService.createAccountTransfer(
                fromAccount, toAccount, amount, mode, description);
            
            if (transactionId != null) {
                return Response.ok(ApiResponse.success("Transfer completed successfully", transactionId)).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Transfer failed - check account balances and IDs"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }
    
    @GET
    @Path("/{transaction_id}")
    public Response getTransaction(@PathParam("transaction_id") String transactionId) {
        try {
            Transaction transaction = transactionService.getTransactionById(transactionId);
            if (transaction != null) {
                return Response.ok(ApiResponse.success("Transaction retrieved successfully", transaction)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Transaction not found"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/account/{account_id}")
    public Response getTransactionsByAccount(@PathParam("account_id") String accountId) {
        try {
            // Use validator to check account existence
            ValidationResult accountValidation = transactionValidator.validateAccountExists(accountId);
            if (!accountValidation.isValid()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(accountValidation.getFirstErrorMessage()))
                    .build();
            }

            List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId);
            return Response.ok(ApiResponse.success("Transactions retrieved successfully", transactions)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    // Add balance checking endpoint
    @GET
    @Path("/balance/{account_id}")
    public Response getAccountBalance(@PathParam("account_id") String accountId) {
        try {
            BigDecimal balance = transactionService.getAccountBalance(accountId);
            return Response.ok(ApiResponse.success("Balance retrieved successfully", balance)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }
}
