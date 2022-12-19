package main.java.cyder.logging;

import main.java.cyder.enums.Dynamic;

/**
 * Supported tags for log entries.
 */
public enum LogTag {
    /**
     * The cyder user typed something through the console input field.
     */
    CLIENT("Client"),

    /**
     * Whatever is printed/appended to the CyderTextPane from the console.
     */
    CONSOLE_OUT("Console Out"),

    /**
     * Something that would have been appended to the Cyder text pane was piped to a file.
     */
    CONSOLE_REDIRECTION("Console Redirection"),

    /**
     * An exception was thrown and handled by the ExceptionHandler.
     */
    EXCEPTION("Exception"),

    /**
     * Audio played/stopped/paused/etc.
     */
    AUDIO("Audio"),

    /**
     * Frame control actions.
     */
    UI_ACTION("UI"),

    /**
     * A link was printed or opened.
     */
    LINK("Link"),

    /**
     * IO by Cyder typically to/from a json file but usually to files within a {@link Dynamic}.
     */
    SYSTEM_IO("System IO"),

    /**
     * A user enters input inside of the login field on the login widget frame.
     */
    LOGIN_INPUT("Login Input"),

    /**
     * Output was printed to the login console.
     */
    LOGIN_OUTPUT("Login Output"),

    /**
     * A user logs out of Cyder, not necessarily a program exit.
     */
    LOGOUT("Logout"),

    /**
     * When Cyder.java is first invoked by the JVM, we log certain properties about
     * the JVM/JRE and send them to the Cyder backend as well.
     */
    JVM_ARGS("JVM"),

    /**
     * JVM program entry.
     */
    JVM_ENTRY("JVM Entry"),

    /**
     * Program controlled exit, right before EOL tags.
     */
    PROGRAM_EXIT("Program Exit"),

    /**
     * A user became corrupted invoking the userJsonCorrupted method.
     */
    USER_CORRUPTION("User Corruption"),

    /**
     * A quick debug information statement.
     */
    DEBUG("Debug"),

    /**
     * A type of input was handled via the InputHandler.
     */
    HANDLE_METHOD("Handle"),

    /**
     * A widget was opened via the reflection method.
     */
    WIDGET_OPENED("Widget"),

    /**
     * An action related to preferences.
     */
    PREFERENCE("Preference"),

    /**
     * A thread was spun up and started by CyderThreadRunner.
     */
    THREAD_STARTED("Thread Started"),

    /**
     * When an object's constructor is invoked.
     */
    OBJECT_CREATION("Object Creation"),

    /**
     * The console was loaded.
     */
    CONSOLE_LOAD("Console Loaded"),

    /**
     * A font was loaded by the sub-routine from the fonts/ directory.
     */
    FONT_LOADED("Font Loaded"),

    /**
     * The CyderSplash loading message was set.
     */
    SPLASH_LOADING_MESSAGE("Splash Loading Message"),

    /**
     * An action related to a prop occurred.
     */
    PROPS_ACTION("Props Action"),

    /**
     * An action related to python.
     */
    PYTHON("Python"),

    /**
     * An action related to the network or IO.
     */
    NETWORK("Network"),

    /**
     * An action related to the watchdog.
     */
    WATCHDOG("Watchdog"),

    /**
     * A warning related to how a handle class/method/annotation usage is used.
     */
    HANDLE_WARNING("Handle Warning"),

    /**
     * A warning related to a widget annotation.
     */
    WIDGET_WARNING("Widget Warning"),

    /**
     * A warning related to a gui test annotation.
     */
    GUI_TEST_WARNING("GuiTest Warning"),

    /**
     * A warning related to a vanilla annotation.
     */
    VANILLA_WARNING("Vanilla Warning"),

    /**
     * A get call was invoked on a user object.
     */
    USER_GET("User Get"),

    /**
     * A warning related to a {@link main.java.cyder.annotations.CyderTest} method not constructed properly.
     */
    CYDER_TEST_WARNING("CyderTest Warning");

    /**
     * The name to be written to the log file when this tag is logged
     */
    private final String logName;

    /**
     * Constructs a new Tag object.
     *
     * @param logName the name to be written to the log file when this tag is logged
     */
    LogTag(String logName) {
        this.logName = logName;
    }

    /**
     * Returns the log name of this log tag.
     *
     * @return the log name of this log tag
     */
    public String getLogName() {
        return logName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return logName;
    }
}
