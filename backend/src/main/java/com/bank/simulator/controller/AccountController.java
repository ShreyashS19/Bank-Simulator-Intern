package com.bank.simulator.controller;

import com.bank.simulator.model.Account;
import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.service.AccountService;
import com.bank.simulator.service.impl.AccountServiceImpl;
import com.bank.simulator.validation.AccountValidator;
import com.bank.simulator.validation.ValidationResult;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

import java.math.BigDecimal;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountController {
    
    private final AccountService accountService = new AccountServiceImpl();
    private final AccountValidator accountValidator = new AccountValidator();

    @POST
    @Path("/add")
    public Response createAccount(Account account) {
        try {
            System.out.println("=== ACCOUNT CREATION REQUEST ===");
            
            ValidationResult validationResult = accountValidator.validateAccountForCreation(account);
            
            if (!validationResult.isValid()) {
                System.err.println("=== ACCOUNT VALIDATION FAILED ===");
                System.err.println("Error: " + validationResult.getFirstErrorMessage());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(validationResult.getFirstErrorMessage()))
                    .build();
            }
            
            if (account.getAmount() == null) {
                account.setAmount(BigDecimal.valueOf(0.00));
            }
            
            if (account.getStatus() == null || account.getStatus().trim().isEmpty()) {
                account.setStatus("ACTIVE");
            }
            
            String result = accountService.createAccount(account);
            
            if (result != null && result.startsWith("ACC_")) {
                return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Account created successfully", result))
                    .build();
            } else if (result != null && result.startsWith("ERROR:")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(result.substring(7)))
                    .build();
            } else if ("ACCOUNT_NUMBER_EXISTS".equals(result)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number already exists"))
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to create account"))
                    .build();
            }
            
        } catch (Exception e) {
            System.err.println("Exception in account creation: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    // @GET
    // @Path("/{account_id}")
    // public Response getAccount(@PathParam("account_id") String accountId) {
    //     try {
    //         Account account = accountService.getAccountById(accountId);
    //         if (account != null) {
    //             // Remove sensitive internal IDs
    //             account.setAccountId(null);
    //             account.setCustomerId(null);
                
    //             return Response.ok(ApiResponse.success("Account retrieved successfully", account)).build();
    //         } else {
    //             return Response.status(Response.Status.NOT_FOUND)
    //                 .entity(ApiResponse.error("Account not found"))
    //                 .build();
    //         }
    //     } catch (Exception e) {
    //         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    //             .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
    //             .build();
    //     }
    // }

    // @GET
    // @Path("/customer/{customer_id}")
    // public Response getAccountByCustomerId(@PathParam("customer_id") String customerId) {
    //     try {
    //         Account account = accountService.getAccountByCustomerId(customerId);
    //         if (account != null) {
    //             // Remove sensitive internal IDs
    //             account.setAccountId(null);
    //             account.setCustomerId(null);
                
    //             return Response.ok(ApiResponse.success("Account retrieved successfully", account)).build();
    //         } else {
    //             return Response.status(Response.Status.NOT_FOUND)
    //                 .entity(ApiResponse.error("Account not found for customer"))
    //                 .build();
    //         }
    //     } catch (Exception e) {
    //         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    //             .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
    //             .build();
    //     }
    // }

    @GET
    @Path("/number/{account_number}")
    public Response getAccountByAccountNumber(@PathParam("account_number") String accountNumber) {
        try {
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number is required"))
                    .build();
            }

            if (!accountNumber.matches("^[0-9]{10,25}$")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number must be 10-25 digits"))
                    .build();
            }

            Account account = accountService.getAccountByAccountNumber(accountNumber);
            if (account != null) {
                account.setAccountId(null);
                account.setCustomerId(null);
                
                return Response.ok(ApiResponse.success("Account retrieved successfully", account)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Account not found"))
                    .build();
            }
        } catch (Exception e) {
            System.err.println("Exception in fetching account by number: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    // @PUT
    // @Path("/{account_id}")
    // public Response updateAccount(@PathParam("account_id") String accountId, Account account) {
    //     try {
    //         System.out.println("=== ACCOUNT UPDATE REQUEST ===");
            
    //         ValidationResult validationResult = accountValidator.validateAccountForUpdate(accountId, account);
            
    //         if (!validationResult.isValid()) {
    //             System.err.println("Account update validation failed: " + validationResult.getFirstErrorMessage());
                
    //             return Response.status(Response.Status.BAD_REQUEST)
    //                 .entity(ApiResponse.error(validationResult.getFirstErrorMessage()))
    //                 .build();
    //         }

    //         boolean updated = accountService.updateAccount(accountId, account);
    //         if (updated) {
    //             return Response.ok(ApiResponse.success("Account updated successfully")).build();
    //         } else {
    //             return Response.status(Response.Status.NOT_FOUND)
    //                 .entity(ApiResponse.error("Account not found or update failed"))
    //                 .build();
    //         }
    //     } catch (Exception e) {
    //         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    //             .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
    //             .build();
    //     }
    // }

    @PUT
    @Path("/number/{account_number}")
    public Response updateByAccountNumber(@PathParam("account_number") String accountNumber, Account account) {
        try {
            System.out.println("=== ACCOUNT UPDATE BY NUMBER REQUEST ===");
            
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number is required"))
                    .build();
            }

            if (!accountNumber.matches("^[0-9]{10,25}$")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number must be 10-25 digits"))
                    .build();
            }

            Account existingAccount = accountService.getAccountByAccountNumber(accountNumber);
            if (existingAccount == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Account not found"))
                    .build();
            }
            
            String accountId = existingAccount.getAccountId();
            ValidationResult validationResult = accountValidator.validateAccountForUpdate(accountId, account);
            
            if (!validationResult.isValid()) {
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
            System.err.println("Exception in update by account number: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    // @DELETE
    // @Path("/{account_id}")
    // public Response deleteAccount(@PathParam("account_id") String accountId) {
    //     try {
    //         System.out.println("=== ACCOUNT DELETION REQUEST ===");
    //         System.out.println("Account ID: " + accountId);
            
    //         boolean deleted = accountService.deleteAccount(accountId);
    //         if (deleted) {
    //             return Response.ok(ApiResponse.success("Account deleted permanently")).build();
    //         } else {
    //             return Response.status(Response.Status.NOT_FOUND)
    //                 .entity(ApiResponse.error("Account not found or deletion failed"))
    //                 .build();
    //         }
    //     } catch (Exception e) {
    //         System.err.println("Exception during account deletion: " + e.getMessage());
    //         e.printStackTrace();
    //         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    //             .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
    //             .build();
    //     }
    // }
    

    @GET
    @Path("/all")
    public Response getAllAccounts() {
        try {
            System.out.println(" GET ALL ACCOUNTS REQUEST");
            
            List<Account> accounts = accountService.getAllAccounts();
            
            for (Account account : accounts) {
                account.setAccountId(null);
                account.setCustomerId(null);
            }
            
            System.out.println(" Returning " + accounts.size() + " accounts");
            
            return Response.ok(ApiResponse.success("Accounts retrieved successfully", accounts))
                    .build();
            
        } catch (Exception e) {
            System.err.println(" Error in getAllAccounts: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                    .build();
        }
    }

    
    @DELETE
    @Path("/number/{account_number}")
    public Response deleteByNumber(@PathParam("account_number") String accountNumber) {
        try {
            System.out.println("=== ACCOUNT DELETION BY NUMBER REQUEST ===");
            System.out.println("Account Number: " + accountNumber);
            
            if (accountNumber == null || accountNumber.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number is required"))
                    .build();
            }

            if (!accountNumber.matches("^[0-9]{10,25}$")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Account number must be 10-25 digits"))
                    .build();
            }

            Account account = accountService.getAccountByAccountNumber(accountNumber);
            if (account == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Account not found"))
                    .build();
            }

            boolean deleted = accountService.deleteAccount(account.getAccountId());
            if (deleted) {
                return Response.ok(ApiResponse.success("Account deleted permanently")).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Deletion failed"))
                    .build();
            }

        } catch (Exception e) {
            System.err.println("Exception during account deletion by number: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal error: " + e.getMessage()))
                .build();
        }
    }
}
