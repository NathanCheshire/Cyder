package cyder.session;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.exceptions.FatalException;
import cyder.strings.StringUtil;

/**
 * Results after determining whether a remote shutdown request should be denied or complied to.
 */
public enum RemoteShutdownRequestResult {
    /**
     * The password was not found.
     */
    PASSWORD_NOT_FOUND(false, "Shutdown request denied, password prop not specified"),

    /**
     * The password was incorrect.
     */
    PASSWORD_INCORRECT(false, "Shutdown request denied, password incorrect"),

    /**
     * The password was correct.
     */
    PASSWORD_CORRECT(true, "Shutdown request accepted, password correct"),

    /**
     * The auto compliance prop for remote shutdown requests is enabled.
     */
    AUTO_COMPLIANCE_ENABLED(true, "Shutdown request accepted, auto comply is enabled");

    /**
     * Whether this result indicates compliance.
     */
    private final boolean shouldComply;

    /**
     * The message for the result.
     */
    private final String message;

    RemoteShutdownRequestResult(boolean shouldComply, String message) {
        this.shouldComply = shouldComply;
        this.message = message;
    }

    /**
     * Returns whether this result is indicative of a compliance.
     *
     * @return whether this result is indicative of a compliance
     */
    public boolean isShouldComply() {
        return shouldComply;
    }

    /**
     * Returns the message for the result.
     *
     * @return the message for the result
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns whether the provided text is indicative of a compliance result.
     *
     * @param text the text
     * @return whether the provided text is indicative of a compliance result
     */
    public static boolean indicativeOfComplianceResult(String text) {
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(text));

        return StringUtil.in(text, true,
                ImmutableList.of(PASSWORD_CORRECT.message, AUTO_COMPLIANCE_ENABLED.message));
    }

    /**
     * Returns the remote shutdown request result which contains the provided message.
     *
     * @param message the message
     * @return the remote shutdown request result which contains the provided message
     */
    public static RemoteShutdownRequestResult fromMessage(String message) {
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(message));

        for (RemoteShutdownRequestResult value : values()) {
            if (message.equalsIgnoreCase(value.getMessage())) {
                return value;
            }
        }

        throw new FatalException("Failed to find result from message: " + message);
    }
}
