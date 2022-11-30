package cyder.session;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.utils.SecurityUtil;

/** The session manager for this instance of Cyder. */
public final class SessionManager {
    /** The ID of this session of Cyder. */
    private static final String sessionId = SecurityUtil.generateUuid();

    /** Suppress default constructor. */
    private SessionManager() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the session ID for this instance of Cyder.
     *
     * @return the session ID for this instance of Cyder
     */
    public static String getSessionId() {
        return sessionId;
    }
}
