package cyder.logging;

import cyder.enums.Dynamic;

import static cyder.constants.CyderStrings.*;

/**
 * Supported tags for log entries.
 */
public enum LogTag {
    /**
     * The cyder user typed something through the console input field.
     */
    CLIENT("CLIENT"),

    /**
     * Whatever is printed/appended to the CyderTextPane from the console.
     */
    CONSOLE_OUT("CONSOLE OUT"),

    /**
     * Something that would have been appended to the Cyder text pane was piped to a file.
     */
    CONSOLE_REDIRECTION("CONSOLE PRINT REDIRECTION"),

    /**
     * An exception was thrown and handled by the ExceptionHandler.
     */
    EXCEPTION("EXCEPTION"),

    /**
     * Audio played/stopped/paused/etc.
     */
    AUDIO("AUDIO"),

    /**
     * Frame control actions.
     */
    UI_ACTION("UI"),

    /**
     * A link was printed or opened.
     */
    LINK("LINK"),

    /**
     * A user made a suggestion which will probably be ignored.
     */
    SUGGESTION("SUGGESTION"),

    /**
     * IO by Cyder typically to/from a json file but usually to files within {@link Dynamic#PATH}
     */
    SYSTEM_IO("SYSTEM IO"),

    /**
     * A user enters input inside of the login field on the login widget frame.
     */
    LOGIN_FIELD("LOGIN FIELD"),

    /**
     * Output was printed to the login console.
     */
    LOGIN_OUTPUT("LOGIN OUTPUT"),

    /**
     * A user logs out of Cyder, not necessarily a program exit.
     */
    LOGOUT("LOGOUT"),

    /**
     * When Cyder.java is first invoked by the JVM, we log certain properties about
     * the JVM/JRE and send them to the Cyder backend as well.
     */
    JVM_ARGS("JVM"),

    /**
     * JVM program entry.
     */
    JVM_ENTRY("ENTRY"),

    /**
     * Program controlled exit, right before EOL tags.
     */
    EXIT("EXIT"),

    /**
     * A user became corrupted invoking the userJsonCorrupted method.
     */
    CORRUPTION("CORRUPTION"),

    /**
     * A quick debug information statement.
     */
    DEBUG("DEBUG"),

    /**
     * A type of input was handled via the InputHandler.
     */
    HANDLE_METHOD("HANDLE"),

    /**
     * A widget was opened via the reflection method.
     */
    WIDGET_OPENED("WIDGET"),

    /**
     * A userdata which exists as a Preference object was toggled between states and refreshed.
     */
    PREFERENCE_REFRESH("PREFERENCE REFRESH"),

    /**
     * A thread was spun up and started by CyderThreadRunner.
     */
    THREAD_STARTED("THREAD STARTED"),

    /**
     * When an object's constructor is invoked.
     */
    OBJECT_CREATION("OBJECT CREATION"),

    /**
     * The console was loaded.
     */
    CONSOLE_LOAD("CONSOLE LOADED"),

    /**
     * A font was loaded by the sub-routine from the fonts/ directory.
     */
    FONT_LOADED("FONT LOADED"),

    /**
     * A prop from props.ini was loaded.
     */
    PROP_LOADED("PROP LOADED"),

    /**
     * The status of a thread, typically AWT-EventQueue-0.
     */
    THREAD_STATUS("THREAD STATUS"),

    /**
     * A Create (PUT), Read (GET), Update (POST), or Delete (DELETE) operation was performed
     * on the Cyder backend.
     */
    CRUD_OP("BACKEND CRUD"),

    /**
     * The CyderSplash loading message was set.
     */
    LOADING_MESSAGE("LOADING MESSAGE"),

    /**
     * A get call was invoked on a user object.
     */
    USER_GET("USER GET");

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
     * Constructs a log tag prepend for this log tag.
     *
     * @return a log tag prepend for this log tag
     */
    public String constructLogTagPrepend() {
        return openingBracket + this.logName + closingBracket + colon + space;
    }

    /**
     * Constructs the string value prepended to the log line before the representation.
     *
     * @param tagString the string representation of an exclusive log tag
     * @return the string to prepend to the log line
     */
    public static String constructLogTagPrepend(String tagString) {
        return openingBracket + tagString + closingBracket + colon + space;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return constructLogTagPrepend(logName);
    }
}
