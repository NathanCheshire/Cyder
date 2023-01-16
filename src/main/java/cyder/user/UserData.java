package cyder.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.console.Console;
import cyder.exceptions.FatalException;
import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * UserData class used to hold user data default values and anyother meta types associated.
 */
@SuppressWarnings({"SpellCheckingInspection", "CodeBlock2Expr"}) /* Key names, readability */
public final class UserData<T> {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String FONT_NAME = "font_name";
    public static final String FOREGROUND = "foreground";
    public static final String BACKGROUND = "background";
    public static final String FONT_SIZE = "font_size";
    public static final String FONT_METRIC = "font_metric";
    public static final String FOREGROUND_COLOR = "foreground_color";
    public static final String BACKGROUND_COLOR = "background_color";
    public static final String INTRO_MUSIC = "intro_music";
    public static final String DEBUG_STATS = "debug_stats";
    public static final String RANDOM_BACKGROUND = "random_background";
    public static final String INPUT_BORDER = "input_border";
    public static final String OUTPUT_BORDER = "output_border";
    public static final String HOURLY_CHIMES = "hourly_chimes";
    public static final String SILENCE_ERRORS = "silence_errors";
    public static final String FULLSCREEN = "fullscreen";
    public static final String OUTPUT_FILL = "output_fill";
    public static final String INPUT_FILL = "input_fill";
    public static final String CONSOLE_CLOCK = "console_clock";
    public static final String CONSOLE_CLOCK_SECONDS = "console_clock_seconds";
    public static final String FILTER_CHAT = "filter_chat";
    public static final String LAST_SESSION_START = "last_session_start";
    public static final String MINIMIZE_ON_CLOSE = "minimize_on_close";
    public static final String TYPING_ANIMATION = "typing_animation";
    public static final String BUSY_ANIMATION = "busy_animation";
    public static final String ROUNDED_FRAME_BORDERS = "rounded_frame_borders";
    public static final String FRAME_COLOR = "frame_color";
    public static final String CLOCK_FORMAT = "clock_format";
    public static final String TYPING_SOUND = "typing_sound";
    public static final String YOUTUBE_UUID = "youtube_uuid";
    public static final String CAPS_MODE = "caps_mode";
    public static final String LOGGED_IN = "logged_in";
    public static final String AUDIO_TOTAL_LENGTH = "audio_total_length";
    public static final String SHOULD_PERSIST_NOTIFICATIONS = "should_persist_notifications";
    public static final String SHOULD_DO_ANIMATIONS = "should_do_animations";
    public static final String COMPACT_TEXT_MODE = "compact_text_mode";
    public static final String WRAP_SHELL = "wrap_shell";
    public static final String DRAW_WEATHER_MAP = "draw_weather_map";
    public static final String PAINT_CLOCK_LABELS = "paint_clock_labels";
    public static final String CLOCK_WIDGET_SECOND_HAND = "clock_widget_second_hand";
    public static final String SCREEN_STAT = "screen_stat";
    public static final String MAPPED_EXECUTABLES = "mapped_executables";
    public static final String FILL_OPACITY = "fill_opacity";
    public static final String SHOWN_WELCOME_MESSAGE = "shown_welcome_message";
    public static final String ACCOUNT_CREATION_TIME = "account_creation_time";

    UserData<String> username = new Builder<>(USERNAME, String.class)
            .setDescription("The user's public username")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, USERNAME);
                Console.INSTANCE.refreshConsoleSuperTitle();
            }).build();

    UserData<String> password = new Builder<>(PASSWORD, String.class)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, PASSWORD);
                // todo log out user
            }).build();

    //    /**
    //     * The immutable collection of user data objects.
    //     */
    //    private static final ImmutableList<UserData<T>> userDatas = ImmutableList.of(
    //            new UserData<String>(USERNAME, IGNORE, IGNORE, IGNORE,
    //                    () -> Logger.log(LogTag.USER_DATA, USERNAME, String.class))
    //                    .setIgnoreForToggleSwitches()
    //                    .setIgnoreForUserCreation(),
    //
    //            new UserData(PASSWORD, IGNORE, IGNORE, IGNORE,
    //                    () -> Logger.log(LogTag.USER_DATA, PASSWORD))
    //                    .setIgnoreForToggleSwitches()
    //                    .setIgnoreForUserCreation(),
    //
    //            new UserData(FONT_NAME, IGNORE, EMPTY, "Agency FB",
    //                    () -> Logger.log(LogTag.USER_DATA, FONT_NAME))
    //                    .setIgnoreForToggleSwitches(),
    //
    //            new UserData(FOREGROUND, IGNORE, EMPTY, "f0f0f0", () -> {
    //                Logger.log(LogTag.USER_DATA, FOREGROUND);
    //                Console.INSTANCE.getInputField().setForeground(UserDataManager.INSTANCE.getForegroundColor());
    //            }).setIgnoreForToggleSwitches(),
    //
    //            new UserData(BACKGROUND, IGNORE, EMPTY, "101010",
    //                    () -> Logger.log(LogTag.USER_DATA, BACKGROUND))
    //                    .setIgnoreForToggleSwitches(),
    //
    //            new UserData(INTRO_MUSIC, "Intro Music", "Play intro music on start",
    //                    "0", () -> Logger.log(LogTag.USER_DATA, INTRO_MUSIC)),
    //
    //            new UserData(DEBUG_STATS, "Debug Windows",
    //                    "Show debug menus on startup", "0",
    //                    () -> Logger.log(LogTag.USER_DATA, DEBUG_WINDOWS)),
    //
    //            new UserData(RANDOM_BACKGROUND, "Random Background",
    //                    "Choose a random background on startup", "0",
    //                    () -> Logger.log(LogTag.USER_DATA, RANDOM_BACKGROUND)),
    //
    //            new UserData(OUTPUT_BORDER, "Output Border",
    //                    "Draw a border around the output area", "0", () -> {
    //                Logger.log(LogTag.USER_DATA, OUTPUT_BORDER);
    //
    //                if (!UserDataManager.INSTANCE.shouldDrawOutputBorder()) {
    //                    Console.INSTANCE.getOutputScroll().setBorder(BorderFactory.createEmptyBorder());
    //                } else {
    //                    LineBorder lineBorder = new LineBorder(UserDataManager.INSTANCE.getBackgroundColor(),
    //                            UserEditor.inputOutputBorderThickness, true);
    //                    Console.INSTANCE.getOutputScroll().setBorder(lineBorder);
    //                }
    //            }),
    //
    //            new UserData(INPUT_BORDER, "Input Border", "Draw a border around the input area",
    //                    "0", () -> {
    //                Logger.log(LogTag.USER_DATA, INPUT_BORDER);
    //
    //                if (!UserDataManager.INSTANCE.shouldDrawInputBorder()) {
    //                    Console.INSTANCE.getInputField().setBorder(null);
    //                } else {
    //                    Console.INSTANCE.getInputField().setBorder(new LineBorder(
    //                            UserDataManager.INSTANCE.getBackgroundColor(), 3, true));
    //                }
    //            }),
    //
    //            new UserData(HOURLY_CHIMES, "Hourly Chimes", "Chime every hour", "1",
    //                    () -> Logger.log(LogTag.USER_DATA, HOURLY_CHIMES)),
    //
    //            new UserData(SILENCE_ERRORS, "Silence Errors", "Don't open errors externally",
    //                    "1", () -> Logger.log(LogTag.USER_DATA, SILENCE_ERRORS)),
    //
    //            new UserData(FULLSCREEN, "Fullscreen",
    //                    "Fullscreen Cyder (this will also cover the Windows taskbar)", "0", () -> {
    //                Logger.log(LogTag.USER_DATA, FULLSCREEN);
    //                Console.INSTANCE.setFullscreen(UserDataManager.INSTANCE.isFullscreen());
    //            }),
    //
    //            new UserData(OUTPUT_FILL, "Output Fill",
    //                    "Fill the output area with the color specified in the \"Fonts & Colors\" panel",
    //                    "0", () -> {
    //                Logger.log(LogTag.USER_DATA, OUTPUT_FILL);
    //
    //                JTextPane outputArea = Console.INSTANCE.getOutputArea();
    //
    //                if (!UserDataManager.INSTANCE.shouldDrawOutputFill()) {
    //                    outputArea.setBackground(null);
    //                    outputArea.setOpaque(false);
    //                } else {
    //                    outputArea.setOpaque(true);
    //                    outputArea.setBackground(UserDataManager.INSTANCE.getBackgroundColor());
    //                    outputArea.repaint();
    //                    outputArea.revalidate();
    //                }
    //            }),
    //
    //            new UserData(INPUT_FILL, "Input Fill",
    //                    "Fill the input area with the color specified in the \"Fonts & Colors\" panel",
    //                    "0", () -> {
    //                Logger.log(LogTag.USER_DATA, INPUT_FILL);
    //
    //                JTextField inputField = Console.INSTANCE.getInputField();
    //
    //                if (!UserDataManager.INSTANCE.shouldDrawInputFill()) {
    //                    inputField.setBackground(null);
    //                    inputField.setOpaque(false);
    //                } else {
    //                    inputField.setOpaque(true);
    //                    inputField.setBackground(UserDataManager.INSTANCE.getBackgroundColor());
    //                    inputField.repaint();
    //                    inputField.revalidate();
    //                }
    //            }),
    //
    //            new UserData(CLOCK_ON_CONSOLE, "Clock On Console",
    //                    "Show a clock at the top of the console", "1", () -> {
    //                Logger.log(LogTag.USER_DATA, CLOCK_ON_CONSOLE);
    //                Console.INSTANCE.refreshClockText();
    //            }),
    //
    //            new UserData(SHOW_SECONDS, "Show Seconds",
    //                    "Show seconds on the console clock if enabled", "1", () -> {
    //                Logger.log(LogTag.USER_DATA, SHOW_SECONDS);
    //                Console.INSTANCE.refreshClockText();
    //            }),
    //
    //            new UserData(FILTER_CHAT, "Filter Chat", "Filter foul language", "1",
    //                    () -> Logger.log(LogTag.USER_DATA, FILTER_CHAT)),
    //
    //            new UserData(LAST_START, IGNORE, EMPTY, String.valueOf(System.currentTimeMillis()),
    //                    () -> Logger.log(LogTag.USER_DATA, LAST_START))
    //                    .setIgnoreForToggleSwitches(),
    //
    //            new UserData(MINIMIZE_ON_CLOSE, "Minimize On Close",
    //                    "Minimize the application instead of exiting whenever a close action is requested",
    //                    "0", () -> Logger.log(LogTag.USER_DATA, MINIMIZE_ON_CLOSE)),
    //
    //            new UserData(TYPING_ANIMATION, "Typing Animation",
    //                    "Typing animation on console for non-vital outputs", "1",
    //                    () -> Logger.log(LogTag.USER_DATA, TYPING_ANIMATION)),
    //
    //            new UserData(TYPING_SOUND, "Typing Animation Sound",
    //                    "Typing animation sound effect to play if typing animation is enabled",
    //                    "1", () -> Logger.log(LogTag.USER_DATA, TYPING_SOUND)),
    //
    //            new UserData(SHOW_BUSY_ICON, "Show Cyder Busy Animation",
    //                    "Show when Cyder is busy by changing the tray icon", "1",
    //                    () -> {
    //                        Console.INSTANCE.hideBusyAnimation();
    //                        Logger.log(LogTag.USER_DATA, SHOW_BUSY_ICON);
    //                    }),
    //
    //            new UserData(ROUNDED_WINDOWS, "Rounded Windows", "Make certain windows rounded",
    //                    "0", () -> {
    //                Logger.log(LogTag.USER_DATA, ROUNDED_WINDOWS);
    //                UiUtil.repaintCyderFrames();
    //            }),
    //
    //            new UserData(WINDOW_COLOR, IGNORE, EMPTY, "1A2033", () -> {
    //                Logger.log(LogTag.USER_DATA, WINDOW_COLOR);
    //
    //                UiUtil.repaintCyderFrames();
    //                Console.INSTANCE.revalidateMenuBackgrounds();
    //            }).setIgnoreForToggleSwitches(),
    //
    //            new UserData(CONSOLE_CLOCK_FORMAT, IGNORE, EMPTY, "EEEEEEEEE h:mmaa", () -> {
    //                Logger.log(LogTag.USER_DATA, CONSOLE_CLOCK_FORMAT);
    //                Console.INSTANCE.refreshClockText();
    //            }).setIgnoreForToggleSwitches(),
    //
    //            new UserData(YOUTUBE_UUID, IGNORE, EMPTY, "aaaaaaaaaaa",
    //                    () -> Logger.log(LogTag.USER_DATA, YOUTUBE_UUID))
    //                    .setIgnoreForToggleSwitches(),
    //
    //            new UserData(CAPS_MODE, "Capital Letters Mode", "Capitalize all console output",
    //                    "0", () -> Logger.log(LogTag.USER_DATA, CAPS_MODE)),
    //
    //            new UserData(LOGGED_IN, IGNORE, EMPTY, "0",
    //                    () -> Logger.log(LogTag.USER_DATA, LOGGED_IN))
    //                    .setIgnoreForToggleSwitches(),
    //
    //            new UserData(AUDIO_LENGTH, "Show Audio Total Length",
    //                    "For the audio player, show the total audio time instead of the time remaining",
    //                    "1", () -> Logger.log(LogTag.USER_DATA, AUDIO_LENGTH)),
    //
    //            new UserData(PERSISTENT_NOTIFICATIONS, "Persistent Notifications",
    //                    "Notifications stay on screen until manually dismissed", "0",
    //                    () -> Logger.log(LogTag.USER_DATA, PERSISTENT_NOTIFICATIONS)),
    //
    //            new UserData(DO_ANIMATIONS, "Do Animations",
    //                    "Use animations for things such as frame movement and notifications", "1",
    //                    () -> Logger.log(LogTag.USER_DATA, DO_ANIMATIONS)),
    //
    //            new UserData(COMPACT_TEXT_MODE, "Compact Text",
    //                    "Compact the text/components in supported text panes", "0", () -> {
    //                Logger.log(LogTag.USER_DATA, COMPACT_TEXT_MODE);
    //
    //                Console.INSTANCE.revalidateConsoleTaskbarMenu();
    //                CyderScrollList.refreshAllLists();
    //            }),
    //
    //            new UserData(FONT_SIZE, IGNORE, EMPTY, "30", () -> {
    //                Logger.log(LogTag.USER_DATA, FONT_SIZE);
    //
    //                Console.INSTANCE.getInputField().setFont(Console.INSTANCE.generateUserFont());
    //                Console.INSTANCE.getOutputArea().setFont(Console.INSTANCE.generateUserFont());
    //            }).setIgnoreForToggleSwitches(),
    //
    //            new UserData(WRAP_SHELL, "Wrap Shell", "Wrap the native shell by"
    //                    + " passing unrecognized commands to it and allowing it to process them", "0",
    //                    () -> Logger.log(LogTag.USER_DATA, WRAP_SHELL)),
    //
    //            new UserData(DARK_MODE, "Dark Mode", "Activate a pleasant dark mode for Cyder",
    //                    "0", () -> Logger.log(LogTag.USER_DATA, DARK_MODE)),
    //
    //            new UserData(WEATHER_MAP, "Weather Map",
    //                    "Show a map of the location's area in the weather widget background", "1",
    //                    () -> {
    //                        Logger.log(LogTag.USER_DATA, WEATHER_MAP);
    //                        WeatherWidget.refreshAllMapBackgrounds();
    //                    }),
    //
    //            new UserData(PAINT_CLOCK_LABELS, "Paint Clock Labels",
    //                    "Whether to paint the hour labels on the clock widget", "1",
    //                    () -> {
    //                        Logger.log(LogTag.USER_DATA, PAINT_CLOCK_LABELS);
    //                        ClockWidget.setPaintHourLabels(UserDataManager.INSTANCE.shouldPaintClockHourLabels());
    //                    }),
    //
    //            new UserData(SHOW_SECOND_HAND, "Show Second Hand",
    //                    "Whether to show the second hand on the clock widget", "1",
    //                    () -> {
    //                        Logger.log(LogTag.USER_DATA, SHOW_SECOND_HAND);
    //                        ClockWidget.setShowSecondHand(UserDataManager.INSTANCE.shouldShowClockWidgetSecondHand());
    //                    }),
    //
    //            new UserData(FILL_OPCACIY, "Fill Color Opacity",
    //                    "The opacity value to use for the output and input fill colors", "255",
    //                    () -> {
    //                        Logger.log(LogTag.USER_DATA, FILL_OPCACIY);
    //                        // todo change things that use opacity
    //                    })

            /*
                To add: create object in User.java with a getter/setter and add a new Preference here

                Everything in userdata must be in this list in some way, perhaps this hints at
                this class being called something different

                Note: non primitive types such as ScreenStat need to be set by using the object reference
                returned by the getter, UserUtil.getCyderUser().getScreenStat().setX(0);
             */
    //);

    /**
     * Returns the user data collection.
     *
     * @return the preferences collection
     */
    public static ImmutableList<UserData<?>> getPreferences() {
        // todo return userDatas;
        return null;
    }

    /**
     * Invokes the onChangeFunction() of the preference with the provided ID, if found.
     *
     * @param preferenceID the onChangeFunction() of the preference with the provided ID
     */
    public static void invokeRefresh(String preferenceID) {
        for (UserData<?> userData : userDatas) {
            if (userData.getID().equals(preferenceID)) {
                userData.getOnChangeFunction().run();
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
    public static UserData<?> get(String preferenceID) {
        Preconditions.checkNotNull(preferenceID);
        Preconditions.checkArgument(!preferenceID.isEmpty());

        for (UserData<?> userData : userDatas) {
            if (userData.getID().equals(preferenceID)) {
                return userData;
            }
        }

        throw new IllegalArgumentException("Preference with id not found: " + preferenceID);
    }

    /**
     * The id of the preference.
     */
    private final String id;

    /**
     * The type this user data is of.
     */
    private final Class<T> type;

    /**
     * The name to display for the preference when allowing the user to make changes.
     */
    private String displayName;

    /**
     * The description for this data if user editing is allowed.
     */
    private String description;

    /**
     * The default value for the preference.
     */
    private T defaultValue;

    /**
     * The method to run when a change of the preference occurs.
     */
    private Runnable onChangeFunction;

    /**
     * Whether this preference should be ignored when creating the user preference toggle switches.
     */
    private boolean ignoreForToggleSwitches;

    /**
     * Constructs a user data object.
     *
     * @param builder the builder to construct  the object from
     */
    private UserData(Builder<T> builder) {
        Preconditions.checkNotNull(builder);

        this.id = builder.id;
        this.type = builder.type;

        this.displayName = builder.dislayName;
        this.description = builder.description;
        this.defaultValue = builder.defaultValue;
        this.onChangeFunction = builder.onChangeFunction;
        this.ignoreForToggleSwitches = builder.ignoreForToggleSwitches;

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the id of the user data.
     *
     * @return the id of the user data
     */
    public String getID() {
        return id;
    }

    /**
     * Returns the type of this user data.
     *
     * @return the type of this user data
     */
    public Class<T> getType() {
        return type;
    }

    // todo optional parameters

    /**
     * A builder for a {@link UserData} object
     */
    private static final class Builder<T> {
        /**
         * The id of the user data.
         */
        private final String id;

        /**
         * The type of the data
         */
        private final Class<T> type;

        /**
         * The name to display for the data when being edited.
         */
        private String dislayName;

        /**
         * The description of the user data.
         */
        private String description;

        /**
         * The default value of the user data.
         */
        private T defaultValue;

        /**
         * The runnable to invoke when a change of this value occurs.
         */
        private Runnable onChangeFunction;

        /**
         * Whether to ignore this data when constructing the toggle switches user editor page.
         */
        private boolean ignoreForToggleSwitches;

        /**
         * Constructs a new Builder for a UserData.
         *
         * @param id   id of the user data
         * @param type the type of the data
         */
        public Builder(String id, Class<T> type) {
            this.id = id;
            this.type = type;
        }

        /**
         * Returns the id of this user data.
         *
         * @return the id of this user data
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the type of this user data.
         *
         * @return the type of this user data
         */
        public Class<T> getType() {
            return type;
        }

        /**
         * Returns the display name of this user data.
         *
         * @return the display name of this user data
         */
        public String getDislayName() {
            return dislayName;
        }

        /**
         * Sets the display name of this user data.
         *
         * @param dislayName the display name of this user data
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder<T> setDislayName(String dislayName) {
            this.dislayName = dislayName;
            return this;
        }

        /**
         * Returns the description of this user data.
         *
         * @return the description of this user data
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the description of this user data.
         *
         * @param description the description of this user data
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder<T> setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Returns the default value of this user data.
         *
         * @return the default value of this user data
         */
        public T getDefaultValue() {
            return defaultValue;
        }

        /**
         * Sets the default value of this user data.
         *
         * @param defaultValue the default value of this user data
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder<T> setDefaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Returns the on change function of this builder.
         *
         * @return the on change function of this builder
         */
        public Runnable getOnChangeFunction() {
            return onChangeFunction;
        }

        /**
         * Sets the on change function of this builder.
         *
         * @param onChangeFunction the on change function of this builder
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder<T> setOnChangeFunction(Runnable onChangeFunction) {
            this.onChangeFunction = onChangeFunction;
            return this;
        }

        /**
         * Returns whether to ignore this data when constructing the toggle switches user editor page.
         *
         * @return whether to ignore this data when constructing the toggle switches user editor page
         */
        public boolean shouldIgnoreForToggleSwitches() {
            return ignoreForToggleSwitches;
        }

        /**
         * Sets whether to ignore this data when constructing the toggle switches user editor page.
         *
         * @param ignoreForToggleSwitches whether to ignore this data when
         *                                constructing the toggle switches user editor page
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder<T> setIgnoreForToggleSwitches(boolean ignoreForToggleSwitches) {
            this.ignoreForToggleSwitches = ignoreForToggleSwitches;
            return this;
        }

        public UserData<T> build() {
            return new UserData<>(this);
        }
    }
}