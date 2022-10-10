package cyder.exceptions;

/**
 * An exception used to indicate that an operation involving YouTube failed.
 */
public class YoutubeException extends RuntimeException {
    public YoutubeException(String errorMessage) {
        super(errorMessage);
    }

    public YoutubeException(Exception e) {
        super(e);
    }
}
