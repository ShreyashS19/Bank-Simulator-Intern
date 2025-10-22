package com.bank.simulator.service;

import com.bank.simulator.model.User;

public interface UserService { 
    String createUser(User user); 
    User getUserByEmail(String email);
    boolean isEmailExists(String email);
    User validateLogin(String email, String password);
    String generateUserId();
}
