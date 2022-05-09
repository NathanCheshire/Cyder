package cyder.ui.objects;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.Logger;

/**
 * An enum used to map a preview value to the actual value to switch on.
 */
public class SwitcherState {
    /**
     * The display value of this state.
     */
    String displayValue;

    /**
     * The underlying value of this state.
     */
    String mappedValue;

    /**
     * Constructs a new switch state
     *
     * @param value the display value and underlying map value of the state
     */
    public SwitcherState(String value) {
        this(value, value);
    }

    /**
     * Constructs a new switch state
     *
     * @param displayValue the display value of the state
     * @param mappedValue  the underlying value of the state
     */
    public SwitcherState(String displayValue, String mappedValue) {
        Preconditions.checkArgument(displayValue != null, "Display value is null");
        Preconditions.checkArgument(mappedValue != null, "Mapped value is null");

        this.displayValue = displayValue;
        this.mappedValue = mappedValue;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the name of this state.
     *
     * @return the name of this state
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Sets the name of this state.
     *
     * @param displayValue the name of this state
     */
    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the underlying value of this state.
     *
     * @return the underlying value of this state
     */
    public String getMappedValue() {
        return mappedValue;
    }

    /**
     * Sets the underlying value of this state.
     *
     * @param mappedValue the underlying value of this state
     */
    public void setMappedValue(String mappedValue) {
        this.mappedValue = mappedValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SwitcherState))
            return false;

        SwitcherState other = (SwitcherState) o;

        return other.getDisplayValue().equals(getDisplayValue())
                && other.getMappedValue().equals(getMappedValue());
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
        return "SwitchState: " + displayValue + " => " + mappedValue;
    }
}