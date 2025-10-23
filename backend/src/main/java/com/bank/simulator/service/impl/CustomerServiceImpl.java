package com.bank.simulator.service.impl;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Customer;
import com.bank.simulator.service.CustomerService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerServiceImpl implements CustomerService {

    @Override
    public String createCustomer(Customer customer) {
        String customerId = generateCustomerId();
        customer.setCustomerId(customerId);

        String query = "INSERT INTO Customer (customer_id, name, phone_number, email, address, " +
                      "customer_pin, aadhar_number, dob, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                System.out.println("\n=== CUSTOMER CREATED SUCCESSFULLY ===");
                System.out.println("Customer ID: " + customerId);
                System.out.println("Customer Name: " + customer.getName());
                System.out.println("Phone Number: " + customer.getPhoneNumber());
                System.out.println("Email: " + customer.getEmail());
                System.out.println("Aadhar Number: " + customer.getAadharNumber());
                System.out.println("Status: " + customer.getStatus());
                System.out.println("Date of Birth: " + customer.getDob());
                System.out.println("=================================\n");
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

    /**
     * ⭐ NEW: Generate customer ID by querying database for the latest ID
     * This ensures IDs are unique even after app restarts
     */
    @Override
    public String generateCustomerId() {
        String query = "SELECT customer_id FROM Customer " +
                      "WHERE customer_id LIKE 'CUST_%' " +
                      "ORDER BY CAST(SUBSTRING(customer_id, 6) AS UNSIGNED) DESC " +
                      "LIMIT 1";

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                String lastId = rs.getString("customer_id");
                System.out.println("Last customer ID in database: " + lastId);

                String numberPart = lastId.substring(5);
                int lastNumber = Integer.parseInt(numberPart);
                int nextNumber = lastNumber + 1;

                String newId = "CUST_" + nextNumber;
                System.out.println("Generated new customer ID: " + newId);
                return newId;

            } else {
                System.out.println("No existing customers found. Starting with CUST_1");
                return "CUST_1";
            }

        } catch (SQLException e) {
            System.err.println("Error generating customer ID: " + e.getMessage());
            e.printStackTrace();
            long timestamp = System.currentTimeMillis();
            String fallbackId = "CUST_" + timestamp;
            System.err.println("Using fallback ID: " + fallbackId);
            return fallbackId;
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
            System.err.println("Error fetching customer by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Customer getCustomerByAadharNumber(String aadharNumber) {
        String query = "SELECT * FROM Customer WHERE aadhar_number = ? LIMIT 1";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, aadharNumber);
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
            System.err.println("Error fetching customer by Aadhar: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Customer getCustomerByPhoneNumber(String phoneNumber) {
        String query = "SELECT * FROM Customer WHERE phone_number = ? LIMIT 1";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, phoneNumber);
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
            System.err.println("Error fetching customer by phone: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean updateCustomer(String customerId, Customer customer) {
        String query = "UPDATE Customer SET name = ?, phone_number = ?, email = ?, " +
                      "address = ?, customer_pin = ?, aadhar_number = ?, dob = ?, status = ? " +
                      "WHERE customer_id = ?";

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

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("\n=== CUSTOMER UPDATED ===");
                System.out.println("Customer ID: " + customerId);
                System.out.println("Updated rows: " + result);
                return true;
            } else {
                System.err.println("No customer found with ID: " + customerId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteCustomer(String customerId) {
        String query = "DELETE FROM Customer WHERE customer_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, customerId);
            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("Customer deleted successfully: " + customerId);
                return true;
            } else {
                System.err.println("No customer found with ID: " + customerId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteCustomerByAadhar(String aadharNumber) {
        String query = "DELETE FROM Customer WHERE aadhar_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, aadharNumber);
            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("Customer deleted by Aadhar: " + aadharNumber);
                return true;
            } else {
                System.err.println("No customer found with Aadhar: " + aadharNumber);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting customer by Aadhar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM Customer ORDER BY customer_id";

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

            System.out.println("Fetched " + customers.size() + " customers from database");

        } catch (SQLException e) {
            System.err.println("Error fetching all customers: " + e.getMessage());
            e.printStackTrace();
        }

        return customers;
    }

    // ⭐ REQUIRED: Methods from CustomerService interface (used by validation layer)
    
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
            System.err.println("Error checking phone number existence: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
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
            System.err.println("Error checking email existence: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
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
            System.err.println("Error checking Aadhar number existence: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
public boolean customerExistsByEmail(String email) {
    String query = "SELECT COUNT(*) FROM Customer WHERE email = ?";
    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    } catch (SQLException e) {
        System.err.println("Error checking customer existence: " + e.getMessage());
        return false;
    }
}

}
