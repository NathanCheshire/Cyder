package cyder.logging;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

/**
 * Utilities necessary for the Cyder logger.
 */
public class LoggingUtil {
    /**
     * Suppress default constructor.
     */
    private LoggingUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }


}
