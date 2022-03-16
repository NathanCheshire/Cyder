package cyder.constants;

import cyder.exceptions.IllegalMethodException;

/**
 * Urls used throughout Cyder.
 */
public class CyderUrls {
    /**
     * Restrict default constructor.
     */
    private CyderUrls() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    // todo after done here, make sure each url only appears here to find duplciate code
    // todo make sure everything is used here
    // todo follow naming procedure of caps and underscores

    public static final String locationUrl = "https://www.google.com/search?q=where+am+i";
    public static final String ispUrl = "https://www.whoismyisp.org/";
    public static final String youtubeWordSearchBase = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";
    public static final String openWeatherBase = "https://api.openweathermap.org/data/2.5/weather?q=";
    public static final String minecraftHamburger = "https://minecraft.net/en-us/?ref=m";
    public static final String minecraftChest = "https://minecraft.net/en-us/store/?ref=m";
    public static final String minecraftRealms = "https://minecraft.net/en-us/realms/?ref=m";
    public static final String minecraftBlock = "https://my.minecraft.net/en-us/store/minecraft/";
    public static final String wikipediaFileSignatures = "https://en.wikipedia.org/wiki/List_of_file_signatures";
    public static final String youtubeDlInstallation = "https://github.com/ytdl-org/youtube-dl#installation";
    public static final String ffmpegInstallation = "https://www.wikihow.com/Install-FFmpeg-on-Windows";
    public static final String environmentVariables = "https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10/";
    public static final String youtubeQueryBase = "https://www.youtube.com/results?search_query=";

    /**
     * The header that all youtube playlists start with.
     */
    public static final String playlistHeader = "https://www.youtube.com/playlist?list=";
    public static final String youtubeApiV3Base = "https://www.googleapis.com/youtube/v3/playlistItems?";

    /**
     * The header for individual youtube videos without their uuid.
     */
    public static final String YOUTUBE_VIDEO_HEADER = "https://www.youtube.com/watch?v=";
}
