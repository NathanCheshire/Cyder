package cyder.session;

import com.google.common.base.Preconditions;
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
    private String sessionId;

    /**
     * Suppress default constructor.
     */
    SessionManager() {
        Logger.log(LogTag.OBJECT_CREATION, "Session Manager singleton constructed");
    }

    /**
     * Initializes the session id.
     */
    public void initializeSessionId() {
        Preconditions.checkState(sessionId == null);

        sessionId = SecurityUtil.generateUuid();
        Logger.log(LogTag.DEBUG, "Set session ID as " + sessionId);
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
