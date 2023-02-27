package cyder.enumerations;

import cyder.utils.OsUtil;

/**
 * Cyder exit codes and their corresponding meanings.
 */
public enum ExitCondition {
    RemoteShutdownFailure(-15, "Remote Shutdown Failure"),
    SufficientSubroutineExit(-14, "Sufficient Subroutine failed"),
    WatchdogBootstrapFail(-13, "Boostrap Fail"),
    WatchdogTimeout(-12, "Watchdog Timeout"),
    NotReleased(-11, "Cyder Not Released"),
    NecessarySubroutineExit(-10, "Necessary Subroutine failed"),
    MultipleInstancesExit(-9, "Multiple Instances Exit"),
    ExternalStop(-8, "External Stop"),
    JsonParsingException(-7, "JSON Parsing Exception"),
    FatalTimeout(-6, "Fatal Timeout"),
    CorruptedSystemFiles(-5, "Corrupted System Files"),
    ImproperOS(-4, "Unsupported OS"),
    CorruptedUser(-3, "Corrupted User"),
    UserDeleted(-2, "User Deleted"),

    /**
     * The program was stopped in a way other than by Cyder.
     */
    TrueExternalStop(-1, "True External Stop"),

    /**
     * The standard Cyder exit.
     */
    StandardControlledExit(0, "Standard Controlled Exit"),

    /**
     * The exit was schedule by a Cyder user.
     */
    ScheduledExit(1, "Scheduled Exit"),

    /**
     * A remote shutdown was requested by a new instance of Cyder.
     */
    RemoteShutdown(2, "Remote Shutdown"),

    /**
     * A forced immediate exit of Cyder with no animations.
     */
    ForcedImmediateExit(3, "Forced Immediate Exit");

    /**
     * The code associated with this ExitCondition.
     * The method {@link OsUtil#exit(ExitCondition)} will invoke {@link System#exit(int)} using this code.
     */
    private final int code;

    /**
     * The description of the exit used for logging.
     */
    private final String description;

    ExitCondition(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the code for this exit condition.
     *
     * @return the code for this exit condition
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the description for this exit condition.
     *
     * @return the description for this exit condition
     */
    public String getDescription() {
        return description;
    }
}
