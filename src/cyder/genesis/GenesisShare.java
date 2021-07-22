package cyder.genesis;

import cyder.handler.ErrorHandler;
import cyder.handler.SessionLogger;
import cyder.obj.Preference;
import cyder.ui.ConsoleFrame;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class GenesisShare {
    //private constructor
    private GenesisShare() {}

    private static Semaphore exitingSem = new Semaphore(1);
    private static Semaphore printinSem = new Semaphore(1);

    public static Semaphore getExitingSem() {
        return exitingSem;
    }

    public static Semaphore getPrintinSem() {
        return printinSem;
    }

    private static boolean suspendFrameChecker = false;

    public static void suspendFrameChecker() {
        suspendFrameChecker = true;
    }

    public static void cancelFrameCheckerSuspention() {
        suspendFrameChecker = false;
    }

    public static boolean framesSuspended() {
        return suspendFrameChecker;
    }

    private static final LinkedList<Preference> prefs = initPreferencesList();

    public static LinkedList<Preference> getPrefs() {
        return prefs;
    }

    private static LinkedList<Preference> initPreferencesList() {
        LinkedList<Preference> ret = new LinkedList<>();

        ret.add(new Preference("name","IGNORE","IGNORE","IGNORE"));
        ret.add(new Preference("pass","IGNORE","IGNORE","IGNORE"));
        ret.add(new Preference("font","IGNORE","IGNORE","tahoma"));
        ret.add(new Preference("foreground","IGNORE","IGNORE","000000"));
        ret.add(new Preference("background","IGNORE","IGNORE","FFFFFF"));
        ret.add(new Preference("intromusic",
                "Intro Music","" +
                "Play intro music on start","0"));
        ret.add(new Preference("debugwindows",
                "Debug Windows",
                "Show debug menus on startup","0"));
        ret.add(new Preference("randombackground",
                "Random Background",
                "Choose a random background on startup","0"));
        ret.add(new Preference("outputborder",
                "Output Border",
                "Draw a border around the output area","0"));
        ret.add(new Preference("inputborder",
                "Input Border",
                "Draw a border around the input area","0"));
        ret.add(new Preference("hourlychimes",
                "Hourly Chimes",
                "Chime every hour","1"));
        ret.add(new Preference("silenceerrors",
                "Silence Errors",
                "Don't open errors externally","1"));
        ret.add(new Preference("fullscreen",
                "Fullscreen",
                "Fullscreen cyder (Extremely experimental)","0"));
        ret.add(new Preference("outputfill",
                "Output Fill",
                "Fill the output area with the color specified in the \"Fonts & Colors\" panel","0"));
        ret.add(new Preference("inputfill",
                "Input Fill",
                "Fill the input area with the color specified in the \"Fonts & Colors\" panel","0"));
        ret.add(new Preference("clockonconsole",
                "Clock On Console",
                "Show a clock at the top of the console","1"));
        ret.add(new Preference("showseconds",
                "Show Seconds",
                "Show seconds on the console clock if enabled","1"));
        ret.add(new Preference("filterchat",
                "Filter Chat",
                "Filter foul language","1"));
        ret.add(new Preference("menudirection", "Menu Minimize Direction",
                "Console Menu Minimize Direction","1"));
        ret.add(new Preference("laststart","IGNORE","IGNORE",
                System.currentTimeMillis() + ""));
        ret.add(new Preference("minimizeonclose","Minimize On Close",
                "Minimize the application instead of exiting whenever a close action is requested","0"));
        ret.add(new Preference("typinganimation","Typing Animation",
                "Typing Animation on console for non-vital outputs", "1"));
        ret.add(new Preference("showbusyicon", "Show Cyder Busy Icon",
                "Show when Cyder is busy by changing the tray icon","0"));
        ret.add(new Preference("executables","IGNORE","IGNORE","IGNORE"));
        //add future default data here

        return ret;
    }

    /**
     * Controled program exit that performs closing actions
     * and then calls System.exit which will also invoke the shutdown hook
     * @param code - the exiting code to describe why the program exited (0 is standard
     *             but for this program, the key/value pairs in ExitCodes.ini are followed)
     */
    public static void exit(int code) {
        try {
            //reset console frame, input handler, bletchy thread, youtube thread,
            // and other stuff linked to console frame
            if (!ConsoleFrame.getConsoleFrame().isClosed())
                ConsoleFrame.getConsoleFrame().close();

            //log exit code and end of log tag
            SessionLogger.log(SessionLogger.Tag.EXIT,null);
            SessionLogger.log(SessionLogger.Tag.EOL, code);

            //acquire and release to ensure no IO is currently underway
            GenesisShare.getExitingSem().acquire();
            GenesisShare.getExitingSem().release();

        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            System.exit(code);
        }
    }
}
