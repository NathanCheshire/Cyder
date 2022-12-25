package cyder.handlers.input;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.Handle;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.network.NetworkUtil;
import cyder.strings.CyderStrings;

import java.net.URL;

/**
 * A handler for opening urls.
 */
public class UrlHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private UrlHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The youtube base url for searching for specific words within a youtube url.
     */
    public static final String YOUTUBE_WORD_SEARCH_BASE =
            "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";

    /**
     * A record to link a trigger to a url and the printable version name.
     */
    private record CyderUrl(String trigger, String url, String printable) {}

    /**
     * The list of urls to search trough before attempting to open the raw user input.
     */
    private static final ImmutableList<CyderUrl> urls = ImmutableList.of(
            new CyderUrl("desmos", CyderUrls.DESMOS, "Opening Desmos graphing calculator"),
            new CyderUrl("404", CyderUrls.GOOGLE_404, "Opening a 404 error"),
            new CyderUrl("coffee", CyderUrls.COFFEE_SHOPS, "Finding coffee shops near you"),
            new CyderUrl("quake3", CyderUrls.QUAKE_3,
                    "Opening a video about the Quake 3 fast inverse square root algorithm"),
            new CyderUrl("triangle", CyderUrls.TRIANGLE, "Opening triangle calculator"),
            new CyderUrl("board", CyderUrls.FLY_SQUIRREL_FLY_HTML, "Opening a slingshot game"),
            new CyderUrl("arduino", CyderUrls.ARDUINO, "Raspberry pis are better"),
            new CyderUrl("raspberrypi", CyderUrls.RASPBERRY_PI, "Arduinos are better"),
            new CyderUrl("vexento", CyderUrls.VEXENTO, "Opening a great artist"),
            new CyderUrl("papersplease", CyderUrls.PAPERS_PLEASE, "Opening a great game"),
            new CyderUrl("donut", CyderUrls.DUNKIN_DONUTS, "Dunkin' Hoes; the world runs on it"),
            new CyderUrl("bai", CyderUrls.BAI, "The best drink"),
            new CyderUrl("occamsrazor", CyderUrls.OCCAM_RAZOR, "Opening Occam's razor"),
            new CyderUrl("rickandmorty", CyderUrls.PICKLE_RICK,
                    "Turned myself into a pickle morty! Boom! Big reveal; I'm a pickle!"),
            new CyderUrl("about:blank", "about:blank", "Opening about:blank")
    );

    @Handle
    public static boolean handle() {
        boolean ret = true;

        for (CyderUrl url : urls) {
            if (getInputHandler().commandIs(url.trigger())) {
                getInputHandler().println(url.printable());
                NetworkUtil.openUrl(url.url());
                return true;
            }
        }

        if (getInputHandler().commandIs("YoutubeWordSearch")) {
            youtubeWordSearch();
        } else {
            String possibleUrl = getInputHandler().commandAndArgsToString();
            if (urlValid(possibleUrl)) {
                NetworkUtil.openUrl(possibleUrl);
            } else {
                ret = false;
            }
        }

        return ret;
    }

    /**
     * Performs the youtube word search routine on the user-entered input.
     */
    private static void youtubeWordSearch() {
        if (getInputHandler().checkArgsLength(1)) {
            String input = getInputHandler().getArg(0);
            String browse = YOUTUBE_WORD_SEARCH_BASE
                    .replace("REPLACE", input).replace(CyderRegexPatterns.whiteSpaceRegex, "+");
            NetworkUtil.openUrl(browse);
        } else {
            getInputHandler().println("YoutubeWordSearch usage: YoutubeWordSearch WORD_TO_FIND");
        }
    }

    /**
     * Returns whether the provided url is valid.
     *
     * @param url the url to validate
     * @return whether the provided url is valid
     */
    private static boolean urlValid(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        try {
            new URL(url).openConnection();
            return true;
        } catch (Exception ignored) {}

        return false;
    }
}
