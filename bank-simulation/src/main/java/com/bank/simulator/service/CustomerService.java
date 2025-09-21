package com.bank.simulator.service;

import com.bank.simulator.model.Customer;
import java.util.List;

public interface CustomerService {
    String createCustomer(Customer customer);
    Customer getCustomerById(String customerId);
    boolean updateCustomer(String customerId, Customer customer);
    boolean deleteCustomer(String customerId);
    List<Customer> getAllCustomers();
    boolean isPhoneNumberValid(String phoneNumber);
    boolean isPhoneNumberExists(String phoneNumber);
    boolean isAadharNumberExists(String aadharNumber);
    String generateCustomerId();
}
