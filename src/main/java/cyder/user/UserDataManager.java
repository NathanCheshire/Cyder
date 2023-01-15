package cyder.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.Dynamic;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
import cyder.strings.StringUtil;
import cyder.user.data.MappedExecutable;
import cyder.user.data.ScreenStat;
import cyder.utils.ColorUtil;
import cyder.utils.FontUtil;
import cyder.utils.SerializationUtil;
import cyder.youtube.YouTubeConstants;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

/**
 * A managed for the current {@link NewUser}.
 * The current Cyder user is not exposed but instead proxied by this manager
 * for purposes of encapsulation, validation, and convenience methods.
 */
public enum UserDataManager {
    /**
     * The user data manager instance.
     */
    INSTANCE;

    /**
     * The current user object this manager is being a proxy for.
     */
    private NewUser user;

    /**
     * The file the current user object is written to periodically and on program closure.
     */
    private File userFile;

    /**
     * Sets the current Cyder user to the user with the provided uuid.
     *
     * @param uuid the uuid of the Cyder user to set for the current session
     * @throws NullPointerException     if the uuid is null
     * @throws IllegalArgumentException if the uuid is empty, the user json does not exist, or the manager
     *                                  is current initialized
     */
    public synchronized void initialize(String uuid) {
        Preconditions.checkState(!isInitialized());
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(!uuid.isEmpty());

        File jsonFile = Dynamic.buildDynamic(Dynamic.USERS.getFileName(), uuid, UserFile.USERDATA.getName());
        Preconditions.checkArgument(jsonFile.exists());

        userFile = jsonFile;
        user = NewUser.fromJson(jsonFile);
    }

    /**
     * Serializes the current Cyder user to the {@link #userFile}, after which the user and user file are set to null
     * allowing for the {@link #initialize(String)} method to be invoked again.
     *
     * @throws IOException if an IO error occurs when writing the current user to the user file
     */
    public synchronized void removeManagement() throws IOException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(userFile));
            SerializationUtil.toJson(user, writer);
        } catch (Exception e) {
            throw new IOException("Failed to write current user to file. Exception: " + e.getMessage());
        }

        userFile = null;
        user = null;
    }

    /**
     * Returns whether this manager is initialized with a user and userfile to manage.
     *
     * @return whether this manager is initialized with a user and userfile to manage
     */
    public synchronized boolean isInitialized() {
        boolean userPresent = user != null;
        boolean filePresent = userFile != null && userFile.exists();

        return userPresent && filePresent;
    }

    /**
     * Creates a new user object with default parameters and the provided username and password.
     *
     * @param username the username
     * @param password the hashed password
     * @return the new user object
     */
    public NewUser createNewUser(String username, String password) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);
        Preconditions.checkArgument(!username.isEmpty());
        Preconditions.checkArgument(!password.isEmpty());
        Preconditions.checkArgument(!UserUtil.usernameInUse(username));

        NewUser newUser = new NewUser();
        newUser.setUsername(username);
        newUser.setPassword(password);

        return newUser;
    }

    /**
     * A common method that should be invoked on all accessor methods contained within
     * this data manger.
     *
     * @param dataId the id of the data being accessed
     */
    private void getterInvoked(String dataId) {
        Preconditions.checkNotNull(dataId);
        Preconditions.checkArgument(!dataId.isEmpty());

        if (!StringUtil.in(dataId, true, UserUtil.getIgnoreUserData())) {
            Logger.log(LogTag.USER_GET, "key: " + dataId);
        }
    }

    // -----------------------------
    // Proxy methods for user object
    // -----------------------------

    /**
     * Returns the name of the current user.
     *
     * @return the name of the current user
     */
    public synchronized String getUsername() {
        Preconditions.checkState(isInitialized());

        getterInvoked("username");
        return user.getUsername();
    }

    /**
     * Sets the username of the current user.
     *
     * @param username the new requested username
     */
    public synchronized void setUsername(String username) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkNotNull(username);
        Preconditions.checkArgument(!username.isEmpty());
        Preconditions.checkArgument(!UserUtil.usernameInUse(username));

        user.setUsername(username);
    }

    /**
     * Returns the password of the current user.
     *
     * @return the password of the current user
     */
    public synchronized String getPassword() {
        Preconditions.checkState(isInitialized());

        getterInvoked("password");
        return user.getPassword();
    }

    /**
     * Sets the password of the current user.
     *
     * @param password the hashed password of the current user
     */
    public synchronized void setPassword(String password) {
        Preconditions.checkNotNull(password);
        Preconditions.checkArgument(!password.isEmpty());

        user.setPassword(password);
    }

    /**
     * Returns the name of the user font.
     *
     * @return the name of the user font
     */
    public synchronized String getFontName() {
        Preconditions.checkState(isInitialized());

        getterInvoked("font_name");
        return user.getFontName();
    }

    /**
     * Sets the name of the user font.
     *
     * @param fontName the name of the user font
     */
    public synchronized void setFontName(String fontName) {
        Preconditions.checkNotNull(fontName);
        Preconditions.checkArgument(!fontName.isEmpty());

        user.setFontName(fontName);
    }

    /**
     * Returns the font size for the user font.
     *
     * @return the font size for the user
     */
    public synchronized int getFontSize() {
        Preconditions.checkState(isInitialized());

        getterInvoked("font_size");
        return user.getFontSize();
    }

    /**
     * Sets the font size for the user.
     *
     * @param fontSize the font size for the user
     */
    public synchronized void setFontSize(int fontSize) {
        Preconditions.checkArgument(fontSize >= Props.minFontSize.getValue());
        Preconditions.checkArgument(fontSize <= Props.maxFontSize.getValue());

        user.setFontSize(fontSize);
    }

    /**
     * Returns the user's font metric.
     *
     * @return the user's font metric
     */
    public synchronized int getFontMetric() {
        Preconditions.checkState(isInitialized());

        getterInvoked("font_metric");
        return user.getFontMetric();
    }

    /**
     * Sets the user's font metric.
     *
     * @param fontMetric the user's font metric
     */
    public synchronized void setFontMetric(int fontMetric) {
        Preconditions.checkArgument(FontUtil.isValidFontMetric(fontMetric));

        user.setFontMetric(fontMetric);
    }

    /**
     * Returns the user's foreground color.
     *
     * @return the user's foreground color
     */
    public synchronized Color getForegroundColorHexCode() {
        Preconditions.checkState(isInitialized());

        getterInvoked("foreground_color");
        return ColorUtil.hexStringToColor(user.getForegroundColorHexCode());
    }

    /**
     * Sets the user's foreground color.
     *
     * @param color the user's foreground color
     */
    public synchronized void setForegroundColor(Color color) {
        Preconditions.checkNotNull(color);

        user.setForegroundColorHexCode(ColorUtil.toRgbHexString(color));
    }

    /**
     * Returns the user's background color.
     *
     * @return the user's background color
     */
    public synchronized Color getBackgroundColor() {
        Preconditions.checkState(isInitialized());

        getterInvoked("background_color");
        return ColorUtil.hexStringToColor(user.getBackgroundColorHexCode());
    }

    /**
     * Sets the user's background color.
     *
     * @param color the user's background color
     */
    public synchronized void setBackgroundColor(Color color) {
        Preconditions.checkNotNull(color);

        user.setBackgroundColorHexCode(ColorUtil.toRgbHexString(color));
    }

    /**
     * Returns whether intro music should be played on user login.
     *
     * @return whether intro music should be played on user login
     */
    public synchronized boolean shouldPlayIntroMusic() {
        Preconditions.checkState(isInitialized());

        getterInvoked("intro_music");
        return user.shouldPlayIntroMusic();
    }

    /**
     * Sets whether intro music should be played on user login.
     *
     * @param shouldPlay whether intro music should be played on user login
     */
    public synchronized void setShouldPlayIntroMusic(boolean shouldPlay) {
        user.setIntroMusic(shouldPlay);
    }

    /**
     * Returns whether debug stats should be shown on initial console load.
     *
     * @return whether debug stats should be shown on initial console load
     */
    public synchronized boolean shouldShowDebugStats() {
        Preconditions.checkState(isInitialized());

        getterInvoked("debug_stats");
        return user.shouldShowDebugStatsOnStart();
    }

    /**
     * Sets whether debug stats should be shown on initial console load.
     *
     * @param shouldShowDebugStats whether debug stats should be shown on initial console load
     */
    public synchronized void setShouldShowDebugStats(boolean shouldShowDebugStats) {
        user.setDebugStats(shouldShowDebugStats);
    }

    /**
     * Returns whether a random background should be chosen on start for the console.
     *
     * @return whether a random background should be chosen on start for the console
     */
    public synchronized boolean shouldChooseRandomBackground() {
        Preconditions.checkState(isInitialized());

        getterInvoked("random_background");
        return user.shouldChooseRandomBackgroundOnStart();
    }

    /**
     * Sets whether a random background should be chosen on start for the console.
     *
     * @param shouldChooseRandomBackground whether a random background should be chosen on start for the console
     */
    public synchronized void setShouldChooseRandomBackground(boolean shouldChooseRandomBackground) {
        user.setRandomBackgroundOnStart(shouldChooseRandomBackground);
    }

    /**
     * Returns whether a border should be drawn around the input field.
     *
     * @return whether a border should be drawn around the input field
     */
    public synchronized boolean shouldDrawInputBorder() {
        Preconditions.checkState(isInitialized());

        getterInvoked("input_border");
        return user.shouldDrawInputBorder();
    }

    /**
     * Sets whether a border should be drawn around the input field.
     *
     * @param shouldDrawInputBorder whether a border should be drawn around the input field
     */
    public synchronized void setShouldDrawInputBorder(boolean shouldDrawInputBorder) {
        user.setDrawInputBorder(shouldDrawInputBorder);
    }

    /**
     * Returns whether a border should be drawn around the output area.
     *
     * @return whether a border should be drawn around the output area
     */
    public synchronized boolean shouldDrawOutputBorder() {
        Preconditions.checkState(isInitialized());

        getterInvoked("output_border");
        return user.shouldDrawOutputBorder();
    }

    /**
     * Sets whether a border should be drawn around the output area.
     *
     * @param shouldDrawOutputBorder whether a border should be drawn around the output area
     */
    public synchronized void setShouldDrawOutputBorder(boolean shouldDrawOutputBorder) {
        user.setDrawOutputBorder(shouldDrawOutputBorder);
    }

    /**
     * Returns whether hourly chimes should be played.
     *
     * @return whether hourly chimes should be played
     */
    public synchronized boolean shouldPlayHourlyChimes() {
        Preconditions.checkState(isInitialized());

        getterInvoked("hourly_chimes");
        return user.shouldPlayHourlyChimes();
    }

    /**
     * Sets whether hourly chimes should be played.
     *
     * @param shouldPlayHourlyChimes whether hourly chimes should be played
     */
    public synchronized void setShouldPlayHourlyChimes(boolean shouldPlayHourlyChimes) {
        user.setPlayHourlyChimes(shouldPlayHourlyChimes);
    }

    /**
     * Returns whether error notifications should be silenced.
     *
     * @return whether error notifications should be silenced
     */
    public synchronized boolean shouldSilenceErrors() {
        Preconditions.checkState(isInitialized());

        getterInvoked("silence_errors");
        return user.shouldSilenceErrors();
    }

    /**
     * Sets whether error notifications should be silenced.
     *
     * @param shouldSilenceErrors whether error notifications should be silenced
     */
    public synchronized void setShouldSilenceErrors(boolean shouldSilenceErrors) {
        user.setSilenceErrors(shouldSilenceErrors);
    }

    /**
     * Returns whether the program should be shown in fullscreen mode.
     *
     * @return whether the program should be shown in fullscreen mode
     */
    public synchronized boolean isFullscreen() {
        Preconditions.checkState(isInitialized());

        getterInvoked("fullscreen");
        return user.isFullscreen();
    }

    /**
     * Sets whether the program should be shown in fullscreen mode.
     *
     * @param fullscreen whether the program should be shown in fullscreen mode
     */
    public synchronized void setFullscreen(boolean fullscreen) {
        user.setFullscreen(fullscreen);
    }

    /**
     * Returns whether the output area should be filled.
     *
     * @return whether the output area should be filled
     */
    public synchronized boolean shouldDrawOutputFill() {
        Preconditions.checkState(isInitialized());

        getterInvoked("output_fill");
        return user.shouldDrawOutputFill();
    }

    /**
     * Sets whether the output area should be filled.
     *
     * @param shouldDrawOutputFill whether the output area should be filled
     */
    public synchronized void setShouldDrawOutputFill(boolean shouldDrawOutputFill) {
        user.setDrawOutputFill(shouldDrawOutputFill);
    }

    /**
     * Returns whether the input field should be filled.
     *
     * @return whether the input field should be filled
     */
    public synchronized boolean shouldDrawInputFill() {
        Preconditions.checkState(isInitialized());

        getterInvoked("input_fill");
        return user.shouldDrawInputFill();
    }

    /**
     * Sets whether the input field should be filled.
     *
     * @param shouldDrawInputFill whether the input field should be filled
     */
    public synchronized void setShouldDrawInputFill(boolean shouldDrawInputFill) {
        user.setDrawInputFill(shouldDrawInputFill);
    }

    /**
     * Returns whether the console clock should be drawn.
     *
     * @return whether the console clock should be drawn
     */
    public synchronized boolean shouldDrawConsoleClock() {
        Preconditions.checkState(isInitialized());

        getterInvoked("console_clock");
        return user.shouldDrawConsoleClock();
    }

    /**
     * Sets whether the console clock should be drawn.
     *
     * @param shouldDrawConsoleClock whether the console clock should be drawn
     */
    public synchronized void setShouldDrawConsoleClock(boolean shouldDrawConsoleClock) {
        user.setDrawConsoleClock(shouldDrawConsoleClock);
    }

    /**
     * Returns whether seconds should be shown on the console clock.
     *
     * @return whether seconds should be shown on the console clock
     */
    public synchronized boolean shouldShowConsoleClockSeconds() {
        Preconditions.checkState(isInitialized());

        getterInvoked("console_clock_seconds");
        return user.shouldShowConsoleClockSeconds();
    }

    /**
     * Sets whether seconds should be shown on the console clock.
     *
     * @param shouldShowConsoleClockSeconds whether seconds should be shown on the console clock
     */
    public synchronized void setShouldShowConsoleClockSeconds(boolean shouldShowConsoleClockSeconds) {
        user.setShowConsoleClockSeconds(shouldShowConsoleClockSeconds);
    }

    /**
     * Returns whether user input should be filtered.
     *
     * @return whether user input should be filtered
     */
    public synchronized boolean shouldFilterchat() {
        Preconditions.checkState(isInitialized());

        getterInvoked("filter_chat");
        return user.shouldFilterChat();
    }

    /**
     * Sets whether user input should be filtered.
     *
     * @param shouldFilterChat whether user input should be filtered
     */
    public synchronized void setShouldFilterChat(boolean shouldFilterChat) {
        user.setFilterChat(shouldFilterChat);
    }

    /**
     * Returns the time at which this user's last session started.
     *
     * @return the time at which this user's last session started
     */
    public synchronized long getLastSessionStart() {
        Preconditions.checkState(isInitialized());

        getterInvoked("last_session_start");
        return user.getLastSessionStart();
    }

    /**
     * Sets the time at which this user's last session started.
     *
     * @param lastSessionStart the time at which this user's last session started
     */
    public synchronized void setLastSessionStart(long lastSessionStart) {
        user.setLastSessionStart(lastSessionStart);
    }

    /**
     * Returns whether the program should be minimized on console close.
     *
     * @return whether the program should be minimized on console close
     */
    public synchronized boolean shouldMinimizeOnClose() {
        Preconditions.checkState(isInitialized());

        getterInvoked("minimize_on_close");
        return user.shouldMinimizeOnClose();
    }

    /**
     * Sets whether the program should be minimized on console close.
     *
     * @param shouldMinimizeOnClose whether the program should be minimized on console close
     */
    public synchronized void setShouldMinimizeOnClose(boolean shouldMinimizeOnClose) {
        user.setMinimizeOnClose(shouldMinimizeOnClose);
    }

    /**
     * Returns whether the typing animation should be shown.
     *
     * @return whether the typing animation should be shown
     */
    public synchronized boolean shouldShowTypingAnimation() {
        Preconditions.checkState(isInitialized());

        getterInvoked("typing_animation");
        return user.shouldShowTypingAnimation();
    }

    /**
     * Sets whether the typing animation should be shown.
     *
     * @param shouldShowTypingAnimation whether the typing animation should be shown
     */
    public synchronized void setShouldShowTypingAnimation(boolean shouldShowTypingAnimation) {
        user.setTypingAnimation(shouldShowTypingAnimation);
    }

    /**
     * Returns whether the busy animation should be shown.
     *
     * @return whether the busy animation should be shown
     */
    public synchronized boolean shouldShowBusyAnimation() {
        Preconditions.checkState(isInitialized());

        getterInvoked("busy_animation");
        return user.showShowBusyAnimation();
    }

    /**
     * Sets whether the busy animation should be shown.
     *
     * @param shouldShowBusyAnimation whether the busy animation should be shown
     */
    public synchronized void setShouldShowBusyAnimation(boolean shouldShowBusyAnimation) {
        user.setShowBusyAnimation(shouldShowBusyAnimation);
    }

    /**
     * Returns whether frames should be drawn with rounded borders.
     *
     * @return whether frames should be drawn with rounded borders
     */
    public synchronized boolean shouldDrawRoundedFrameBorders() {
        Preconditions.checkState(isInitialized());

        getterInvoked("rounded_frame_borders");
        return user.shouldDrawRoundedFrameBorders();
    }

    /**
     * Sets whether frames should be drawn with rounded borders.
     *
     * @param shouldDrawRoundedFrameBorders whether frames should be drawn with rounded borders
     */
    public synchronized void setShouldDrawRoundedFrameBorders(boolean shouldDrawRoundedFrameBorders) {
        user.setRoundedFrameBorders(shouldDrawRoundedFrameBorders);
    }

    /**
     * Returns the frame color.
     *
     * @return the frame color
     */
    public synchronized Color getFrameColor() {
        Preconditions.checkState(isInitialized());

        getterInvoked("frame_color");
        return ColorUtil.hexStringToColor(user.getFrameColorHexCode());
    }

    /**
     * Sets the frame color.
     *
     * @param color the frame color
     */
    public synchronized void setFrameColor(Color color) {
        Preconditions.checkNotNull(color);

        user.setFrameColorHexCode(ColorUtil.toRgbHexString(color));
    }

    /**
     * Returns the console clock format.
     *
     * @return the console clock format
     */
    public synchronized String getConsoleClockFormat() {
        Preconditions.checkState(isInitialized());

        getterInvoked("clock_format");
        return user.getConsoleClockFormat();
    }

    /**
     * Sets the console clock format.
     *
     * @param clockFormat the console clock format
     */
    public synchronized void setConsoleClockFormat(String clockFormat) {
        Preconditions.checkNotNull(clockFormat);
        Preconditions.checkArgument(!clockFormat.isEmpty());
        Preconditions.checkArgument(UserEditor.validateDatePattern(clockFormat));

        user.setConsoleClockFormat(clockFormat);
    }

    /**
     * Returns whether a typing sound should be played when the typing animation is on-going.
     *
     * @return whether a typing sound should be played when the typing animation is on-going
     */
    public synchronized boolean shouldPlayTypingSound() {
        Preconditions.checkState(isInitialized());

        getterInvoked("typing_sound");
        return user.shouldPlayTypingSound();
    }

    /**
     * Sets whether a typing sound should be played when the typing animation is on-going.
     *
     * @param shouldPlayTypingSound whether a typing sound should be played when the typing animation is on-going
     */
    public synchronized void setShouldPlayTypingSound(boolean shouldPlayTypingSound) {
        user.setPlayTypingSound(shouldPlayTypingSound);
    }

    /**
     * Returns the YouTube uuid this user is at in the random generation cycle.
     *
     * @return the YouTube uuid this user is at in the random generation cycle
     */
    public synchronized String getYouTubeUuid() {
        Preconditions.checkState(isInitialized());

        getterInvoked("youtube_uuid");
        return user.getYoutubeUuid();
    }

    /**
     * Sets the YouTube uuid this user is at in the random generation cycle.
     *
     * @param youTubeUuid the YouTube uuid this user is at in the random generation cycle
     */
    public synchronized void setYouTubeUuid(String youTubeUuid) {
        Preconditions.checkNotNull(youTubeUuid);
        Preconditions.checkArgument(youTubeUuid.length() == YouTubeConstants.UUID_LENGTH);
        Preconditions.checkArgument(CyderRegexPatterns.youTubeUuidPattern.matcher(youTubeUuid).matches());

        user.setYoutubeUuid(youTubeUuid);
    }

    /**
     * Returns whether caps mode should be enabled.
     *
     * @return whether caps mode should be enabled
     */
    public synchronized boolean isCapsMode() {
        Preconditions.checkState(isInitialized());

        getterInvoked("caps_mode");
        return user.isCapsMode();
    }

    /**
     * Sets whether caps mode should be enabled.
     *
     * @param capsMode whether caps mode should be enabled
     */
    public synchronized void setCapsMode(boolean capsMode) {
        user.setCapsMode(capsMode);
    }

    /**
     * Returns whether this user is logged in.
     *
     * @return whether this user is logged in
     */
    public synchronized boolean isLoggedIn() {
        Preconditions.checkState(isInitialized());

        getterInvoked("logged_in");
        return user.isLoggedIn();
    }

    /**
     * Sets whether this user is logged in.
     *
     * @param loggedIn whether this user is logged in
     */
    public synchronized void setLoggedIn(boolean loggedIn) {
        user.setLoggedIn(loggedIn);
    }

    /**
     * Returns whether the audio total length should be shown instead of the remaining time for the audio player.
     *
     * @return whether the audio total length should be shown instead of the remaining time for the audio player
     */
    public synchronized boolean shouldShowAudioTotalLength() {
        Preconditions.checkState(isInitialized());

        getterInvoked("audio_total_length");
        return user.shouldShowAudioTotalLength();
    }

    /**
     * Sets whether the audio total length should be shown instead of the remaining time for the audio player.
     *
     * @param shouldShowAudioTotalLength whether the audio total length should
     *                                   be shown instead of the remaining time for the audio player
     */
    public synchronized void setShouldShowAudioTotalLength(boolean shouldShowAudioTotalLength) {
        user.setShowAudioTotalLength(shouldShowAudioTotalLength);
    }

    /**
     * Returns whether notifications should be persisted until user dismissal.
     *
     * @return whether notifications should be persisted until user dismissal
     */
    public synchronized boolean shouldPersistNotifications() {
        Preconditions.checkState(isInitialized());

        getterInvoked("should_persist_notifications");
        return user.shouldPersistNotifications();
    }

    /**
     * Sets whether notifications should be persisted until user dismissal.
     *
     * @param shouldPersistNotifications whether notifications should be persisted until user dismissal
     */
    public synchronized void setShouldPersistNotifications(boolean shouldPersistNotifications) {
        user.setPersistNotifications(shouldPersistNotifications);
    }

    /**
     * Returns whether certain animations should be performed.
     *
     * @return whether certain animations should be performed
     */
    public synchronized boolean shouldDoAnimations() {
        Preconditions.checkState(isInitialized());

        getterInvoked("should_do_animations");
        return user.shouldDoAnimations();
    }

    /**
     * Sets whether certain animations should be performed.
     *
     * @param shouldDoAnimations whether certain animations should be performed
     */
    public synchronized void setShouldDoAnimations(boolean shouldDoAnimations) {
        user.setDoAnimations(shouldDoAnimations);
    }

    /**
     * Returns whether compact text mode is enabled.
     *
     * @return whether compact text mode is enabled
     */
    public synchronized boolean compactTextMode() {
        Preconditions.checkState(isInitialized());

        getterInvoked("compact_text_mode");
        return user.isCompactTextMode();
    }

    /**
     * Sets whether compact text mode is enabled.
     *
     * @param compactTextMode whether compact text mode is enabled
     */
    public synchronized void compactTextModeEnabled(boolean compactTextMode) {
        user.setCompactTextMode(compactTextMode);
    }

    /**
     * Returns whether unknown user commands should be passed to the native shell.
     *
     * @return whether unknown user commands should be passed to the native shell
     */
    public synchronized boolean shouldWrapShell() {
        Preconditions.checkState(isInitialized());

        getterInvoked("wrap_shell");
        return user.shouldWrapNativeShell();
    }

    /**
     * Sets whether unknown user commands should be passed to the native shell.
     *
     * @param wrapShell whether unknown user commands should be passed to the native shell
     */
    public synchronized void setWrapShell(boolean wrapShell) {
        user.setWrapNativeShell(wrapShell);
    }

    /**
     * Returns whether a map should be displayed as the background of the weather widget.
     *
     * @return whether a map should be displayed as the background of the weather widget
     */
    public synchronized boolean shouldDrawWeatherMap() {
        Preconditions.checkState(isInitialized());

        getterInvoked("draw_weather_map");
        return user.shouldDrawWeatherMap();
    }

    /**
     * Sets whether a map should be displayed as the background of the weather widget.
     *
     * @param shouldDrawWeatherMap whether a map should be displayed as the background of the weather widget
     */
    public synchronized void setShouldDrawWeatherMap(boolean shouldDrawWeatherMap) {
        user.setDrawWeatherMap(shouldDrawWeatherMap);
    }

    /**
     * Returns whether the hour labels should be painted for the clock widget.
     *
     * @return whether the hour labels should be painted for the clock widget
     */
    public synchronized boolean shouldPaintClockHourLabels() {
        Preconditions.checkState(isInitialized());

        getterInvoked("paint_clock_labels");
        return user.shouldPaintClockWidgetHourLabels();
    }

    /**
     * Sets whether the hour labels should be painted for the clock widget.
     *
     * @param shouldPaintClockHourLabels whether the hour labels should be painted for the clock widget
     */
    public synchronized void setShouldPaintClockHourLabels(boolean shouldPaintClockHourLabels) {
        user.setPaintClockWidgetHourLabels(shouldPaintClockHourLabels);
    }

    /**
     * Returns whether the second hand should be shown on the clock widget.
     *
     * @return whether the second hand should be shown on the clock widget
     */
    public synchronized boolean shouldShowClockWidgetSecondHand() {
        Preconditions.checkState(isInitialized());

        getterInvoked("clock_widget_second_hand");
        return user.shouldShowClockWidgetSecondHand();
    }

    /**
     * Sets whether the second hand should be shown on the clock widget.
     *
     * @param shouldShowClockWidgetSecondHand whether the second hand should be shown on the clock widget
     */
    public synchronized void setShouldShowClockWidgetSecondHand(boolean shouldShowClockWidgetSecondHand) {
        user.setShowClockWidgetSecondHand(shouldShowClockWidgetSecondHand);
    }

    /**
     * Returns the current screen stat for this user.
     *
     * @return the current screen stat for this user
     */
    public synchronized ScreenStat getScreenStat() {
        Preconditions.checkState(isInitialized());

        getterInvoked("screen_stat");
        return user.getScreenStat();
    }

    /**
     * Sets the current screen stat for this user.
     *
     * @param screenStat the current screen stat for this user
     */
    public synchronized void setScreenStat(ScreenStat screenStat) {
        Preconditions.checkNotNull(screenStat);

        user.setScreenStat(screenStat);
    }

    /**
     * Returns the mapped executables for this user.
     *
     * @return the mapped executables for this user
     */
    public synchronized ImmutableList<MappedExecutable> getMappedExecutables() {
        Preconditions.checkState(isInitialized());

        getterInvoked("mapped_executables");
        return user.getMappedExecutables();
    }

    /**
     * Sets the mapped executables for this user.
     *
     * @param mappedExecutables the mapped executables for this user
     */
    public synchronized void setMappedExecutables(Collection<MappedExecutable> mappedExecutables) {
        Preconditions.checkNotNull(mappedExecutables);

        user.setMappedExecutables(ImmutableList.copyOf(mappedExecutables));
    }

    /**
     * Returns the fill opacity.
     *
     * @return the fill opacity
     */
    public synchronized int getFillOpacity() {
        Preconditions.checkState(isInitialized());

        getterInvoked("fill_opacity");
        return user.getFillOpacity();
    }

    /**
     * Sets the the fill opacity.
     *
     * @param fillOpacity the fill opacity
     */
    public synchronized void setFillOpacity(int fillOpacity) {
        Preconditions.checkArgument(fillOpacity >= 0 && fillOpacity <= 255);

        user.setFillOpacity(fillOpacity);
    }

    /**
     * Returns whether the welcome message has been shown.
     *
     * @return whether the welcome message has been shown
     */
    public synchronized boolean hasShownWelcomeMessage() {
        Preconditions.checkState(isInitialized());

        getterInvoked("shown_welcome_message");
        return user.hasShownWelcomeMessage();
    }

    /**
     * Sets whether the welcome message has been shown.
     *
     * @param shownWelcomeMessage whether the welcome message has been shown
     */
    public synchronized void setShownWelcomeMessage(boolean shownWelcomeMessage) {
        user.setShownWelcomeMessage(shownWelcomeMessage);
    }

    /**
     * Returns the time at which this account was created.
     *
     * @return the time at which this account was created
     */
    public synchronized long getAccountCreationTime() {
        Preconditions.checkState(isInitialized());

        getterInvoked("account_creation_time");
        return user.getAccountCreationTime();
    }

    /**
     * Sets the time at which this account was created.
     *
     * @param accountCreationTime the time at which this account was created
     */
    public synchronized void setAccountCreationTime(long accountCreationTime) {
        user.setAccountCreationTime(accountCreationTime);
    }
}
