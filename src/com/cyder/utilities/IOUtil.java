package com.cyder.utilities;

import com.cyder.genesis.CyderMain;
import com.cyder.handler.ErrorHandler;
import com.cyder.handler.PhotoViewer;
import com.cyder.handler.TextEditor;
import com.cyder.obj.NST;
import com.cyder.ui.ConsoleFrame;
import com.cyder.widgets.GenericInform;
import com.cyder.widgets.MPEGPlayer;
import javazoom.jl.player.Player;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IOUtil {
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

        else if (!new File("src/users/" + user + "/Userdata.txt").exists())
            corruptedUser();

        try (BufferedReader dataReader = new BufferedReader(new FileReader("src/users/" + user + "/Userdata.txt"))){

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

    public static void readSystemData() {
        systemData.clear();

        try (BufferedReader sysReader = new BufferedReader(new FileReader(
                "src/com/cyder/genesis/Sys.ini"))){

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
                    "src/users/" + ConsoleFrame.getUUID() + "/Userdata.txt", false));

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
                    "src/com/cyder/genesis/Sys.ini", false));

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

        corruptedUser();
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

    public static void logArgs(String[] cyderArgs) {
        try {
            if (cyderArgs.length == 0)
                cyderArgs = new String[]{"Started by " + System.getProperty("user.name")};

            File log = new File("src/com/cyder/genesis/StartLog.log");

            if (!log.exists())
                log.createNewFile();

            BufferedReader br = new BufferedReader(new FileReader(log));

            LinkedList<String> dates = new LinkedList<>();

            String line;
            boolean section0 = true;

            while ((line = br.readLine()) != null)
                dates.add(line);

            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(log,false));

            String argsString = "";

            for (int i = 0 ; i < cyderArgs.length ; i++) {
                if (i != 0)
                    argsString += ",";
                argsString += cyderArgs[i];
            }

            IPUtil ipu = new IPUtil();

            dates.push(new SimpleDateFormat("MM-dd-yy HH:mm:ss").format(new Date())
                    + " : " + argsString + " in " + ipu.getUserCity() + ", " + ipu.getUserState());

            for (String lin : dates) {
                bw.write(lin);
                bw.newLine();
            }

            bw.flush();
            bw.close();
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void cleanUpUsers() {
        File top = new File("src/users");
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
        File topDir = new File("src/users");
        File[] users = topDir.listFiles();

        for (File f : users) {
            if (f.isDirectory()) {
                File throwDir = new File("src/users/" + f.getName() + "/throws");
                if (throwDir.exists()) SystemUtil.deleteFolder(throwDir);
            }
        }
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
            Desktop.getDesktop().open(new File("src/com/cyder/sys/jars/FileChooser.jar"));

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

    public static void corruptedUser() {
        try {
            CyderMain.exitingSem.acquire();

            Frame[] frames = Frame.getFrames();

            for(Frame f : frames)
                f.dispose();

            GenericInform.inform("Sorry, " + SystemUtil.getWindowsUsername() + ", but your user was corrupted. " +
                    "Your data has been saved, zipped, and placed in your Downloads folder","Corrupted User");
            Thread.sleep(7000);

            File mainZipFile = new File("src/users/" + ConsoleFrame.getUUID());

            for (File f : mainZipFile.listFiles()) {
                if (f.getName().equals("Throws"))
                    SystemUtil.deleteFolder(f);
                else if (f.getName().equals("Userdata.txt"))
                    f.delete();
            }

            String sourceFile = mainZipFile.getAbsolutePath();

            FileOutputStream fos = new FileOutputStream("Cyder_Corrupted_Userdata_" + TimeUtil.errorTime() + ".zip");

            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(sourceFile);

            zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();

            SystemUtil.deleteFolder(mainZipFile);

            Files.move(Paths.get("Cyder_Corrupted_Userdata.zip"),
                   Paths.get("C:/Users/" + SystemUtil.getWindowsUsername() + "/Downloads/Cyder_Corrupted_Userdata.zip"));

            System.exit(78);
        } catch (Exception e) {
            e.printStackTrace();
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
}
