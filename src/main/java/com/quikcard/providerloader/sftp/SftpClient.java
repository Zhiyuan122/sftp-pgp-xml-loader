package com.quikcard.providerloader.sftp;

import java.util.List;

/**
 * Interface for SFTP client operations
 */
public interface SftpClient {
    
    /**
     * Connect to the SFTP server
     * @throws SftpException if connection fails
     */
    void connect() throws SftpException;
    
    /**
     * Disconnect from the SFTP server
     */
    void disconnect();
    
    /**
     * Check if currently connected to the SFTP server
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * List files in the remote directory matching the given pattern
     * @param remoteDirectory the remote directory to list files from
     * @param filePattern the file pattern to match (e.g., "*.xml")
     * @return list of matching file names
     * @throws SftpException if listing fails
     */
    List<String> listFiles(String remoteDirectory, String filePattern) throws SftpException;
    
    /**
     * Download a file from the remote server to local directory
     * @param remoteDirectory the remote directory containing the file
     * @param fileName the name of the file to download
     * @param localDirectory the local directory to save the file
     * @throws SftpException if download fails
     */
    void downloadFile(String remoteDirectory, String fileName, String localDirectory) throws SftpException;
    
    /**
     * Download multiple files from the remote server to local directory
     * @param remoteDirectory the remote directory containing the files
     * @param fileNames list of file names to download
     * @param localDirectory the local directory to save the files
     * @throws SftpException if any download fails
     */
    void downloadFiles(String remoteDirectory, List<String> fileNames, String localDirectory) throws SftpException;
    
    /**
     * Get the size of a remote file
     * @param remoteDirectory the remote directory containing the file
     * @param fileName the name of the file
     * @return file size in bytes
     * @throws SftpException if operation fails
     */
    long getFileSize(String remoteDirectory, String fileName) throws SftpException;
}
