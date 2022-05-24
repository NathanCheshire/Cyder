package cyder.common;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.Logger;

/**
 * An enum used to map a preview value to the actual value to switch on.
 */
public record SwitcherState(String displayValue, String mappedValue) {
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
        Preconditions.checkNotNull(displayValue);
        Preconditions.checkNotNull(mappedValue);

        this.displayValue = displayValue;
        this.mappedValue = mappedValue;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }
}