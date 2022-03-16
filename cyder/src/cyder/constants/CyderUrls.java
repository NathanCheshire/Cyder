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
    public static final String wikipediaSummaryBase = "https://en.wikipedia.org/w/api.php?format=json&action=query";
    public static final String dictionaryBase = "https://www.dictionary.com/browse/";
    public static final String microsoft = "https://www.microsoft.com//en-us//";
    public static final String apple = "https://www.apple.com";
    public static final String youtube = "https://www.youtube.com";
    public static final String google = "https://www.google.com";
    public static final String ipdataBase = "https://api.ipdata.co/?api-key=";
    public static final String gitDownload = "https://git-scm.com/downloads";
    public static final String cyderIssues = "https://api.github.com/repos/nathancheshire/cyder/issues";
    public static final String youtubeApiV3Search = "https://www.googleapis.com/youtube/v3/search";
    public static final String simpleDatePatternGuide = "https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html";
    public static final String openWeatherSignUp = "https://home.openweathermap.org/users/sign_up";
    public static final String ipdataSignUp = "https://ipdata.co/";
    public static final String youtubeApi3SignUp = "https://developers.google.com/youtube/v3/getting-started";
    public static final String youtubeWatchBase = "https://www.youtube.com/watch?v=";
    public static final String thumbnailBaseURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";
    public static final String desmos = "https://www.desmos.com/calculator";
    public static final String google404 = "http://google.com/=";
    public static final String coffeeShops = "https://www.google.com/search?q=coffe+shops+near+me";
    public static final String quake3 = "https://www.youtube.com/watch?v=p8u_k2LIZyo&ab_channel=Nemean";
    public static final String triangle = "https://www.triangle-calculator.com/";
    public static final String flySquirelFly = "http://gameninja.com//games//fly-squirrel-fly.html";
    public static final String arduino = "https://www.arduino.cc/";
    public static final String raspberryPi = "https://www.raspberrypi.org/";
    public static final String vexento = "https://www.youtube.com/user/Vexento/videos";
    public static final String papersPlease = "http://papersplea.se/";
    public static final String dunkinDonuts = "https://www.dunkindonuts.com/en/food-drinks/donuts/donuts";
    public static final String bai = "http://www.drinkbai.com";
    public static final String occamRazor = "http://en.wikipedia.org/wiki/Occam%27s_razor";
    public static final String pickleRick = "https://www.youtube.com/watch?v=s_1lP4CBKOg";
    public static final String cyderSource = "https://github.com/nathancheshire/cyder";
    public static final String whereAmI = "https://www.google.com/search?q=where+am+i";
    public static final String pastebinRawBase = "https://pastebin.com/raw/";
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
    public static final String youtubeApiV3PlaylistItems = "https://www.googleapis.com/youtube/v3/playlistItems?";


    /**
     * The header for individual youtube videos without their uuid.
     */
    public static final String YOUTUBE_VIDEO_HEADER = "https://www.youtube.com/watch?v=";
}
