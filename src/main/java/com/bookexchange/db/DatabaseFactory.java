package com.bookexchange.db;

import com.bookexchange.config.ConfigManager;

public class DatabaseFactory {

    public static DatabaseManager getDatabaseManager() {
        ConfigManager config = ConfigManager.getInstance();
        String dbType = config.getProperty(ConfigManager.DB_TYPE);

        if (dbType == null || dbType.isEmpty()) {
            throw new IllegalStateException("Database type not configured. Please check your configuration.");
        }

        if (config.isMongoDB()) {
            System.out.println("Creating MongoDB manager");
            return new MongoDBManager();
        } else if (config.isMySQL()) {
            System.out.println("Creating MySQL manager");
            return new MySQLManager();
        } else {
            throw new IllegalStateException("Unknown database type configured: " + dbType +
                    ". Valid types are 'mysql' or 'mongodb'.");
        }
    }
}
