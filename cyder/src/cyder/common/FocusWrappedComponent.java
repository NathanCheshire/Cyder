package cyder.common;

import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

import java.awt.*;

/**
 * A class to link the ability to focus with a component.
 */
public class FocusWrappedComponent {
    /**
     * The component.
     */
    private final Component comp;

    /**
     * Whether the component is allowed to gain focus.
     */
    private final boolean canFocus;

    /**
     * Constructs a new FocusWrappedComponent.
     *
     * @param comp the component
     */
    public FocusWrappedComponent(Component comp) {
        this.comp = comp;
        canFocus = comp.isFocusable();
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComp() {
        return comp;
    }

    /**
     * Returns whether the component could originally hold focus.
     *
     * @return whether the component could originally hold focus
     */
    public boolean canFocus() {
        return canFocus;
    }

    /**
     * Resets the original focusable property of the encapsulated component.
     */
    public void restore() {
        if (canFocus) {
            comp.setFocusable(canFocus);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (this == o)
            return true;
        else if (!(o instanceof FocusWrappedComponent))
            return false;

        FocusWrappedComponent other = (FocusWrappedComponent) o;

        return other.canFocus == canFocus() && other.getComp().equals(getComp());
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int ret = comp.hashCode();
        ret = 31 * ret + Boolean.hashCode(canFocus);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
