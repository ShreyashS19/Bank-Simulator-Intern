package com.bank.simulator.controller;

import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.model.LoginRequest;
import com.bank.simulator.model.SignupRequest;
import com.bank.simulator.model.User;
import com.bank.simulator.service.UserService;
import com.bank.simulator.service.impl.UserServiceImpl;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {
    
    private final UserService userService = new UserServiceImpl();

    @POST
    @Path("/signup")
    public Response signup(SignupRequest signupRequest) {
        try {
            System.out.println("\n=== SIGNUP REQUEST ===");
            System.out.println("Full Name: " + signupRequest.getFullName());
            System.out.println("Email: " + signupRequest.getEmail());
            
            if (signupRequest.getFullName() == null || signupRequest.getFullName().trim().isEmpty()) {
                System.err.println(" Validation failed: Full name is required");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Full name is required"))
                        .build();
            }
            
            if (signupRequest.getEmail() == null || signupRequest.getEmail().trim().isEmpty()) {
                System.err.println(" Validation failed: Email is required");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Email is required"))
                        .build();
            }
            
            if (signupRequest.getPassword() == null || signupRequest.getPassword().trim().isEmpty()) {
                System.err.println(" Validation failed: Password is required");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Password is required"))
                        .build();
            }
            
            if (signupRequest.getConfirmPassword() == null || signupRequest.getConfirmPassword().trim().isEmpty()) {
                System.err.println(" Validation failed: Confirm password is required");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Confirm password is required"))
                        .build();
            }
            
            if (!signupRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.err.println(" Validation failed: Invalid email format");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid email format"))
                        .build();
            }

            if (signupRequest.getPassword().length() < 6) {
                System.err.println(" Validation failed: Password too short");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Password must be at least 6 characters long"))
                        .build();
            }
     
            if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
                System.err.println(" Validation failed: Passwords do not match");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Password and Confirm Password do not match"))
                        .build();
            }
        
            if (userService.isEmailExists(signupRequest.getEmail())) {
                System.err.println(" Email already exists: " + signupRequest.getEmail());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Email already exists"))
                        .build();
            }
            
            User user = new User();
            user.setFullName(signupRequest.getFullName());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(signupRequest.getPassword());
            
            String userId = userService.createUser(user);
            
            if (userId != null) {
                System.out.println(" User registered successfully");
                System.out.println("User ID: " + userId);
                
                user.setPassword(null);
                user.setId(userId);
                
                return Response.status(Response.Status.CREATED)
                        .entity(ApiResponse.success("User registered successfully", user))
                        .build();
            } else {
                System.err.println("User registration failed");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ApiResponse.error("Failed to register user"))
                        .build();
            }
            
        } catch (Exception e) {
            System.err.println("=== EXCEPTION IN SIGNUP ===");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                    .build();
        }
    }


    
    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        try {
            System.out.println("\n=== LOGIN REQUEST ===");
            System.out.println("Email: " + loginRequest.getEmail());
            
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                System.err.println(" Validation failed: Email is required");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Email is required"))
                        .build();
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                System.err.println(" Validation failed: Password is required");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Password is required"))
                        .build();
            }

            User user = userService.validateLogin(loginRequest.getEmail(), loginRequest.getPassword());
            
            if (user != null) {
                System.out.println(" Login successful");
                System.out.println("User: " + user.getFullName());
                
                return Response.ok()
                        .entity(ApiResponse.success("Login successful", user))
                        .build();
            } else {
                System.err.println(" Invalid email or password");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ApiResponse.error("Invalid email or password"))
                        .build();
            }
            
        } catch (Exception e) {
            System.err.println("=== EXCEPTION IN LOGIN ===");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                    .build();
        }
    }
}
