package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.utilities.NetworkUtil;
import cyder.utilities.OSUtil;

import java.net.URL;

/**
 * A handler for opening urls.
 */
public class UrlHandler extends InputHandlerBase {
    /**
     * Suppress default constructor.
     */
    private UrlHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle("")
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("YoutubeWordSearch")) {
            if (getInputHandler().checkArgsLength(1)) {
                String input = getInputHandler().getArg(0);
                String browse = CyderUrls.YOUTUBE_WORD_SEARCH_BASE
                        .replace("REPLACE", input).replace(" ", "+");
                NetworkUtil.openUrl(browse);
            } else {
                getInputHandler().println("YoutubeWordSearch usage: YoutubeWordSearch WORD_TO_FIND");
            }
        } else if (getInputHandler().commandIs("echo")
                || getInputHandler().commandIs("print")
                || getInputHandler().commandIs("println")) {
            getInputHandler().println(getInputHandler().argsToString());
        } else if (getInputHandler().commandIs("cmd")) {
            OSUtil.openShell();
        } else if (getInputHandler().commandIs("desmos")) {
            NetworkUtil.openUrl(CyderUrls.DESMOS);
        } else if (getInputHandler().commandIs("404")) {
            NetworkUtil.openUrl(CyderUrls.GOOGLE_404);
        } else if (getInputHandler().commandIs("coffee")) {
            NetworkUtil.openUrl(CyderUrls.COFFEE_SHOPS);
        } else if (getInputHandler().inputWithoutSpacesIs("quake3")) {
            NetworkUtil.openUrl(CyderUrls.QUAKE_3);
        } else if (getInputHandler().commandIs("triangle")) {
            NetworkUtil.openUrl(CyderUrls.TRIANGLE);
        } else if (getInputHandler().commandIs("board")) {
            NetworkUtil.openUrl(CyderUrls.FLY_SQUIRREL_FLY_HTML);
        } else if (getInputHandler().commandIs("arduino")) {
            NetworkUtil.openUrl(CyderUrls.ARDUINO);
        } else if (getInputHandler().inputWithoutSpacesIs("rasberrypi")) {
            NetworkUtil.openUrl(CyderUrls.RASPBERRY_PI);
        } else if (getInputHandler().commandIs("vexento")) {
            NetworkUtil.openUrl(CyderUrls.VEXENTO);
        } else if (getInputHandler().inputWithoutSpacesIs("papersplease")) {
            NetworkUtil.openUrl(CyderUrls.PAPERS_PLEASE);
        } else if (getInputHandler().commandIs("donut")) {
            NetworkUtil.openUrl(CyderUrls.DUNKIN_DONUTS);
        } else if (getInputHandler().commandIs("bai")) {
            NetworkUtil.openUrl(CyderUrls.BAI);
        } else if (getInputHandler().inputWithoutSpacesIs("occamrazor")) {
            NetworkUtil.openUrl(CyderUrls.OCCAM_RAZOR);
        } else if (getInputHandler().inputWithoutSpacesIs("rickandmorty")) {
            getInputHandler().println("Turned myself into a pickle morty! Boom! Big reveal; I'm a pickle!");
            NetworkUtil.openUrl(CyderUrls.PICKLE_RICK);
        } else if (getInputHandler().commandIs("about:blank")) {
            NetworkUtil.openUrl("about:blank");
        } else {
            try {
                URL url = new URL(getInputHandler().commandAndArgsToString());
                url.openConnection();
                NetworkUtil.openUrl(getInputHandler().commandAndArgsToString());
            } catch (Exception ignored) {
                ret = false;
            }
        }

        return ret;
    }
}
