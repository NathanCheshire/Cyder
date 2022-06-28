package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import cyder.audio.AudioPlayer;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.external.TextViewer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.parsers.UsbResponse;
import cyder.threads.CyderThreadRunner;
import javazoom.jl.player.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.regex.Matcher;

public final class IOUtil {
    /**
     * No objects of util methods allowed.
     */
    private IOUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Player used to play general audio files that may be user terminated.
     */
    private static Player player;

    /**
     * Opens the provided file outside of the program regardless of whether a
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
                Logger.log(Logger.Tag.LINK, filePath);
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }
    }

    /**
     * Determines whether the provided string is a link or a file/directory path and then opens it.
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
     * The element class when scraping the user's primary location from Google.
     */
    private static final String PRIMARY_LOCATION_CLASS = "desktop-title-content";

    /**
     * The element class when scraping the user's secondary location from Google.
     */
    private static final String SECONDARY_LOCATION_CLASS = "desktop-title-subcontent";

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

                for (int i = 0 ; i < cyderArgs.length ; i++) {
                    if (i != 0) {
                        argBuilder.append(",");
                    }

                    argBuilder.append(cyderArgs[i]);
                }

                Document locationDocument = Jsoup.connect(CyderUrls.LOCATION_URL).get();
                Elements primaryLocation = locationDocument.getElementsByClass(PRIMARY_LOCATION_CLASS);
                Elements secondaryLocation = locationDocument.getElementsByClass(SECONDARY_LOCATION_CLASS);

                String isp = "NOT FOUND";

                String[] lines = NetworkUtil.readUrl(CyderUrls.ISP_URL).split("\n");

                for (String line : lines) {
                    Matcher matcher = CyderRegexPatterns.whereAmIPattern.matcher(line);

                    if (matcher.find()) {
                        isp = matcher.group(1);
                    }
                }

                if (argBuilder.length() > 0) {
                    argBuilder.append("; ");
                }

                argBuilder.append("primary location = ")
                        .append(primaryLocation.text())
                        .append(", secondary location = ")
                        .append(secondaryLocation.text());

                if (!isp.isEmpty()) {
                    argBuilder.append(", isp = ").append(StringUtil.capsFirstWords(isp));
                }

                // only log if autoCypher, means either Nathan or an advanced developer
                if (!PropLoader.getBoolean("autocypher")) {
                    Logger.log(Logger.Tag.JVM_ARGS, argBuilder);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "JVM Args Logger");
    }

    /**
     * Opens the provided file, possibly inside of the program if a handler exists for it.
     *
     * @param filePath the path to the file to open
     */
    public static void openFile(String filePath) {
        // create file object
        File file = new File(filePath);

        // check for Cyder support
        if (filePath.endsWith(".txt")) {
            TextViewer.getInstance(file).showGui();
            return;
        } else if (FileUtil.isSupportedImageExtension(new File(filePath))) {
            PhotoViewer.getInstance(file).showGui();
            return;
        } else if (FileUtil.isSupportedAudioExtension(new File(filePath))) {
            AudioPlayer.showGui(file);
            return;
        }

        // in the end, just open it on the host OS

        Desktop OpenFile = Desktop.getDesktop();

        try {
            OpenFile.browse(file.toURI());
            Logger.log(Logger.Tag.LINK, file.getAbsoluteFile());
        } catch (Exception e) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + filePath);
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
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
            stopGeneralAudio();
            FileInputStream FileInputStream = new FileInputStream(FilePath);
            player = new Player(FileInputStream);
            Logger.log(Logger.Tag.AUDIO, FilePath);

            CyderThreadRunner.submit(() -> {
                try {
                    player.play();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                } finally {
                    ConsoleFrame.INSTANCE.revalidateAudioMenu();
                }
            }, "IOUtil audio thread");
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns whether general audio is playing.
     *
     * @return whether general audio is playing
     */
    public static boolean generalAudioPlaying() {
        return player != null && !player.isComplete();
    }

    /**
     * Plays the requested audio file using a new JLayer Player object.
     * (this cannot be stopped util the mpeg is finished)
     *
     * @param filePath the path to the audio file to play
     */
    public static void playSystemAudio(String filePath) {
        playSystemAudio(filePath, true);
    }

    /**
     * Plays the requested audio file using a new JLayer Player object.
     * (this cannot be stopped util the mpeg is finished)
     *
     * @param filePath the path to the audio file to play
     * @param log      whether to log the system audio play request
     */
    public static void playSystemAudio(String filePath, boolean log) {
        try {
            FileInputStream FileInputStream = new FileInputStream(filePath);
            Player systemPlayer = new Player(FileInputStream);

            if (log) {
                Logger.log(Logger.Tag.AUDIO, "[SYSTEM AUDIO] " + filePath);
            }

            CyderThreadRunner.submit(() -> {
                try {
                    systemPlayer.play();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "System Audio Player");
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Stops the audio currently playing. Note that this does not include
     * any system audio or AudioPlayer widget audio.
     */
    public static void stopGeneralAudio() {
        try {
            if (player != null && !player.isComplete()) {
                player.close();
                player = null;
                //set to null so that generalAudioPlaying works as intended
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            ConsoleFrame.INSTANCE.revalidateAudioMenu();
        }
    }

    /**
     * Stops any and all audio playing either through the audio player or the general player.
     */
    public static void stopAllAudio() {
        if (generalAudioPlaying()) {
            stopGeneralAudio();
        }

        if (AudioPlayer.isAudioPlaying()) {
            AudioPlayer.handlePlayPauseButtonClick();
        }
    }

    /**
     * Pause audio if playing via AudioPlayer.
     */
    public static void pauseAudio() {
        if (AudioPlayer.isAudioPlaying()) {
            AudioPlayer.handlePlayPauseButtonClick();
        } else if (generalAudioPlaying()) {
            stopGeneralAudio();
        }
    }

    /**
     * Changes the current user from console frame's name to the provided name.
     *
     * @param newName the new name of the user
     */
    public static void changeUsername(String newName) {
        Preconditions.checkNotNull(newName);
        Preconditions.checkArgument(!newName.isEmpty());
        UserUtil.getCyderUser().setName(newName);
    }

    /**
     * Changes the current user from console frame's password to the provided password.
     *
     * @param newPassword the raw char[] new password to hash and store
     */
    public static void changePassword(char[] newPassword) {
        Preconditions.checkNotNull(newPassword);
        Preconditions.checkArgument(newPassword.length > 0);
        UserUtil.getCyderUser().setPass(SecurityUtil.toHexString(SecurityUtil.getSHA256(
                SecurityUtil.toHexString(SecurityUtil.getSHA256(newPassword)).toCharArray())));
    }

    private static record DosAttribute(String name, String value) {
    }

    /**
     * Gets DOS attributes of the provided file.
     *
     * @param file the file to obtain the attributes of
     * @return the DOS attributes of the file
     */
    public static ImmutableList<DosAttribute> getDOSAttributes(File file) {
        try {
            DosFileAttributes attr = Files.readAttributes(Paths.get(file.getPath()), DosFileAttributes.class);
            return ImmutableList.of(
                    new DosAttribute("isArchive", String.valueOf(attr.isArchive())),
                    new DosAttribute("isHidden", String.valueOf(attr.isHidden())),
                    new DosAttribute("isReadOnly", String.valueOf(attr.isReadOnly())),
                    new DosAttribute("isSystem", String.valueOf(attr.isSystem())),
                    new DosAttribute("creationTime", String.valueOf(attr.creationTime())),
                    new DosAttribute("isDirectory", String.valueOf(attr.isDirectory())),
                    new DosAttribute("isOther", String.valueOf(attr.isOther())),
                    new DosAttribute("isSymbolicLink", String.valueOf(attr.isSymbolicLink())),
                    new DosAttribute("lastAccessTime", String.valueOf(attr.lastAccessTime())),
                    new DosAttribute("lastModifiedTime", String.valueOf(attr.lastModifiedTime())));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ImmutableList.of();
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
     * The location for a usb get request.
     */
    private static final String USB_GET_LOCATION = BackendUtil.constructPath("usb", "devices");

    /**
     * The encoding used for a usb get request.
     */
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * The gson object used to serialize a usb get response.
     */
    private static final Gson gson = new Gson();

    /**
     * Executes the usb_q.py script to find the devices connected to the PC via a USB protocol.
     */
    public static ArrayList<String> getUsbDevices() {
        ArrayList<String> ret = new ArrayList<>();

        try {
            URL url = new URL(USB_GET_LOCATION);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    con.getInputStream(), ENCODING))) {
                StringBuilder response = new StringBuilder();
                String responseLine;

                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                responseLine = response.toString().replace("'", "");

                UsbResponse usbResponse = gson.fromJson(responseLine, UsbResponse.class);

                ret = usbResponse.parse();
            }

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }
}