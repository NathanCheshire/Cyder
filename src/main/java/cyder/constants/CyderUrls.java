package main.java.cyder.constants;

import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.strings.CyderStrings;

/**
 * Urls used throughout Cyder.
 */
public final class CyderUrls {
    /**
     * Suppress default constructor.
     */
    private CyderUrls() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The open weather url to get weather data from.
     */
    public static final String OPEN_WEATHER_BASE = "https://api.openweathermap.org/data/2.5/weather?q=";

    /**
     * The wikipedia query base.
     */
    public static final String WIKIPEDIA_SUMMARY_BASE = "https://en.wikipedia.org/w/api.php?format=json&action=query";

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
     * The search base for youtube api v3.
     */
    public static final String YOUTUBE_API_V3_SEARCH = "https://www.googleapis.com/youtube/v3/search";

    /**
     * The link to learn about simple date patterns in Java.
     */
    public static final String SIMPLE_DATE_PATTERN_GUIDE =
            "https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html";

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
     * The url of the default background to give to newly created users provided a
     * connection is available.
     */
    public static final String DEFAULT_BACKGROUND_URL = "https://i.imgur.com/kniH8y9.png";

    /**
     * The header for individual youtube videos without their uuid.
     */
    public static final String YOUTUBE_VIDEO_HEADER = "https://www.youtube.com/watch?v=";
}
