package cyder.exceptions;

/**
 * An exception used to indicate the host's operating system could not be validated as a supported one.
 */
public class IllegalOperatingSystemException extends RuntimeException {
    public IllegalOperatingSystemException(String errorMessage) {
        super(errorMessage);
    }
}
