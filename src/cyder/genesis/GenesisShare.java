package cyder.genesis;

import cyder.genobjects.Preference;
import cyder.genobjects.User;
import cyder.handler.ErrorHandler;
import cyder.handler.SessionLogger;
import cyder.threads.CyderThreadFactory;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;

import java.awt.*;
import java.lang.reflect.Method;
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

    public static void cancelFrameCheckerSuspention() {
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

            if (validFrames < 1 && !GenesisShare.framesSuspended()) {
                GenesisShare.exit(120);
            }
        }, 10, 5, SECONDS);
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
        ret.add(new Preference("font","IGNORE","","tahoma"));
        ret.add(new Preference("foreground","IGNORE","","000000"));
        ret.add(new Preference("background","IGNORE","","FFFFFF"));
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
        ret.add(new Preference("laststart","IGNORE","",
                System.currentTimeMillis() + ""));
        ret.add(new Preference("minimizeonclose","Minimize On Close",
                "Minimize the application instead of exiting whenever a close action is requested","0"));
        ret.add(new Preference("typinganimation","Typing Animation",
                "Typing Animation on console for non-vital outputs", "1"));
        ret.add(new Preference("showbusyicon", "Show Cyder Busy Icon",
                "Show when Cyder is busy by changing the tray icon","0"));
        ret.add(new Preference("ffmpegpath","IGNORE","",""));
        ret.add(new Preference("youtubedlpath","IGNORE","",""));
        ret.add(new Preference("windowlocx","IGNORE","","-80000"));
        ret.add(new Preference("windowlocy","IGNORE","","-80000"));
        ret.add(new Preference("roundedwindows","Rounded Windows",
                "Make certain windows rounded","false"));

        // IGNORE for display name means ignore for UserEditor,
        // IGNORE for tooltip means don't write when creating user since it was already set

        //adding future prefs: you'll need to add the preference here and also the data in user.java
        // since gson parses the userdata.json into a user object.
        // some rare cases might require deeper manipulation such as the case for executables

        return ret;
    }

    //todo method to parse an old json file and add default new prefs to it

    public User getDefaultUser() {
        User ret = new User();

        //get all methods of user
        for (Method m : ret.getClass().getMethods()) {
            //make sure it's a setter with one parameter
            if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
                //parse away set from method name and find default preference from list above
                String methodName = m.getName().replace("set","");
                String data = null;

                //methods should follow set standards so that this will work
                // (method names should be sub-names of other methods)
                for (Preference pref : getPrefs()) {
                    if (pref.getID().equalsIgnoreCase(methodName)); {
                        data = pref.getDefaultValue();
                    }
                }

                try {
                    m.invoke(ret, data);
                } catch (Exception e) {
                    // :/ not sure what happened here
                    ErrorHandler.silentHandle(e);
                }
            }
        }

        //exernal things stored in a user aside from preferences
        ret.setExecutables(null);

        return ret;
    }

    /**
     * Controled program exit that performs closing actions
     * and then calls System.exit which will also invoke the shutdown hook
     * @param code - the exiting code to describe why the program exited (0 is standard
     *             but for this program, the key/value pairs in Sys.json are followed)
     */
    public static void exit(int code) {
        try {
            //reset console frame, input handler, bletchy thread, YouTube thread,
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

    public static CyderFrame getDominantFrame() {
        if (!ConsoleFrame.getConsoleFrame().isClosed()) {
            return ConsoleFrame.getConsoleFrame().getConsoleCyderFrame();
        }

        return Login.getFrame();
    }

    private static boolean quesitonableInternet;

    public static boolean isQuesitonableInternet() {
        return quesitonableInternet;
    }

    public static void setQuesitonableInternet(boolean quesitonableInternet) {
        GenesisShare.quesitonableInternet = quesitonableInternet;
    }
}
