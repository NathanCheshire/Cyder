package cyder.utilities;

import cyder.genesis.CyderMain;
import cyder.handler.ErrorHandler;
import cyder.handler.PhotoViewer;
import cyder.handler.TextEditor;
import cyder.obj.NST;
import cyder.ui.ConsoleFrame;
import cyder.widgets.GenericInform;
import cyder.widgets.MPEGPlayer;
import javazoom.jl.player.Player;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IOUtil {

    private IOUtil () {} //private constructor to avoid object creation

    private static LinkedList<NST> userData = new LinkedList<>();
    private static LinkedList<NST> systemData = new LinkedList<>();

    private static MPEGPlayer CyderPlayer;
    private static Player player;

    public static void openFileOutsideProgram(String filePath) {
        Desktop OpenFile = Desktop.getDesktop();

        try {
            File FileToOpen = new File(filePath);
            URI FileURI = FileToOpen.toURI();
            OpenFile.browse(FileURI);
        }

        catch (Exception e) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + filePath);
            }

            catch(Exception ex) {
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

            for (String line: lines) {
                tmpFileWriter.write(line);
                tmpFileWriter.newLine();
            }

            tmpFileWriter.flush();
            tmpFileWriter.close();

            openFileOutsideProgram(tmpFile.getAbsolutePath());
        }

        catch (Exception e) {
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

    public static void readUserData() {
        userData.clear();
        String user = ConsoleFrame.getUUID();

        if (user == null)
            return;

        if (!new File("users/" + user + "/Userdata.txt").exists())
            corruptedUser();

        try (BufferedReader dataReader = new BufferedReader(new FileReader("users/" + user + "/Userdata.txt"))){

            String Line;

            while ((Line = dataReader.readLine()) != null) {
                String[] parts = Line.split(":");
                userData.add(new NST(parts[0], parts[1]));
            }
        }

        catch(Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //todo corrupted user broken
    //todo start up is broken
    //todo corrupted user inform is broken since cyderframe is broken from background color i guess

    /**
     * This method removes any repeated user data. Any repeated keys are thrown away and the first occurences are kept.
     */
    public static void fixUserData() {
        //get user var for later use
        String user = ConsoleFrame.getUUID();

        //return if no user, shouldn't be possible anyway
        if (user == null)
            return;

        //if the data file is gone then we're screwed
        if (!new File("users/" + user + "/Userdata.txt").exists())
            corruptedUser();

        //try with resources to write all the default pairs in case some are missing, only the first pairs will be saved
        // so any that we already have will be kept and any duplicates will be removed
        try (BufferedWriter userWriter = new BufferedWriter(new FileWriter(
                "users/" + ConsoleFrame.getUUID() + "/Userdata.txt", true))) {
            CyderMain.exitingSem.acquire();

            //always just add a newline to the front to be safe
            userWriter.newLine();

            //write default pairs
            for (int i = 0 ;  i < CyderMain.prefs.size() ; i++) {
                userWriter.write(CyderMain.prefs.get(i).getID() + ":" + CyderMain.prefs.get(i).getDefaultValue());
                userWriter.newLine();
            }

            CyderMain.exitingSem.release();

        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            CyderMain.exitingSem.release();
        }

        //try with resources reading all user data
        try (BufferedReader dataReader = new BufferedReader(new FileReader("users/" + user + "/Userdata.txt"))) {
            CyderMain.exitingSem.acquire();
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
                    "users/" + ConsoleFrame.getUUID() + "/Userdata.txt", false));

            for (NST currentData : reWriteData) {
                userWriter.write(currentData.getName() + ":" + currentData.getData());
                userWriter.newLine();
            }

            userWriter.close();

            CyderMain.exitingSem.release();

        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            CyderMain.exitingSem.release();
        }

    }

    public static void readSystemData() {
        systemData.clear();

        try (BufferedReader sysReader = new BufferedReader(new FileReader(
                "Sys.ini"))){

            String Line;

            while ((Line = sysReader.readLine()) != null) {
                String[] parts = Line.split(":");
                systemData.add(new NST(parts[0], parts[1]));
            }
        }

        catch(Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void writeUserData(String name, String value) {
        if (ConsoleFrame.getUUID() == null)
            return;

        try {
            CyderMain.exitingSem.acquire();

            BufferedWriter userWriter = new BufferedWriter(new FileWriter(
                    "users/" + ConsoleFrame.getUUID() + "/Userdata.txt", false));

            for (NST data : userData) {
                if (data.getName().equalsIgnoreCase(name))
                    data.setData(value);

                userWriter.write(data.getName() + ":" + data.getData());
                userWriter.newLine();
            }

            userWriter.close();
            CyderMain.exitingSem.release();
        }

        catch (Exception e) {
           ErrorHandler.handle(e);
        }
    }

    public static void writeSystemData(String name, String value) {
        try {
            CyderMain.exitingSem.acquire();
            BufferedWriter sysWriter = new BufferedWriter(new FileWriter(
                    "Sys.ini", false));

            for (NST data : systemData) {
                if (data.getName().equalsIgnoreCase(name))
                    data.setData(value);

                sysWriter.write(data.getName() + ":" + data.getData());

                sysWriter.newLine();
            }

            sysWriter.flush();
            CyderMain.exitingSem.release();
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static String getUserData(String name) {
        readUserData();

        if (userData.isEmpty())
            return null;

        for (NST data : userData) {
            if (data.getName().equalsIgnoreCase(name)) {
                return data.getData();
            }
        }

        //todo if some data doesn't exist it's auto correcupted? rethink this
        //corruptedUser();

        return null;
    }

    public static String getSystemData(String name) {
        readSystemData();

        if (systemData.isEmpty()) {
            return null;
        }

        for (NST data : systemData) {
            if (data.getName().equalsIgnoreCase(name)) {
                return data.getData();
            }
        }

        return null;
    }

    /**
     * Logs any possible command line arguments passed in to Cyder upon starting.
     * Appends the start date along with some information to StartLog.log
     * @param cyderArgs - command line arguments passed in
     */
    public static void logArgs(String[] cyderArgs) {
        try {
            File log = new File("StartLog.log");

            if (!log.exists())
                log.createNewFile();

            String argsString = "";

            for (int i = 0 ; i < cyderArgs.length ; i++) {
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

            Files.write(Paths.get("StartLog.log"), append.getBytes(), StandardOpenOption.APPEND);

        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void cleanUpUsers() {
        File top = new File("users");
        File[] users = top.listFiles();

        for (File userDir : users) {
            if (!userDir.isDirectory())
                return;

            File[] currentUserFiles = userDir.listFiles();

            if (currentUserFiles.length == 1 && currentUserFiles[0].getName().equalsIgnoreCase("Userdata.txt"))
                SystemUtil.deleteFolder(userDir);
        }
    }

    public static void wipeErrors() {
        File topDir = new File("users");
        File[] users = topDir.listFiles();

        for (File f : users) {
            if (f.isDirectory()) {
                File throwDir = new File("users/" + f.getName() + "/throws");
                if (throwDir.exists()) SystemUtil.deleteFolder(throwDir);
            }
        }

        File throwsFolder = new File("throws");
        SystemUtil.deleteFolder(throwsFolder);
    }

    public static void openFile(String FilePath) {
        //use our custom text editor
        if (FilePath.endsWith(".txt")) {
            TextEditor te = new TextEditor(FilePath);
        }

        else if (FilePath.endsWith(".png")) {
            PhotoViewer pv = new PhotoViewer(new File(FilePath));
            pv.start();
        }

        //use our own mp3 player
        else if (FilePath.endsWith(".mp3")) {
            CyderPlayer = new MPEGPlayer(new File(FilePath));
        }

        //welp just open it outside of the program :(
        else {
            Desktop OpenFile = Desktop.getDesktop();

            try {
                File FileToOpen = new File(FilePath);
                URI FileURI = FileToOpen.toURI();
                OpenFile.browse(FileURI);
            }

            catch (Exception e) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + FilePath);
                }

                catch(Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }
        }
    }

    public static void mp3(String FilePath) {
        if (CyderPlayer != null)
            CyderPlayer.kill();

        stopMusic();
        CyderPlayer = new MPEGPlayer(new File(FilePath));
    }

    public static void playAudio(String FilePath) {
        try {
            stopMusic();
            FileInputStream FileInputStream = new FileInputStream(FilePath);
            player = new Player(FileInputStream);
            Thread MusicThread = new Thread(() -> {
                try {
                    player.play();
                }

                catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            });

            MusicThread.start();
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //static music player widget
    public static void stopMusic() {
        try {
            if (player != null && !player.isComplete()) {
                player.close();
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static File getFile() {
        try {
            Desktop.getDesktop().open(new File("sys/jars/FileChooser.jar"));

            File f = new File("File.txt");
            f.delete();

            while (!f.exists()) {
                Thread.onSpinWait();
            }

            Thread.sleep(200);

            BufferedReader waitReader = new BufferedReader(new FileReader("File.txt"));

            File chosenFile = new File(waitReader.readLine());
            waitReader.close();

            f.delete();

            return (chosenFile.getName().equalsIgnoreCase("null") ? null : chosenFile);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }

    //todo this should be somewhere else?

    /**
     * If a user becomes corrupted for any reason which may be determined any way we choose,
     * this method will aquire the exiting semaphore, dispose of all frames, and attempt to
     * zip any user data aside from userdata.txt and the Throws directory
     *
     * This could fail if something has already been deleted which is fine since we want to
     * go to the starting
     */
    public static void corruptedUser() {
        try {
            //get the exiting sem to avoid any other threads exiting during this method resulting from context switching
            CyderMain.exitingSem.acquire();

            //close all open frames
            Frame[] frames = Frame.getFrames();
            for(Frame f : frames)
                f.dispose();

            //var for the folder we are zipping, if it's already gone then it really wasn't a corrupted user,
            // possibly a user deleting their account
            File mainZipFile = new File("users/" + ConsoleFrame.getUUID());
            if (mainZipFile == null || mainZipFile.listFiles() == null || mainZipFile.listFiles().length == 0)
                return;

            //confirmed that the user was corrupted so we inform the user
            GenericInform.inform("Sorry, " + SystemUtil.getWindowsUsername() + ", but your user was corrupted. " +
                    "Your data has been saved, zipped, and placed in your Downloads folder","Corrupted User");

            //delete the stuff we don't care about
            for (File f : mainZipFile.listFiles()) {
                if (f.getName().equals("Throws"))
                    SystemUtil.deleteFolder(f);
                else if (f.getName().equals("Userdata.txt"))
                    f.delete();
            }

            //zip the remaining user data
            String sourceFile = mainZipFile.getAbsolutePath();
            FileOutputStream fos = new FileOutputStream("src/Cyder_Corrupted_Userdata_" + TimeUtil.errorTime() + ".zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(sourceFile);
            zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();

            //delete the folder we just zipped since it's a duplicate
            SystemUtil.deleteFolder(mainZipFile);

            //move the zipped folder to downloads
            Files.move(Paths.get("src/Cyder_Corrupted_Userdata.zip"),
                   Paths.get("C:/Users/" + SystemUtil.getWindowsUsername() + "/Downloads/Cyder_Corrupted_Userdata.zip"));

            //release sem
            CyderMain.exitingSem.release();

            //todo go to login method instead
            System.exit(25);
        } catch (Exception e) {
            e.printStackTrace();
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
                    zipOut.closeEntry();
                }

                else {
                    zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                    zipOut.closeEntry();
                }

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
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void changeUsername(String newName) {
        try {
            readUserData();
            writeUserData("name",newName);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void changePassword(char[] newPassword) {
        try {
            readUserData();
            writeUserData("password", SecurityUtil.toHexString(SecurityUtil.getSHA(newPassword)));
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    @Override
    public String toString() {
        return "IOUtil object, hash=" + this.hashCode();
    }
}
