package cyder.genesis;

import cyder.handler.ErrorHandler;
import cyder.handler.SessionLogger;
import cyder.threads.CyderThreadFactory;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.UserUtil;

import java.awt.*;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static java.util.concurrent.TimeUnit.SECONDS;

public class GenesisShare {
    private GenesisShare() {}

    private static Semaphore exitingSem = new Semaphore(1);
    private static Semaphore printingSem = new Semaphore(1);

    public static Semaphore getExitingSem() {
        return exitingSem;
    }

    public static Semaphore getPrintingSem() {
        return printingSem;
    }

    private static boolean suspendFrameChecker = false;

    public static void suspendFrameChecker() {
        suspendFrameChecker = true;
    }

    public static void resumeFrameChecker() {
        suspendFrameChecker = false;
    }

    public static void startFinalFrameDisposedChecker() {
        Executors.newSingleThreadScheduledExecutor(
                new CyderThreadFactory("Final Frame Disposed Checker")).scheduleAtFixedRate(() -> {
            Frame[] frames = Frame.getFrames();
            int validFrames = 0;

            for (Frame f : frames) {
                if (f.isShowing()) {
                    validFrames++;
                }
            }

            if (validFrames < 1 && !suspendFrameChecker) {
                GenesisShare.exit(120);
            }
        }, 10, 5, SECONDS);
    }

    private static final LinkedList<Preference> prefs = initPreferencesList();

    public static LinkedList<Preference> getPrefs() {
        return prefs;
    }

    private static LinkedList<Preference> initPreferencesList() {
        LinkedList<Preference> ret = new LinkedList<>();

        ret.add(new Preference("name","IGNORE","IGNORE","IGNORE"));
        ret.add(new Preference("pass","IGNORE","IGNORE","IGNORE"));
        ret.add(new Preference("font","IGNORE","","Agency FB"));
        ret.add(new Preference("foreground","IGNORE","","f0f0f0"));
        ret.add(new Preference("background","IGNORE","","101010"));
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
        ret.add(new Preference("laststart","IGNORE","",
                System.currentTimeMillis() + ""));
        ret.add(new Preference("minimizeonclose","Minimize On Close",
                "Minimize the application instead of exiting whenever a close action is requested","0"));
        ret.add(new Preference("typinganimation","Typing Animation",
                "Typing animation on console for non-vital outputs", "1"));
        ret.add(new Preference("typingsound","Typing Animation Sound",
                "Typing animation sound effect to play if typing animation is enabled","1"));
        ret.add(new Preference("showbusyicon", "Show Cyder Busy Icon",
                "Show when Cyder is busy by changing the tray icon","0"));
        ret.add(new Preference("ffmpegpath","IGNORE","",""));
        ret.add(new Preference("youtubedlpath","IGNORE","",""));
        ret.add(new Preference("windowlocx","IGNORE","","-80000"));
        ret.add(new Preference("windowlocy","IGNORE","","-80000"));
        ret.add(new Preference("roundedwindows","Rounded Windows",
                "Make certain windows rounded","0"));
        ret.add(new Preference("windowcolor","IGNORE","","1A2033"));
        ret.add(new Preference("consoleclockformat","IGNORE","","EEEEEEEEE h:mmaa"));
        ret.add(new Preference("youtubeuuid","IGNORE","","aaaaaaaaaaa"));
        ret.add(new Preference("ipkey","IGNORE","",""));
        ret.add(new Preference("weatherkey","IGNORE","",""));
        ret.add(new Preference("capsmode","Capital Letters Mode","Capitalize all console output","0"));
        ret.add(new Preference("loggedin","IGNORE","","0"));

        // IGNORE for display name means ignore for UserEditor,
        // IGNORE for tooltip means don't write when creating user since it was already set

        //adding future prefs: you'll need to add the preference here and also the data in user.java
        // since gson parses the userdata.json into a user object.
        // some rare cases might require deeper manipulation such as the case for executables

        return ret;
    }

    /**
     * Controled program exit that calls System.exit which will also invoke the shutdown hook
     * @param code the exiting code to describe why the program exited (0 is standard
     *             but for this program, the key/value pairs in Sys.json are followed)
     */
    public static void exit(int code) {
        try {
            //log exit code and end of log tag
            SessionLogger.log(SessionLogger.Tag.EXIT,null);
            SessionLogger.log(SessionLogger.Tag.EOL, code);

            //sign user out in logs
            UserUtil.setUserData("loggedin","false");

            //acquire and release sems to ensure no IO is currently underway
            GenesisShare.getExitingSem().acquire();
            GenesisShare.getExitingSem().release();
            UserUtil.getJsonIOSem().acquire();
            UserUtil.getJsonIOSem().release();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            System.exit(code);
        }
    }

    public static CyderFrame getDominantFrame() {
        if (!ConsoleFrame.getConsoleFrame().isClosed() && ConsoleFrame.getConsoleFrame() != null) {
            return ConsoleFrame.getConsoleFrame().getConsoleCyderFrame();
        } else if (!Login.isClosed() && Login.getLoginFrame() != null){
            return Login.getLoginFrame();
        }
        //other possibly dominant/stand-alone frame checks here
        else {
            return null;
        }
    }

    private static boolean quesitonableInternet;

    public static boolean isQuesitonableInternet() {
        return quesitonableInternet;
    }

    public static void setQuesitonableInternet(boolean quesitonableInternet) {
        GenesisShare.quesitonableInternet = quesitonableInternet;
    }

    //pref class

    public static class Preference<T> {
        private String ID;
        private String displayName;
        private String tooltip;
        private T defaultValue;

        public Preference(String id, String displayName, String tooltip, T defaultValue) {
            this.ID = id;
            this.displayName = displayName;
            this.tooltip = tooltip;
            this.defaultValue = defaultValue;
        }

        public String getID() {
            return ID;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getTooltip() {
            return tooltip;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public void setTooltip(String tooltip) {
            this.tooltip = tooltip;
        }

        public void setDefaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public String toString() {
            return "Preference object: (" +
                    this.getID() + "," +
                    this.getDisplayName() + "," +
                    this.getTooltip() + "," +
                    this.defaultValue +
                    "), hash=" + this.hashCode();
        }
    }
}
