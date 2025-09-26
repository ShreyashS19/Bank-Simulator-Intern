package com.bank.simulator.service.impl;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Customer;
import com.bank.simulator.service.CustomerService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDate;
import java.time.Period;


public class CustomerServiceImpl implements CustomerService {
    private static final AtomicInteger customerCounter = new AtomicInteger(1);

    @Override
public String createCustomer(Customer customer) {
    String customerId = generateCustomerId();
    customer.setCustomerId(customerId);

    String query = """
        INSERT INTO Customer (customer_id, name, phone_number, email, address, 
                            customer_pin, aadhar_number, dob, status) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, customer.getCustomerId());
        stmt.setString(2, customer.getName());
        stmt.setString(3, customer.getPhoneNumber());
        stmt.setString(4, customer.getEmail());
        stmt.setString(5, customer.getAddress());
        stmt.setString(6, customer.getCustomerPin());
        stmt.setString(7, customer.getAadharNumber());
        stmt.setDate(8, Date.valueOf(customer.getDob()));
        stmt.setString(9, customer.getStatus());

        int result = stmt.executeUpdate();
        if (result > 0) {
            System.out.println("\n"); // Empty line for readability
            System.out.println("=== CUSTOMER CREATED SUCCESSFULLY ===");
            System.out.println("Customer ID: " + customerId);
            System.out.println("Customer Name: " + customer.getName());
            System.out.println("Phone Number: " + customer.getPhoneNumber());
            System.out.println("Email: " + customer.getEmail());
            System.out.println("Aadhar Number: " + customer.getAadharNumber());
            System.out.println("Status: " + customer.getStatus());
            System.out.println("Date of Birth: " + customer.getDob());
            System.out.println("=== END CUSTOMER CREATION ===");
            System.out.println("\n"); // Empty line after success
            return customerId;
        } else {
            System.err.println("Failed to create customer");
            return null;
        }

    } catch (SQLException e) {
        System.err.println("Database error: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}


    @Override
    public Customer getCustomerById(String customerId) {
        String query = "SELECT * FROM Customer WHERE customer_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Customer customer = new Customer();
                customer.setCustomerId(rs.getString("customer_id"));
                customer.setName(rs.getString("name"));
                customer.setPhoneNumber(rs.getString("phone_number"));
                customer.setEmail(rs.getString("email"));
                customer.setAddress(rs.getString("address"));
                customer.setCustomerPin(rs.getString("customer_pin"));
                customer.setAadharNumber(rs.getString("aadhar_number"));
                customer.setDob(rs.getDate("dob").toLocalDate());
                customer.setStatus(rs.getString("status"));
                return customer;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateCustomer(String customerId, Customer customer) {
        String query = """
            UPDATE Customer SET name = ?, phone_number = ?, email = ?, address = ?, 
                              customer_pin = ?, aadhar_number = ?, dob = ?, status = ? 
            WHERE customer_id = ?
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhoneNumber());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getCustomerPin());
            stmt.setString(6, customer.getAadharNumber());
            stmt.setDate(7, Date.valueOf(customer.getDob()));
            stmt.setString(8, customer.getStatus());
            stmt.setString(9, customerId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteCustomer(String customerId) {
        String query = "DELETE FROM Customer WHERE customer_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM Customer";
        
        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Customer customer = new Customer();
                customer.setCustomerId(rs.getString("customer_id"));
                customer.setName(rs.getString("name"));
                customer.setPhoneNumber(rs.getString("phone_number"));
                customer.setEmail(rs.getString("email"));
                customer.setAddress(rs.getString("address"));
                customer.setCustomerPin(rs.getString("customer_pin"));
                customer.setAadharNumber(rs.getString("aadhar_number"));
                customer.setDob(rs.getDate("dob").toLocalDate());
                customer.setStatus(rs.getString("status"));
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    @Override
    public boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("\\d{10}") && !phoneNumber.startsWith("0");
    }

    @Override
    public boolean isPhoneNumberExists(String phoneNumber) {
        String query = "SELECT COUNT(*) FROM Customer WHERE phone_number = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
     
    /**
 * Add these methods to your existing CustomerServiceImpl class
 */

@Override
public boolean isEmailValid(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email.matches(emailRegex);
}

@Override
public boolean isEmailExists(String email) {
    String query = "SELECT COUNT(*) FROM Customer WHERE email = ?";
    
    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

@Override
public boolean isAadharValid(String aadhar) {
    if (aadhar == null || aadhar.trim().isEmpty()) {
        return false;
    }
    
    String cleanAadhar = aadhar.replaceAll("[^0-9]", "");
    return cleanAadhar.length() == 12 && cleanAadhar.matches("\\d{12}");
}

@Override
public boolean isPinValid(String pin) {
    if (pin == null || pin.trim().isEmpty()) {
        return false;
    }
    
    return pin.matches("^[0-9]{4,6}$");
}

@Override
public boolean isAgeValid(LocalDate dob) {
    if (dob == null) {
        return false;
    }
    
    LocalDate now = LocalDate.now();
    
    if (dob.isAfter(now)) {
        return false;
    }
    
    int age = Period.between(dob, now).getYears();
    return age >= 18 && age <= 120;
}


    @Override
    public boolean isAadharNumberExists(String aadharNumber) {
        String query = "SELECT COUNT(*) FROM Customer WHERE aadhar_number = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, aadharNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String generateCustomerId() {
        return "CUST_" + customerCounter.getAndIncrement();
    }
}
