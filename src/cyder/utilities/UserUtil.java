package cyder.utilities;

import com.google.gson.Gson;
import cyder.handler.ErrorHandler;
import cyder.obj.User;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;

public class UserUtil {

    public static User readUser(File userBin) {
        return null;
    }

    public static void writeUser(User user) {

    }

    public static void fixUser(User user) {
        //for all getters on the user that are null:
        // use corresponding setters to set to the data from the default user
    }

    public static User extractUser(File f) {
        if (!f.exists())
            throw new IllegalArgumentException("Provided file does not exist");
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("Provided file is not a json");

        User ret = null;
        Gson gson = new Gson();

        try (Reader reader = new FileReader(f)) {
            ret = gson.fromJson(reader, User.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return ret;
        }
    }

    /**
     * Assuming the corresponding getter and setter functions exist in User.java,
     * this method will call the getter method that matches the provided data
     * @param u - the initialized user containing the data we want to obtain
     * @param data - the data id for which to return
     * @return - the requested data
     */
    public static String extractUserData(User u, String data) {
        String ret = null;

        try {
            final Object o = "";
            for (Method m : o.getClass().getMethods()) {
                if (m.getName().startsWith("get")
                        && m.getParameterTypes().length == 0
                        && m.getName().contains(data)) {
                    final Object r = m.invoke(o);
                    ret = (String) r;
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }
}
