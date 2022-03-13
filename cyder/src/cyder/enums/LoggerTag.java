package cyder.enums;

/**
 * Supported tags for log entries
 */
public enum LoggerTag {
    /**
     * The cyder user typed something through the console input field.
     */
    CLIENT,
    /**
     * Whatever is printed/appended to the CyderTextPane from the console frame.
     */
    CONSOLE_OUT,
    /**
     * An exception was thrown and handled by the ExceptionHandler.
     */
    EXCEPTION,
    /**
     * Audio played/stoped/paused/etc.
     */
    AUDIO,
    /**
     * Frame control actions.
     */
    UI_ACTION,
    /**
     * A link was printed or opened.
     */
    LINK,
    /**
     * A user made a suggestion which will probably be ignored.
     */
    SUGGESTION,
    /**
     * IO by Cyder typically to/from a json file but moreso to files within dynamic/
     */
    SYSTEM_IO,
    /**
     * A user starts Cyder or enters the main program, that of the ConsoleFrame.
     */
    LOGIN,
    /**
     * A user logs out of Cyder, not necessarily a program exit.
     */
    LOGOUT,
    /**
     * When Cyder.java is first invoked by the JVM, we log certain properties about
     * the JVM/JRE and send them to the Cyder backend as well.
     */
    JVM_ARGS,
    /**
     * JVM program entry.
     */
    JVM_ENTRY,
    /**
     * Program controlled exit, right before EOL tags.
     */
    EXIT,
    /**
     * A user became corrupted invoking the userJsonCorrupted method.
     */
    CORRUPTION,
    /**
     * A quick debug information statment.
     */
    DEBUG,
    /**
     * A type of input was handled via the InputHandler.
     */
    HANDLE_METHOD,
    /**
     * A widget was opened via the reflection method.
     */
    WIDGET_OPENED,
    /**
     * A userdata which exists as a Preference object was toggled between states and refreshed.
     */
    PREFERENCE_REFRESH,
    /**
     * A thread was spun up and started by CyderThreadRunner.
     */
    THREAD,
    /**
     * When an object's constructor is invoked.
     */
    OBJECT_CREATION,
    /**
     * The console was loaded.
     */
    CONSOLE_LOAD,
}
