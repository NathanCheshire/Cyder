package cyder.utilities;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;

public class YoutubeUtil {
    private YoutubeUtil() {}

    public static void setYoutubeDLExecutablePath(String path) {
        youtubeDLExecutablePath = path;
    }

    public static String getYoutubeDLExecutablePath() {
        return youtubeDLExecutablePath;
    }

    private static String youtubeDLExecutablePath = "C:/Program Files/youtube-dl/youtube-dl.exe";

    public static void download(String videoURL) {
        if (ffmpegInstalled() && youtubedlInstalled()) {
            download(videoURL, "users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/");
        } else {
            error();
        }
    }

    public static void download(String videoURL, String outputDir) {
        if (ffmpegInstalled() && youtubedlInstalled()) {
            YoutubeDL.setExecutablePath(youtubeDLExecutablePath);

            try {
                new Thread(() -> {
                    try {
                        //req build
                        YoutubeDLRequest request = new YoutubeDLRequest(videoURL, outputDir);
                        request.setOption("ignore-errors");
                        request.setOption("extract-audio");
                        request.setOption("audio-format","mp3");
                        request.setOption("output", "%(title)s.%(ext)s");

                        //req and response ret
                        YoutubeDLResponse response = YoutubeDL.execute(request);

                        //show user response from yt-dl / ffmpeg
                        //String stdOut = response.getOut(); // Executable output
                        //ConsoleFrame.getConsoleFrame().getInputHandler().println(stdOut);
                    } catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                }, "Youtube Audio Extractor").start();
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        } else {
            error();
        }
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

    private static void error() {
        ConsoleFrame.getConsoleFrame().getInputHandler().println("Sorry, but ffmpeg and/or youtube-dl " +
                "couldn't be located. Please make sure they are both installed and added to your PATH windows" +
                " variable");
        ConsoleFrame.getConsoleFrame().getInputHandler().println("Click the following button to go the download pages" +
                " for ffmpeg and youtube-dl\n" +
                "Remember to also set the path to your youtube-dl executable in the user editor");

        //todo adding environment variable button
        //todo downloading ffmpeg button
        //todo download youtube-dl button
    }
}
