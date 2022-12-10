package cyder.managers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * A manager for the program's name, version name, release date, released state, and similar states.
 */
@SuppressWarnings("FieldCanBeLocal") /* Readability */
public enum CyderVersionManager {
    /**
     * The version manager instance.
     */
    INSTANCE;

    /**
     * Constructor explicitly defined for logging purposes.
     */
    CyderVersionManager() {
        Logger.log(LogTag.OBJECT_CREATION, "Version manager constructed");
    }

    /**
     * The program name.
     */
    private final String programName = "Cyder";

    /**
     * The version name.
     */
    private final String version = "Liminal";

    /**
     * The date of release.
     */
    private final String releaseDate = "Not Yet Released";

    /**
     * Whether this release is publicly available.
     */
    private final boolean released = false;

    /**
     * Returns the program name.
     *
     * @return the program name
     */
    public String getProgramName() {
        return programName;
    }

    /**
     * Returns the name for this version.
     *
     * @return the name for this version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the release date for this version.
     *
     * @return the release date for this version
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Returns whether this release is publicly available.
     *
     * @return whether this release is publicly available
     */
    public boolean isReleased() {
        return released;
    }
}
