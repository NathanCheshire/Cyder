package cyder.genesis;

import cyder.handlers.internal.LoginHandler;
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
        IOUtil.checkSystemData();
        IOUtil.fixUsers();
        IOUtil.fixLogs();
        IOUtil.cleanUsers();

        //IOUtil secondary subroutines that can be executed when program has started essentially
        new Thread(() -> {
            IOUtil.logArgs(CA);
            IOUtil.cleanSandbox();
            IOUtil.deleteTempDir();
        },"Cyder Start Secondary Subroutines").start();

        //start GUI exiting failsafe
        CyderSetup.initFrameChecker();

        //todo can we use this sooner? CyderSplash.setLoadingMessage("message");
        //launch splash screen
        CyderSplash.showSplash();

        //figure out how to enter program
        if (SecurityUtil.nathanLenovo())  {
            CyderSplash.setLoadingMessage("Checking for autocypher");
            CyderSplash.setLoadingMessage("Checking for autocypher");
            if (IOUtil.getSystemData().isAutocypher()) {
                SessionHandler.log(SessionHandler.Tag.LOGIN, "AUTOCYPHER ATTEMPT");
                CyderSplash.setLoadingMessage("Autocyphering");
                boolean ret = LoginHandler.autoCypher();

                if (!ret) {
                    SessionHandler.log(SessionHandler.Tag.LOGIN, "AUTOCYPHER FAIL");
                    LoginHandler.showGUI();
                }
                else {} //AutoCypher spun off console frame, no further action necessary
            }
            else LoginHandler.showGUI();
        }
        else if (IOUtil.getSystemData().isReleased()) LoginHandler.showGUI();
        else GenesisShare.exit(-600);
    }
}