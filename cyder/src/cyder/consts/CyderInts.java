package cyder.consts;

/**
 * A class of commonly used integers throughout Cyder
 */
public class CyderInts {
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int NEG_INFINITY = Integer.MIN_VALUE;
    public static final int INSTANCE_SOCKET_PORT = 143;
    public static final long singleInstanceEnsurerTimeout = 500;

    /**
     * Instantiation of CyderInts is not allowed
     */
    private CyderInts() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }
}
