package cyder.constants;

import cyder.exceptions.IllegalMethodException;

/**
 * A class of commonly used integers throughout Cyder
 */
public class CyderNumbers {
    public static final int JENNY = 8675309;

    /**
     * The port to ensure one instance of Cyder is ever active.
     * 143 does have a hidden meaning ;)
     */
    public static final int INSTANCE_SOCKET_PORT = 143;

    /**
     * The timeout to wait for the server socket to connect/fail.
     */
    public static final long singleInstanceEnsurerTimeout = 500;

    /**
     * The tolerance value that the similar command function must be at or above
     * to be passed off as a legit recommendation.
     */
    public static final float SIMILAR_COMMAND_TOL = 0.80f;

    /**
     * Suppress default constructor.
     */
    private CyderNumbers() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }
}
