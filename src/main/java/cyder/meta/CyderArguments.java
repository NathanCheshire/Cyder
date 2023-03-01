package cyder.meta;

/**
 * Arguments recognized by the main entry point of Cyder.
 */
public enum CyderArguments {
    /**
     * The session id argument.
     */
    SESSION_ID("session-id"),

    /**
     * The argument to indicate an instance was started via a boostrap.
     */
    BOOSTRAP("boostrap");

    /**
     * A double dash for full parameter construction.
     */
    private static final String dashDash = "--";

    /**
     * The name of this argument.
     */
    private final String name;

    CyderArguments(String name) {
        this.name = name;
    }

    /**
     * Returns the full parameter for this argument.
     *
     * @return the full parameter for this argument
     */
    public String constructFullParameter() {
        return dashDash + name;
    }

    /**
     * Returns the name of this argument.
     *
     * @return the name of this argument
     */
    public String getName() {
        return name;
    }
}
