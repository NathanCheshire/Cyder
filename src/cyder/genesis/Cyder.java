package cyder.genesis;

import cyder.handler.SessionLogger;
import cyder.testing.DebugConsole;
import cyder.utilities.IOUtil;
import cyder.utilities.SecurityUtil;
import cyder.utilities.SystemUtil;

public class Cyder {
    /**
     * Setup and start the best program ever made :D
     * @param CA possible command line args passed in. They serve no purpose yet
     *           but we shall log them regardless (just like Big Brother would want)
     */
    public static void main(String[] CA)  {
        //set shutdown hook
        CyderSetup.addCommonExitHook();

        //start session logger
        SessionLogger.SessionLogger();
        SessionLogger.log(SessionLogger.Tag.ENTRY, SystemUtil.getWindowsUsername());

        //CyderSetup subroutines
        CyderSetup.initSystemProperties();
        CyderSetup.initUIManager();

        //possibly fatal subroutines
        if (!CyderSetup.registerFonts()) {
            CyderSetup.exceptionExit("Font required by system could not be loaded","Font failure");
            return;
        }

        if (IOUtil.checkForExitCollisions()) {
            CyderSetup.duplicateExitCodesExit();
            return;
        }

        if (SystemUtil.osxSystem()) {
            CyderSetup.osxExit();
            return;
        }

        //IOUtil subroutines
        IOUtil.cleanUsers();
        IOUtil.deleteTempDir();
        IOUtil.logArgs(CA);
        IOUtil.cleanSandbox();
        IOUtil.fixLogs();
        IOUtil.fixUsers();

        //start exiting failsafe
        CyderSetup.initFrameChecker();

        //figure out how to enter program
        if (SecurityUtil.nathanLenovo())  {
            if (IOUtil.getSystemData().isTestingmode()) {
                SessionLogger.log(SessionLogger.Tag.ENTRY, "TESTING MODE");
                DebugConsole.launchTests();
            } else if (IOUtil.getSystemData().isAutocypher()) {
                SessionLogger.log(SessionLogger.Tag.LOGIN, "AUTOCYPHER ATTEMPT");
                Login.autoCypher();
            } else {
                Login.showGUI();
            }
        } else if (IOUtil.getSystemData().isReleased()) {
            Login.showGUI();
        } else {
            GenesisShare.exit(-600);
        }
    }
}