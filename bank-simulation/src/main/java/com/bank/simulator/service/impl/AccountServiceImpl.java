package com.bank.simulator.service.impl;

import com.bank.simulator.config.DBConfig;
import com.bank.simulator.model.Account;
import com.bank.simulator.service.AccountService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountServiceImpl implements AccountService {
    private static final AtomicInteger accountCounter = new AtomicInteger(1);

    @Override
    public String createAccount(Account account) {
        String accountId = generateAccountId();
        account.setAccountId(accountId);
        
        String query = """
            INSERT INTO Account (account_id, customer_id, balance, account_type, 
                               account_name, account_number, phone_number_linked, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, account.getAccountId());
            stmt.setString(2, account.getCustomerId());
            stmt.setBigDecimal(3, account.getBalance());
            stmt.setString(4, account.getAccountType());
            stmt.setString(5, account.getAccountName());
            stmt.setString(6, account.getAccountNumber());
            stmt.setString(7, account.getPhoneNumberLinked());
            stmt.setString(8, account.getStatus());
            
            int result = stmt.executeUpdate();
            return result > 0 ? accountId : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Account getAccountById(String accountId) {
        String query = "SELECT * FROM Account WHERE account_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Account account = new Account();
                account.setAccountId(rs.getString("account_id"));
                account.setCustomerId(rs.getString("customer_id"));
                account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                account.setModifiedAt(rs.getTimestamp("modified_at").toLocalDateTime());
                account.setBalance(rs.getBigDecimal("balance"));
                account.setAccountType(rs.getString("account_type"));
                account.setAccountName(rs.getString("account_name"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setPhoneNumberLinked(rs.getString("phone_number_linked"));
                account.setStatus(rs.getString("status"));
                return account;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateAccount(String accountId, Account account) {
        String query = """
            UPDATE Account SET customer_id = ?, balance = ?, account_type = ?, 
                             account_name = ?, account_number = ?, phone_number_linked = ?, 
                             status = ? WHERE account_id = ?
        """;
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, account.getCustomerId());
            stmt.setBigDecimal(2, account.getBalance());
            stmt.setString(3, account.getAccountType());
            stmt.setString(4, account.getAccountName());
            stmt.setString(5, account.getAccountNumber());
            stmt.setString(6, account.getPhoneNumberLinked());
            stmt.setString(7, account.getStatus());
            stmt.setString(8, accountId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteAccount(String accountId) {
        String query = "DELETE FROM Account WHERE account_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isAccountNumberExists(String accountNumber) {
        String query = "SELECT COUNT(*) FROM Account WHERE account_number = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountNumber);
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
    public boolean isCustomerExists(String customerId) {
        String query = "SELECT COUNT(*) FROM Customer WHERE customer_id = ?";
        
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, customerId);
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
    public String generateAccountId() {
        return "ACC_" + accountCounter.getAndIncrement();
    }
}
