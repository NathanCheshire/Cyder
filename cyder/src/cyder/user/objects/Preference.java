package cyder.user.objects;

import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

import java.util.function.Function;

/**
 * Preference class used to hold user data in the form of strings.
 */
public class Preference {
    private String id;
    private String displayName;
    private String tooltip;
    private String defaultValue;
    private Function<Void, Void> onChangeFunction;

    public Preference(String id, String displayName,
                      String tooltip, String defaultValue,
                      Function<Void, Void> onChangeFunction) {
        this.id = id;
        this.displayName = displayName;
        this.tooltip = tooltip;
        this.defaultValue = defaultValue;
        this.onChangeFunction = onChangeFunction;

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    public String getID() {
        return id;
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

    public void setID(String id) {
        this.id = id;
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

    public Function<Void, Void> getOnChangeFunction() {
        return onChangeFunction;
    }

    public void setOnChangeFunction(Function<Void, Void> onChangeFunction) {
        this.onChangeFunction = onChangeFunction;
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        else if (!(o instanceof Preference))
            return false;

        return ((Preference) o).getID().equals(getID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + tooltip.hashCode();
        result = 31 * result + defaultValue.hashCode();
        return result;
    }
}