package cyder.exceptions;

import java.io.IOException;

/**
 * An exception used to indicate that an operation involving YouTube failed.
 */
public class YoutubeException extends IOException {
    public YoutubeException(String errorMessage) {
        super(errorMessage);
    }
}
