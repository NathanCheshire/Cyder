package cyder.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.exceptions.FatalException;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.UiUtil;
import cyder.ui.pane.CyderScrollList;
import cyder.user.data.ScreenStat;
import cyder.weather.WeatherWidget;
import cyder.widgets.ClockWidget;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Optional;

/**
 * UserData class used to hold user data default values and anyother meta types associated.
 */
@SuppressWarnings("SpellCheckingInspection") /* Key names */
public final class UserData<T> {
    /*
    UserData keys.
     */
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String FONT_NAME = "font_name";
    public static final String FONT_SIZE = "font_size";
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
    public static final String FILL_OPACITY = "fill_opacity";
    public static final String SHOWN_WELCOME_MESSAGE = "shown_welcome_message";
    public static final String ACCOUNT_CREATION_TIME = "account_creation_time";

    // todo object does not exist for
    public static final String MAPPED_EXECUTABLES = "mapped_executables";

    // todo only booleans will be pulled for toggle switches unless they have ignore for toggle switches enabled

    public static final UserData<String> username = new Builder<>(USERNAME, String.class)
            .setDescription("The user's public username")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, USERNAME);
                Console.INSTANCE.refreshConsoleSuperTitle();
            }).build();

    public static final UserData<String> password = new Builder<>(PASSWORD, String.class)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, PASSWORD);
                // todo log out user
            }).build();

    public static final UserData<String> fontName = new Builder<>(FONT_NAME, String.class)
            .setDescription("The name of the font for the input and output fields")
            .setDefaultValue(CyderFonts.AGENCY_FB)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FONT_NAME);
                // todo refresh output and input fields.
            }).build();

    public static final UserData<Integer> fontSize = new Builder<>(FONT_SIZE, Integer.class)
            .setDescription("The size of the user font")
            .setOnChangeFunction(() -> {
                // todo
            }).build();

    public static final UserData<Color> foregroundColor = new Builder<>(FOREGROUND_COLOR, Color.class)
            .setDescription("The text color for the input and output fields")
            .setDefaultValue(CyderColors.navy)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FOREGROUND_COLOR);
                Color foregroundColor = UserDataManager.INSTANCE.getForegroundColor();

                Console.INSTANCE.getInputField().setForeground(foregroundColor);
                Console.INSTANCE.getOutputArea().setForeground(foregroundColor);
                Console.INSTANCE.getOutputScroll().setForeground(foregroundColor);
            }).build();

    public static final UserData<Color> backgroundColor = new Builder<>(BACKGROUND_COLOR, Color.class)
            .setDescription("The color for the output/input field borders and fills")
            .setDefaultValue(Color.black)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, BACKGROUND_COLOR);
                Color backgroundColor = UserDataManager.INSTANCE.getBackgroundColor();
                // todo refresh with backgroundColor
            }).build();

    public static final UserData<Boolean> introMusic = new Builder<>(INTRO_MUSIC, Boolean.class)
            .setDescription("Whether to play intro music on user login")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, INTRO_MUSIC)).build();

    public static final UserData<Boolean> debugStats = new Builder<>(DEBUG_STATS, Boolean.class)
            .setDescription("Whether to show debug windows on initial console load")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, DEBUG_STATS)).build();

    public static final UserData<Boolean> randomBackground = new Builder<>(RANDOM_BACKGROUND, Boolean.class)
            .setDescription("Whether to choose a random background for the console on console load")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, RANDOM_BACKGROUND)).build();

    public static final UserData<Boolean> outputBorder = new Builder<>(OUTPUT_BORDER, Boolean.class)
            .setDescription("Whether to draw a border around the output field")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, OUTPUT_BORDER);

                if (!UserDataManager.INSTANCE.shouldDrawOutputBorder()) {
                    Console.INSTANCE.getOutputScroll().setBorder(BorderFactory.createEmptyBorder());
                } else {
                    LineBorder lineBorder = new LineBorder(UserDataManager.INSTANCE.getBackgroundColor(),
                            UserEditor.inputOutputBorderThickness, true);
                    Console.INSTANCE.getOutputScroll().setBorder(lineBorder);
                }
            }).build();

    public static final UserData<Boolean> inputBorder = new Builder<>(INPUT_BORDER, Boolean.class)
            .setDescription("Whether to draw a border around the input field")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, INPUT_BORDER);

                if (!UserDataManager.INSTANCE.shouldDrawInputBorder()) {
                    Console.INSTANCE.getInputField().setBorder(null);
                } else {
                    Console.INSTANCE.getInputField().setBorder(new LineBorder(
                            UserDataManager.INSTANCE.getBackgroundColor(), 3, true));
                }
            }).build();

    public static final UserData<Boolean> hourlyChimes = new Builder<>(HOURLY_CHIMES, Boolean.class)
            .setDescription("Whether to play chime sounds on the turning of the hour")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, HOURLY_CHIMES)).build();

    public static final UserData<Boolean> silenceErrors = new Builder<>(SILENCE_ERRORS, Boolean.class)
            .setDescription("Whether to silence error notifications")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, SILENCE_ERRORS);
                // todo if error panes are present, remove
            }).build();

    public static final UserData<Boolean> fullscreen = new Builder<>(FULLSCREEN, Boolean.class)
            .setDescription("Whether the program shoul be in fullscreen mode")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FULLSCREEN);
                Console.INSTANCE.setFullscreen(UserDataManager.INSTANCE.isFullscreen());
            }).build();

    public static final UserData<Boolean> outputFill = new Builder<>(OUTPUT_FILL, Boolean.class)
            .setDescription("Whether the output area should be filled")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, OUTPUT_FILL);

                JTextPane outputArea = Console.INSTANCE.getOutputArea();

                if (!UserDataManager.INSTANCE.shouldDrawOutputFill()) {
                    outputArea.setBackground(null);
                    outputArea.setOpaque(false);
                } else {
                    outputArea.setOpaque(true);
                    outputArea.setBackground(UserDataManager.INSTANCE.getBackgroundColor());
                    outputArea.repaint();
                    outputArea.revalidate();
                }
            }).build();

    public static final UserData<Boolean> inputFill = new Builder<>(INPUT_FILL, Boolean.class)
            .setDescription("Whether the input field should be filled")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, INPUT_FILL);

                JTextField inputField = Console.INSTANCE.getInputField();

                if (!UserDataManager.INSTANCE.shouldDrawInputFill()) {
                    inputField.setBackground(null);
                    inputField.setOpaque(false);
                } else {
                    inputField.setOpaque(true);
                    inputField.setBackground(UserDataManager.INSTANCE.getBackgroundColor());
                    inputField.repaint();
                    inputField.revalidate();
                }
            }).build();

    public static final UserData<Boolean> consoleClock = new Builder<>(CONSOLE_CLOCK, Boolean.class)
            .setDescription("Whether to show a clock on the console")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, CONSOLE_CLOCK);
                Console.INSTANCE.refreshClockText();
            }).build();

    public static final UserData<Boolean> consoleClockSeconds = new Builder<>(CONSOLE_CLOCK_SECONDS, Boolean.class)
            .setDescription("Whether to show seconds on the console clock")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, CONSOLE_CLOCK_SECONDS);
                Console.INSTANCE.refreshClockText();
            }).build();

    public static final UserData<Boolean> filterChat = new Builder<>(FILTER_CHAT, Boolean.class)
            .setDescription("Whether the user input should be filtered")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, FILTER_CHAT)).build();

    public static final UserData<Long> lastSessionStart = new Builder<>(LAST_SESSION_START, Long.class)
            .setDescription("The time at which the last session for this user was started")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, LAST_SESSION_START)).build();

    public static final UserData<Boolean> minimizeOnClose = new Builder<>(MINIMIZE_ON_CLOSE, Boolean.class)
            .setDescription("Whether the cnosole should be minimized instead"
                    + " of closed when the closed button is pressed")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, MINIMIZE_ON_CLOSE)).build();

    public static final UserData<Boolean> typingAnimation = new Builder<>(TYPING_ANIMATION, Boolean.class)
            .setDescription("Whether to show a typing animation for the console")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, TYPING_ANIMATION)).build();

    public static final UserData<Boolean> typingSound = new Builder<>(TYPING_SOUND, Boolean.class)
            .setDescription("Whether to play a typing sound effect when the typing animation is enabled")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, TYPING_SOUND)).build();

    public static final UserData<Boolean> busyAnimation = new Builder<>(BUSY_ANIMATION, Boolean.class)
            .setDescription("Whether to show a busy animation")
            .setOnChangeFunction(() -> {
                Console.INSTANCE.hideBusyAnimation();
                Logger.log(LogTag.USER_DATA, BUSY_ANIMATION);
            }).build();

    public static final UserData<Boolean> roundedWindows = new Builder<>(ROUNDED_FRAME_BORDERS, Boolean.class)
            .setDescription("Whether to paint frames with rounded corners")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, ROUNDED_FRAME_BORDERS);
                UiUtil.repaintCyderFrames();
            }).build();

    public static final UserData<Color> frameColor = new Builder<>(FRAME_COLOR, Color.class)
            .setDescription("The color for frame borders")
            .setDefaultValue(CyderColors.navy)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FRAME_COLOR);

                UiUtil.repaintCyderFrames();
                Console.INSTANCE.revalidateMenuBackgrounds();
            }).build();

    public static final UserData<String> clockFormat = new Builder<>(CLOCK_FORMAT, String.class)
            .setDescription("The date pattern for the console clock")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, CLOCK_FORMAT);
                Console.INSTANCE.refreshClockText();
            }).build();

    public static final UserData<String> youtubeUuid = new Builder<>(YOUTUBE_UUID, String.class)
            .setDescription("The uuid this user is at for YouTube UUID generation")
            .setDefaultValue("aaaaaaaaaaa")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, YOUTUBE_UUID)).build();

    public static final UserData<Boolean> capsMode = new Builder<>(CAPS_MODE, Boolean.class)
            .setDescription("Whether conosle output should be appended in capital letters")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, CAPS_MODE)).build();

    public static final UserData<Boolean> loggedIn = new Builder<>(LOGGED_IN, Boolean.class)
            .setDescription("Whether this user is currently logged in")
            .setIgnoreForToggleSwitches(true)
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, LOGGED_IN)).build();

    public static final UserData<Boolean> audioTotalLength = new Builder<>(AUDIO_TOTAL_LENGTH, Boolean.class)
            .setDescription("Whether the audio total length should be shown instead"
                    + " of the time remaining for the audio player")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, AUDIO_TOTAL_LENGTH)).build();

    public static final UserData<Boolean> shouldPersistNotifications =
            new Builder<>(SHOULD_PERSIST_NOTIFICATIONS, Boolean.class)
                    .setDescription("Whether notifications should be persited until manually dismissed")
                    .setOnChangeFunction(() -> {
                        Logger.log(LogTag.USER_DATA, SHOULD_PERSIST_NOTIFICATIONS);
                        // todo hook to remove persisting notifications
                    }).build();

    public static final UserData<Boolean> shouldDoAnimations = new Builder<>(SHOULD_DO_ANIMATIONS, Boolean.class)
            .setDescription("Whether certain animations shoudl be performed")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, SHOULD_DO_ANIMATIONS)).build();

    public static final UserData<Boolean> compactText = new Builder<>(COMPACT_TEXT_MODE, Boolean.class)
            .setDescription("Whether compact text mode is enabled")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, COMPACT_TEXT_MODE);

                Console.INSTANCE.revalidateConsoleTaskbarMenu();
                CyderScrollList.refreshAllLists();
            }).build();

    public static final UserData<Boolean> wrapShell = new Builder<>(WRAP_SHELL, Boolean.class)
            .setDescription("Whether unrecognized user commands should be pased to the native shell")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, WRAP_SHELL)).build();

    public static final UserData<Boolean> drawWeatherMap = new Builder<>(DRAW_WEATHER_MAP, Boolean.class)
            .setDescription("Whether a map should be drawn on the background of the weather widget")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, DRAW_WEATHER_MAP);
                WeatherWidget.refreshAllMapBackgrounds();
            }).build();

    public static final UserData<Boolean> paintClocklabels = new Builder<>(PAINT_CLOCK_LABELS, Boolean.class)
            .setDescription("Whether the hour labels should be painted for the clock widget")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, PAINT_CLOCK_LABELS);
                ClockWidget.setPaintHourLabels(UserDataManager.INSTANCE.shouldPaintClockHourLabels());
            }).build();

    public static final UserData<Boolean> clockWidgetSecondHand =
            new Builder<>(CLOCK_WIDGET_SECOND_HAND, Boolean.class)
                    .setDescription("Whether the second hand should be shown for the clock widget")
                    .setOnChangeFunction(() -> {
                        Logger.log(LogTag.USER_DATA, CLOCK_WIDGET_SECOND_HAND);
                        ClockWidget.setShowSecondHand(UserDataManager.INSTANCE.shouldShowClockWidgetSecondHand());
                    }).build();

    public static final UserData<Integer> fillOpacity = new Builder<>(FILL_OPACITY, Integer.class)
            .setDescription("The opacity of the input and output fills")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FILL_OPACITY);
                // todo change things that use opacity
            }).build();

    public static final UserData<ScreenStat> screenStat = new Builder<>(SCREEN_STAT, ScreenStat.class)
            .setDescription("The user's screen stats")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, SCREEN_STAT)).build();

    public static final UserData<Boolean> shownWelcomeMessage = new Builder<>(SHOWN_WELCOME_MESSAGE, Boolean.class)
            .setDescription("Whether the welcome message has been shown for this user")
            .setIgnoreForToggleSwitches(true)
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, SHOWN_WELCOME_MESSAGE)).build();

    public static final UserData<Long> accountCreationTime = new Builder<>(ACCOUNT_CREATION_TIME, Long.class)
            .setDescription("The time at which this account was created")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, ACCOUNT_CREATION_TIME)).build();

    /**
     * The collection of {@link UserData} pieces.
     */
    public static final ImmutableList<UserData<?>> datas = ImmutableList.of(
            username,
            password,
            fontName,
            fontSize,
            foregroundColor,
            backgroundColor,
            introMusic,
            debugStats,
            randomBackground,
            outputBorder,
            inputBorder,
            hourlyChimes,
            silenceErrors,
            fullscreen,
            outputFill,
            inputFill,
            consoleClock,
            consoleClockSeconds,
            filterChat,
            lastSessionStart,
            minimizeOnClose,
            typingAnimation,
            typingSound,
            busyAnimation,
            roundedWindows,
            frameColor,
            clockFormat,
            youtubeUuid,
            capsMode,
            loggedIn,
            audioTotalLength,
            shouldPersistNotifications,
            shouldDoAnimations,
            compactText,
            wrapShell,
            drawWeatherMap,
            paintClocklabels,
            clockWidgetSecondHand,
            fillOpacity,
            screenStat,
            shownWelcomeMessage,
            accountCreationTime
    );

    /**
     * Returns the user data collection.
     *
     * @return the user data collection
     */
    public static ImmutableList<UserData<?>> getUserDatas() {
        return datas;
    }

    /**
     * Invokes the refresh function of the user data with the provided ID, if found.
     *
     * @param userDataId the id of the user data
     */
    public static void invokeRefresh(String userDataId) {
        for (UserData<?> userData : datas) {
            if (userData.getId().equals(userDataId)) {
                Optional<Runnable> optionalRunnable = userData.getOnChangeRunnable();
                optionalRunnable.ifPresent(Runnable::run);

                onUserDataRefresh();
                return;
            }
        }

        throw new FatalException("Failed to invoke user data refresh, failed to find id: " + userDataId);
    }

    /**
     * A hook to be ran after all user data on change function invocations.
     */
    private static void onUserDataRefresh() {
        UserEditor.revalidateCheckboxesIfOpen();
    }

    /**
     * Returns the user data with the provided id.
     *
     * @param userDataId the provided id to get
     * @return the user data with the provided id
     * @throws IllegalArgumentException if a user data with the provided id cannot be found
     */
    public static UserData<?> get(String userDataId) {
        Preconditions.checkNotNull(userDataId);
        Preconditions.checkArgument(!userDataId.isEmpty());

        for (UserData<?> userData : datas) {
            if (userData.getId().equals(userDataId)) {
                return userData;
            }
        }

        throw new IllegalArgumentException("User data with id not found: " + userDataId);
    }

    /**
     * The id of the user data.
     */
    private final String id;

    /**
     * The type this user data is of.
     */
    private final Class<T> type;

    /**
     * The name to display for the user data when allowing the user to make changes.
     */
    private final String displayName;

    /**
     * The description for this data if user editing is allowed.
     */
    private final String description;

    /**
     * The default value for the user data.
     */
    private final T defaultValue;

    /**
     * The method to run when a change of the user data occurs.
     */
    private final Runnable onChangeFunction;

    /**
     * Whether this user data should be ignored when creating the user data toggle switches.
     */
    private final boolean ignoreForToggleSwitches;

    /**
     * Constructs a user data object.
     *
     * @param builder the builder to construct  the object from
     */
    private UserData(Builder<T> builder) {
        Preconditions.checkNotNull(builder);

        // Required
        this.id = builder.id;
        this.type = builder.type;

        // Optional
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
     * Returns the display name if present. Empty optional else.
     *
     * @return the display name if present. Empty optional else
     */
    public Optional<String> getDisplayName() {
        return displayName != null ? Optional.of(displayName) : Optional.empty();
    }

    /**
     * Returns the description if present. Empty optional else.
     *
     * @return the description if present. Empty optional else
     */
    public Optional<String> getDescription() {
        return description != null ? Optional.of(description) : Optional.empty();
    }

    /**
     * Returns the default value if present. Empty optional else.
     *
     * @return the default value if present. Empty optional else
     */
    public Optional<T> getDefaultValue() {
        return defaultValue != null ? Optional.of(defaultValue) : Optional.empty();
    }

    /**
     * Returns the on change runnable if present. Empty optional else.
     *
     * @return the on change runnable if present. Empty optional else
     */
    public Optional<Runnable> getOnChangeRunnable() {
        return onChangeFunction != null ? Optional.of(onChangeFunction) : Optional.empty();
    }

    /**
     * Returns whether this user data should be ignored for toggle switches.
     *
     * @return whether this user data should be ignored for toggle switches
     */
    public boolean shouldIgnoreForToggleSwitches() {
        return ignoreForToggleSwitches || type != Boolean.class; // todo test
    }

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