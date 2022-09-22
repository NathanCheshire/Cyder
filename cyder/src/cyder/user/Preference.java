package cyder.user;

import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.logging.Logger;
import cyder.ui.pane.CyderScrollList;
import cyder.utils.ColorUtil;
import cyder.utils.ReflectionUtil;
import cyder.utils.UiUtil;
import cyder.widgets.WeatherWidget;

import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Preference class used to hold user data in the form of strings.
 * Instances of this class are immutable and thus thread safe.
 */
@SuppressWarnings("SpellCheckingInspection") /* key names */
public class Preference {
    /*
    Keys
     */

    public static final String NAME = "name";
    public static final String PASS = "pass";
    public static final String FONT = "Font";
    public static final String FOREGROUND = "foreground";
    public static final String BACKGROUND = "background";
    public static final String INTRO_MUSIC = "intromusic";
    public static final String DEBUG_WINDOWS = "debugwindows";
    public static final String RANDOM_BACKGROUND = "randombackground";
    public static final String OUTPUT_BORDER = "outputborder";
    public static final String INPUT_BORDER = "inputborder";
    public static final String HOURLY_CHIMES = "hourlychimes";
    public static final String SILENCE_ERRORS = "silenceerrors";
    public static final String FULLSCREEN = "fullscreen";
    public static final String OUTPUT_FILL = "outputfill";
    public static final String INPUT_FILL = "inputfill";
    public static final String CLOCK_ON_CONSOLE = "clockonconsole";
    public static final String SHOW_SECONDS = "showseconds";
    public static final String FILTER_CHAT = "filterchat";
    public static final String LAST_START = "laststart";
    public static final String MINIMIZE_ON_CLOSE = "minimizeonclose";
    public static final String TYPING_ANIMATION = "typinganimation";
    public static final String TYPING_SOUND = "typingsound";
    public static final String SHOW_BUSY_ICON = "showbusyicon";
    public static final String ROUNDED_WINDOWS = "roundedwindows";
    public static final String WINDOW_COLOR = "windowcolor";
    public static final String CONSOLE_CLOCK_FORMAT = "consoleclockformat";
    public static final String YOUTUBE_UUID = "youtubeuuid";
    public static final String IP_KEY = "ipkey";
    public static final String WEATHER_KEY = "weatherkey";
    public static final String YOUTUBE_API_3_KEY = "youtubeapi3key";
    public static final String CAPS_MODE = "capsmode";
    public static final String LOGGED_IN = "loggedin";
    public static final String AUDIO_LENGTH = "audiolength";
    public static final String PERSISTENT_NOTIFICATIONS = "persistentnotifications";
    public static final String DO_ANIMATIONS = "doanimations";
    public static final String COMPACT_TEXT_MODE = "compacttextmode";
    public static final String FONT_METRIC = "fontmetric";
    public static final String FONT_SIZE = "fontsize";
    public static final String WRAP_SHELL = "wrapshell";
    public static final String DARK_MODE = "darkmode";
    private static final String WEATHER_MAP = "weathermap";

    /*
    Special values.
     */

    private static final String IGNORE = "IGNORE";
    private static final String EMPTY = "";

    /**
     * The immutable collection of preference objects.
     */
    private static final ImmutableList<Preference> preferences = ImmutableList.of(
            new Preference(NAME, IGNORE, IGNORE, IGNORE,
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, NAME))
                    .setIgnoreForToggleSwitches(true)
                    .setIgnoreForUserCreation(true),

            new Preference(PASS, IGNORE, IGNORE, IGNORE,
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, PASS))
                    .setIgnoreForToggleSwitches(true)
                    .setIgnoreForUserCreation(true),

            new Preference(FONT, IGNORE, EMPTY, "Agency FB",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, FONT))
                    .setIgnoreForToggleSwitches(true),

            new Preference(FOREGROUND, IGNORE, EMPTY, "f0f0f0", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, FOREGROUND);
                Console.INSTANCE.getInputField().setForeground(
                        ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()));
            }).setIgnoreForToggleSwitches(true),

            new Preference(BACKGROUND, IGNORE, EMPTY, "101010",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, BACKGROUND))
                    .setIgnoreForToggleSwitches(true),

            new Preference(INTRO_MUSIC, "Intro Music", "Play intro music on start",
                    "0", () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, INTRO_MUSIC)),

            new Preference(DEBUG_WINDOWS, "Debug Windows",
                    "Show debug menus on startup", "0",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, "DEBUG_WINDOWS")),

            new Preference(RANDOM_BACKGROUND, "Random Background",
                    "Choose a random background on startup", "0",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, RANDOM_BACKGROUND)),

            new Preference(OUTPUT_BORDER, "Output Border",
                    "Draw a border around the output area", "0", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, OUTPUT_BORDER);

                if (UserUtil.getCyderUser().getOutputborder().equals("0")) {
                    Console.INSTANCE.getOutputScroll().setBorder(BorderFactory.createEmptyBorder());
                } else {
                    LineBorder lineBorder = new LineBorder(ColorUtil.hexStringToColor(
                            UserUtil.getCyderUser().getBackground()), UserEditor.inputOutputBorderThickness, true);
                    Console.INSTANCE.getOutputScroll().setBorder(lineBorder);
                }
            }),

            new Preference(INPUT_BORDER, "Input Border", "Draw a border around the input area",
                    "0", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, INPUT_BORDER);

                if (UserUtil.getCyderUser().getInputborder().equals("0")) {
                    Console.INSTANCE.getInputField().setBorder(null);
                } else {
                    Console.INSTANCE.getInputField().setBorder(new LineBorder(ColorUtil.hexStringToColor(
                            UserUtil.getCyderUser().getBackground()), 3, true));
                }
            }),

            new Preference(HOURLY_CHIMES, "Hourly Chimes", "Chime every hour", "1",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, HOURLY_CHIMES)),

            new Preference(SILENCE_ERRORS, "Silence Errors", "Don't open errors externally",
                    "1", () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, SILENCE_ERRORS)),

            new Preference(FULLSCREEN, "Fullscreen",
                    "Fullscreen Cyder (this will also cover the Windows taskbar)", "0", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, FULLSCREEN);
                Console.INSTANCE.setFullscreen(UserUtil.getCyderUser().getFullscreen().equals("1"));
            }),

            new Preference(OUTPUT_FILL, "Output Fill",
                    "Fill the output area with the color specified in the \"Fonts & Colors\" panel",
                    "0", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, OUTPUT_FILL);

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
            }),

            new Preference(INPUT_FILL, "Input Fill",
                    "Fill the input area with the color specified in the \"Fonts & Colors\" panel",
                    "0", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, INPUT_FILL);

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
            }),

            new Preference(CLOCK_ON_CONSOLE, "Clock On Console",
                    "Show a clock at the top of the console", "1", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, CLOCK_ON_CONSOLE);
                Console.INSTANCE.refreshClockText();
            }),

            new Preference(SHOW_SECONDS, "Show Seconds",
                    "Show seconds on the console clock if enabled", "1", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, SHOW_SECONDS);
                Console.INSTANCE.refreshClockText();
            }),

            new Preference(FILTER_CHAT, "Filter Chat", "Filter foul language", "1",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, FILTER_CHAT)),

            new Preference(LAST_START, IGNORE, EMPTY, String.valueOf(System.currentTimeMillis()),
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, LAST_START))
                    .setIgnoreForToggleSwitches(true),

            new Preference(MINIMIZE_ON_CLOSE, "Minimize On Close",
                    "Minimize the application instead of exiting whenever a close action is requested",
                    "0", () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, MINIMIZE_ON_CLOSE)),

            new Preference(TYPING_ANIMATION, "Typing Animation",
                    "Typing animation on console for non-vital outputs", "1",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, TYPING_ANIMATION)),

            new Preference(TYPING_SOUND, "Typing Animation Sound",
                    "Typing animation sound effect to play if typing animation is enabled",
                    "1", () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, TYPING_SOUND)),

            new Preference(SHOW_BUSY_ICON, "Show Cyder Busy Icon",
                    "Show when Cyder is busy by changing the tray icon", "0",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, SHOW_BUSY_ICON)),

            new Preference(ROUNDED_WINDOWS, "Rounded Windows", "Make certain windows rounded",
                    "0", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, ROUNDED_WINDOWS);
                UiUtil.repaintCyderFrames();
            }),

            new Preference(WINDOW_COLOR, IGNORE, EMPTY, "1A2033", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, WINDOW_COLOR);

                UiUtil.repaintCyderFrames();
                Console.INSTANCE.revalidateMenuBackgrounds();
            }).setIgnoreForToggleSwitches(true),

            new Preference(CONSOLE_CLOCK_FORMAT, IGNORE, EMPTY, "EEEEEEEEE h:mmaa", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, CONSOLE_CLOCK_FORMAT);
                Console.INSTANCE.refreshClockText();
            }).setIgnoreForToggleSwitches(true),

            new Preference(YOUTUBE_UUID, IGNORE, EMPTY, "aaaaaaaaaaa",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, YOUTUBE_UUID))
                    .setIgnoreForToggleSwitches(true),

            new Preference(IP_KEY, IGNORE, EMPTY, EMPTY,
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, IP_KEY))
                    .setIgnoreForToggleSwitches(true),

            new Preference(WEATHER_KEY, IGNORE, EMPTY, EMPTY,
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, WEATHER_KEY))
                    .setIgnoreForToggleSwitches(true),

            new Preference(YOUTUBE_API_3_KEY, IGNORE, EMPTY, EMPTY,
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, YOUTUBE_API_3_KEY))
                    .setIgnoreForToggleSwitches(true),

            new Preference(CAPS_MODE, "Capital Letters Mode", "Capitalize all console output",
                    "0", () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, CAPS_MODE)),

            new Preference(LOGGED_IN, IGNORE, EMPTY, "0",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, LOGGED_IN))
                    .setIgnoreForToggleSwitches(true),

            new Preference(AUDIO_LENGTH, "Show Audio Total Length",
                    "For the audio player, show the total audio time instead of the time remaining",
                    "1", () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, AUDIO_LENGTH)),

            new Preference(PERSISTENT_NOTIFICATIONS, "Persistent Notifications",
                    "Notifications stay on screen until manually dismissed", "0",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, PERSISTENT_NOTIFICATIONS)),

            new Preference(DO_ANIMATIONS, "Do Animations",
                    "Use animations for things such as frame movement and notifications", "1",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, DO_ANIMATIONS)),

            new Preference(COMPACT_TEXT_MODE, "Compact Text",
                    "Compact the text/components in supported text panes", "0", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, COMPACT_TEXT_MODE);

                Console.INSTANCE.revalidateMenu();
                CyderScrollList.refreshAllLists();
            }),

            new Preference(FONT_METRIC, IGNORE, EMPTY, "1", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, FONT_METRIC);

                Console.INSTANCE.getInputField().setFont(Console.INSTANCE.generateUserFont());
                Console.INSTANCE.getOutputArea().setFont(Console.INSTANCE.generateUserFont());
            }).setIgnoreForToggleSwitches(true),

            new Preference(FONT_SIZE, IGNORE, EMPTY, "30", () -> {
                Logger.log(Logger.Tag.PREFERENCE_REFRESH, FONT_SIZE);

                Console.INSTANCE.getInputField().setFont(Console.INSTANCE.generateUserFont());
                Console.INSTANCE.getOutputArea().setFont(Console.INSTANCE.generateUserFont());
            }).setIgnoreForToggleSwitches(true),

            new Preference(WRAP_SHELL, "Wrap Shell", "Wrap the native shell by"
                    + " passing unrecognized commands to it and allowing it to process them", "0",
                    () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, WRAP_SHELL)),

            new Preference(DARK_MODE, "Dark Mode", "Activate a pleasant dark mode for Cyder",
                    "0", () -> Logger.log(Logger.Tag.PREFERENCE_REFRESH, DARK_MODE)),

            new Preference(WEATHER_MAP, "Weather Map",
                    "Show a map of the location's area in the weather widget background", "1",
                    () -> {
                        Logger.log(Logger.Tag.PREFERENCE_REFRESH, WEATHER_MAP);
                        WeatherWidget.refreshAllMapBackgrounds();
                    })

            /*
                To add: create object in User.java with a getter/setter and add a new Preference here

                Everything in userdata must be in this list in some way, perhaps this hints at
                this class being called something different

                Note: non primitive types such as ScreenStat need to be set by using the object reference
                returned by the getter, UserUtil.getCyderUser().getScreenStat().setX(0);
             */
    );

    /**
     * Returns the preferences collection.
     *
     * @return the preferences collection
     */
    public static ImmutableList<Preference> getPreferences() {
        return preferences;
    }

    /**
     * Invokes the onChangeFunction() of the preference with the provided ID, if found.
     *
     * @param preferenceID the onChangeFunction() of the preference with the provided ID
     */
    public static void invokeRefresh(String preferenceID) {
        boolean invoked = false;

        for (Preference preference : preferences) {
            if (preference.getID().equals(preferenceID)) {
                preference.getOnChangeFunction().run();
                postPreferenceOnChangeFunctionHook();
                invoked = true;
                break;
            }
        }

        if (!invoked) {
            Logger.log(Logger.Tag.DEBUG, "Failed to invoke preference refresh."
                    + " Provided id: " + preferenceID);
        }
    }

    /**
     * A hook to be ran after all preference on change function invocations.
     */
    private static void postPreferenceOnChangeFunctionHook() {
        UserEditor.revalidatePreferencesIfOpen();
    }

    /**
     * Returns the preference with the provided id.
     *
     * @param preferenceID the provided id to get
     * @return the preference with the provided id
     * @throws IllegalArgumentException if a preference with the provided id cannot be found
     */
    public static Preference get(String preferenceID) {
        for (Preference preference : preferences) {
            if (preference.getID().equals(preferenceID)) {
                return preference;
            }
        }

        throw new IllegalArgumentException("Preference with id not found: " + preferenceID);
    }

    /**
     * The id of the preference.
     */
    private final String id;

    /**
     * The name to display for the preference when allowing the user to make changes.
     */
    private final String displayName;

    /**
     * The tooltip for the toggle/change button/field.
     */
    private final String tooltip;

    /**
     * The default value for the preference.
     */
    private final Object defaultValue;

    /**
     * The method to run when a change of the preference occurs.
     */
    private final Runnable onChangeFunction;

    /**
     * Constructs a preference object.
     *
     * @param id               the id of the preference
     * @param displayName      the display name
     * @param tooltip          the tooltip text for the toggle button
     * @param defaultValue     the default value
     * @param onChangeFunction the method to run when a change of the preference occurs
     */
    public Preference(String id, String displayName, String tooltip, Object defaultValue, Runnable onChangeFunction) {
        this.id = id;
        this.displayName = displayName;
        this.tooltip = tooltip;
        this.defaultValue = defaultValue;
        this.onChangeFunction = onChangeFunction;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the id of the preference.
     *
     * @return the id of the preference
     */
    public String getID() {
        return id;
    }

    /**
     * Returns the display name of the preference.
     *
     * @return the display name of the preference
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the tooltip text used for the preference toggler.
     *
     * @return the tooltip text used for the preference toggler
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Returns the default value.
     *
     * @return the default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the function to invoke upon a change of the preference.
     *
     * @return the function to invoke upon a change of the preference
     */
    public Runnable getOnChangeFunction() {
        return onChangeFunction;
    }

    /**
     * Whether this preference should be ignored when creating the user preference toggle switches.
     */
    private boolean ignoreForToggleSwitches = false;

    /**
     * Returns whether this preference should be ignored when setting up the toggle switches for the user editor.
     *
     * @return whether this preference should be ignored when setting up the toggle switches for the user editor
     */
    public boolean getIgnoreForToggleSwitches() {
        return ignoreForToggleSwitches;
    }

    /**
     * Sets whether this preference should be ignored when setting up the toggle switches for the user editor.
     *
     * @param ignore whether this preference should be ignored when setting up the toggle switches for the user editor
     * @return this preference
     */
    public Preference setIgnoreForToggleSwitches(boolean ignore) {
        this.ignoreForToggleSwitches = ignore;
        return this;
    }

    /**
     * Whether this preference should be ignored when creating a new user.
     * Typically this is only username and password.
     */
    private boolean ignoreForUserCreation = false;

    /**
     * Returns whether this preference should be ignored when building a default/new user.
     * Typically this is only username and password.
     *
     * @return whether this preference should be ignored when building a default/new user
     */
    public boolean getIgnoreForUserCreation() {
        return ignoreForUserCreation;
    }

    /**
     * Sets whether this preference should be ignored when building a new user.
     *
     * @param ignore whether this preference should be ignored when building a new user
     * @return this preference
     */
    public Preference setIgnoreForUserCreation(boolean ignore) {
        this.ignoreForUserCreation = ignore;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Preference)) {
            return false;
        }

        return ((Preference) o).getID().equals(getID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + tooltip.hashCode();
        result = 31 * result + defaultValue.hashCode();
        result = 31 * result + onChangeFunction.hashCode();
        return result;
    }
}