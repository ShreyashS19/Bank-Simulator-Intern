package com.bank.simulator.validation;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class CustomerValidator {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[1-9][0-9]{9}$");
    
    private static final Pattern AADHAR_PATTERN = 
        Pattern.compile("^[0-9]{12}$");

    public ValidationResult validateCustomerForCreation(Customer customer) {
        System.out.println("=== CUSTOMER CREATION VALIDATION STARTED ===");
        System.out.println("Customer Name: " + customer.getName());
        System.out.println("Phone Number: " + customer.getPhoneNumber());
        System.out.println("Email: " + customer.getEmail());
        
        ValidationResult result = new ValidationResult();
        
        ValidationResult nameValidation = validateName(customer.getName());
        if (!nameValidation.isValid()) {
            result.addError(nameValidation.getFirstErrorMessage());
        }
        
        ValidationResult phoneValidation = validatePhoneNumberFormat(customer.getPhoneNumber());
        if (!phoneValidation.isValid()) {
            result.addError(phoneValidation.getFirstErrorMessage());
        }
        
        ValidationResult emailValidation = validateEmail(customer.getEmail());
        if (!emailValidation.isValid()) {
            result.addError(emailValidation.getFirstErrorMessage());
        }
        
        ValidationResult addressValidation = validateAddress(customer.getAddress());
        if (!addressValidation.isValid()) {
            result.addError(addressValidation.getFirstErrorMessage());
        }
        
        ValidationResult pinValidation = validateCustomerPin(customer.getCustomerPin());
        if (!pinValidation.isValid()) {
            result.addError(pinValidation.getFirstErrorMessage());
        }
        
        ValidationResult aadharValidation = validateAadharNumberFormat(customer.getAadharNumber());
        if (!aadharValidation.isValid()) {
            result.addError(aadharValidation.getFirstErrorMessage());
        }
        
        ValidationResult dobValidation = validateDateOfBirth(customer.getDob());
        if (!dobValidation.isValid()) {
            result.addError(dobValidation.getFirstErrorMessage());
        }
        
        if (result.isValid()) {
            System.out.println("=== BASIC VALIDATIONS PASSED - CHECKING UNIQUENESS ===");
            
            ValidationResult phoneUniqueValidation = validatePhoneNumberUniqueness(customer.getPhoneNumber());
            if (!phoneUniqueValidation.isValid()) {
                result.addError(phoneUniqueValidation.getFirstErrorMessage(), "PHONE_EXISTS");
            }
            
            // ValidationResult emailUniqueValidation = validateEmailUniqueness(customer.getEmail());
            // if (!emailUniqueValidation.isValid()) {
            //     result.addError(emailUniqueValidation.getFirstErrorMessage(), "EMAIL_EXISTS");
            // }
            
            ValidationResult aadharUniqueValidation = validateAadharNumberUniqueness(customer.getAadharNumber());
            if (!aadharUniqueValidation.isValid()) {
                result.addError(aadharUniqueValidation.getFirstErrorMessage(), "AADHAR_EXISTS");
            }
        }
        
        System.out.println("=== CUSTOMER VALIDATION RESULT ===");
        System.out.println("Valid: " + result.isValid());
        if (!result.isValid()) {
            System.out.println("Errors: " + result.getAllErrorMessages());
        }
        
        return result;
    }

    public ValidationResult validateCustomerForUpdate(String customerId, Customer customer) {
        System.out.println("=== CUSTOMER UPDATE VALIDATION STARTED ===");
        System.out.println("Customer ID: " + customerId);
        System.out.println("Customer Name: " + customer.getName());
        System.out.println("Phone Number: " + customer.getPhoneNumber());
        System.out.println("Email: " + customer.getEmail());
        
        ValidationResult result = new ValidationResult();
        
        ValidationResult nameValidation = validateName(customer.getName());
        if (!nameValidation.isValid()) {
            result.addError(nameValidation.getFirstErrorMessage());
        }
        
        ValidationResult phoneValidation = validatePhoneNumberFormat(customer.getPhoneNumber());
        if (!phoneValidation.isValid()) {
            result.addError(phoneValidation.getFirstErrorMessage());
        }
        
        ValidationResult emailValidation = validateEmail(customer.getEmail());
        if (!emailValidation.isValid()) {
            result.addError(emailValidation.getFirstErrorMessage());
        }
        
        ValidationResult addressValidation = validateAddress(customer.getAddress());
        if (!addressValidation.isValid()) {
            result.addError(addressValidation.getFirstErrorMessage());
        }
        
        ValidationResult customerPinValidation = validateCustomerPin(customer.getCustomerPin());
        if (!customerPinValidation.isValid()) {
            result.addError(customerPinValidation.getFirstErrorMessage());
        }
        
        ValidationResult aadharValidation = validateAadharNumberFormat(customer.getAadharNumber());
        if (!aadharValidation.isValid()) {
            result.addError(aadharValidation.getFirstErrorMessage());
        }
        
        ValidationResult dobValidation = validateDateOfBirth(customer.getDob());
        if (!dobValidation.isValid()) {
            result.addError(dobValidation.getFirstErrorMessage());
        }
        
        if (result.isValid()) {
            System.out.println("=== BASIC VALIDATIONS PASSED - CHECKING UNIQUENESS (EXCLUDING CURRENT CUSTOMER) ===");
            
            ValidationResult phoneUniqueValidation = validatePhoneNumberUniqueForUpdate(customerId, customer.getPhoneNumber());
            if (!phoneUniqueValidation.isValid()) {
                result.addError(phoneUniqueValidation.getFirstErrorMessage());
            }
            
            ValidationResult emailUniqueValidation = validateEmailUniqueForUpdate(customerId, customer.getEmail());
            if (!emailUniqueValidation.isValid()) {
                result.addError(emailUniqueValidation.getFirstErrorMessage());
            }
            
            ValidationResult aadharUniqueValidation = validateAadharNumberUniqueForUpdate(customerId, customer.getAadharNumber());
            if (!aadharUniqueValidation.isValid()) {
                result.addError(aadharUniqueValidation.getFirstErrorMessage());
            }
        }
        
        System.out.println("=== CUSTOMER UPDATE VALIDATION RESULT ===");
        System.out.println("Valid: " + result.isValid());
        if (!result.isValid()) {
            System.out.println("Errors: " + result.getAllErrorMessages());
        }
        
        return result;
    }

    public ValidationResult validatePhoneNumberUniqueForUpdate(String customerId, String phoneNumber) {
        String query = "SELECT COUNT(*) FROM Customer WHERE phone_number = ? AND customer_id != ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, phoneNumber);
            stmt.setString(2, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("=== PHONE NUMBER ALREADY EXISTS (OTHER CUSTOMER) ===");
                System.out.println("Phone: " + phoneNumber);
                return ValidationResult.failure("Phone number already exists");
            }
            
            System.out.println("✓ Phone number is unique for update: " + phoneNumber);
            return ValidationResult.success();
        } catch (SQLException e) {
            System.err.println("Error checking phone uniqueness for update: " + e.getMessage());
            return ValidationResult.failure("Database error while checking phone number");
        }
    }

    public ValidationResult validateEmailUniqueForUpdate(String customerId, String email) {
        String query = "SELECT COUNT(*) FROM Customer WHERE email = ? AND customer_id != ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            stmt.setString(2, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("=== EMAIL ALREADY EXISTS (OTHER CUSTOMER) ===");
                System.out.println("Email: " + email);
                return ValidationResult.failure("Email already exists");
            }
            
            System.out.println(" Email is unique for update: " + email);
            return ValidationResult.success();
        } catch (SQLException e) {
            System.err.println("Error checking email uniqueness for update: " + e.getMessage());
            return ValidationResult.failure("Database error while checking email");
        }
    }

    public ValidationResult validateAadharNumberUniqueForUpdate(String customerId, String aadharNumber) {
        String query = "SELECT COUNT(*) FROM Customer WHERE aadhar_number = ? AND customer_id != ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, aadharNumber);
            stmt.setString(2, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("=== AADHAR NUMBER ALREADY EXISTS (OTHER CUSTOMER) ===");
                System.out.println("Aadhar: " + aadharNumber);
                return ValidationResult.failure("Aadhar number already exists");
            }
            
            System.out.println("✓ Aadhar number is unique for update: " + aadharNumber);
            return ValidationResult.success();
        } catch (SQLException e) {
            System.err.println("Error checking aadhar uniqueness for update: " + e.getMessage());
            return ValidationResult.failure("Database error while checking aadhar number");
        }
    }

    public ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.failure("Customer name is required");
        }
        
        if (name.trim().length() < 2) {
            return ValidationResult.failure("Customer name must be at least 2 characters long");
        }
        
        if (name.length() > 100) {
            return ValidationResult.failure("Customer name cannot exceed 100 characters");
        }
        
        if (!name.matches("^[a-zA-Z\\s'.-]+$")) {
            return ValidationResult.failure("Customer name contains invalid characters");
        }
        
        return ValidationResult.success();
    }

    public ValidationResult validatePhoneNumberFormat(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return ValidationResult.failure("Phone number is required");
        }
        
        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
        
        if (cleanPhone.length() != 10) {
            return ValidationResult.failure("Phone number must be 10 digits and cannot start with 0");
        }
        
        if (cleanPhone.startsWith("0")) {
            return ValidationResult.failure("Phone number must be 10 digits and cannot start with 0");
        }
        
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return ValidationResult.failure("Phone number must be 10 digits and cannot start with 0");
        }
        
        return ValidationResult.success();
    }

    public ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.failure("Email is required");
        }
        
        if (email.length() > 100) {
            return ValidationResult.failure("Email cannot exceed 100 characters");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.failure("Email format is invalid");
        }
        
        return ValidationResult.success();
    }

    public ValidationResult validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return ValidationResult.failure("Address is required");
        }
        
        if (address.trim().length() < 10) {
            return ValidationResult.failure("Address must be at least 10 characters long");
        }
        
        if (address.length() > 500) {
            return ValidationResult.failure("Address cannot exceed 500 characters");
        }
        
        return ValidationResult.success();
    }

    public ValidationResult validateCustomerPin(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            return ValidationResult.failure("Customer PIN is required");
        }
        
        if (!pin.matches("^[0-9]{6}$")) {
            return ValidationResult.failure("Customer PIN must be exactly 6 digits");
        }
        
        return ValidationResult.success();
    }

    public ValidationResult validateAadharNumberFormat(String aadhar) {
        if (aadhar == null || aadhar.trim().isEmpty()) {
            return ValidationResult.failure("Aadhar number is required");
        }
        
        String cleanAadhar = aadhar.replaceAll("[^0-9]", "");
        
        if (cleanAadhar.length() != 12) {
            return ValidationResult.failure("Aadhar number must be exactly 12 digits");
        }
        
        if (!AADHAR_PATTERN.matcher(cleanAadhar).matches()) {
            return ValidationResult.failure("Aadhar number format is invalid");
        }
        
        return ValidationResult.success();
    }

    public ValidationResult validateDateOfBirth(LocalDate dob) {
        if (dob == null) {
            return ValidationResult.failure("Date of birth is required");
        }
        
        LocalDate now = LocalDate.now();
        
        if (dob.isAfter(now)) {
            return ValidationResult.failure("Date of birth cannot be in the future");
        }
        
        int age = Period.between(dob, now).getYears();
        
        if (age < 18) {
            return ValidationResult.failure("Customer must be at least 18 years old");
        }
        
        if (age > 120) {
            return ValidationResult.failure("Invalid date of birth");
        }
        
        return ValidationResult.success();
    }

    public ValidationResult validatePhoneNumberUniqueness(String phoneNumber) {
        String query = "SELECT COUNT(*) FROM Customer WHERE phone_number = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("=== PHONE NUMBER ALREADY EXISTS ===");
                System.out.println("Phone: " + phoneNumber);
                return ValidationResult.failure("Phone number already exists");
            }
        } catch (SQLException e) {
            System.err.println("Error checking phone number uniqueness: " + e.getMessage());
            return ValidationResult.failure("Database error while checking phone uniqueness");
        }
        
        return ValidationResult.success();
    }

    public ValidationResult validateEmailUniqueness(String email) {
        String query = "SELECT COUNT(*) FROM Customer WHERE email = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("=== EMAIL ALREADY EXISTS ===");
                System.out.println("Email: " + email);
                return ValidationResult.failure("Email already exists");
            }
        } catch (SQLException e) {
            System.err.println("Error checking email uniqueness: " + e.getMessage());
            return ValidationResult.failure("Database error while checking email uniqueness");
        }
        
        return ValidationResult.success();
    }
    
    

    public ValidationResult validateAadharNumberUniqueness(String aadharNumber) {
        String query = "SELECT COUNT(*) FROM Customer WHERE aadhar_number = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, aadharNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("=== AADHAR NUMBER ALREADY EXISTS ===");
                System.out.println("Aadhar: " + aadharNumber);
                return ValidationResult.failure("Aadhar number already exists");
            }
        } catch (SQLException e) {
            System.err.println("Error checking Aadhar uniqueness: " + e.getMessage());
            return ValidationResult.failure("Database error while checking Aadhar uniqueness");
        }
        
        return ValidationResult.success();
    }
}