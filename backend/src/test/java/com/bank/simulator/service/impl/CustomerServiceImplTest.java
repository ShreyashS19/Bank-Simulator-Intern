package com.bank.simulator.service.impl;

import com.bank.simulator.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceImplTest {

    private Customer testCustomer;
    private Map<String, Customer> mockDatabase;

    @BeforeEach
    void setUp() {
        mockDatabase = new HashMap<>();
        testCustomer = createValidCustomer();
        mockDatabase.put("CUST001", testCustomer);
    }

    @Test
    void testCreateCustomer_ValidData() {
        Customer newCustomer = createValidCustomer();
        newCustomer.setPhoneNumber("9876543211");
        String result = simulateCreateCustomer(newCustomer);
        assertNotNull(result);
        assertTrue(result.startsWith("CUST"));
    }

    @Test
    void testCreateCustomer_NullCustomer() {
        String result = simulateCreateCustomer(null);
        assertNull(result);
    }

    @Test
    void testCreateCustomer_EmptyName() {
        testCustomer.setName("");
        String result = simulateCreateCustomer(testCustomer);
        assertNull(result);
    }

    @Test
    void testCreateCustomer_InvalidPhone() {
        testCustomer.setPhoneNumber("123");
        String result = simulateCreateCustomer(testCustomer);
        assertNull(result);
    }

    @Test
    void testCreateCustomer_DuplicatePhone() {
        Customer duplicate = createValidCustomer();
        String result = simulateCreateCustomer(duplicate);
        assertNull(result);
    }

    @Test
    void testGetCustomerById_ValidId() {
        Customer result = simulateGetCustomerById("CUST001");
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetCustomerById_InvalidId() {
        Customer result = simulateGetCustomerById("CUST999");
        assertNull(result);
    }

    @Test
    void testUpdateCustomer_ValidData() {
        testCustomer.setName("John Updated");
        boolean result = simulateUpdateCustomer("CUST001", testCustomer);
        assertTrue(result);
    }

    @Test
    void testUpdateCustomer_InvalidId() {
        boolean result = simulateUpdateCustomer("CUST999", testCustomer);
        assertFalse(result);
    }

    @Test
    void testDeleteCustomer_ValidId() {
        boolean result = simulateDeleteCustomer("CUST001");
        assertTrue(result);
    }

    @Test
    void testDeleteCustomer_InvalidId() {
        boolean result = simulateDeleteCustomer("CUST999");
        assertFalse(result);
    }

    @Test
    void testGetAllCustomers() {
        List<Customer> result = simulateGetAllCustomers();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testIsPhoneNumberExists_Existing() {
        boolean result = simulateIsPhoneExists("9876543210");
        assertTrue(result);
    }

    @Test
    void testIsPhoneNumberExists_NotExisting() {
        boolean result = simulateIsPhoneExists("9999999999");
        assertFalse(result);
    }

    @Test
    void testGenerateCustomerId() {
        String id1 = generateCustomerId();
        String id2 = generateCustomerId();
        assertNotNull(id1);
        assertNotEquals(id1, id2);
        assertTrue(id1.startsWith("CUST"));
    }

    // Helper methods
    private String simulateCreateCustomer(Customer customer) {
        if (customer == null || customer.getName() == null || customer.getName().trim().isEmpty()) return null;
        if (!customer.getPhoneNumber().matches("^[6-9]\\d{9}$")) return null;
        if (simulateIsPhoneExists(customer.getPhoneNumber())) return null;
        String id = generateCustomerId();
        mockDatabase.put(id, customer);
        return id;
    }

    private Customer simulateGetCustomerById(String id) {
        return mockDatabase.get(id);
    }

    private boolean simulateUpdateCustomer(String id, Customer customer) {
        if (!mockDatabase.containsKey(id)) return false;
        mockDatabase.put(id, customer);
        return true;
    }

    private boolean simulateDeleteCustomer(String id) {
        return mockDatabase.remove(id) != null;
    }

    private List<Customer> simulateGetAllCustomers() {
        return new ArrayList<>(mockDatabase.values());
    }

    private boolean simulateIsPhoneExists(String phone) {
    for (Customer c : mockDatabase.values()) {
        if (phone.equals(c.getPhoneNumber())) {
            return true;
        }
    }
    return false;
}


    private String generateCustomerId() {
        return "CUST" + (int)(Math.random() * 1000);
    }

    private Customer createValidCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId("CUST001");
        customer.setName("John Doe");
        customer.setPhoneNumber("9876543210");
        customer.setEmail("john@example.com");
        customer.setAddress("123 Main Street");
        customer.setCustomerPin("123456");
        customer.setAadharNumber("123456789012");
        customer.setDob(LocalDate.of(1990, 1, 15));
        customer.setStatus("Active");
        return customer;
    }
}
