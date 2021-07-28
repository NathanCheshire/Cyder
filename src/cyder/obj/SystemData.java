package cyder.obj;

import java.util.LinkedList;

public class SystemData {
    private boolean released;
    private String version;
    private String releasedate;
    private String mastermac;
    private String ipkey;
    private String weatherkey;
    private boolean uiloc;
    private String ytt;
    private double uiscale;
    private boolean consoleresizable;
    private boolean autocypher;
    private Hash cypherhash;
    private LinkedList<ExitCondition> exitconditions;

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getReleasedate() {
        return releasedate;
    }

    public void setReleasedate(String releasedate) {
        this.releasedate = releasedate;
    }

    public String getMastermac() {
        return mastermac;
    }

    public void setMastermac(String mastermac) {
        this.mastermac = mastermac;
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

    public boolean isUiloc() {
        return uiloc;
    }

    public void setUiloc(boolean uiloc) {
        this.uiloc = uiloc;
    }

    public String getYtt() {
        return ytt;
    }

    public void setYtt(String ytt) {
        this.ytt = ytt;
    }

    public double getUiscale() {
        return uiscale;
    }

    public void setUiscale(double uiscale) {
        this.uiscale = uiscale;
    }

    public boolean isConsoleresizable() {
        return consoleresizable;
    }

    public void setConsoleresizable(boolean consoleresizable) {
        this.consoleresizable = consoleresizable;
    }

    public boolean isAutocypher() {
        return autocypher;
    }

    public void setAutocypher(boolean autocypher) {
        this.autocypher = autocypher;
    }

    public Hash getCypherhash() {
        return cypherhash;
    }

    public void setCypherhash(Hash cypherhash) {
        this.cypherhash = cypherhash;
    }

    public LinkedList<ExitCondition> getExitconditions() {
        return exitconditions;
    }

    public void setExitconditions(LinkedList<ExitCondition> exitconditions) {
        this.exitconditions = exitconditions;
    }

    public static class Hash {
        private String name;
        private String hashpass;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHashpass() {
            return hashpass;
        }

        public void setHashpass(String hashpass) {
            this.hashpass = hashpass;
        }
    }

    public static class ExitCondition {
        private int code;
        private String description;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
