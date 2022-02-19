package cyder.user;

import cyder.utilities.ReflectionUtil;

import java.util.LinkedList;

@SuppressWarnings("UnusedReturnValue") /* lots of things are simply setting */
public class User {
    // ------------------------------------
    // primitive data types. In the future, allow this to be anything
    // which will require methods that use generic classes and instanceof operators.
    // ------------------------------------

    private String name;
    private String pass;
    private String font;
    private String fontmetric;
    private String fontsize;
    private String foreground;
    private String background;
    private String intromusic; 
    private String debugwindows; 
    private String randombackground; 
    private String outputborder; 
    private String inputborder; 
    private String hourlychimes; 
    private String silenceerrors; 
    private String fullscreen; 
    private String outputfill; 
    private String inputfill; 
    private String clockonconsole; 
    private String showseconds; 
    private String filterchat; 
    private String laststart;
    private String minimizeonclose; 
    private String typinganimation; 
    private String showbusyicon; 
    private String ffmpegpath;
    private String youtubedlpath;
    private String roundedwindows; 
    private String windowColor;
    private String consoleclockformat;
    private String typingsound; 
    private String youtubeuuid;
    private String ipkey;
    private String weatherkey;
    private String capsmode; 
    private String loggedin; 
    private String audiolength;
    private String persistentnotifications;
    private String closeAnimation;
    private String minimizeAnimation;
    private String compactTextMode;

    // -------------------
    // non primitive types
    // -------------------

    /**
     * The screen stat object which holds the console frame's position,
     * size, pinned, monitor, and rotation vars.
     */
    private ScreenStat screenStat;

    // -------------------
    // non primitive lists
    // -------------------

    /**
     * List of mapped executables that map a string to a file path.
     */
    private LinkedList<MappedExecutable> executables;

    /**
     * Default constructor not restricted due to GSON json parsing.
     */
    public User() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getForeground() {
        return foreground;
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getIntromusic() {
        return intromusic;
    }

    public void setIntromusic(String intromusic) {
        this.intromusic = intromusic;
    }

    public String getDebugwindows() {
        return debugwindows;
    }

    public void setDebugwindows(String debugwindows) {
        this.debugwindows = debugwindows;
    }

    public String getRandombackground() {
        return randombackground;
    }

    public void setRandombackground(String randombackground) {
        this.randombackground = randombackground;
    }

    public String getOutputborder() {
        return outputborder;
    }

    public void setOutputborder(String outputborder) {
        this.outputborder = outputborder;
    }

    public String getInputborder() {
        return inputborder;
    }

    public void setInputborder(String inputborder) {
        this.inputborder = inputborder;
    }

    public String getHourlychimes() {
        return hourlychimes;
    }

    public void setHourlychimes(String hourlychimes) {
        this.hourlychimes = hourlychimes;
    }

    public String getSilenceerrors() {
        return silenceerrors;
    }

    public void setSilenceerrors(String silenceerrors) {
        this.silenceerrors = silenceerrors;
    }

    public String getFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(String fullscreen) {
        this.fullscreen = fullscreen;
    }

    public String getOutputfill() {
        return outputfill;
    }

    public void setOutputfill(String outputfill) {
        this.outputfill = outputfill;
    }

    public String getInputfill() {
        return inputfill;
    }

    public void setInputfill(String inputfill) {
        this.inputfill = inputfill;
    }

    public String getClockonconsole() {
        return clockonconsole;
    }

    public void setClockonconsole(String clockonconsole) {
        this.clockonconsole = clockonconsole;
    }

    public String getShowseconds() {
        return showseconds;
    }

    public void setShowseconds(String showseconds) {
        this.showseconds = showseconds;
    }

    public String getFilterchat() {
        return filterchat;
    }

    public void setFilterchat(String filterchat) {
        this.filterchat = filterchat;
    }

    public String getLaststart() {
        return laststart;
    }

    public void setLaststart(String laststart) {
        this.laststart = laststart;
    }

    public String getMinimizeonclose() {
        return minimizeonclose;
    }

    public void setMinimizeonclose(String minimizeonclose) {
        this.minimizeonclose = minimizeonclose;
    }

    public String getTypinganimation() {
        return typinganimation;
    }

    public void setTypinganimation(String typinganimation) {
        this.typinganimation = typinganimation;
    }

    public String getShowbusyicon() {
        return showbusyicon;
    }

    public void setShowbusyicon(String showbusyicon) {
        this.showbusyicon = showbusyicon;
    }

    public String getFfmpegpath() {
        return ffmpegpath;
    }

    public void setFfmpegpath(String ffmpegpath) {
        this.ffmpegpath = ffmpegpath;
    }

    public String getYoutubedlpath() {
        return youtubedlpath;
    }

    public void setYoutubedlpath(String youtubedlpath) {
        this.youtubedlpath = youtubedlpath;
    }

    public String getRoundedwindows() {
        return roundedwindows;
    }

    public void setRoundedwindows(String roundedwindows) {
        this.roundedwindows = roundedwindows;
    }

    public String getWindowColor() {
        return windowColor;
    }

    public void setWindowColor(String windowColor) {
        this.windowColor = windowColor;
    }

    public String getConsoleclockformat() {
        return consoleclockformat;
    }

    public void setConsoleclockformat(String consoleclockformat) {
        this.consoleclockformat = consoleclockformat;
    }

    public String getTypingsound() {
        return typingsound;
    }

    public void setTypingsound(String typingsound) {
        this.typingsound = typingsound;
    }

    public String getYoutubeuuid() {
        return youtubeuuid;
    }

    public void setYoutubeuuid(String youtubeuuid) {
        this.youtubeuuid = youtubeuuid;
    }

    public String getIpkey() {
        return ipkey;
    }

    public void setIpkey(String ipkey) {
        this.ipkey = ipkey;
    }

    public String getWeatherkey() {
        return weatherkey;
    }

    public void setWeatherkey(String weatherkey) {
        this.weatherkey = weatherkey;
    }

    public String getCapsmode() {
        return capsmode;
    }

    public void setCapsmode(String capsmode) {
        this.capsmode = capsmode;
    }

    public LinkedList<MappedExecutable> getExecutables() {
        return executables;
    }

    public void setExecutables(LinkedList<MappedExecutable> executables) {
        this.executables = executables;
    }

    public String isLoggedin() {
        return loggedin;
    }

    public void setLoggedin(String loggedin) {
        this.loggedin = loggedin;
    }

    public String getLoggedin() {
        return loggedin;
    }

    public String getAudiolength() {
        return audiolength;
    }

    public void setAudiolength(String audiolength) {
        this.audiolength = audiolength;
    }

    public String getPersistentnotifications() {
        return persistentnotifications;
    }

    public void setPersistentnotifications(String persistentnotifications) {
        this.persistentnotifications = persistentnotifications;
    }

    public String getCloseAnimation() {
        return closeAnimation;
    }

    public void setCloseAnimation(String closeAnimation) {
        this.closeAnimation = closeAnimation;
    }

    public String getMinimizeAnimation() {
        return minimizeAnimation;
    }

    public void setMinimizeAnimation(String minimizeAnimation) {
        this.minimizeAnimation = minimizeAnimation;
    }

    public ScreenStat getScreenStat() {
        return screenStat;
    }

    public User setScreenStat(ScreenStat screenStat) {
        this.screenStat = screenStat;
        return this;
    }

    public String getCompactTextMode() {
        return compactTextMode;
    }

    public void setCompactTextMode(String compactTextMode) {
        this.compactTextMode = compactTextMode;
    }

    public String getFontmetric() {
        return fontmetric;
    }

    public void setFontmetric(String fontmetric) {
        this.fontmetric = fontmetric;
    }

    public String getFontsize() {
        return fontsize;
    }

    public void setFontsize(String fontsize) {
        this.fontsize = fontsize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }

    /**
     * Class representing a name and a path to an executable/link to open.
     */
    public static class MappedExecutable {
        private final String name;
        private final String filepath;

        public MappedExecutable(String name, String filepath) {
            this.name = name;
            this.filepath = filepath;
        }

        public String getName() {
            return name;
        }

        public String getFilepath() {
            return filepath;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MappedExecutable) {
                return ((MappedExecutable) o).getName().equals(this.getName())
                        || ((MappedExecutable) o).getFilepath().equals(this.getFilepath());
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return ReflectionUtil.commonCyderToString(this);
        }

        @Override
        public int hashCode() {
            int ret = this.name.hashCode();
            ret = 31 * ret + filepath.hashCode();
            return ret;
        }
    }

    /**
     * Object to store statistics about the ConsoleFrame and where it is.
     */
    public static class ScreenStat {
        private int consoleX;
        private int consoleY;
        private int consoleWidth;
        private int consoleHeight;
        private int monitor;
        private boolean consoleOnTop;

        public ScreenStat() {}

        public ScreenStat(int consoleX, int consoleY, int consoleWidth,
                          int consoleHeight, int monitor, boolean consoleOnTop) {
            this.consoleX = consoleX;
            this.consoleY = consoleY;
            this.consoleWidth = consoleWidth;
            this.consoleHeight = consoleHeight;
            this.monitor = monitor;
            this.consoleOnTop = consoleOnTop;
        }

        public int getConsoleX() {
            return consoleX;
        }

        public void setConsoleX(int consoleX) {
            this.consoleX = consoleX;
        }

        public int getConsoleY() {
            return consoleY;
        }

        public void setConsoleY(int consoleY) {
            this.consoleY = consoleY;
        }

        public int getConsoleWidth() {
            return consoleWidth;
        }

        public void setConsoleWidth(int consoleWidth) {
            this.consoleWidth = consoleWidth;
        }

        public int getConsoleHeight() {
            return consoleHeight;
        }

        public void setConsoleHeight(int consoleHeight) {
            this.consoleHeight = consoleHeight;
        }

        public int getMonitor() {
            return monitor;
        }

        public void setMonitor(int monitor) {
            this.monitor = monitor;
        }

        public boolean isConsoleOnTop() {
            return consoleOnTop;
        }

        public void setConsoleOnTop(boolean consoleOnTop) {
            this.consoleOnTop = consoleOnTop;
        }

        @Override
        public String toString() {
            return "[" + this.consoleX + ", " + this.consoleY + ", "
                    + this.consoleWidth + ", " + this.consoleHeight + ", "
                    + this.monitor + ", " + this.consoleOnTop + "]";
        }
    }
}
