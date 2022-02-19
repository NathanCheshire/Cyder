package cyder.utilities;

import com.google.gson.Gson;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.PopupHandler;
import cyder.ui.ConsoleFrame;
import cyder.user.Preferences;
import cyder.user.User;
import cyder.user.UserFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * Utilities regarding a user, their json file, and IO to/from that json file.
 */
public class UserUtil {
    /**
     * Instantiation of util method not allowed.
     */
    private UserUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    //the semaphore to use when reading or writing userdata
    private static final Semaphore userIOSemaphore = new Semaphore(1);

    /**
     * The user used for IO to the user's json every {@link UserUtil#IO_TIMEOUT}
     */
    private static User cyderUser = buildDefaultUser();

    /**
     * The corresponding file for cyderUser.
     */
    private static File cyderUserFile;

    /**
     * The timeout between writes to the user json file in ms.
     */
    public static final int IO_TIMEOUT = 3000;

    /**
     * Returns the semaphore used for IO to/from the user's JSON file.
     *
     * @return the semaphore used for IO to/from the user's JSON file
     */
    public static Semaphore getUserIOSemaphore() {
        return userIOSemaphore;
    }

    /**
     * Blocks any future user IO by acquiring the semaphore and never releasing it.
     * This method blocks until the IO semaphore can be acquired.
     */
    public static synchronized void blockFutureIO() {
        try {
            userIOSemaphore.acquire();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    //todo utilize every IO_TIMEOUT ms and block IO, or figure out way to update when cyderUser is updated
    /**
     * Refreshes the current user's json with the current state of {@link UserUtil#cyderUser}.
     */
    public static synchronized void writeUser() {
        setUserData(cyderUserFile, cyderUser);
    }

    /**
     * Refreshes {@link UserUtil#cyderUser} with the data stored in the current user json.
     */
    public static synchronized void readUser() {
        cyderUser = extractUser(cyderUserFile);
    }

    /**
     * Sets the given user to the current Cyder user.
     *
     * @param u the user to set as the current Cyder user
     */
    public static void setCyderUser(User u) {
        cyderUser = u;
    }

    /**
     * Returns the current cyderUser.
     *
     * @return the resulting user object
     */
    public static User extractUser() {
        return cyderUser;
    }

    /**
     * Sets the key for the current user to the provided data.
     *
     * @param name the name of the data to set
     * @param value the new value
     */
    public static void setUserData(String name, String value) {
        File f = new File(OSUtil.buildPath("dynamic","users",
                ConsoleFrame.getConsoleFrame().getUUID(), UserFile.USERDATA.getName()));

        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");

        try {
            for (Method m : cyderUser.getClass().getMethods()) {
                if (m.getName().startsWith("set")
                        && m.getParameterTypes().length == 1
                        && m.getName().replace("set","").equalsIgnoreCase(name)) {
                    m.invoke(cyderUser, value);
                    break;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Writes the provided user to the provided file.
     *
     * @param f the file to write to
     * @param u the user object to write to the file
     */
    public static void setUserData(File f, User u) {
        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");
        if (!FileUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");

        Gson gson = new Gson();

        try  {
            FileWriter writer = new FileWriter(f);
            userIOSemaphore.acquire();
            gson.toJson(u, writer);
            writer.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            userIOSemaphore.release();
        }
    }

    /**
     * Function called upon UUID being set for consoleFrame to attempt to fix any user data
     * in case it was corrupted. If we fail to correct any corrupted data, then we corrupt the user and exit
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") /* making directories */
    public static void fixUser() {
        String UUID = ConsoleFrame.getConsoleFrame().getUUID();

        if (UUID == null)
            return;

        File userBackgroundsFile = new File(OSUtil.buildPath("dynamic","users",
                UUID, UserFile.BACKGROUNDS.getName()));
        File userMusicFile = new File(OSUtil.buildPath("dynamic","users",
                UUID, UserFile.MUSIC.getName()));
        File userJsonFile = new File(OSUtil.buildPath("dynamic","users",
                UUID, UserFile.USERDATA.getName()));

        if (!userBackgroundsFile.exists())
            userBackgroundsFile.mkdir();

        if (!userMusicFile.exists())
            userMusicFile.mkdir();

        if (!userJsonFile.exists()) {
            userJsonDeleted(UUID);
            return;
        }

        User user = extractUser(userJsonFile);

        try {
            //this handles data who's ID is still there
            for (Method getterMethod : user.getClass().getMethods()) {
                if (getterMethod.getName().startsWith("get")
                        && getterMethod.getParameterTypes().length == 0) {
                    //object returned by current getter
                    final Object getterRet = getterMethod.invoke(user);
                    if (getterRet == null || (getterRet instanceof String && ((String) getterRet).length() == 0)) {
                        //fatal data that results in the user being corrupted if it is corrupted
                        if (getterMethod.getName().toLowerCase().contains("pass") ||
                            getterMethod.getName().toLowerCase().contains("name")) {
                            userJsonDeleted(UUID);
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

                                    //find corresponding default vale
                                    for (Preferences.Preference pref : Preferences.getPreferences()) {
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
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Attempts to read backgrounds that Cyder would use for a user.
     * If failure, the image is corrupted, so we delete it in the calling function.
     */
    public static void deleteInvalidBackgrounds() {
        try {
            //acquire sem so that any user requested exit will not corrupt the background
            getUserIOSemaphore().acquire();

            for (File f : new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Backgrounds").listFiles()) {
                boolean valid = true;

                try (FileInputStream fi = new FileInputStream(f)) {
                    BufferedImage sourceImg = ImageIO.read(fi);
                    //noinspection unused, need access to ensure image is valid
                    int w = sourceImg.getWidth();
                } catch (Exception e) {
                    valid = false;
                    ExceptionHandler.silentHandle(e);
                }

                if (!valid) {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
            }

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            getUserIOSemaphore().release();
        }
    }

    /**
     * Extracts the user from the provided json file.
     *
     * @param f the json file to extract a user object from
     * @return the resulting user object
     */
    public static User extractUser(File f) {
        if (!f.exists())
            throw new IllegalArgumentException("Provided file does not exist");
        if (!FileUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("Provided file is not a json");

        User ret = null;
        Gson gson = new Gson();

        try {
            userIOSemaphore.acquire();
            Reader reader = new FileReader(f);
            ret = gson.fromJson(reader, User.class);
            reader.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            userIOSemaphore.release();
        }

        return ret;
    }

    /**
     * Gets the requested data from the currently logged-in user.
     * This method exists purely for legacy calls such as getUserData("foreground").
     * Ideally the call should be extractUser().getForeground().
     *
     * @param name the ID of the data we want to obtain
     * @return the resulting data
     */
    public static String getUserData(String name) {
        return extractUserData(extractUser(), name);
    }

    /**
     * Assuming the corresponding getter function exist in User.java,
     * this method will call the getter method that matches the provided data.
     * This method exists purely for legacy calls such as extractUserData("font")
     * Ideally this method should be done away with if possible, perhaps adding a default function
     * o the {@code Preference} object could lead to a new path of thinking about user prefs/data.
     *
     * @param u the initialized user containing the data we want to obtain
     * @param name the data id for which to return
     * @return the requested data
     */
    public static String extractUserData(User u, String name) {
        if (u == null || u.getClass() == null || u.getClass().getMethods() == null)
            throw new IllegalArgumentException("Something is null :/\nUser: " + u + "\nName: " + name);

        String ret = null;

        //log handler calls that aren't spammed
        if (!IOUtil.ignoreLogData(name))
            Logger.log(Logger.Tag.SYSTEM_IO, "Userdata requested: " + name);

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
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * @return a user object with all the default
     * {@link Preferences} found in {@code GenesisShare}.
     */
    public static User buildDefaultUser() {
        User ret = new User();

        //for all the preferences
        for (Preferences.Preference pref : Preferences.getPreferences()) {
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
                            ExceptionHandler.silentHandle(e);
                        }
                    }
                }
            }
        }

        //external things stored in a user aside from preferences
        ret.setExecutables(null);

        return ret;
    }

    /**
     * Injects new preferences and their default values into an old json
     * if it is found to not contain all the required user data.
     *
     * @param f the file to check for corrections
     */
    public static boolean updateOldJson(File f) {
        if (!FileUtil.getExtension(f).equals(".json")) {
            throw new IllegalArgumentException("Provided file is not a json");
        } else if (!FileUtil.getFilename(f).equalsIgnoreCase(FileUtil.getFilename(UserFile.USERDATA.getName()))) {
            throw new IllegalArgumentException("Provided file is not a userdata file");
        }

        boolean ret = true;

        try {
            // acquire sem
            userIOSemaphore.acquire();

            //gson obj
            Gson gson = new Gson();

            //read into the object if parsable
            Reader reader = new FileReader(f);
            User userObj;

            try {
                userObj = gson.fromJson(reader, User.class);
            } catch (Exception ignored) {
                //couldn't be parsed so delete it
                userIOSemaphore.release();
                reader.close();
                return false;
            }

            //validate all fields, if anything isn't there, delete the json so this user is ignored

            //so google is literally NO help in searching "gson tell if field was ignored"
            // so let's just find all getters and call them and see if the thing returned is null-like
            // if so, we're fucked so delete the file

            if (userObj == null) {
                userIOSemaphore.release();
                reader.close();
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

                        if (value == null || value.equalsIgnoreCase("null")) {
                            //this thing doesn't exist so return false where the IOUtil method will delete the file
                            userIOSemaphore.release();
                            reader.close();
                            return false;
                        }
                    }
                }
            }

            //write back so that it's a singular line to prepare for pref injection
            Writer writer = new FileWriter(f);
            gson.toJson(userObj, writer);
            writer.close();

            //read contents into master String
            BufferedReader jsonReader = new BufferedReader(new FileReader(f));
            StringBuilder masterJson = new StringBuilder(jsonReader.readLine());
            jsonReader.close();

            //make sure contents are not null-like
            if (masterJson == null || masterJson.toString().trim().length() == 0 || masterJson.toString().equalsIgnoreCase("null")) {
                userIOSemaphore.release();
                reader.close();
                writer.close();
                jsonReader.close();
                return false;
            }

            //remove closing curly brace
            masterJson = new StringBuilder(masterJson.substring(0, masterJson.length() - 1));

            //keep track of if we injected anything
            LinkedList<String> injections = new LinkedList<>();

            //loop through default preferences
            for (Preferences.Preference pref : Preferences.getPreferences()) {
                //old json detected, and we found a pref that doesn't exist
                if (!masterJson.toString().toLowerCase().contains(pref.getID().toLowerCase())) {
                    //inject into json
                    String injectionBuilder = "\"" +
                            pref.getID() +
                            "\":\"" +
                            pref.getDefaultValue() +

                            //adding a trailing comma is fine since it will be parsed away by gson upon
                            // serialization of a user object
                            "\",";

                    masterJson.append(injectionBuilder);

                    // add what was injected so that we can log it
                    injections.add(pref.getID() + "=" + pref.getDefaultValue());
                }
            }

            //add closing curly brace back in
            masterJson.append("}");

            //write back to file
            BufferedWriter jsonWriter = new BufferedWriter(new FileWriter(f,false));
            jsonWriter.write(masterJson.toString());
            jsonWriter.close();

            if (!injections.isEmpty()) {
                StringBuilder appendBuilder = new StringBuilder();

                for (int i = 0 ; i < injections.size() ; i++) {
                    appendBuilder.append(injections.get(i));

                    if (i != injections.size() - 1)
                        appendBuilder.append(", ");
                }

                //log the injection
                Logger.log(Logger.Tag.ACTION,
                        "User " + f.getParentFile().getName() +
                        " was found to have an outdated userdata file; preference injection " +
                        "was attempted on the following: [" + appendBuilder + "]");
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            ret = false;
        } finally {
            userIOSemaphore.release();
        }

        return ret;
    }

    /**
     * After a user's json file was deleted due to it being un-parsable, null, or any other reason,
     * this method informs the user that a user was corrupted and attempts to tell the user
     * which user it was by listing the files associated with the corrupted user.
     *
     * This method should be utilized anywhere a userdata file is deleted during Cyder runtime.
     *
     * @param UUID the uuid of the corrupted user
     */
    public static void userJsonDeleted(String UUID) {
        try {
            //create parent directory
            File userDir = new File(OSUtil.buildPath("dynamic","users",
                    UUID));
            File userJson = new File(OSUtil.buildPath("dynamic","users",
                    UUID, UserFile.USERDATA.getName()));

            //delete the json if it still exists for some reason
            if (userJson.exists())
                //noinspection ResultOfMethodCallIgnored
                userJson.delete();

            //if there's nothing left in the dir, delete the whole folder
            if (userDir.listFiles().length == 0)
                OSUtil.deleteFolder(userDir);
            else {
                //otherwise, we need to figure out all the file names in each sub-dir, not recursive, and inform the user
                // that a json was deleted and tell them which files are remaining

                String path = "Cyder/dynamic/" + UUID;
                String informString = "Unfortunately a user's data file was corrupted and had to be deleted. " +
                        "The following files still exists and are associated with the user at the following path:<br/>" + path + "<br/>Files:";

                LinkedList<String> filenames = new LinkedList<>();

                for (File f : userDir.listFiles()) {
                    if (f.isFile()) {
                        filenames.add(FileUtil.getFilename(f));
                    } else if (f.isDirectory()) {
                        for (File file : f.listFiles()) {
                            filenames.add(FileUtil.getFilename(file));
                        }
                    }
                }

                if (filenames.size() == 0) {
                    informString += "No files found associated with the corrupted user";
                } else {
                    StringBuilder sb = new StringBuilder();

                    for (String filename : filenames) {
                        sb.append("<br/>").append(filename);
                    }

                    informString += sb;
                }

                //inform of message
                PopupHandler.inform(informString, "Userdata Corruption");

                //log the corruption
                Logger.log(Logger.Tag.CORRUPTION, "[Resulting Popup]\n" + informString);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Ensure all user files from {@link UserFile} are created.
     */
    public static void createUserFiles() {
        for (UserFile userFile : UserFile.getFiles()) {
            getUserFile(userFile.getName());
        }
    }

    /**
     * Returns the provided user file after creating it if it did not exist.
     *
     * @param userFile the user file to return a reference to
     * @return the provided user file
     */
    @SuppressWarnings("unused") /* for consistency purposes */
    public File getUserFile(UserFile userFile) {
        return getUserFile(userFile.getName());
    }

    /**
     * Returns the provided user file after creating it if it did not exist.
     *
     * @param fileName the file name of the user file to return a reference to
     * @return the provided user file
     * @throws IllegalArgumentException if the filename is not a standard enum
     * @throws RuntimeException if the file/directory fails to be created
     * @throws IllegalStateException if the consoleFrame uuid has not yet been set
     */
    public static File getUserFile(String fileName) {
        if (ConsoleFrame.getConsoleFrame().getUUID() == null)
            throw new IllegalStateException("ConsoleFrame UUID is not yet set");

        boolean valid = false;

        for (UserFile userFile : UserFile.getFiles()) {
            if (fileName.equalsIgnoreCase(userFile.getName())) {
                valid = true;
                break;
            }
        }

        if (!valid)
            throw new IllegalArgumentException("Provided userfile does not exists as standard enum type");

        File ret = new File(OSUtil.buildPath("dynamic",
                "users", ConsoleFrame.getConsoleFrame().getUUID(), fileName));

        if (!ret.exists()) {
            if (ret.mkdir()) {
                return ret;
            } else {
                throw new RuntimeException("Failed to create: " + fileName);
            }
        }

        return ret;
    }

    /**
     * Returns the number of users associated with Cyder.
     *
     * @return the number of users associated with Cyder
     */
    public static int getUserCount() {
        ArrayList<File> userJsons = new ArrayList<>();

        for (File user : new File(OSUtil.buildPath("dynamic","users")).listFiles()) {
            if (user.isDirectory()) {
                File json = new File(OSUtil.buildPath(user.getAbsolutePath(), UserFile.USERDATA.getName()));

                if (json.exists())
                    userJsons.add(json);
            }
        }

        return userJsons.size();
    }

    /**
     * Returns a list of valid uuids associated with Cyder users.
     *
     * @return a list of valid uuids associated with Cyder users
     */
    public static ArrayList<String> getUserUUIDs() {
        ArrayList<String> uuids = new ArrayList<>();

        for (File user : new File(OSUtil.buildPath("dynamic","users")).listFiles()) {
            File json = new File(OSUtil.buildPath(user.getAbsolutePath(), UserFile.USERDATA.getName()));

            if (json.exists())
                uuids.add(user.getName());
        }

        return uuids;
    }

    /**
     * Returns a list of valid user jsons associated with Cyder users.
     *
     * @return a list of valid user jsons associated with Cyder users
     */
    public static ArrayList<File> getUserJsons() {
        ArrayList<File> userFiles = new ArrayList<>();

        for (File user : new File(OSUtil.buildPath("dynamic","users")).listFiles()) {
            File json = new File(OSUtil.buildPath(user.getAbsolutePath(), UserFile.USERDATA.getName()));

            if (json.exists())
                userFiles.add(json);
        }

        return userFiles;
    }

    /**
     * For all users within dynamic/users, sets the loggedin key to 0.
     */
    public static void logoutAllUsers() {
        File usersDir = new File(OSUtil.buildPath("dynamic","users"));

        if (!usersDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            usersDir.mkdir();
            return;
        }

        File[] users = usersDir.listFiles();

        if (users.length == 0)
            throw new IllegalArgumentException("No users were found");

        for (File user : users) {
            File jsonFile = new File(OSUtil.buildPath(user.getAbsolutePath(), UserFile.USERDATA.getName()));

            if (jsonFile.exists() && !FileUtil.getFilename(jsonFile).equals(ConsoleFrame.getConsoleFrame().getUUID())) {
                User u = UserUtil.extractUser(jsonFile);
                u.setLoggedin("0");
                UserUtil.setUserData(jsonFile, u);
            }
        }
    }

    /**
     * Searches through the users/ directory and finds the first logged-in user.
     *
     * @return the uuid of the first logged-in user
     */
    public static Optional<String> getFirstLoggedInUser() {
        for (File userJSON : UserUtil.getUserJsons()) {
            if (UserUtil.extractUser(userJSON).getLoggedin().equals("1"))
                return Optional.of(FileUtil.getFilename(userJSON.getParentFile().getName()));
        }

        return Optional.empty();
    }

    /**
     * Creates the default background inside the user's Backgrounds/ directory.
     *
     * @param uuid the user's uuid to save the default background to
     * @return a reference to the file created
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") /* Creating a file result does not matter */
    public static File createDefaultBackground(String uuid) {
        //default background is creating an image gradient
        Image img = CyderIcons.defaultBackground.getImage();

        BufferedImage bi;

        //try to get default image that isn't bundled with Cyder
        try {
            bi = ImageIO.read(new URL("https://i.imgur.com/kniH8y9.png"));
        } catch (Exception e) {
            ExceptionHandler.handle(e);

            bi = new BufferedImage(img.getWidth(null),
                    img.getHeight(null),BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
        }

        File backgroundFile = new File(OSUtil.buildPath("dynamic","users",
                uuid, UserFile.BACKGROUNDS.getName(), "Default.png"));

        File backgroundFolder = new File(OSUtil.buildPath("dynamic","users",
                uuid, UserFile.BACKGROUNDS.getName()));

        try {
            if (!backgroundFolder.exists()) {
                backgroundFolder.createNewFile();
            }

            ImageIO.write(bi, "png", backgroundFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return backgroundFile;
    }
}
