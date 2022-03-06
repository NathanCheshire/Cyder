package cyder.utilities;

import cyder.constants.CyderStrings;

/**
 * Util methods for performing GET/POST request to the Cyder FastAPI backend.
 */
public class BackendUtil {
    /**
     * Restrict class instantiation.
     */
    private BackendUtil() {
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Standard CRUD update operation.
     *
     * @param postSchema the schema to post to the backend
     * @return whether the post result
     */
    public static String post(String postSchema) {

        return "";
    }

    /**
     * Standard CRUD read operation.
     *
     * @param getSchema the schema to post to the backend
     * @return the get result
     */
    public static String get(String getSchema) {

        return "";
    }

    /**
     * Standard CRUD create operation.
     *
     * @param putSchema the schema to post to the backend
     * @return whether the operation was successful
     */
    public static boolean put(String putSchema) {

        return false;
    }

    /**
     * Standard CRUD delete operation.
     *
     * @param deleteSchema the schema to post to the backend
     * @return whether the operation was successful
     */
    public static boolean delete(String deleteSchema) {

        return false;
    }

    /**
     * Attempts to download the static Cyder files.
     */
    public static void downloadStatic() {

    }
}
