package cyder.user;

import com.google.common.base.Preconditions;
import cyder.enums.Dynamic;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
import cyder.strings.StringUtil;
import cyder.utils.ColorUtil;
import cyder.utils.FontUtil;
import cyder.utils.SerializationUtil;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
}
