package cyder.user;

import cyder.constants.CyderStrings;
import cyder.handlers.internal.Logger;
import cyder.ui.ConsoleFrame;
import cyder.utilities.ColorUtil;
import cyder.utilities.FrameUtil;
import cyder.utilities.ReflectionUtil;
import cyder.utilities.UserUtil;

import java.util.ArrayList;
import java.util.function.Function;

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
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
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

        ret.add(new Preference("name","IGNORE",
                "IGNORE", "IGNORE", (optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = name");
            //no action required
            return null;
        }));
        ret.add(new Preference("pass","IGNORE",
                "IGNORE", "IGNORE",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = pass");
            //no action required
            return null;
        }));
        ret.add(new Preference("font","IGNORE",
                "","Agency FB",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = font");
            //no action required, updates are done via font chooser field, font metric field, and scroll wheel
            // for the respective font params
            return null;
        }));
        ret.add(new Preference("foreground","IGNORE",
                "","f0f0f0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = foreground");

            ConsoleFrame.getConsoleFrame().getInputField()
                    .setForeground(ColorUtil.hextorgbColor(UserUtil.extractUser().getForeground()));

            return null;
        }));
        ret.add(new Preference("background","IGNORE",
                "","101010",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = background");
            //todo update output/input fill colors/ border colors if shown
            return null;
        }));
        ret.add(new Preference("intromusic", "Intro Music",
                "Play intro music on start","0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = intromusic");
            //no action required
            return null;
        }));
        ret.add(new Preference("debugwindows", "Debug Windows",
                "Show debug menus on startup","0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = debugwindows");
            //no action required
            return null;
        }));
        ret.add(new Preference("randombackground", "Random Background",
                "Choose a random background on startup","0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = randombackground");
            //no action required
            return null;
        }));
        ret.add(new Preference("outputborder", "Output Border",
                "Draw a border around the output area","0", (optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = outputborder");
            //todo update output area border shown
            return null;
        }));
        ret.add(new Preference("inputborder", "Input Border",
                "Draw a border around the input area","0", (optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = inputborder");
            //todo update input field border shown
            return null;
        }));
        ret.add(new Preference("hourlychimes", "Hourly Chimes",
                "Chime every hour","1",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = hourlychimes");
            //no action required, this is checked once an hour
            return null;
        }));
        ret.add(new Preference("silenceerrors", "Silence Errors",
                "Don't open errors externally","1", (optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = silenceerrors");
            //no action required
            return null;
        }));
        ret.add(new Preference("fullscreen", "Fullscreen",
                "Fullscreen cyder (Extremely experimental)","0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = fullscreen");
            ConsoleFrame.getConsoleFrame().setFullscreen(!ConsoleFrame.getConsoleFrame().isFullscreen());
            return null;
        }));
        ret.add(new Preference("outputfill", "Output Fill",
                "Fill the output area with the color specified in the \"Fonts & Colors\" panel",
                "0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = outputfill");
            //todo update output field border shown/not shown
            return null;
        }));
        ret.add(new Preference("inputfill", "Input Fill",
                "Fill the input area with the color specified in the \"Fonts & Colors\" panel",
                "0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = inputfill");
            //todo update input field border shown/not shown
            return null;
        }));
        ret.add(new Preference("clockonconsole", "Clock On Console",
                "Show a clock at the top of the console","1",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = clockonconsole");
            //todo update console clocok
            return null;
        }));
        ret.add(new Preference("showseconds", "Show Seconds",
                "Show seconds on the console clock if enabled","1",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = showseconds");
            //todo update console clock
            return null;
        }));
        ret.add(new Preference("filterchat", "Filter Chat",
                "Filter foul language","1",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = filterchat");
            //no action required
            return null;
        }));
        ret.add(new Preference("laststart","IGNORE",
                "", String.valueOf(System.currentTimeMillis()),(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = laststart");
            //no action required, this is set once on Cyder start
            return null;
        }));
        ret.add(new Preference("minimizeonclose","Minimize On Close",
                "Minimize the application instead of exiting whenever a close action is requested",
                "0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = minimizeonclose");
            //no action required, frames check this before animating
            return null;
        }));
        ret.add(new Preference("typinganimation","Typing Animation",
                "Typing animation on console for non-vital outputs", "1",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = typinganimation");
            //no action required, typing thread will figure it out
            return null;
        }));
        ret.add(new Preference("typingsound","Typing Animation Sound",
                "Typing animation sound effect to play if typing animation is enabled",
                "1",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = typingsound");
            //no action required, typing thread will figure it out
            return null;
        }));
        ret.add(new Preference("showbusyicon", "Show Cyder Busy Icon",
                "Show when Cyder is busy by changing the tray icon","0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = showbusyicon");
            //no action required, busy thread will figure it out
            return null;
        }));
        ret.add(new Preference("ffmpegpath","IGNORE",
                "","",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = ffmpegpath");
            //no update required
            return null;
        }));
        ret.add(new Preference("youtubedlpath","IGNORE",
                "","",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = youtubedlpath");
            //no update required
            return null;
        }));
        ret.add(new Preference("roundedwindows","Rounded Windows",
                "Make certain windows rounded","0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = roundedwindows");
            FrameUtil.repaintCyderFrames();
            return null;
        }));
        ret.add(new Preference("windowcolor","IGNORE",
                "","1A2033",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = windowcolor");
            FrameUtil.repaintCyderFrames();
            return null;
        }));
        ret.add(new Preference("consoleclockformat","IGNORE",
                "","EEEEEEEEE h:mmaa",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key =  consoleclockformat");
            ConsoleFrame.getConsoleFrame().refreshClockText();
            return null;
        }));
        ret.add(new Preference("youtubeuuid","IGNORE",
                "","aaaaaaaaaaa",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = youtubeuuid");
            //no action required
            return null;
        }));
        ret.add(new Preference("ipkey","IGNORE",
                "","",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = ipkey");
            //no action required
            return null;
        }));
        ret.add(new Preference("weatherkey","IGNORE",
                "","",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = weatherkey");
            //no action required
            return null;
        }));
        ret.add(new Preference("capsmode","Capital Letters Mode",
                "Capitalize all console output","0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = capsmode");
            //no action required
            return null;
        }));
        ret.add(new Preference("loggedin","IGNORE",
                "","0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = loggedin");
            //no action required, todo maybe ensure console frame doesn't have this uuid
            return null;
        }));
        ret.add(new Preference("audiolength","Show Audio Total Length",
                "For the audio player, show the total audio time instead of the time remaining",
                "1",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = audiolength");
            //no action required
            return null;
        }));
        ret.add(new Preference("persistentnotifications","Persistent Notifications",
                "Notifications stay on screen until manually dismissed","0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = persistentnotifications");
            //todo any notifications shown, cancel them being vanished if it's before vanish is called
            return null;
        }));
        ret.add(new Preference("minimizeanimation","Minimize Animation",
                "Animate the window away for minimizations","1",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = minimizeanimation");
            //no action required
            return null;
        }));
        ret.add(new Preference("closeanimation","Close Animation",
                "Animate the window away for close requests","1",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = closeanimation");
            //no action required
            return null;
        }));
        ret.add(new Preference("compacttextmode", "Compact Text",
                "Compact the text/components in supported text panes","0",(optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = compacttextmode");
            //todo refresh CyderPanes and the console frame menu
            return null;
        }));
        ret.add(new Preference("fontmetric","IGNORE", "",
                "1", (optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = fontmetric");

            ConsoleFrame.getConsoleFrame().getInputField().setFont(ConsoleFrame.getConsoleFrame().generateUserFont());
            ConsoleFrame.getConsoleFrame().getOutputArea().setFont(ConsoleFrame.getConsoleFrame().generateUserFont());

            return null;
        }));
        ret.add(new Preference("fontsize","IGNORE", "",
                "30", (optionalParam) -> {
            Logger.log(Logger.Tag.PREFERENCE_REFRESH, "key = fontsize");

            ConsoleFrame.getConsoleFrame().getInputField().setFont(ConsoleFrame.getConsoleFrame().generateUserFont());
            ConsoleFrame.getConsoleFrame().getOutputArea().setFont(ConsoleFrame.getConsoleFrame().generateUserFont());

            return null;
        }));

        // IGNORE for display name means ignore for UserEditor,
        // IGNORE for tooltip means don't write when creating user since it was already set
        // such as the case for name and password

        // Adding future prefs:
        // you'll need to add the preference here and also the data in user.java
        // since gson parses the userdata file into a user object.

        //EVERYTHING that is in userdata must be in this list

        // Some rare cases might require deeper manipulation such as the case for executables
        // where we don't add it here but add it for the user object and for user creation
        // this also requires more handling where we want to use the non-string data

        return ret;
    }

    /**
     * Invokes the onChangeFunction() of the preference with the provided ID, if found.
     *
     * @param preferenceID the onChangeFunction() of the preference with the provided ID
     * @return whether the function was found and invoked properly
     */
    public static boolean invokeRefresh(String preferenceID) {
        boolean ret = false;

        for (Preference pref : prefs) {
            if (pref.getID().equalsIgnoreCase(preferenceID)) {
                pref.onChangeFunction.apply(null);
                ret = true;
            }
        }

        return ret;
    }

    /**
     * Preference class used to hold user data in the form of strings.
     */
    public static class Preference {
        private String ID;
        private String displayName;
        private String tooltip;
        private String defaultValue;
        private Function<Void, Void> onChangeFunction;

        public Preference(String id, String displayName,
                          String tooltip, String defaultValue,
                            Function<Void, Void> onChangeFunction) {
            this.ID = id;
            this.displayName = displayName;
            this.tooltip = tooltip;
            this.defaultValue = defaultValue;
            this.onChangeFunction = onChangeFunction;
        }

        public String getID() {
            return ID;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getTooltip() {
            return tooltip;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public void setTooltip(String tooltip) {
            this.tooltip = tooltip;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Function<Void, Void> getOnChangeFunction() {
            return onChangeFunction;
        }

        public void setOnChangeFunction(Function<Void, Void> onChangeFunction) {
            this.onChangeFunction = onChangeFunction;
        }

        @Override
        public String toString() {
            return ReflectionUtil.commonCyderToString(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            else if (!(o instanceof Preference))
                return false;

            return ((Preference) o).getID().equals(this.getID());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int result = ID.hashCode();
            result = 31 * result + displayName.hashCode();
            result = 31 * result + tooltip.hashCode();
            result = 31 * result + defaultValue.hashCode();
            return result;
        }
    }
}
