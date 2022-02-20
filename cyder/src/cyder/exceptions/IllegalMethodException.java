package cyder.exceptions;

public class IllegalMethodException extends IllegalArgumentException {
    public IllegalMethodException(String errorMessage) {
        super(errorMessage);
    }
}
