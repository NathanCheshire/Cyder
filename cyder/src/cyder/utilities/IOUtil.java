package cyder.utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.handlers.external.AudioPlayer;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.external.TextViewer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.SessionHandler;
import cyder.ui.ConsoleFrame;
import cyder.user.UserFile;
import javazoom.jl.player.Player;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.LinkedList;

public class IOUtil {
    /**
     * No objects of util methods allowed.
     */
    private IOUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Player used to play general audio files that may be user terminated.
     */
    private static Player player;

    /**
     * Opens the provided file outside of the program regardless of whether or not a
     * handler exists for the file (e.g.: TextHandler, AudioPlayer, etc.).
     *
     * @param filePath the path to the file to open
     */
    public static void openFileOutsideProgram(String filePath) {
        Desktop OpenFile = Desktop.getDesktop();

        try {
            File FileToOpen = new File(filePath);
            URI FileURI = FileToOpen.toURI();
            OpenFile.browse(FileURI);
        } catch (Exception e) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + filePath);
                SessionHandler.log(SessionHandler.Tag.LINK, filePath);
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }
    }

    /**
     * Determines whether or not the provided string is a link or a file/directory path and then opens it.
     *
     * @param fileOrLink the link/file to open
     */
    public static void openOutsideProgram(String fileOrLink) {
        boolean validLink = false;

        try {
            URL url = new URL(fileOrLink);
            URLConnection conn = url.openConnection();
            conn.connect();
            validLink = true;
        } catch (Exception ex) {
            validLink = false;
        }

        if (validLink) {
            NetworkUtil.internetConnect(fileOrLink);
        } else {
            openFileOutsideProgram(fileOrLink);
        }
    }

    /**
     * Creates the provided temporary file in the temp directory and writes the given string lines to it.
     *
     * @param filename the name of the file to create
     * @param extension the extension of the file to create
     * @param lines the Strings to write to the file
     */
    public static void createAndOpenTmpFile(String filename, String extension, String[] lines) {
        try {
            File tmpDir = new File("cyder/src/cyder/tmp");

            if (!tmpDir.exists())
                tmpDir.mkdir();

            File tmpFile = new File(tmpDir + "/" + filename + extension);

            if (!tmpFile.exists())
                tmpFile.createNewFile();

            BufferedWriter tmpFileWriter = new BufferedWriter(new FileWriter(tmpFile));

            for (String line : lines) {
                tmpFileWriter.write(line);
                tmpFileWriter.newLine();
            }

            tmpFileWriter.flush();
            tmpFileWriter.close();

            SessionHandler.log(SessionHandler.Tag.LINK, "[TEMP FILE] " + filename + "." + extension);
            openFileOutsideProgram(tmpFile.getAbsolutePath());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Deletes the temperary directory if it exists.
     */
    public static void deleteTempDir() {
        try {
            File tmpDir = new File("cyder/src/cyder/tmp");
            SystemUtil.deleteFolder(tmpDir);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The file path to the sys json file.
     */
    private static String sysFilePath = "static/json/sys.json";

    //determines whether or not the sys.json file exists, is parsable, and contains non-null fields
    public static void checkSystemData() {
        try {
            File sysFile = new File(sysFilePath);

            if (!sysFile.exists())
                CyderCommon.exit(-112);

            //gson obj
            Gson gson = new Gson();

            //read into the object if parsable
            FileReader reader = new FileReader(sysFile);
            SystemData sysObj = null;

            try {
                sysObj = gson.fromJson(reader, SystemData.class);
            } catch (Exception ignored) {
                //couldn't be parsed so exit program
                reader.close();
                throw new Exception("Could not parse system data");
            }

            //object successfully parsed by GSON at this point

            //make sure the obj isn't null
            if (sysObj == null) {
                throw new Exception("System data object serialized to null");
            }

            //try all getters and make sure not null
            for (Method m : sysObj.getClass().getMethods()) {
                //get the getter method
                if (m.getName().toLowerCase().contains("get") && m.getParameterCount() == 0) {
                    //all getters return strings for user objects so invoke the method
                    Object object = m.invoke(sysObj);

                    if (object == null)
                        throw new Exception("System data object attribute was serialized to null: " + m.getName());
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            CyderCommon.exit(-112);
        }
    }

    /**
     * Returns the SystemData object representing all the system data located in sys.json.
     *
     * @return the SystemData object
     */
    public static SystemData getSystemData() {
        return sd;
    }

    /**
     * The SystemData object.
     */
    private static SystemData sd = null;

    static {
        loadSystemData();
    }

    /**
     * Loads the system data.
     */
    private static void loadSystemData() {
        SystemData ret = null;
        Gson gson = new Gson();

        SessionHandler.log(SessionHandler.Tag.SYSTEM_IO, "System data pared in IOUtil's static block");

        try (Reader reader = new FileReader(sysFilePath)) {
            ret = gson.fromJson(reader, SystemData.class);

            //if successful set as our sd object
            sd = ret;
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Writes the provided SystemData object to sys.json, overriding whatever object is represented there.
     *
     * @param sd the SystemData object to write
     */
    public static void setSystemData(SystemData sd) {
        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(sysFilePath)) {
            gson.toJson(sd, writer);
            SessionHandler.log(SessionHandler.Tag.SYSTEM_IO, "System data set to sd: " + sd);

            //now update IOUtil's sd object
            loadSystemData();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Logs any possible command line arguments passed in to Cyder upon starting.
     * Appends JVM Command Line Arguments along with the start location to the log.
     *
     * @param cyderArgs command line arguments passed in
     */
    public static void logArgs(String[] cyderArgs) {
        try {
            String argsString = "";

            for (int i = 0; i < cyderArgs.length; i++) {
                if (i != 0)
                    argsString += ",";
                argsString += cyderArgs[i];
            }

            String append = "[LOCATION] " + (SecurityUtil.nathanLenovo() ?
                    "[La casa de Nathan]" :
                    (IPUtil.getIpdata().getCity() + ", " + IPUtil.getIpdata().getRegion()));

            if (argsString.trim().length() > 0) {
                append += "; args: " + argsString;
            }

            SessionHandler.log(SessionHandler.Tag.JAVA_ARGS, append);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the linked list of suggestions loaded from helps.json
     *
     * @return a linked list of Suggestion objects
     */
    public static LinkedList<Suggestion> getSuggestions() {
        if (suggestions == null)
            loadSuggestions();
        return suggestions;
    }

    /**
     * Suggestions list of suggestions to prompt a user.
     */
    private static LinkedList<Suggestion> suggestions = null;

    /**
     * The path to helps.json
     */
    private static String helpFilePath = "static/json/helps.json";

    static {
        loadSuggestions();
    }

    /**
     * Loads the suggestions into memory.
     */
    private static void loadSuggestions() {
        LinkedList<Suggestion> ret = null;
        Gson gson = new Gson();

        SessionHandler.log(SessionHandler.Tag.SYSTEM_IO, "Suggestions pared in IOUtil's static block");

        try (Reader reader = new FileReader(helpFilePath)) {
            Type helpType = new TypeToken<LinkedList<Suggestion>>(){}.getType();

            ret = gson.fromJson(reader, helpType);

            //if successful set as our suggestions object
            suggestions = ret;
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Adds a suggestion to the helps.json file, should never be invoked by a user.
     * Make this method public when needing access to add suggestions.
     *
     * @param suggestion the suggestion to add to helps.json
     */
    private static void addSuggestion(Suggestion suggestion) {
        Gson gson = new Gson();

        loadSuggestions();

        if (suggestions == null)
            suggestions = new LinkedList<>();

        if (suggestions.contains(suggestion))
            return;

        suggestions.add(suggestion);

        try (FileWriter writer = new FileWriter(helpFilePath)) {
            gson.toJson(suggestions, writer);
            SessionHandler.log(SessionHandler.Tag.SYSTEM_IO, "Suggestions had " + suggestion + " added.");

            //now update suggestions
            loadSuggestions();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Clean the users/ dir of any possibly corrupted or invalid user folders.
     */
    public static void cleanUsers() {
        File users = new File("dynamic/users");

        if (!users.exists()) {
            users.mkdirs();
        } else {
            File[] UUIDs = users.listFiles();

            for (File user : UUIDs) {
                if (!user.isDirectory())
                    continue;
                if (user.isDirectory() && (user.getName().contains("VoidUser") || user.listFiles().length < 2)) {
                    SystemUtil.deleteFolder(user);
                }
            }
        }
    }

    /**
     * Attempts to fix any user files that may be outdated via preference injection.
     */
    public static void fixUsers() {
        //all users
        File users = new File(OSUtil.buildPath("dynamic","users"));

        //for all files
        for (File user : users.listFiles()) {
            //file userdata
            File json = new File(OSUtil.buildPath(
                    user.getAbsolutePath(), UserFile.USERDATA.getName()));

            if (json.exists()) {
                //attempt to update the json

                //todo this fails for some reason? maybe new users don't actually have all needed data?
                boolean success = UserUtil.updateOldJson(json);

                //if it fails then delete the json
                if (!success) {
                    json.delete();
                    UserUtil.userJsonDeleted(StringUtil.getFilename(user));
                }
            }
        }
     }

    /**
     * Opens the provided file, possibly inside of the program if a handler exists for it.
     *
     * @param FilePath the path to the file to open
     */
    public static void openFile(String FilePath) {
        //use our custom text editor
        if (FilePath.endsWith(".txt")) {
            TextViewer te = new TextViewer(FilePath);
        }
        //use our custom photo viewer
        else if (FilePath.endsWith(".png")) {
            PhotoViewer pv = new PhotoViewer(new File(FilePath));
        }
        //use our own mp3 player
        else if (FilePath.endsWith(".mp3")) {
            AudioPlayer.showGUI(new File(FilePath));
        }
        //welp just open it outside of the program :(
        else {
            Desktop OpenFile = Desktop.getDesktop();

            try {
                File FileToOpen = new File(FilePath);
                URI FileURI = FileToOpen.toURI();
                OpenFile.browse(FileURI);
                SessionHandler.log(SessionHandler.Tag.LINK, FileToOpen.getAbsoluteFile());
            } catch (Exception e) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + FilePath);
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }
        }
    }

    /**
     * Plays the requested audio file using the general IOUtil JLayer player which can be terminated by the user.
     *
     * @param FilePath the path to the audio file to play
     */
    public static void playAudio(String FilePath) {
        try {
            stopAudio();
            FileInputStream FileInputStream = new FileInputStream(FilePath);
            player = new Player(FileInputStream);
            SessionHandler.log(SessionHandler.Tag.ACTION,"[AUDIO] " + FilePath);

            new Thread(() -> {
                try {
                    player.play();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                } finally {
                    ConsoleFrame.getConsoleFrame().revalidateAudioMenu();
                }
            }, "IOUtil audio thread").start();

            ConsoleFrame.getConsoleFrame().revalidateAudioMenu();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns whether or not general audio is playing.
     *
     * @return whether or not general audio is playing
     */
    public static boolean generalAudioPlaying() {
        return player != null && !player.isComplete();
    }

    /**
     * Plays the requested audio file using a new JLayer Player object.
     *      (this cannot be stopped util the mpeg is finished)
     *
     * @param FilePath the path to the mp3 file to play
     */
    public static void playSystemAudio(String FilePath) {
        try {
            FileInputStream FileInputStream = new FileInputStream(FilePath);
            Player systemPlayer = new Player(FileInputStream);

            if (!FilePath.equals("static/audio/Typing.mp3"))
                SessionHandler.log(SessionHandler.Tag.ACTION,"[SYSTEM AUDIO] " + FilePath);
            new Thread(() -> {
                try {
                    systemPlayer.play();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "system audio thread").start();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Stops the audio currently playing. Note that this does not include any system audio or AudioPlayer widget audio.
     */
    public static void stopAudio() {
        try {
            if (player != null && !player.isComplete()) {
                player.close();
                player = null;
                //set to null so that generalAudioPlaying works as intended
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        ConsoleFrame.getConsoleFrame().revalidateAudioMenu();
    }

    /**
     * Stops any and all audio playing either through the audio player or the general player.
     */
    public static void stopAllAudio() {
        if (IOUtil.generalAudioPlaying()) {
            stopAudio();
        }

        AudioPlayer.stopAudio();
    }

    /**
     * Pause audio if playing via AudioPlayer.
     */
    public static void pauseAudio() {
        if (AudioPlayer.audioPlaying()) {
            AudioPlayer.pauseAudio();
        } else if (IOUtil.generalAudioPlaying()) {
            stopAudio();
        }
    }

    /**
     * Changes the current user from console frame's name to the provided name.
     * @param newName the new name of the user
     */
    public static void changeUsername(String newName) {
        try {
            UserUtil.setUserData("name", newName);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Changes the current user from console frame's password to the provided password.
     *
     * @param newPassword the raw char[] new password to hash and store
     */
    public static void changePassword(char[] newPassword) {
        try {
            UserUtil.setUserData("pass", SecurityUtil.toHexString(SecurityUtil.getSHA256(
                    SecurityUtil.toHexString(SecurityUtil.getSHA256(newPassword)).toCharArray())));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Gets DOS attributes of the provided file.
     *
     * @param file the file to obtain the attributes of
     * @return the DOS attributes in the following order: isArchive, isHidden,
     *              isReadOnly, isSystem, creationTime, isDirectory, isOther, isSymbolicLink,
     *              lastAccessTime, lastModifiedTime
     */
    public static String[] getDOSAttributes(File file) {
        String[] ret = new String[10];

        try {
            DosFileAttributes attr = Files.readAttributes(Paths.get(file.getPath()), DosFileAttributes.class);
            ret[0] = String.valueOf(attr.isArchive());
            ret[1] = String.valueOf(attr.isHidden());
            ret[2] = String.valueOf(attr.isReadOnly());
            ret[3] = String.valueOf(attr.isSystem());
            ret[4] = String.valueOf(attr.creationTime());
            ret[5] = String.valueOf(attr.isDirectory());
            ret[6] = String.valueOf(attr.isOther());
            ret[7] = String.valueOf(attr.isSymbolicLink());
            ret[8] = String.valueOf(attr.lastAccessTime());
            ret[9] = String.valueOf(attr.lastModifiedTime());
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns the size of the provided file in bytes.
     *
     * @param f the file to calculate the size of
     * @return the size in bytes of the file
     */
    public static long getFileSize(File f) {
        long ret = 0;
        try {
            ret = Files.readAttributes(Paths.get(f.getPath()), DosFileAttributes.class).size();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns a binary string for the provided binary file.
     *
     * @param f the binary file of pure binary contents
     * @return the String of binary data from the file
     */
    public static String getBinaryString(File f) {
        if (!f.exists())
            throw new IllegalArgumentException("bin does not exist");
        if (!StringUtil.getExtension(f).equalsIgnoreCase(".bin"))
            throw new IllegalArgumentException("File is not a binary");

        String ret = null;

        try {
            BufferedReader fis = new BufferedReader(new FileReader(f));
            String stringBytes = fis.readLine();
            fis.close();
            ret = stringBytes;

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns a hex string for the provided binary file.
     *
     * @param f the binary file of pure binary contents
     * @return the String of hex data from the file
     */
    public static String getHexString(File f) {
        if (!f.exists())
            throw new IllegalArgumentException("bin does not exist");
        if (!StringUtil.getExtension(f).equalsIgnoreCase(".bin"))
            throw new IllegalArgumentException("File is not a binary");

        String ret = null;

        try {
            BufferedReader fis = new BufferedReader(new FileReader(f));
            String[] stringBytes = fis.readLine().split("(?<=\\G........)");
            StringBuilder sb = new StringBuilder();

            for (String stringByte : stringBytes) {
                sb.append(Integer.toString(Integer.parseInt(stringByte, 2), 16));
            }

            fis.close();
            ret = sb.toString();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Handles the sandbox and it's files depending on whether or not Cyder is in development
     * or production mode.
     */
    public static void cleanSandbox() {
        if (!SecurityUtil.nathanLenovo()) {
            wipeSandbox();
        } else {
            File sandbox = new File("static/sandbox");

            if (!sandbox.exists()) {
                sandbox.mkdir();
            }
        }
    }

    /**
     * Wipes the Sandbox of files if we are not in developer mode, ensures the folder stays though.
     */
    public static void wipeSandbox() {
        File sandbox = new File("static/sandbox");

        if (sandbox.exists()) {
            SystemUtil.deleteFolder(sandbox);
        }
    }

    /**
     * Upon entry this method attempts to fix any user logs that ended abruptly (an exit code of -1)
     * as a result of an IDE stop or OS Task Manager Stop.
     */
    public static void fixLogs() {
        try {
            for (File logDir : new File("logs").listFiles()) {
                //for all directories of days of logs
                for (File log : logDir.listFiles()) {
                    if (!log.equals(SessionHandler.getCurrentLog())) {
                        BufferedReader br = new BufferedReader(new FileReader(log));
                        String line;
                        boolean containsEOL = false;

                        int exceptions = 0;

                        while ((line = br.readLine()) != null) {
                            if (line.contains("[EOL]") || line.contains("[EXTERNAL STOP]")) {
                                containsEOL = true;
                                break;
                            } else if (line.contains("[EXCEPTION]")) {
                                exceptions++;
                            }
                        }

                        if (!containsEOL) {
                            String logBuilder = "[" + TimeUtil.logTime() + "] [EOL]: " +
                                    "Log completed, Cyder was force closed by an external entity: " +
                                    "exit code: -200 [External Stop], exceptions thrown: " + exceptions;

                            Files.write(Paths.get(log.getAbsolutePath()),
                                    (logBuilder).getBytes(), StandardOpenOption.APPEND);
                        }
                    }
                }
            }

            //now fix userdata associated with the logs
            UserUtil.fixLoggedInValues();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Used to test for Nathan being an idiot and having duplicate exit condition codes.
     *
     * @return - boolean describing whether or not Nathan messed up
     */
    public static boolean checkForExitCollisions() {
        boolean ret = false;

        //if there are exit conditions with the same number exit and inform
        LinkedList<Integer> exitCodes = new LinkedList<>();

        for (ExitCondition exitCondition : exitConditions) {
            if (!exitCodes.contains(exitCondition.getCode())) {
                exitCodes.add(exitCondition.getCode());
            } else {
                //you're an idiot
                ret = true;
                break;
            }
        }

        return ret;
    }

    /**
     * Exit conditions ArrayList.
     */
    private static ArrayList<ExitCondition> exitConditions = null;

    /**
     * Returns the ArrayList of ExitCodes which contain the integer code and a description String.
     *
     * @return the ArrayList of ExitCodes which contain the integer code and a description String
     */
    public static ArrayList<ExitCondition> getExitConditions() {
        return exitConditions;
    }

    static {
        loadExitConditions();
    }

    /**
     * Loads the exit conditions ArrayList into memory.
     */
    public static void loadExitConditions() {
        ArrayList<ExitCondition> ret = null;
        Gson gson = new Gson();

        SessionHandler.log(SessionHandler.Tag.SYSTEM_IO, "Exit conditions pared in IOUtil's static block");

        try (Reader reader = new FileReader("static/json/exitconditions.json")) {
            Type exittype = new TypeToken<ArrayList<ExitCondition>>(){}.getType();

            ret = gson.fromJson(reader, exittype);

            //if successful set as our suggestions object
            exitConditions = ret;
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * DebugHash ArrayList.
     */
    private static ArrayList<DebugHash> debugHashes = null;

    /**
     * Returns the ArrayList of DebugHashes.
     *
     * @return the ArrayList of DebugHashes
     */
    public static ArrayList<DebugHash> getDebugHashes() {
        return debugHashes;
    }

    static {
        loadDebugHashes();
    }

    /**
     * Loads the exit conditions ArrayList into memory.
     */
    public static void loadDebugHashes() {
        ArrayList<DebugHash> ret = null;
        Gson gson = new Gson();

        SessionHandler.log(SessionHandler.Tag.SYSTEM_IO, "DebugHashes pared in IOUtil's static block");

        try (Reader reader = new FileReader("static/json/debughashes.json")) {
            Type debughash = new TypeToken<ArrayList<DebugHash>>(){}.getType();

            ret = gson.fromJson(reader, debughash);

            //if successful set as our suggestions object
            debugHashes = ret;
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    //todo I feel like this and all jsons loaded from the jsons should be inside of CyderCommon OR
    // it's own system data class loader thing, like a util to load system jsons
    /**
     * SystemData class used by sys.json, no lists should be contained within SystemData.
     */
    public static class SystemData {
        private boolean released;
        private String version;
        private String releasedate;
        private String mastermac;
        private boolean uiloc;
        private double uiscale;
        private boolean consoleresizable;
        private boolean autocypher;
        private boolean testingmode;

        public boolean isReleased() {
            return released;
        }

        public void setReleased(boolean released) {
            this.released = released;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getReleasedate() {
            return releasedate;
        }

        public void setReleasedate(String releasedate) {
            this.releasedate = releasedate;
        }

        public String getMastermac() {
            return mastermac;
        }

        public void setMastermac(String mastermac) {
            this.mastermac = mastermac;
        }

        public boolean isUiloc() {
            return uiloc;
        }

        public void setUiloc(boolean uiloc) {
            this.uiloc = uiloc;
        }

        public double getUiscale() {
            return uiscale;
        }

        public void setUiscale(double uiscale) {
            this.uiscale = uiscale;
        }

        public boolean isConsoleresizable() {
            return consoleresizable;
        }

        public void setConsoleresizable(boolean consoleresizable) {
            this.consoleresizable = consoleresizable;
        }

        public boolean isAutocypher() {
            return autocypher;
        }

        public void setAutocypher(boolean autocypher) {
            this.autocypher = autocypher;
        }

        public boolean isTestingmode() {
            return testingmode;
        }

        public void setTestingmode(boolean testingmode) {
            this.testingmode = testingmode;
        }

        @Override
        public String toString() {
            return ReflectionUtil.commonCyderToString(this);
        }
    }

    /**
     * Suggestion class used to store and output command suggestions to the user.
     */
    public static class Suggestion {
        private String command;
        private String result;

        public Suggestion(String command, String result) {
            this.command = command;
            this.result = result;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "\"" + command + "\" should trigger: \"" + result + "\"";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Suggestion))
                return false;

            Suggestion sug = (Suggestion) o;

            return sug.getResult().equals(this.getResult())
                    && sug.getCommand().equals(this.getCommand());
        }
    }

    /**
     * ExitCondition used for exiting Cyder in a controlled way.
     */
    public static class ExitCondition {
        private int code;
        private String description;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "[code: " + this.code + ", desc: " + this.description + "]";
        }
    }

    public static class DebugHash {
        private String name;
        private String hashpass;

        public DebugHash(String name, String hashpass) {
            this.name = name;
            this.hashpass = hashpass;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHashpass() {
            return hashpass;
        }

        public void setHashpass(String hashpass) {
            this.hashpass = hashpass;
        }
    }

    //read once on compile time
    private static IgnoreData ignoreDatas = null;

    static {
        loadIgnoreDatas();
    }

    public static void loadIgnoreDatas() {
        Gson gson = new Gson();

        try (Reader reader = new FileReader("static/json/ignoredatas.json")) {
            IgnoreData ret = gson.fromJson(reader, IgnoreData.class);

            //if successful set as our suggestions object
            ignoreDatas = ret;
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * @param dataid the id of the data we wish to obtain from userdata file
     *
     * @return boolean detemrining whether or not this data should be ignored by the SessionLogger
     */
    public static boolean ignoreLogData(String dataid) {
        return ignoreDatas.getIgnoreData().contains(dataid);
    }

    private static class IgnoreData {
        private ArrayList<String> ignorelogdata;

        public IgnoreData(ArrayList<String> ignorelogdata) {
            this.ignorelogdata = ignorelogdata;
        }

        public ArrayList<String> getIgnoreData() {
            return ignorelogdata;
        }

        public void setIgnoreData(ArrayList<String> ignorelogdata) {
            this.ignorelogdata = ignorelogdata;
        }
    }

    //read once on compile time
    private static IgnoreThread ignoreThreads = null;

    public static IgnoreThread getIgnoreThreads() {
        return ignoreThreads;
    }

    static {
        loadIgnoreThreads();
    }

    public static void loadIgnoreThreads() {
        Gson gson = new Gson();

        try (Reader reader = new FileReader("static/json/ignorethreads.json")) {
            IgnoreThread ret = gson.fromJson(reader, IgnoreThread.class);

            //if successful set as our suggestions object
            ignoreThreads = ret;
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    public static class IgnoreThread {
        private ArrayList<String> ignorethreads;

        public IgnoreThread(ArrayList<String> ignorethreads) {
            this.ignorethreads = ignorethreads;
        }

        public ArrayList<String> getIgnorethreads() {
            return ignorethreads;
        }

        public void setIgnorethreads(ArrayList<String> ignorethreads) {
            this.ignorethreads = ignorethreads;
        }
    }
}