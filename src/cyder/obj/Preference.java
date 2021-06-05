package cyder.obj;

public class Preference {
    private String ID;
    private String displayName;
    private String tooltip;
    private String defaultValue;

    public Preference(String id, String displayName, String tooltip, String defaultValue) {
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

    public String getDefaultValue() {
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

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
