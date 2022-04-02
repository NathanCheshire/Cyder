package cyder.genesis;

/**
 * Booelean toggles used through Cyder components.
 * These are not in a yml file and loaded because they
 * should not be able to be triggered or changed if Cyder
 * is running in Jar mode.
 */
public class CyderToggles {
    /**
     * Whether Cyder is currently released.
     */
    public static final boolean RELEASED = false;

    /**
     * Whether Cyder is in fast testing mode.
     */
    public static final boolean FAST_TESTING_MODE = false;

    /**
     * The name of the current Cyder version.
     */
    public static final String VERSION = "Liminal";

    /**
     * The release date of the current Cyder version.
     */
    public static final String RELEASE_DATE = "22.3.6";

    /**
     * Whether components can be moved on their parent.
     */
    public static final boolean COMPONENTS_RELOCATABLE = false;

    /**
     * Whether normal testing mode is on.
     */
    public static final boolean TESTING_MODE = true;

    /**
     * Whether auto cypher is active.
     */
    public static final boolean AUTO_CYPHER = true;

    /**
     * Whether the splash frame should be auto disposed.
     */
    public static final boolean DISPOSE_SPLASH = true;

    // todo implement me
    /**
     * Whether to automatically wipe all past logs on Cyder start.
     */
    public static final boolean WIPE_LOGS_ON_START = true;
}
