package cyder.props;

import com.google.common.collect.ImmutableList;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.user.UserData;

import static cyder.strings.CyderStrings.EMPTY;

/**
 * The props recognized by Cyder.
 */
@SuppressWarnings("unused") /* Have yet to be used */
public final class Props {
    /**
     * Whether the props can be reloaded mid-runtime (Meta AF).
     */
    public static final Prop<Boolean> propsReloadable =
            new Prop<>("props_reloadable", false, Boolean.class);

    /**
     * The default location for services which require a location.
     * (All about a buck, baby, bay just like Tampa)
     */
    public static final Prop<String> defaultLocation =
            new Prop<>("default_location", "Tampa,FL,USA", String.class);

    /**
     * The data stored within a User object which should not be logged on access.
     */
    public static final Prop<PropValueList> ignoreData =
            new Prop<>("ignore_data", new PropValueList(ImmutableList.of(
                    UserData.TYPING_ANIMATION,
                    UserData.SHOW_CONSOLE_CLOCK_SECONDS,
                    UserData.ROUNDED_FRAME_BORDERS,
                    UserData.FRAME_COLOR,
                    UserData.SHOW_AUDIO_TOTAL_LENGTH,
                    UserData.CAPS_MODE,
                    UserData.PLAY_TYPING_SOUND,
                    UserData.SHOW_BUSY_ANIMATION,
                    UserData.DRAW_CONSOLE_CLOCK,
                    UserData.SHOW_CONSOLE_CLOCK_SECONDS,
                    UserData.CONSOLE_CLOCK_FORMAT,
                    UserData.DO_ANIMATIONS)), PropValueList.class);

    /**
     * The font metric for the input/output areas (One of: bold, italic, bold-italic, or plain).
     */
    public static final Prop<String> fontMetric =
            new Prop<>("font_metric", "bold", String.class);

    /**
     * The maximum font size allowable for the input/output areas.
     */
    public static final Prop<Integer> maxFontSize =
            new Prop<>("max_font_size", 50, Integer.class);

    /**
     * The minimum font size allowable for the input/output areas.
     */
    public static final Prop<Integer> minFontSize =
            new Prop<>("min_font_size", 25, Integer.class);

    /**
     * The font for the console clock if enabled.
     */
    public static final Prop<String> consoleClockFontName =
            new Prop<>("console_clock_font_name", "Segoe UI Black", String.class);

    /**
     * The font size for the console clock if enabled.
     */
    public static final Prop<Integer> consoleClockFontSize =
            new Prop<>("console_clock_font_size", 22, Integer.class);

    /**
     * Whether testing mode is active. (Any CyderTest annotations found with the trigger of "test"
     * will be invoked immediately following a Console load)
     */
    public static final Prop<Boolean> testingMode =
            new Prop<>("testing_mode", false, Boolean.class);

    /**
     * Whether an auto-cypher should be attempted (Requires {@link #autocypherName}
     * and {@link #autocypherPassword} props).
     */
    public static final Prop<Boolean> autocypher =
            new Prop<>("autocypher", false, Boolean.class);

    /**
     * Whether past logs should be wiped when Cyder is launched.
     */
    public static final Prop<Boolean> wipeLogsOnStart =
            new Prop<>("wipe_logs_on_start", false, Boolean.class);

    /**
     * Whether the splash should be disposed normally.
     */
    public static final Prop<Boolean> disposeSplash =
            new Prop<>("dispose_splash", true, Boolean.class);

    /**
     * Whether the splash animation should complete before disposal.
     */
    public static final Prop<Boolean> allowSplashCompletion =
            new Prop<>("allow_splash_completion", true, Boolean.class);

    /**
     * The preferred audio output format when FFMPEG wrappers are used.
     */
    public static final Prop<String> ffmpegAudioOutputFormat =
            new Prop<>("ffmpeg_audio_output_format", "mp3", String.class);

    /**
     * The time in ms between char appends for the console printing animation if enabled.
     */
    public static final Prop<Integer> printingAnimationCharTimeout =
            new Prop<>("printing_animation_char_timeout", 8, Integer.class);

    /**
     * The time in ms between sound effects for the console printing animation if enabled.
     */
    public static final Prop<Integer> printingAnimationSoundFrequency =
            new Prop<>("printing_animation_sound_frequency", 4, Integer.class);

    /**
     * The time in ms between finishing one line and starting the
     * next line for the console printing animation if enabled.
     */
    public static final Prop<Integer> printingAnimationLineTimeout =
            new Prop<>("printing_animation_line_timeout", 100, Integer.class);

    /**
     * Whether similar commands should be automatically triggered if a command cannot be found for the exactly input.
     */
    public static final Prop<Boolean> autoTriggerSimilarCommands =
            new Prop<>("auto_trigger_similar_commands", false, Boolean.class);

    /**
     * The tolerance required to automatically trigger a similar command.
     */
    public static final Prop<Float> autoTriggerSimilarCommandTolerance =
            new Prop<>("auto_trigger_similar_command_tolerance", 0.95f, Float.class);

    /**
     * The IP to ping when determining the system network latency.
     */
    public static final Prop<String> latencyIp =
            new Prop<>("latency_ip", "172.217.4.78", String.class);

    /**
     * The port to ping when determine the system network latency.
     */
    public static final Prop<Integer> latencyPort =
            new Prop<>("latency_port", 80, Integer.class);

    /**
     * The name of the domain being pinged when determining the network latency
     * (If absent the DNS records will be used to determine the name).
     */
    public static final Prop<String> latencyName =
            new Prop<>("latency_name", "Google", String.class);

    /**
     * The size of drag label buttons (One of: small, medium, large, full_drag_label).
     */
    public static final Prop<String> dragLabelButtonSize =
            new Prop<>("drag_label_button_size", "small", String.class);

    /**
     * The length of the frame borders (left, right, and bottom drag labels).
     */
    public static final Prop<Integer> dragLabelHeight =
            new Prop<>("drag_label_height", 30, Integer.class);

    /**
     * The length of the frame borders (Left, right, and bottom drag labels).
     */
    public static final Prop<Integer> frameBorderLength =
            new Prop<>("frame_border_length", 5, Integer.class);

    /**
     * Whether certain components on absolute layouts may be moved around using the mouse.
     */
    public static final Prop<Boolean> componentsRelocatable =
            new Prop<>("components_relocatable", false, Boolean.class);

    /**
     * The pixel snap size for resizing the console frame.
     */
    public static final Prop<Integer> consoleSnapSize =
            new Prop<>("console_snap_size", 1, Integer.class);

    /**
     * The port the instance socket binds to (changing this is not recommended).
     */
    public static final Prop<Integer> instanceSocketPort =
            new Prop<>("instance_socket_port", 8888, Integer.class);

    /**
     * Whether local host shutdown requests are permitted.
     */
    public static final Prop<Boolean> localhostShutdownRequestsEnabled =
            new Prop<>("localhost_shutdown_requests_enabled", true, Boolean.class);

    /**
     * Whether the localhost shutdown password can be ignored
     */
    public static final Prop<Boolean> autoComplyToLocalhostShutdownRequests =
            new Prop<>("auto_comply_to_localhost_shutdown_requests", false, Boolean.class);

    /**
     * The password foreign instances must provide in order to shutdown this version of Cyder.
     */
    public static final Prop<String> localhostShutdownRequestPassword =
            new Prop<>("localhost_shutdown_request_password", "Vexento", String.class);

    /**
     * Whether the watchdog should be activated on start if all other conditions are met.
     * (Watchdog is not activated if JDWP is found in the JVM input arguments)
     */
    public static final Prop<Boolean> activateWatchdog =
            new Prop<>("activate_watchdog", true, Boolean.class);

    /**
     * The timeout in ms between watchdog polls of the AWT event queue 0 thread.
     */
    public static final Prop<Integer> watchdogPollTimeout =
            new Prop<>("watchdog_poll_timeout", 100, Integer.class);

    /**
     * Whether to attempt a boostrap if the program detects a GUI freeze.
     */
    public static final Prop<Boolean> attemptBootstrap =
            new Prop<>("attempt_bootstrap", true, Boolean.class);

    /**
     * Use javaw.exe instead of java.exe for bootstrapping (javaw has no console window).
     */
    public static final Prop<Boolean> preferJavawOverJavaForBootstrapping =
            new Prop<>("prefer_javaw_over_java_for_bootstrapping", false, Boolean.class);

    /**
     * The frequency at which to serialize and save the current, logged-in, user to their JSON file.
     */
    public static final Prop<Integer> serializeAndSaveCurrentUserFrequency =
            new Prop<>("serialize_and_save_current_user_frequency", 3, Integer.class);

    /**
     * The list of specific objects to log when they are created.
     */
    public static final Prop<PropValueList> specificObjectCreationLogs =
            new Prop<>("specific_object_creation_logs",
                    new PropValueList(ImmutableList.of()), PropValueList.class);

    /**
     * The maximum number of UUIDs to get the title of and choose the one with the lowest
     * Levenshtein distance when attempting to find the most likely YouTube UUID for a search query.
     */
    public static final Prop<Integer> maxYouTubeUuidChecksPlayCommand =
            new Prop<>("max_youtube_uuid_checks_play_command", 10, Integer.class);

    // ---------------------------------------------------
    // Props which should not be logged or tracked by VCS.
    // ---------------------------------------------------

    /**
     * The key used to access the Open Weather Map API.
     */
    public static final Prop<String> weatherKey = new Prop<>("weather_key", EMPTY, String.class);

    /**
     * The key used to access the Map Quest API.
     */
    public static final Prop<String> mapQuestApiKey = new Prop<>("map_quest_api_key", EMPTY, String.class);

    /**
     * The key used to access the YouTube V3 API.
     */
    public static final Prop<String> youTubeApi3key = new Prop<>("youtube_api_3_key", EMPTY, String.class);

    /**
     * The key used to access the IP data API.
     */
    public static final Prop<String> ipKey = new Prop<>("ip_key", EMPTY, String.class);

    /**
     * The username used for an autocypher if {@link #autocypher} is true.
     */
    public static final Prop<String> autocypherName = new Prop<>("autocypher_name", EMPTY, String.class);

    /**
     * The password used for an autocypher if {@link #autocypher} is true.
     */
    public static final Prop<String> autocypherPassword = new Prop<>("autocypher_password", EMPTY, String.class);

    /**
     * Suppress default constructor.
     */
    private Props() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
