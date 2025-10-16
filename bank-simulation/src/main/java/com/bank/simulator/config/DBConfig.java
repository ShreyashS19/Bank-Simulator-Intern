package com.bank.simulator.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DBConfig {
    private static final String DB_URL;
    private static final String DB_USERNAME;
    private static final String DB_PASSWORD;
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded in DBConfig.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found in DBConfig!");
            e.printStackTrace();
        }
        
        Properties props = new Properties();
        try (InputStream input = DBConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
                System.out.println("✓ Loaded database configuration from application.properties");
            } else {
                System.err.println("⚠️ application.properties not found!");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Could not load application.properties: " + e.getMessage());
        }
        
        // ⭐ UPDATED: Remove hardcoded password from fallback
        DB_URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/bank_simulation?useSSL=false&serverTimezone=UTC");
        DB_USERNAME = props.getProperty("db.username", "root");
        DB_PASSWORD = props.getProperty("db.password", "");  // ← Changed to empty string
        
        if (DB_PASSWORD.isEmpty()) {
            System.err.println("⚠️ WARNING: Database password not found in application.properties!");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    public static void testConnection() throws SQLException {   
        try (Connection conn = getConnection()) {
            System.out.println("✓ Database connection successful!");
        }
    }
}
