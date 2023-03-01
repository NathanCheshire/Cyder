package cyder.session;

/**
 * The possible socket bind attempt results.
 */
public enum SocketBindAttemptResult {
    PORT_AVAILABLE(true, "Port available"),
    REMOTE_SHUTDOWN_REQUESTS_DISABLED(false, "Remote shutdown requests are disabled"),
    INVALID_PORT(false, "The instance socket port is invalid"),
    PASSWORD_NOT_SET(false, "The remote shutdown request password is not set"),
    REMOTE_SHUTDOWN_REQUEST_DENIED(false, "The remote shutdown request was denied"),
    FAILURE_WHILE_ATTEMPTING_REMOTE_SHUTDOWN(false,
            "An exception occurred while attempting to shutdown a remote instance"),
    TIMED_OUT_AFTER_SUCCESSFUL_REMOTE_SHUTDOWN(false,
            "A remote shutdown was successful but the instance port failed to free"),
    SUCCESS_AFTER_REMOTE_SHUTDOWN(true, "A remote shutdown request was successful and the port freed up"),
    PORT_UNAVAILABLE(false, "The port was unavailable and non-responsive to a remote shutdown request");

    /**
     * Whether this socket bind attempt result is a success.
     */
    private final boolean successful;

    /**
     * The message for this result.
     */
    private final String message;

    SocketBindAttemptResult(boolean successful, String message) {
        this.successful = successful;
        this.message = message;
    }

    /**
     * Returns whether this bind attempt result is a success.
     *
     * @return whether this bind attempt result is a success
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Returns the message for this result.
     *
     * @return the message for this result
     */
    public String getMessage() {
        return message;
    }
}
