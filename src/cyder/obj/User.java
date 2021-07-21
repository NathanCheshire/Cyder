package cyder.obj;

import java.util.LinkedList;
import java.util.List;

public class User {
    private String name;
    private String pass;
    private String font;
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
    private String menudirection;
    private String laststart;
    private String minimizeonclose;
    private String typinganimation;
    private String showbusyicon;
    private LinkedList<MappedExecutable> executables;

    private User() {}

    //getters

    public String getName() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    public String getFont() {
        return font;
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

    public String getMenudirection() {
        return menudirection;
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

    public LinkedList<MappedExecutable> getExecutables() {
        return executables;
    }

    //setters


    public void setName(String name) {
        this.name = name;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setFont(String font) {
        this.font = font;
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

    public void setMenudirection(String menudirection) {
        this.menudirection = menudirection;
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

    public void setExecutables(LinkedList<MappedExecutable> executables) {
        this.executables = executables;
    }

    public static User createDefaultUser() {
        User jsonUser = new User();

        jsonUser.setName("name");
        jsonUser.setPass("password");
        jsonUser.setFont("tahoma");
        jsonUser.setForeground("000000");
        jsonUser.setBackground("ffffff");
        jsonUser.setIntromusic("0");
        jsonUser.setInputborder("0");
        jsonUser.setOutputborder("0");
        jsonUser.setDebugwindows("0");
        jsonUser.setRandombackground("0");
        jsonUser.setHourlychimes("1");
        jsonUser.setSilenceerrors("1");
        jsonUser.setFullscreen("0");
        jsonUser.setOutputfill("0");
        jsonUser.setInputfill("0");
        jsonUser.setClockonconsole("1");
        jsonUser.setShowseconds("1");
        jsonUser.setFilterchat("1");
        jsonUser.setMenudirection("1");
        jsonUser.setLaststart("0");
        jsonUser.setMinimizeonclose("0");
        jsonUser.setTypinganimation("1");
        jsonUser.setShowbusyicon("0");

        LinkedList<MappedExecutable> exes = new LinkedList<>();
        MappedExecutable defaultExe = new MappedExecutable(
                "Minecraft","C:/Program Files (x86)/Minecraft Launcher/MinecraftLauncher.exe");
        MappedExecutable defaultExe2 = new MappedExecutable(
                "Lunar","C:\\Users\\Nathan\\AppData\\Local\\Programs\\lunarclient\\lunar client.exe");
        exes.add(defaultExe);
        exes.add(defaultExe2);
        jsonUser.setExecutables(exes);

        return jsonUser;
    }

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
    }
}
