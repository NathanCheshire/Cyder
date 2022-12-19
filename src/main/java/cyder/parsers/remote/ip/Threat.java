package main.java.cyder.parsers.remote.ip;

/**
 * An object for parsing returned ip data threat objects.
 */
public class Threat {
    private boolean is_tor;
    private boolean is_proxy;
    private boolean is_anonymous;
    private boolean is_known_attacker;
    private boolean is_known_abuser;
    private boolean is_threat;
    private boolean is_bogon;

    public void setIs_tor(boolean is_tor) {
        this.is_tor = is_tor;
    }

    public void setIs_proxy(boolean is_proxy) {
        this.is_proxy = is_proxy;
    }

    public void setIs_anonymous(boolean is_anonymous) {
        this.is_anonymous = is_anonymous;
    }

    public void setIs_known_attacker(boolean is_known_attacker) {
        this.is_known_attacker = is_known_attacker;
    }

    public void setIs_known_abuser(boolean is_known_abuser) {
        this.is_known_abuser = is_known_abuser;
    }

    public void setIs_threat(boolean is_threat) {
        this.is_threat = is_threat;
    }

    public void setIs_bogon(boolean is_bogon) {
        this.is_bogon = is_bogon;
    }

    public boolean isIs_tor() {
        return is_tor;
    }

    public boolean isIs_proxy() {
        return is_proxy;
    }

    public boolean isIs_anonymous() {
        return is_anonymous;
    }

    public boolean isIs_known_attacker() {
        return is_known_attacker;
    }

    public boolean isIs_known_abuser() {
        return is_known_abuser;
    }

    public boolean isIs_threat() {
        return is_threat;
    }

    public boolean isIs_bogon() {
        return is_bogon;
    }
}
