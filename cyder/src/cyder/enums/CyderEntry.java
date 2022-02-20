package cyder.enums;

/**
 * Possible ways to enter Cyder.
 */
public enum CyderEntry {
    /**
     * DebugHashes within DebugHash.
     */
    AutoCypher("AutoCypher","AutoCypher Success", "AutoCypher Fail"),

    /**
     * The official login frame.
     */
    Login("Login","STD Login Success", "STD Login FAIL"),

    /**
     * If the previous session was terminated without a logout.
     */
    PreviouslyLoggedIn("PreviouslyLoggedIn", "Previous Session Resumed",
            "No previously logged in users found");

    /**
     * The name associated with a CyderEntry.
     */
    private final String name;

    /**
     * The success message associated with a CyderEntry.
     */
    private final String passMessage;

    /**
     * The fail message associated with a CyderEntry.
     */
    private final String failMessage;

    /**
     * Constructs a new CyderEntry type.
     *
     * @param name the name for this entry method
     * @param passMessage the success message
     * @param failMessage the fail message
     */
    CyderEntry(String name, String passMessage, String failMessage) {
        this.name = name;
        this.passMessage = passMessage;
        this.failMessage = failMessage;
    }

    /**
     * Returns the name of this entry point.
     *
     * @return the name of this entry point
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the success message of this entry point.
     *
     * @return the success message of this entry point
     */
    public String getPassMessage() {
        return passMessage;
    }

    /**
     * Returns the fail message of this entry point.
     *
     * @return the fail message of this entry point
     */
    public String getFailMessage() {
        return failMessage;
    }
}
