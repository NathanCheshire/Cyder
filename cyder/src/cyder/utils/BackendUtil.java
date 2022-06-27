package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;

import java.io.File;

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
     * Restrict class instantiation.
     */
    private BackendUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Sets up the backend environment if possible.
     *
     * @throws FatalException if python is not installed
     */
    public static void setupBackend(int port) throws FatalException {
        try {
            CyderThreadRunner.submit(() -> {
                try {
                    Preconditions.checkArgument(OSUtil.isBinaryInstalled("python"));

                    File setupFile = OSUtil.buildFile("backend", "setup.bat");

                    String[] command = {"Backend\\setup.bat", "--port", String.valueOf(port)};
                    Runtime.getRuntime().exec(command);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Backend Hoster"); // todo ignore thread probably
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Constructs and returns a backend path.
     * For example, passing "usb","keyboards will return {@link #FULL_BACKEND_PATH} + "/usb/keyboards/"
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
            if (partsPart.isEmpty()) {
                throw new IllegalArgumentException("Provided additional part is empty");
            }

            ret.append(partsPart);
            ret.append(DELIMITER);
        }

        return ret.toString();
    }
}
