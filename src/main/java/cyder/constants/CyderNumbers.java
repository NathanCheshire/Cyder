package main.java.cyder.constants;

import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.strings.CyderStrings;

/**
 * A class of commonly used integers throughout Cyder
 */
public final class CyderNumbers {
    /**
     * Don't change your number.
     */
    public static final int JENNY = 8675309;

    /**
     * Suppress default constructor.
     */
    private CyderNumbers() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
