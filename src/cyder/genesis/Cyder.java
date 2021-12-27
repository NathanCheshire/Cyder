package cyder.genesis;

import cyder.handlers.internal.SessionHandler;
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
        //set start time
        GenesisShare.setAbsoluteStartTime(System.currentTimeMillis());

        //set shutdown hook
        CyderSetup.addCommonExitHook();

        //start session logger
        SessionHandler.SessionLogger();
        SessionHandler.log(SessionHandler.Tag.ENTRY, SystemUtil.getWindowsUsername());

        //CyderSetup subroutines
        CyderSetup.initSystemProperties();
        CyderSetup.initUIManager();

        //possibly fatal subroutines
        if (!CyderSetup.registerFonts()) {
            SessionHandler.log(SessionHandler.Tag.EXCEPTION, "SYSTEM FAILURE");
            CyderSetup.exceptionExit("Font required by system could not be loaded","Font failure");
            return;
        }

        if (IOUtil.checkForExitCollisions()) {
            SessionHandler.log(SessionHandler.Tag.EXCEPTION, "DUPLICATE EXIT CODES");
            CyderSetup.exceptionExit("You messed up exit codes :/","Exit Codes Exception");
            return;
        }

        if (SystemUtil.osxSystem()) {
            SessionHandler.log(SessionHandler.Tag.EXCEPTION, "IMPROPER OS");
            CyderSetup.exceptionExit("System OS not intended for Cyder use. You should" +
                    " install a dual boot or a VM or something.","OS Exception");
            return;
        }

        //IOUtil necessary subroutines to complete with success before continuing
        IOUtil.fixUsers();
        IOUtil.fixLogs();

        //IOUtil secondary subroutines
        new Thread(() -> {
            IOUtil.logArgs(CA);
            IOUtil.cleanSandbox();
            IOUtil.cleanUsers();
            IOUtil.deleteTempDir();
        },"Cyder Start Secondary Subroutines").start();

        //start exiting failsafe
        CyderSetup.initFrameChecker();

        //launch splash screen
        CyderSplash.showSplash();

        //figure out how to enter program
        if (SecurityUtil.nathanLenovo())  {
            if (IOUtil.getSystemData().isAutocypher()) {
                SessionHandler.log(SessionHandler.Tag.LOGIN, "AUTOCYPHER ATTEMPT");
                boolean ret = Login.autoCypher();

                if (!ret) {
                    SessionHandler.log(SessionHandler.Tag.LOGIN, "AUTOCYPHER FAIL");
                    Login.showGUI();
                } else {} //AutoCypher spun off console frame, no further action necessary
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