package com.bank.simulator.controller;

import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.model.Customer;
import com.bank.simulator.service.CustomerService; 
import com.bank.simulator.service.impl.CustomerServiceImpl;
import com.bank.simulator.validation.CustomerValidator;
import com.bank.simulator.validation.ValidationResult;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerController {
    
    private final CustomerService customerService = new CustomerServiceImpl();
    private final CustomerValidator customerValidator = new CustomerValidator();

    @POST
    @Path("/onboard")
    public Response createCustomer(Customer customer) {
        try {
            System.out.println("=== CUSTOMER CREATION REQUEST ===");
            
            ValidationResult validationResult = customerValidator.validateCustomerForCreation(customer);
            
            if (!validationResult.isValid()) {
                System.out.println("=== VALIDATION FAILED ===");
                System.out.println("Errors: " + validationResult.getAllErrorMessages());
                
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(validationResult.getFirstErrorMessage()))
                    .build();
            }

            if (customer.getStatus() == null || customer.getStatus().trim().isEmpty()) {
                customer.setStatus("Inactive");
            }

            String customerId = customerService.createCustomer(customer);
            if (customerId != null) {
                return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Customer created successfully", customerId))
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to create customer"))
                    .build();
            }
        } catch (Exception e) {
            System.err.println("Exception in customer creation: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{customer_id}")
    public Response getCustomer(@PathParam("customer_id") String customerId) {
        try {
            Customer customer = customerService.getCustomerById(customerId);
            if (customer != null) {
                return Response.ok(ApiResponse.success("Customer retrieved successfully", customer)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Customer not found"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    @PUT
@Path("/{customer_id}")
public Response updateCustomer(@PathParam("customer_id") String customerId, Customer customer) {
    try {
        System.out.println("=== CUSTOMER UPDATE REQUEST ===");
        
        ValidationResult validationResult = customerValidator.validateCustomerForUpdate(customerId, customer);
        
        if (!validationResult.isValid()) {
            System.err.println("=== UPDATE VALIDATION FAILED ===");
            System.err.println("Errors: " + validationResult.getAllErrorMessages());
            
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error(validationResult.getFirstErrorMessage()))
                .build();
        }

        boolean updated = customerService.updateCustomer(customerId, customer);
        if (updated) {
            return Response.ok(ApiResponse.success("Customer updated successfully")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(ApiResponse.error("Customer not found or update failed"))
                .build();
        }
    } catch (Exception e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
            .build();
    }
}

    @DELETE
    @Path("/{customer_id}")
    public Response deleteCustomer(@PathParam("customer_id") String customerId) {
        try {
            boolean deleted = customerService.deleteCustomer(customerId);
            if (deleted) {
                return Response.ok(ApiResponse.success("Customer deleted successfully")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Customer not found"))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            return Response.ok(ApiResponse.success("Customers retrieved successfully", customers)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "CustomerController is working with validation!";
    }
}
