package cyder.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.exceptions.FatalException;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.ui.UiUtil;
import cyder.ui.pane.CyderScrollList;
import cyder.utils.ColorUtil;
import cyder.weather.WeatherWidget;
import cyder.widgets.ClockWidget;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static cyder.strings.CyderStrings.EMPTY;

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
    public static final String DEBUG_STATS = "debugstats";
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
    public static final String CAPS_MODE = "capsmode";
    public static final String LOGGED_IN = "loggedin";
    public static final String AUDIO_LENGTH = "audiolength";
    public static final String PERSISTENT_NOTIFICATIONS = "persistentnotifications";
    public static final String DO_ANIMATIONS = "doanimations";
    public static final String COMPACT_TEXT_MODE = "compacttextmode";
    public static final String FONT_SIZE = "fontsize";
    public static final String WRAP_SHELL = "wrapshell";
    public static final String DARK_MODE = "darkmode";
    private static final String WEATHER_MAP = "weathermap";
    private static final String PAINT_CLOCK_LABELS = "paintclocklabels";
    private static final String SHOW_SECOND_HAND = "showsecondhand";

    /*
    Special values.
     */

    private static final String IGNORE = "IGNORE";

    /**
     * The immutable collection of preference objects.
     */
    private static final ImmutableList<Preference> preferences = ImmutableList.of(
            new Preference(NAME, IGNORE, IGNORE, IGNORE,
                    () -> Logger.log(LogTag.PREFERENCE, NAME))
                    .setIgnoreForToggleSwitches()
                    .setIgnoreForUserCreation(),

            new Preference(PASS, IGNORE, IGNORE, IGNORE,
                    () -> Logger.log(LogTag.PREFERENCE, PASS))
                    .setIgnoreForToggleSwitches()
                    .setIgnoreForUserCreation(),

            new Preference(FONT, IGNORE, EMPTY, "Agency FB",
                    () -> Logger.log(LogTag.PREFERENCE, FONT))
                    .setIgnoreForToggleSwitches(),

            new Preference(FOREGROUND, IGNORE, EMPTY, "f0f0f0", () -> {
                Logger.log(LogTag.PREFERENCE, FOREGROUND);
                Console.INSTANCE.getInputField().setForeground(
                        ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()));
            }).setIgnoreForToggleSwitches(),

            new Preference(BACKGROUND, IGNORE, EMPTY, "101010",
                    () -> Logger.log(LogTag.PREFERENCE, BACKGROUND))
                    .setIgnoreForToggleSwitches(),

            new Preference(INTRO_MUSIC, "Intro Music", "Play intro music on start",
                    "0", () -> Logger.log(LogTag.PREFERENCE, INTRO_MUSIC)),

            new Preference(DEBUG_STATS, "Debug Windows",
                    "Show debug menus on startup", "0",
                    () -> Logger.log(LogTag.PREFERENCE, DEBUG_WINDOWS)),

            new Preference(RANDOM_BACKGROUND, "Random Background",
                    "Choose a random background on startup", "0",
                    () -> Logger.log(LogTag.PREFERENCE, RANDOM_BACKGROUND)),

            new Preference(OUTPUT_BORDER, "Output Border",
                    "Draw a border around the output area", "0", () -> {
                Logger.log(LogTag.PREFERENCE, OUTPUT_BORDER);

                if (UserUtil.getCyderUser().getOutputBorder().equals("0")) {
                    Console.INSTANCE.getOutputScroll().setBorder(BorderFactory.createEmptyBorder());
                } else {
                    LineBorder lineBorder = new LineBorder(ColorUtil.hexStringToColor(
                            UserUtil.getCyderUser().getBackground()), UserEditor.inputOutputBorderThickness, true);
                    Console.INSTANCE.getOutputScroll().setBorder(lineBorder);
                }
            }),

            new Preference(INPUT_BORDER, "Input Border", "Draw a border around the input area",
                    "0", () -> {
                Logger.log(LogTag.PREFERENCE, INPUT_BORDER);

                if (UserUtil.getCyderUser().getInputBorder().equals("0")) {
                    Console.INSTANCE.getInputField().setBorder(null);
                } else {
                    Console.INSTANCE.getInputField().setBorder(new LineBorder(ColorUtil.hexStringToColor(
                            UserUtil.getCyderUser().getBackground()), 3, true));
                }
            }),

            new Preference(HOURLY_CHIMES, "Hourly Chimes", "Chime every hour", "1",
                    () -> Logger.log(LogTag.PREFERENCE, HOURLY_CHIMES)),

            new Preference(SILENCE_ERRORS, "Silence Errors", "Don't open errors externally",
                    "1", () -> Logger.log(LogTag.PREFERENCE, SILENCE_ERRORS)),

            new Preference(FULLSCREEN, "Fullscreen",
                    "Fullscreen Cyder (this will also cover the Windows taskbar)", "0", () -> {
                Logger.log(LogTag.PREFERENCE, FULLSCREEN);
                Console.INSTANCE.setFullscreen(UserUtil.getCyderUser().getFullscreen().equals("1"));
            }),

            new Preference(OUTPUT_FILL, "Output Fill",
                    "Fill the output area with the color specified in the \"Fonts & Colors\" panel",
                    "0", () -> {
                Logger.log(LogTag.PREFERENCE, OUTPUT_FILL);

                JTextPane outputArea = Console.INSTANCE.getOutputArea();

                if (UserUtil.getCyderUser().getOutputFill().equals("0")) {
                    outputArea.setBackground(null);
                    outputArea.setOpaque(false);
                } else {
                    Color outputFillColor = ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground());

                    outputArea.setOpaque(true);
                    outputArea.setBackground(outputFillColor);
                    outputArea.repaint();
                    outputArea.revalidate();

                    // todo this works so need a working system
                    outputArea.setBackground(new Color(outputFillColor.getRed(), outputFillColor.getGreen(),
                            outputFillColor.getBlue(), 120));
                    outputArea.revalidate();
                    outputArea.repaint();
                }
            }),

            new Preference(INPUT_FILL, "Input Fill",
                    "Fill the input area with the color specified in the \"Fonts & Colors\" panel",
                    "0", () -> {
                Logger.log(LogTag.PREFERENCE, INPUT_FILL);

                if (UserUtil.getCyderUser().getInputFill().equals("0")) {
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
                Logger.log(LogTag.PREFERENCE, CLOCK_ON_CONSOLE);
                Console.INSTANCE.refreshClockText();
            }),

            new Preference(SHOW_SECONDS, "Show Seconds",
                    "Show seconds on the console clock if enabled", "1", () -> {
                Logger.log(LogTag.PREFERENCE, SHOW_SECONDS);
                Console.INSTANCE.refreshClockText();
            }),

            new Preference(FILTER_CHAT, "Filter Chat", "Filter foul language", "1",
                    () -> Logger.log(LogTag.PREFERENCE, FILTER_CHAT)),

            new Preference(LAST_START, IGNORE, EMPTY, String.valueOf(System.currentTimeMillis()),
                    () -> Logger.log(LogTag.PREFERENCE, LAST_START))
                    .setIgnoreForToggleSwitches(),

            new Preference(MINIMIZE_ON_CLOSE, "Minimize On Close",
                    "Minimize the application instead of exiting whenever a close action is requested",
                    "0", () -> Logger.log(LogTag.PREFERENCE, MINIMIZE_ON_CLOSE)),

            new Preference(TYPING_ANIMATION, "Typing Animation",
                    "Typing animation on console for non-vital outputs", "1",
                    () -> Logger.log(LogTag.PREFERENCE, TYPING_ANIMATION)),

            new Preference(TYPING_SOUND, "Typing Animation Sound",
                    "Typing animation sound effect to play if typing animation is enabled",
                    "1", () -> Logger.log(LogTag.PREFERENCE, TYPING_SOUND)),

            new Preference(SHOW_BUSY_ICON, "Show Cyder Busy Animation",
                    "Show when Cyder is busy by changing the tray icon", "1",
                    () -> {
                        Console.INSTANCE.hideBusyAnimation();
                        Logger.log(LogTag.PREFERENCE, SHOW_BUSY_ICON);
                    }),

            new Preference(ROUNDED_WINDOWS, "Rounded Windows", "Make certain windows rounded",
                    "0", () -> {
                Logger.log(LogTag.PREFERENCE, ROUNDED_WINDOWS);
                UiUtil.repaintCyderFrames();
            }),

            new Preference(WINDOW_COLOR, IGNORE, EMPTY, "1A2033", () -> {
                Logger.log(LogTag.PREFERENCE, WINDOW_COLOR);

                UiUtil.repaintCyderFrames();
                Console.INSTANCE.revalidateMenuBackgrounds();
            }).setIgnoreForToggleSwitches(),

            new Preference(CONSOLE_CLOCK_FORMAT, IGNORE, EMPTY, "EEEEEEEEE h:mmaa", () -> {
                Logger.log(LogTag.PREFERENCE, CONSOLE_CLOCK_FORMAT);
                Console.INSTANCE.refreshClockText();
            }).setIgnoreForToggleSwitches(),

            new Preference(YOUTUBE_UUID, IGNORE, EMPTY, "aaaaaaaaaaa",
                    () -> Logger.log(LogTag.PREFERENCE, YOUTUBE_UUID))
                    .setIgnoreForToggleSwitches(),

            new Preference(CAPS_MODE, "Capital Letters Mode", "Capitalize all console output",
                    "0", () -> Logger.log(LogTag.PREFERENCE, CAPS_MODE)),

            new Preference(LOGGED_IN, IGNORE, EMPTY, "0",
                    () -> Logger.log(LogTag.PREFERENCE, LOGGED_IN))
                    .setIgnoreForToggleSwitches(),

            new Preference(AUDIO_LENGTH, "Show Audio Total Length",
                    "For the audio player, show the total audio time instead of the time remaining",
                    "1", () -> Logger.log(LogTag.PREFERENCE, AUDIO_LENGTH)),

            new Preference(PERSISTENT_NOTIFICATIONS, "Persistent Notifications",
                    "Notifications stay on screen until manually dismissed", "0",
                    () -> Logger.log(LogTag.PREFERENCE, PERSISTENT_NOTIFICATIONS)),

            new Preference(DO_ANIMATIONS, "Do Animations",
                    "Use animations for things such as frame movement and notifications", "1",
                    () -> Logger.log(LogTag.PREFERENCE, DO_ANIMATIONS)),

            new Preference(COMPACT_TEXT_MODE, "Compact Text",
                    "Compact the text/components in supported text panes", "0", () -> {
                Logger.log(LogTag.PREFERENCE, COMPACT_TEXT_MODE);

                Console.INSTANCE.revalidateConsoleTaskbarMenu();
                CyderScrollList.refreshAllLists();
            }),

            new Preference(FONT_SIZE, IGNORE, EMPTY, "30", () -> {
                Logger.log(LogTag.PREFERENCE, FONT_SIZE);

                Console.INSTANCE.getInputField().setFont(Console.INSTANCE.generateUserFont());
                Console.INSTANCE.getOutputArea().setFont(Console.INSTANCE.generateUserFont());
            }).setIgnoreForToggleSwitches(),

            new Preference(WRAP_SHELL, "Wrap Shell", "Wrap the native shell by"
                    + " passing unrecognized commands to it and allowing it to process them", "0",
                    () -> Logger.log(LogTag.PREFERENCE, WRAP_SHELL)),

            new Preference(DARK_MODE, "Dark Mode", "Activate a pleasant dark mode for Cyder",
                    "0", () -> Logger.log(LogTag.PREFERENCE, DARK_MODE)),

            new Preference(WEATHER_MAP, "Weather Map",
                    "Show a map of the location's area in the weather widget background", "1",
                    () -> {
                        Logger.log(LogTag.PREFERENCE, WEATHER_MAP);
                        WeatherWidget.refreshAllMapBackgrounds();
                    }),

            new Preference(PAINT_CLOCK_LABELS, "Paint Clock Labels",
                    "Whether to paint the hour labels on the clock widget", "1",
                    () -> {
                        Logger.log(LogTag.PREFERENCE, PAINT_CLOCK_LABELS);
                        ClockWidget.setPaintHourLabels(UserUtil.getCyderUser().getPaintClockLabels().equals("1"));
                    }),

            new Preference(SHOW_SECOND_HAND, "Show Second Hand",
                    "Whether to show the second hand on the clock widget", "1",
                    () -> {
                        Logger.log(LogTag.PREFERENCE, SHOW_SECOND_HAND);
                        ClockWidget.setShowSecondHand(UserUtil.getCyderUser().getShowSecondHand().equals("1"));
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
        for (Preference preference : preferences) {
            if (preference.getID().equals(preferenceID)) {
                preference.getOnChangeFunction().run();
                onPreferenceRefresh();
                return;
            }
        }

        throw new FatalException("Failed to invoke preference refresh, failed to find id: " + preferenceID);
    }

    /**
     * A hook to be ran after all preference on change function invocations.
     */
    private static void onPreferenceRefresh() {
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
        Preconditions.checkNotNull(preferenceID);
        Preconditions.checkArgument(!preferenceID.isEmpty());

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
     * Whether this preference should be ignored when creating the user preference toggle switches.
     */
    private boolean ignoreForToggleSwitches = false;

    /**
     * Whether this preference should be ignored when creating a new user.
     * Typically this is only username and password.
     */
    private boolean ignoreForUserCreation = false;

    /**
     * Constructs a preference object.
     *
     * @param id               the id of the preference
     * @param displayName      the display name
     * @param tooltip          the tooltip text for the toggle button
     * @param defaultValue     the default value
     * @param onChangeFunction the method to run when a change of the preference occurs
     */
    private Preference(String id, String displayName, String tooltip, Object defaultValue, Runnable onChangeFunction) {
        Preconditions.checkNotNull(id);
        Preconditions.checkArgument(!id.isEmpty());
        Preconditions.checkNotNull(displayName);
        Preconditions.checkArgument(!displayName.isEmpty());
        Preconditions.checkNotNull(tooltip);
        Preconditions.checkNotNull(defaultValue);
        Preconditions.checkNotNull(onChangeFunction);

        this.id = id;
        this.displayName = displayName;
        this.tooltip = tooltip;
        this.defaultValue = defaultValue;
        this.onChangeFunction = onChangeFunction;

        Logger.log(LogTag.OBJECT_CREATION, this);
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
     * Returns whether this preference should be ignored when setting up the toggle switches for the user editor.
     *
     * @return whether this preference should be ignored when setting up the toggle switches for the user editor
     */
    public boolean getIgnoreForToggleSwitches() {
        return ignoreForToggleSwitches;
    }

    /**
     * Sets this preference to be ignored when setting up the toggle switches for the user editor.
     *
     * @return this preference
     */
    private Preference setIgnoreForToggleSwitches() {
        this.ignoreForToggleSwitches = true;
        return this;
    }

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
     * Sets this preference to be ignored when building a new user.
     *
     * @return this preference
     */
    private Preference setIgnoreForUserCreation() {
        this.ignoreForUserCreation = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Preference{"
                + "id=" + CyderStrings.quote + id + CyderStrings.quote
                + ", displayName=" + CyderStrings.quote + displayName + CyderStrings.quote
                + ", tooltip=" + CyderStrings.quote + tooltip + CyderStrings.quote
                + ", defaultValue=" + defaultValue
                + ", onChangeFunction=" + onChangeFunction
                + ", ignoreForToggleSwitches=" + ignoreForToggleSwitches
                + ", ignoreForUserCreation=" + ignoreForUserCreation
                + "}";
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

        Preference other = (Preference) o;

        return getID().equals(other.getID())
                && getDisplayName().equals(other.getDisplayName())
                && getTooltip().equals(other.getTooltip())
                && getDefaultValue().equals(other.getDefaultValue())
                && getOnChangeFunction().equals(other.getOnChangeFunction());
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