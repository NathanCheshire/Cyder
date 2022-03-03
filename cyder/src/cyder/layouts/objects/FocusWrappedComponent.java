package cyder.layouts.objects;

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
        this.canFocus = comp.isFocusable();
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
}
