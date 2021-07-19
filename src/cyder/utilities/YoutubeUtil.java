package cyder.utilities;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;

public class YoutubeUtil {
    //TODO this functionality requires both ffmpeg and youtube-dl
    //TODO it also requires paths to each executibles

    public static void test() {
        //set paths
        YoutubeDL.setExecutablePath("C:/Program Files/youtube-dl/youtube-dl.exe");

        try {
            //vars
            String videoUrl = "https://www.youtube.com/watch?v=AoXewWu38ac";
            String outputDir = "users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/";

            //req build
            YoutubeDLRequest request = new YoutubeDLRequest(videoUrl, outputDir);
            request.setOption("ignore-errors");
            request.setOption("extract-audio");
            request.setOption("audio-format","mp3");
            request.setOption("output", "%(title)s.%(ext)s");

            //req and response ret
            YoutubeDLResponse response = YoutubeDL.execute(request);

            //show user response from yt-dl / ffmpeg
            String stdOut = response.getOut(); // Executable output
            ConsoleFrame.getConsoleFrame().getInputHandler().println(stdOut);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
