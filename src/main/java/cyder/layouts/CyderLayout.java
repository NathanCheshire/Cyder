package main.java.cyder.layouts;

import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.ui.CyderPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * A base layout class to suppress confusing, leftover
 * methods resulting from extending {@link JLabel}.
 */
public abstract class CyderLayout extends JLabel {
    /**
     * The unsupported string.
     */
    private static final String UNSUPPORTED = "Unsupported";

    /**
     * Adds the specified component to the layout.
     * The layout will figure out how add the component to the panel successfully.
     * You should typically implement additional addComponent() methods.
     *
     * @param component the component to add to the layout
     */
    public abstract void addComponent(Component component);

    /**
     * Removes the specified component from the linked CyderPanel.
     * The layout will figure out how to remove and revalidate the panel successfully.
     * You should typically implement additional removeComponent() methods.
     *
     * @param component the component to remove from the panel
     */
    public abstract void removeComponent(Component component);

    /**
     * Recalculates the bounds of all components currently managed by the layout.
     */
    public abstract void revalidateComponents();

    /**
     * Sets the CyderPanel for the LayoutManager to add to and manage the components of.
     *
     * @param panel the panel for the LayoutManager to manage the components of
     */
    public abstract void setAssociatedPanel(CyderPanel panel);

    /**
     * Returns all components managed by this layout.
     *
     * @return all components managed by this layout
     */
    public abstract Collection<Component> getLayoutComponents();

    /**
     * Calculates and returns the minimum necessary size to fit all components
     * on its panel.
     *
     * @return the minimum size necessary to allow all components to be visible
     */
    public abstract Dimension getPackSize();

    /*
    Override add methods so that a developer doesn't
    accidentally call them and wonder why their components aren't
    appearing on the CyderFrame.
     */

    /**
     * Illegal method for a CyderLayout.
     *
     * @throws IllegalMethodException if invoked
     */
    @Override
    @Deprecated
    public Component add(Component component) {
        throw new IllegalMethodException(UNSUPPORTED);
    }

    /**
     * Illegal method for a CyderLayout.
     *
     * @throws IllegalMethodException if invoked
     */
    @Override
    @Deprecated
    public void add(PopupMenu popup) {
        throw new IllegalMethodException(UNSUPPORTED);
    }

    /**
     * Illegal method for a CyderLayout.
     *
     * @throws IllegalMethodException if invoked
     */
    @Override
    @Deprecated
    public Component add(Component comp, int index) {
        throw new IllegalMethodException(UNSUPPORTED);
    }

    /**
     * Illegal method for a CyderLayout.
     *
     * @throws IllegalMethodException if invoked
     */
    @Override
    @Deprecated
    public Component add(String name, Component comp) {
        throw new IllegalMethodException(UNSUPPORTED);
    }

    /**
     * Illegal method for a CyderLayout.
     *
     * @throws IllegalMethodException if invoked
     */
    @Override
    @Deprecated
    public void add(Component comp, Object constraints) {
        throw new IllegalMethodException(UNSUPPORTED);
    }

    /**
     * Illegal method for a CyderLayout.
     *
     * @throws IllegalMethodException if invoked
     */
    @Override
    @Deprecated
    public void add(Component comp, Object constraints, int index) {
        throw new IllegalMethodException(UNSUPPORTED);
    }

    /*
    Override remove methods so that a developer doesn't
    accidentally call them and wonder why their components aren't
    disappearing on the CyderFrame.
     */

    /**
     * Illegal method for a CyderLayout.
     *
     * @throws IllegalMethodException if invoked
     */
    @Override
    @Deprecated
    public void removeAll() {
        throw new IllegalMethodException(UNSUPPORTED);
    }

    /**
     * Illegal method for a CyderLayout.
     *
     * @throws IllegalMethodException if invoked
     */
    @Override
    @Deprecated
    public void remove(int index) {
        throw new IllegalMethodException(UNSUPPORTED);
    }

    /**
     * Illegal method for a CyderLayout.
     *
     * @throws IllegalMethodException if invoked
     */
    @Override
    @Deprecated
    public void remove(Component comp) {
        throw new IllegalMethodException(UNSUPPORTED);
    }

    /**
     * Illegal method for a CyderLayout.
     *
     * @throws IllegalMethodException if invoked
     */
    @Override
    @Deprecated
    public void remove(MenuComponent popup) {
        throw new IllegalMethodException(UNSUPPORTED);
    }
}
