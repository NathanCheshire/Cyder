package cyder.utilities;

import com.google.gson.Gson;
import cyder.genesis.GenesisShare;
import cyder.genesis.GenesisShare.Preference;
import cyder.genesis.User;
import cyder.handlers.internal.ErrorHandler;
import cyder.handlers.internal.SessionHandler;
import cyder.ui.ConsoleFrame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class UserUtil {
    //the semaphore to use when reading or writing from/to a JSON file
    private static Semaphore jsonIOSem = new Semaphore(1);

    //getter so exiting method can make sure jsonIOSem is not locked
    public static Semaphore getJsonIOSem() {
        return jsonIOSem;
    }

    /**
     * Sets the given user's data using the provided name and data value
     * @param user the user object to call the setter on
     * @param name the name of the data to change
     * @param value the data value to set it to
     */
    public static void setUserData(User user, String name, String value) {
        try {
            for (Method m : user.getClass().getMethods()) {
                if (m.getName().startsWith("set")
                        && m.getParameterTypes().length == 1
                        && m.getName().replace("set","").equalsIgnoreCase(name)) {
                        m.invoke(user, value);
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Sets the user data of the provided file using the given data and name
     * @param f the file to write the json data to
     * @param name the data name
     * @param value the value of the data to update
     */
    public static void setUserData(File f, String name, String value) {
        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");

        User user = extractUser(f);

        try {
            for (Method m : user.getClass().getMethods()) {
                if (m.getName().startsWith("set")
                        && m.getParameterTypes().length == 1
                        && m.getName().replace("set","").equalsIgnoreCase(name)) {
                    m.invoke(user, value);
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        Gson gson = new Gson();


        try  {
            FileWriter writer = new FileWriter(f);
            jsonIOSem.acquire();
            gson.toJson(user, writer);
            writer.close();
            jsonIOSem.release();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void setUserData(String name, String value) {
        File f = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/userdata.json");

        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");

        User user = extractUser(f);

        try {
            for (Method m : user.getClass().getMethods()) {
                if (m.getName().startsWith("set")
                        && m.getParameterTypes().length == 1
                        && m.getName().replace("set","").equalsIgnoreCase(name)) {
                    m.invoke(user, value);
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        Gson gson = new Gson();

        try  {
            FileWriter writer = new FileWriter(f);
            jsonIOSem.acquire();
            gson.toJson(user, writer);
            writer.close();
            jsonIOSem.release();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Writes the provided user after being converted to JSON format to the provided file.
     * @param f the file to write to
     * @param u the user object to write to the file
     */
    public static void setUserData(File f, User u) {
        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");

        Gson gson = new Gson();

        try  {
            FileWriter writer = new FileWriter(f);
            jsonIOSem.acquire();
            gson.toJson(u, writer);
            writer.close();
            jsonIOSem.release();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Writes the given user to the current user's Json file
     * @param u - the user to serialize and write to a file
     */
    public static void setUserData(User u) {
        File f = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/userdata.json");

        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");

        Gson gson = new Gson();

        try  {
            FileWriter writer = new FileWriter(f);
            jsonIOSem.acquire();
            gson.toJson(u, writer);
            writer.close();
            jsonIOSem.release();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Function called upon UUID being set for consoleFrame to attempt to fix any user data
     * in case it was corrupted. If we fail to correct any corrupted data, then we corrupt the user and exit
     */
    public static void fixUser() {
        String UUID = ConsoleFrame.getConsoleFrame().getUUID();

        if (UUID == null)
            return;

        File userBackgroundsFile = new File("dynamic/users/" + UUID + "/Backgrounds");
        File userMusicFile = new File("dynamic/users/" + UUID + "/Music");
        File userJsonFile = new File("dynamic/users/" + UUID + "/userdata.json");

        if (!userBackgroundsFile.exists())
            userBackgroundsFile.mkdir();

        if (!userMusicFile.exists())
            userMusicFile.mkdir();

        if (!userJsonFile.exists())
            IOUtil.corruptedUser();

        User user = extractUser(userJsonFile);

        try {
            //this handles data whos ID is still there
            for (Method getterMethod : user.getClass().getMethods()) {
                if (getterMethod.getName().startsWith("get")
                        && getterMethod.getParameterTypes().length == 0) {
                    //object returned by current getter
                    final Object getterRet = getterMethod.invoke(user);
                    if (getterRet == null || (getterRet instanceof String && ((String) getterRet).length() == 0)) {
                        //fatal data that results in the user being corrupted if it is corrupted
                        if (getterMethod.getName().toLowerCase().contains("pass") ||
                            getterMethod.getName().toLowerCase().contains("name")) {
                            IOUtil.corruptedUser();
                            return;
                        }
                        //non-fatal data that we can restore from the default data
                        else {
                            //find corresponding setter
                            for (Method setterMethod : user.getClass().getMethods()) {
                                if (setterMethod.getName().startsWith("set")
                                        && setterMethod.getParameterTypes().length == 1
                                        && setterMethod.getName().toLowerCase().contains(getterMethod.getName()
                                        .toLowerCase().replace("get",""))) {

                                    Object defaultValue = null;

                                    //find corresponding default vaule
                                    for (Preference pref : GenesisShare.getPrefs()) {
                                        if (pref.getID().toLowerCase().contains(getterMethod.getName()
                                                .toLowerCase().replace("get",""))) {
                                            defaultValue = pref.getDefaultValue();
                                            break;
                                        }
                                    }

                                    setterMethod.invoke(user, defaultValue);
                                    setUserData(userJsonFile,user);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            LinkedList<User.MappedExecutable> exes = user.getExecutables();
            LinkedList<User.MappedExecutable> nonDuplicates = new LinkedList<>();

            for (User.MappedExecutable me : exes) {
                if (!nonDuplicates.contains(me)) {
                    nonDuplicates.add(me);
                }
            }

            user.setExecutables(nonDuplicates);
            setUserData(userJsonFile, user);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Attempts to read backgrounds that Cyder would use for a user.
     * If it fails, the image is corrupted so we delete it in the calling function.
     */
    public static void fixBackgrounds() {
        for (File f : new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Backgrounds").listFiles()) {
           boolean valid = true;

            try (FileInputStream fi = new FileInputStream(f)) {
                BufferedImage sourceImg = ImageIO.read(fi);
                int w = sourceImg.getWidth();
            } catch (Exception e) {
                valid = false;
                ErrorHandler.silentHandle(e);
            }

            if (!valid) {
                f.delete();
            }
        }
    }

    /**
     * Extracts the user from the provided json file
     * @param f the json file to extract a user object from
     * @return the resulting user object
     */
    public static User extractUser(File f) {
        if (!f.exists())
            throw new IllegalArgumentException("Provided file does not exist");
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("Provided file is not a json");

        User ret = null;
        Gson gson = new Gson();

        try {
            jsonIOSem.acquire();
            Reader reader = new FileReader(f);
            ret = gson.fromJson(reader, User.class);
            reader.close();
            jsonIOSem.release();
        } catch (IOException e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Extracts the user from the provided json file
     * @param UUID the uuid if the user we want to obtain
     * @return the resulting user object
     */
    public static User extractUser(String UUID) {
        File f = new File("dynamic/users/" + UUID + "/userdata.json");

        if (!f.exists())
            throw new IllegalArgumentException("Provided file does not exist");

        User ret = null;
        Gson gson = new Gson();

        try {
            jsonIOSem.acquire();
            Reader reader = new FileReader(f);
            ret = gson.fromJson(reader, User.class);
            reader.close();
            jsonIOSem.release();
        } catch (IOException e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Extracts the user from the the currently logged in user.
     * @return the resulting user object
     */
    public static User extractUser() {
        File f = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/userdata.json");

        if (!f.exists())
            throw new IllegalArgumentException("Provided file does not exist");

        User ret = null;
        Gson gson = new Gson();

        try {
            jsonIOSem.acquire();
            Reader reader = new FileReader(f);
            ret = gson.fromJson(reader, User.class);
            reader.close();
            jsonIOSem.release();
        } catch (IOException e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Extracts the requested data from the provided json file
     * @param f the json file to extract data from
     * @param name the data to extract from the file
     * @return the requested data
     */
    public static String extractUserData(File f, String name) {
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");
        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");

        return extractUserData(extractUser(f), name);
    }

    /**
     * Gets the requested data from the currently logged in user.
     * This method exists purely for legacy calls such as getUserData("foreground").
     * Ideally the call should be extractUser().getForeground()
     * @param name the ID of the data we want to obtain
     * @return the resulting data
     */
    public static String getUserData(String name) {
        String retData = "";
        String defaultValue = "";

        try {
            if (ConsoleFrame.getConsoleFrame().getUUID() == null)
                throw new IllegalArgumentException("UUID not yet set");
            File userJsonFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID()
                    + "/userdata.json");

            if (!userJsonFile.exists())
                throw new IllegalArgumentException("userdata.json does not exist");

            //find default value as a fail safe
            for (Preference pref : GenesisShare.getPrefs()) {
                if (pref.getID().equalsIgnoreCase(name)) {
                    defaultValue = pref.getDefaultValue();
                    break;
                }
            }

            User user = extractUser(userJsonFile);
            retData = extractUserData(user, name);

        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        } finally {
            return retData != null ? retData : defaultValue;
        }
    }

    /**
     * Assuming the corresponding getter and setter functions exist in User.java,
     * this method will call the getter method that matches the provided data.
     * This method exists purely for legacy calls such as extractUserData("font")
     * Ideally this method should be done away with if possible, perhaps adding a default function
     * o the {@code Preference} object could lead to a new path of thinking about user prefs/data.
     * @param u the initialized user containing the data we want to obtain
     * @param name the data id for which to return
     * @return the requested data
     */
    public static String extractUserData(User u, String name) {
        if (u == null || u.getClass() == null || u.getClass().getMethods() == null)
            throw new IllegalArgumentException("Something is null :/");

        String ret = null;

        try {
            for (Method m : u.getClass().getMethods()) {
                if (m.getName().startsWith("get")
                        && m.getParameterTypes().length == 0
                        && m.getName().toLowerCase().contains(name.toLowerCase())) {
                    final Object r = m.invoke(u);
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

    /**
     * Determines whether the provided uuid is already logged in
     * @param uuid the uuid to test for
     * @return boolean describing whether or not the provided uuid assocaited with a cyder user is currently logged in
     */
    public static boolean isLoggedIn(String uuid) {
        boolean ret = false;

        File userJsonFile = new File("dynamic/users/" + uuid + "/userdata.json");

        //should be impossible to not exists but I'll still check it regardless
        if (userJsonFile.exists()) {
            User u = extractUser(userJsonFile);
            ret = u.isLoggedin().equalsIgnoreCase("1");
        }

        return ret;
    }

    /**
     * For all the user's in the users/ dir, sets their loggedin data
     * to what it actually is regardless of what it says
     */
    public static void fixLoggedInValues() {
        try {
            File usersDir = new File("dynamic/users");

            if (usersDir.exists()) {
                File[] users = usersDir.listFiles();

                if (users.length > 0) {
                    for (File userFile : users) {
                        File userJsonFile = new File(userFile.getAbsolutePath() + "/userdata.json");
                        String currentUUID = StringUtil.getFilename(userJsonFile.getParentFile().getName());

                        //what we'll write to this json file
                        String loggedIn = "0";

                        File logsDir = new File("logs");

                        if (logsDir.exists()) {
                            File[] logs = logsDir.listFiles();

                            //we've started a Cyder instance already so there will always be one
                            if (logs.length > 1) {
                                //loop through logs backwards
                                for (int i = logs.length - 2; i >= 0 ; i--) {
                                    BufferedReader logReader = new BufferedReader(new FileReader(logs[i]));
                                    String line;
                                    String lineBeforeNull = "";
                                    boolean lastLoggedInuser = false;
                                    boolean breakAfter = false;

                                    while ((line = logReader.readLine()) != null) {
                                        if (line.contains("STD LOGIN") || line.contains("AUTOCYPHER PASS")){
                                            //this user we're on now was the last logged in if we get to the end of the file
                                            // and the last login tag we find has their uuid associated wit hit
                                            lastLoggedInuser = line.contains(currentUUID);
                                            breakAfter = true;
                                        }

                                        lineBeforeNull = line;
                                    }

                                    //now we have the last line as well so check if it contains the tags
                                    if (!(lineBeforeNull.contains("[EOL]") || lineBeforeNull.contains("EXTERNAL STOP"))) {
                                        //they're logged in then since this is the last time this user was logged in
                                        // and that log doesn't conclude properly
                                        loggedIn = "1";
                                    }

                                    if (breakAfter)
                                        break;
                                }
                            }
                        }

                        setUserData(userJsonFile,"loggedin", loggedIn);
                    }
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Checks whether or not the given name/pass combo is valid and if so, sets the
     * ConsoleFrame UUID to it
     * @param name the username given
     * @param hashedPass the already once SHA256 hashed password
     * @return whether or not the name/pass combo was valid
     */
    public static boolean checkPassword(String name, String hashedPass) {
        boolean ret = false;

        try {
            IOUtil.cleanUsers();

            hashedPass = SecurityUtil.toHexString(SecurityUtil.getSHA256(hashedPass.toCharArray()));

            //get all users
            File[] UUIDs = new File("dynamic/users").listFiles();
            LinkedList<File> userDataFiles = new LinkedList<>();

            //get all valid users
            for (File user : UUIDs) {
                userDataFiles.add(new File(user.getAbsolutePath() + "/userdata.json"));
            }

            //loop through all users and extract the name and password fields
            for (int i = 0 ; i < userDataFiles.size() ; i++) {
                User user = extractUser(userDataFiles.get(i));

                //if it's the one we're looking for, set consoel UUID, free resources, and return true
                if (name.equalsIgnoreCase(user.getName()) && hashedPass.equals(user.getPass())) {
                    ConsoleFrame.getConsoleFrame().setUUID(UUIDs[i].getName());
                    ret = true;
                }
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return ret;
    }

    /**
     * @return a user object with all of the default values found in {@code GenesisShare}
     */
    public static User getDefaultUser() {
        User ret = new User();

        //for all the preferences
        for (Preference pref : GenesisShare.getPrefs()) {
            //get all methods of user
            for (Method m : ret.getClass().getMethods()) {
                //make sure it's a setter with one parameter
                if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
                    //parse away set from method name and find default preference from list above
                    String methodName = m.getName().replace("set", "");

                    //find default value to match
                    if (methodName.equalsIgnoreCase(pref.getID())) {
                        try {
                            Class<?> castTo = m.getParameterTypes()[0];
                            if (castTo.isPrimitive()) {
                                m.invoke(ret, pref.getDefaultValue());
                            } else {
                                m.invoke(ret, castTo.cast(pref.getDefaultValue()));
                            }

                            //we've invoked this setter with the preference so next preference
                            break;
                        } catch (Exception e) {
                            // :/ not sure what happened here
                            ErrorHandler.silentHandle(e);
                        }
                    }
                }
            }
        }

        //exernal things stored in a user aside from preferences
        ret.setExecutables(null);

        return ret;
    }

    /**
     * Injects new preferences and their default values into an old json if it is found to not contain all the required user data.
     * @param f the file to check for corrections
     */
    public static void updateOldJson(File f) {
        if (!StringUtil.getExtension(f).equals(".json")) {
            throw new IllegalArgumentException("Provided file is not a json");
        } else if (!StringUtil.getFilename(f).equals("userdata")) {
            throw new IllegalArgumentException("Provided file is not a userdata file");
        }

        try {
            //read and write data so that it's all on one line
            jsonIOSem.acquire();

            Gson gson = new Gson();

            Reader reader = new FileReader(f);
            User writeBack = gson.fromJson(reader, User.class);
            reader.close();

            Writer writer = new FileWriter(f);
            gson.toJson(writeBack, writer);
            writer.close();

            jsonIOSem.release();

            //get contents of json (single line)
            jsonIOSem.acquire();
            BufferedReader jsonReader = new BufferedReader(new FileReader(f));
            String masterJson = jsonReader.readLine();
            jsonReader.close();
            jsonIOSem.release();

            //remove closing curly brace
            masterJson = masterJson.substring(0 ,masterJson.length() - 1);

            //keep track of if we injected anything
            LinkedList<String> injections = new LinkedList<>();

            //loop through default perferences
            for (Preference pref : GenesisShare.getPrefs()) {
                //old json detected and we found a pref that doesn't exist
                if (!masterJson.toLowerCase().contains(pref.getID().toLowerCase())) {
                    //inject into json
                    StringBuilder injectionBuilder = new StringBuilder();
                    injectionBuilder.append("\"");
                    injectionBuilder.append(pref.getID());
                    injectionBuilder.append("\":\"");
                    injectionBuilder.append(pref.getDefaultValue());
                    injectionBuilder.append("\",");
                    //adding a trailing comma is fine since it will be parsed away by gson upon
                    // serialization of a user object
                    injections.add(pref.getID() + "=" + pref.getDefaultValue());
                }
            }

            //add closing curly brace back in
            masterJson += "}";

            //write back to file
            jsonIOSem.acquire();
            BufferedWriter jsonWriter = new BufferedWriter(new FileWriter(f,false));
            jsonWriter.write(masterJson);
            jsonWriter.close();
            jsonIOSem.release();

            if (!injections.isEmpty()) {
                StringBuilder appendBuilder = new StringBuilder();

                for (int i = 0 ; i < injections.size() ; i++) {
                    appendBuilder.append(injections.get(i));

                    if (i != injections.size() - 1)
                        appendBuilder.append(", ");
                }

                //log the injection
                SessionHandler.log(SessionHandler.Tag.ACTION, "User " + f.getParentFile().getName() +
                        " was found to have an outdated userdata.json; preference injection " +
                        "performed on the following: [" + appendBuilder + "]");
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
