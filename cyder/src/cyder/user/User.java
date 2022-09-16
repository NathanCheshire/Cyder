package cyder.user;

import com.google.common.base.Objects;
import cyder.handlers.internal.Logger;
import cyder.utils.ReflectionUtil;
import cyder.utils.StringUtil;

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
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    // ---------------------------------------------------
    // primitive data types. In the future, allow
    // this to be anything which will require methods that
    // use Class<?>, instanceof operators, and more.
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
    private String fontsize;

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
    private String intromusic;

    /**
     * Whether to open debug menus when the user logs in.
     */
    private String debugwindows;

    /**
     * Whether to choose a random background on startup.
     */
    private String randombackground;

    /**
     * Whether to draw a border around the output area.
     */
    private String outputborder;

    /**
     * Whether to draw a border around the input field.
     */
    private String inputborder;

    /**
     * Whether to chime hourly.
     */
    private String hourlychimes;

    /**
     * Whether to silence errors when they occur.
     */
    private String silenceerrors;

    /**
     * Whether to draw the console as a fullscreen frame.
     */
    private String fullscreen;

    /**
     * Whether to fill the output area.
     */
    private String outputfill;

    /**
     * Whether to fill the input field.
     */
    private String inputfill;

    /**
     * Whether to show the clock on the console
     */
    private String clockonconsole;

    /**
     * Whether to show seconds on the console clock.
     */
    private String showseconds;

    /**
     * Whether to filter the chat of foul language.
     */
    private String filterchat;

    /**
     * The last time this user start and logged in to Cyder.
     */
    private String laststart;

    /**
     * Whether to minimize instead of closing.
     */
    private String minimizeonclose;

    /**
     * Whether to perform the typing animation.
     */
    private String typinganimation;

    /**
     * Whether to show the busy icon.
     */
    private String showbusyicon;

    /**
     * Whether to round CyderFrames.
     */
    private String roundedwindows;

    /**
     * The color to use for CyderFrame components and common panels such as menus.
     */
    private String windowcolor;

    /**
     * The java date pattern to use for the console clock.
     */
    private String consoleclockformat;

    /**
     * Whether to play a typing sound during typing animations.
     */
    private String typingsound;

    /**
     * The current uuid the procedural script is at.
     */
    private String youtubeuuid;

    /**
     * Whether to output ascii chars as capitalized letters.
     */
    private String capsmode;

    /**
     * Whether the user is logged in.
     */
    private String loggedin;

    /**
     * Whether to show the audio total length or the time remaining for the audio player.
     */
    private String audiolength;

    /**
     * Whether to persist notifications.
     */
    private String persistentnotifications;

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
    private String wrapshell;

    /**
     * Dark mode controls certain gui aspects about Cyder
     * such as inform panes and the file chooser.
     */
    private String darkmode;

    /**
     * Weather to draw a location map as the background of the weather widget.
     */
    private String weatherMap;

    // -------------------
    // non primitive types
    // -------------------

    /**
     * The screen stat object which holds the console's position,
     * size, pinned, monitor, and rotation vars.
     */
    private ScreenStat screenStat = new ScreenStat();

    // --------------------------
    // data structures of objects
    // --------------------------

    /**
     * List of mapped executables that map a string to a file path.
     */
    private LinkedList<MappedExecutable> executables = new LinkedList<>();

    // -------
    // getters
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
    public String getFontsize() {
        getterHook(Preference.FONT_SIZE, fontsize);
        return fontsize;
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
    public String getIntromusic() {
        getterHook(Preference.INTRO_MUSIC, intromusic);
        return intromusic;
    }

    /**
     * Returns whether to show debug menus on start.
     *
     * @return whether to show debug menus on start
     */
    public String getDebugwindows() {
        getterHook(Preference.DEBUG_WINDOWS, debugwindows);
        return debugwindows;
    }

    /**
     * Returns whether to choose a random background on launch.
     *
     * @return whether to choose a random background on launch
     */
    public String getRandombackground() {
        getterHook(Preference.RANDOM_BACKGROUND, randombackground);
        return randombackground;
    }

    /**
     * Returns whether to draw the output border.
     *
     * @return whether to draw the output border
     */
    public String getOutputborder() {
        getterHook(Preference.OUTPUT_BORDER, outputborder);
        return outputborder;
    }

    /**
     * Returns whether to draw the input border.
     *
     * @return whether to draw the input border
     */
    public String getInputborder() {
        getterHook(Preference.INPUT_BORDER, inputborder);
        return inputborder;
    }

    /**
     * Returns whether to chime hourly.
     *
     * @return whether to chime hourly
     */
    public String getHourlychimes() {
        getterHook(Preference.HOURLY_CHIMES, hourlychimes);
        return hourlychimes;
    }

    /**
     * Returns whether to silence errors.
     *
     * @return whether to silence errors
     */
    public String getSilenceerrors() {
        getterHook(Preference.SILENCE_ERRORS, silenceerrors);
        return silenceerrors;
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
    public String getOutputfill() {
        getterHook(Preference.OUTPUT_FILL, outputfill);
        return outputfill;
    }

    /**
     * Returns whether to fill the input field.
     *
     * @return whether to fill the input field
     */
    public String getInputfill() {
        getterHook(Preference.INPUT_FILL, inputfill);
        return inputfill;
    }

    /**
     * Returns whether the clock should be displayed on the console.
     *
     * @return whether the clock should be displayed on the console
     */
    public String getClockonconsole() {
        getterHook(Preference.CLOCK_ON_CONSOLE, clockonconsole);
        return clockonconsole;
    }

    /**
     * Returns whether to show seconds if console clock is enabled.
     *
     * @return whether to show seconds if console clock is enabled
     */
    public String getShowseconds() {
        getterHook(Preference.SHOW_SECONDS, showseconds);
        return showseconds;
    }

    /**
     * Returns whether to filter the chat of foul language.
     *
     * @return whether to filter the chat of foul language
     */
    public String getFilterchat() {
        getterHook(Preference.FILTER_CHAT, filterchat);
        return filterchat;
    }

    /**
     * Returns the unix time of the last time this user started Cyder.
     *
     * @return the unix time of the last time this user started Cyder
     */
    public String getLaststart() {
        getterHook(Preference.LAST_START, laststart);
        return laststart;
    }

    /**
     * Returns whether to minimize on close.
     *
     * @return whether to minimize on close
     */
    public String getMinimizeonclose() {
        getterHook(Preference.MINIMIZE_ON_CLOSE, minimizeonclose);
        return minimizeonclose;
    }

    /**
     * Returns whether to perform the typing animation.
     *
     * @return whether to perform the typing animation
     */
    public String getTypinganimation() {
        getterHook(Preference.TYPING_ANIMATION, typinganimation);
        return typinganimation;
    }

    /**
     * Returns whether to show the busy icon when Cyder is busy.
     *
     * @return whether to show the busy icon when Cyder is busy
     */
    public String getShowbusyicon() {
        getterHook(Preference.SHOW_BUSY_ICON, showbusyicon);
        return showbusyicon;
    }

    /**
     * Returns whether to round frames.
     *
     * @return whether to round frames
     */
    public String getRoundedwindows() {
        getterHook(Preference.ROUNDED_WINDOWS, roundedwindows);
        return roundedwindows;
    }

    /**
     * Returns the window color.
     *
     * @return the window color
     */
    public String getWindowcolor() {
        getterHook(Preference.WINDOW_COLOR, windowcolor);
        return windowcolor;
    }

    /**
     * Returns the java date pattern for the console clock.
     *
     * @return the java date pattern for the console clock
     */
    public String getConsoleclockformat() {
        getterHook(Preference.CONSOLE_CLOCK_FORMAT, consoleclockformat);
        return consoleclockformat;
    }

    /**
     * Returns whether to send a typing sound every typing animation.
     *
     * @return whether to send a typing sound every typing animation
     */
    public String getTypingsound() {
        getterHook(Preference.TYPING_SOUND, typingsound);
        return typingsound;
    }

    /**
     * Returns the youtube uuid the procedural checker script is at.
     *
     * @return the youtube uuid the procedural checker script is at
     */
    public String getYoutubeuuid() {
        getterHook(Preference.YOUTUBE_UUID, youtubeuuid);
        return youtubeuuid;
    }

    /**
     * Returns whether caps mode is on.
     *
     * @return whether caps mode is on
     */
    public String getCapsmode() {
        getterHook(Preference.CAPS_MODE, capsmode);
        return capsmode;
    }

    /**
     * Returns whether this user is logged in.
     *
     * @return whether this user is logged in
     */
    public String getLoggedin() {
        getterHook(Preference.LOGGED_IN, loggedin);
        return loggedin;
    }

    /**
     * Returns whether to show the audio total length or the time remaining.
     *
     * @return whether to show the audio total length or the time remaining
     */
    public String getAudiolength() {
        getterHook(Preference.AUDIO_LENGTH, audiolength);
        return audiolength;
    }

    /**
     * Returns whether to persist notifications.
     *
     * @return whether to persist notifications
     */
    public String getPersistentnotifications() {
        getterHook(Preference.PERSISTENT_NOTIFICATIONS, persistentnotifications);
        return persistentnotifications;
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
    public String getWrapshell() {
        getterHook(Preference.WRAP_SHELL, wrapshell);
        return wrapshell;
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
     * @param fontsize the user font size
     */
    public void setFontsize(String fontsize) {
        this.fontsize = fontsize;
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
     * @param intromusic whether to perform intro music
     */
    public void setIntromusic(String intromusic) {
        this.intromusic = intromusic;
    }

    /**
     * Sets whether to show debug menus on start.
     *
     * @param debugwindows whether to show the debug menus on start
     */
    public void setDebugwindows(String debugwindows) {
        this.debugwindows = debugwindows;
    }

    /**
     * Sets whether to choose a random background on start.
     *
     * @param randombackground whether to choose a random background on start
     */
    public void setRandombackground(String randombackground) {
        this.randombackground = randombackground;
    }

    /**
     * Sets whether to draw the output border.
     *
     * @param outputborder whether to draw the output border
     */
    public void setOutputborder(String outputborder) {
        this.outputborder = outputborder;
    }

    /**
     * Sets whether to draw the input border.
     *
     * @param inputborder whether to draw the input border
     */
    public void setInputborder(String inputborder) {
        this.inputborder = inputborder;
    }

    /**
     * Sets whether to chime hourly.
     *
     * @param hourlychimes whether to chime hourly
     */
    public void setHourlychimes(String hourlychimes) {
        this.hourlychimes = hourlychimes;
    }

    /**
     * Sets whether to silence errors.
     *
     * @param silenceerrors whether to silence errors
     */
    public void setSilenceerrors(String silenceerrors) {
        this.silenceerrors = silenceerrors;
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
     * @param outputfill whether to fill the output area
     */
    public void setOutputfill(String outputfill) {
        this.outputfill = outputfill;
    }

    /**
     * Sets whether to fill the input field.
     *
     * @param inputfill whether to fill the input field
     */
    public void setInputfill(String inputfill) {
        this.inputfill = inputfill;
    }

    /**
     * Sets whether to show the console clock.
     *
     * @param clockonconsole whether to show the console clock
     */
    public void setClockonconsole(String clockonconsole) {
        this.clockonconsole = clockonconsole;
    }

    /**
     * Sets whether to show seconds on the console clock.
     *
     * @param showseconds whether to show seconds on the console clock
     */
    public void setShowseconds(String showseconds) {
        this.showseconds = showseconds;
    }

    /**
     * Sets whether to filter the chat of any foul language.
     *
     * @param filterchat whether to filter the chat of any foul language
     */
    public void setFilterchat(String filterchat) {
        this.filterchat = filterchat;
    }

    /**
     * Sets the last start time for this user.
     *
     * @param laststart the last start time for this user
     */
    public void setLaststart(String laststart) {
        this.laststart = laststart;
    }

    /**
     * Sets whether to minimize instead of close when the console close button is pressed.
     *
     * @param minimizeonclose whether to minimize instead of close when the console close button is pressed
     */
    public void setMinimizeonclose(String minimizeonclose) {
        this.minimizeonclose = minimizeonclose;
    }

    /**
     * Sets whether to perform typing animations.
     *
     * @param typinganimation whether to perform typing animations
     */
    public void setTypinganimation(String typinganimation) {
        this.typinganimation = typinganimation;
    }

    /**
     * Sets whether to show the busy icon.
     *
     * @param showbusyicon whether to show the busy icon
     */
    public void setShowbusyicon(String showbusyicon) {
        this.showbusyicon = showbusyicon;
    }

    /**
     * Sets whether windows should be rounded.
     *
     * @param roundedwindows whether windows should be rounded
     */
    public void setRoundedwindows(String roundedwindows) {
        this.roundedwindows = roundedwindows;
    }

    /**
     * Sets the window color.
     *
     * @param windowcolor the window color
     */
    public void setWindowcolor(String windowcolor) {
        this.windowcolor = windowcolor;
    }

    /**
     * Sets the console clock format.
     *
     * @param consoleclockformat the console clock format
     */
    public void setConsoleclockformat(String consoleclockformat) {
        this.consoleclockformat = consoleclockformat;
    }

    /**
     * Sets whether to play a typing sound on typing animations calls.
     *
     * @param typingsound whether to play a typing sound on typing animations calls
     */
    public void setTypingsound(String typingsound) {
        this.typingsound = typingsound;
    }

    /**
     * Sets the youtube uuid the procedural finder is at.
     *
     * @param youtubeuuid the youtube uuid the procedural finder is at
     */
    public void setYoutubeuuid(String youtubeuuid) {
        this.youtubeuuid = youtubeuuid;
    }

    /**
     * Sets caps mode.
     *
     * @param capsmode caps mode
     */
    public void setCapsmode(String capsmode) {
        this.capsmode = capsmode;
    }

    /**
     * Sets whether this user is logged in.
     *
     * @param loggedin whether this user is logged in
     */
    public void setLoggedin(String loggedin) {
        this.loggedin = loggedin;
    }

    /**
     * Sets whether to show the audio total length or time remaining for the audio player.
     *
     * @param audiolength whether to show the audio total length or time remaining for the audio player
     */
    public void setAudiolength(String audiolength) {
        this.audiolength = audiolength;
    }

    /**
     * Sets whether to persist notifications.
     *
     * @param persistentnotifications whether to persist notifications
     */
    public void setPersistentnotifications(String persistentnotifications) {
        this.persistentnotifications = persistentnotifications;
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
     * @param wrapshell whether to wrap the native shell
     */
    public void setWrapshell(String wrapshell) {
        this.wrapshell = wrapshell;
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
        if (!StringUtil.in(id, true, UserUtil.getIgnoreUserData())) {
            Logger.log(Logger.Tag.USER_GET, "key = " + id + ", value = " + value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(name, pass);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof User other)) {
            return false;
        }

        return other.getPass().equals(getPass()) && other.getName().equals(getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
