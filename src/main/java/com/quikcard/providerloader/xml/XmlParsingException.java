package com.quikcard.providerloader.xml;

/**
 * Exception class for XML parsing operations
 */
public class XmlParsingException extends Exception {
    
    public XmlParsingException(String message) {
        super(message);
    }
    
    public XmlParsingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public XmlParsingException(Throwable cause) {
        super(cause);
    }
}
