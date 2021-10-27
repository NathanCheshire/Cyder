package cyder.utilities;

import com.google.gson.Gson;
import cyder.genesis.GenesisShare;
import cyder.genesis.Login;
import cyder.handler.*;
import cyder.ui.ConsoleFrame;
import cyder.widgets.AudioPlayer;
import cyder.widgets.GenericInformer;
import javazoom.jl.player.Player;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.DosFileAttributes;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IOUtil {
    private IOUtil() {} //private constructor to avoid object creation

    private static Player player;

    /**
     * Opens the provided file outside of the program regardless of whether or not a
     * handler exists for the file type
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
                SessionLogger.log(SessionLogger.Tag.LINK, filePath);
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }
    }

    /**
     * Creates the provided temporary file in the temp directory and writes the given string lines to it
     * @param filename the name of the file to create
     * @param extension the extension of the file to create
     * @param lines the Strings to write to the file
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
     * Returns the SystemData object representing all the system data located in sys.json
     * @return the SystemData object
     */
    public static SystemData getSystemData() {
        SystemData ret = null;
        Gson gson = new Gson();

        try (Reader reader = new FileReader("static/sys.json")) {
            ret = gson.fromJson(reader, SystemData.class);
        } catch (IOException e) {
            ErrorHandler.handle(e);
        }

        return ret;
    }

    /**
     * Writes the provided SystemData object to sys.json, overriding whatever object is represented there.
     * @param sd the SystemData object to write
     */
    public static void setSystemData(SystemData sd) {
        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter("static/sys.json")) {
            gson.toJson(sd, writer);
        } catch (IOException e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Logs any possible command line arguments passed in to Cyder upon starting.
     * Appends JVM Command Line Arguments along with the start location to the log
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
                    "[400 S. Monroe St. Tallahassee, FL]" :
                    (IPUtil.getIpdata().getCity() + ", " + IPUtil.getIpdata().getRegion()));

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
     * Fixes any user files that may be outdated via preference injection
     */
    public static void fixUsers() {
        File users = new File("dynamic/users");

        for (File user : users.listFiles()) {
            File json = new File(user + "/userdata.json");

            if (json.exists()) {
                UserUtil.updateOldJson(json);
            }
        }
     }

    /**
     * Opens the provided file, possibly inside of the program if a handler exists for it
     * @param FilePath the path to the file to open
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
            AudioPlayer.showGUI(new File(FilePath));
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
     * Plays the requested mp3 audio file using the general IOUtil JLayer player
     * @param FilePath the path to the mp3 file to play
     */
    public static void playAudio(String FilePath) {
        try {
            stopAudio();
            FileInputStream FileInputStream = new FileInputStream(FilePath);
            player = new Player(FileInputStream);
            SessionLogger.log(SessionLogger.Tag.ACTION,"[AUDIO] " + FilePath);

            new Thread(() -> {
                try {
                    player.play();
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                } finally {
                    ConsoleFrame.getConsoleFrame().revalidateAudioMenu();
                }
            }, "ioutil audio thread").start();

            ConsoleFrame.getConsoleFrame().revalidateAudioMenu();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static boolean generalAudioPlaying() {
        return player != null && !player.isComplete();
    }

    /**
     * Plays the requested system audio file using a new JLayer player
     *      (this cannot be stopped util the mpeg is finished)
     * @param FilePath the path to the mp3 file to play
     */
    public static void playSystemAudio(String FilePath) {
        try {
            FileInputStream FileInputStream = new FileInputStream(FilePath);
            Player systemPlayer = new Player(FileInputStream);

            if (!FilePath.equals("static/audio/Typing.mp3"))
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
     * Stops the audio currently playing that is absent of an AudioPlayer object
     */
    public static void stopAudio() {
        try {
            if (player != null && !player.isComplete()) {
                player.close();
                player = null;
                //set to null so that generalAudioPlaying works as intended
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        ConsoleFrame.getConsoleFrame().revalidateAudioMenu();
    }

    /**
     * Stops any and all audio playing either through flash player or the general IOUtil JLayer player
     */
    public static void stopAllAudio() {
        if (IOUtil.generalAudioPlaying()) {
            stopAudio();
        }

        if (AudioPlayer.audioPlaying()) {
            AudioPlayer.stopAudio();
        }
    }

    /**
     * Pause audio if playing via flash player
     */
    public static void pauseAudio() {
        if (AudioPlayer.audioPlaying()) {
            AudioPlayer.pauseAudio();
        } else if (IOUtil.generalAudioPlaying()) {
            stopAudio();
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
            File mainZipFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID());
            if (mainZipFile == null || mainZipFile.listFiles() == null || mainZipFile.listFiles().length == 0)
                return;

            //confirmed that the user was corrupted so we inform the user
            GenericInformer.inform("Sorry, " + SystemUtil.getWindowsUsername() + ", but your user was corrupted. " +
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

    /**
     * Changes the current user from console frame's name to the provided name.
     * @param newName the new name of the user
     */
    public static void changeUsername(String newName) {
        try {
            UserUtil.setUserData("name", newName);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Changes the current user from console frame's password to the provided password
     * @param newPassword the raw char[] new password to hash and store
     */
    public static void changePassword(char[] newPassword) {
        try {
            UserUtil.setUserData("pass", SecurityUtil.toHexString(SecurityUtil.getSHA256(
                    SecurityUtil.toHexString(SecurityUtil.getSHA256(newPassword)).toCharArray())));
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Gets DOS attributes of the provided file
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
            ErrorHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns the size of the provided file in bytes
     * @param f the file to calculate the size of
     * @return the size in bytes of the file
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
            ErrorHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns a hex string from the provided binary file
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

    /**
     * Used to test for Nathan being an idiot and having duplicate exit condition codes
     * @return - boolean describing whether or not Nathan messed up
     */
    public static boolean checkForExitCollisions() {
        boolean ret = false;

        //if there are exit conditions with the same number exit and inform
        LinkedList<Integer> exitCodes = new LinkedList<>();

        for (SystemData.ExitCondition exitCondition : IOUtil.getSystemData().getExitconditions()) {
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

    //system data class
    public static class SystemData {
        private boolean released;
        private String version;
        private String releasedate;
        private String mastermac;
        private boolean uiloc;
        private String ytt;
        private double uiscale;
        private boolean consoleresizable;
        private boolean autocypher;
        private LinkedList<Hash> cypherhashes;
        private boolean testingmode;
        private LinkedList<ExitCondition> exitconditions;
        private LinkedList<String> ignorethreads;

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

        public String getYtt() {
            return ytt;
        }

        public void setYtt(String ytt) {
            this.ytt = ytt;
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

        public LinkedList<Hash> getCypherhashes() {
            return cypherhashes;
        }

        public void setCypherhashes(LinkedList<Hash> cypherhashes) {
            this.cypherhashes = cypherhashes;
        }

        public LinkedList<ExitCondition> getExitconditions() {
            return exitconditions;
        }

        public void setExitconditions(LinkedList<ExitCondition> exitconditions) {
            this.exitconditions = exitconditions;
        }

        public boolean isTestingmode() {
            return testingmode;
        }

        public void setTestingmode(boolean testingmode) {
            this.testingmode = testingmode;
        }

        public LinkedList<String> getIgnorethreads() {
            return ignorethreads;
        }

        public void setIgnorethreads(LinkedList<String> ignorethreads) {
            this.ignorethreads = ignorethreads;
        }

        public static class Hash {
            private String name;
            private String hashpass;

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
        }
    }
}