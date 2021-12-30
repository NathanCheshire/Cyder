package cyder.cyderuser;

import java.util.LinkedList;

public class User {
    private String name;
    private String pass;
    private String font;
    private String foreground;
    private String background;
    private String intromusic; //boolean
    private String debugwindows; //boolean
    private String randombackground; //boolean
    private String outputborder; //boolean
    private String inputborder; //boolean
    private String hourlychimes; //boolean
    private String silenceerrors; //boolean
    private String fullscreen; //boolean
    private String outputfill; //boolean
    private String inputfill; //boolean
    private String clockonconsole; //boolean
    private String showseconds; //boolean
    private String filterchat; //boolean
    private String laststart; //long
    private String minimizeonclose; //boolean
    private String typinganimation; //boolean
    private String showbusyicon; //boolean
    private String ffmpegpath;
    private String youtubedlpath;
    private String windowlocx;
    private String windowlocy;
    private String roundedwindows; //boolean
    private String windowColor;
    private String consoleclockformat;
    private String typingsound; //boolean
    private String youtubeuuid;
    private String ipkey;
    private String weatherkey;
    private String capsmode; //boolean
    private String loggedin; //boolean
    private String audiolength;
    private String persistentnotifications;
    private String closeAnimation;
    private String minimizeAnimation;
    private String consolePinned;
    private LinkedList<MappedExecutable> executables;

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

    public String getWindowlocx() {
        return windowlocx;
    }

    public void setWindowlocx(String windowlocx) {
        this.windowlocx = windowlocx;
    }

    public String getWindowlocy() {
        return windowlocy;
    }

    public void setWindowlocy(String windowlocy) {
        this.windowlocy = windowlocy;
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

    public String getConsolePinned() {
        return consolePinned;
    }

    public void setConsolePinned(String consolePinned) {
        this.consolePinned = consolePinned;
    }

    //inner classes

    public static class MappedExecutable {
        private String name;
        private String filepath;

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

        public void setName(String name) {
            this.name = name;
        }

        public void setFilepath(String filepath) {
            this.filepath = filepath;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MappedExecutable) {
                return ((MappedExecutable) o).getName().equals(this.getName())
                        && ((MappedExecutable) o).getFilepath().equals(this.getFilepath());
            } else {
                return false;
            }
        }
    }
}
