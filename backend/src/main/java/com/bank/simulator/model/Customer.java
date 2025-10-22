package com.bank.simulator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

public class Customer {
    @JsonIgnore 
    private String customerId;

    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    
    @JsonIgnore 
    private String customerPin;
    
    private String aadharNumber;
    private LocalDate dob;
    private String status = "Inactive";

    public Customer() {}

    public Customer(String customerId, String name, String phoneNumber, String email, String address,
                   String customerPin, String aadharNumber, LocalDate dob, String status) {
        this.customerId = customerId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.customerPin = customerPin;
        this.aadharNumber = aadharNumber;
        this.dob = dob;
        this.status = status;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCustomerPin() { return customerPin; }
    public void setCustomerPin(String customerPin) { this.customerPin = customerPin; }

    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
