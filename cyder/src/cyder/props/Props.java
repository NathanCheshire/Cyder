package cyder.props;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.drag.DragLabelButtonSize;

/**
 * The props recognized by Cyder.
 */
@SuppressWarnings("unused") /* Readability */
public final class Props {
    /**
     * Suppress default constructor.
     */
    private Props() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The empty string, used for default key values to indicate there is no default value present.
     */
    private static final String EMPTY = "";

    /*
    Regular props.
     */

    /**
     * Whether the props can be reloaded mid-runtime (Meta AF).
     */
    public static final Proper<Boolean> propsReloadable =
            new Proper<>("props_reloadable", false, Boolean.class);

    /**
     * The default location for services which require a location.
     * (All about a buck, baby, bay just like Tampa)
     */
    public static final Proper<String> defaultLocation =
            new Proper<>("default_location", "Tampa,FL,USA", String.class);

    /**
     * The data stored within a User object which should not be logged on access.
     */
    public static final Proper<?> ignoreData = new Proper<>("ignore_data", new String[]{
            "typinganimation", "showseconds", "roundedwindows", "windowcolor", "audiolength", "capsmode",
            "typingsound", "showbusyicon", "clockonconsole", "consoleclockformat", "doanimations"},
            String[].class);

    /**
     * The font metric for the input/output areas (One of: bold, italic, bold-italic, or plain).
     */
    public static final Proper<String> fontMetric =
            new Proper<>("font_metric", "bold", String.class);

    /**
     * The maximum font size allowable for the input/output areas.
     */
    public static final Proper<Integer> maxFontSize =
            new Proper<>("max_font_size", 50, Integer.class);

    /**
     * The minimum font size allowable for the input/output areas.
     */
    public static final Proper<Integer> minFontSize =
            new Proper<>("min_font_size", 25, Integer.class);

    /**
     * The font for the console clock if enabled.
     */
    public static final Proper<String> consoleClockFontName =
            new Proper<>("console_clock_font_name", "Agency FB", String.class);

    /**
     * The font size for the console clock if enabled.
     */
    public static final Proper<Integer> consoleClockFontSize =
            new Proper<>("console_clock_font_size", 26, Integer.class);

    /**
     * Whether testing mode is active. (Any CyderTest annotations found with the trigger of "test"
     * will be invoked immediately following a Console load)
     */
    public static final Proper<Boolean> testingMode =
            new Proper<>("testing_mode", false, Boolean.class);

    /**
     * Whether an auto-cypher should be attempted (Requires {@link #debugHashName}
     * and {@link #debugHashPassword} props).
     */
    public static final Proper<Boolean> autocypher =
            new Proper<>("autocypher", false, Boolean.class);

    /**
     * Whether past logs should be wiped when Cyder is launched.
     */
    public static final Proper<Boolean> wipeLogsOnStart =
            new Proper<>("wipe_logs_on_start", true, Boolean.class);

    /**
     * Whether the splash should be disposed normally.
     */
    public static final Proper<Boolean> disposeSplash =
            new Proper<>("dispose_splash", true, Boolean.class);

    /**
     * Whether the splash animation should complete before disposal.
     */
    public static final Proper<Boolean> allowSplashCompletion =
            new Proper<>("allow_splash_completion", false, Boolean.class);

    /**
     * The preferred audio output format when FFMPEG wrappers are used.
     */
    public static final Proper<String> ffmpegAudioOutputFormat =
            new Proper<>("ffmpeg_audio_output_format", "mp3", String.class);

    /**
     * The time in ms between char appends for the console printing animation if enabled.
     */
    public static final Proper<Integer> printingAnimationCharTimeout =
            new Proper<>("printing_animation_char_timeout", 8, Integer.class);

    /**
     * The time in ms between sound effects for the console printing animation if enabled.
     */
    public static final Proper<Integer> printingAnimationSoundFrequency =
            new Proper<>("printing_animation_sound_frequency", 4, Integer.class);

    /**
     * The time in ms between finishing one line and starting the
     * next line for the console printing animation if enabled.
     */
    public static final Proper<Integer> printingAnimationLineTimeout =
            new Proper<>("printing_animation_line_timeout", 100, Integer.class);

    /**
     * Whether similar commands should be automatically triggered if a command cannot be found for the exactly input.
     */
    public static final Proper<Boolean> autoTriggerSimilarCommands =
            new Proper<>("auto_trigger_similar_commands", false, Boolean.class);

    /**
     * The tolerance required to automatically trigger a similar command.
     */
    public static final Proper<Float> autoTriggerSimilarCommandTolerance =
            new Proper<>("auto_trigger_similar_command_tolerance", 0.95f, Float.class);

    /**
     * The IP to ping when determining the system network latency.
     */
    public static final Proper<String> latencyIp =
            new Proper<>("latency_ip", "172.217.4.78", String.class);

    /**
     * The port to ping when determine the system network latency.
     */
    public static final Proper<Integer> latencyPort =
            new Proper<>("latency_port", 80, Integer.class);

    /**
     * The name of the domain being pinged when determining the network latency
     * (If absent the DNS records will be used to determine the name).
     */
    public static final Proper<String> latencyName =
            new Proper<>("latency_name", "Google", String.class);

    /**
     * The size of drag label buttons.
     */
    public static final Proper<DragLabelButtonSize> dragLabelButtonSize =
            new Proper<>("drag_label_button_size", DragLabelButtonSize.SMALL, DragLabelButtonSize.class);

    /**
     * The length of the frame borders (left, right, and bottom drag labels).
     */
    public static final Proper<Integer> dragLabelHeight =
            new Proper<>("drag_label_height", 30, Integer.class);

    /**
     * The length of the frame borders (Left, right, and bottom drag labels).
     */
    public static final Proper<Integer> frameBorderLength =
            new Proper<>("frame_border_length", 5, Integer.class);

    /**
     * Whether certain components on absolute layouts may be moved around using the mouse.
     */
    public static final Proper<Boolean> componentsRelocatable =
            new Proper<>("components_relocatable", false, Boolean.class);

    /**
     * The pixel snap size for resizing the console frame.
     */
    public static final Proper<Integer> consoleSnapSize =
            new Proper<>("console_snap_size", 1, Integer.class);

    /**
     * The port the instance socket binds to (changing this is not recommended).
     */
    public static final Proper<Integer> instanceSocketPort =
            new Proper<>("instance_socket_port", 8888, Integer.class);

    /**
     * Whether local host shutdown requests are permitted.
     */
    public static final Proper<Boolean> localhostShutdownRequestsEnabled =
            new Proper<>("localhost_shutdown_requests_enabled", true, Boolean.class);

    /**
     * Whether the localhost shutdown password can be ignored
     */
    public static final Proper<Boolean> autoComplyToLocalhostShutdownRequests =
            new Proper<>("auto_comply_to_localhost_shutdown_requests", false, Boolean.class);

    /**
     * The password foreign instances must provide in order to shutdown this version of Cyder.
     */
    public static final Proper<String> localhostShutdownRequestPassword =
            new Proper<>("localhost_shutdown_request_password", "Vexento", String.class);

    /**
     * Whether the watchdog should be activated on start if all other conditions are met.
     * (Watchdog is not activated if JDWP is found in the JVM input arguments)
     */
    public static final Proper<Boolean> activateWatchdog =
            new Proper<>("activate_watchdog", true, Boolean.class);

    /**
     * The timeout in ms between watchdog polls of the AWT event queue 0 thread.
     */
    public static final Proper<Integer> watchdogPollTimeout =
            new Proper<>("watchdog_poll_timeout", 100, Integer.class);

    /**
     * Whether to attempt a boostrap if the program detects a GUI freeze.
     */
    public static final Proper<Boolean> attemptBootstrap =
            new Proper<>("attempt_bootstrap", true, Boolean.class);

    /**
     * Use javaw.exe instead of java.exe for bootstrapping (javaw has no console window).
     */
    public static final Proper<Boolean> preferJavawOverJavaForBootstrapping =
            new Proper<>("prefer_javaw_over_java_for_bootstrapping", false, Boolean.class);

    /*
    Props which should be logged or tracked by VCS.
     */

    /**
     * The key used to access the Open Weather Map API.
     */
    public static final Proper<String> weatherKey = new Proper<>("weather_key", EMPTY, String.class);

    /**
     * The key used to access the Map Quest API.
     */
    public static final Proper<String> mapQuestApiKey = new Proper<>("map_quest_api_key", EMPTY, String.class);

    /**
     * The key used to access the YouTube V3 API.
     */
    public static final Proper<String> youtubeApi3key = new Proper<>("youtube_api_3_key", EMPTY, String.class);

    /**
     * The key used to access the IP data API.
     */
    public static final Proper<String> ipKey = new Proper<>("ip_key", EMPTY, String.class);

    /**
     * The username used for an autocypher if {@link #autocypher} is true.
     */
    public static final Proper<String> debugHashName = new Proper<>("debug_hash_name", EMPTY, String.class);

    /**
     * The password used for an autocypher if {@link #autocypher} is true.
     */
    public static final Proper<String> debugHashPassword =
            new Proper<>("debug_hash_password", EMPTY, String.class);
}
