package cyder.utilities;

import cyder.constants.CyderStrings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Util methods for performing GET/POST request to the Cyder FastAPI backend.
 */
public class BackendUtil {
    /**
     * The backend path.
     */
    public static final String BACKEND_PATH = "http://127.0.0.1:8000";

    /**
     * The relative path from the backend url to the jvm post path.
     */
    public static final String JVM_PATH = "/posts/jvm";

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
     * @param relativePath the relative path to post to
     * @return whether the post result
     */
    public static String post(String postSchema, String relativePath) {
        postHelper();
        return "";
    }

    /**
     * Standard CRUD read operation.
     *
     * @param relativePath the relative path to get from
     * @return the get result
     */
    public static String get(String relativePath) {

        return "";
    }

    /**
     * Downloads the static/ Cyder files.
     */
    public static void downloadStatic() {
        // need a get request to get the typical url for the static dir zip on google drive
    }

    private static void postHelper() {
        try {
            URL url = new URL("http://127.0.0.1:8000");
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

            OSUtil.out.println(content);

            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            //ExceptionHandler.handle(e);
        }
    }
}
