package cyder.consts;

public class CyderInts {
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int NEG_INFINITY = Integer.MIN_VALUE;

    private CyderInts() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }
}
