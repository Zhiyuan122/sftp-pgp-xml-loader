package com.quikcard.providerloader.sftp;

/**
 * Exception class for SFTP operations
 */
public class SftpException extends Exception {
    
    public SftpException(String message) {
        super(message);
    }
    
    public SftpException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SftpException(Throwable cause) {
        super(cause);
    }
}
