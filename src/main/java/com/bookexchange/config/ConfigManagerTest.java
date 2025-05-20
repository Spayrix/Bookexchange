package com.bookexchange.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ConfigManagerTest {
    private ConfigManager configManager;
    private File configFile;

    @Before
    public void setUp() {
        configManager = ConfigManager.getInstance();
        configFile = new File("config.properties");
    }

    @After
    public void tearDown() {
        // Restore default configuration
        configManager.setProperty(ConfigManager.DB_TYPE, "mysql");
        configManager.setProperty(ConfigManager.DB_HOST, "localhost");
        configManager.setProperty(ConfigManager.DB_PORT, "3306");
        configManager.setProperty(ConfigManager.DB_NAME, "bookexchange");
        configManager.setProperty(ConfigManager.DB_USER, "root");
        configManager.setProperty(ConfigManager.DB_PASSWORD, "");
        configManager.saveConfig();
    }

    @Test
    public void testSingletonInstance() {
        ConfigManager anotherInstance = ConfigManager.getInstance();
        assertSame(configManager, anotherInstance);
    }

    @Test
    public void testDefaultConfig() {
        // Delete config file if it exists
        if (configFile.exists()) {
            configFile.delete();
        }

        // Load config (should create default)
        configManager.loadConfig();

        // Check default values
        assertEquals("mysql", configManager.getProperty(ConfigManager.DB_TYPE));
        assertEquals("localhost", configManager.getProperty(ConfigManager.DB_HOST));
        assertEquals("3306", configManager.getProperty(ConfigManager.DB_PORT));
        assertEquals("bookexchange", configManager.getProperty(ConfigManager.DB_NAME));
        assertEquals("root", configManager.getProperty(ConfigManager.DB_USER));
        assertEquals("", configManager.getProperty(ConfigManager.DB_PASSWORD));

        // Check that file was created
        assertTrue(configFile.exists());
    }

    @Test
    public void testSaveAndLoadConfig() {
        // Set custom values
        configManager.setProperty(ConfigManager.DB_TYPE, "mongodb");
        configManager.setProperty(ConfigManager.DB_HOST, "127.0.0.1");
        configManager.setProperty(ConfigManager.DB_PORT, "27017");
        configManager.setProperty(ConfigManager.DB_NAME, "bookexchange_test");
        configManager.setProperty(ConfigManager.DB_USER, "testuser");
        configManager.setProperty(ConfigManager.DB_PASSWORD, "testpass");

        // Save config
        configManager.saveConfig();

        // Create new instance and load config
        ConfigManager newInstance = ConfigManager.getInstance();
        newInstance.loadConfig();

        // Check values
        assertEquals("mongodb", newInstance.getProperty(ConfigManager.DB_TYPE));
        assertEquals("127.0.0.1", newInstance.getProperty(ConfigManager.DB_HOST));
        assertEquals("27017", newInstance.getProperty(ConfigManager.DB_PORT));
        assertEquals("bookexchange_test", newInstance.getProperty(ConfigManager.DB_NAME));
        assertEquals("testuser", newInstance.getProperty(ConfigManager.DB_USER));
        assertEquals("testpass", newInstance.getProperty(ConfigManager.DB_PASSWORD));
    }

    @Test
    public void testDatabaseTypeChecks() {
        // Test MySQL
        configManager.setProperty(ConfigManager.DB_TYPE, "mysql");
        assertTrue(configManager.isMySQL());
        assertFalse(configManager.isMongoDB());

        // Test MongoDB
        configManager.setProperty(ConfigManager.DB_TYPE, "mongodb");
        assertTrue(configManager.isMongoDB());
        assertFalse(configManager.isMySQL());

        // Test invalid type
        configManager.setProperty(ConfigManager.DB_TYPE, "invalid");
        assertFalse(configManager.isMySQL());
        assertFalse(configManager.isMongoDB());
    }
}
