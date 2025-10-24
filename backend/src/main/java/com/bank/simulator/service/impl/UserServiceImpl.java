package com.bank.simulator.service.impl;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.User;
import com.bank.simulator.service.UserService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class UserServiceImpl implements UserService {
    
    private static final AtomicInteger userCounter;
    
    static {
        userCounter = new AtomicInteger(getMaxUserIdFromDB() + 1);
        System.out.println("=== USER SERVICE INITIALIZED ===");
        System.out.println("Starting user counter at: " + userCounter.get());
    }
    
    private static int getMaxUserIdFromDB() {
        String query = "SELECT MAX(CAST(SUBSTRING(id, 6) AS UNSIGNED)) as max_id FROM User";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                System.out.println(" Loaded max user ID from database: " + maxId);
                return maxId;
            }
        } catch (SQLException e) {
            System.err.println("Warning: Could not load max user ID from database");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Starting counter from 0 (first ID will be USER_1)");
        }
        
        return 0;
    }

   @Override
public String createUser(User user) {
    System.out.println("\n=== USER CREATION STARTED ===");
    System.out.println("Full Name: " + user.getFullName());
    System.out.println("Email: " + user.getEmail());
    
    String userId = generateUserId();
    user.setId(userId);
    user.setActive(true); 
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    
    String query = """
        INSERT INTO User (id, full_name, email, password, active, created_at, updated_at) 
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;
    
    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, user.getId());
        stmt.setString(2, user.getFullName());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getPassword());
        stmt.setBoolean(5, user.isActive());
        stmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
        stmt.setTimestamp(7, Timestamp.valueOf(user.getUpdatedAt()));
        
        int result = stmt.executeUpdate();
        
        if (result > 0) {
            System.out.println(" User created successfully");
            System.out.println("User ID: " + userId);
            return userId;
        } else {
            System.err.println(" User creation failed - no rows affected");
            return null;
        }
        
    } catch (SQLException e) {
        System.err.println("Error creating user: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}

    @Override
public User getUserByEmail(String email) {
    System.out.println("\n=== FETCHING USER BY EMAIL ===");
    System.out.println("Email: " + email);
    
    String query = "SELECT * FROM User WHERE email = ?";
    
    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            User user = new User();
            user.setId(rs.getString("id"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setActive(rs.getBoolean("active"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            
            System.out.println(" User found: " + user.getFullName());
            return user;
        } else {
            System.out.println(" User not found");
            return null;
        }
        
    } catch (SQLException e) {
        System.err.println("Error fetching user by email: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}

    @Override
    public boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM User WHERE email = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                if (exists) {
                    System.out.println(" Email already exists: " + email);
                } else {
                    System.out.println(" Email is unique: " + email);
                }
                return exists;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        
        return false;
    }

   @Override
public User validateLogin(String email, String password) {
    System.out.println("\n=== VALIDATING LOGIN ===");
    System.out.println("Email: " + email);
    
    String query = "SELECT * FROM User WHERE email = ? AND password = ?";
    
    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, email);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            User user = new User();
            user.setId(rs.getString("id"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(null); 
            user.setActive(rs.getBoolean("active"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            
          
            if (!user.isActive()) {
                System.out.println(" User account is deactivated");
                return null; 
            }
            
            System.out.println(" Login successful for: " + user.getFullName());
            return user;
        } else {
            System.out.println(" Invalid credentials");
            return null;
        }
        
    } catch (SQLException e) {
        System.err.println("Error validating login: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}

    @Override
public boolean updateUserStatus(String email, boolean active) {
    System.out.println("\n=== UPDATING USER STATUS ===");
    System.out.println("Email: " + email);
    System.out.println("New Status: " + (active ? "Active" : "Inactive"));
    
    String query = "UPDATE User SET active = ?, updated_at = ? WHERE email = ?";
    
    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setBoolean(1, active);
        stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        stmt.setString(3, email);
        
        int result = stmt.executeUpdate();
        
        if (result > 0) {
            System.out.println(" User status updated successfully");
            return true;
        } else {
            System.err.println(" User not found");
            return false;
        }
        
    } catch (SQLException e) {
        System.err.println("Error updating user status: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

@Override
public User getUserById(String userId) {
    System.out.println("\n=== FETCHING USER BY ID ===");
    System.out.println("User ID: " + userId);
    
    String query = "SELECT * FROM User WHERE id = ?";
    
    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            User user = new User();
            user.setId(rs.getString("id"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setActive(rs.getBoolean("active"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            
            System.out.println(" User found: " + user.getFullName());
            return user;
        } else {
            System.out.println(" User not found");
            return null;
        }
        
    } catch (SQLException e) {
        System.err.println("Error fetching user by ID: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}
    @Override
    public String generateUserId() {
        return "USER_" + userCounter.getAndIncrement();
    }
}
