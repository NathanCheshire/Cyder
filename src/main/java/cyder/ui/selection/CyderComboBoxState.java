package cyder.ui.selection;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * A combo box state with the option to show a value different than the one set to when toggled.
 */
@Immutable
public final class CyderComboBoxState {
    /**
     * The value to display for this state.
     */
    private final String displayValue;

    /**
     * The underlying value for this state.
     */
    private final String mappedValue;

    /**
     * Constructs a new switch state
     *
     * @param value the display value and underlying map value of the state
     */
    public CyderComboBoxState(String value) {
        this(value, value);
    }

    /**
     * Constructs a new switch state
     *
     * @param displayValue the display value of the state
     * @param mappedValue  the underlying value of the state
     */
    public CyderComboBoxState(String displayValue, String mappedValue) {
        this.displayValue = Preconditions.checkNotNull(displayValue);
        this.mappedValue = Preconditions.checkNotNull(mappedValue);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the display value.
     *
     * @return the display value
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Returns the mapped value.
     *
     * @return the mapped value
     */
    public String getMappedValue() {
        return mappedValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof CyderComboBoxState)) {
            return false;
        }

        CyderComboBoxState other = (CyderComboBoxState) o;
        return other.displayValue.equals(displayValue)
                && other.mappedValue.equals(mappedValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = displayValue.hashCode();
        ret = 31 * ret + mappedValue.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CyderComboBoxState{"
                + "displayValue=\"" + displayValue + "\""
                + ", mappedValue=\"" + mappedValue + "\""
                + "}";
    }
}
