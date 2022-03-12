package cyder.exceptions;

/**
 * An exception thrown when an illegal method is invoked or triggered.
 */
public class IllegalMethodException extends IllegalArgumentException {
    public IllegalMethodException(String errorMessage) {
        super(errorMessage);
    }
}
