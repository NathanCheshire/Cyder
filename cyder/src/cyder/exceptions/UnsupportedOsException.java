package cyder.exceptions;

/**
 * An exception used to indicate the host's operating system could not be validated as a supported one.
 */
public class UnsupportedOsException extends RuntimeException {
    public UnsupportedOsException(String errorMessage) {
        super(errorMessage);
    }
}
