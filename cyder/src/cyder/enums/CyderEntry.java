package cyder.enums;

/**
 * Possible ways to enter Cyder.
 */
public enum CyderEntry {
    /**
     * Hashes within debughashes.json.
     */
    AutoCypher,
    /**
     * The official login frame.
     */
    Login,
    /**
     * If the previous session was termianted without a logout.
     */
    PreviouslyLoggedIn,

    //todo add pass/fail strings
}
