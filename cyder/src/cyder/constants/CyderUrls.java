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
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The url used for web scraping where a user is.
     */
    public static final String LOCATION_URL = "https://www.google.com/search?q=where+am+i";

    /**
     * The url used for web scraping who a user's ISP is.
     */
    public static final String ISP_URL = "https://www.whoismyisp.org/";

    /**
     * The youtube base url for searching for specific words within a youtube url.
     */
    public static final String YOUTUBE_WORD_SEARCH_BASE = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";

    /**
     * The open weather url to get weather data from.
     */
    public static final String OPEN_WEATHER_BASE = "https://api.openweathermap.org/data/2.5/weather?q=";

    /**
     * The minecraft.net link that redirects to the hamburger icon's result.
     */
    public static final String MINECRAFT_HAMBURGER = "https://minecraft.net/en-us/?ref=m";

    /**
     * The minecraft.net link that redirects to the store icon's result.
     */
    public static final String MINECRAFT_CHEST = "https://minecraft.net/en-us/store/?ref=m";

    /**
     * The minecraft.net link that redirects to the realm icon's result.
     */
    public static final String MINECRAFT_REALMS = "https://minecraft.net/en-us/realms/?ref=m";

    /**
     * The minecraft.net link that redirects to the block icon's result.
     */
    public static final String MINECRAFT_BLOCK = "https://my.minecraft.net/en-us/store/minecraft/";

    /**
     * A link for common file signatures.
     */
    public static final String WIKIPEDIA_FILE_SIGNATURES = "https://en.wikipedia.org/wiki/List_of_file_signatures";

    /**
     * A link for how to install youtube-dl.
     */
    public static final String YOUTUBE_DL_INSTALLATION = "https://github.com/ytdl-org/youtube-dl#installation";

    /**
     * A link for how to install ffmpeg.
     */
    public static final String FFMPEG_INSTALLATION = "https://www.wikihow.com/Install-FFmpeg-on-Windows";

    /**
     * A link to set environment variables for Windows.
     */
    public static final String environmentVariables = "https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10/";

    /**
     * The youtube query base url.
     */
    public static final String YOUTUBE_QUERY_BASE = "https://www.youtube.com/results?search_query=";

    /**
     * The wikipedia query base.
     */
    public static final String WIKIPEDIA_SUMMARY_BASE = "https://en.wikipedia.org/w/api.php?format=json&action=query";

    /**
     * The dictionary base url.
     */
    public static final String DICTIONARY_BASE = "https://www.dictionary.com/browse/";

    /**
     * Microsoft.com
     */
    public static final String MICROSOFT = "https://www.microsoft.com//en-us//";

    /**
     * Apple.com
     */
    public static final String APPLE = "https://www.apple.com";

    /**
     * YouTube.com
     */
    public static final String YOUTUBE = "https://www.youtube.com";

    /**
     * Google.com
     */
    public static final String GOOGLE = "https://www.google.com";

    /**
     * The ip data base url.
     */
    public static final String IPDATA_BASE = "https://api.ipdata.co/?api-key=";

    /**
     * The link to download git from.
     */
    public static final String GIT_DOWNLOAD = "https://git-scm.com/downloads";

    /**
     * The link for the github api to return a json of currently open issues for Cyder.
     */
    public static final String CYDER_ISSUES = "https://api.github.com/repos/nathancheshire/cyder/issues";

    /**
     * The search base for youtube api v3.
     */
    public static final String YOUTUBE_API_V3_SEARCH = "https://www.googleapis.com/youtube/v3/search";

    /**
     * The link to learn about simple date patterns in Java.
     */
    public static final String SIMPLE_DATE_PATTERN_GUIDE = "https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html";

    /**
     * The default thumbnail base url for youtube videos.
     */
    public static final String THUMBNAIL_BASE_URL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";

    /**
     * A link for Desmos graphing calculator.
     */
    public static final String DESMOS = "https://www.desmos.com/calculator";

    /**
     * A link for Google's 404.
     */
    public static final String GOOGLE_404 = "http://google.com/=";

    /**
     * A link for finding coffee shops near a person.
     */

    public static final String COFFEE_SHOPS = "https://www.google.com/search?q=coffe+shops+near+me";
    /**
     * A link to the Quake 3 fast inverse sqrt algorithm.
     */
    public static final String QUAKE_3 = "https://www.youtube.com/watch?v=p8u_k2LIZyo&ab_channel=Nemean";

    /**
     * A link to a triangle calculator.
     */
    public static final String TRIANGLE = "https://www.triangle-calculator.com/";

    /**
     * A link to GameNinja's Fly Squirrel Fly.
     */
    public static final String FLY_SQUIRREL_FLY_HTML = "http://gameninja.com//games//fly-squirrel-fly.html";

    /**
     * A link to Arduino.
     */
    public static final String ARDUINO = "https://www.arduino.cc/";

    /**
     * A link to Raspberry Pis.
     */
    public static final String RASPBERRY_PI = "https://www.raspberrypi.org/";

    /**
     * A link to Vexento.
     */
    public static final String VEXENTO = "https://www.youtube.com/user/Vexento/videos";

    /**
     * A link to papers please.
     */
    public static final String PAPERS_PLEASE = "http://papersplea.se/";

    /**
     * A link to Dunkin' Donuts.
     */
    public static final String DUNKIN_DONUTS = "https://www.dunkindonuts.com/en/food-drinks/donuts/donuts";

    /**
     * A link to bai.
     */
    public static final String BAI = "http://www.drinkbai.com";

    /**
     * A wikipedia link for Occam's razor.
     */
    public static final String OCCAM_RAZOR = "http://en.wikipedia.org/wiki/Occam%27s_razor";

    /**
     * A pickle rick youtube clip.
     */
    public static final String PICKLE_RICK = "https://www.youtube.com/watch?v=s_1lP4CBKOg";

    /**
     * The source link for this project.
     */
    public static final String CYDER_SOURCE = "https://github.com/nathancheshire/cyder";

    /**
     * The pastebin link for raw text
     */
    public static final String PASTEBIN_RAW_BASE = "https://pastebin.com/raw/";

    /**
     * The default github url.
     */
    public static final String GITHUB_BASE = "www.github.com";

    /**
     * The backend path.
     */
    public static final String CYDER_BACKEND_URL = "http://127.0.0.1:8000";

    /**
     * The url of the default background to give to newly created users provided a
     * connection is available.
     */
    public static final String DEFAULT_BACKGROUND_URL = "https://i.imgur.com/kniH8y9.png";

    /**
     * The header that all youtube playlists start with.
     */
    public static final String YOUTUBE_PLAYLIST_HEADER = "https://www.youtube.com/playlist?list=";

    /**
     * The google youtube api v3 for getting a playlist's items
     */
    public static final String YOUTUBE_API_V3_PLAYLIST_ITEMS = "https://www.googleapis.com/youtube/v3/playlistItems?";

    /**
     * The header for individual youtube videos without their uuid.
     */
    public static final String YOUTUBE_VIDEO_HEADER = "https://www.youtube.com/watch?v=";

    /**
     * The header used for obtaining a youtube video's highest resolution thumbnail.
     */
    public static final String YOUTUBE_THUMBNAIL_BASE = "https://img.youtube.com/vi/";

    /**
     * The base for youtube api v3 search queries.
     */
    public static final String YOUTUBE_API_V3_SEARCH_BASE = "https://www.googleapis.com/youtube/v3/search?part=snippet";

    /*
    Binary and resource Url links.
     */

    /**
     * The resource link to download the ffmpeg binary.
     */
    public static final String DOWNLOAD_RESOURCE_FFMPEG
            = "https://github.com/NathanCheshire/Cyder/raw/main/resources/ffmpeg.zip";

    /**
     * The resource link to download the ffplay binary.
     */
    public static final String DOWNLOAD_RESOURCE_FFPLAY
            = "https://github.com/NathanCheshire/Cyder/raw/main/resources/ffplay.zip";

    /**
     * The resource link to download the ffprobe binary.
     */
    public static final String DOWNLOAD_RESOURCE_FFPROBE
            = "https://github.com/NathanCheshire/Cyder/raw/main/resources/ffprobe.zip";

    /**
     * The resource link to download the youtube-dl binary.
     */
    public static final String DOWNLOAD_RESOURCE_YOUTUBE_DL
            = "https://github.com/NathanCheshire/Cyder/raw/main/resources/youtube-dl.zip";
}
