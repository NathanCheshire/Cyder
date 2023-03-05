package cyder.ui.resizing;

import com.google.common.base.Preconditions;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.awt.*;

/**
 * An entity to link the original ability of a {@link Component} to gain focus to itself.
 * Used for restorations after resize events.
 */
class FocusWrappedComponent {
    /**
     * The component.
     */
    private final Component component;
    /**
     * Whether the component in its current state can gain focus.
     */
    private final boolean wasFocusable;

    /**
     * Constructs a new FocusWrappedComponent.
     *
     * @param component    the component
     * @param wasFocusable whether the component in its current state can gain focus
     */
    public FocusWrappedComponent(Component component, boolean wasFocusable) {
        this.component = Preconditions.checkNotNull(component);
        this.wasFocusable = wasFocusable;

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Resets the original focusable property of the encapsulated component.
     */
    public void restoreOriginalFocusableState() {
        if (wasFocusable) component.setFocusable(true);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Returns whether the component in its current state can gain focus.
     *
     * @return whether the component in its current state can gain focus
     */
    public boolean isWasFocusable() {
        return wasFocusable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "FocusWrappedComponent{"
                + "component=" + component
                + ", wasFocusable=" + wasFocusable
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FocusWrappedComponent)) {
            return false;
        }

        FocusWrappedComponent other = (FocusWrappedComponent) o;
        return component.equals(other.component)
                && wasFocusable == other.wasFocusable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = component.hashCode();
        ret = 31 * ret + Boolean.hashCode(wasFocusable);
        return ret;
    }
}
