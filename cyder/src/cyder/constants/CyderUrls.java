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

    // todo follow naming procedure of caps and underscores

    /**
     * The url used for web scraping where a user is.
     */
    public static final String locationUrl = "https://www.google.com/search?q=where+am+i";

    /**
     * The url used for web scraping who a user's ISP is.
     */
    public static final String ispUrl = "https://www.whoismyisp.org/";

    /**
     * The youtube base url for searching for specific words within a youtube url.
     */
    public static final String youtubeWordSearchBase = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";

    /**
     * The open weather url to get weather data from.
     */
    public static final String openWeatherBase = "https://api.openweathermap.org/data/2.5/weather?q=";

    /**
     * The mincraft.net link that redirects to the hamburger icon's result.
     */
    public static final String minecraftHamburger = "https://minecraft.net/en-us/?ref=m";

    /**
     * The mincraft.net link that redirects to the store icon's result.
     */
    public static final String minecraftChest = "https://minecraft.net/en-us/store/?ref=m";

    /**
     * The mincraft.net link that redirects to the realm icon's result.
     */
    public static final String minecraftRealms = "https://minecraft.net/en-us/realms/?ref=m";

    /**
     * The mincraft.net link that redirects to the block icon's result.
     */
    public static final String minecraftBlock = "https://my.minecraft.net/en-us/store/minecraft/";

    /**
     * A link for common file signatures.
     */
    public static final String wikipediaFileSignatures = "https://en.wikipedia.org/wiki/List_of_file_signatures";

    /**
     * A link to install youtube-dl
     */
    public static final String youtubeDlInstallation = "https://github.com/ytdl-org/youtube-dl#installation";

    /**
     * A link to install ffmpeg.
     */
    public static final String ffmpegInstallation = "https://www.wikihow.com/Install-FFmpeg-on-Windows";

    /**
     * A link to set environment variables for Windows.
     */
    public static final String environmentVariables = "https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10/";

    /**
     * The youtube query base url.
     */
    public static final String youtubeQueryBase = "https://www.youtube.com/results?search_query=";

    /**
     * The wikipedia query base.
     */
    public static final String wikipediaSummaryBase = "https://en.wikipedia.org/w/api.php?format=json&action=query";

    /**
     * The dictionary base url.
     */
    public static final String dictionaryBase = "https://www.dictionary.com/browse/";

    /**
     * Microsoft.com
     */
    public static final String microsoft = "https://www.microsoft.com//en-us//";

    /**
     * Apple.com
     */
    public static final String apple = "https://www.apple.com";

    /**
     * YouTube.com
     */
    public static final String youtube = "https://www.youtube.com";

    /**
     * Google.com
     */
    public static final String google = "https://www.google.com";

    /**
     * The ip data base url.
     */
    public static final String ipdataBase = "https://api.ipdata.co/?api-key=";

    /**
     * The link to download git from.
     */
    public static final String gitDownload = "https://git-scm.com/downloads";

    /**
     * The link for the github api to return a json of currently open issues for Cyder.
     */
    public static final String cyderIssues = "https://api.github.com/repos/nathancheshire/cyder/issues";

    /**
     * The search base for youtube api v3.
     */
    public static final String youtubeApiV3Search = "https://www.googleapis.com/youtube/v3/search";

    /**
     * The link to learn about simple date patterns in Java.
     */
    public static final String simpleDatePatternGuide = "https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html";

    /**
     * The link to sign up for Open Weather.
     */
    public static final String openWeatherSignUp = "https://home.openweathermap.org/users/sign_up";

    /**
     * The link to sign up for IpData.
     */
    public static final String ipdataSignUp = "https://ipdata.co/";

    /**
     * The link to sign up for youtube api v3.
     */
    public static final String youtubeApi3SignUp = "https://developers.google.com/youtube/v3/getting-started";

    /**
     * The default thumbnail base url for youtube videos.
     */
    public static final String thumbnailBaseURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";

    /**
     * A link for Desmos' graphing calculator.
     */
    public static final String desmos = "https://www.desmos.com/calculator";

    /**
     * A link for Google's 404.
     */
    public static final String google404 = "http://google.com/=";

    /**
     * A link for finding coffee shops near a person.
     */

    public static final String coffeeShops = "https://www.google.com/search?q=coffe+shops+near+me";
    /**
     * A link to the Quake 3 fast inverse sqrt algorithm.
     */
    public static final String quake3 = "https://www.youtube.com/watch?v=p8u_k2LIZyo&ab_channel=Nemean";

    /**
     * A link to a triangle calculator.
     */
    public static final String triangle = "https://www.triangle-calculator.com/";

    /**
     * A link to GameNinja's Fly Squirel Fly.
     */
    public static final String flySquirelFly = "http://gameninja.com//games//fly-squirrel-fly.html";

    /**
     * A link to Arduino.
     */
    public static final String arduino = "https://www.arduino.cc/";

    /**
     * A link to Raspberry Pis.
     */
    public static final String raspberryPi = "https://www.raspberrypi.org/";

    /**
     * A link to Vexento.
     */
    public static final String vexento = "https://www.youtube.com/user/Vexento/videos";

    /**
     * A link to papers please.
     */
    public static final String papersPlease = "http://papersplea.se/";

    /**
     * A link to Dunkin' Donuts.
     */
    public static final String dunkinDonuts = "https://www.dunkindonuts.com/en/food-drinks/donuts/donuts";

    /**
     * A link to bai.
     */
    public static final String bai = "http://www.drinkbai.com";

    /**
     * A wikipedia link for Occam's razor.
     */
    public static final String occamRazor = "http://en.wikipedia.org/wiki/Occam%27s_razor";

    /**
     * A pickle rick youtube clip.
     */
    public static final String pickleRick = "https://www.youtube.com/watch?v=s_1lP4CBKOg";

    /**
     * The source link for this project.
     */
    public static final String cyderSource = "https://github.com/nathancheshire/cyder";

    /**
     * The pastebin link for raw text
     */
    public static final String pastebinRawBase = "https://pastebin.com/raw/";

    /**
     * The default github url.
     */
    public static final String githubBase = "www.github.com";

    /**
     * The backend path.
     */
    public static final String BACKEND = "http://127.0.0.1:8000";

    /**
     * The url of the default background to give to newly created users provided a
     * connection is available.
     */
    public static final String DEFAULT_BACKGROUND_URL = "https://i.imgur.com/kniH8y9.png";

    /**
     * The header that all youtube playlists start with.
     */
    public static final String playlistHeader = "https://www.youtube.com/playlist?list=";

    /**
     * The google youtube api v3 for getting a playlist's items
     */
    public static final String youtubeApiV3PlaylistItems = "https://www.googleapis.com/youtube/v3/playlistItems?";

    /**
     * The header for individual youtube videos without their uuid.
     */
    public static final String YOUTUBE_VIDEO_HEADER = "https://www.youtube.com/watch?v=";
}
