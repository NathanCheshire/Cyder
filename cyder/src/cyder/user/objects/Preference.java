package cyder.user.objects;

import com.google.errorprone.annotations.Immutable;
import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

/**
 * Preference class used to hold user data in the form of strings.
 * Instances of this class are immutable and thus thread safe.
 */
@Immutable
public class Preference {
    /**
     * The id of the preference.
     */
    private final String id;

    /**
     * The name to display for the preference when allowing the user to make changes.
     */
    private final String displayName;

    /**
     * The tooltip for the toggle button.
     */
    private final String tooltip;

    /**
     * The default value for the preference.
     */
    private final String defaultValue;

    /**
     * The method to run when a change of the preference occurs.
     */
    private final Runnable onChangeFunction;

    /**
     * Constructs a preference object.
     *
     * @param id               the id of the preference
     * @param displayName      the display name
     * @param tooltip          the tooltip text for the toggle button
     * @param defaultValue     the default value
     * @param onChangeFunction the method to run when a change of the preference occurs.
     */
    public Preference(String id, String displayName,
                      String tooltip, String defaultValue,
                      Runnable onChangeFunction) {
        this.id = id;
        this.displayName = displayName;
        this.tooltip = tooltip;
        this.defaultValue = defaultValue;
        this.onChangeFunction = onChangeFunction;

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the id of the preference.
     *
     * @return the id of the preference
     */
    public String getID() {
        return id;
    }

    /**
     * Returns the display name of the preference.
     *
     * @return the display name of the preference
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the tooltip text used for the preference toggler.
     *
     * @return the tooltip text used for the preference toggler
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Returns the default value.
     *
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the function to invoke upon a change of the preference.
     *
     * @return the function to invoke upon a change of the preference
     */
    public Runnable getOnChangeFunction() {
        return onChangeFunction;
    }

    /**
     * {@inheritDoc}
     */
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