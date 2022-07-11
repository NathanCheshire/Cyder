package cyder.user;

import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderScrollList;
import cyder.utils.ColorUtil;
import cyder.utils.FrameUtil;

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
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
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

    /**
     * Initializes the preferences collection.
     *
     * @return the immutable collection
     */
    private static ArrayList<Preference> initialize() {
        ArrayList<Preference> ret = new ArrayList<>();

        ret.add(new Preference("name", "IGNORE",
                "IGNORE", "IGNORE", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = name");
            //no action required
        }));
        ret.add(new Preference("pass", "IGNORE",
                "IGNORE", "IGNORE", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = pass");
            //no action required
        }));
        ret.add(new Preference("font", "IGNORE",
                "", "Agency FB", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = font");
            //no action required, updates are done via font chooser field, font metric field, and scroll wheel
            // for the respective font params
        }));
        ret.add(new Preference("foreground", "IGNORE",
                "", "f0f0f0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = foreground");

            Console.INSTANCE.getInputField()
                    .setForeground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()));
        }));
        ret.add(new Preference("background", "IGNORE",
                "", "101010", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = background");
            //no action needed
        }));
        ret.add(new Preference("intromusic", "Intro Music",
                "Play intro music on start", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = intromusic");
            //no action required
        }));
        ret.add(new Preference("debugwindows", "Debug Windows",
                "Show debug menus on startup", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = debugwindows");
            //no action required
        }));
        ret.add(new Preference("randombackground", "Random Background",
                "Choose a random background on startup", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = randombackground");
            //no action required
        }));
        ret.add(new Preference("outputborder", "Output Border",
                "Draw a border around the output area", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = outputborder");

            if (UserUtil.getCyderUser().getOutputborder().equals("0")) {
                Console.INSTANCE.getOutputScroll().setBorder(BorderFactory.createEmptyBorder());
            } else {
                Console.INSTANCE.getOutputScroll().setBorder(new LineBorder(ColorUtil.hexStringToColor(
                        UserUtil.getCyderUser().getBackground()), 3, true));
            }

        }));
        ret.add(new Preference("inputborder", "Input Border",
                "Draw a border around the input area", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = inputborder");

            if (UserUtil.getCyderUser().getInputborder().equals("0")) {
                Console.INSTANCE.getInputField().setBorder(null);
            } else {
                Console.INSTANCE.getInputField().setBorder(
                        new LineBorder(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()),
                                3, true));
            }

        }));
        ret.add(new Preference("hourlychimes", "Hourly Chimes",
                "Chime every hour", "1", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = hourlychimes");
            //no action required, this is checked once an hour
        }));
        ret.add(new Preference("silenceerrors", "Silence Errors",
                "Don't open errors externally", "1", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = silenceerrors");
            //no action required
        }));
        ret.add(new Preference("fullscreen", "Fullscreen",
                "Fullscreen Cyder (this will also cover the Windows taskbar)", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = fullscreen");

            Console.INSTANCE.setFullscreen(UserUtil.getCyderUser().getFullscreen().equals("1"));
        }));
        ret.add(new Preference("outputfill", "Output Fill",
                "Fill the output area with the color specified in the \"Fonts & Colors\" panel",
                "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = outputfill");

            if (UserUtil.getCyderUser().getOutputfill().equals("0")) {
                Console.INSTANCE.getOutputArea().setBackground(null);
                Console.INSTANCE.getOutputArea().setOpaque(false);
            } else {
                Console.INSTANCE.getOutputArea().setOpaque(true);
                Console.INSTANCE.getOutputArea().setBackground(
                        ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()));
                Console.INSTANCE.getOutputArea().repaint();
                Console.INSTANCE.getOutputArea().revalidate();
            }

        }));
        ret.add(new Preference("inputfill", "Input Fill",
                "Fill the input area with the color specified in the \"Fonts & Colors\" panel",
                "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = inputfill");

            if (UserUtil.getCyderUser().getInputfill().equals("0")) {
                Console.INSTANCE.getInputField().setBackground(null);
                Console.INSTANCE.getInputField().setOpaque(false);
            } else {
                Console.INSTANCE.getInputField().setOpaque(true);
                Console.INSTANCE.getInputField().setBackground(
                        ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()));
                Console.INSTANCE.getInputField().repaint();
                Console.INSTANCE.getInputField().revalidate();
            }

        }));
        ret.add(new Preference("clockonconsole", "Clock On Console",
                "Show a clock at the top of the console", "1", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = clockonconsole");
            Console.INSTANCE.refreshClockText();
        }));
        ret.add(new Preference("showseconds", "Show Seconds",
                "Show seconds on the console clock if enabled", "1", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = showseconds");
            Console.INSTANCE.refreshClockText();
        }));
        ret.add(new Preference("filterchat", "Filter Chat",
                "Filter foul language", "1", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = filterchat");
            //no action required
        }));
        ret.add(new Preference("laststart", "IGNORE",
                "", String.valueOf(System.currentTimeMillis()), () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = laststart");
            //no action required, this is set once on Cyder start
        }));
        ret.add(new Preference("minimizeonclose", "Minimize On Close",
                "Minimize the application instead of exiting whenever a close action is requested",
                "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = minimizeonclose");
            //no action required, frames check this before animating
        }));
        ret.add(new Preference("typinganimation", "Typing Animation",
                "Typing animation on console for non-vital outputs", "1", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = typinganimation");
            //no action required, typing thread will figure it out
        }));
        ret.add(new Preference("typingsound", "Typing Animation Sound",
                "Typing animation sound effect to play if typing animation is enabled",
                "1", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = typingsound");
            //no action required, typing thread will figure it out
        }));
        ret.add(new Preference("showbusyicon", "Show Cyder Busy Icon",
                "Show when Cyder is busy by changing the tray icon", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = showbusyicon");
            //no action required, busy thread will figure it out
        }));
        ret.add(new Preference("roundedwindows", "Rounded Windows",
                "Make certain windows rounded", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = roundedwindows");
            FrameUtil.repaintCyderFrames();
        }));
        ret.add(new Preference("windowcolor", "IGNORE",
                "", "1A2033", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = windowcolor");

            FrameUtil.repaintCyderFrames();
            Console.INSTANCE.revalidateMenuBackgrounds();
        }));
        ret.add(new Preference("consoleclockformat", "IGNORE",
                "", "EEEEEEEEE h:mmaa", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key =  consoleclockformat");
            Console.INSTANCE.refreshClockText();
        }));
        ret.add(new Preference("youtubeuuid", "IGNORE",
                "", "aaaaaaaaaaa", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = youtubeuuid");
            //no action required
        }));
        ret.add(new Preference("ipkey", "IGNORE",
                "", "", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = ipkey");
            //no action required
        }));
        ret.add(new Preference("weatherkey", "IGNORE",
                "", "", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = weatherkey");
            //no action required
        }));
        ret.add(new Preference("youtubeapi3key", "IGNORE",
                "", "", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = youtubeapi3key");
            //no update required
        }));
        ret.add(new Preference("capsmode", "Capital Letters Mode",
                "Capitalize all console output", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = capsmode");
            //no action required
        }));
        ret.add(new Preference("loggedin", "IGNORE",
                "", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = loggedin");
            //no action required
        }));
        ret.add(new Preference("audiolength", "Show Audio Total Length",
                "For the audio player, show the total audio time instead of the time remaining",
                "1", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = audiolength");
            //no action required
        }));
        ret.add(new Preference("persistentnotifications", "Persistent Notifications",
                "Notifications stay on screen until manually dismissed", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = persistentnotifications");
            //no action required
        }));
        ret.add(new Preference("doanimations", "Do Animations",
                "Use animations for things such as frame movement and notifications", "1", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = doanimations");
            //no action required
        }));
        ret.add(new Preference("compacttextmode", "Compact Text",
                "Compact the text/components in supported text panes", "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = compacttextmode");

            Console.INSTANCE.revalidateMenu();
            CyderScrollList.refreshAllLists();

        }));
        ret.add(new Preference("fontmetric", "IGNORE", "",
                "1", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = fontmetric");

            Console.INSTANCE.getInputField().setFont(Console.INSTANCE.generateUserFont());
            Console.INSTANCE.getOutputArea().setFont(Console.INSTANCE.generateUserFont());

        }));
        ret.add(new Preference("fontsize", "IGNORE", "",
                "30", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = fontsize");

            Console.INSTANCE.getInputField().setFont(Console.INSTANCE.generateUserFont());
            Console.INSTANCE.getOutputArea().setFont(Console.INSTANCE.generateUserFont());

        }));
        ret.add(new Preference("wrapshell", "Wrap Shell",
                "Wrap the native shell by passing unrecognized commands to it and allowing it to process them",
                "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = wrapshell");

            //no action required

        }));
        ret.add(new Preference("darkmode", "Dark Mode",
                "Activate a pleasant dark mode for Cyder",
                "0", () -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = darkmode");

            // no action required

        }));

        // display name IGNORE -> ignore for UserEditor switches
        // tooltip IGNORE -> don't write default value when creating a user (username, password)

        // to add: create object in User.java with getter/setter and add new Preference here

        // EVERYTHING that is in userdata must be in this list

        // non primitive types/Strings need to be set via their own object via
        // UserUtil.getCyderUser().getMyObject().setMyMember(myValue);
        // this works because objects are references

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
                pref.getOnChangeFunction().run();
            }
        }
    }
}
