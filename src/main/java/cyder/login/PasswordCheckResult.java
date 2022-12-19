package main.java.cyder.login;

/**
 * The result of a password check.
 */
enum PasswordCheckResult {
    /**
     * The login failed due to invalid credentials.
     */
    FAILED,
    /**
     * The login failed due to not being able to locate the username.
     */
    UNKNOWN_USER,
    /**
     * The login succeeded and the user uuid should be present.
     */
    SUCCESS
}
