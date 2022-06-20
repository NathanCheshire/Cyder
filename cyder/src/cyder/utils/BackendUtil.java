package cyder.utils;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

/**
 * Util methods for common local backend requests.
 */
public class BackendUtil {
    /**
     * Restrict class instantiation.
     */
    private BackendUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Downloads the static/ Cyder files.
     *
     * @return whether the static files were successfully downloaded.
     */
    public static boolean downloadStatic() {
        // todo

        return false;
    }
}
