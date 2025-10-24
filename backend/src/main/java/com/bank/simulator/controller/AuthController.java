package com.bank.simulator.controller;

import com.bank.simulator.model.ApiResponse;
import com.bank.simulator.model.LoginRequest;
import com.bank.simulator.model.SignupRequest;
import com.bank.simulator.model.User;
import com.bank.simulator.service.CustomerService;
import com.bank.simulator.service.UserService;
import com.bank.simulator.service.impl.CustomerServiceImpl;
import com.bank.simulator.service.impl.UserServiceImpl;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    private final UserService userService = new UserServiceImpl();
    private final CustomerService customerService = new CustomerServiceImpl();

    @POST
    @Path("/signup")
    public Response signup(SignupRequest request) {
        try {
            System.out.println("\n=== SIGNUP REQUEST ===");
            System.out.println("Full Name: " + request.getFullName());
            System.out.println("Email: " + request.getEmail());

            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Full name is required"))
                        .build();
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Email is required"))
                        .build();
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Password is required"))
                        .build();
            }

            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Passwords do not match"))
                        .build();
            }

            if (userService.isEmailExists(request.getEmail())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(ApiResponse.error("Email already registered"))
                        .build();
            }

            User user = new User();
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setActive(true);

            String userId = userService.createUser(user);

            if (userId != null) {
                user.setId(userId);
                user.setPassword(null);
                
                System.out.println(" Signup successful");
                return Response.status(Response.Status.CREATED)
                        .entity(ApiResponse.success("User registered successfully", user))
                        .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ApiResponse.error("Failed to create user"))
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
    public Response login(LoginRequest request) {
        try {
            System.out.println("\n=== LOGIN REQUEST ===");
            System.out.println("Email: " + request.getEmail());

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Email is required"))
                        .build();
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Password is required"))
                        .build();
            }

            User user = userService.validateLogin(request.getEmail(), request.getPassword());

            if (user != null) {
                System.out.println(" Login successful");
                return Response.ok()
                        .entity(ApiResponse.success("Login successful", user))
                        .build();
            } else {
                System.err.println(" Invalid credentials");
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

    @GET
    @Path("/check-customer")
    public Response checkCustomerExists(@QueryParam("email") String email) {
        try {
            System.out.println("\n=== CHECK CUSTOMER EXISTS REQUEST ===");
            System.out.println("Email: " + email);

            if (email == null || email.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Email is required"))
                        .build();
            }

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("User not found"))
                        .build();
            }

            boolean hasCustomerRecord = customerService.isCustomerExistsByEmail(email);

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("hasCustomerRecord", hasCustomerRecord);
            data.put("userId", user.getId());
            data.put("email", user.getEmail());

            System.out.println(" Customer record exists: " + hasCustomerRecord);

            return Response.ok()
                    .entity(ApiResponse.success("Customer check completed", data))
                    .build();

        } catch (Exception e) {
            System.err.println("=== EXCEPTION IN CHECK CUSTOMER ===");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/users/all")
    public Response getAllUsers() {
        try {
            System.out.println("\n=== GET ALL USERS REQUEST ===");
            
            String query = "SELECT id, full_name, email, active, created_at, updated_at FROM User ORDER BY created_at DESC";
            
            java.util.List<User> users = new java.util.ArrayList<>();
            
            try (java.sql.Connection conn = com.bank.simulator.config.DBConfig.getConnection();
                 java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(query)) {
                
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setActive(rs.getBoolean("active"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    users.add(user);
                }
            }
            
            System.out.println(" Retrieved " + users.size() + " users");
            return Response.ok()
                    .entity(ApiResponse.success("Users retrieved successfully", users))
                    .build();
            
        } catch (Exception e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/user/status")
    public Response updateUserStatus(@QueryParam("email") String email, @QueryParam("active") boolean active) {
        try {
            System.out.println("\n=== UPDATE USER STATUS REQUEST ===");
            System.out.println("Email: " + email);
            System.out.println("Active: " + active);
            
            if (email == null || email.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Email is required"))
                        .build();
            }
            
            boolean updated = userService.updateUserStatus(email, active);
            
            if (updated) {
                String message = active ? "Account activated successfully" : "Account deactivated successfully";
                System.out.println(" " + message);
                return Response.ok()
                        .entity(ApiResponse.success(message))
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("User not found"))
                        .build();
            }
            
        } catch (Exception e) {
            System.err.println("=== EXCEPTION IN UPDATE USER STATUS ===");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Internal server error: " + e.getMessage()))
                    .build();
        }
    }
}
