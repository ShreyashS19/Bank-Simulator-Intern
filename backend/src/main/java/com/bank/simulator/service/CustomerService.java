package com.bank.simulator.service;

import com.bank.simulator.model.Customer;
import java.util.List;

public interface CustomerService {
    
    String createCustomer(Customer customer); 
    String generateCustomerId();  
    Customer getCustomerById(String customerId); 
    Customer getCustomerByAadharNumber(String aadharNumber);  
    Customer getCustomerByPhoneNumber(String phoneNumber); 
    boolean updateCustomer(String customerId, Customer customer); 
    boolean deleteCustomer(String customerId);
    boolean deleteCustomerByAadhar(String aadharNumber);
    List<Customer> getAllCustomers();
    boolean isPhoneNumberExists(String phoneNumber);
    boolean isEmailExists(String email);
    boolean isAadharNumberExists(String aadharNumber);
    boolean isAadharExists(String aadharNumber);
    boolean customerExistsByEmail(String email);
    boolean isCustomerExistsByEmail(String email);
}
