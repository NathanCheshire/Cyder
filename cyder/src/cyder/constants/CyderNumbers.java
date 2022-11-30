package cyder.constants;

import cyder.exceptions.IllegalMethodException;

/** A class of commonly used integers throughout Cyder */
public final class CyderNumbers {
    /** Don't change your number. */
    public static final int JENNY = 8675309;

    /** Suppress default constructor. */
    private CyderNumbers() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
