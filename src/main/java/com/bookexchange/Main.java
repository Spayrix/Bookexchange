package com.bookexchange;

import com.bookexchange.config.ConfigManager;
import com.bookexchange.ui.LoginFrame;

import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize configuration
        try {
            ConfigManager configManager = ConfigManager.getInstance();

            // Check if config file exists
            File configFile = new File("config.properties");
            if (!configFile.exists()) {
                System.out.println("Creating default configuration file...");
                configManager.saveConfig();
            }

            // Print current configuration for debugging
            System.out.println("Current configuration:");
            System.out.println("DB Type: " + configManager.getProperty(ConfigManager.DB_TYPE));
            System.out.println("DB Host: " + configManager.getProperty(ConfigManager.DB_HOST));
            System.out.println("DB Port: " + configManager.getProperty(ConfigManager.DB_PORT));
            System.out.println("DB Name: " + configManager.getProperty(ConfigManager.DB_NAME));
            System.out.println("DB User: " + configManager.getProperty(ConfigManager.DB_USER));

            // Check and create database if needed
            checkDatabase();

        } catch (Exception e) {
            System.err.println("Error initializing configuration: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error initializing application configuration: " + e.getMessage(),
                    "Configuration Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Start application with login screen
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                System.err.println("Error starting application: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Application Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void checkDatabase() {
        ConfigManager config = ConfigManager.getInstance();
        String dbType = config.getProperty(ConfigManager.DB_TYPE);

        if ("mysql".equals(dbType)) {
            try {
                // Load MySQL driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                String host = config.getProperty(ConfigManager.DB_HOST);
                String port = config.getProperty(ConfigManager.DB_PORT);
                String user = config.getProperty(ConfigManager.DB_USER);
                String password = config.getProperty(ConfigManager.DB_PASSWORD);
                String dbName = config.getProperty(ConfigManager.DB_NAME);

                // Connect to MySQL server (without database)
                String url = "jdbc:mysql://" + host + ":" + port;
                System.out.println("Checking MySQL database: " + url);

                try (Connection conn = DriverManager.getConnection(url, user, password)) {
                    try (Statement stmt = conn.createStatement()) {
                        // Create database if it doesn't exist
                        System.out.println("Creating database if not exists: " + dbName);
                        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                    }
                }

                System.out.println("Database check completed successfully");
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Error checking database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
