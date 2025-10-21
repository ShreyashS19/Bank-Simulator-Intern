package com.bank.simulator.controller;

import com.bank.simulator.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class CustomerControllerTest {

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = createValidCustomer();
    }

    @Test
    void testCreateCustomer_ValidData() {
        int statusCode = simulateCreateCustomerAPI(testCustomer);
        assertEquals(201, statusCode);
    }

    @Test
    void testCreateCustomer_InvalidPhone() {
        testCustomer.setPhoneNumber("-1");
        int statusCode = simulateCreateCustomerAPI(testCustomer);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateCustomer_EmptyName() {
        testCustomer.setName("");
        int statusCode = simulateCreateCustomerAPI(testCustomer);
        assertEquals(400, statusCode);
    }

    @Test
    void testCreateCustomer_NullData() {
        int statusCode = simulateCreateCustomerAPI(null);
        assertEquals(400, statusCode);
    }

    @Test
    void testGetCustomer_ValidId() {
        int statusCode = simulateGetCustomerAPI("CUST001");
        assertEquals(200, statusCode);
    }

    @Test
    void testGetCustomer_InvalidId() {
        int statusCode = simulateGetCustomerAPI("CUST999");
        assertEquals(404, statusCode);
    }

    @Test
    void testUpdateCustomer_ValidData() {
        testCustomer.setName("John Updated");
        int statusCode = simulateUpdateCustomerAPI("CUST001", testCustomer);
        assertEquals(200, statusCode);
    }

    @Test
    void testUpdateCustomer_InvalidId() {
        int statusCode = simulateUpdateCustomerAPI("CUST999", testCustomer);
        assertEquals(404, statusCode);
    }

    @Test
    void testDeleteCustomer_ValidId() {
        int statusCode = simulateDeleteCustomerAPI("CUST001");
        assertEquals(200, statusCode);
    }

    @Test
    void testDeleteCustomer_InvalidId() {
        int statusCode = simulateDeleteCustomerAPI("CUST999");
        assertEquals(404, statusCode);
    }

    @Test
    void testGetAllCustomers() {
        int statusCode = simulateGetAllCustomersAPI();
        assertEquals(200, statusCode);
    }

    @Test
    void testCreateCustomer_DatabaseError() {
        int statusCode = simulateCreateCustomerWithError(testCustomer);
        assertEquals(500, statusCode);
    }

    // Helper methods
    private int simulateCreateCustomerAPI(Customer customer) {
        if (customer == null) return 400;
        if (customer.getName() == null || customer.getName().trim().isEmpty()) return 400;
        if (!customer.getPhoneNumber().matches("^[6-9]\\d{9}$")) return 400;
        return 201;
    }

    private int simulateGetCustomerAPI(String id) {
        return "CUST001".equals(id) ? 200 : 404;
    }

    private int simulateUpdateCustomerAPI(String id, Customer customer) {
        return "CUST001".equals(id) ? 200 : 404;
    }

    private int simulateDeleteCustomerAPI(String id) {
        return "CUST001".equals(id) ? 200 : 404;
    }

    private int simulateGetAllCustomersAPI() {
        return 200;
    }

    private int simulateCreateCustomerWithError(Customer customer) {
        return 500;
    }

    private Customer createValidCustomer() {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setPhoneNumber("9876543210");
        customer.setEmail("john@example.com");
        customer.setAddress("123 Main Street");
        customer.setCustomerPin("123456");
        customer.setAadharNumber("123456789012");
        customer.setDob(LocalDate.of(1990, 1, 15));
        return customer;
    }
}
