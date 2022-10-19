package cyder.login;

/**
 * The status of a {@link PasswordCheckResult}.
 */
enum Result {
    /**
     * The login failed due to invalid credentials.
     */
    FAILED,
    /**
     * The login failed due to not being able to locate the username.
     */
    UNKNOWN_USER,
    /**
     * The login succeeded and the optional user should be present.
     */
    SUCCESS
}
