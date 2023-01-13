package cyder.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.user.data.MappedExecutable;
import cyder.user.data.ScreenStat;
import cyder.utils.SerializationUtil;

/**
 * A user object.
 */
public final class NewUser {
    private UserData<String> username = new UserData<>("username",
            "Username", "The username", "", String.class, () -> {

    });

    private String password;
    private String fontName;
    private int fontSize;
    private int fontMetric;
    private String foregroundColorHexCode;
    private String backgroundColorHexCode;
    private boolean introMusic;
    private boolean debugStats;
    private boolean randomBackgroundOnStart;
    private boolean drawOutputBorder;
    private boolean drawInputBorder;
    private boolean playHourlyChimes;
    private boolean silenceErrors;
    private boolean fullscreen;
    private boolean drawOutputFill;
    private boolean drawInputFill;
    private boolean drawConsoleClock;
    private boolean showConsoleClockSeconds;
    private boolean filterChat;
    private long lastSessionStart;
    private boolean minimizeOnClose;
    private boolean typingAnimation;
    private boolean showBusyAnimation;
    private boolean roundedFrameBorders;
    private String windowColorHexCode;
    private String consoleClockFormat;
    private boolean playTypingSound;
    private String youtubeUuid;
    private boolean capsMode;
    private boolean loggedIn;
    private boolean showAudioTotalLength;
    private boolean persistNotifications;
    private boolean doAnimations;
    private boolean compactTextMode;
    private boolean wrapNativeShell;
    private boolean darkMode;
    private boolean drawWeatherMap;
    private boolean paintClockWidgetHourLabels;
    private boolean showClockWidgetSecondHand;
    private int fillOpacity;
    private ScreenStat screenStat;
    private ImmutableList<MappedExecutable> executables;

    /**
     * Constructs a new user object.
     */
    public NewUser() {
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
     * Serializes and returns a new user object from the provided json.
     *
     * @param jsonMessage the json message
     * @return a new user object from the provided json
     */
    public static NewUser fromJson(String jsonMessage) {
        Preconditions.checkNotNull(jsonMessage);
        Preconditions.checkArgument(!jsonMessage.isEmpty());

        return SerializationUtil.fromJson(jsonMessage, NewUser.class);
    }
}
