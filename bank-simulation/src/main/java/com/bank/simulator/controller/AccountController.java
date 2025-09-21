package com.bank.simulator.controller;

import com.bank.simulator.model.Account;
import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.service.impl.AccountServiceImpl;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountController {
    
    private final AccountServiceImpl accountService = new AccountServiceImpl();

    @POST
    @Path("/add")
    public Response createAccount(Account account) {
        try {
            // Validation
            if (!accountService.isCustomerExists(account.getCustomerId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Customer does not exist"))
                    .build();
            }

            if (accountService.isAccountNumberExists(account.getAccountNumber())) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number already exists"))
                    .build();
            }

            if (account.getBalance() == null) {
                account.setBalance(BigDecimal.valueOf(50.00));
            }

            String accountId = accountService.createAccount(account);
            if (accountId != null) {
                return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Account created successfully", accountId))
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to create account"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

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
            if (!accountService.isCustomerExists(account.getCustomerId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Customer does not exist"))
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
