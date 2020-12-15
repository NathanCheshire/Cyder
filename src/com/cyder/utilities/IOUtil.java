package com.cyder.utilities;

import com.cyder.obj.NST;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.LinkedList;

public class IOUtil {
    private static LinkedList<NST> userData = new LinkedList<>();

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
                new GeneralUtil().handle(ex);
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
            new GeneralUtil().handle(e);
        }
    }

    public static void deleteTempDir() {
        try {
            File tmpDir = new File("src/tmp");
            new SystemUtil().deleteFolder(tmpDir);
        } catch (Exception e) {
            new GeneralUtil().handle(e);
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
            GeneralUtil.handle(e);
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
            GeneralUtil.handle(e);
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
}
