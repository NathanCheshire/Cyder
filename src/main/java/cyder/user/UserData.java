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
import cyder.ui.field.CyderCaret;
import cyder.ui.list.CyderScrollList;
import cyder.user.data.MappedExecutables;
import cyder.user.data.ScreenStat;
import cyder.utils.ColorUtil;
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
    public static final String FONT_NAME = "fontName";
    public static final String FONT_SIZE = "fontSize";
    public static final String FOREGROUND_COLOR = "foregroundColor";
    public static final String BACKGROUND_COLOR = "backgroundColor";
    public static final String INTRO_MUSIC = "introMusic";
    public static final String DEBUG_STATS = "debugStats";
    public static final String RANDOM_BACKGROUND_ON_START = "randomBackgroundOnStart";
    public static final String DRAW_INPUT_BORDER = "drawInputBorder";
    public static final String DRAW_OUTPUT_BORDER = "drawOutputBorder";
    public static final String PLAY_HOURLY_CHIMES = "playHourlyChimes";
    public static final String SILENCE_ERRORS = "silenceErrors";
    public static final String FULLSCREEN = "fullscreen";
    public static final String DRAW_OUTPUT_FILL = "drawOutputFill";
    public static final String DRAW_INPUT_FILL = "drawInputFill";
    public static final String DRAW_CONSOLE_CLOCK = "drawConsoleClock";
    public static final String SHOW_CONSOLE_CLOCK_SECONDS = "showConsoleClockSeconds";
    public static final String FILTER_CHAT = "filterChat";
    public static final String LAST_SESSION_START = "lastSessionStart";
    public static final String MINIMIZE_ON_CLOSE = "minimizeOnClose";
    public static final String TYPING_ANIMATION = "typingAnimation";
    public static final String SHOW_BUSY_ANIMATION = "showBusyAnimation";
    public static final String ROUNDED_FRAME_BORDERS = "roundedFrameBorders";
    public static final String FRAME_COLOR = "frameColor";
    public static final String CONSOLE_CLOCK_FORMAT = "consoleClockFormat";
    public static final String PLAY_TYPING_SOUND = "playTypingSound";
    public static final String YOUTUBE_UUID = "youtubeUuid";
    public static final String CAPS_MODE = "capsMode";
    public static final String LOGGED_IN = "loggedIn";
    public static final String SHOW_AUDIO_TOTAL_LENGTH = "showAudioTotalLength";
    public static final String SHOULD_PERSIST_NOTIFICATIONS = "persistNotifications";
    public static final String DO_ANIMATIONS = "doAnimations";
    public static final String COMPACT_TEXT_MODE = "compactTextMode";
    public static final String WRAP_NATIVE_SHELL = "wrapNativeShell";
    public static final String DRAW_WEATHER_MAP = "drawWeatherMap";
    public static final String PAINT_CLOCK_WIDGET_HOUR_LABELS = "paintClockWidgetHourLabels";
    public static final String SHOW_CLOCK_WIDGET_SECOND_HAND = "showClockWidgetSecondHand";
    public static final String SCREEN_STAT = "screenStat";
    public static final String FILL_OPACITY = "fillOpacity";
    public static final String SHOWN_WELCOME_MESSAGE = "shownWelcomeMessage";
    public static final String ACCOUNT_CREATION_TIME = "accountCreationTime";
    public static final String MAPPED_EXECUTABLES = "mappedExecutables";
    public static final String AUDIO_PLAYER_VOLUME_PERCENT = "audioPlayerVolumePercent";

    /**
     * The thickness of the border for the input and output areas if enabled.
     */
    private static final int inputOutputBorderThickness = 3;

    /**
     * The username data piece.
     */
    public static final UserData<String> username = new Builder<>(USERNAME, String.class)
            .setDescription("The user's public username")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, USERNAME);
                Console.INSTANCE.refreshConsoleSuperTitle();
            }).build();

    /**
     * The password data piece.
     */
    public static final UserData<String> password = new Builder<>(PASSWORD, String.class)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, PASSWORD);
                // todo log out user and show login screen?
            }).build();

    /**
     * The font name data piece.
     */
    public static final UserData<String> fontName = new Builder<>(FONT_NAME, String.class)
            .setDescription("The name of the font for the input and output fields")
            .setDefaultValue(CyderFonts.AGENCY_FB)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FONT_NAME);

                Font applyFont = Console.INSTANCE.generateUserFont();
                Console.INSTANCE.getOutputArea().setFont(applyFont);
                Console.INSTANCE.getInputField().setFont(applyFont);
                Console.INSTANCE.getInputHandler().refreshPrintedLabels();
            }).build();

    /**
     * The font size data piece.
     */
    public static final UserData<Integer> fontSize = new Builder<>(FONT_SIZE, Integer.class)
            .setDescription("The size of the user font")
            .setDefaultValue(User.DEFAULT_FONT_SIZE)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FONT_SIZE);

                Font applyFont = Console.INSTANCE.generateUserFont();
                Console.INSTANCE.getOutputArea().setFont(applyFont);
                Console.INSTANCE.getInputField().setFont(applyFont);
                Console.INSTANCE.getInputHandler().refreshPrintedLabels();
            }).build();

    /**
     * The foreground color data piece.
     */
    public static final UserData<Color> foregroundColor = new Builder<>(FOREGROUND_COLOR, Color.class)
            .setDescription("The text color for the input and output fields")
            .setDefaultValue(CyderColors.navy)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FOREGROUND_COLOR);
                Color foregroundColor = UserDataManager.INSTANCE.getForegroundColor();

                Console.INSTANCE.getOutputArea().setForeground(foregroundColor);
                Console.INSTANCE.getInputField().setForeground(foregroundColor);
                Console.INSTANCE.getOutputScroll().setForeground(foregroundColor);
                Console.INSTANCE.getInputField().setCaret(new CyderCaret(foregroundColor));
                Console.INSTANCE.getInputHandler().refreshPrintedLabels();
            }).build();

    /**
     * The background color data piece.
     */
    public static final UserData<Color> backgroundColor = new Builder<>(BACKGROUND_COLOR, Color.class)
            .setDescription("The color for the output/input field borders and fills")
            .setDefaultValue(Color.black)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, BACKGROUND_COLOR);
                Color backgroundColor = UserDataManager.INSTANCE.getBackgroundColor();

                if (UserDataManager.INSTANCE.shouldDrawOutputFill()) {
                    Console.INSTANCE.getOutputArea().setOpaque(true);
                    Console.INSTANCE.getOutputArea().setBackground(backgroundColor);
                    Console.INSTANCE.getOutputArea().repaint();
                    Console.INSTANCE.getOutputArea().revalidate();
                }
                if (UserDataManager.INSTANCE.shouldDrawInputFill()) {
                    Console.INSTANCE.getInputField().setOpaque(true);
                    Console.INSTANCE.getInputField().setBackground(backgroundColor);
                    Console.INSTANCE.getInputField().repaint();
                    Console.INSTANCE.getInputField().revalidate();
                }

                LineBorder inputOutputBorder = new LineBorder(backgroundColor,
                        inputOutputBorderThickness, false);
                if (UserDataManager.INSTANCE.shouldDrawOutputBorder()) {
                    Console.INSTANCE.getOutputScroll().setBorder(inputOutputBorder);
                    Console.INSTANCE.getOutputScroll().repaint();
                    Console.INSTANCE.getOutputScroll().revalidate();
                }
                if (UserDataManager.INSTANCE.shouldDrawInputBorder()) {
                    Console.INSTANCE.getInputField().setBorder(inputOutputBorder);
                    Console.INSTANCE.getInputField().repaint();
                    Console.INSTANCE.getInputField().revalidate();
                }
            }).build();

    /**
     * The intro music data piece.
     */
    public static final UserData<Boolean> introMusic = new Builder<>(INTRO_MUSIC, Boolean.class)
            .setDescription("Whether to play intro music on user login")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, INTRO_MUSIC)).build();

    /**
     * The debug stats data piece.
     */
    public static final UserData<Boolean> debugStats = new Builder<>(DEBUG_STATS, Boolean.class)
            .setDescription("Whether to show debug windows on initial console load")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, DEBUG_STATS)).build();

    /**
     * The random background on start data piece.
     */
    public static final UserData<Boolean> randomBackgroundOnStart =
            new Builder<>(RANDOM_BACKGROUND_ON_START, Boolean.class)
                    .setDescription("Whether to choose a random background for the console on console load")
                    .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, RANDOM_BACKGROUND_ON_START)).build();

    /**
     * The draw output border data piece.
     */
    public static final UserData<Boolean> drawOutputBorder = new Builder<>(DRAW_OUTPUT_BORDER, Boolean.class)
            .setDescription("Whether to draw a border around the output field")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, DRAW_OUTPUT_BORDER);

                if (!UserDataManager.INSTANCE.shouldDrawOutputBorder()) {
                    Console.INSTANCE.getOutputScroll().setBorder(BorderFactory.createEmptyBorder());
                } else {
                    LineBorder lineBorder = new LineBorder(UserDataManager.INSTANCE.getBackgroundColor(),
                            inputOutputBorderThickness, true);
                    Console.INSTANCE.getOutputScroll().setBorder(lineBorder);
                }
            }).build();

    /**
     * The draw input border data piece.
     */
    public static final UserData<Boolean> drawInputBorder = new Builder<>(DRAW_INPUT_BORDER, Boolean.class)
            .setDescription("Whether to draw a border around the input field")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, DRAW_INPUT_BORDER);

                if (!UserDataManager.INSTANCE.shouldDrawInputBorder()) {
                    Console.INSTANCE.getInputField().setBorder(null);
                } else {
                    Console.INSTANCE.getInputField().setBorder(new LineBorder(
                            UserDataManager.INSTANCE.getBackgroundColor(), 3, true));
                }
            }).build();

    /**
     * The play hourly chimes data piece.
     */
    public static final UserData<Boolean> playHourlyChimes = new Builder<>(PLAY_HOURLY_CHIMES, Boolean.class)
            .setDescription("Whether to play chime sounds on the turning of the hour")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, PLAY_HOURLY_CHIMES)).build();

    /**
     * The silence errors data piece.
     */
    public static final UserData<Boolean> silenceErrors = new Builder<>(SILENCE_ERRORS, Boolean.class)
            .setDescription("Whether to silence error notifications")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, SILENCE_ERRORS);
                // todo if error panes are present, remove
                // todo this implies we need some kind of a manager, should also have errors stck up the side
                //  with padding and if more than like half of screen height, just say like "2 more..."
            }).build();

    /**
     * The fullscreen data piece.
     */
    public static final UserData<Boolean> fullscreen = new Builder<>(FULLSCREEN, Boolean.class)
            .setDescription("Whether the program shoul be in fullscreen mode")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FULLSCREEN);
                Console.INSTANCE.setFullscreen(UserDataManager.INSTANCE.isFullscreen());
            }).build();

    /**
     * The draw output fill data piece.
     */
    public static final UserData<Boolean> drawOutputFill = new Builder<>(DRAW_OUTPUT_FILL, Boolean.class)
            .setDescription("Whether the output area should be filled")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, DRAW_OUTPUT_FILL);

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

    /**
     * The draw input fill data piece.
     */
    public static final UserData<Boolean> drawInputFill = new Builder<>(DRAW_INPUT_FILL, Boolean.class)
            .setDescription("Whether the input field should be filled")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, DRAW_INPUT_FILL);

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

    /**
     * The draw console clock data piece..
     */
    public static final UserData<Boolean> drawConsoleClock = new Builder<>(DRAW_CONSOLE_CLOCK, Boolean.class)
            .setDescription("Whether to show a clock on the console")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, DRAW_CONSOLE_CLOCK);
                Console.INSTANCE.refreshClockText();
            }).build();

    /**
     * The show console clock seconds data piece.
     */
    public static final UserData<Boolean> showConsoleClockSeconds =
            new Builder<>(SHOW_CONSOLE_CLOCK_SECONDS, Boolean.class)
                    .setDescription("Whether to show seconds on the console clock")
                    .setOnChangeFunction(() -> {
                        Logger.log(LogTag.USER_DATA, SHOW_CONSOLE_CLOCK_SECONDS);
                        Console.INSTANCE.refreshClockText();
                    }).build();

    /**
     * The filter chat data piece.
     */
    public static final UserData<Boolean> filterChat = new Builder<>(FILTER_CHAT, Boolean.class)
            .setDescription("Whether the user input should be filtered")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, FILTER_CHAT)).build();

    /**
     * The last session start data piece.
     */
    public static final UserData<Long> lastSessionStart = new Builder<>(LAST_SESSION_START, Long.class)
            .setDescription("The time at which the last session for this user was started")
            .setDefaultValue(Long.MAX_VALUE)
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, LAST_SESSION_START)).build();

    /**
     * The minimize on close data piece.
     */
    public static final UserData<Boolean> minimizeOnClose = new Builder<>(MINIMIZE_ON_CLOSE, Boolean.class)
            .setDescription("Whether the cnosole should be minimized instead"
                    + " of closed when the closed button is pressed")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, MINIMIZE_ON_CLOSE)).build();

    /**
     * The typing animation data piece.
     */
    public static final UserData<Boolean> typingAnimation = new Builder<>(TYPING_ANIMATION, Boolean.class)
            .setDescription("Whether to show a typing animation for the console")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, TYPING_ANIMATION)).build();

    /**
     * The play typing sound data piece.
     */
    public static final UserData<Boolean> playTypingSound = new Builder<>(PLAY_TYPING_SOUND, Boolean.class)
            .setDescription("Whether to play a typing sound effect when the typing animation is enabled")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, PLAY_TYPING_SOUND)).build();

    /**
     * The show busy animation data piece.
     */
    public static final UserData<Boolean> showBusyAnimation = new Builder<>(SHOW_BUSY_ANIMATION, Boolean.class)
            .setDescription("Whether to show a busy animation")
            .setOnChangeFunction(() -> {
                Console.INSTANCE.hideBusyAnimation();
                Logger.log(LogTag.USER_DATA, SHOW_BUSY_ANIMATION);
            }).build();

    /**
     * The rounded frame borders data piece.
     */
    public static final UserData<Boolean> roundedFrameBorders = new Builder<>(ROUNDED_FRAME_BORDERS, Boolean.class)
            .setDescription("Whether to paint frames with rounded corners")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, ROUNDED_FRAME_BORDERS);
                UiUtil.repaintCyderFrames();
            }).build();

    /**
     * The frame color data piece.
     */
    public static final UserData<Color> frameColor = new Builder<>(FRAME_COLOR, Color.class)
            .setDescription("The color for frame borders")
            .setDefaultValue(CyderColors.navy)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FRAME_COLOR);

                UiUtil.repaintCyderFrames();
                Console.INSTANCE.revalidateMenuBackgrounds();
            }).build();

    /**
     * The console clock format data piece.
     */
    public static final UserData<String> consoleClockFormat = new Builder<>(CONSOLE_CLOCK_FORMAT, String.class)
            .setDescription("The date pattern for the console clock")
            .setDefaultValue(User.DEFAULT_CONSOLE_CLOCK_FORMAT)
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, CONSOLE_CLOCK_FORMAT);
                Console.INSTANCE.refreshClockText();
            }).build();

    /**
     * The YouTube UUID data piece.
     */
    public static final UserData<String> youtubeUuid = new Builder<>(YOUTUBE_UUID, String.class)
            .setDescription("The uuid this user is at for YouTube UUID generation")
            .setDefaultValue("aaaaaaaaaaa")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, YOUTUBE_UUID)).build();

    /**
     * The caps mode data piece.
     */
    public static final UserData<Boolean> capsMode = new Builder<>(CAPS_MODE, Boolean.class)
            .setDescription("Whether conosle output should be appended in capital letters")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, CAPS_MODE);
                // todo make all text in console capital letters? how would this be undone?
                // todo would have to maintain two styled text documents basically
            }).build();

    /**
     * The logged in data piece.
     */
    public static final UserData<Boolean> loggedIn = new Builder<>(LOGGED_IN, Boolean.class)
            .setDescription("Whether this user is currently logged in")
            .setIgnoreForToggleSwitches(true)
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, LOGGED_IN)).build();

    /**
     * The show audio total length data piece.
     */
    public static final UserData<Boolean> showAudioTotalLength = new Builder<>(SHOW_AUDIO_TOTAL_LENGTH, Boolean.class)
            .setDescription("Whether the audio total length should be shown instead"
                    + " of the time remaining for the audio player")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, SHOW_AUDIO_TOTAL_LENGTH)).build();

    /**
     * The should persist notifications data piece.
     */
    public static final UserData<Boolean> shouldPersistNotifications =
            new Builder<>(SHOULD_PERSIST_NOTIFICATIONS, Boolean.class)
                    .setDescription("Whether notifications should be persited until manually dismissed")
                    .setOnChangeFunction(() -> {
                        Logger.log(LogTag.USER_DATA, SHOULD_PERSIST_NOTIFICATIONS);
                        // todo hook to remove persisting notifications
                    }).build();

    /**
     * The do animations data piece.
     */
    public static final UserData<Boolean> doAnimations = new Builder<>(DO_ANIMATIONS, Boolean.class)
            .setDescription("Whether certain animations shoudl be performed")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, DO_ANIMATIONS)).build();

    /**
     * The compact text mode data piece.
     */
    public static final UserData<Boolean> compactTextMode = new Builder<>(COMPACT_TEXT_MODE, Boolean.class)
            .setDescription("Whether compact text mode is enabled")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, COMPACT_TEXT_MODE);

                Console.INSTANCE.revalidateConsoleTaskbarMenu();
                CyderScrollList.refreshAllLists();
            }).build();

    /**
     * The wrap native shell data piece.
     */
    public static final UserData<Boolean> wrapNativeShell = new Builder<>(WRAP_NATIVE_SHELL, Boolean.class)
            .setDescription("Whether unrecognized user commands should be pased to the native shell")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, WRAP_NATIVE_SHELL)).build();

    /**
     * The draw weather map data piece.
     */
    public static final UserData<Boolean> drawWeatherMap = new Builder<>(DRAW_WEATHER_MAP, Boolean.class)
            .setDescription("Whether a map should be drawn on the background of the weather widget")
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, DRAW_WEATHER_MAP);
                WeatherWidget.refreshAllMapBackgrounds();
            }).build();

    /**
     * The paint clock widget hour labels data piece.
     */
    public static final UserData<Boolean> paintclockWidgetHourLabels =
            new Builder<>(PAINT_CLOCK_WIDGET_HOUR_LABELS, Boolean.class)
                    .setDescription("Whether the hour labels should be painted for the clock widget")
                    .setOnChangeFunction(() -> {
                        Logger.log(LogTag.USER_DATA, PAINT_CLOCK_WIDGET_HOUR_LABELS);
                        ClockWidget.setPaintHourLabels(UserDataManager.INSTANCE.shouldPaintClockHourLabels());
                    }).build();

    /**
     * The show clock widget second hand data piece.
     */
    public static final UserData<Boolean> showClockWidgetSecondHand =
            new Builder<>(SHOW_CLOCK_WIDGET_SECOND_HAND, Boolean.class)
                    .setDescription("Whether the second hand should be shown for the clock widget")
                    .setOnChangeFunction(() -> {
                        Logger.log(LogTag.USER_DATA, SHOW_CLOCK_WIDGET_SECOND_HAND);
                        ClockWidget.setShowSecondHand(UserDataManager.INSTANCE.shouldShowClockWidgetSecondHand());
                    }).build();

    /**
     * The fill opacity data piece.
     */
    public static final UserData<Integer> fillOpacity = new Builder<>(FILL_OPACITY, Integer.class)
            .setDescription("The opacity of the input and output fills")
            .setDefaultValue(ColorUtil.opacityRange.upperEndpoint())
            .setOnChangeFunction(() -> {
                Logger.log(LogTag.USER_DATA, FILL_OPACITY);

                if (UserDataManager.INSTANCE.shouldDrawInputFill()) {
                    JTextField inputField = Console.INSTANCE.getInputField();
                    inputField.setOpaque(true);
                    inputField.setBackground(ColorUtil.setColorOpacity(
                            UserDataManager.INSTANCE.getBackgroundColor(),
                            UserDataManager.INSTANCE.getFillOpacity()));
                    inputField.revalidate();
                    inputField.repaint();
                }
            }).build();

    /**
     * The audio player volume percent data piece.
     */
    public static final UserData<Integer> audioPlayerVolumePercent = new Builder<>(AUDIO_PLAYER_VOLUME_PERCENT,
            Integer.class).setDescription("The volume for the audio player")
            .setDefaultValue(50).build();

    /**
     * The screen stat data piece.
     */
    public static final UserData<ScreenStat> screenStat = new Builder<>(SCREEN_STAT, ScreenStat.class)
            .setDescription("The user's screen stats")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, SCREEN_STAT)).build();

    /**
     * The shown welcome message data piece.
     */
    public static final UserData<Boolean> shownWelcomeMessage = new Builder<>(SHOWN_WELCOME_MESSAGE, Boolean.class)
            .setDescription("Whether the welcome message has been shown for this user")
            .setIgnoreForToggleSwitches(true)
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, SHOWN_WELCOME_MESSAGE)).build();

    /**
     * The account creation time data piece.
     */
    public static final UserData<Long> accountCreationTime = new Builder<>(ACCOUNT_CREATION_TIME, Long.class)
            .setDefaultValue(System.currentTimeMillis())
            .setDescription("The time at which this account was created")
            .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, ACCOUNT_CREATION_TIME)).build();

    /**
     * The mapped executables data piece.
     */
    public static final UserData<MappedExecutables> mappedExecutables =
            new Builder<>(MAPPED_EXECUTABLES, MappedExecutables.class)
                    .setDescription("The list of mapped executables stored by this user")
                    .setOnChangeFunction(() -> Logger.log(LogTag.USER_DATA, MAPPED_EXECUTABLES))
                    .build();

    /**
     * The list of all {@link UserData} pieces.
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
            randomBackgroundOnStart,
            drawOutputBorder,
            drawInputBorder,
            playHourlyChimes,
            silenceErrors,
            fullscreen,
            drawOutputFill,
            drawInputFill,
            drawConsoleClock,
            showConsoleClockSeconds,
            filterChat,
            lastSessionStart,
            minimizeOnClose,
            typingAnimation,
            playTypingSound,
            showBusyAnimation,
            roundedFrameBorders,
            frameColor,
            consoleClockFormat,
            youtubeUuid,
            capsMode,
            loggedIn,
            showAudioTotalLength,
            shouldPersistNotifications,
            doAnimations,
            compactTextMode,
            wrapNativeShell,
            drawWeatherMap,
            paintclockWidgetHourLabels,
            showClockWidgetSecondHand,
            fillOpacity,
            screenStat,
            shownWelcomeMessage,
            accountCreationTime,
            mappedExecutables,
            audioPlayerVolumePercent
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
        datas.stream().filter(userData -> userData.getId().equalsIgnoreCase(userDataId))
                .findFirst().ifPresentOrElse(userData -> {
                    Optional<Runnable> optionalRunnable = userData.getOnChangeRunnable();
                    optionalRunnable.ifPresent(Runnable::run);

                    onUserDataRefresh();
                }, () -> {
                    throw new FatalException("Failed to invoke user data refresh, failed to find id: " + userDataId);
                });
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") /* Readability */
    public boolean shouldIgnoreForToggleSwitches() {
        return ignoreForToggleSwitches || type != Boolean.class;
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