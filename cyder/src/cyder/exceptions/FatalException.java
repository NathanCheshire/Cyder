package cyder.exceptions;

public class FatalException extends RuntimeException {
    public FatalException(String errorMessage) {
        super(errorMessage);
    }
}

