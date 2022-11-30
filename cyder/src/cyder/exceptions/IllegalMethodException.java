package cyder.exceptions;

/** An exception thrown when an illegal method is invoked or triggered. */
public class IllegalMethodException extends IllegalArgumentException {
    /** Constructs a new IllegalMethod exception using the provided error message. */
    public IllegalMethodException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new IllegalMethod exception from the provided exception. */
    public IllegalMethodException(Exception e) {
        super(e);
    }
}
