package com.quikcard.providerloader;

import com.quikcard.providerloader.audit.AuditLogger;
import com.quikcard.providerloader.config.AppConfig;
import com.quikcard.providerloader.db.DatabaseException;
import com.quikcard.providerloader.db.ProviderRepository;
import com.quikcard.providerloader.pgp.PgpException;
import com.quikcard.providerloader.pgp.PgpService;
import com.quikcard.providerloader.sftp.JschSftpClient;
import com.quikcard.providerloader.sftp.SftpClient;
import com.quikcard.providerloader.sftp.SftpException;
import com.quikcard.providerloader.xml.ProviderXmlParser;
import com.quikcard.providerloader.xml.XmlParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Main application entry point for ProviderLoader
 * Connects to SFTP server, downloads XML files, and processes them
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    private AppConfig config;
    private AuditLogger auditLogger;
    private SftpClient sftpClient;
    private PgpService pgpService;
    private ProviderXmlParser xmlParser;
    private ProviderRepository repository;
    
    public static void main(String[] args) {
        App app = new App();
        
        try {
            app.initialize();
            app.run();
        } catch (Exception e) {
            logger.error("Application failed", e);
            System.exit(1);
        } finally {
            app.cleanup();
        }
    }
    
    /**
     * Initialize application components
     */
    private void initialize() {
        logger.info("Initializing ProviderLoader application...");
        
        try {
            // Load configuration
            config = new AppConfig();
            logger.info("Configuration loaded successfully");
            
            // Initialize audit logger
            auditLogger = new AuditLogger("INFO", null);
            auditLogger.logApplicationStart();
            
            // Initialize SFTP client
            sftpClient = new JschSftpClient(
                config.getSftpHost(),
                config.getSftpPort(),
                config.getSftpUsername(),
                config.getSftpPassword(),
                config.getSftpPrivateKeyPath(),
                config.getSftpPrivateKeyPassphrase(),
                config.getSftpKnownHostsPath(),
                config.getConnectionTimeoutMs()
            );
            
            // Initialize service components (stubs for now)
            pgpService = new PgpService(
                config.getPgpPrivateKeyPath(),
                config.getPgpPrivateKeyPassphrase(),
                config.getPgpPublicKeyPath()
            );
            
            xmlParser = new ProviderXmlParser();
            
            repository = new ProviderRepository(
                config.getDbUrl(),
                config.getDbUsername(),
                config.getDbPassword(),
                config.getDbDriver()
            );
            
            logger.info("Application initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize application", e);
            throw new RuntimeException("Initialization failed", e);
        }
    }
    
    /**
     * Main application workflow
     */
    private void run() {
        logger.info("Starting ProviderLoader workflow...");
        
        try {
            // Step 1: Connect to SFTP server
            connectToSftp();
            
            // Step 2: List and download XML files
            List<String> xmlFiles = listXmlFiles();
            downloadXmlFiles(xmlFiles);
            
            // Step 3: Process downloaded files (future implementation)
            processDownloadedFiles(xmlFiles);
            
            logger.info("ProviderLoader workflow completed successfully");
            
        } catch (Exception e) {
            logger.error("Workflow failed", e);
            auditLogger.logProcessingEvent("WORKFLOW", "Main workflow execution", false, e.getMessage());
            throw new RuntimeException("Workflow failed", e);
        }
    }
    
    /**
     * Connect to SFTP server
     */
    private void connectToSftp() throws SftpException {
        logger.info("Connecting to SFTP server: {}:{}", config.getSftpHost(), config.getSftpPort());
        
        long startTime = System.currentTimeMillis();
        
        try {
            sftpClient.connect();
            
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logSftpConnection(config.getSftpHost(), true, null);
            auditLogger.logPerformance("SFTP_CONNECTION", duration, null);
            
            logger.info("Successfully connected to SFTP server");
            
        } catch (SftpException e) {
            auditLogger.logSftpConnection(config.getSftpHost(), false, e.getMessage());
            throw e;
        }
    }
    
    /**
     * List XML files on the SFTP server
     */
    private List<String> listXmlFiles() throws SftpException {
        logger.info("Listing XML files in remote directory: {}", config.getSftpRemoteDirectory());
        
        long startTime = System.currentTimeMillis();
        
        try {
            List<String> xmlFiles = sftpClient.listFiles(
                config.getSftpRemoteDirectory(),
                config.getXmlFilePattern()
            );
            
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logPerformance("LIST_FILES", duration, xmlFiles.size() + " files found");
            
            logger.info("Found {} XML files to download", xmlFiles.size());
            
            for (String fileName : xmlFiles) {
                logger.debug("Found XML file: {}", fileName);
            }
            
            return xmlFiles;
            
        } catch (SftpException e) {
            logger.error("Failed to list XML files", e);
            throw e;
        }
    }
    
    /**
     * Download XML files from SFTP server
     */
    private void downloadXmlFiles(List<String> xmlFiles) throws SftpException {
        logger.info("Downloading {} XML files to local inbox: {}", 
                   xmlFiles.size(), config.getSftpLocalInboxDirectory());
        
        // Ensure local inbox directory exists
        File inboxDir = new File(config.getSftpLocalInboxDirectory());
        if (!inboxDir.exists()) {
            inboxDir.mkdirs();
            logger.info("Created inbox directory: {}", inboxDir.getAbsolutePath());
        }
        
        int successCount = 0;
        int failureCount = 0;
        
        for (String fileName : xmlFiles) {
            try {
                logger.info("Downloading file: {}", fileName);
                
                long startTime = System.currentTimeMillis();
                
                // Get file size for audit logging
                long fileSize = sftpClient.getFileSize(config.getSftpRemoteDirectory(), fileName);
                
                // Download the file
                sftpClient.downloadFile(
                    config.getSftpRemoteDirectory(),
                    fileName,
                    config.getSftpLocalInboxDirectory()
                );
                
                long duration = System.currentTimeMillis() - startTime;
                
                auditLogger.logFileDownload(fileName, fileSize, true, null);
                auditLogger.logPerformance("FILE_DOWNLOAD", duration, fileName);
                
                successCount++;
                logger.info("Successfully downloaded: {} ({} bytes)", fileName, fileSize);
                
            } catch (SftpException e) {
                logger.error("Failed to download file: {}", fileName, e);
                auditLogger.logFileDownload(fileName, 0, false, e.getMessage());
                failureCount++;
                
                // Continue with other files instead of failing completely
            }
        }
        
        logger.info("Download summary - Success: {}, Failures: {}", successCount, failureCount);
        
        if (failureCount > 0) {
            logger.warn("Some files failed to download. Check logs for details.");
        }
    }
    
    /**
     * Process downloaded files (placeholder for future implementation)
     */
    private void processDownloadedFiles(List<String> xmlFiles) {
        logger.info("Processing downloaded files (stub implementation)");
        
        File inboxDir = new File(config.getSftpLocalInboxDirectory());
        
        for (String fileName : xmlFiles) {
            File xmlFile = new File(inboxDir, fileName);
            
            if (!xmlFile.exists()) {
                logger.warn("Downloaded file not found: {}", xmlFile.getAbsolutePath());
                continue;
            }
            
            try {
                // Future implementation will include:
                
                // 1. PGP decryption if file is encrypted
                if (pgpService.isPgpEncrypted(xmlFile)) {
                    logger.info("File {} appears to be PGP encrypted (would decrypt)", fileName);
                    // pgpService.decryptFile(xmlFile, decryptedFile);
                }
                
                // 2. XML parsing and validation
                if (xmlParser.isWellFormed(xmlFile)) {
                    logger.info("File {} is well-formed XML (would parse)", fileName);
                    // ProviderData data = xmlParser.parseProviderXml(xmlFile);
                }
                
                // 3. Database insertion
                logger.info("File {} would be inserted into database", fileName);
                // repository.saveProvider(data);
                
                auditLogger.logProcessingEvent("FILE_PROCESSING", fileName, true, null);
                
            } catch (Exception e) {
                logger.error("Failed to process file: {}", fileName, e);
                auditLogger.logProcessingEvent("FILE_PROCESSING", fileName, false, e.getMessage());
            }
        }
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup() {
        logger.info("Cleaning up application resources...");
        
        try {
            if (sftpClient != null && sftpClient.isConnected()) {
                sftpClient.disconnect();
                logger.info("SFTP connection closed");
            }
            
            if (repository != null) {
                repository.disconnect();
                logger.info("Database connection closed");
            }
            
            if (auditLogger != null) {
                auditLogger.logApplicationStop();
                auditLogger.close();
            }
            
        } catch (Exception e) {
            logger.warn("Error during cleanup", e);
        }
        
        logger.info("Application cleanup completed");
    }
}
