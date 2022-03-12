package cyder.user;

import cyder.handlers.internal.Logger;
import cyder.user.objects.MappedExecutable;
import cyder.user.objects.ScreenStat;
import cyder.utilities.ReflectionUtil;

import java.util.LinkedList;

/**
 * A user object which holds all relavent data about a Cyder user.
 * Note that the default constructor is not restricted due to GSON parsing.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"}) /* lots of things are invoked via reflection */
public class User {
    /**
     * Creates a new User object.
     */
    public User() {
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

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
    private String windowcolor;
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
    private String youTubeAPI3Key;
    private String wrapterminal;

    // -------------------
    // non primitive types
    // -------------------

    /**
     * The screen stat object which holds the console frame's position,
     * size, pinned, monitor, and rotation vars.
     */
    private ScreenStat screenStat;

    // --------------------------
    // data structures of objects
    // --------------------------

    /**
     * List of mapped executables that map a string to a file path.
     */
    private LinkedList<MappedExecutable> executables;

    // -------
    // getters
    // -------

    public String getName() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    public String getFont() {
        return font;
    }

    public String getFontmetric() {
        return fontmetric;
    }

    public String getFontsize() {
        return fontsize;
    }

    public String getForeground() {
        return foreground;
    }

    public String getBackground() {
        return background;
    }

    public String getIntromusic() {
        return intromusic;
    }

    public String getDebugwindows() {
        return debugwindows;
    }

    public String getRandombackground() {
        return randombackground;
    }

    public String getOutputborder() {
        return outputborder;
    }

    public String getInputborder() {
        return inputborder;
    }

    public String getHourlychimes() {
        return hourlychimes;
    }

    public String getSilenceerrors() {
        return silenceerrors;
    }

    public String getFullscreen() {
        return fullscreen;
    }

    public String getOutputfill() {
        return outputfill;
    }

    public String getInputfill() {
        return inputfill;
    }

    public String getClockonconsole() {
        return clockonconsole;
    }

    public String getShowseconds() {
        return showseconds;
    }

    public String getFilterchat() {
        return filterchat;
    }

    public String getLaststart() {
        return laststart;
    }

    public String getMinimizeonclose() {
        return minimizeonclose;
    }

    public String getTypinganimation() {
        return typinganimation;
    }

    public String getShowbusyicon() {
        return showbusyicon;
    }

    public String getFfmpegpath() {
        return ffmpegpath;
    }

    public String getYoutubedlpath() {
        return youtubedlpath;
    }

    public String getRoundedwindows() {
        return roundedwindows;
    }

    public String getWindowcolor() {
        return windowcolor;
    }

    public String getConsoleclockformat() {
        return consoleclockformat;
    }

    public String getTypingsound() {
        return typingsound;
    }

    public String getYoutubeuuid() {
        return youtubeuuid;
    }

    public String getIpkey() {
        return ipkey;
    }

    public String getWeatherkey() {
        return weatherkey;
    }

    public String getCapsmode() {
        return capsmode;
    }

    public String getLoggedin() {
        return loggedin;
    }

    public String getAudiolength() {
        return audiolength;
    }

    public String getPersistentnotifications() {
        return persistentnotifications;
    }

    public String getCloseAnimation() {
        return closeAnimation;
    }

    public String getMinimizeAnimation() {
        return minimizeAnimation;
    }

    public String getCompactTextMode() {
        return compactTextMode;
    }

    public String getYouTubeAPI3Key() {
        return youTubeAPI3Key;
    }

    public String getWrapterminal() {
        return wrapterminal;
    }

    public ScreenStat getScreenStat() {
        return screenStat;
    }

    public LinkedList<MappedExecutable> getExecutables() {
        return executables;
    }

    // -------
    // setters
    // -------

    public void setName(String name) {
        this.name = name;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public void setFontmetric(String fontmetric) {
        this.fontmetric = fontmetric;
    }

    public void setFontsize(String fontsize) {
        this.fontsize = fontsize;
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public void setIntromusic(String intromusic) {
        this.intromusic = intromusic;
    }

    public void setDebugwindows(String debugwindows) {
        this.debugwindows = debugwindows;
    }

    public void setRandombackground(String randombackground) {
        this.randombackground = randombackground;
    }

    public void setOutputborder(String outputborder) {
        this.outputborder = outputborder;
    }

    public void setInputborder(String inputborder) {
        this.inputborder = inputborder;
    }

    public void setHourlychimes(String hourlychimes) {
        this.hourlychimes = hourlychimes;
    }

    public void setSilenceerrors(String silenceerrors) {
        this.silenceerrors = silenceerrors;
    }

    public void setFullscreen(String fullscreen) {
        this.fullscreen = fullscreen;
    }

    public void setOutputfill(String outputfill) {
        this.outputfill = outputfill;
    }

    public void setInputfill(String inputfill) {
        this.inputfill = inputfill;
    }

    public void setClockonconsole(String clockonconsole) {
        this.clockonconsole = clockonconsole;
    }

    public void setShowseconds(String showseconds) {
        this.showseconds = showseconds;
    }

    public void setFilterchat(String filterchat) {
        this.filterchat = filterchat;
    }

    public void setLaststart(String laststart) {
        this.laststart = laststart;
    }

    public void setMinimizeonclose(String minimizeonclose) {
        this.minimizeonclose = minimizeonclose;
    }

    public void setTypinganimation(String typinganimation) {
        this.typinganimation = typinganimation;
    }

    public void setShowbusyicon(String showbusyicon) {
        this.showbusyicon = showbusyicon;
    }

    public void setFfmpegpath(String ffmpegpath) {
        this.ffmpegpath = ffmpegpath;
    }

    public void setYoutubedlpath(String youtubedlpath) {
        this.youtubedlpath = youtubedlpath;
    }

    public void setRoundedwindows(String roundedwindows) {
        this.roundedwindows = roundedwindows;
    }

    public void setWindowcolor(String windowcolor) {
        this.windowcolor = windowcolor;
    }

    public void setConsoleclockformat(String consoleclockformat) {
        this.consoleclockformat = consoleclockformat;
    }

    public void setTypingsound(String typingsound) {
        this.typingsound = typingsound;
    }

    public void setYoutubeuuid(String youtubeuuid) {
        this.youtubeuuid = youtubeuuid;
    }

    public void setIpkey(String ipkey) {
        this.ipkey = ipkey;
    }

    public void setWeatherkey(String weatherkey) {
        this.weatherkey = weatherkey;
    }

    public void setCapsmode(String capsmode) {
        this.capsmode = capsmode;
    }

    public void setLoggedin(String loggedin) {
        this.loggedin = loggedin;
    }

    public void setAudiolength(String audiolength) {
        this.audiolength = audiolength;
    }

    public void setPersistentnotifications(String persistentnotifications) {
        this.persistentnotifications = persistentnotifications;
    }

    public void setCloseAnimation(String closeAnimation) {
        this.closeAnimation = closeAnimation;
    }

    public void setMinimizeAnimation(String minimizeAnimation) {
        this.minimizeAnimation = minimizeAnimation;
    }

    public void setCompactTextMode(String compactTextMode) {
        this.compactTextMode = compactTextMode;
    }

    public void setYouTubeAPI3Key(String youTubeAPI3Key) {
        this.youTubeAPI3Key = youTubeAPI3Key;
    }

    public void setWrapterminal(String wrapterminal) {
        this.wrapterminal = wrapterminal;
    }

    public void setScreenStat(ScreenStat screenStat) {
        this.screenStat = screenStat;
    }

    public void setExecutables(LinkedList<MappedExecutable> executables) {
        this.executables = executables;
    }

    // ------------------------------------------
    // methods that are good practice to override
    // ------------------------------------------

    @Override
    public int hashCode() {
        int ret = name.hashCode();

        ret = 31 * ret + name.hashCode();
        ret = 31 * ret + pass.hashCode();
        ret = 31 * ret + font.hashCode();
        ret = 31 * ret + fontmetric.hashCode();
        ret = 31 * ret + foreground.hashCode();
        ret = 31 * ret + background.hashCode();
        ret = 31 * ret + intromusic.hashCode();
        ret = 31 * ret + debugwindows.hashCode();
        ret = 31 * ret + inputborder.hashCode();
        ret = 31 * ret + outputborder.hashCode();
        ret = 31 * ret + hourlychimes.hashCode();
        ret = 31 * ret + silenceerrors.hashCode();
        ret = 31 * ret + fullscreen.hashCode();
        ret = 31 * ret + outputfill.hashCode();
        ret = 31 * ret + inputfill.hashCode();
        ret = 31 * ret + clockonconsole.hashCode();
        ret = 31 * ret + showseconds.hashCode();
        ret = 31 * ret + filterchat.hashCode();
        ret = 31 * ret + laststart.hashCode();
        ret = 31 * ret + minimizeAnimation.hashCode();
        ret = 31 * ret + typinganimation.hashCode();
        ret = 31 * ret + showbusyicon.hashCode();
        ret = 31 * ret + ffmpegpath.hashCode();
        ret = 31 * ret + youtubedlpath.hashCode();
        ret = 31 * ret + roundedwindows.hashCode();
        ret = 31 * ret + windowcolor.hashCode();
        ret = 31 * ret + consoleclockformat.hashCode();
        ret = 31 * ret + typingsound.hashCode();
        ret = 31 * ret + youtubeuuid.hashCode();
        ret = 31 * ret + ipkey.hashCode();
        ret = 31 * ret + weatherkey.hashCode();
        ret = 31 * ret + capsmode.hashCode();
        ret = 31 * ret + loggedin.hashCode();
        ret = 31 * ret + audiolength.hashCode();
        ret = 31 * ret + persistentnotifications.hashCode();
        ret = 31 * ret + closeAnimation.hashCode();
        ret = 31 * ret + compactTextMode.hashCode();
        ret = 31 * ret + youTubeAPI3Key.hashCode();
        ret = 31 * ret + wrapterminal.hashCode();
        ret = 31 * ret + screenStat.hashCode();
        ret = 31 * ret + executables.hashCode();
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof User))
            return false;

        User other = (User) o;

        // name and password serve as a primary key so we only need to compare them
        return other.getPass().equals(getPass()) && other.getName().equals(getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
