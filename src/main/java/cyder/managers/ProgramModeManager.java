package main.java.cyder.managers;

import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;
import main.java.cyder.login.LoginHandler;
import main.java.cyder.utils.JvmUtil;
import main.java.cyder.utils.OsUtil;

/**
 * A manager for the program mode.
 */
public enum ProgramModeManager {
    /**
     * The program mode manager instance.
     */
    INSTANCE;

    /**
     * The program mode for this session of Cyder.
     */
    private static ProgramMode sessionProgramMode;

    /**
     * Refreshes the current program mode.
     */
    public void refreshProgramMode() {
        if (LoginHandler.wasStartedViaAutoCypher()) {
            sessionProgramMode = ProgramMode.DEVELOPER_DEBUG;
        } else if (JvmUtil.currentInstanceLaunchedWithDebug()) {
            sessionProgramMode = ProgramMode.IDE_DEBUG;
        } else if (!OsUtil.JAR_MODE) {
            sessionProgramMode = ProgramMode.IDE_NORMAL;
        } else {
            sessionProgramMode = ProgramMode.NORMAL;
        }

        Logger.log(LogTag.DEBUG, "Refreshed program mode, set as: " + sessionProgramMode.getName());
    }

    /**
     * Returns the program mode for this session of Cyder.
     *
     * @return the program mode for this session of Cyder
     */
    public ProgramMode getProgramMode() {
        return sessionProgramMode;
    }
}
