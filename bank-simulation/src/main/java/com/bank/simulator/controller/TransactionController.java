package com.bank.simulator.controller;

import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.model.Transaction;
import com.bank.simulator.service.impl.TransactionServiceImpl;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {
    
    private final TransactionServiceImpl transactionService = new TransactionServiceImpl();

    @POST
    @Path("/add")
    public Response createTransaction(Transaction transaction) {
        try {
            // Validation
            if (!transactionService.isAccountExists(transaction.getAccountId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account does not exist"))
                    .build();
            }

            if (transaction.getTransactionAmount() == null || transaction.getTransactionAmount().signum() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Transaction amount must be greater than zero"))
                    .build();
            }

            if (!transaction.getTransactionType().equals("debited") && !transaction.getTransactionType().equals("credited")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Transaction type must be 'debited' or 'credited'"))
                    .build();
            }

            String transactionId = transactionService.createTransaction(transaction);
        if (transactionId != null) {
            return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Transaction created successfully", transactionId))
                .build();
        } else {
            // Check if it was due to insufficient balance
            if ("debited".equals(transaction.getTransactionType())) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Transaction failed: Insufficient balance"))
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to create transaction"))
                    .build();
            }
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
            if (!transactionService.isAccountExists(accountId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account does not exist"))
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
}
