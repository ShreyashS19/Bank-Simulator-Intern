package com.bank.simulator.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

@WebListener
public class DatabaseInitializerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== DATABASE INITIALIZER LISTENER STARTED ===");
        System.out.println("WebApp STARTING UP: Initializing database connection..");
        
        try {
            createDatabaseIfNotExists();
            createTablesIfNotExists();
            System.out.println("WebApp STARTED SUCCESSFULLY: Database initialization sequence completed.");
        } catch (SQLException e) {
            System.err.println("!!! ERROR: Database initialization failed !!!");
            e.printStackTrace();
        }
    }

    private void createDatabaseIfNotExists() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
        
        
        Properties props = new Properties();
        String username = "root";
        String password = "";
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
                username = props.getProperty("db.username", "root");
                password = props.getProperty("db.password", "");
                System.out.println("Loaded database credentials from application.properties");
            } else {
                System.err.println(" application.properties not found! Using default credentials.");
            }
        } catch (IOException e) {
            System.err.println(" Could not load application.properties: " + e.getMessage());
        }
        
        String dbUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        
        try (Connection conn = java.sql.DriverManager.getConnection(dbUrl, username, password);
             Statement stmt = conn.createStatement()) {
            
            String createDbQuery = "CREATE DATABASE IF NOT EXISTS bank_simulation";
            stmt.executeUpdate(createDbQuery);
            System.out.println("Database 'bank_simulation' is ready.");
        }
    }

    private void createTablesIfNotExists() throws SQLException {
        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            String customerTable = """
                CREATE TABLE IF NOT EXISTS Customer (
                    customer_id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    phone_number VARCHAR(10) NOT NULL UNIQUE,
                    email VARCHAR(100) NOT NULL,
                    address TEXT NOT NULL,
                    customer_pin VARCHAR(6) NOT NULL,
                    aadhar_number VARCHAR(12) NOT NULL UNIQUE,
                    dob DATE NOT NULL,
                    status VARCHAR(20) DEFAULT 'Inactive'
                )
            """;

            String accountTable = """
                CREATE TABLE IF NOT EXISTS Account (
                    account_id VARCHAR(50) PRIMARY KEY,
                    customer_id VARCHAR(50) NOT NULL,
                    account_number VARCHAR(30) NOT NULL UNIQUE,
                    aadhar_number VARCHAR(12) NOT NULL,
                    ifsc_code VARCHAR(11) NOT NULL,
                    phone_number_linked VARCHAR(10) NOT NULL,
                    amount DECIMAL(15,2) DEFAULT 600.00,
                    bank_name VARCHAR(100) NOT NULL,
                    name_on_account VARCHAR(100) NOT NULL,
                    status VARCHAR(20) DEFAULT 'ACTIVE',
                    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
                )
            """;

            String transactionTable = """
                CREATE TABLE IF NOT EXISTS Transaction (
                    transaction_id VARCHAR(50) PRIMARY KEY,
                    account_id VARCHAR(50) NOT NULL,
                    sender_account_number VARCHAR(30) NOT NULL,
                    receiver_account_number VARCHAR(30) NOT NULL,
                    amount DECIMAL(15,2) NOT NULL,
                    transaction_type ENUM('ONLINE') NOT NULL DEFAULT 'ONLINE',
                    description TEXT,
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (account_id) REFERENCES Account(account_id) ON DELETE CASCADE
                )
            """;
                    String userTable = """
                CREATE TABLE IF NOT EXISTS User (
                    id VARCHAR(50) PRIMARY KEY,
                    full_name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """;


            stmt.executeUpdate(userTable);
            System.out.println("Table 'User' is ready.");

            stmt.executeUpdate(customerTable);
            System.out.println(" Table 'Customer' is ready.");
            
            stmt.executeUpdate(accountTable);
            System.out.println(" Table 'Account' is ready with new structure.");
            
            stmt.executeUpdate(transactionTable);
            System.out.println("  Table 'Transaction' is ready with new structure.");
            
            System.out.println("All tables are ready.");
            
        } catch (SQLException e) {
            System.err.println("!!! ERROR: Could not create tables in the database !!!");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("WebApp SHUTTING DOWN: Database connections closed.");
    }
}
