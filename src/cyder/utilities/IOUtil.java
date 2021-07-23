package cyder.utilities;

import cyder.genesis.Entry;
import cyder.genesis.GenesisShare;
import cyder.handler.*;
import cyder.obj.NST;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.widgets.AudioPlayer;
import cyder.widgets.GenericInform;
import cyder.widgets.Notes;
import javazoom.jl.player.Player;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IOUtil {

    private IOUtil() {} //private constructor to avoid object creation

    private static AudioPlayer CyderPlayer;
    private static Notes Notes;
    private static Player player;

    /**
     * Opens the provided file outside of the program regardless of whether or not a
     * handler exists for the file type
     * @param filePath - the path to the file to open
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
                SessionLogger.log(SessionLogger.Tag.LINK, filePath);
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }
    }

    /**
     * Creates the provided temporary file in the temp directory and writes the given string lines to it
     * @param filename - the name of the file to create
     * @param extension - the extension of the file to create
     * @param lines - the Strings to write to the file
     */
    public static void createAndOpenTmpFile(String filename, String extension, String[] lines) {
        try {
            File tmpDir = new File("src/tmp");

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

            SessionLogger.log(SessionLogger.Tag.LINK, "[TEMP FILE] " + filename + "." + extension);
            openFileOutsideProgram(tmpFile.getAbsolutePath());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Deletes the temperary directory if it exists
     */
    public static void deleteTempDir() {
        try {
            File tmpDir = new File("src/tmp");
            SystemUtil.deleteFolder(tmpDir);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * This method removes any repeated user data. Any repeated keys are thrown away and the first occurences are kept.
     * If any keys are missing, the default ones necessary for the program to function properly are inserted.
     */
    public static void fixUserData() {
        //get user var for later use
        String user = ConsoleFrame.getConsoleFrame().getUUID();

        //return if no user, shouldn't be possible anyway
        if (user == null)
            return;

        //if the data file is gone then we're screwed
        if (!new File("users/" + user + "/Userdata.txt").exists())
            corruptedUser();

        //try with resources to write all the default pairs in case some are missing, only the first pairs will be saved
        // so any that we already have will be kept and any duplicates will be removed
        try (BufferedWriter userWriter = new BufferedWriter(new FileWriter(
                "users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Userdata.txt", true))) {
            GenesisShare.getExitingSem().acquire();

            //always just add a newline to the front to be safe
            userWriter.newLine();

            //write default pairs
            for (int i = 0; i < GenesisShare.getPrefs().size(); i++) {
                userWriter.write(GenesisShare.getPrefs().get(i).getID() + ":" + GenesisShare.getPrefs().get(i).getDefaultValue());
                userWriter.newLine();
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            GenesisShare.getExitingSem().release();
        }

        //try with resources reading all user data
        try (BufferedReader dataReader = new BufferedReader(new FileReader("users/" + user + "/Userdata.txt"))) {
            GenesisShare.getExitingSem().acquire();
            String line;
            ArrayList<NST> data = new ArrayList<>();

            //read all data from in
            while ((line = dataReader.readLine()) != null) {
                //skip for blank lines
                if (line.trim().length() == 0)
                    continue;

                long count = line.chars().filter(charaizard -> charaizard == ':').count(); //charizard, rawr

                //if more than one colon on a line, screwed
                if (count != 1 && line.trim().length() != 0)
                    corruptedUser();

                String[] parts = line.split(":");

                //if not two parts, then screwed
                if (parts.length != 2)
                    corruptedUser();

                //we're good so form a NST object and place in data list
                data.add(new NST(parts[0], parts[1]));
            }

            //list to hold only the first data pairs
            ArrayList<NST> reWriteData = new ArrayList<>();

            //loop through all data
            for (NST datum : data) {
                String currentName = datum.getName();
                boolean alreadyHas = false;

                //if the current name is already in the rewrite data, skip it
                for (NST reWriteDatum : reWriteData) {
                    if (reWriteDatum.getName().equalsIgnoreCase(currentName)) {
                        alreadyHas = true;
                        break;
                    }
                }

                if (!alreadyHas)
                    reWriteData.add(datum);
            }

            //write the data we want to keep
            BufferedWriter userWriter = new BufferedWriter(new FileWriter(
                    "users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Userdata.txt", false));

            for (NST currentData : reWriteData) {
                userWriter.write(currentData.getName() + ":" + currentData.getData());
                userWriter.newLine();
            }

            userWriter.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            GenesisShare.getExitingSem().release();
        }
    }

    /**
     * Sets the targeted key in userdata to the requested value
     * @param name - the target ID to find
     * @param value - the value to set the ID to
     */
    public static void setUserData(String name, String value) {
        if (ConsoleFrame.getConsoleFrame().getUUID() == null)
            return;

        try {
            //block other functions from reading/writing to userdata until we're done
            GenesisShare.getExitingSem().acquire();

            //check for no user
            String user = ConsoleFrame.getConsoleFrame().getUUID();
            if (user == null)
                return;

            //check for absense of data file
            if (!new File("users/" + user + "/Userdata.txt").exists())
                corruptedUser();

            SessionLogger.log(SessionLogger.Tag.CLIENT_IO,"[SET] [KEY] " + name + " [VALUE] " + value);

            //init reader, list, and line var
            BufferedReader dataReader = new BufferedReader(new FileReader("users/" + user + "/Userdata.txt"));
            LinkedList<NST> userData = new LinkedList<>();
            String Line;

            //read all data into our list
            while ((Line = dataReader.readLine()) != null) {
                String[] parts = Line.split(":");
                userData.add(new NST(parts[0], parts[1]));
            }

            //free resources
            dataReader.close();

            //init writer
            BufferedWriter userWriter = new BufferedWriter(new FileWriter(
                    "users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Userdata.txt", false));

            //loop through data and change the value we need to update, then write to file
            for (NST data : userData) {
                if (data.getName().equalsIgnoreCase(name))
                    data.setData(value);

                userWriter.write(data.getName() + ":" + data.getData());
                userWriter.newLine();
            }

            //free resouces
            userWriter.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            GenesisShare.getExitingSem().release();
        }
    }

    /**
     * Changes the requested system data to the provided value
     * @param name - the system data ID to find
     * @param value - the value of the requested system data ID to update
     */
    public static void setSystemData(String name, String value) {
        try {
            SessionLogger.log(SessionLogger.Tag.SYSTEM_IO,"[SET] [KEY] " + name + " [VALUE] " + value);

            //block other functiosn from changing system data while we are changing it here
            GenesisShare.getExitingSem().acquire();

            //init needed vars
            BufferedReader sysReader = new BufferedReader(new FileReader("Sys.ini"));
            LinkedList<NST> systemData = new LinkedList<>();
            String Line;

            //read data into list
            while ((Line = sysReader.readLine()) != null) {
                String[] parts = Line.split(":");
                systemData.add(new NST(parts[0], parts[1]));
            }

            //free resources
            sysReader.close();

            //init writer
            BufferedWriter sysWriter = new BufferedWriter(new FileWriter("Sys.ini", false));

            //loop through data and write to file, change the data we wanted to change
            for (NST data : systemData) {
                if (data.getName().equalsIgnoreCase(name)) {
                    data.setData(value);
                }

                sysWriter.write(data.getName() + ":" + data.getData());
                sysWriter.newLine();
            }

            sysWriter.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            GenesisShare.getExitingSem().release();
        }
    }

    /**
     * Finds and returns the requested userdata stored in key/value pairs.
     * @param name - the data ID to find
     * @return - the data associated with the provided ID
     */
    public static String getUserData(String name) {
        if (ConsoleFrame.getConsoleFrame().getUUID() == null)
            return null;

        String ret = "null";

        try {
            //block other functions from reading/writing to userdata until we're done
            GenesisShare.getExitingSem().acquire();

            //check for no user
            String user = ConsoleFrame.getConsoleFrame().getUUID();
            if (user == null)
                return null;

            //check for absense of data file
            if (!new File("users/" + user + "/Userdata.txt").exists())
                corruptedUser();

            //init reader and line var
            BufferedReader dataReader = new BufferedReader(new FileReader("users/" + user + "/Userdata.txt"));
            String Line;

            //read all data into our list
            while ((Line = dataReader.readLine()) != null) {
                String[] parts = Line.split(":");

                if (parts[0].equalsIgnoreCase(name)) {
                    ret = parts[1];
                    break;
                }
            }

            //free resources
            dataReader.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            if (!name.equalsIgnoreCase("CLOCKONCONSOLE")
                    && !name.equalsIgnoreCase("SHOWSECONDS"))
            SessionLogger.log(SessionLogger.Tag.CLIENT_IO, "[GET] [" +  name
                    + "] [RETURN VALUE] " + ret);
            GenesisShare.getExitingSem().release();
            return ret;
        }
    }

    /**
     * Finds and returns the data associated with the provided ID
     * @param name - the ID of the data to be returned
     * @return - the data associated with the provided ID
     */
    public static String getSystemData(String name) {
        String ret = "null";

        try {
            //block other functiosn from changing system data while we are changing it here
            GenesisShare.getExitingSem().acquire();

            //init needed vars
            BufferedReader sysReader = new BufferedReader(new FileReader("Sys.ini"));
            String Line;

            //read data until we find what we are looking for
            while ((Line = sysReader.readLine()) != null) {
                String[] parts = Line.split(":");

                if (parts[0].equalsIgnoreCase(name)) {
                    ret = parts[1];
                    break;
                }
            }

            //free resources
            sysReader.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            SessionLogger.log(SessionLogger.Tag.SYSTEM_IO, "[GET] [" +  name
                    + "] [RETURN VALUE] " + ret);
            GenesisShare.getExitingSem().release();
            return ret;
        }
    }

    /**
     * Logs any possible command line arguments passed in to Cyder upon starting.
     * Appends JVM Command Line Arguments along with the start location to the log
     * @param cyderArgs - command line arguments passed in
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
                    "[400 S. Monroe St. Tallahassee, FL] //Ron DeSantis is a GOAT" :
                    (IPUtil.getUserCity() + ", " + IPUtil.getUserState()));

            if (argsString.trim().length() > 0) {
                append += "; args: " + argsString;
            }

            SessionLogger.log(SessionLogger.Tag.JAVA_ARGS, append);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Clean the users/ dir of any possibly corrupted or invalid user folders
     */
    public static void cleanUsers() {
        File users = new File("users");

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
     * Opens the provided file, possibly inside of the program if a handler exists for it
     * @param FilePath - the path to the file to open
     */
    public static void openFile(String FilePath) {
        //use our custom text editor
        if (FilePath.endsWith(".txt")) {
            TextEditor te = new TextEditor(FilePath);
        }
        //use our custom photo viewer
        else if (FilePath.endsWith(".png")) {
            PhotoViewer pv = new PhotoViewer(new File(FilePath));
        }
        //use our own mp3 player
        else if (FilePath.endsWith(".mp3")) {
            mp3(FilePath);
        }
        //welp just open it outside of the program :(
        else {
            Desktop OpenFile = Desktop.getDesktop();

            try {
                File FileToOpen = new File(FilePath);
                URI FileURI = FileToOpen.toURI();
                OpenFile.browse(FileURI);
                SessionLogger.log(SessionLogger.Tag.LINK, FileToOpen.getAbsoluteFile());
            } catch (Exception e) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + FilePath);
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }
        }
    }

    /**
     * Opens the current user's notes; ensoures only one note editor is open at any given time
     */
    public static void startNoteEditor() {
        if (Notes != null)
            Notes.kill();

        Notes = new Notes();
    }

    /**
     * Ends the current AudioPlayer session if ongoing and starts a new one with the requested file
     * @param FilePath - the path to the audio file to start playing
     */
    public static void mp3(String FilePath) {
        if (CyderPlayer != null)
            CyderPlayer.kill();

        stopAudio();
        CyderPlayer = new AudioPlayer(new File(FilePath));
    }

    /**
     * Gets the current audio file playing through the AudioPlayer if any
     * @return - the current audio file being played - null if no file is being played
     */
    public static File getCurrentMP3() {
        if (CyderPlayer == null)
            return null;
         else
            return CyderPlayer.getCurrentAudio();
    }

    /**
     * Plays the requested mp3 audio file using the general IOUtil JLayer player
     * @param FilePath - the path to the mp3 file to play
     * @param inputHandler - the inputhandler to use when appending the stop button
     * @param showStopButton - whether or not to print a button to stop the audio.
     */
    public static void playAudio(String FilePath, InputHandler inputHandler, boolean showStopButton) {
        try {
            stopAudio();
            FileInputStream FileInputStream = new FileInputStream(FilePath);
            player = new Player(FileInputStream);
            SessionLogger.log(SessionLogger.Tag.ACTION,"[AUDIO] " + FilePath);
            Thread AudioThread = new Thread(() -> {
                try {
                    player.play();
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }, "mp3 audio thread");

            AudioThread.start();

            if (showStopButton) {
                CyderButton stopMusicButton = new CyderButton("Stop Audio");
                stopMusicButton.addActionListener((e) -> IOUtil.stopAudio());
                inputHandler.printlnComponent(stopMusicButton);
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Plays the requested system audio file using a new JLayer player
     * @param FilePath - the path to the mp3 file to play
     */
    public static void playSystemAudio(String FilePath) {
        try {
            FileInputStream FileInputStream = new FileInputStream(FilePath);
            Player systemPlayer = new Player(FileInputStream);
            SessionLogger.log(SessionLogger.Tag.ACTION,"[SYSTEM AUDIO] " + FilePath);
            new Thread(() -> {
                try {
                    systemPlayer.play();
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }, "system audio thread").start();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Simpler function for playing an audio file
     * @param FilePath - the path to the mp3 file
     * @param inputHandler - the inputhandler to use hwen appending the stop button
     */
    public static void playAudio(String FilePath, InputHandler inputHandler) {
        playAudio(FilePath, inputHandler, true);
    }

    /**
     * Stops the audio currently playing that is absent of an AudioPlayer object
     */
    public static void stopAudio() {
        try {
            if (player != null && !player.isComplete()) {
                player.close();
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * If a user becomes corrupted for any reason which may be determined any way we choose,
     * this method will aquire the exiting semaphore, dispose of all frames, and attempt to
     * zip any user data aside from userdata.json and the Throws directory
     * <p>
     * This could fail if something has already been deleted which is fine since we want to
     * go to the starting
     */
    public static void corruptedUser() {
        try {
            GenesisShare.suspendFrameChecker();
            ConsoleFrame.getConsoleFrame().close();

            //close all open frames
            Frame[] frames = Frame.getFrames();
            for (Frame f : frames)
                f.dispose();

            //if it's already gone then it really wasn't a corrupted user, possibly a user deleting their account
            File mainZipFile = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID());
            if (mainZipFile == null || mainZipFile.listFiles() == null || mainZipFile.listFiles().length == 0)
                return;

            //confirmed that the user was corrupted so we inform the user
            GenericInform.inform("Sorry, " + SystemUtil.getWindowsUsername() + ", but your user was corrupted. " +
                    "Your data has been saved, zipped, and placed in your Downloads folder", "Corrupted User :(");

            //delete the stuff we don't care about
            for (File f : mainZipFile.listFiles()) {
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

            SessionLogger.log(SessionLogger.Tag.CORRUPTION,fileName);

            //delete the folder we just zipped since it's a duplicate
            SystemUtil.deleteFolder(mainZipFile);

            Entry.showEntryGUI();
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }
    }

    /**
     * Zips the provided file with the given name using hte provided ZOS
     * @param fileToZip - the file/dir to zip
     * @param fileName - the name of the resulting file (path included)
     * @param zipOut - the Zip Output Stream
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

    /**
     * Changes the current user from console frame's name to the provided name.
     * @param newName - the new name of the user
     */
    public static void changeUsername(String newName) {
        try {
            setUserData("name", newName);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Changes the current user from console frame's password to the provided password
     * @param newPassword - the raw char[] new password to hash and store
     */
    public static void changePassword(char[] newPassword) {
        try {
            setUserData("password", SecurityUtil.toHexString(SecurityUtil.getSHA256(
                    SecurityUtil.toHexString(SecurityUtil.getSHA256(newPassword)).toCharArray())));
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Gets DOS attributes of the provided file
     * @param file - the file to obtain the attributes of
     * @return - the DOS attributes in the following order: isArchive, isHidden,
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
            ErrorHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns the size of the provided file in bytes
     * @param f - the file to calculate the size of
     * @return - the size in bytes of the file
     */
    public static long getFileSize(File f) {
        long ret = 0;
        try {
            ret = Files.readAttributes(Paths.get(f.getPath()), DosFileAttributes.class).size();
        } catch (IOException e) {
            ErrorHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns a binary string from the provided binary file
     * @param f - the binary file of pure binary contents
     * @return - the String of binary data from the file
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
            ErrorHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns a hex string from the provided binary file
     * @param f - the binary file of pure binary contents
     * @return - the String of hex data from the file
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
            ErrorHandler.handle(e);
        }

        return ret;
    }

    /**
     * Handles the sandbox and it's files depending on the computer we are on
     */
    public static void cleanSandbox() {
        if (!SecurityUtil.nathanLenovo()) {
            wipeSandbox();
        } else {
            File sandbox = new File("Sandbox");

            if (!sandbox.exists()) {
                sandbox.mkdir();
            }
        }
    }

    /**
     * Wipes the Sandbox of files if we are not in developer mode, ensures the folder stays though.
     */
    public static void wipeSandbox() {
        File sandbox = new File("Sandbox");

        if (sandbox.exists()) {
            SystemUtil.deleteFolder(sandbox);
        }
    }

    /**
     * Upon entry this method attempts to fix any user logs that ended abruptly (an exit code of -1 most likely)
     *  as a result of an IDE stop or OS Task Manager Stop.
     */
    public static void fixLogs() {
        try {
            for (File log : new File("logs").listFiles()) {
                if (!log.equals(SessionLogger.getCurrentLog())) {
                    BufferedReader br = new BufferedReader(new FileReader(log));
                    String line;
                    boolean containsEOL = false;

                    while ((line = br.readLine()) != null) {
                        if (line.contains("[EOL]") || line.contains("[EXTERNAL STOP]")) {
                            containsEOL = true;
                            break;
                        }
                    }

                    if (!containsEOL) {
                        Files.write(Paths.get(log.getAbsolutePath()),
                                ("[EXTERNAL STOP] Cyder was force closed by an external entity such " +
                                        "as an IDE stop or the OS' Task Manager\n").getBytes(), StandardOpenOption.APPEND);
                    }
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}