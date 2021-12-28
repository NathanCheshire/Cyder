package cyder.utilities;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.threads.CyderThreadFactory;
import cyder.ui.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeUtil {

    private YoutubeUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    private static ExecutorService executor = Executors.newSingleThreadExecutor(
            new CyderThreadFactory("Youtube Audio Extractor"));

    public static Future<LinkedList<File>> downloadPlaylist(String playlistID, String outputDir) {
        return executor.submit(() -> {
            LinkedList<File> ret = null;

            if (ffmpegInstalled() && youtubedlInstalled()) {
                String ydlPath = UserUtil.extractUser().getYoutubedlpath();
                if (ydlPath != null && ydlPath.trim().length() > 0) {
                    YoutubeDL.setExecutablePath(ydlPath);
                }

                try {
                    //req build
                    YoutubeDLRequest request =
                            new YoutubeDLRequest( "https://www.youtube.com/playlist?list=" + playlistID, outputDir);
                    request.setOption("ignore-errors");
                    request.setOption("extract-audio");
                    request.setOption("audio-format", "mp3");
                    request.setOption("output", "%(title)s.%(ext)s");
                    request.setOption("yes-playlist", "https://www.youtube.com/playlist?list=" + playlistID);

                    //req and response ret
                    YoutubeDLResponse response = YoutubeDL.execute(request);
                    response.getOut();

                    String[] outLines = response.getOut().split("\n");
                    LinkedList<String> fileNames = new LinkedList<>();

                    for (String line: outLines) {
                        if (line.contains("[ffmpeg] Destination:")) {
                            String currentTitle = line.replace("[ffmpeg] Destination:","").trim();

                            if (!fileNames.contains(currentTitle))
                                fileNames.add(currentTitle);
                        }
                    }

                    //build file objects to return
                    for (String fileName : fileNames) {
                        ret.add(new File(response.getDirectory() + fileName));
                    }
                } catch (Exception e) {
                    ErrorHandler.silentHandle(e);
                    ConsoleFrame.getConsoleFrame().getInputHandler().println("Could not download video's audio at this time");
                }
            } else {
                error();
            }

            return ret;
        });
    }

    public static Future<File> download(String videoURL, String outputDir) {
        return executor.submit(() -> {
            File ret = null;

            if (ffmpegInstalled() && youtubedlInstalled()) {
                String ydlPath = UserUtil.extractUser().getYoutubedlpath();
                if (ydlPath != null && ydlPath.trim().length() > 0) {
                    YoutubeDL.setExecutablePath(ydlPath);
                }

                try {
                    //req build
                    YoutubeDLRequest request = new YoutubeDLRequest(videoURL, outputDir);
                    request.setOption("ignore-errors");
                    request.setOption("extract-audio");
                    request.setOption("audio-format","mp3");
                    request.setOption("output", "%(title)s.%(ext)s");

                    //req and response ret
                    YoutubeDLResponse response = YoutubeDL.execute(request);
                    response.getOut();

                    String[] outLines = response.getOut().split("\n");
                    String outName = "NULL";

                    for (String line: outLines) {
                        if (line.contains("[ffmpeg] Destination:")) {
                            outName = line.replace("[ffmpeg] Destination:","").trim();
                            break;
                        }
                    }

                    ret = new File(response.getDirectory() + outName);
                } catch (YoutubeDLException e) {
                    ErrorHandler.silentHandle(e);
                    ConsoleFrame.getConsoleFrame().getInputHandler().println("Could not download video's audio at this time");
                }
            } else {
                error();
            }

            //make sure the audio was downloaded
            if (ret.exists()) {
                try {
                    BufferedImage save = getSquareThumbnail(videoURL);
                    String name = StringUtil.getFilename(ret) + ".png";

                    File albumArtDir = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/AlbumArt");

                    if (!albumArtDir.exists())
                        albumArtDir.mkdir();

                    File saveFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID()
                            + "/Music/AlbumArt/" + name);
                    ImageIO.write(save, "png", saveFile);
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }

            return ret;
        });
    }

    public static boolean isPlaylist(String youtubeURL) {
        return youtubeURL.startsWith("https://www.youtube.com/playlist?list=");
    }

    public static boolean ffmpegInstalled() {
        boolean ret = true;

        try {
            Runtime rt = Runtime.getRuntime();
            String command = "ffmpeg";
            Process proc = rt.exec(command);
        } catch (Exception e) {
            ret = false;
            ErrorHandler.silentHandle(e);
        } finally {
            return ret;
        }
    }

    public static boolean youtubedlInstalled() {
        boolean ret = true;

        try {
            Runtime rt = Runtime.getRuntime();
            String command = "youtube-dl";
            Process proc = rt.exec(command);
        } catch (Exception e) {
            ret = false;
            ErrorHandler.silentHandle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Retreives the first valid UUID (if one exists)
     * @param youtubeQuery the user friendly query on youtube. Example: "Gryffin Digital Mirage"
     * @return the first UUID obtained from the raw html page youtube returns corresponding to the desired query
     */
    public static String getFirstUUID(String youtubeQuery) {
        String ret = null;

        String query = "https://www.youtube.com/results?search_query=" + youtubeQuery.replace(" ", "+");
        String jsonString = NetworkUtil.readUrl(query);

        if (jsonString.contains("\"videoId\":\"")) {
            String[] parts = jsonString.split("\"videoId\":\"");
            ret =  parts[1].substring(0,11);
        }

        return ret;
    }

    private static void error() {
        ConsoleFrame.getConsoleFrame().getInputHandler().println("Sorry, but ffmpeg and/or youtube-dl " +
                "couldn't be located. Please make sure they are both installed and added to your PATH Windows" +
                " variable");
        ConsoleFrame.getConsoleFrame().getInputHandler().println(
                "Remember to also set the path to your youtube-dl executable in the user editor");

        CyderButton environmentVariableHelp = new CyderButton("Learn how to add environment variables");
        environmentVariableHelp.addActionListener(e ->
                NetworkUtil.internetConnect("https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10/"));
        ConsoleFrame.getConsoleFrame().getInputHandler().printlnComponent(environmentVariableHelp);

        CyderButton downloadFFMPEG = new CyderButton("Learn how to download ffmpeg");
        downloadFFMPEG.addActionListener(e ->
                NetworkUtil.internetConnect("https://www.wikihow.com/Install-FFmpeg-on-Windows"));
        ConsoleFrame.getConsoleFrame().getInputHandler().printlnComponent(downloadFFMPEG);

        CyderButton downloadYoutubeDL = new CyderButton("Learn how to download youtube-dl");
        downloadYoutubeDL.addActionListener(e ->
                NetworkUtil.internetConnect("https://github.com/ytdl-org/youtube-dl#installation"));
        ConsoleFrame.getConsoleFrame().getInputHandler().printlnComponent(downloadYoutubeDL);
    }

    public static void showGUI() {
        CyderFrame uuidFrame = new CyderFrame(400,240, CyderImages.defaultBackground);
        uuidFrame.setTitle("Thumbnail Stealer");
        uuidFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);

        CyderLabel label = new CyderLabel("Enter any valid YouTube UUID");
        label.setFont(label.getFont().deriveFont(22f));
        int labelWidth = CyderFrame.getMinWidth("Enter any valid YouTube UUID", label.getFont());
        label.setBounds(400 / 2 - labelWidth / 2, 60, labelWidth, 30);
        uuidFrame.add(label);

        CyderTextField inputField = new CyderTextField(30);
        inputField.setBounds(200 - labelWidth / 2, 100, labelWidth, 40);
        inputField.setToolTipText("Must be a valid UUID");
        uuidFrame.add(inputField);

        CyderButton stealButton = new CyderButton("Submit");
        stealButton.setBounds(200 - labelWidth / 2, 160, labelWidth, 40);
        uuidFrame.add(stealButton);
        stealButton.setToolTipText("View Thumbnail");
        stealButton.addActionListener(e -> {
            try {
                BufferedImage Thumbnail = ImageIO.read(new URL(
                        "https://img.youtube.com/vi/" + inputField.getText().trim() + "/maxresdefault.jpg"));
                Thumbnail = ImageUtil.resizeImage(Thumbnail, Thumbnail.getType(), Thumbnail.getWidth(), Thumbnail.getHeight());

                CyderFrame thumbnailFrame = new CyderFrame(Thumbnail.getWidth() + 10, Thumbnail.getHeight() + 60, new ImageIcon(Thumbnail));
                thumbnailFrame.setBackground(CyderColors.navy);
                thumbnailFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
                thumbnailFrame.setTitle(inputField.getText().trim());

                CyderButton addToBackgrounds = new CyderButton("Set as background");
                addToBackgrounds.setBounds(10, Thumbnail.getHeight() + 10, (Thumbnail.getWidth() - 30) / 2 , 40);
                addToBackgrounds.addActionListener(e1 -> {
                    String thumbnailURL = "https://img.youtube.com/vi/" + inputField.getText().trim() + "/maxresdefault.jpg";
                    try {
                        BufferedImage save = ImageIO.read(new URL(thumbnailURL));
                        File saveFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID()
                                + "/Backgrounds/" +inputField.getText().trim() + ".png");
                        ImageIO.write(save, "png", saveFile);
                        thumbnailFrame.notify("Successfully saved as a background file." +
                                " You may view this by switching the background or by typing \"prefs\" " +
                                "to view your profile settings.");
                    } catch (IOException ex) {
                        ErrorHandler.handle(ex);
                    }
                });
                thumbnailFrame.add(addToBackgrounds);

                CyderButton openVideo = new CyderButton("Open Video");
                openVideo.setBounds(20 + addToBackgrounds.getWidth(),
                        Thumbnail.getHeight() + 10, (Thumbnail.getWidth() - 30) / 2, 40);
                openVideo.addActionListener(e1 -> NetworkUtil.internetConnect("https://www.youtube.com/watch?v=" + inputField.getText().trim()));
                thumbnailFrame.add(openVideo);

                thumbnailFrame.setVisible(true);
                thumbnailFrame.setLocationRelativeTo(uuidFrame);
                uuidFrame.dispose();
            }

            catch (Exception exc) {
                uuidFrame.notify("Invalid YouTube UUID");
            }
        });

        uuidFrame.setVisible(true);
        uuidFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static BufferedImage getSquareThumbnail(String videoURL) {
        String uuid = getYoutubeUUID(videoURL);

        String thumbnailURL = "https://img.youtube.com/vi/" + uuid + "/maxresdefault.jpg";
        BufferedImage ret = null;

        try {
            BufferedImage save = ImageIO.read(new URL(thumbnailURL));
            int w = save.getWidth();
            int h = save.getHeight();

            if (w > 720) {
                //crop to middle of w
                int cropWStart = (w - 720) / 2;
                BufferedImage croppedWidth = save.getSubimage(cropWStart, 0, 720, h);
                save = croppedWidth;
            }

            w = save.getWidth();
            h = save.getHeight();

            if (h > 720) {
                //crop to middle of h
                int cropHStart = (h - 720) / 2;
                BufferedImage croppedHeight = save.getSubimage(0, cropHStart, w, 720);
                save = croppedHeight;
            }

            ret = save;

        } catch (IOException ex) {
            ErrorHandler.handle(ex);
        }

        return ret;
    }

    public static String getYoutubeUUID(String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed/)[^#&?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);

        if(matcher.find()){
            return matcher.group();
        } else throw new RuntimeException("No UUID found in provided string: " + youTubeUrl);
    }
}
