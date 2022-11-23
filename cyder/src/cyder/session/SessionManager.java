package cyder.session;

import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.SecurityUtil;

/**
 * The session manager for this instance of Cyder.
 */
public enum SessionManager {
    /**
     * The session manager instance.
     */
    INSTANCE;

    /**
     * The ID of this session of Cyder.
     */
    private final String sessionId = SecurityUtil.generateUuid();

    /**
     * Suppress default constructor.
     */
    SessionManager() {
        Logger.log(LogTag.OBJECT_CREATION, "Session manager instance constructed");
        Logger.log(LogTag.OBJECT_CREATION, "Session ID generated and set to: " + sessionId);
    }

    /**
     * Returns the session ID for this instance of Cyder.
     *
     * @return the session ID for this instance of Cyder
     */
    public String getSessionId() {
        return sessionId;
    }
}
