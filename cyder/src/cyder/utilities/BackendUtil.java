package cyder.utilities;

import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Util methods for performing GET/POST request to the Cyder FastAPI backend.
 */
public class BackendUtil {
    /**
     * The relative path from the backend url to the jvm post path.
     */
    public static final String JVM_PATH = "/posts/jvm";

    /**
     * Restrict class instantiation.
     */
    private BackendUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Standard CRUD create operation.
     *
     * @param putSchema    the schema to put on the backend
     * @param relativePath the relative path to the backend
     * @return the create result
     */
    public static boolean PUT(String putSchema, String relativePath) {

        return false;
    }

    /**
     * Standard CRUD read operation.
     *
     * @param relativePath the relative path to the backend
     * @return the get result
     */
    public static String GET(String relativePath) {

        return "";
    }

    /**
     * Standard CRUD update operation.
     *
     * @param postSchema   the schema to post to the backend
     * @param relativePath the relative path to the backend path
     * @return whether the post result
     */
    public static String POST(String postSchema, String relativePath) {

        return "";
    }

    /**
     * Standard CRUD delete operation.
     *
     * @param deleteSchema the schema to pass to the delete backend
     * @param relativePath the relative path to the backend
     * @return whether the delete operation was successful
     */
    public static boolean DELETE(String deleteSchema, String relativePath) {
        return false;
    }

    /**
     * Downloads the static/ Cyder files.
     *
     * @return whether the static files were successfully downloaded.
     */
    public static boolean downloadStatic() {
        // get the url to download from via a get request

        // download static dir

        // ensure was successful

        // continue with cyder

        return false;
    }

    // testing method
    private static void postHelper() {
        try {
            URL url = new URL(CyderUrls.CYDER_BACKEND_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            con.disconnect();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
