package cyder.ui.objects;

import cyder.utilities.ReflectionUtil;

/**
 * An enum used to map a preview value to the actual value to switch on.
 */
public class SwitchState {
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
     * @param displayValue the display value of the state
     * @param mappedValue the underlying value of the state
     */
    public SwitchState(String displayValue, String mappedValue) {
        this.displayValue = displayValue;
        this.mappedValue = mappedValue;
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
        if (!(o instanceof SwitchState))
            return false;

        SwitchState other = (SwitchState) o;

        return other.getDisplayValue().equals(this.getDisplayValue())
                && other.getMappedValue().equals(this.getMappedValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = this.displayValue.hashCode();
        ret = 31 * ret + this.mappedValue.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}