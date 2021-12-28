package cyder.utilities;

import com.google.gson.Gson;
import cyder.genesis.GenesisShare;
import cyder.genesis.GenesisShare.Preference;
import cyder.genesis.Login;
import cyder.userobj.User;
import cyder.handlers.internal.ErrorHandler;
import cyder.handlers.internal.PopupHandler;
import cyder.handlers.internal.SessionHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            jsonIOSem.release();
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
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            jsonIOSem.release();
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
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            jsonIOSem.release();
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
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            jsonIOSem.release();
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

        if (!userJsonFile.exists()) {
            corruptedUser();
            return;
        }

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
                            corruptedUser();
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
        try {
            GenesisShare.getExitingSem().acquire();

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

        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            GenesisShare.getExitingSem().release();
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
        } catch (IOException e) {
            ErrorHandler.handle(e);
        } finally {
            jsonIOSem.release();
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
        } catch (IOException e) {
            ErrorHandler.handle(e);
        } finally {
            jsonIOSem.release();
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
        } catch (IOException e) {
            ErrorHandler.handle(e);
        } finally {
            jsonIOSem.release();
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
            //find default value as a fail safe
            for (Preference pref : GenesisShare.getPrefs()) {
                if (pref.getID().equalsIgnoreCase(name)) {
                    defaultValue = pref.getDefaultValue();
                    break;
                }
            }

            //data not set to return default
            if (ConsoleFrame.getConsoleFrame().getUUID() == null) {
                return defaultValue;
            }

            File userJsonFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID()
                    + "/userdata.json");

            if (!userJsonFile.exists())
                throw new IllegalArgumentException("userdata.json does not exist");

            User user = extractUser(userJsonFile);
            retData = extractUserData(user, name);
            return retData;

        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }

        return defaultValue;
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
            throw new IllegalArgumentException("Something is null :/\nUser: " + u + "\nName: " + name);

        String ret = null;

        //log handler calls that aren't spammed
        if (!ignoreLogData(name))
            SessionHandler.log(SessionHandler.Tag.SYSTEM_IO, "Userdata requested: " + name);

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

    //read once on compile time
    private static LinkedList<String> ignoreDatas = IOUtil.getSystemData().getIgnoreLogData();

    /**
     * @param dataid the id of the data we wish to obtain from userdata.json
     * @return boolean detemrining whether or not this data should be ignored by the SessionLogger
     */
    public static boolean ignoreLogData(String dataid) {
       return ignoreDatas.contains(dataid);
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

                        if (!userJsonFile.exists()) {
                            continue;
                        }

                        String currentUUID = StringUtil.getFilename(userJsonFile.getParentFile().getName());

                        //what we'll write to this json file
                        String loggedIn = "0";

                        File masterLogs = new File("logs");

                        if (masterLogs.exists()) {
                            File[] logsDirs = masterLogs.listFiles();

                            for (File logsDir : logsDirs) {
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
            hashedPass = SecurityUtil.toHexString(SecurityUtil.getSHA256(hashedPass.toCharArray()));

            //get all users
            File[] UUIDs = new File("dynamic/users").listFiles();
            LinkedList<File> userDataFiles = new LinkedList<>();

            //get all valid users
            for (File user : UUIDs) {
                File json = new File(user.getAbsolutePath() + "/userdata.json");

                if (json.exists())
                    userDataFiles.add(json);
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
    public static boolean updateOldJson(File f) {
        if (!StringUtil.getExtension(f).equals(".json")) {
            throw new IllegalArgumentException("Provided file is not a json");
        } else if (!StringUtil.getFilename(f).equals("userdata")) {
            throw new IllegalArgumentException("Provided file is not a userdata file");
        }

        boolean ret = true;

        try {
            //aquire sem
            jsonIOSem.acquire();

            //init IO for json
            Reader reader = null;
            Writer writer = null;
            BufferedReader jsonReader = null;
            BufferedWriter jsonWriter = null;

            //gson obj
            Gson gson = new Gson();

            //read into the object if parsable
            reader = new FileReader(f);
            User userObj = null;

            try {
                userObj = gson.fromJson(reader, User.class);
            } catch (Exception ignored) {
                //couldn't be parsed so delete it
                jsonIOSem.release();
                reader.close();
                writer.close();
                jsonReader.close();
                jsonWriter.close();
                return false;
            }

            //validate all fields, if anything isn't there, delete the json so this user is ignored

            //so google is literally NO help in searching "gson tell if field was ignored"
            // so let's just find all getters and call them and see if the thing returned is null-like
            // if so, we're fucked so delete the file

            if (userObj == null) {
                jsonIOSem.release();
                reader.close();
                writer.close();
                jsonReader.close();
                jsonWriter.close();
                return false;
            }

            //for all methods
            for (Method m : userObj.getClass().getMethods()) {
                //get the getter method
                if (m.getName().toLowerCase().contains("get") && m.getParameterCount() == 0) {
                    //all getters return strings for user objects so invoke the method
                    Object object = m.invoke(userObj);

                    if (object instanceof String) {
                        String value = (String) object;

                        if (value == null || value.trim().length() == 0 || value.equalsIgnoreCase("null")) {
                            //this thing doesn't exist so return false where the IOUtil method will delete the file
                            jsonIOSem.release();
                            reader.close();
                            writer.close();
                            jsonReader.close();
                            jsonWriter.close();
                            return false;
                        }
                    }
                }
            }

            //write back so that it's a singular line to prepare for pref injection
            writer = new FileWriter(f);
            gson.toJson(userObj, writer);
            writer.close();

            //read contents into master String
            jsonReader = new BufferedReader(new FileReader(f));
            String masterJson = jsonReader.readLine();
            jsonReader.close();

            //make sure contents are not null-like
            if (masterJson == null || masterJson.trim().length() == 0 || masterJson.equalsIgnoreCase("null")) {
                jsonIOSem.release();
                reader.close();
                writer.close();
                jsonReader.close();
                jsonWriter.close();
                return false;
            }

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
            jsonWriter = new BufferedWriter(new FileWriter(f,false));
            jsonWriter.write(masterJson);
            jsonWriter.close();

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
                        "was attempted on the following: [" + appendBuilder + "]");
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
            ret = false;
        } finally {
            jsonIOSem.release();
        }

        return ret;
    }

    /**
     * Used to log out all users before logging in a new user
     */
    public static void logoutAllUsers() {
        File usersDir = new File("dynamic/users");

        if (!usersDir.exists()) {
            usersDir.mkdir();
            return;
        }

        File[] users = usersDir.listFiles();

        if (users.length == 0)
            throw new IllegalArgumentException("No users were found");

        for (File user : users) {
            File jsonFile = new File(user.getAbsolutePath() + "/userdata.json");

            if (jsonFile.exists() && !StringUtil.getFilename(jsonFile).equals(ConsoleFrame.getConsoleFrame().getUUID()))
                setUserData(jsonFile, "loggedin","0");
        }
    }

    /**
     * If a user becomes corrupted for any reason which may be determined any way we choose,
     * this method will aquire the exiting semaphore, dispose of all frames, and attempt to
     * zip any user data aside from userdata.json
     */

    //todo test this
    public static void corruptedUser() {
        try {
            GenesisShare.suspendFrameChecker();

            //close all open frames
            Frame[] frames = Frame.getFrames();
            for (Frame f : frames) {
                if (f instanceof CyderFrame) {
                    ((CyderFrame) f).dispose(true);
                } else {
                    f.dispose();
                }
            }

            File mainZipFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID());

            //confirmed that the user was corrupted so we inform the user
            PopupHandler.inform("Sorry, " + SystemUtil.getWindowsUsername() + ", but your user was corrupted. " +
                    "Your data has been saved, zipped, and placed in your Downloads folder", "Corrupted User :(");

            //delete the stuff we don't care about
            for (File f : mainZipFile.listFiles()) {
                //currently only deleting userdata.json if it exists
                if (f.getName().equalsIgnoreCase("userdata.json"))
                    f.delete();
            }

            //zip the remaining user data
            String sourceFile = mainZipFile.getAbsolutePath();
            String fileName = "C:/Users/" + SystemUtil.getWindowsUsername() +
                    "/Downloads/Cyder_Corrupted_Userdata_" + TimeUtil.errorTime() + ".zip";
            FileOutputStream fos = new FileOutputStream(fileName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(sourceFile);
            zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();

            SessionHandler.log(SessionHandler.Tag.CORRUPTION, fileName);

            //delete the folder we just zipped since it's a duplicate
            SystemUtil.deleteFolder(mainZipFile);

            //all frames should be gone so show login
            Login.showGUI();
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }
    }

    /**
     * Zips the provided file with the given name using hte provided ZOS
     * @param fileToZip the file/dir to zip
     * @param fileName the name of the resulting file (path included)
     * @param zipOut the Zip Output Stream
     */
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) {
        try {
            if (fileToZip.isHidden())
                return;

            if (fileToZip.isDirectory()) {
                if (fileName.endsWith("/")) {
                    zipOut.putNextEntry(new ZipEntry(fileName));
                } else {
                    zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                }
                zipOut.closeEntry();

                File[] children = fileToZip.listFiles();
                for (File childFile : children)
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);

                return;
            }

            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);

            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;

            while ((length = fis.read(bytes)) >= 0)
                zipOut.write(bytes, 0, length);

            fis.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
