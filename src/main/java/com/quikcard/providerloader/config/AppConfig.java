package com.quikcard.providerloader.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application configuration manager that loads settings from application.properties
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final String PROPERTIES_FILE = "/application.properties";
    
    private final Properties properties;
    
    public AppConfig() {
        this.properties = new Properties();
        loadProperties();
    }
    
    private void loadProperties() {
        try (InputStream inputStream = getClass().getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("Configuration file not found: " + PROPERTIES_FILE);
            }
            properties.load(inputStream);
            logger.info("Configuration loaded successfully from {}", PROPERTIES_FILE);
        } catch (IOException e) {
            logger.error("Failed to load configuration from {}", PROPERTIES_FILE, e);
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
    
    // SFTP Configuration
    public String getSftpHost() {
        return getProperty("sftp.host");
    }
    
    public int getSftpPort() {
        return Integer.parseInt(getProperty("sftp.port", "22"));
    }
    
    public String getSftpUsername() {
        return getProperty("sftp.username");
    }
    
    public String getSftpPassword() {
        return getProperty("sftp.password");
    }
    
    public String getSftpPrivateKeyPath() {
        return getProperty("sftp.private.key.path", null);
    }
    
    public String getSftpPrivateKeyPassphrase() {
        return getProperty("sftp.private.key.passphrase", null);
    }
    
    public String getSftpRemoteDirectory() {
        return getProperty("sftp.remote.directory");
    }
    
    public String getSftpLocalInboxDirectory() {
        return getProperty("sftp.local.inbox.directory", "./inbox");
    }
    
    public String getSftpKnownHostsPath() {
        return getProperty("sftp.known.hosts.path");
    }
    
    // File Patterns
    public String getXmlFilePattern() {
        return getProperty("file.pattern.xml", "*.xml");
    }
    
    // PGP Configuration
    public String getPgpPrivateKeyPath() {
        return getProperty("pgp.private.key.path");
    }
    
    public String getPgpPrivateKeyPassphrase() {
        return getProperty("pgp.private.key.passphrase");
    }
    
    public String getPgpPublicKeyPath() {
        return getProperty("pgp.public.key.path");
    }
    
    // Database Configuration
    public String getDbUrl() {
        return getProperty("db.url");
    }
    
    public String getDbUsername() {
        return getProperty("db.username");
    }
    
    public String getDbPassword() {
        return getProperty("db.password");
    }
    
    public String getDbDriver() {
        return getProperty("db.driver", "oracle.jdbc.driver.OracleDriver");
    }
    
    // Application Settings
    public int getRetryAttempts() {
        return Integer.parseInt(getProperty("app.retry.attempts", "3"));
    }
    
    public long getRetryDelayMs() {
        return Long.parseLong(getProperty("app.retry.delay.ms", "1000"));
    }
    
    public int getConnectionTimeoutMs() {
        return Integer.parseInt(getProperty("app.connection.timeout.ms", "30000"));
    }
    
    // Helper methods
    private String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Required property not found or empty: " + key);
        }
        return value.trim();
    }
    
    private String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }
    
    /**
     * Get all properties for debugging purposes
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }
}
