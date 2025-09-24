package com.bank.simulator.controller;

import com.bank.simulator.model.Account;
import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.service.AccountService;  // Use interface
import com.bank.simulator.service.impl.AccountServiceImpl;
import com.bank.simulator.validation.AccountValidator;
import com.bank.simulator.validation.ValidationResult;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountController {
    
    // Use interface reference
    private final AccountService accountService = new AccountServiceImpl();
    private final AccountValidator accountValidator = new AccountValidator();

    @POST
    @Path("/add")
    public Response createAccount(Account account) {
        try {
            System.out.println("=== ACCOUNT CREATION REQUEST ===");
            
            // Use validator
            ValidationResult validationResult = accountValidator.validateAccountForCreation(account);
            
            if (!validationResult.isValid()) {
                System.out.println("=== ACCOUNT VALIDATION FAILED ===");
                System.out.println("Errors: " + validationResult.getAllErrorMessages());
                System.out.println("Error Code: " + validationResult.getErrorCode());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(validationResult.getFirstErrorMessage()))
                    .build();
            }
            
            // Set default balance if not provided
            if (account.getBalance() == null) {
                account.setBalance(BigDecimal.valueOf(50.00));
            }
            
            String accountId = accountService.createAccount(account);
            
            if (accountId != null && !accountId.startsWith("CUSTOMER_") && 
                !accountId.startsWith("PHONE_") && !accountId.startsWith("ACCOUNT_")) {
                
                System.out.println("=== ACCOUNT CREATED SUCCESSFULLY ===");
                System.out.println("New Account ID: " + accountId);
                
                return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Account created successfully", accountId))
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to create account"))
                    .build();
            }
            
        } catch (Exception e) {
            System.err.println("=== EXCEPTION IN ACCOUNT CREATION ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    // ... rest of the methods remain the same
    @GET
    @Path("/{account_id}")
    public Response getAccount(@PathParam("account_id") String accountId) {
        try {
            Account account = accountService.getAccountById(accountId);
            if (account != null) {
                return Response.ok(ApiResponse.success("Account retrieved successfully", account)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Account not found"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{account_id}")
    public Response updateAccount(@PathParam("account_id") String accountId, Account account) {
        try {
            System.out.println("=== ACCOUNT UPDATE REQUEST ===");
            
            // Use validator for update
            ValidationResult validationResult = accountValidator.validateAccountForUpdate(accountId, account);
            
            if (!validationResult.isValid()) {
                System.out.println("=== UPDATE VALIDATION FAILED ===");
                System.out.println("Errors: " + validationResult.getAllErrorMessages());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(validationResult.getFirstErrorMessage()))
                    .build();
            }

            boolean updated = accountService.updateAccount(accountId, account);
            if (updated) {
                return Response.ok(ApiResponse.success("Account updated successfully")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Account not found or update failed"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{account_id}")
    public Response deleteAccount(@PathParam("account_id") String accountId) {
        try {
            boolean deleted = accountService.deleteAccount(accountId);
            if (deleted) {
                return Response.ok(ApiResponse.success("Account deleted successfully")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Account not found"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }
}
