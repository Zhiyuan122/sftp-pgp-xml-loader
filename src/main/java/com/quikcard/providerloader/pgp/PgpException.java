package com.quikcard.providerloader.pgp;

/**
 * Exception class for PGP operations
 */
public class PgpException extends Exception {
    
    public PgpException(String message) {
        super(message);
    }
    
    public PgpException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PgpException(Throwable cause) {
        super(cause);
    }
}
