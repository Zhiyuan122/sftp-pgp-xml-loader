package com.quikcard.providerloader;

import com.quikcard.providerloader.config.AppConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for the ProviderLoader application
 */
public class AppTest {
    
    @Test
    public void testConfigurationLoading() {
        // Test that configuration can be loaded without throwing exceptions
        assertDoesNotThrow(() -> {
            AppConfig config = new AppConfig();
            assertNotNull(config);
            
            // Test some basic configuration properties
            assertNotNull(config.getSftpHost());
            assertTrue(config.getSftpPort() > 0);
            assertNotNull(config.getSftpUsername());
        });
    }
    
    @Test
    public void testApplicationMain() {
        // Test that the main method exists and can be invoked
        // Note: This won't actually run the application due to SFTP connection requirements
        assertDoesNotThrow(() -> {
            // Just verify the main method exists
            App.class.getDeclaredMethod("main", String[].class);
        });
    }
}
