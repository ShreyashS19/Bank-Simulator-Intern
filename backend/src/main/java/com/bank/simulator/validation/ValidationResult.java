package com.bank.simulator.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private boolean valid;
    private List<String> errorMessages;
    private String errorCode;
    
    public ValidationResult() {
        this.valid = true;
        this.errorMessages = new ArrayList<>();
    }
    
    public ValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessages = new ArrayList<>();
        if (!valid && errorMessage != null) {
            this.errorMessages.add(errorMessage);
        }
    }
    
    public ValidationResult(boolean valid, String errorMessage, String errorCode) {
        this(valid, errorMessage);
        this.errorCode = errorCode;
    }
    
    public void addError(String errorMessage) {
        this.valid = false;
        this.errorMessages.add(errorMessage);
    }
    
    public void addError(String errorMessage, String errorCode) {
        this.valid = false;
        this.errorMessages.add(errorMessage);
        this.errorCode = errorCode;
    }
    
    public static ValidationResult success() {
        return new ValidationResult();
    }
    
    public static ValidationResult failure(String errorMessage) {
        return new ValidationResult(false, errorMessage);
    }
    
    public static ValidationResult failure(String errorMessage, String errorCode) {
        return new ValidationResult(false, errorMessage, errorCode);
    }
    
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    
    public List<String> getErrorMessages() { return errorMessages; }
    public void setErrorMessages(List<String> errorMessages) { this.errorMessages = errorMessages; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getFirstErrorMessage() {
        return errorMessages.isEmpty() ? null : errorMessages.get(0);
    }
    
    public String getAllErrorMessages() {
        return String.join("; ", errorMessages);
    }
}
