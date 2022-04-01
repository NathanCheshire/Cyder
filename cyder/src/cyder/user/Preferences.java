package cyder.user;

import cyder.constants.CyderStrings;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderScrollList;
import cyder.user.objects.Preference;
import cyder.utilities.ColorUtil;
import cyder.utilities.FrameUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.util.ArrayList;

/**
 * Preferences class to hold and allow access to the default CyderUser preferences.
 */
public class Preferences {
    /**
     * The maximum allowable size for the input field and output area font.
     */
    public static final int FONT_MAX_SIZE = 50;

    /**
     * The minimum allowable size for the input field and output area font.
     */
    public static final int FONT_MIN_SIZE = 25;

    /**
     * Instantiation of Preferences not allowed.
     */
    private Preferences() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The immutable collection of preference objects.
     */
    private static final ArrayList<Preference> prefs = initialize();

    /**
     * Returns the preferences collection.
     *
     * @return the preferences collection
     */
    public static ArrayList<Preference> getPreferences() {
        return prefs;
    }

    // todo these should be Runnables and not functions :/

    /**
     * Initializes the preferences collection.
     *
     * @return the immutable collection
     */
    private static ArrayList<Preference> initialize() {
        ArrayList<Preference> ret = new ArrayList<>();

        ret.add(new Preference("name","IGNORE",
                "IGNORE", "IGNORE", (optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = name");
            //no action required
            return null;
        }));
        ret.add(new Preference("pass","IGNORE",
                "IGNORE", "IGNORE",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = pass");
            //no action required
            return null;
        }));
        ret.add(new Preference("font","IGNORE",
                "","Agency FB",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = font");
            //no action required, updates are done via font chooser field, font metric field, and scroll wheel
            // for the respective font params
            return null;
        }));
        ret.add(new Preference("foreground","IGNORE",
                "","f0f0f0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = foreground");

            ConsoleFrame.INSTANCE.getInputField()
                    .setForeground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getForeground()));

            return null;
        }));
        ret.add(new Preference("background","IGNORE",
                "","101010",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = background");
            //no action needed
            return null;
        }));
        ret.add(new Preference("intromusic", "Intro Music",
                "Play intro music on start","0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = intromusic");
            //no action required
            return null;
        }));
        ret.add(new Preference("debugwindows", "Debug Windows",
                "Show debug menus on startup","0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = debugwindows");
            //no action required
            return null;
        }));
        ret.add(new Preference("randombackground", "Random Background",
                "Choose a random background on startup","0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = randombackground");
            //no action required
            return null;
        }));
        ret.add(new Preference("outputborder", "Output Border",
                "Draw a border around the output area","0", (optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = outputborder");

            if (UserUtil.getCyderUser().getOutputborder().equals("0")) {
                ConsoleFrame.INSTANCE.getOutputScroll().setBorder(BorderFactory.createEmptyBorder());
            } else {
                ConsoleFrame.INSTANCE.getOutputScroll().setBorder(new LineBorder(ColorUtil.hexToRgb(
                        UserUtil.getCyderUser().getBackground()), 3, true));
            }

            return null;
        }));
        ret.add(new Preference("inputborder", "Input Border",
                "Draw a border around the input area","0", (optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = inputborder");

            if (UserUtil.getCyderUser().getInputborder().equals("0")) {
                ConsoleFrame.INSTANCE.getInputField().setBorder(null);
            } else {
                ConsoleFrame.INSTANCE.getInputField().setBorder(
                        new LineBorder(ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()),
                                3, true));
            }

            return null;
        }));
        ret.add(new Preference("hourlychimes", "Hourly Chimes",
                "Chime every hour","1",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = hourlychimes");
            //no action required, this is checked once an hour
            return null;
        }));
        ret.add(new Preference("silenceerrors", "Silence Errors",
                "Don't open errors externally","1", (optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = silenceerrors");
            //no action required
            return null;
        }));
        ret.add(new Preference("fullscreen", "Fullscreen",
                "Fullscreen Cyder (this will also cover the Windows taskbar)","0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = fullscreen");

            ConsoleFrame.INSTANCE.setFullscreen(UserUtil.getCyderUser().getFullscreen().equals("1"));

            return null;
        }));
        ret.add(new Preference("outputfill", "Output Fill",
                "Fill the output area with the color specified in the \"Fonts & Colors\" panel",
                "0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = outputfill");

            if (UserUtil.getCyderUser().getOutputfill().equals("0")) {
                ConsoleFrame.INSTANCE.getOutputArea().setBackground(null);
                ConsoleFrame.INSTANCE.getOutputArea().setOpaque(false);
            } else {
                ConsoleFrame.INSTANCE.getOutputArea().setOpaque(true);
                ConsoleFrame.INSTANCE.getOutputArea().setBackground(
                        ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()));
                ConsoleFrame.INSTANCE.getOutputArea().repaint();
                ConsoleFrame.INSTANCE.getOutputArea().revalidate();
            }

            return null;
        }));
        ret.add(new Preference("inputfill", "Input Fill",
                "Fill the input area with the color specified in the \"Fonts & Colors\" panel",
                "0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = inputfill");

            if (UserUtil.getCyderUser().getInputfill().equals("0")) {
                ConsoleFrame.INSTANCE.getInputField().setBackground(null);
                ConsoleFrame.INSTANCE.getInputField().setOpaque(false);
            } else {
                ConsoleFrame.INSTANCE.getInputField().setOpaque(true);
                ConsoleFrame.INSTANCE.getInputField().setBackground(
                        ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()));
                ConsoleFrame.INSTANCE.getInputField().repaint();
                ConsoleFrame.INSTANCE.getInputField().revalidate();
            }

            return null;
        }));
        ret.add(new Preference("clockonconsole", "Clock On Console",
                "Show a clock at the top of the console","1",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = clockonconsole");
            ConsoleFrame.INSTANCE.refreshClockText();
            return null;
        }));
        ret.add(new Preference("showseconds", "Show Seconds",
                "Show seconds on the console clock if enabled","1",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = showseconds");
            ConsoleFrame.INSTANCE.refreshClockText();
            return null;
        }));
        ret.add(new Preference("filterchat", "Filter Chat",
                "Filter foul language","1",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = filterchat");
            //no action required
            return null;
        }));
        ret.add(new Preference("laststart","IGNORE",
                "", String.valueOf(System.currentTimeMillis()),(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = laststart");
            //no action required, this is set once on Cyder start
            return null;
        }));
        ret.add(new Preference("minimizeonclose","Minimize On Close",
                "Minimize the application instead of exiting whenever a close action is requested",
                "0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = minimizeonclose");
            //no action required, frames check this before animating
            return null;
        }));
        ret.add(new Preference("typinganimation","Typing Animation",
                "Typing animation on console for non-vital outputs", "1",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = typinganimation");
            //no action required, typing thread will figure it out
            return null;
        }));
        ret.add(new Preference("typingsound","Typing Animation Sound",
                "Typing animation sound effect to play if typing animation is enabled",
                "1",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = typingsound");
            //no action required, typing thread will figure it out
            return null;
        }));
        ret.add(new Preference("showbusyicon", "Show Cyder Busy Icon",
                "Show when Cyder is busy by changing the tray icon","0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = showbusyicon");
            //no action required, busy thread will figure it out
            return null;
        }));
        ret.add(new Preference("ffmpegpath","IGNORE",
                "","",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = ffmpegpath");
            //no update required
            return null;
        }));
        ret.add(new Preference("youtubedlpath","IGNORE",
                "","",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = youtubedlpath");
            //no update required
            return null;
        }));
        ret.add(new Preference("roundedwindows","Rounded Windows",
                "Make certain windows rounded","0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = roundedwindows");
            FrameUtil.repaintCyderFrames();
            return null;
        }));
        ret.add(new Preference("windowcolor","IGNORE",
                "","1A2033",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = windowcolor");

            FrameUtil.repaintCyderFrames();
            ConsoleFrame.INSTANCE.revalidateMenuBackgrounds();
            // todo revalidate menus on frames

            return null;
        }));
        ret.add(new Preference("consoleclockformat","IGNORE",
                "","EEEEEEEEE h:mmaa",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key =  consoleclockformat");
            ConsoleFrame.INSTANCE.refreshClockText();
            return null;
        }));
        ret.add(new Preference("youtubeuuid","IGNORE",
                "","aaaaaaaaaaa",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = youtubeuuid");
            //no action required
            return null;
        }));
        ret.add(new Preference("ipkey","IGNORE",
                "","",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = ipkey");
            //no action required
            return null;
        }));
        ret.add(new Preference("weatherkey","IGNORE",
                "","",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = weatherkey");
            //no action required
            return null;
        }));
        ret.add(new Preference("youtubeapi3key","IGNORE",
                "","",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = youtubeapi3key");
            //no update required
            return null;
        }));
        ret.add(new Preference("capsmode","Capital Letters Mode",
                "Capitalize all console output","0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = capsmode");
            //no action required
            return null;
        }));
        ret.add(new Preference("loggedin","IGNORE",
                "","0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = loggedin");
            //no action required
            return null;
        }));
        ret.add(new Preference("audiolength","Show Audio Total Length",
                "For the audio player, show the total audio time instead of the time remaining",
                "1",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = audiolength");
            //no action required
            return null;
        }));
        ret.add(new Preference("persistentnotifications","Persistent Notifications",
                "Notifications stay on screen until manually dismissed","0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = persistentnotifications");
            //no action required
            return null;
        }));
        ret.add(new Preference("minimizeanimation","Minimize Animation",
                "Animate the window away for minimizations","1",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = minimizeanimation");
            //no action required
            return null;
        }));
        ret.add(new Preference("closeanimation","Close Animation",
                "Animate the window away for close requests","1",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = closeanimation");
            //no action required
            return null;
        }));
        ret.add(new Preference("compacttextmode", "Compact Text",
                "Compact the text/components in supported text panes","0",(optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = compacttextmode");

            ConsoleFrame.INSTANCE.revalidateMenu();
            CyderScrollList.refreshAllLists();

            return null;
        }));
        ret.add(new Preference("fontmetric","IGNORE", "",
                "1", (optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = fontmetric");

            ConsoleFrame.INSTANCE.getInputField().setFont(ConsoleFrame.INSTANCE.generateUserFont());
            ConsoleFrame.INSTANCE.getOutputArea().setFont(ConsoleFrame.INSTANCE.generateUserFont());

            return null;
        }));
        ret.add(new Preference("fontsize","IGNORE", "",
                "30", (optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = fontsize");

            ConsoleFrame.INSTANCE.getInputField().setFont(ConsoleFrame.INSTANCE.generateUserFont());
            ConsoleFrame.INSTANCE.getOutputArea().setFont(ConsoleFrame.INSTANCE.generateUserFont());

            return null;
        }));
        ret.add(new Preference("wrapterminal","Wrap Terminal",
                "Wrap the native shell by passing unrecognized commands to it and allowing it to process them",
                "0", (optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = wrapterminal");

            //no action required

            return null;
        }));
        ret.add(new Preference("darkmode", "Dark Mode",
                "Activate a pleasant dark mode for Cyder",
                "0", (optionalParam) -> {
            Logger.log(LoggerTag.PREFERENCE_REFRESH, "key = darkmode");

            // no action required

            return null;
        }));

        // IGNORE for display name means ignore for UserEditor checkboxes,
        // IGNORE for tooltip means don't write when creating user since it was already set
        // such as the case for name and password

        // Adding future prefs:
        // you'll need to add the preference here and also the data in user.java
        // since gson parses the userdata file into a user object.

        // EVERYTHING that is in userdata must be in this list

        // Some rare cases might require deeper manipulation such as the case for executables
        // where we don't add it here but add it for the user object and for user creation
        // this also requires more handling where we want to use the non-string data

        return ret;
    }

    /**
     * Invokes the onChangeFunction() of the preference with the provided ID, if found.
     *
     * @param preferenceID the onChangeFunction() of the preference with the provided ID
     */
    public static void invokeRefresh(String preferenceID) {
        for (Preference pref : prefs) {
            if (pref.getID().equalsIgnoreCase(preferenceID)) {
                pref.getOnChangeFunction().apply(null);
            }
        }
    }
}
