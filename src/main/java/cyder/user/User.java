package cyder.user;

import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.StringUtil;
import cyder.strings.ToStringUtil;
import cyder.user.data.MappedExecutable;
import cyder.user.data.ScreenStat;

import java.util.LinkedList;

/**
 * A user object which holds all relevant data about a Cyder user.
 * Note that the default constructor is not restricted due to GSON parsing.
 */
public class User {
    /**
     * Creates a new User object.
     */
    public User() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    // ---------------------------------------------------
    // Primitive data types. In the future, allow
    // this to be anything which will require methods that
    // use Class<?>, instanceof operators, and more.
    //
    // Preference<Boolean>, Preference<String> and such
    // ---------------------------------------------------

    /**
     * The Cyder username.
     */
    private String name;

    /**
     * The Cyder user's hashed password.
     */
    private String pass;

    /**
     * The current font name.
     */
    private String font;

    /**
     * The font size.
     */
    private String fontSize;

    /**
     * The foreground color.
     */
    private String foreground;

    /**
     * The background color.
     */
    private String background;

    /**
     * Whether to play intro music when Cyder starts up after the user has logged in.
     */
    private String introMusic;

    /**
     * Whether to open debug menus when the user logs in.
     */
    private String debugStats;

    /**
     * Whether to choose a random background on startup.
     */
    private String randomBackground;

    /**
     * Whether to draw a border around the output area.
     */
    private String outputBorder;

    /**
     * Whether to draw a border around the input field.
     */
    private String inputBorder;

    /**
     * Whether to chime hourly.
     */
    private String hourlyChimes;

    /**
     * Whether to silence errors when they occur.
     */
    private String silenceErrors;

    /**
     * Whether to draw the console as a fullscreen frame.
     */
    private String fullscreen;

    /**
     * Whether to fill the output area.
     */
    private String outputFill;

    /**
     * Whether to fill the input field.
     */
    private String inputFill;

    /**
     * Whether to show the clock on the console
     */
    private String clockOnConsole;

    /**
     * Whether to show seconds on the console clock.
     */
    private String showSeconds;

    /**
     * Whether to filter the chat of foul language.
     */
    private String filterChat;

    /**
     * The last time this user start and logged in to Cyder.
     */
    private String lastStart;

    /**
     * Whether to minimize instead of closing.
     */
    private String minimizeOnClose;

    /**
     * Whether to perform the typing animation.
     */
    private String typingAnimation;

    /**
     * Whether to show the busy icon.
     */
    private String showBusyIcon;

    /**
     * Whether to round CyderFrames.
     */
    private String roundedWindows;

    /**
     * The color to use for CyderFrame components and common panels such as menus.
     */
    private String windowColor;

    /**
     * The java date pattern to use for the console clock.
     */
    private String consoleClockFormat;

    /**
     * Whether to play a typing sound during typing animations.
     */
    private String typingSound;

    /**
     * The current uuid the procedural script is at.
     */
    private String youtubeUuid;

    /**
     * Whether to output ascii chars as capitalized letters.
     */
    private String capsMode;

    /**
     * Whether the user is logged in.
     */
    private String loggedIn;

    /**
     * Whether to show the audio total length or the time remaining for the audio player.
     */
    private String audioLength;

    /**
     * Whether to persist notifications.
     */
    private String persistentNotifications;

    /**
     * Whether to do animations such as close, minimize, and notification slides.
     */
    private String doAnimations;

    /**
     * Whether compact text mode is active.
     */
    private String compactTextMode;

    /**
     * If true, any unrecognized input is passed to the native terminal/shell.
     */
    private String wrapShell;

    /**
     * Dark mode controls certain gui aspects about Cyder
     * such as inform panes and the file chooser.
     */
    private String darkmode;

    /**
     * Weather to draw a location map as the background of the weather widget.
     */
    private String weatherMap;

    /**
     * Whether to paint the hour labels for the clock widget.
     */
    private String paintClockLabels;

    /**
     * Whether to show the second hand for the clock widget.
     */
    private String showSecondHand;

    // -------------------
    // Non-primitive types
    // -------------------

    /**
     * The screen stat object which holds the console's position,
     * size, pinned, monitor, and rotation vars.
     */
    private ScreenStat screenStat = new ScreenStat();

    // --------------------------
    // Data structures of objects
    // --------------------------

    /**
     * List of mapped executables that map a string to a file path.
     */
    private LinkedList<MappedExecutable> executables = new LinkedList<>();

    // -------
    // Getters
    // -------

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getName() {
        getterHook(Preference.NAME, name);
        return name;
    }

    /**
     * Returns the user password.
     *
     * @return the user password
     */
    public String getPass() {
        getterHook(Preference.PASS, pass);
        return pass;
    }

    /**
     * Returns the user font.
     *
     * @return the user font
     */
    public String getFont() {
        getterHook(Preference.FONT, font);
        return font;
    }

    /**
     * Returns the user font size.
     *
     * @return the user font size
     */
    public String getFontSize() {
        getterHook(Preference.FONT_SIZE, fontSize);
        return fontSize;
    }

    /**
     * Returns the foreground color.
     *
     * @return the foreground color
     */
    public String getForeground() {
        getterHook(Preference.FOREGROUND, foreground);
        return foreground;
    }

    /**
     * Returns the background color.
     *
     * @return the background color
     */
    public String getBackground() {
        getterHook(Preference.BACKGROUND, background);
        return background;
    }

    /**
     * Returns whether intro music is active.
     *
     * @return whether intro music is active
     */
    public String getIntroMusic() {
        getterHook(Preference.INTRO_MUSIC, introMusic);
        return introMusic;
    }

    /**
     * Returns whether to show debug stats on start.
     *
     * @return whether to show debug stats on start
     */
    public String getDebugStats() {
        getterHook(Preference.DEBUG_STATS, debugStats);
        return debugStats;
    }

    /**
     * Returns whether to choose a random background on launch.
     *
     * @return whether to choose a random background on launch
     */
    public String getRandomBackground() {
        getterHook(Preference.RANDOM_BACKGROUND, randomBackground);
        return randomBackground;
    }

    /**
     * Returns whether to draw the output border.
     *
     * @return whether to draw the output border
     */
    public String getOutputBorder() {
        getterHook(Preference.OUTPUT_BORDER, outputBorder);
        return outputBorder;
    }

    /**
     * Returns whether to draw the input border.
     *
     * @return whether to draw the input border
     */
    public String getInputBorder() {
        getterHook(Preference.INPUT_BORDER, inputBorder);
        return inputBorder;
    }

    /**
     * Returns whether to chime hourly.
     *
     * @return whether to chime hourly
     */
    public String getHourlyChimes() {
        getterHook(Preference.HOURLY_CHIMES, hourlyChimes);
        return hourlyChimes;
    }

    /**
     * Returns whether to silence errors.
     *
     * @return whether to silence errors
     */
    public String getSilenceErrors() {
        getterHook(Preference.SILENCE_ERRORS, silenceErrors);
        return silenceErrors;
    }

    /**
     * Returns whether fullscreen is active.
     *
     * @return whether fullscreen is active
     */
    public String getFullscreen() {
        getterHook(Preference.FULLSCREEN, fullscreen);
        return fullscreen;
    }

    /**
     * Returns whether to fill the output area.
     *
     * @return whether to fill the output area
     */
    public String getOutputFill() {
        getterHook(Preference.OUTPUT_FILL, outputFill);
        return outputFill;
    }

    /**
     * Returns whether to fill the input field.
     *
     * @return whether to fill the input field
     */
    public String getInputFill() {
        getterHook(Preference.INPUT_FILL, inputFill);
        return inputFill;
    }

    /**
     * Returns whether the clock should be displayed on the console.
     *
     * @return whether the clock should be displayed on the console
     */
    public String getClockOnConsole() {
        getterHook(Preference.CLOCK_ON_CONSOLE, clockOnConsole);
        return clockOnConsole;
    }

    /**
     * Returns whether to show seconds if console clock is enabled.
     *
     * @return whether to show seconds if console clock is enabled
     */
    public String getShowSeconds() {
        getterHook(Preference.SHOW_SECONDS, showSeconds);
        return showSeconds;
    }

    /**
     * Returns whether to filter the chat of foul language.
     *
     * @return whether to filter the chat of foul language
     */
    public String getFilterChat() {
        getterHook(Preference.FILTER_CHAT, filterChat);
        return filterChat;
    }

    /**
     * Returns the unix time of the last time this user started Cyder.
     *
     * @return the unix time of the last time this user started Cyder
     */
    public String getLastStart() {
        getterHook(Preference.LAST_START, lastStart);
        return lastStart;
    }

    /**
     * Returns whether to minimize on close.
     *
     * @return whether to minimize on close
     */
    public String getMinimizeOnClose() {
        getterHook(Preference.MINIMIZE_ON_CLOSE, minimizeOnClose);
        return minimizeOnClose;
    }

    /**
     * Returns whether to perform the typing animation.
     *
     * @return whether to perform the typing animation
     */
    public String getTypingAnimation() {
        getterHook(Preference.TYPING_ANIMATION, typingAnimation);
        return typingAnimation;
    }

    /**
     * Returns whether to show the busy icon when Cyder is busy.
     *
     * @return whether to show the busy icon when Cyder is busy
     */
    public String getShowBusyIcon() {
        getterHook(Preference.SHOW_BUSY_ICON, showBusyIcon);
        return showBusyIcon;
    }

    /**
     * Returns whether to round frames.
     *
     * @return whether to round frames
     */
    public String getRoundedWindows() {
        getterHook(Preference.ROUNDED_WINDOWS, roundedWindows);
        return roundedWindows;
    }

    /**
     * Returns the window color.
     *
     * @return the window color
     */
    public String getWindowColor() {
        getterHook(Preference.WINDOW_COLOR, windowColor);
        return windowColor;
    }

    /**
     * Returns the java date pattern for the console clock.
     *
     * @return the java date pattern for the console clock
     */
    public String getConsoleClockFormat() {
        getterHook(Preference.CONSOLE_CLOCK_FORMAT, consoleClockFormat);
        return consoleClockFormat;
    }

    /**
     * Returns whether to send a typing sound every typing animation.
     *
     * @return whether to send a typing sound every typing animation
     */
    public String getTypingSound() {
        getterHook(Preference.TYPING_SOUND, typingSound);
        return typingSound;
    }

    /**
     * Returns the youtube uuid the procedural checker script is at.
     *
     * @return the youtube uuid the procedural checker script is at
     */
    public String getYoutubeUuid() {
        getterHook(Preference.YOUTUBE_UUID, youtubeUuid);
        return youtubeUuid;
    }

    /**
     * Returns whether caps mode is on.
     *
     * @return whether caps mode is on
     */
    public String getCapsMode() {
        getterHook(Preference.CAPS_MODE, capsMode);
        return capsMode;
    }

    /**
     * Returns whether this user is logged in.
     *
     * @return whether this user is logged in
     */
    public String getLoggedIn() {
        getterHook(Preference.LOGGED_IN, loggedIn);
        return loggedIn;
    }

    /**
     * Returns whether to show the audio total length or the time remaining.
     *
     * @return whether to show the audio total length or the time remaining
     */
    public String getAudioLength() {
        getterHook(Preference.AUDIO_LENGTH, audioLength);
        return audioLength;
    }

    /**
     * Returns whether to persist notifications.
     *
     * @return whether to persist notifications
     */
    public String getPersistentNotifications() {
        getterHook(Preference.PERSISTENT_NOTIFICATIONS, persistentNotifications);
        return persistentNotifications;
    }

    /**
     * Returns whether to perform animations.
     *
     * @return whether to perform animations
     */
    public String getDoAnimations() {
        getterHook(Preference.DO_ANIMATIONS, doAnimations);
        return doAnimations;
    }

    /**
     * Returns whether compact text mode.
     *
     * @return whether compact text mode
     */
    public String getCompactTextMode() {
        getterHook(Preference.COMPACT_TEXT_MODE, compactTextMode);
        return compactTextMode;
    }

    /**
     * Returns whether to wrap the native terminal.
     *
     * @return whether to wrap the native terminal
     */
    public String getWrapShell() {
        getterHook(Preference.WRAP_SHELL, wrapShell);
        return wrapShell;
    }

    private static final String SCREEN_STAT = "screenstat";

    /**
     * Returns the screen stat object.
     *
     * @return the screen stat object
     */
    public ScreenStat getScreenStat() {
        getterHook(SCREEN_STAT, screenStat);
        return screenStat;
    }

    private static final String MAPPED_EXECUTABLE = "mappedexecutable";

    /**
     * Returns the list of mapped executables.
     *
     * @return the list of mapped executables
     */
    public LinkedList<MappedExecutable> getExecutables() {
        getterHook(MAPPED_EXECUTABLE, executables);
        return executables;
    }

    /**
     * Returns whether dark mode is active.
     *
     * @return whether dark mode is active
     */
    public String getDarkmode() {
        getterHook(Preference.DARK_MODE, darkmode);
        return darkmode;
    }

    /**
     * Returns whether to draw a location map as the background of the weather widget.
     *
     * @return whether to draw a location map as the background of the weather widget
     */
    public String getWeatherMap() {
        return weatherMap;
    }

    /**
     * Returns whether to paint the hour labels for the clock widget.
     *
     * @return whether to paint the hour labels for the clock widget
     */
    public String getPaintClockLabels() {
        return paintClockLabels;
    }

    /**
     * Returns whether to show the seconds hand for the clock widget.
     *
     * @return whether to show the seconds hand for the clock widget
     */
    public String getShowSecondHand() {
        return showSecondHand;
    }

    // -------
    // Setters
    // -------

    /**
     * Sets the username.
     *
     * @param name the username
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the user password.
     *
     * @param pass the user password
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * Sets the user font name.
     *
     * @param font the user font name
     */
    public void setFont(String font) {
        this.font = font;
    }

    /**
     * Sets the user font size.
     *
     * @param fontSize the user font size
     */
    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Sets the foreground color.
     *
     * @param foreground the foreground color
     */
    public void setForeground(String foreground) {
        this.foreground = foreground;
    }

    /**
     * Sets the background color.
     *
     * @param background the background color
     */
    public void setBackground(String background) {
        this.background = background;
    }

    /**
     * Sets whether to perform intro music.
     *
     * @param introMusic whether to perform intro music
     */
    public void setIntroMusic(String introMusic) {
        this.introMusic = introMusic;
    }

    /**
     * Sets whether to show debug stats on start.
     *
     * @param debugStats whether to show the debug stats on start
     */
    public void setDebugStats(String debugStats) {
        this.debugStats = debugStats;
    }

    /**
     * Sets whether to choose a random background on start.
     *
     * @param randomBackground whether to choose a random background on start
     */
    public void setRandomBackground(String randomBackground) {
        this.randomBackground = randomBackground;
    }

    /**
     * Sets whether to draw the output border.
     *
     * @param outputBorder whether to draw the output border
     */
    public void setOutputBorder(String outputBorder) {
        this.outputBorder = outputBorder;
    }

    /**
     * Sets whether to draw the input border.
     *
     * @param inputBorder whether to draw the input border
     */
    public void setInputBorder(String inputBorder) {
        this.inputBorder = inputBorder;
    }

    /**
     * Sets whether to chime hourly.
     *
     * @param hourlyChimes whether to chime hourly
     */
    public void setHourlyChimes(String hourlyChimes) {
        this.hourlyChimes = hourlyChimes;
    }

    /**
     * Sets whether to silence errors.
     *
     * @param silenceErrors whether to silence errors
     */
    public void setSilenceErrors(String silenceErrors) {
        this.silenceErrors = silenceErrors;
    }

    /**
     * Sets whether fullscreen is active.
     *
     * @param fullscreen whether fullscreen is active
     */
    public void setFullscreen(String fullscreen) {
        this.fullscreen = fullscreen;
    }

    /**
     * Sets whether to fill the output area.
     *
     * @param outputFill whether to fill the output area
     */
    public void setOutputFill(String outputFill) {
        this.outputFill = outputFill;
    }

    /**
     * Sets whether to fill the input field.
     *
     * @param inputFill whether to fill the input field
     */
    public void setInputFill(String inputFill) {
        this.inputFill = inputFill;
    }

    /**
     * Sets whether to show the console clock.
     *
     * @param clockOnConsole whether to show the console clock
     */
    public void setClockOnConsole(String clockOnConsole) {
        this.clockOnConsole = clockOnConsole;
    }

    /**
     * Sets whether to show seconds on the console clock.
     *
     * @param showSeconds whether to show seconds on the console clock
     */
    public void setShowSeconds(String showSeconds) {
        this.showSeconds = showSeconds;
    }

    /**
     * Sets whether to filter the chat of any foul language.
     *
     * @param filterChat whether to filter the chat of any foul language
     */
    public void setFilterChat(String filterChat) {
        this.filterChat = filterChat;
    }

    /**
     * Sets the last start time for this user.
     *
     * @param lastStart the last start time for this user
     */
    public void setLastStart(String lastStart) {
        this.lastStart = lastStart;
    }

    /**
     * Sets whether to minimize instead of close when the console close button is pressed.
     *
     * @param minimizeOnClose whether to minimize instead of close when the console close button is pressed
     */
    public void setMinimizeOnClose(String minimizeOnClose) {
        this.minimizeOnClose = minimizeOnClose;
    }

    /**
     * Sets whether to perform typing animations.
     *
     * @param typingAnimation whether to perform typing animations
     */
    public void setTypingAnimation(String typingAnimation) {
        this.typingAnimation = typingAnimation;
    }

    /**
     * Sets whether to show the busy icon.
     *
     * @param showBusyIcon whether to show the busy icon
     */
    public void setShowBusyIcon(String showBusyIcon) {
        this.showBusyIcon = showBusyIcon;
    }

    /**
     * Sets whether windows should be rounded.
     *
     * @param roundedWindows whether windows should be rounded
     */
    public void setRoundedWindows(String roundedWindows) {
        this.roundedWindows = roundedWindows;
    }

    /**
     * Sets the window color.
     *
     * @param windowColor the window color
     */
    public void setWindowColor(String windowColor) {
        this.windowColor = windowColor;
    }

    /**
     * Sets the console clock format.
     *
     * @param consoleClockFormat the console clock format
     */
    public void setConsoleClockFormat(String consoleClockFormat) {
        this.consoleClockFormat = consoleClockFormat;
    }

    /**
     * Sets whether to play a typing sound on typing animations calls.
     *
     * @param typingSound whether to play a typing sound on typing animations calls
     */
    public void setTypingSound(String typingSound) {
        this.typingSound = typingSound;
    }

    /**
     * Sets the youtube uuid the procedural finder is at.
     *
     * @param youtubeUuid the youtube uuid the procedural finder is at
     */
    public void setYoutubeUuid(String youtubeUuid) {
        this.youtubeUuid = youtubeUuid;
    }

    /**
     * Sets caps mode.
     *
     * @param capsMode caps mode
     */
    public void setCapsMode(String capsMode) {
        this.capsMode = capsMode;
    }

    /**
     * Sets whether this user is logged in.
     *
     * @param loggedIn whether this user is logged in
     */
    public void setLoggedIn(String loggedIn) {
        this.loggedIn = loggedIn;
    }

    /**
     * Sets whether to show the audio total length or time remaining for the audio player.
     *
     * @param audioLength whether to show the audio total length or time remaining for the audio player
     */
    public void setAudioLength(String audioLength) {
        this.audioLength = audioLength;
    }

    /**
     * Sets whether to persist notifications.
     *
     * @param persistentNotifications whether to persist notifications
     */
    public void setPersistentNotifications(String persistentNotifications) {
        this.persistentNotifications = persistentNotifications;
    }

    /**
     * Sets whether to do animations.
     *
     * @param doAnimations whether to do animations
     */
    public void setDoAnimations(String doAnimations) {
        this.doAnimations = doAnimations;
    }

    /**
     * Sets whether compact text mode is active.
     *
     * @param compactTextMode whether compact text mode is active
     */
    public void setCompactTextMode(String compactTextMode) {
        this.compactTextMode = compactTextMode;
    }

    /**
     * Sets whether to wrap the native shell.
     *
     * @param wrapShell whether to wrap the native shell
     */
    public void setWrapShell(String wrapShell) {
        this.wrapShell = wrapShell;
    }

    /**
     * Sets the screenstat object.
     *
     * @param screenStat the screenstat object
     */
    public void setScreenStat(ScreenStat screenStat) {
        this.screenStat = screenStat;
    }

    /**
     * Sets the list of mapped executables.
     *
     * @param executables the list of mapped executables
     */
    public void setExecutables(LinkedList<MappedExecutable> executables) {
        this.executables = executables;
    }

    /**
     * Sets the darkmode property.
     *
     * @param darkmode the darkmode property
     */
    public void setDarkmode(String darkmode) {
        this.darkmode = darkmode;
    }

    /**
     * Sets whether to draw a location map as the background of the weather widget.
     *
     * @param weatherMap whether to draw a location map as the background of the weather widget
     */
    public void setWeatherMap(String weatherMap) {
        this.weatherMap = weatherMap;
    }

    /**
     * Sets whether to paint the hour labels for the clock widget.
     *
     * @param paintClockLabels whether to paint the hour labels for the clock widget
     */
    public void setPaintClockLabels(String paintClockLabels) {
        this.paintClockLabels = paintClockLabels;
    }

    /**
     * Sets whether to show the seconds hand for the clock widget.
     *
     * @param showSecondHand whether to show the seconds hand for the clock widget
     */
    public void setShowSecondHand(String showSecondHand) {
        this.showSecondHand = showSecondHand;
    }

    // -----------
    // End setters
    // -----------

    /**
     * The hook to call on all getters inside of the user class.
     *
     * @param id    the id of a user data
     * @param value the current value of the user data
     */
    private static void getterHook(String id, Object value) {
        // todo should have a method for this in case user specifies "all" which should be allowed
        if (!StringUtil.in(id, true, UserUtil.getIgnoreUserData())) {
            Logger.log(LogTag.USER_GET, "key: " + id + ", value: " + value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = name.hashCode();
        ret = 31 * ret + pass.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof User)) {
            return false;
        }

        User other = (User) o;

        return other.getPass().equals(getPass())
                && other.getName().equals(getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringUtil.commonCyderToString(this);
    }
}
