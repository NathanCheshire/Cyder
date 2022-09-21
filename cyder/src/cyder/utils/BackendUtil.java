package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

/**
 * Util methods for common local backend requests.
 */
public final class BackendUtil {
    /**
     * The standard location of the local backend.
     */
    public static final String BACKEND_LOCATION = "http://127.0.0.1";

    /**
     * The standard port of the local backend.
     */
    public static final int BACKEND_PORT = 8080;

    /**
     * The full base path of the local backend.
     */
    public static final String FULL_BACKEND_PATH = BACKEND_LOCATION + ":" + BACKEND_PORT;

    /**
     * A url delimiter.
     */
    public static final String DELIMITER = "/";

    /**
     * Suppress default constructor.
     */
    private BackendUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Constructs and returns a backend path.
     * For example, passing "usb","keyboards will return: {@link #FULL_BACKEND_PATH} + "/usb/keyboards/"
     *
     * @param part  the first part of the path to construct
     * @param parts the additional (optional) parts of the path to construct
     * @return the constructed backend path
     */
    public static String constructPath(String part, String... parts) {
        Preconditions.checkNotNull(part);
        Preconditions.checkArgument(!part.isEmpty());

        StringBuilder ret = new StringBuilder(FULL_BACKEND_PATH)
                .append(DELIMITER).append(part).append(DELIMITER);

        for (String partsPart : parts) {
            if (partsPart == null || partsPart.isEmpty()) {
                throw new IllegalArgumentException("Provided additional empty part");
            }

            ret.append(partsPart);
            ret.append(DELIMITER);
        }

        return ret.toString();
    }
}
