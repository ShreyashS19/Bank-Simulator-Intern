package com.bank.simulator.model;

public class SignupRequest {
    
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;

    // Constructors
    public SignupRequest() {
    }

    public SignupRequest(String fullName, String email, String password, String confirmPassword) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Override
    public String toString() {
        return "SignupRequest{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
