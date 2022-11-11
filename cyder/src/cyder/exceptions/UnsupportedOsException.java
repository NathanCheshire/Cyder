package cyder.exceptions;

/**
 * An exception used to indicate the host's operating system could not be validated 
 * as a supported one.
 */
public class UnsupportedOsException extends RuntimeException {
     /**
     * Constructs a new UnsupportedOs exception using the provided error message.
     */
    public UnsupportedOsException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new UnsupportedOs exception from the provided exception.
     */
    public UnsupportedOsException(Exception e) {
        super(e);
    }
}
