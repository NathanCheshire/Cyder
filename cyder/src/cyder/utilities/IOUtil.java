package cyder.utilities;

import cyder.constants.CyderStrings;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.handlers.external.AudioPlayer;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.external.TextViewer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.ConsoleFrame;
import javazoom.jl.player.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOUtil {
    /**
     * No objects of util methods allowed.
     */
    private IOUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
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
                Logger.log(LoggerTag.LINK, filePath);
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
        boolean validLink;

        try {
            URL url = new URL(fileOrLink);
            URLConnection conn = url.openConnection();
            conn.connect();
            validLink = true;
        } catch (Exception ex) {
            validLink = false;
        }

        if (validLink) {
            NetworkUtil.openUrl(fileOrLink);
        } else {
            openFileOutsideProgram(fileOrLink);
        }
    }

    /**
     * Logs any possible command line arguments passed in to Cyder upon starting.
     * Appends JVM Command Line Arguments along with the start location to the log.
     *
     * @param cyderArgs command line arguments passed in
     */
    public static void logArgs(String[] cyderArgs) {
        CyderThreadRunner.submit(() -> {
            try {
                // build string of all JVM args
                StringBuilder argBuilder = new StringBuilder();

                for (int i = 0; i < cyderArgs.length; i++) {
                    if (i != 0)
                        argBuilder.append(",");

                    argBuilder.append(cyderArgs[i]);
                }

                String locationUrl = "https://www.google.com/search?q=where+am+i";
                String ispUrl = "https://www.whoismyisp.org/";

                Document locationDocument = Jsoup.connect(locationUrl).get();
                Elements primaryLocation = locationDocument.getElementsByClass("desktop-title-content");
                Elements secondaryLocation = locationDocument.getElementsByClass("desktop-title-subcontent");

                String isp = "NOT FOUND";

                String[] lines = NetworkUtil.readUrl(ispUrl).split("\n");

                Pattern p = Pattern.compile("^\\s*<p class=\"isp\">(.*)</p>\\s*$");

                for (String line : lines) {
                    Matcher matcher = p.matcher(line);
                    if (matcher.find()) {
                        isp = matcher.group(1);
                    }
                }

                if (argBuilder.length() > 0) {
                    argBuilder.append("; ");
                }

                argBuilder.append("primary location = ").append(primaryLocation.text())
                        .append(", secondary location = ").append(secondaryLocation.text())
                        .append(", isp = ").append(StringUtil.capsFirst(isp));

                // only log if autoCypher, means either Nathan or an advanced developer
                if (!CyderShare.isAutoCypher())
                    Logger.log(LoggerTag.JVM_ARGS, argBuilder);

                BackendUtil.put(String.valueOf(argBuilder), BackendUtil.JVM_PATH);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "JVM Logger");
    }

    /**
     * Opens the provided file, possibly inside of the program if a handler exists for it.
     *
     * @param filePath the path to the file to open
     */
    public static void openFile(String filePath) {
        File file = new File(filePath);

        if (filePath.endsWith(".txt")) {
            TextViewer.getInstance(file).showGUI();
        } else if (FileUtil.isSupportedImageExtension(new File(filePath))) {
            PhotoViewer.getInstance(file).showGUI();
        } else if (filePath.endsWith(".mp3")) {
            AudioPlayer.showGUI(file);
        } else {
            Desktop OpenFile = Desktop.getDesktop();

            try {
                OpenFile.browse(file.toURI());
                Logger.log(LoggerTag.LINK, file.getAbsoluteFile());
            } catch (Exception e) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + filePath);
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
            Logger.log(LoggerTag.AUDIO, FilePath);

            CyderThreadRunner.submit(() -> {
                try {
                    player.play();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                } finally {
                    ConsoleFrame.getConsoleFrame().revalidateAudioMenu();
                }
            }, "IOUtil audio thread");

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
                Logger.log(LoggerTag.AUDIO,"[SYSTEM AUDIO] " + FilePath);
            CyderThreadRunner.submit(() -> {
                try {
                    systemPlayer.play();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "system audio thread");
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
        if (generalAudioPlaying()) {
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
        } else if (generalAudioPlaying()) {
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
    @SuppressWarnings("unused")
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
        if (!FileUtil.getExtension(f).equalsIgnoreCase(".bin"))
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
        if (!FileUtil.getExtension(f).equalsIgnoreCase(".bin"))
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
        File sandbox = new File("static/sandbox");

        if (!sandbox.exists()) {
            //noinspection ResultOfMethodCallIgnored
            sandbox.mkdir();
        }
    }
}