package cyder.constants;

import cyder.exceptions.IllegalMethodException;

/**
 * A class of commonly used integers throughout Cyder
 */
public final class CyderNumbers {
    /**
     * Don't change your number.
     */
    public static final int JENNY = 8675309;

    /**
     * The port to ensure one instance of Cyder is ever active.
     * 143 does have a hidden meaning ;)
     */
    public static final int INSTANCE_SOCKET_PORT = 143;

    /**
     * The timeout to wait for the server socket to connect/fail.
     */
    public static final long SINGLE_INSTANCE_ENSURER_TIMEOUT = 500;

    /**
     * The tolerance value that the similar command function must be at or above
     * to be passed off as a legit recommendation.
     */
    public static final float SIMILAR_COMMAND_TOL = 0.80f;

    /**
     * Suppress default constructor.
     */
    private CyderNumbers() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
