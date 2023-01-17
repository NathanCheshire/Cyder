package cyder.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.enums.Extension;
import cyder.files.FileUtil;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.user.data.MappedExecutable;
import cyder.user.data.ScreenStat;
import cyder.utils.SerializationUtil;

import java.io.File;

/**
 * A user object.
 */
public final class User {
    /**
     * The username.
     */
    private String username;

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * The user's password
     */
    private String password;

    /**
     * Returns the user's password.
     *
     * @return the user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password the user's password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * The name of the font used for the input and output fields.
     */
    private String fontName = "Agency FB";

    /**
     * Returns the name of the font used for the input and output fields.
     *
     * @return the name of the font used for the input and output fields
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Sets the name of the font used for the input and output fields.
     *
     * @param fontName the name of the font used for the input and output fields
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    /**
     * The size of the font used for the input and output fields.
     */
    private int fontSize = 28;

    /**
     * Returns the size of the font used for the input and output fields.
     *
     * @return the size of the font used for the input and output fields
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Sets the size of the font used for the input and output fields.
     *
     * @param fontSize the size of the font used for the input and output fields
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * The hex color code of the foreground of the input and output fields.
     */
    private String foregroundColorHexCode = "F0F0F0";

    /**
     * Returns the hex color code of the foreground of the input and output fields.
     *
     * @return the hex color code of the foreground of the input and output fields
     */
    public String getForegroundColorHexCode() {
        return foregroundColorHexCode;
    }

    /**
     * Sets the hex color code of the foreground of the input and output fields.
     *
     * @param foregroundColorHexCode the hex color code of the foreground of the input and output fields
     */
    public void setForegroundColorHexCode(String foregroundColorHexCode) {
        this.foregroundColorHexCode = foregroundColorHexCode;
    }

    /**
     * The hex color of the background of the input and output fields.
     */
    private String backgroundColorHexCode = "0F0F0F";

    /**
     * Returns the hex color of the background of the input and output fields.
     *
     * @return the hex color of the background of the input and output fields
     */
    public String getBackgroundColorHexCode() {
        return backgroundColorHexCode;
    }

    /**
     * Sets the hex color of the background of the input and output fields.
     *
     * @param backgroundColorHexCode the hex color of the background of the input and output fields
     */
    public void setBackgroundColorHexCode(String backgroundColorHexCode) {
        this.backgroundColorHexCode = backgroundColorHexCode;
    }

    /**
     * Whether to play intro music on user login.
     */
    private boolean introMusic;

    /**
     * Returns whether to play intro music on user login.
     *
     * @return whether to play intro music on user login
     */
    public boolean shouldPlayIntroMusic() {
        return introMusic;
    }

    /**
     * Sets whether to play intro music on user login.
     *
     * @param introMusic whether to play intro music on user login
     */
    public void setIntroMusic(boolean introMusic) {
        this.introMusic = introMusic;
    }

    /**
     * Whether to show the debug windows on user login.
     */
    private boolean debugStats;

    /**
     * Returns whether to show the debug windows on user login.
     *
     * @return whether to show the debug windows on user login
     */
    public boolean shouldShowDebugStatsOnStart() {
        return debugStats;
    }

    /**
     * Sets whether to show the debug windows on user login
     *
     * @param debugStats whether to show the debug windows on user login
     */
    public void setDebugStats(boolean debugStats) {
        this.debugStats = debugStats;
    }

    /**
     * Whether a random background should be chosen on program start.
     */
    private boolean randomBackgroundOnStart;

    /**
     * Returns whether a random background should be chosen on program start.
     *
     * @return whether a random background should be chosen on program start
     */
    public boolean shouldChooseRandomBackgroundOnStart() {
        return randomBackgroundOnStart;
    }

    /**
     * Sets whether a random background should be chosen on program start
     *
     * @param randomBackgroundOnStart whether a random background should be chosen on program start
     */
    public void setRandomBackgroundOnStart(boolean randomBackgroundOnStart) {
        this.randomBackgroundOnStart = randomBackgroundOnStart;
    }

    /**
     * Whether to draw a border on the output area.
     */
    private boolean drawOutputBorder = true;

    /**
     * Returns whether to draw a border on the output area.
     *
     * @return whether to draw a border on the output area
     */
    public boolean shouldDrawOutputBorder() {
        return drawOutputBorder;
    }

    /**
     * Sets whether to draw a border on the output area.
     *
     * @param drawOutputBorder whether to draw a border on the output area
     */
    public void setDrawOutputBorder(boolean drawOutputBorder) {
        this.drawOutputBorder = drawOutputBorder;
    }

    /**
     * Whether to draw a border on the input field.
     */
    private boolean drawInputBorder = true;

    /**
     * Returns whether to draw a border on the input field.
     *
     * @return whether to draw a border on the input field
     */
    public boolean shouldDrawInputBorder() {
        return drawInputBorder;
    }

    /**
     * Sets whether to draw a border on the input field.
     *
     * @param drawInputBorder whether to draw a border on the input field
     */
    public void setDrawInputBorder(boolean drawInputBorder) {
        this.drawInputBorder = drawInputBorder;
    }

    /**
     * Whether to play chimes on the passing of each hour.
     */
    private boolean playHourlyChimes = true;

    /**
     * Returns whether to play chimes on the passing of each hour.
     *
     * @return whether to play chimes on the passing of each hour
     */
    public boolean shouldPlayHourlyChimes() {
        return playHourlyChimes;
    }

    /**
     * Sets whether to play chimes on the passing of each hour.
     *
     * @param playHourlyChimes whether to play chimes on the passing of each hour
     */
    public void setPlayHourlyChimes(boolean playHourlyChimes) {
        this.playHourlyChimes = playHourlyChimes;
    }

    /**
     * Whether error notifications should not be shown.
     */
    private boolean silenceErrors;

    /**
     * Returns whether error notifications should not be shown.
     *
     * @return whether error notifications should not be shown
     */
    public boolean shouldSilenceErrors() {
        return silenceErrors;
    }

    /**
     * Sets whether error notifications should not be shown.
     *
     * @param silenceErrors whether error notifications should not be shown
     */
    public void setSilenceErrors(boolean silenceErrors) {
        this.silenceErrors = silenceErrors;
    }

    /**
     * Whether the program is in fullscreen mode.
     */
    private boolean fullscreen;

    /**
     * Returns whether the program is in fullscreen mode.
     *
     * @return whether the program is in fullscreen mode.
     */
    public boolean isFullscreen() {
        return fullscreen;
    }

    /**
     * Sets whether the program is in fullscreen mode.
     *
     * @param fullscreen whether the program is in fullscreen mode
     */
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    /**
     * Whether to fill the output area.
     */
    private boolean drawOutputFill;

    /**
     * Returns whether to fill the output area.
     *
     * @return whether to fill the output area
     */
    public boolean shouldDrawOutputFill() {
        return drawOutputFill;
    }

    /**
     * Sets whether to fill the output area.
     *
     * @param drawOutputFill whether to fill the output area
     */
    public void setDrawOutputFill(boolean drawOutputFill) {
        this.drawOutputFill = drawOutputFill;
    }

    /**
     * Whether to fill the input field.
     */
    private boolean drawInputFill;

    /**
     * Returns whether to fill the input field.
     *
     * @return whether to fill the input field
     */
    public boolean shouldDrawInputFill() {
        return drawInputFill;
    }

    /**
     * Sets whether to fill the input field.
     *
     * @param drawInputFill whether to fill the input field
     */
    public void setDrawInputFill(boolean drawInputFill) {
        this.drawInputFill = drawInputFill;
    }

    /**
     * Whether to draw a clock on the top of the console.
     */
    private boolean drawConsoleClock = true;

    /**
     * Returns whether to draw a clock on the top of the console.
     *
     * @return whether to draw a clock on the top of the console
     */
    public boolean shouldDrawConsoleClock() {
        return drawConsoleClock;
    }

    /**
     * Sets whether to draw a clock on the top of the console.
     *
     * @param drawConsoleClock whether to draw a clock on the top of the console
     */
    public void setDrawConsoleClock(boolean drawConsoleClock) {
        this.drawConsoleClock = drawConsoleClock;
    }

    /**
     * Whether seconds should be shown on the console clock.
     */
    private boolean showConsoleClockSeconds = true;

    /**
     * Returns whether seconds should be shown on the console clock.
     *
     * @return whether seconds should be shown on the console clock
     */
    public boolean shouldShowConsoleClockSeconds() {
        return showConsoleClockSeconds;
    }

    /**
     * Sets whether seconds should be shown on the console clock.
     *
     * @param showConsoleClockSeconds whether seconds should be shown on the console clock
     */
    public void setShowConsoleClockSeconds(boolean showConsoleClockSeconds) {
        this.showConsoleClockSeconds = showConsoleClockSeconds;
    }

    /**
     * Whether to filter the user input of any blocked words.
     */
    private boolean filterChat = true;

    /**
     * Returns whether to filter the user input of any blocked words.
     *
     * @return whether to filter the user input of any blocked words
     */
    public boolean shouldFilterChat() {
        return filterChat;
    }

    /**
     * Sets whether to filter the user input of any blocked words.
     *
     * @param filterChat whether to filter the user input of any blocked words
     */
    public void setFilterChat(boolean filterChat) {
        this.filterChat = filterChat;
    }

    /**
     * The last time this user was logged in.
     */
    private long lastSessionStart = System.currentTimeMillis();

    /**
     * Returns the last time this user was logged in.
     *
     * @return the last time this user was logged in
     */
    public long getLastSessionStart() {
        return lastSessionStart;
    }

    /**
     * Sets the last time this user was logged in.
     *
     * @param lastSessionStart the last time this user was logged in
     */
    public void setLastSessionStart(long lastSessionStart) {
        this.lastSessionStart = lastSessionStart;
    }

    /**
     * Whether the console should be minimized when the close button is pressed.
     */
    private boolean minimizeOnClose;

    /**
     * Returns whether the console should be minimized when the close button is pressed.
     *
     * @return whether the console should be minimized when the close button is pressed
     */
    public boolean shouldMinimizeOnClose() {
        return minimizeOnClose;
    }

    /**
     * Sets whether the console should be minimized when the close button is pressed.
     *
     * @param minimizeOnClose whether the console should be minimized when the close button is pressed
     */
    public void setMinimizeOnClose(boolean minimizeOnClose) {
        this.minimizeOnClose = minimizeOnClose;
    }

    /**
     * Whether to show the typing animation.
     */
    private boolean typingAnimation = true;

    /**
     * Returns whether to show the typing animation.
     *
     * @return whether to show the typing animation
     */
    public boolean shouldShowTypingAnimation() {
        return typingAnimation;
    }

    /**
     * Sets whether to show the typing animation.
     *
     * @param typingAnimation whether to show the typing animation
     */
    public void setTypingAnimation(boolean typingAnimation) {
        this.typingAnimation = typingAnimation;
    }

    /**
     * Whether to show the busy animation.
     */
    private boolean showBusyAnimation = true;

    /**
     * Returns whether to show the busy animation.
     *
     * @return whether to show the busy animation
     */
    public boolean showShowBusyAnimation() {
        return showBusyAnimation;
    }

    /**
     * Sets whether to show the busy animation.
     *
     * @param showBusyAnimation whether to show the busy animation
     */
    public void setShowBusyAnimation(boolean showBusyAnimation) {
        this.showBusyAnimation = showBusyAnimation;
    }

    /**
     * Whether to draw frame borders rounded.
     */
    private boolean roundedFrameBorders;

    /**
     * Returns whether to draw frame borders rounded.
     *
     * @return whether to draw frame borders rounded
     */
    public boolean shouldDrawRoundedFrameBorders() {
        return roundedFrameBorders;
    }

    /**
     * Sets whether to draw frame borders rounded.
     *
     * @param roundedFrameBorders whether to draw frame borders rounded
     */
    public void setRoundedFrameBorders(boolean roundedFrameBorders) {
        this.roundedFrameBorders = roundedFrameBorders;
    }

    /**
     * The hex color code of the frames.
     */
    private String frameColorHexCode = "0F0F0F";

    /**
     * Returns the color code of the frames.
     *
     * @return the color code of the frames
     */
    public String getFrameColorHexCode() {
        return frameColorHexCode;
    }

    /**
     * Sets the color code of the frames.
     *
     * @param frameColorHexCode the color code of the frames
     */
    public void setFrameColorHexCode(String frameColorHexCode) {
        this.frameColorHexCode = frameColorHexCode;
    }

    /**
     * The console clock date pattern format.
     */
    private String consoleClockFormat;

    /**
     * Returns the console clock date pattern format.
     *
     * @return the console clock date pattern format
     */
    public String getConsoleClockFormat() {
        return consoleClockFormat;
    }

    /**
     * Sets the console clock date pattern format.
     *
     * @param consoleClockFormat the console clock date pattern format
     */
    public void setConsoleClockFormat(String consoleClockFormat) {
        this.consoleClockFormat = consoleClockFormat;
    }

    /**
     * Whether to play a typing sound for typing animation frames.
     */
    private boolean playTypingSound;

    /**
     * Returns whether to play a typing sound for typing animation frames.
     *
     * @return whether to play a typing sound for typing animation frames
     */
    public boolean shouldPlayTypingSound() {
        return playTypingSound;
    }

    /**
     * Sets whether to play a typing sound for typing animation frames.
     *
     * @param playTypingSound whether to play a typing sound for typing animation frames
     */
    public void setPlayTypingSound(boolean playTypingSound) {
        this.playTypingSound = playTypingSound;
    }

    /**
     * The YouTube UUID this user is at for sequential generation.
     */
    private String youtubeUuid = "aaaaaaaaaaa";

    /**
     * Returns the YouTube UUID this user is at for sequential generation.
     *
     * @return the YouTube UUID this user is at for sequential generation
     */
    public String getYoutubeUuid() {
        return youtubeUuid;
    }

    /**
     * Sets the YouTube UUID this user is at for sequential generation.
     *
     * @param youtubeUuid the YouTube UUID this user is at for sequential generation
     */
    public void setYoutubeUuid(String youtubeUuid) {
        this.youtubeUuid = youtubeUuid;
    }

    /**
     * Whether to output all text output using capital letters.
     */
    private boolean capsMode;

    /**
     * Returns whether to output all text output using capital letters.
     *
     * @return whether to output all text output using capital letters
     */
    public boolean isCapsMode() {
        return capsMode;
    }

    /**
     * Sets whether to output all text output using capital letters.
     *
     * @param capsMode whether to output all text output using capital letters
     */
    public void setCapsMode(boolean capsMode) {
        this.capsMode = capsMode;
    }

    /**
     * Whether this user is logged in.
     */
    private boolean loggedIn;

    /**
     * Returns whether this user is logged in.
     *
     * @return whether this user is logged in
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Sets whether this user is logged in.
     *
     * @param loggedIn whether this user is logged in
     */
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    /**
     * Whether to show the audio total length instead of the time remaining for the audio player.
     */
    private boolean showAudioTotalLength;

    /**
     * Returns whether to show the audio total length instead of the time remaining for the audio player.
     *
     * @return whether to show the audio total length instead of the time remaining for the audio player
     */
    public boolean shouldShowAudioTotalLength() {
        return showAudioTotalLength;
    }

    /**
     * Sets whether to show the audio total length instead of the time remaining for the audio player.
     *
     * @param showAudioTotalLength whether to show the audio total length
     *                             instead of the time remaining for the audio player
     */
    public void setShowAudioTotalLength(boolean showAudioTotalLength) {
        this.showAudioTotalLength = showAudioTotalLength;
    }

    /**
     * Whether to persist frame notifications until dismissal.
     */
    private boolean persistNotifications;

    /**
     * Returns whether to persist frame notifications until dismissal.
     *
     * @return whether to persist frame notifications until dismissal
     */
    public boolean shouldPersistNotifications() {
        return persistNotifications;
    }

    /**
     * Sets whether to persist frame notifications until dismissal.
     *
     * @param persistNotifications whether to persist frame notifications until dismissal
     */
    public void setPersistNotifications(boolean persistNotifications) {
        this.persistNotifications = persistNotifications;
    }

    /**
     * Returns whether to perform certain animations.
     */
    private boolean doAnimations = true;

    /**
     * Returns whether whether to perform certain animations.
     *
     * @return whether to perform certain animations
     */
    public boolean shouldDoAnimations() {
        return doAnimations;
    }

    /**
     * Sets whether whether to perform certain animations.
     *
     * @param doAnimations whether to perform certain animations
     */
    public void setDoAnimations(boolean doAnimations) {
        this.doAnimations = doAnimations;
    }

    /**
     * Whether compact text mode is enabled for frame menus and lists.
     */
    private boolean compactTextMode;

    /**
     * Returns whether compact text mode is enabled for frame menus and lists.
     *
     * @return whether compact text mode is enabled for frame menus and lists
     */
    public boolean isCompactTextMode() {
        return compactTextMode;
    }

    /**
     * Sets whether compact text mode is enabled for frame menus and lists.
     *
     * @param compactTextMode whether compact text mode is enabled for frame menus and lists
     */
    public void setCompactTextMode(boolean compactTextMode) {
        this.compactTextMode = compactTextMode;
    }

    /**
     * Whether to pass unknown commands to the native shell.
     */
    private boolean wrapNativeShell;

    /**
     * Returns whether to pass unknown commands to the native shell.
     *
     * @return whether to pass unknown commands to the native shell
     */
    public boolean shouldWrapNativeShell() {
        return wrapNativeShell;
    }

    /**
     * Sets whether to pass unknown commands to the native shell.
     *
     * @param wrapNativeShell whether to pass unknown commands to the native shell
     */
    public void setWrapNativeShell(boolean wrapNativeShell) {
        this.wrapNativeShell = wrapNativeShell;
    }

    /**
     * Whether to draw a background map for the weather widget.
     */
    private boolean drawWeatherMap = true;

    /**
     * Returns whether to draw a background map for the weather widget.
     *
     * @return whether to draw a background map for the weather widget
     */
    public boolean shouldDrawWeatherMap() {
        return drawWeatherMap;
    }

    /**
     * Sets whether to draw a background map for the weather widget.
     *
     * @param drawWeatherMap whether to draw a background map for the weather widget
     */
    public void setDrawWeatherMap(boolean drawWeatherMap) {
        this.drawWeatherMap = drawWeatherMap;
    }

    /**
     * Whether to paint hour labels on the clock widget.
     */
    private boolean paintClockWidgetHourLabels = true;

    /**
     * Returns whether to paint hour labels on the clock widget.
     *
     * @return whether to paint hour labels on the clock widget.
     */
    public boolean shouldPaintClockWidgetHourLabels() {
        return paintClockWidgetHourLabels;
    }

    /**
     * Sets whether to paint hour labels on the clock widget.
     *
     * @param paintClockWidgetHourLabels whether to paint hour labels on the clock widget
     */
    public void setPaintClockWidgetHourLabels(boolean paintClockWidgetHourLabels) {
        this.paintClockWidgetHourLabels = paintClockWidgetHourLabels;
    }

    /**
     * Whether to show the second hand for the clock widget.
     */
    private boolean showClockWidgetSecondHand = true;

    /**
     * Returns whether to show the second hand for the clock widget.
     *
     * @return whether to show the second hand for the clock widget
     */
    public boolean shouldShowClockWidgetSecondHand() {
        return showClockWidgetSecondHand;
    }

    /**
     * Sets whether to show the second hand for the clock widget.
     *
     * @param showClockWidgetSecondHand whether to show the second hand for the clock widget
     */
    public void setShowClockWidgetSecondHand(boolean showClockWidgetSecondHand) {
        this.showClockWidgetSecondHand = showClockWidgetSecondHand;
    }

    /**
     * The screen stat object containing this user's console stats.
     */
    private ScreenStat screenStat = new ScreenStat();

    /**
     * Returns the screen stat object containing this user's console stats.
     *
     * @return the screen stat object containing this user's console stats
     */
    public ScreenStat getScreenStat() {
        return screenStat;
    }

    /**
     * Sets the screen stat object containing this user's console stats.
     *
     * @param screenStat the screen stat object containing this user's console stats
     */
    public void setScreenStat(ScreenStat screenStat) {
        this.screenStat = screenStat;
    }

    /**
     * The list of mapped executables.
     */
    private ImmutableList<MappedExecutable> mappedExecutables = ImmutableList.of();

    /**
     * Returns the list of mapped executables.
     *
     * @return the list of mapped executables
     */
    public ImmutableList<MappedExecutable> getMappedExecutables() {
        return mappedExecutables;
    }

    /**
     * Sets the list of mapped executables.
     *
     * @param mappedExecutables the list of mapped executables
     */
    public void setMappedExecutables(ImmutableList<MappedExecutable> mappedExecutables) {
        this.mappedExecutables = mappedExecutables;
    }

    /**
     * The opacity of the input and output fills.
     */
    private int fillOpacity; // todo use me

    /**
     * Returns the opacity of the input and output fills.
     *
     * @return the opacity of the input and output fills
     */
    public int getFillOpacity() {
        return fillOpacity;
    }

    /**
     * Sets the opacity of the input and output fills.
     *
     * @param fillOpacity the opacity of the input and output fills
     */
    public void setFillOpacity(int fillOpacity) {
        this.fillOpacity = fillOpacity;
    }

    /**
     * Whether the welcome message has been shown for this user.
     */
    private boolean shownWelcomeMessage; // todo use me

    /**
     * Returns whether the welcome message has been shown for this user.
     *
     * @return whether the welcome message has been shown for this user
     */
    public boolean hasShownWelcomeMessage() {
        return shownWelcomeMessage;
    }

    /**
     * Sets the the welcome message has been shown for this user.
     *
     * @param shownWelcomeMessage whether the welcome message has been shown for this user
     */
    public void setShownWelcomeMessage(boolean shownWelcomeMessage) {
        this.shownWelcomeMessage = shownWelcomeMessage;
    }

    /**
     * The time at which this user was created.
     */
    private long accountCreationTime; // todo use me

    /**
     * Returns the time at which this user was created.
     *
     * @return the time at which this user was created
     */
    public long getAccountCreationTime() {
        return accountCreationTime;
    }

    /**
     * Sets the time at which this user was created.
     *
     * @param accountCreationTime the time at which this user was created
     */
    public void setAccountCreationTime(long accountCreationTime) {
        this.accountCreationTime = accountCreationTime;
    }

    /**
     * Constructs a new user object.
     */
    public User() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Converts this user to json format.
     *
     * @return this user in json format
     */
    public String toJson() {
        return SerializationUtil.toJson(this);
    }

    /**
     * Serializes and returns a user object from the provided json.
     *
     * @param jsonMessage the json message
     * @return a user object from the provided json
     */
    public static User fromJson(String jsonMessage) {
        Preconditions.checkNotNull(jsonMessage);
        Preconditions.checkArgument(!jsonMessage.isEmpty());

        return SerializationUtil.fromJson(jsonMessage, User.class);
    }

    /**
     * Serializes and returns a user object from the json contained in the provided file.
     *
     * @param file the file
     * @return a user object from the provided file
     */
    public static User fromJson(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.JSON.getExtension()));

        return SerializationUtil.fromJson(file, User.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof User)) {
            return false;
        }

        User other = (User) o;
        return username.equals(other.username)
                && password.equals(other.getPassword());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = username.hashCode();
        ret = 31 * ret + password.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NewUser{"
                + "username=\"" + username + "\""
                + ", password=\"" + password + "\""
                + ", fontName=\"" + fontName + "\""
                + ", fontSize=" + fontSize
                + ", foregroundColorHexCode=\"" + foregroundColorHexCode + "\""
                + ", backgroundColorHexCode=\"" + backgroundColorHexCode + "\""
                + ", introMusic=" + introMusic
                + ", debugStats=" + debugStats
                + ", randomBackgroundOnStart=" + randomBackgroundOnStart
                + ", drawOutputBorder=" + drawOutputBorder
                + ", drawInputBorder=" + drawInputBorder
                + ", playHourlyChimes=" + playHourlyChimes
                + ", silenceErrors=" + silenceErrors
                + ", fullscreen=" + fullscreen
                + ", drawOutputFill=" + drawOutputFill
                + ", drawInputFill=" + drawInputFill
                + ", drawConsoleClock=" + drawConsoleClock
                + ", showConsoleClockSeconds=" + showConsoleClockSeconds
                + ", filterChat=" + filterChat
                + ", lastSessionStart=" + lastSessionStart
                + ", minimizeOnClose=" + minimizeOnClose
                + ", typingAnimation=" + typingAnimation
                + ", showBusyAnimation=" + showBusyAnimation
                + ", roundedFrameBorders=" + roundedFrameBorders
                + ", frameColorHexCode=\"" + frameColorHexCode + "\""
                + ", consoleClockFormat=\"" + consoleClockFormat + "\""
                + ", playTypingSound=" + playTypingSound
                + ", youtubeUuid=\"" + youtubeUuid + "\""
                + ", capsMode=" + capsMode
                + ", loggedIn=" + loggedIn
                + ", showAudioTotalLength=" + showAudioTotalLength
                + ", persistNotifications=" + persistNotifications
                + ", doAnimations=" + doAnimations
                + ", compactTextMode=" + compactTextMode
                + ", wrapNativeShell=" + wrapNativeShell
                + ", drawWeatherMap=" + drawWeatherMap
                + ", paintClockWidgetHourLabels=" + paintClockWidgetHourLabels
                + ", showClockWidgetSecondHand=" + showClockWidgetSecondHand
                + ", screenStat=" + screenStat
                + ", mappedExecutables=" + mappedExecutables
                + ", fillOpacity=" + fillOpacity
                + ", shownWelcomeMessage=" + shownWelcomeMessage
                + ", accountCreationTime=" + accountCreationTime
                + "}";
    }
}
