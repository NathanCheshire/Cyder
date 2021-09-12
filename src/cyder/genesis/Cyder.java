package cyder.genesis;

import cyder.handler.ErrorHandler;
import cyder.handler.SessionLogger;
import cyder.utilities.IOUtil;
import cyder.utilities.SecurityUtil;
import cyder.utilities.SystemUtil;

public class Cyder {
    /**
     * Setup and start the best program ever made :D
     * @param CA - possible command line args passed in.
     *           They serve no purpose yet we'll still log them
     */
    public static void main(String[] CA)  {
        //set shutdown hook
        CyderSetup.addCommonExitHook();

        //start the logger
        SessionLogger.SessionLogger();
        SessionLogger.log(SessionLogger.Tag.ENTRY, SystemUtil.getWindowsUsername());

        CyderSetup.initSystemProperties();
        CyderSetup.initUIManager();

        IOUtil.cleanUsers();
        IOUtil.deleteTempDir();
        IOUtil.logArgs(CA);
        IOUtil.cleanSandbox();
        IOUtil.fixLogs();

        CyderSetup.initFrameChecker();

        if (SystemUtil.osxSystem()) {
           CyderSetup.osxExit();
        } else if (SecurityUtil.nathanLenovo() && IOUtil.getSystemData().isAutocypher()) {
            SessionLogger.log(SessionLogger.Tag.LOGIN, "AUTOCYPHER ATTEMPT");
            Login.autoCypher();
        } else if (IOUtil.getSystemData().isReleased()|| SecurityUtil.nathanLenovo()) {
            Login.showEntryGUI();
        } else {
            try {
                GenesisShare.getExitingSem().acquire();
                GenesisShare.getExitingSem().release();
                GenesisShare.exit(-600);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }
    }
}