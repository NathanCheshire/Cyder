package com.cyder.utilities;

import com.cyder.handler.ErrorHandler;
import com.cyder.handler.PhotoViewer;
import com.cyder.handler.TextEditor;
import com.cyder.obj.NST;
import com.cyder.widgets.MPEGPlayer;
import javazoom.jl.player.Player;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

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
            new SystemUtil().deleteFolder(tmpDir);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void readUserData() {
        userData.clear();
        String user = GeneralUtil.getUserUUID();

        if (user == null)
            return;

        try (BufferedReader dataReader = new BufferedReader(new FileReader(
                "src/users/" + user + "/Userdata.txt"))){

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
                "src/Sys.log"))){

            String Line;

            while ((Line = sysReader.readLine()) != null) {
                if (Line.equals("----------------------------------------"))
                    systemData.add(new NST("sep", "----------------------------------------"));
                else {
                    String[] parts = Line.split(":");
                    systemData.add(new NST(parts[0], parts[1]));
                }
            }
        }

        catch(Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void writeUserData(String name, String value) {
        if (GeneralUtil.getUserUUID() == null)
            return;

        try (BufferedWriter userWriter = new BufferedWriter(new FileWriter(
                "src/users/" + GeneralUtil.getUserUUID() + "/Userdata.txt", false))) {


            for (NST data : userData) {
                if (data.getName().equalsIgnoreCase(name))
                    data.setData(value);

                userWriter.write(data.getName() + ":" + data.getData());
                userWriter.newLine();
            }
        }

        catch (Exception e) {
           ErrorHandler.handle(e);
        }
    }

    public static void writeSystemData(String name, String value) {
        try (BufferedWriter sysWriter = new BufferedWriter(new FileWriter(
                "src/Sys.log", false))) {

            for (NST data : systemData) {
                if (data.getName().equalsIgnoreCase(name))
                    data.setData(value);

                if (data.getName().equals("sep"))
                    sysWriter.write("----------------------------------------");
                else
                    sysWriter.write(data.getName() + ":" + data.getData());

                sysWriter.newLine();
            }
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

        return null;
    }

    public static String getSystemData(String name) {
        readSystemData();

        if (systemData.isEmpty())
            return null;

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

            File log = new File("src/Sys.log");

            if (!log.exists())
                log.createNewFile();

            BufferedReader br = new BufferedReader(new FileReader(log));

            LinkedList<String> dates = new LinkedList<>();
            LinkedList<String> data = new LinkedList<>();

            String line;
            boolean section0 = true;

            while ((line = br.readLine()) != null) {
                if (section0) {
                    if (!line.equals("----------------------------------------"))
                        data.add(line);
                    else {
                        data.add(line);
                        section0 = false;
                    }
                }

                else {
                    dates.add(line);
                }
            }

            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(log,false));

            String argsString = "";

            for (int i = 0 ; i < cyderArgs.length ; i++) {
                if (i != 0)
                    argsString += ",";
                argsString += cyderArgs[i];
            }

            IPUtil ipu = new IPUtil();

            dates.push(new SimpleDateFormat("MM-dd-yy HH:mm:ss").format(new Date()) + " : " + argsString + " in " + ipu.getUserCity() + ", " + ipu.getUserState());

            for (String d : data) {
                bw.write(d);
                bw.newLine();
            }

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
                new SystemUtil().deleteFolder(userDir);
        }
    }

    public static void wipeErrors() {
        File topDir = new File("src/users");
        File[] users = topDir.listFiles();

        for (File f : users) {
            if (f.isDirectory()) {
                File throwDir = new File("src/users/" + f.getName() + "/throws");
                if (throwDir.exists()) new SystemUtil().deleteFolder(throwDir);
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
            CyderPlayer = new MPEGPlayer(new File(FilePath), GeneralUtil.getUsername(), GeneralUtil.getUserUUID());
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

    public static void mp3(String FilePath, String user, String uuid) {
        if (CyderPlayer != null)
            CyderPlayer.kill();

        stopMusic();
        CyderPlayer = new MPEGPlayer(new File(FilePath), user, uuid);
    }

    public static void playMusic(String FilePath) {
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
}
