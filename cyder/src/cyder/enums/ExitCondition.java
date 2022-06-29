package cyder.enums;

import cyder.utils.OSUtil;

/**
 * Cyder exit codes and their corresponding meanings.
 */
public enum ExitCondition {
    SufficientSubroutineExit(-14, "A sufficient subroutine failed"),
    WatchdogTimeout(-13, "Watchdog Timeout"),
    NotReleased(-12, "Cyder Not Released"),
    SubroutineException(-11, "Subroutine Exception"),
    MultipleInstancesExit(-10, "Multiple Instances Exit"),
    ExternalStop(-9, "External Stop"),
    JsonParsingException(-8, "JSON Parsing Exception"),
    FatalTimeout(-7, "Fatal Timeout"),
    CorruptedSystemFiles(-6, "Corrupted System Files"),
    ImproperOS(-5, "Unsupported OS"),
    CorruptedUser(-4, "Corrupted User"),
    UserDeleted(-3, "User Deleted"),
    ForcedImmediateExit(-2, "Forced Immediate Exit"),

    /**
     * Reserved as this indicates something specific to JVM exits
     */
    TrueExternalStop(-1, "DO NOT OVERRIDE"),

    /**
     * The standard Cyder exit.
     */
    GenesisControlledExit(0, "Genesis Controlled Exit"),

    /**
     * The exit was schedule via the user or Cyder.
     */
    ScheduledExit(1, "Genesis Controlled Exit");

    /**
     * The code associated with this ExitCondition.
     * The method {@link OSUtil#exit(ExitCondition)} will invoke {@link System#exit(int)} using this code.
     */
    private final int code;

    /**
     * The associated description with the exit condition used for logging.
     */
    private final String description;

    ExitCondition(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the associated code with this exit condition.
     *
     * @return the associated code with this exit condition
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the associated description with this exit condition.
     *
     * @return the associated description with this exit condition
     */
    public String getDescription() {
        return description;
    }
}
