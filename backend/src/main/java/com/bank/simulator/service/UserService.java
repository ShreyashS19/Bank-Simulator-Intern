package com.bank.simulator.service;

import com.bank.simulator.model.User;

public interface UserService {
    
    /**
     * Create a new user
     * @param user User object
     * @return User ID if successful, null otherwise
     */
    String createUser(User user);
    
    /**
     * Get user by email
     * @param email User email
     * @return User object if found, null otherwise
     */
    User getUserByEmail(String email);
    
    /**
     * Check if email already exists
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    boolean isEmailExists(String email);
    
    /**
     * Validate login credentials
     * @param email User email
     * @param password User password
     * @return User object if credentials are valid, null otherwise
     */
    User validateLogin(String email, String password);
    
    /**
     * Generate unique user ID
     * @return Generated user ID
     */
    String generateUserId();
}
