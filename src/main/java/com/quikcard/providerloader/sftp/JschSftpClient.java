package com.quikcard.providerloader.sftp;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * JSch implementation of SftpClient interface
 */
public class JschSftpClient implements SftpClient {
    private static final Logger logger = LoggerFactory.getLogger(JschSftpClient.class);
    
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String privateKeyPath;
    private final String privateKeyPassphrase;
    private final String knownHostsPath;
    private final int connectionTimeoutMs;
    
    private JSch jsch;
    private Session session;
    private ChannelSftp sftpChannel;
    
    public JschSftpClient(String host, int port, String username, String password, 
                         String privateKeyPath, String privateKeyPassphrase, 
                         String knownHostsPath, int connectionTimeoutMs) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.privateKeyPath = privateKeyPath;
        this.privateKeyPassphrase = privateKeyPassphrase;
        this.knownHostsPath = knownHostsPath;
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.jsch = new JSch();
    }
    
    @Override
    public void connect() throws SftpException {
        try {
            logger.info("Connecting to SFTP server: {}:{}", host, port);
            
            // Set up known hosts if provided
            if (knownHostsPath != null && !knownHostsPath.trim().isEmpty()) {
                String expandedPath = knownHostsPath.replace("~", System.getProperty("user.home"));
                jsch.setKnownHosts(expandedPath);
                logger.debug("Using known hosts file: {}", expandedPath);
            }
            
            // Set up private key authentication if provided
            if (privateKeyPath != null && !privateKeyPath.trim().isEmpty()) {
                if (privateKeyPassphrase != null && !privateKeyPassphrase.trim().isEmpty()) {
                    jsch.addIdentity(privateKeyPath, privateKeyPassphrase);
                } else {
                    jsch.addIdentity(privateKeyPath);
                }
                logger.debug("Using private key authentication: {}", privateKeyPath);
            }
            
            // Create session
            session = jsch.getSession(username, host, port);
            
            // Set password if provided (for password authentication or private key passphrase)
            if (password != null && !password.trim().isEmpty()) {
                session.setPassword(password);
            }
            
            // Configure session properties
            session.setTimeout(connectionTimeoutMs);
            
            // For development/testing - in production, you should properly manage host key verification
            session.setConfig("StrictHostKeyChecking", "no");
            
            // Connect session
            session.connect();
            logger.info("Session connected successfully");
            
            // Open SFTP channel
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;
            
            logger.info("SFTP channel connected successfully");
            
        } catch (JSchException e) {
            logger.error("Failed to connect to SFTP server", e);
            disconnect(); // Clean up any partial connections
            throw new SftpException("Failed to connect to SFTP server: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void disconnect() {
        try {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
                logger.debug("SFTP channel disconnected");
            }
            
            if (session != null && session.isConnected()) {
                session.disconnect();
                logger.debug("Session disconnected");
            }
            
            logger.info("Disconnected from SFTP server");
        } catch (Exception e) {
            logger.warn("Error during disconnect", e);
        } finally {
            sftpChannel = null;
            session = null;
        }
    }
    
    @Override
    public boolean isConnected() {
        return sftpChannel != null && sftpChannel.isConnected() && 
               session != null && session.isConnected();
    }
    
    @Override
    public List<String> listFiles(String remoteDirectory, String filePattern) throws SftpException {
        if (!isConnected()) {
            throw new SftpException("Not connected to SFTP server");
        }
        
        try {
            logger.debug("Listing files in directory: {} with pattern: {}", remoteDirectory, filePattern);
            
            // Change to remote directory
            sftpChannel.cd(remoteDirectory);
            
            // List files matching pattern
            Vector<ChannelSftp.LsEntry> entries = sftpChannel.ls(filePattern);
            List<String> fileNames = new ArrayList<>();
            
            for (ChannelSftp.LsEntry entry : entries) {
                if (!entry.getAttrs().isDir()) {
                    fileNames.add(entry.getFilename());
                }
            }
            
            logger.info("Found {} files matching pattern {} in directory {}", 
                       fileNames.size(), filePattern, remoteDirectory);
            
            return fileNames;
            
        } catch (com.jcraft.jsch.SftpException e) {
            logger.error("Failed to list files in directory: {}", remoteDirectory, e);
            throw new SftpException("Failed to list files: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void downloadFile(String remoteDirectory, String fileName, String localDirectory) throws SftpException {
        if (!isConnected()) {
            throw new SftpException("Not connected to SFTP server");
        }
        
        try {
            logger.debug("Downloading file: {} from {} to {}", fileName, remoteDirectory, localDirectory);
            
            // Ensure local directory exists
            File localDir = new File(localDirectory);
            if (!localDir.exists()) {
                localDir.mkdirs();
            }
            
            // Construct full paths
            String remotePath = remoteDirectory.endsWith("/") ? 
                               remoteDirectory + fileName : 
                               remoteDirectory + "/" + fileName;
            String localPath = localDirectory.endsWith("/") ? 
                              localDirectory + fileName : 
                              localDirectory + "/" + fileName;
            
            // Download file
            sftpChannel.get(remotePath, localPath);
            
            logger.info("Successfully downloaded file: {} to {}", fileName, localPath);
            
        } catch (com.jcraft.jsch.SftpException e) {
            logger.error("Failed to download file: {}", fileName, e);
            throw new SftpException("Failed to download file: " + fileName + " - " + e.getMessage(), e);
        }
    }
    
    @Override
    public void downloadFiles(String remoteDirectory, List<String> fileNames, String localDirectory) throws SftpException {
        for (String fileName : fileNames) {
            downloadFile(remoteDirectory, fileName, localDirectory);
        }
    }
    
    @Override
    public long getFileSize(String remoteDirectory, String fileName) throws SftpException {
        if (!isConnected()) {
            throw new SftpException("Not connected to SFTP server");
        }
        
        try {
            String remotePath = remoteDirectory.endsWith("/") ? 
                               remoteDirectory + fileName : 
                               remoteDirectory + "/" + fileName;
            
            SftpATTRS attrs = sftpChannel.stat(remotePath);
            return attrs.getSize();
            
        } catch (com.jcraft.jsch.SftpException e) {
            logger.error("Failed to get file size for: {}", fileName, e);
            throw new SftpException("Failed to get file size: " + fileName + " - " + e.getMessage(), e);
        }
    }
}
