package cyder.utilities;

import com.google.gson.Gson;
import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.genobjects.Preference;
import cyder.genobjects.User;
import cyder.handler.SessionLogger;
import cyder.testing.DebugConsole;
import cyder.ui.ConsoleFrame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.util.LinkedList;

public class UserUtil {
    public static void setUserData(String name, String value) {
       if (ConsoleFrame.getConsoleFrame().getUUID() == null)
           throw new IllegalArgumentException("UUID is null");

       File userJsonFile = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID()
                                    + "/userdata.json");

       if (!userJsonFile.exists())
           throw new IllegalArgumentException("userdata.json does not exist");

       setUserData(userJsonFile, name, value);
    }

    public static void setUserData(User user, String name, String value) {
        try {
            for (Method m : user.getClass().getMethods()) {
                if (m.getName().startsWith("set")
                        && m.getParameterTypes().length == 1
                        && m.getName().toLowerCase().contains(name.toLowerCase())) {
                    m.invoke(user, value);
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static <T> void setUserData(File f, String name, T value) {
        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");

        User user = extractUser(f);

        try {
            for (Method m : user.getClass().getMethods()) {
                if (m.getName().startsWith("set")
                        && m.getParameterTypes().length == 1
                        && m.getName().toLowerCase().contains(name.toLowerCase())) {
                    m.invoke(user, value);
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(f)) {
            gson.toJson(user, writer);
        } catch (IOException e) {
            ErrorHandler.handle(e);
        }
    }

    public static <T> void setUserData(String name, T value) {
        File f = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/userdata.json");

        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");

        User user = extractUser(f);

        try {
            for (Method m : user.getClass().getMethods()) {
                if (m.getName().startsWith("set")
                        && m.getParameterTypes().length == 1
                        && m.getName().toLowerCase().contains(name.toLowerCase())) {
                    m.invoke(user, value);
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(f)) {
            gson.toJson(user, writer);
        } catch (IOException e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Writes the provided user after being converted to JSON format to the provided file.
     * @param f - the file to write to
     * @param u - the user object to write to the file
     */
    public static void setUserData(File f, User u) {
        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");

        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(f)) {
            gson.toJson(u, writer);
        } catch (IOException e) {
            ErrorHandler.handle(e);
        }
    }

    public static void setUserData(User u) {
        File f = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/userdata.json");

        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");

        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(f)) {
            gson.toJson(u, writer);
        } catch (IOException e) {
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

        File userBackgroundsFile = new File("users/" + UUID + "/Backgrounds");
        File userMusicFile = new File("users/" + UUID + "/Music");
        File userJsonFile = new File("users/" + UUID + "/userdata.json");

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

    public static void fixBackgrounds() {
        for (File f : new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Backgrounds").listFiles()) {
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
     * @param f - the json file to extract a user object from
     * @return - the resulting user object
     */
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
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Extracts the user from the provided json file
     * @param UUID - the uuid if the user we want to obtain
     * @return - the resulting user object
     */
    public static User extractUser(String UUID) {
        File f = new File("users/" + UUID + "/userdata.json");

        if (!f.exists())
            throw new IllegalArgumentException("Provided file does not exist");

        User ret = null;
        Gson gson = new Gson();

        try (Reader reader = new FileReader(f)) {
            ret = gson.fromJson(reader, User.class);
        } catch (IOException e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Extracts the user from the the currently logged in user.
     * @return - the resulting user object
     */
    public static User extractUser() {
        File f = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/userdata.json");

        if (!f.exists())
            throw new IllegalArgumentException("Provided file does not exist");

        User ret = null;
        Gson gson = new Gson();

        try (Reader reader = new FileReader(f)) {
            ret = gson.fromJson(reader, User.class);
        } catch (IOException e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Extracts the requested data from the provided json file
     * @param f - the json file to extract data from
     * @param data - the data type to extract from the file
     * @return - the requested data
     */
    public static String extractUserData(File f, String data) {
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");
        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");

        return extractUserData(extractUser(f), data);
    }

    /**
     * Gets the requested data from the currently logged in user.
     * This method exists purely for legacy calls such as getUserData("foreground").
     * Ideally the call should be extractUser().getForeground()
     * @param name - the ID of the data we want to obtain
     * @return - the resulting data
     */
    public static String getUserData(String name) {
        if (ConsoleFrame.getConsoleFrame().getUUID() == null)
            throw new IllegalArgumentException("UUID not yet set");
        File userJsonFile = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID()
                + "/userdata.json");

        if (!userJsonFile.exists())
            throw new IllegalArgumentException("userdata.json does not exist");

        User user = extractUser(userJsonFile);
        String retData = extractUserData(user, name);

        String defaultValue = "";

        //find default value as a fail safe
        for (Preference pref : GenesisShare.getPrefs()) {
            if (pref.getID().equalsIgnoreCase(name)) {
                defaultValue = pref.getDefaultValue();
                break;
            }
        }

        return retData != null ? retData : defaultValue;
    }

    /**
     * Assuming the corresponding getter and setter functions exist in User.java,
     * this method will call the getter method that matches the provided data.
     * This method exists purely for legacy calls such as extractUserData("font")
     * @param u - the initialized user containing the data we want to obtain
     * @param data - the data id for which to return
     * @return - the requested data
     */
    public static String extractUserData(User u, String data) {
        String ret = null;

        try {
            for (Method m : u.getClass().getMethods()) {
                if (m.getName().startsWith("get")
                        && m.getParameterTypes().length == 0
                        && m.getName().toLowerCase().contains(data.toLowerCase())) {
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

    public static boolean checkPassword(String name, String hashedPass) {
        try {
            IOUtil.cleanUsers();

            hashedPass = SecurityUtil.toHexString(SecurityUtil.getSHA256(hashedPass.toCharArray()));

            //get all users
            File[] UUIDs = new File("users").listFiles();
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
                    return true;
                }
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return false;
    }

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
                            m.invoke(ret, pref.getDefaultValue());
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

    public static void updateOldJson(File f) {
        if (!StringUtil.getExtension(f).equals(".json")) {
            throw new IllegalArgumentException("Provided file is not a json");
        } else if (!StringUtil.getFilename(f).equals("userdata")) {
            throw new IllegalArgumentException("Provided file is not a userdata file");
        }

        try {
            //get contents of json
            BufferedReader jsonReader = new BufferedReader(new FileReader(f));
            String masterJson = jsonReader.readLine();

            //remove closing curly brace
            masterJson = masterJson.substring(0 ,masterJson.length() - 1);

            //keep track of if we injected anything
            boolean injectionPerformed = false;

            //loop through default perferences
            for (Preference pref : GenesisShare.getPrefs()) {
                //old json detected and we found a pref that doesn't exist
                if (!masterJson.contains(pref.getID())) {
                    //inject into json
                    StringBuilder injectionBuilder = new StringBuilder();
                    injectionBuilder.append("\"");
                    injectionBuilder.append(pref.getID());
                    injectionBuilder.append("\":\"");
                    injectionBuilder.append(pref.getDefaultValue());
                    injectionBuilder.append("\",");
                    //adding a trailing comma is fine since it will be parsed away by gson upon
                    // serialization of a user object
                    injectionPerformed = true;
                }
            }

            //add closing curly brace back in
            masterJson += "}";

            //write back to file
            BufferedWriter jsonWriter = new BufferedWriter(new FileWriter(f,false));
            jsonWriter.write(masterJson);
            jsonWriter.close();

            //log the injection
            SessionLogger.log(SessionLogger.Tag.ACTION, "User file: \"" + f +
                    "\" was found to be an older userdata file; preference injection performed");
        } catch (Exception e) {
            ErrorHandler.handle(e);
            DebugConsole.println("Something horrible happened while trying to fix an old userdata.json file");
        }
    }
}
