package cyder.exceptions;

/**
 * An exception used to indicate that an operation involving YouTube failed.
 */
public class YoutubeException extends RuntimeException {
    /**
     * Constructs a new YouTube exception using the provided error message.
     */
    public YoutubeException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new YouTube exception from the provided exception.
     */
    public YoutubeException(Exception e) {
        super(e);
    }
}
