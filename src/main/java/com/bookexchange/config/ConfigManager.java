package com.bookexchange.config;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;
    private final String CONFIG_FILE = "config.properties";

    public static final String DB_TYPE = "db.type";
    public static final String DB_HOST = "db.host";
    public static final String DB_PORT = "db.port";
    public static final String DB_NAME = "db.name";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";

    private ConfigManager() {
        properties = new Properties();
        loadConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void loadConfig() {
        File configFile = new File(CONFIG_FILE);

        try {
            if (configFile.exists()) {
                try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                    properties.load(input);
                    System.out.println("Configuration loaded from " + CONFIG_FILE);
                }
            } else {
                System.out.println("Config file not found. Creating default configuration.");
                setDefaultConfig();
                saveConfig();
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            e.printStackTrace();
            setDefaultConfig();
        }

        // Validate configuration
        validateConfig();
    }

    private void validateConfig() {
        boolean modified = false;

        // Check if db.type is set and valid
        String dbType = properties.getProperty(DB_TYPE);
        if (dbType == null || dbType.isEmpty() || (!dbType.equals("mysql") && !dbType.equals("mongodb"))) {
            properties.setProperty(DB_TYPE, "mysql"); // Default to MySQL
            modified = true;
        }

        // Check other required properties
        if (properties.getProperty(DB_HOST) == null || properties.getProperty(DB_HOST).isEmpty()) {
            properties.setProperty(DB_HOST, "localhost");
            modified = true;
        }

        if (properties.getProperty(DB_PORT) == null || properties.getProperty(DB_PORT).isEmpty()) {
            // Set default port based on database type
            if ("mongodb".equals(properties.getProperty(DB_TYPE))) {
                properties.setProperty(DB_PORT, "27017");
            } else {
                properties.setProperty(DB_PORT, "3306");
            }
            modified = true;
        }

        if (properties.getProperty(DB_NAME) == null || properties.getProperty(DB_NAME).isEmpty()) {
            properties.setProperty(DB_NAME, "bookexchange");
            modified = true;
        }

        if (properties.getProperty(DB_USER) == null) {
            properties.setProperty(DB_USER, "root");
            modified = true;
        }

        if (properties.getProperty(DB_PASSWORD) == null) {
            properties.setProperty(DB_PASSWORD, "");
            modified = true;
        }

        // Save if changes were made
        if (modified) {
            saveConfig();
        }
    }

    private void setDefaultConfig() {
        properties.setProperty(DB_TYPE, "mysql"); // Default to MySQL
        properties.setProperty(DB_HOST, "localhost");
        properties.setProperty(DB_PORT, "3306");
        properties.setProperty(DB_NAME, "bookexchange");
        properties.setProperty(DB_USER, "root");
        properties.setProperty(DB_PASSWORD, "");
    }

    public void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Book Exchange Configuration");
            System.out.println("Configuration saved to " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Error saving configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public boolean isMongoDB() {
        return "mongodb".equals(properties.getProperty(DB_TYPE));
    }

    public boolean isMySQL() {
        return "mysql".equals(properties.getProperty(DB_TYPE));
    }
}
