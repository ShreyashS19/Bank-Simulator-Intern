package com.bank.simulator.service;

import com.bank.simulator.model.Customer;
import java.time.LocalDate;
import java.util.List;

public interface CustomerService {
    // Core CRUD operations
    String createCustomer(Customer customer);
    Customer getCustomerById(String customerId);
    boolean updateCustomer(String customerId, Customer customer);
    boolean deleteCustomer(String customerId);
     boolean deleteCustomerByAadhar(String aadharNumber);
    List<Customer> getAllCustomers();
    
    
    // Validation methods
   // boolean isPhoneNumberValid(String phoneNumber);
    boolean isPhoneNumberExists(String phoneNumber);
 //   boolean isEmailValid(String email);
    boolean isEmailExists(String email);
    boolean isAadharNumberExists(String aadharNumber);
    // boolean isAadharValid(String aadhar);
    // boolean isPinValid(String pin);
    // boolean isAgeValid(LocalDate dob);
    
    // ID generation
    String generateCustomerId();
    Customer getCustomerByAadharNumber(String aadharNumber);
Customer getCustomerByPhoneNumber(String phoneNumber);

}
