package cyder.parsers.remote.ip;

/** An object for parsing returned ip data timezone objects. */
public class TimeZone {
    private String name;
    private String abbr;
    private String offset;
    private boolean is_dst;
    private String current_time;

    public String getName() {
        return name;
    }

    public String getAbbr() {
        return abbr;
    }

    public String getOffset() {
        return offset;
    }

    public boolean isIs_dst() {
        return is_dst;
    }

    public String getCurrent_time() {
        return current_time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public void setIs_dst(boolean is_dst) {
        this.is_dst = is_dst;
    }

    public void setCurrent_time(String current_time) {
        this.current_time = current_time;
    }
}
