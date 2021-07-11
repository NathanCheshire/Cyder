package cyder.utilities;

import cyder.exception.FatalException;
import cyder.genesis.Entry;
import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.handler.PhotoViewer;
import cyder.handler.SessionLogger;
import cyder.handler.TextEditor;
import cyder.obj.NST;
import cyder.obj.Preference;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.widgets.AudioPlayer;
import cyder.widgets.GenericInform;
import cyder.widgets.Notes;
import javazoom.jl.player.Player;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.DosFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IOUtil {

    private IOUtil() {} //private constructor to avoid object creation

    private static AudioPlayer CyderPlayer;
    private static Notes Notes;
    private static Player player;

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

    public static void deleteTempDir() {
        try {
            File tmpDir = new File("src/tmp");
            SystemUtil.deleteFolder(tmpDir);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Used to obtain data from any binary file (not just the current user) that is stored in binary
     *  using the regular key/value pair preference encoding
     *
     * @param userDataBin - the .bin file to read
     * @param dataKey     - the identifier of the data to be obtained
     * @return - the data associated with dataKey
     * @throws FatalException - if file DNE or file is a non-binary
     */
    public static String extractUserData(File userDataBin, String dataKey) throws FatalException {
        if (!userDataBin.exists())
            throw new FatalException("Userdata.bin does not exist");
        else if (!userDataBin.getName().endsWith(".bin")) {
            throw new FatalException("Userdata is not a binary");
        }

        String ret = null;

        try {
            GenesisShare.getExitingSem().acquire();

            BufferedReader fis = new BufferedReader(new FileReader(userDataBin));
            String[] stringBytes = fis.readLine().split("(?<=\\G........)");
            StringBuilder sb = new StringBuilder();

            for (String stringByte : stringBytes) {
                sb.append(new String(
                        new BigInteger(stringByte, 2).toByteArray(),
                        StandardCharsets.UTF_8
                ));
            }

            fis.close();
            String lines[] = sb.toString().split("\\r?\\n");

            for (String line : lines) {
                String parts[] = line.split(":");

                if (parts[0].equalsIgnoreCase(dataKey)) {
                    ret = parts[1];
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            GenesisShare.getExitingSem().release();
        }

        return ret;
    }

    /**
     * Function to replace the old setUserData to overwrite targetID's value in usersdata.bin with the passed in value.
     * Binary writing will replace string writing for security purposes even though it complicates the program flow
     * @param targetID - the preference ID to find
     * @param value - the new value to set targetID to
     */
    public static void newSetUserData(String targetID, String value) {
        if (ConsoleFrame.getConsoleFrame().getUUID() == null)
            return;

        if (!new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/userdata.bin").exists())
            corruptedUser();

        try {
            //only this function can be accessing io data now
            GenesisShare.getExitingSem().acquire();

            //init list to hold user data
            LinkedList<NST> userData = new LinkedList<>();

            //init reader, read bin data, init string builder
            BufferedReader fis = new BufferedReader(new FileReader("users/"
                    + ConsoleFrame.getConsoleFrame().getUUID() + "/userdata.bin"));
            String[] stringBytes = fis.readLine().split("(?<=\\G........)");
            StringBuilder sb = new StringBuilder();

            //decoding string
            for (String stringByte : stringBytes) {
                sb.append(new String(
                        new BigInteger(stringByte, 2).toByteArray(),
                        StandardCharsets.UTF_8
                ));
            }

            //free resources
            fis.close();
            //get all lines from decoded string
            String lines[] = sb.toString().split("\\r?\\n");

            //validate data and all to list
            for (String line : lines) {
                if (!line.contains(":"))
                    corruptedUser();

                String parts[] = line.split(":");
                userData.add(new NST(parts[0], parts[1]));
            }

            //now we write back and set
            BufferedWriter fos = new BufferedWriter(new FileWriter("src/cyder/genesis/userdata.bin"));
            //reset string builder to use for writing
            sb.setLength(0);

            //loop through data and change the value we are writing
            for (NST data : userData) {
                if (data.getName().equalsIgnoreCase(targetID))
                    data.setData(value);

                sb.append(data.getName());
                sb.append(":");
                sb.append(data.getData());
                sb.append("\n");
            }

            //get bytes from string
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

            //writing bytes to file after changing data
            for (byte b : bytes) {
                int result = b & 0xff;
                String resultWithPadZero = String.format("%8s", Integer.toBinaryString(result))
                        .replace(" ", "0");
                fos.write(resultWithPadZero);
            }

            //freeing resources
            fos.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            GenesisShare.getExitingSem().release();
        }
    }

    public static void newFixUserData() {
        //get user var for later use
        String user = ConsoleFrame.getConsoleFrame().getUUID();

        //return if no user, shouldn't be possible anyway
        if (user == null)
            return;

        File dataFile = new File("users/" + user + "/userdata.bin");

        //if the data file is gone then we're screwed
        if (!dataFile.exists())
            corruptedUser();

        ArrayList<NST> data = new ArrayList<>();

        try {
            GenesisShare.getExitingSem().acquire();

            BufferedReader fis = new BufferedReader(new FileReader(dataFile));
            String[] stringBytes = fis.readLine().split("(?<=\\G........)");
            StringBuilder sb = new StringBuilder();

            for (String stringByte : stringBytes) {
                sb.append(new String(
                        new BigInteger(stringByte, 2).toByteArray(),
                        StandardCharsets.UTF_8
                ));
            }

            fis.close();
            String lines[] = sb.toString().split("\\r?\\n");

            for (String line : lines) {
                if (!line.contains(":"))
                    corruptedUser();

                String parts[] = line.split(":");

                if (parts.length != 2)
                    corruptedUser();

                data.add(new NST(parts[0], parts[1]));
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            GenesisShare.getExitingSem().release();
        }

        for (Preference pref : GenesisShare.getPrefs()) {
            data.add(new NST(pref.getID(), pref.getDefaultValue()));
        }

        ArrayList<NST> reWriteData = new ArrayList<>();

        for (NST datum : data) {
            String currentName = datum.getName();
            boolean alreadyHas = false;

            for (NST reWriteDatum : reWriteData) {
                if (reWriteDatum.getName().equalsIgnoreCase(currentName)) {
                    alreadyHas = true;
                    break;
                }
            }

            if (!alreadyHas)
                reWriteData.add(datum);
        }

        try {
            GenesisShare.getExitingSem().acquire();
            BufferedWriter fos = new BufferedWriter(new FileWriter(dataFile));
            StringBuilder sb = new StringBuilder();

            for (NST redata : reWriteData) {
                sb.append(redata.getName());
                sb.append(":");
                sb.append(redata.getData());
                sb.append("\n");
            }

            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

            //writing bytes of bytes, change any before here
            for (byte b : bytes) {
                int result = b & 0xff;
                String resultWithPadZero = String.format("%8s", Integer.toBinaryString(result))
                        .replace(" ", "0");
                fos.write(resultWithPadZero);
            }

            fos.flush();
            fos.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            GenesisShare.getExitingSem().release();
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
     * Reads and returns the data with the keyname given. Data should be stored in binary using
     * key/value pair encoding.
     * @param name - the data ID to target
     * @return - the data associated with the requested key
     */
    public static String newGetUserData(String name) {
        String ret = null;

        if (ConsoleFrame.getConsoleFrame().getUUID() == null)
            throw new IllegalArgumentException("User not set");

        if (!new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/userdata.bin").exists())
            corruptedUser();

        try {
            //only this function can be accessing io data now
            GenesisShare.getExitingSem().acquire();

            //init reader, read bin data, init string builder
            BufferedReader fis = new BufferedReader(new FileReader("users/"
                    + ConsoleFrame.getConsoleFrame().getUUID() + "/userdata.bin"));
            String[] stringBytes = fis.readLine().split("(?<=\\G........)");
            StringBuilder sb = new StringBuilder();

            //decoding string
            for (String stringByte : stringBytes) {
                sb.append(new String(
                        new BigInteger(stringByte, 2).toByteArray(),
                        StandardCharsets.UTF_8
                ));
            }

            //free resources
            fis.close();
            //get all lines from decoded string
            String lines[] = sb.toString().split("\\r?\\n");

            //validate data and all to list
            for (String line : lines) {
                String parts[] = line.split(":");

                if (parts[0].equalsIgnoreCase(name)) {
                    ret = parts[1];
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            GenesisShare.getExitingSem().release();
            return ret;
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

        String ret = null;

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
            SessionLogger.log(SessionLogger.Tag.CLIENT_IO, "[SET] [" +  name
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
        String ret = null;

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
            SessionLogger.log(SessionLogger.Tag.SYSTEM_IO, "[SET] [" +  name
                    + "] [RETURN VALUE] " + ret);
            GenesisShare.getExitingSem().release();
            return ret;
        }
    }

    /**
     * Logs any possible command line arguments passed in to Cyder upon starting.
     * Appends the start date along with some information to StartLog.ini
     * @param cyderArgs - command line arguments passed in
     */
    public static void logArgs(String[] cyderArgs) {
        try {
            File log = new File("StartLog.ini");

            if (!log.exists())
                log.createNewFile();

            String argsString = "";

            for (int i = 0; i < cyderArgs.length; i++) {
                if (i != 0)
                    argsString += ",";
                argsString += cyderArgs[i];
            }

            String append = new SimpleDateFormat("MM-dd-yy HH:mm:ss").format(new Date())
                    + " : " + "Started by " + System.getProperty("user.name") + " in "
                    + (SecurityUtil.nathanLenovo() ? "[LOCATION NOT AVAILABLE]" :
                    (IPUtil.getUserCity() + ", " + IPUtil.getUserState())) + System.getProperty("line.separator");

            if (argsString.trim().length() > 0) {
                append += "; args: " + argsString;
            }

            Files.write(Paths.get("StartLog.ini"), append.getBytes(), StandardOpenOption.APPEND);

        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void cleanUsers() {
        File[] UUIDs = new File("users").listFiles();

        for (File user : UUIDs) {
            if (!user.isDirectory())
                return;

            if (user.getName().contains("VoidUser") || (user.isDirectory() && user.listFiles().length < 2)) {
                SystemUtil.deleteFolder(user);
            }
        }
    }

    public static void openFile(String FilePath) {
        //use our custom text editor
        if (FilePath.endsWith(".txt")) {
            TextEditor te = new TextEditor(FilePath);
        } else if (FilePath.endsWith(".png")) {
            PhotoViewer pv = new PhotoViewer(new File(FilePath));
        }

        //use our own mp3 player
        else if (FilePath.endsWith(".mp3")) {
            CyderPlayer = new AudioPlayer(new File(FilePath));
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

    public static void startNoteEditor() {
        if (Notes != null)
            Notes.kill();

        Notes = new Notes();
    }

    public static void mp3(String FilePath) {
        if (CyderPlayer != null)
            CyderPlayer.kill();

        stopMusic();
        CyderPlayer = new AudioPlayer(new File(FilePath));
    }

    public static void playAudio(String FilePath, JTextPane appendButtonPane, boolean showStopButton) {
        try {
            stopMusic();
            FileInputStream FileInputStream = new FileInputStream(FilePath);
            player = new Player(FileInputStream);
            Thread MusicThread = new Thread(() -> {
                try {
                    player.play();
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }, "mp3 audio thread");

            MusicThread.start();

            if (showStopButton) {
                CyderButton stopMusicButton = new CyderButton("Stop Audio");
                stopMusicButton.addActionListener((e) -> IOUtil.stopMusic());
                StringUtil su = new StringUtil(appendButtonPane);
                su.printlnComponent(stopMusicButton,"button","button");
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void playAudio(String FilePath, JTextPane appendButtonPane) {
        playAudio(FilePath, appendButtonPane, true);
    }

    //static music player widget
    public static void stopMusic() {
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
     * zip any user data aside from userdata.bin and the Throws directory
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
                if (f.getName().equalsIgnoreCase("userdata.bin")
                        || f.getName().equalsIgnoreCase("userdata.txt"))
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

    public static void changeUsername(String newName) {
        try {
            setUserData("name", newName);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void changePassword(char[] newPassword) {
        try {
            setUserData("password", SecurityUtil.toHexString(SecurityUtil.getSHA(newPassword)));
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    @Override
    public String toString() {
        return "IOUtil object, hash=" + this.hashCode();
    }

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

    public static long getFileSize(File f) {
        long ret = 0;
        try {
            ret = Files.readAttributes(Paths.get(f.getPath()), DosFileAttributes.class).size();
        } catch (IOException e) {
            ErrorHandler.handle(e);
        }

        return ret;
    }

    public static void legacyDataToBinary(File userDataTxt) throws FatalException, IOException, InterruptedException {
        if (!userDataTxt.isFile())
            throw new FatalException("Given file is not a file");
        else if (!userDataTxt.getName().endsWith(".txt"))
            throw new FatalException("Given file is not a legacy user data file");
        else if (!userDataTxt.exists())
            throw new FatalException("Given file does not exist");

        GenesisShare.getExitingSem().acquire();

        BufferedReader legacyReader = new BufferedReader(new FileReader(userDataTxt));
        LinkedList<NST> legacyData = new LinkedList<>();
        String line = null;

        while ((line = legacyReader.readLine()) != null) {
            if (!line.contains(":"))
                throw new FatalException("Legacy data not formatting properly");

            String[] parts = line.split(":");

            if (parts.length != 2)
                throw new FatalException("Legacy data has more or less than 2 parts");

            legacyData.add(new NST(parts[0], parts[1]));
        }

        StringBuilder sb = new StringBuilder();

        for (NST data : legacyData) {
            sb.append(data.getName());
            sb.append(":");
            sb.append(data.getData());
            sb.append("\n");
        }

        File binaryUserData = new File(userDataTxt.getParentFile(), "userdata.bin");
        binaryUserData.createNewFile();

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        BufferedWriter fos = new BufferedWriter(new FileWriter(binaryUserData));

        for (byte b : bytes) {
            int result = b & 0xff;
            String resultWithPadZero = String.format("%8s", Integer.toBinaryString(result))
                    .replace(" ", "0");
            fos.write(resultWithPadZero);
        }

        fos.flush();
        fos.close();

        GenesisShare.getExitingSem().release();
    }

    public static String getBinaryString(File f) throws FatalException {
        if (!f.exists())
            throw new FatalException("bin does not exist");
        else if (!f.getName().endsWith(".bin")) {
            throw new FatalException("bin is not a binary");
        }

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

    public static String getHexString(File f) throws FatalException {
        if (!f.exists())
            throw new FatalException("bin does not exist");
        else if (!f.getName().endsWith(".bin")) {
            throw new FatalException("bin is not a binary");
        }

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
}