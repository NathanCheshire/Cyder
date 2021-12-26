package cyder.genesis;

import cyder.handlers.internal.SessionHandler;
import cyder.utilities.IOUtil;
import cyder.utilities.SecurityUtil;
import cyder.utilities.SystemUtil;
import cyder.utilities.UserUtil;

//todo bugs: corrupting a user should not exit program simply remove users that could not be parsed
//todo login not printing and breaking bug
//todo userdata not being parsed correctly completely fucks program bug
//todo default preferences not working? windows should animate if the program isn't sure
//todo default prefs not working, if no users animations should still exist
//todo i don't give a shit what is in a json, if you can't serialize it, it's corrupted so corrupt function the folder


//{"name":"Nathan","pass":"a1ed49ffc79fec196dcc25555352cba3e74ddc0e474af08736a4410e0dfd695d","font":"Agency FB","foreground":"f0f0f0","background":"101010","intromusic":"0","debugwindows":"0","randombackground":"101010","outputborder":"0","inputborder":"0","hourlychimes":"1","silenceerrors":"0","fullscreen":"0","outputfill":"0","inputfill":"0","clockonconsole":"1","showseconds":"0","filterchat":"0","laststart":"1640477182534","minimizeonclose":"0","typinganimation":"1","showbusyicon":"0","ffmpegpath":"C:\\FFmpeg\\bin\\ffmpeg.exe","youtubedlpath":"C:\\Program Files\\youtube-dl\\youtube-dl.exe","windowlocx":"1392","windowlocy":"272","roundedwindows":"0","windowColor":"1A2033","consoleclockformat":"EEEEEEEEE h:mmaa","typingsound":"0","youtubeuuid":"aaaaaaaaaVp","ipkey":"8eac4e7ab34eb235c4a888bfdbedc8bb8093ec1490790d139cf58932","weatherkey":"2d790dd0766f1da62af488f101380c75","capsmode":"0","loggedin":"0","audiolength":"1","persistentnotifications":"0","closeAnimation":"1","minimizeAnimation":"1","consolePinned":"0","executables":[]}

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

        //makes sure all jsons are parsable and deletes the files found with non-parsable ones
        UserUtil.parseJsons();

        //launch splash screen
        CyderSplash.showSplash();

        //IOUtil necessary subroutines
        IOUtil.fixLogs();
        IOUtil.fixUsers();

        //IOUtil secondary subroutines
        new Thread(() -> {
            IOUtil.logArgs(CA);
            IOUtil.cleanSandbox();
            IOUtil.cleanUsers();
            IOUtil.deleteTempDir();
        },"Cyder Start Secondary Subroutines").start();

        //start exiting failsafe
        CyderSetup.initFrameChecker();

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