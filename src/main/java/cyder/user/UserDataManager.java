package cyder.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.Dynamic;
import cyder.exceptions.FatalException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.logging.LoggingUtil;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.LevenshteinUtil;
import cyder.threads.CyderThreadFactory;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.user.data.MappedExecutable;
import cyder.user.data.ScreenStat;
import cyder.utils.ColorUtil;
import cyder.utils.OsUtil;
import cyder.utils.SerializationUtil;
import cyder.youtube.YouTubeConstants;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A managed for the current {@link User}.
 * The current Cyder user is not exposed but instead proxied by this manager
 * for purposes of encapsulation, validation, and convenience methods.
 */
public enum UserDataManager {
    /**
     * The user data manager instance.
     */
    INSTANCE;

    /**
     * The tag for json writes.
     */
    private final String jsonTag = LoggingUtil.surroundWithBrackets("JSON Write");

    /**
     * A default user object.
     */
    private final User defaultUser = new User();

    /**
     * The current user object this manager is being a proxy for.
     */
    private User user;

    /**
     * The file the current user object is written to periodically and on program closure.
     */
    private File userFile;

    /**
     * The current levenshtein distance between the last and current write to the user json file.
     */
    private int currentLevenshteinDistance;

    /**
     * The last result of serializing {@link #user} before writing to {@link #userFile}
     */
    private String lastSerializedUser = "";

    UserDataManager() {
        Logger.log(LogTag.OBJECT_CREATION, "UserDataManager singleton constructed");
        user = defaultUser;
    }

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
        user = User.fromJson(jsonFile);

        startUserSaverSubroutine();
    }

    /**
     * The future
     */
    private Future<?> lastStartedUserSaverSubroutine;

    /**
     * Starts the subroutine to execute every {@link Props#serializeAndSaveCurrentUser}
     * seconds to save the current {@link #user} to their {@link #userFile}.
     */
    private synchronized void startUserSaverSubroutine() {
        lastStartedUserSaverSubroutine = Executors.newSingleThreadExecutor(
                new CyderThreadFactory(IgnoreThread.UserSaver.getName())).submit(() -> {
            while (true) {
                writeUser();
                ThreadUtil.sleepSeconds(Props.serializeAndSaveCurrentUser.getValue());
            }
        });
    }

    /**
     * Stops the user saver subroutine if running.
     */
    private synchronized void stopUserSaverSubroutine() {
        if (lastStartedUserSaverSubroutine != null) {
            lastStartedUserSaverSubroutine.cancel(true);
        }
    }

    /**
     * Writes the current user to the user's source JSON file.
     */
    public synchronized void writeUser() {

        Preconditions.checkState(isInitialized());

        try {
            if (!userFile.exists()) {
                if (!userFile.createNewFile()) {
                    throw new FatalException("Failed to re-create user data file: " + userFile.getAbsolutePath());
                }
            }

            updateCurrentLevenshteinDistance();

            SerializationUtil.toJson(user, userFile);

            if (currentLevenshteinDistance > 0) {
                String representation = jsonTag + CyderStrings.space + CyderStrings.openingBracket
                        + "Levenshtein: " + currentLevenshteinDistance + CyderStrings.closingBracket
                        + CyderStrings.space + "User" + CyderStrings.space + CyderStrings.quote
                        + getUsername() + CyderStrings.quote + CyderStrings.space
                        + "was written to file" + CyderStrings.colon + CyderStrings.space
                        + userFile.getParentFile().getName() + OsUtil.FILE_SEP + userFile.getName();
                Logger.log(LogTag.SYSTEM_IO, representation);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Updates the levenshtein distance between the last serialized user
     * and the result of serializing the current user fields.
     */
    private synchronized void updateCurrentLevenshteinDistance() {
        String serialized = user.toJson();
        currentLevenshteinDistance = LevenshteinUtil.computeLevenshteinDistance(serialized, lastSerializedUser);
        lastSerializedUser = serialized;
    }

    /**
     * Serializes the current Cyder user to the {@link #userFile}, after which the user and user file are set to null
     * allowing for the {@link #initialize(String)} method to be invoked again.
     */
    public synchronized void removeManagement() {
        Preconditions.checkState(isInitialized());

        stopUserSaverSubroutine();
        writeUser();

        userFile = null;
        user = defaultUser;
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
    public User createNewUser(String username, String password) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);
        Preconditions.checkArgument(!username.isEmpty());
        Preconditions.checkArgument(!password.isEmpty());
        Preconditions.checkArgument(!UserUtil.usernameInUse(username));

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        return user;
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

        if (!UserUtil.shouldIgnoreForLogging(dataId)) {
            Logger.log(LogTag.USER_GET, "ID" + CyderStrings.colon + CyderStrings.space + dataId);
        }
    }

    /**
     * Sets the user data with the provided id to the value if a setter method can be found.
     *
     * @param id    the id of the data such as "username"
     * @param value the value to set the data piece to
     * @param <T>   the type of the value
     * @return whether the mutator succeeded
     */
    @CanIgnoreReturnValue
    public <T> boolean setUserDataById(String id, T value) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkNotNull(id);
        Preconditions.checkArgument(!id.isEmpty());
        Preconditions.checkNotNull(value);

        Optional<Method> methodOptional = UserUtil.getSetterMethodForDataWithName(id, user);

        if (methodOptional.isPresent()) {
            try {
                methodOptional.get().invoke(user, value);
                return true;
            } catch (Exception ignored) {}
        }

        return false;
    }

    /**
     * Returns the user data with the provided is if found. Empty optional else.
     *
     * @param id   the id of the user data
     * @param type the type to cast the return value to
     * @param <T>  the type
     * @return the result of invoking the accessor method if found. Empty optional else
     */
    public <T> Optional<T> getUserDataById(String id, Class<T> type) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkNotNull(id);
        Preconditions.checkArgument(!id.isEmpty());
        Preconditions.checkNotNull(type);

        Optional<Method> methodOptional = UserUtil.getGetterMethodForDataWithName(id, user);

        if (methodOptional.isPresent()) {
            try {
                Object result = methodOptional.get().invoke(user);
                return Optional.of(type.cast(result));
            } catch (Exception ignored) {}
        }

        return Optional.empty();
    }

    /**
     * Returns the boolean value for the user data with the provided ID.
     *
     * @param id the id of the boolean user data to return
     * @return the value of the user data
     */
    public boolean getBooleanUserDataValue(String id) {
        Preconditions.checkNotNull(id);
        Preconditions.checkArgument(!id.isEmpty());

        Optional<Method> methodOptional = UserUtil.getGetterMethodForDataWithName(id, user);

        if (methodOptional.isPresent()) {
            try {
                return (boolean) methodOptional.get().invoke(user);
            } catch (Exception ignored) {}
        }

        throw new IllegalArgumentException("No boolean user data found with ID: " + id);
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
        getterInvoked(UserData.USERNAME);
        return user.getUsername();
    }

    /**
     * Sets the username of the current user.
     *
     * @param username the new requested username
     */
    public synchronized void setUsername(String username) {
        Preconditions.checkState(isInitialized());
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
        getterInvoked(UserData.PASSWORD);
        return user.getPassword();
    }

    /**
     * Sets the password of the current user.
     *
     * @param password the hashed password of the current user
     */
    public synchronized void setPassword(String password) {
        Preconditions.checkState(isInitialized());
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
        getterInvoked(UserData.FONT_NAME);
        return user.getFontName();
    }

    /**
     * Sets the name of the user font.
     *
     * @param fontName the name of the user font
     */
    public synchronized void setFontName(String fontName) {
        Preconditions.checkState(isInitialized());
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
        getterInvoked(UserData.FONT_SIZE);
        return user.getFontSize();
    }

    /**
     * Sets the font size for the user.
     *
     * @param fontSize the font size for the user
     */
    public synchronized void setFontSize(int fontSize) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkArgument(fontSize >= Props.minFontSize.getValue());
        Preconditions.checkArgument(fontSize <= Props.maxFontSize.getValue());

        user.setFontSize(fontSize);
    }

    /**
     * Returns the user's foreground color.
     *
     * @return the user's foreground color
     */
    public synchronized Color getForegroundColor() {
        getterInvoked(UserData.FOREGROUND_COLOR);
        return ColorUtil.hexStringToColor(user.getForegroundColorHexCode());
    }

    /**
     * Returns the hex code for the user's foreground color.
     *
     * @return the hex code for the user's foreground color
     */
    public synchronized String getForegroundHexCode() {
        getterInvoked(UserData.FOREGROUND_COLOR);
        return user.getForegroundColorHexCode();
    }

    /**
     * Sets the user's foreground color.
     *
     * @param color the user's foreground color
     */
    public synchronized void setForegroundColor(Color color) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkNotNull(color);

        user.setForegroundColorHexCode(ColorUtil.toRgbHexString(color));
    }

    /**
     * Returns the user's background color.
     *
     * @return the user's background color
     */
    public synchronized Color getBackgroundColor() {
        getterInvoked(UserData.BACKGROUND_COLOR);
        return ColorUtil.hexStringToColor(user.getBackgroundColorHexCode());
    }

    /**
     * Returns the user's background color hex code.
     *
     * @return the user's background color hex code
     */
    public synchronized String getBackgroundHexCode() {
        getterInvoked(UserData.BACKGROUND_COLOR);
        return user.getBackgroundColorHexCode();
    }

    /**
     * Sets the user's background color.
     *
     * @param color the user's background color
     */
    public synchronized void setBackgroundColor(Color color) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkNotNull(color);

        user.setBackgroundColorHexCode(ColorUtil.toRgbHexString(color));
    }

    /**
     * Returns whether intro music should be played on user login.
     *
     * @return whether intro music should be played on user login
     */
    public synchronized boolean shouldPlayIntroMusic() {
        getterInvoked(UserData.INTRO_MUSIC);
        return user.shouldPlayIntroMusic();
    }

    /**
     * Sets whether intro music should be played on user login.
     *
     * @param shouldPlay whether intro music should be played on user login
     */
    public synchronized void setShouldPlayIntroMusic(boolean shouldPlay) {
        Preconditions.checkState(isInitialized());
        user.setIntroMusic(shouldPlay);
    }

    /**
     * Returns whether debug stats should be shown on initial console load.
     *
     * @return whether debug stats should be shown on initial console load
     */
    public synchronized boolean shouldShowDebugStats() {
        getterInvoked(UserData.DEBUG_STATS);
        return user.shouldShowDebugStatsOnStart();
    }

    /**
     * Sets whether debug stats should be shown on initial console load.
     *
     * @param shouldShowDebugStats whether debug stats should be shown on initial console load
     */
    public synchronized void setShouldShowDebugStats(boolean shouldShowDebugStats) {
        Preconditions.checkState(isInitialized());
        user.setDebugStats(shouldShowDebugStats);
    }

    /**
     * Returns whether a random background should be chosen on start for the console.
     *
     * @return whether a random background should be chosen on start for the console
     */
    public synchronized boolean shouldChooseRandomBackground() {
        getterInvoked(UserData.RANDOM_BACKGROUND_ON_START);
        return user.shouldChooseRandomBackgroundOnStart();
    }

    /**
     * Sets whether a random background should be chosen on start for the console.
     *
     * @param shouldChooseRandomBackground whether a random background should be chosen on start for the console
     */
    public synchronized void setShouldChooseRandomBackground(boolean shouldChooseRandomBackground) {
        Preconditions.checkState(isInitialized());
        user.setRandomBackgroundOnStart(shouldChooseRandomBackground);
    }

    /**
     * Returns whether a border should be drawn around the input field.
     *
     * @return whether a border should be drawn around the input field
     */
    public synchronized boolean shouldDrawInputBorder() {
        getterInvoked(UserData.DRAW_INPUT_BORDER);
        return user.shouldDrawInputBorder();
    }

    /**
     * Sets whether a border should be drawn around the input field.
     *
     * @param shouldDrawInputBorder whether a border should be drawn around the input field
     */
    public synchronized void setShouldDrawInputBorder(boolean shouldDrawInputBorder) {
        Preconditions.checkState(isInitialized());
        user.setDrawInputBorder(shouldDrawInputBorder);
    }

    /**
     * Returns whether a border should be drawn around the output area.
     *
     * @return whether a border should be drawn around the output area
     */
    public synchronized boolean shouldDrawOutputBorder() {
        getterInvoked(UserData.DRAW_OUTPUT_BORDER);
        return user.shouldDrawOutputBorder();
    }

    /**
     * Sets whether a border should be drawn around the output area.
     *
     * @param shouldDrawOutputBorder whether a border should be drawn around the output area
     */
    public synchronized void setShouldDrawOutputBorder(boolean shouldDrawOutputBorder) {
        Preconditions.checkState(isInitialized());
        user.setDrawOutputBorder(shouldDrawOutputBorder);
    }

    /**
     * Returns whether hourly chimes should be played.
     *
     * @return whether hourly chimes should be played
     */
    public synchronized boolean shouldPlayHourlyChimes() {
        getterInvoked(UserData.PLAY_HOURLY_CHIMES);
        return user.shouldPlayHourlyChimes();
    }

    /**
     * Sets whether hourly chimes should be played.
     *
     * @param shouldPlayHourlyChimes whether hourly chimes should be played
     */
    public synchronized void setShouldPlayHourlyChimes(boolean shouldPlayHourlyChimes) {
        Preconditions.checkState(isInitialized());
        user.setPlayHourlyChimes(shouldPlayHourlyChimes);
    }

    /**
     * Returns whether error notifications should be silenced.
     *
     * @return whether error notifications should be silenced
     */
    public synchronized boolean shouldSilenceErrors() {
        getterInvoked(UserData.SILENCE_ERRORS);
        return user.shouldSilenceErrors();
    }

    /**
     * Sets whether error notifications should be silenced.
     *
     * @param shouldSilenceErrors whether error notifications should be silenced
     */
    public synchronized void setShouldSilenceErrors(boolean shouldSilenceErrors) {
        Preconditions.checkState(isInitialized());
        user.setSilenceErrors(shouldSilenceErrors);
    }

    /**
     * Returns whether the program should be shown in fullscreen mode.
     *
     * @return whether the program should be shown in fullscreen mode
     */
    public synchronized boolean isFullscreen() {
        getterInvoked(UserData.FULLSCREEN);
        return user.isFullscreen();
    }

    /**
     * Sets whether the program should be shown in fullscreen mode.
     *
     * @param fullscreen whether the program should be shown in fullscreen mode
     */
    public synchronized void setFullscreen(boolean fullscreen) {
        Preconditions.checkState(isInitialized());
        user.setFullscreen(fullscreen);
    }

    /**
     * Returns whether the output area should be filled.
     *
     * @return whether the output area should be filled
     */
    public synchronized boolean shouldDrawOutputFill() {
        getterInvoked(UserData.DRAW_OUTPUT_FILL);
        return user.shouldDrawOutputFill();
    }

    /**
     * Sets whether the output area should be filled.
     *
     * @param shouldDrawOutputFill whether the output area should be filled
     */
    public synchronized void setShouldDrawOutputFill(boolean shouldDrawOutputFill) {
        Preconditions.checkState(isInitialized());
        user.setDrawOutputFill(shouldDrawOutputFill);
    }

    /**
     * Returns whether the input field should be filled.
     *
     * @return whether the input field should be filled
     */
    public synchronized boolean shouldDrawInputFill() {
        getterInvoked(UserData.DRAW_INPUT_FILL);
        return user.shouldDrawInputFill();
    }

    /**
     * Sets whether the input field should be filled.
     *
     * @param shouldDrawInputFill whether the input field should be filled
     */
    public synchronized void setShouldDrawInputFill(boolean shouldDrawInputFill) {
        Preconditions.checkState(isInitialized());
        user.setDrawInputFill(shouldDrawInputFill);
    }

    /**
     * Returns whether the console clock should be drawn.
     *
     * @return whether the console clock should be drawn
     */
    public synchronized boolean shouldDrawConsoleClock() {
        getterInvoked(UserData.DRAW_CONSOLE_CLOCK);
        return user.shouldDrawConsoleClock();
    }

    /**
     * Sets whether the console clock should be drawn.
     *
     * @param shouldDrawConsoleClock whether the console clock should be drawn
     */
    public synchronized void setShouldDrawConsoleClock(boolean shouldDrawConsoleClock) {
        Preconditions.checkState(isInitialized());
        user.setDrawConsoleClock(shouldDrawConsoleClock);
    }

    /**
     * Returns whether seconds should be shown on the console clock.
     *
     * @return whether seconds should be shown on the console clock
     */
    public synchronized boolean shouldShowConsoleClockSeconds() {
        getterInvoked(UserData.SHOW_CONSOLE_CLOCK_SECONDS);
        return user.shouldShowConsoleClockSeconds();
    }

    /**
     * Sets whether seconds should be shown on the console clock.
     *
     * @param shouldShowConsoleClockSeconds whether seconds should be shown on the console clock
     */
    public synchronized void setShouldShowConsoleClockSeconds(boolean shouldShowConsoleClockSeconds) {
        Preconditions.checkState(isInitialized());
        user.setShowConsoleClockSeconds(shouldShowConsoleClockSeconds);
    }

    /**
     * Returns whether user input should be filtered.
     *
     * @return whether user input should be filtered
     */
    public synchronized boolean shouldFilterchat() {
        getterInvoked(UserData.FILTER_CHAT);
        return user.shouldFilterChat();
    }

    /**
     * Sets whether user input should be filtered.
     *
     * @param shouldFilterChat whether user input should be filtered
     */
    public synchronized void setShouldFilterChat(boolean shouldFilterChat) {
        Preconditions.checkState(isInitialized());
        user.setFilterChat(shouldFilterChat);
    }

    /**
     * Returns the time at which this user's last session started.
     *
     * @return the time at which this user's last session started
     */
    public synchronized long getLastSessionStart() {
        getterInvoked(UserData.LAST_SESSION_START);
        return user.getLastSessionStart();
    }

    /**
     * Sets the time at which this user's last session started.
     *
     * @param lastSessionStart the time at which this user's last session started
     */
    public synchronized void setLastSessionStart(long lastSessionStart) {
        Preconditions.checkState(isInitialized());
        user.setLastSessionStart(lastSessionStart);
    }

    /**
     * Returns whether the program should be minimized on console close.
     *
     * @return whether the program should be minimized on console close
     */
    public synchronized boolean shouldMinimizeOnClose() {
        getterInvoked(UserData.MINIMIZE_ON_CLOSE);
        return user.shouldMinimizeOnClose();
    }

    /**
     * Sets whether the program should be minimized on console close.
     *
     * @param shouldMinimizeOnClose whether the program should be minimized on console close
     */
    public synchronized void setShouldMinimizeOnClose(boolean shouldMinimizeOnClose) {
        Preconditions.checkState(isInitialized());
        user.setMinimizeOnClose(shouldMinimizeOnClose);
    }

    /**
     * Returns whether the typing animation should be shown.
     *
     * @return whether the typing animation should be shown
     */
    public synchronized boolean shouldShowTypingAnimation() {
        getterInvoked(UserData.TYPING_ANIMATION);
        return user.shouldShowTypingAnimation();
    }

    /**
     * Sets whether the typing animation should be shown.
     *
     * @param shouldShowTypingAnimation whether the typing animation should be shown
     */
    public synchronized void setShouldShowTypingAnimation(boolean shouldShowTypingAnimation) {
        Preconditions.checkState(isInitialized());
        user.setTypingAnimation(shouldShowTypingAnimation);
    }

    /**
     * Returns whether the busy animation should be shown.
     *
     * @return whether the busy animation should be shown
     */
    public synchronized boolean shouldShowBusyAnimation() {
        getterInvoked(UserData.SHOW_BUSY_ANIMATION);
        return user.showShowBusyAnimation();
    }

    /**
     * Sets whether the busy animation should be shown.
     *
     * @param shouldShowBusyAnimation whether the busy animation should be shown
     */
    public synchronized void setShouldShowBusyAnimation(boolean shouldShowBusyAnimation) {
        Preconditions.checkState(isInitialized());
        user.setShowBusyAnimation(shouldShowBusyAnimation);
    }

    /**
     * Returns whether frames should be drawn with rounded borders.
     *
     * @return whether frames should be drawn with rounded borders
     */
    public synchronized boolean shouldDrawRoundedFrameBorders() {
        getterInvoked(UserData.ROUNDED_FRAME_BORDERS);
        return user.shouldDrawRoundedFrameBorders();
    }

    /**
     * Sets whether frames should be drawn with rounded borders.
     *
     * @param shouldDrawRoundedFrameBorders whether frames should be drawn with rounded borders
     */
    public synchronized void setShouldDrawRoundedFrameBorders(boolean shouldDrawRoundedFrameBorders) {
        Preconditions.checkState(isInitialized());
        user.setRoundedFrameBorders(shouldDrawRoundedFrameBorders);
    }

    /**
     * Returns the frame color.
     *
     * @return the frame color
     */
    public synchronized Color getFrameColor() {
        getterInvoked(UserData.FRAME_COLOR);
        return ColorUtil.hexStringToColor(user.getFrameColorHexCode());
    }

    /**
     * Returns the hex code for teh frame color.
     *
     * @return the hex code for teh frame color
     */
    public synchronized String getFrameColorHexCode() {
        getterInvoked(UserData.FRAME_COLOR);
        return user.getFrameColorHexCode();
    }

    /**
     * Sets the frame color.
     *
     * @param color the frame color
     */
    public synchronized void setFrameColor(Color color) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkNotNull(color);

        user.setFrameColorHexCode(ColorUtil.toRgbHexString(color));
    }

    /**
     * Returns the console clock format.
     *
     * @return the console clock format
     */
    public synchronized String getConsoleClockFormat() {
        getterInvoked(UserData.CONSOLE_CLOCK_FORMAT);
        return user.getConsoleClockFormat();
    }

    /**
     * Sets the console clock format.
     *
     * @param clockFormat the console clock format
     */
    public synchronized void setConsoleClockFormat(String clockFormat) {
        Preconditions.checkState(isInitialized());
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
        getterInvoked(UserData.PLAY_TYPING_SOUND);
        return user.shouldPlayTypingSound();
    }

    /**
     * Sets whether a typing sound should be played when the typing animation is on-going.
     *
     * @param shouldPlayTypingSound whether a typing sound should be played when the typing animation is on-going
     */
    public synchronized void setShouldPlayTypingSound(boolean shouldPlayTypingSound) {
        Preconditions.checkState(isInitialized());
        user.setPlayTypingSound(shouldPlayTypingSound);
    }

    /**
     * Returns the YouTube uuid this user is at in the random generation cycle.
     *
     * @return the YouTube uuid this user is at in the random generation cycle
     */
    public synchronized String getYouTubeUuid() {
        getterInvoked(UserData.YOUTUBE_UUID);
        return user.getYoutubeUuid();
    }

    /**
     * Sets the YouTube uuid this user is at in the random generation cycle.
     *
     * @param youTubeUuid the YouTube uuid this user is at in the random generation cycle
     */
    public synchronized void setYouTubeUuid(String youTubeUuid) {
        Preconditions.checkState(isInitialized());
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
        getterInvoked(UserData.CAPS_MODE);
        return user.isCapsMode();
    }

    /**
     * Sets whether caps mode should be enabled.
     *
     * @param capsMode whether caps mode should be enabled
     */
    public synchronized void setCapsMode(boolean capsMode) {
        Preconditions.checkState(isInitialized());
        user.setCapsMode(capsMode);
    }

    /**
     * Returns whether this user is logged in.
     *
     * @return whether this user is logged in
     */
    public synchronized boolean isLoggedIn() {
        getterInvoked(UserData.LOGGED_IN);
        return user.isLoggedIn();
    }

    /**
     * Sets whether this user is logged in.
     *
     * @param loggedIn whether this user is logged in
     */
    public synchronized void setLoggedIn(boolean loggedIn) {
        Preconditions.checkState(isInitialized());
        user.setLoggedIn(loggedIn);
    }

    /**
     * Returns whether the audio total length should be shown instead of the remaining time for the audio player.
     *
     * @return whether the audio total length should be shown instead of the remaining time for the audio player
     */
    public synchronized boolean shouldShowAudioTotalLength() {
        getterInvoked(UserData.SHOW_AUDIO_TOTAL_LENGTH);
        return user.shouldShowAudioTotalLength();
    }

    /**
     * Sets whether the audio total length should be shown instead of the remaining time for the audio player.
     *
     * @param shouldShowAudioTotalLength whether the audio total length should
     *                                   be shown instead of the remaining time for the audio player
     */
    public synchronized void setShouldShowAudioTotalLength(boolean shouldShowAudioTotalLength) {
        Preconditions.checkState(isInitialized());
        user.setShowAudioTotalLength(shouldShowAudioTotalLength);
    }

    /**
     * Returns whether notifications should be persisted until user dismissal.
     *
     * @return whether notifications should be persisted until user dismissal
     */
    public synchronized boolean shouldPersistNotifications() {
        getterInvoked(UserData.SHOULD_PERSIST_NOTIFICATIONS);
        return user.shouldPersistNotifications();
    }

    /**
     * Sets whether notifications should be persisted until user dismissal.
     *
     * @param shouldPersistNotifications whether notifications should be persisted until user dismissal
     */
    public synchronized void setShouldPersistNotifications(boolean shouldPersistNotifications) {
        Preconditions.checkState(isInitialized());
        user.setPersistNotifications(shouldPersistNotifications);
    }

    /**
     * Returns whether certain animations should be performed.
     *
     * @return whether certain animations should be performed
     */
    public synchronized boolean shouldDoAnimations() {
        getterInvoked(UserData.DO_ANIMATIONS);
        return user.shouldDoAnimations();
    }

    /**
     * Sets whether certain animations should be performed.
     *
     * @param shouldDoAnimations whether certain animations should be performed
     */
    public synchronized void setShouldDoAnimations(boolean shouldDoAnimations) {
        Preconditions.checkState(isInitialized());
        user.setDoAnimations(shouldDoAnimations);
    }

    /**
     * Returns whether compact text mode is enabled.
     *
     * @return whether compact text mode is enabled
     */
    public synchronized boolean compactTextMode() {
        getterInvoked(UserData.COMPACT_TEXT_MODE);
        return user.isCompactTextMode();
    }

    /**
     * Sets whether compact text mode is enabled.
     *
     * @param compactTextMode whether compact text mode is enabled
     */
    public synchronized void compactTextModeEnabled(boolean compactTextMode) {
        Preconditions.checkState(isInitialized());
        user.setCompactTextMode(compactTextMode);
    }

    /**
     * Returns whether unknown user commands should be passed to the native shell.
     *
     * @return whether unknown user commands should be passed to the native shell
     */
    public synchronized boolean shouldWrapShell() {
        getterInvoked(UserData.WRAP_NATIVE_SHELL);
        return user.shouldWrapNativeShell();
    }

    /**
     * Sets whether unknown user commands should be passed to the native shell.
     *
     * @param wrapShell whether unknown user commands should be passed to the native shell
     */
    public synchronized void setWrapShell(boolean wrapShell) {
        Preconditions.checkState(isInitialized());
        user.setWrapNativeShell(wrapShell);
    }

    /**
     * Returns whether a map should be displayed as the background of the weather widget.
     *
     * @return whether a map should be displayed as the background of the weather widget
     */
    public synchronized boolean shouldDrawWeatherMap() {
        getterInvoked(UserData.DRAW_WEATHER_MAP);
        return user.shouldDrawWeatherMap();
    }

    /**
     * Sets whether a map should be displayed as the background of the weather widget.
     *
     * @param shouldDrawWeatherMap whether a map should be displayed as the background of the weather widget
     */
    public synchronized void setShouldDrawWeatherMap(boolean shouldDrawWeatherMap) {
        Preconditions.checkState(isInitialized());
        user.setDrawWeatherMap(shouldDrawWeatherMap);
    }

    /**
     * Returns whether the hour labels should be painted for the clock widget.
     *
     * @return whether the hour labels should be painted for the clock widget
     */
    public synchronized boolean shouldPaintClockHourLabels() {
        getterInvoked(UserData.PAINT_CLOCK_WIDGET_HOUR_LABELS);
        return user.shouldPaintClockWidgetHourLabels();
    }

    /**
     * Sets whether the hour labels should be painted for the clock widget.
     *
     * @param shouldPaintClockHourLabels whether the hour labels should be painted for the clock widget
     */
    public synchronized void setShouldPaintClockHourLabels(boolean shouldPaintClockHourLabels) {
        Preconditions.checkState(isInitialized());
        user.setPaintClockWidgetHourLabels(shouldPaintClockHourLabels);
    }

    /**
     * Returns whether the second hand should be shown on the clock widget.
     *
     * @return whether the second hand should be shown on the clock widget
     */
    public synchronized boolean shouldShowClockWidgetSecondHand() {
        getterInvoked(UserData.SHOW_CLOCK_WIDGET_SECOND_HAND);
        return user.shouldShowClockWidgetSecondHand();
    }

    /**
     * Sets whether the second hand should be shown on the clock widget.
     *
     * @param shouldShowClockWidgetSecondHand whether the second hand should be shown on the clock widget
     */
    public synchronized void setShouldShowClockWidgetSecondHand(boolean shouldShowClockWidgetSecondHand) {
        Preconditions.checkState(isInitialized());
        user.setShowClockWidgetSecondHand(shouldShowClockWidgetSecondHand);
    }

    /**
     * Returns the current screen stat for this user.
     *
     * @return the current screen stat for this user
     */
    public synchronized ScreenStat getScreenStat() {
        getterInvoked(UserData.SCREEN_STAT);
        return user.getScreenStat();
    }

    /**
     * Sets the current screen stat for this user.
     *
     * @param screenStat the current screen stat for this user
     */
    public synchronized void setScreenStat(ScreenStat screenStat) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkNotNull(screenStat);

        user.setScreenStat(screenStat);
    }

    /**
     * Returns the mapped executables for this user.
     *
     * @return the mapped executables for this user
     */
    public synchronized ImmutableList<MappedExecutable> getMappedExecutables() {
        getterInvoked(UserData.MAPPED_EXECUTABLES);
        return user.getMappedExecutables();
    }

    /**
     * Sets the mapped executables for this user.
     *
     * @param mappedExecutables the mapped executables for this user
     */
    public synchronized void setMappedExecutables(Collection<MappedExecutable> mappedExecutables) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkNotNull(mappedExecutables);

        user.setMappedExecutables(ImmutableList.copyOf(mappedExecutables));
    }

    /**
     * Returns the fill opacity.
     *
     * @return the fill opacity
     */
    public synchronized int getFillOpacity() {
        getterInvoked(UserData.FILL_OPACITY);
        return user.getFillOpacity();
    }

    /**
     * Sets the the fill opacity.
     *
     * @param fillOpacity the fill opacity
     */
    public synchronized void setFillOpacity(int fillOpacity) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkArgument(fillOpacity >= 0 && fillOpacity <= 255);

        user.setFillOpacity(fillOpacity);
    }

    /**
     * Returns whether the welcome message has been shown.
     *
     * @return whether the welcome message has been shown
     */
    public synchronized boolean hasShownWelcomeMessage() {
        getterInvoked(UserData.SHOWN_WELCOME_MESSAGE);
        return user.hasShownWelcomeMessage();
    }

    /**
     * Sets whether the welcome message has been shown.
     *
     * @param shownWelcomeMessage whether the welcome message has been shown
     */
    public synchronized void setShownWelcomeMessage(boolean shownWelcomeMessage) {
        Preconditions.checkState(isInitialized());
        user.setShownWelcomeMessage(shownWelcomeMessage);
    }

    /**
     * Returns the time at which this account was created.
     *
     * @return the time at which this account was created
     */
    public synchronized long getAccountCreationTime() {
        getterInvoked(UserData.ACCOUNT_CREATION_TIME);
        return user.getAccountCreationTime();
    }

    /**
     * Sets the time at which this account was created.
     *
     * @param accountCreationTime the time at which this account was created
     */
    public synchronized void setAccountCreationTime(long accountCreationTime) {
        Preconditions.checkState(isInitialized());
        user.setAccountCreationTime(accountCreationTime);
    }
}
