package cyder.meta;

/**
 * Arguments recognized by the main entry point of Cyder.
 */
public enum CyderArguments {
    LOG_FILE("log-file");

    /**
     * A double dash for full parameter construction.
     */
    private static final String dashDash = "--";

    /**
     * The name of this argument
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
}
