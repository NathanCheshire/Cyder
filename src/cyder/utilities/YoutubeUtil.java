package cyder.utilities;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import cyder.handler.ErrorHandler;
import cyder.threads.CyderThreadFactory;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class YoutubeUtil {

    private YoutubeUtil() {}

    public static void setYoutubeDLExecutablePath(String path) {
        youtubeDLExecutablePath = path;
    }

    public static String getYoutubeDLExecutablePath() {
        return youtubeDLExecutablePath;
    }

    private static String youtubeDLExecutablePath = "C:/Program Files/youtube-dl/youtube-dl.exe";

    private static ExecutorService executor = Executors.newSingleThreadExecutor(
            new CyderThreadFactory("Youtube Audio Extractor"));

    public static Future<File> download(String videoURL, String outputDir) {
        return executor.submit(() -> {
            File ret = null;

            if (ffmpegInstalled() && youtubedlInstalled()) {
                YoutubeDL.setExecutablePath(youtubeDLExecutablePath);

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

                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            } else {
                error();
            }

            return ret;
        });
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
     * @param youtubeQuery - the user friendly query on youtube. Example: "Gryffin Digital Mirage"
     * @return - the first UUID obtained from the raw html page youtube returns corresponding to the desired query
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
        environmentVariableHelp.addActionListener(e ->
                NetworkUtil.internetConnect("https://www.wikihow.com/Install-FFmpeg-on-Windows"));
        ConsoleFrame.getConsoleFrame().getInputHandler().printlnComponent(downloadFFMPEG);

        CyderButton downloadYoutubeDL = new CyderButton("Learn how to download youtube-dl");
        environmentVariableHelp.addActionListener(e ->
                NetworkUtil.internetConnect("https://github.com/ytdl-org/youtube-dl#installation"));
        ConsoleFrame.getConsoleFrame().getInputHandler().printlnComponent(downloadYoutubeDL);
    }
}
