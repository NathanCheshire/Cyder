package cyder.constants;

import cyder.exceptions.IllegalMethodException;

/**
 * A class of commonly used integers throughout Cyder
 */
public class CyderNumbers {
    /**
     * 'nuff said.
     */
    public static final int JENNY = 8675309;

    /**
     * Java integer limit.
     */
    public static final int INFINITY = Integer.MAX_VALUE;

    /**
     * Java integer negative limit.
     */
    public static final int NEG_INFINITY = Integer.MIN_VALUE;

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
     * The start of function key code identifiers for Windows.
     */
    public static final int FUNCTION_KEY_START = 61427;

    /**
     * Instantiation of CyderInts is not allowed
     */
    private CyderNumbers() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }
}
