package cyder.layouts;

import cyder.ui.CyderPanel;

import java.awt.*;
import java.util.ArrayList;

/**
 * An interface for Cyder layouts to implement.
 */
public interface ICyderLayout {
    /**
     * Adds the specified component to the layout.
     * The layout will figure out how add the component to the panel successfully.
     * You should typically implement additional addComponent() methods.
     *
     * @param component the component to add to the layout
     */
    default void addComponent(Component component) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * Removes the specified component from the linked CyderPanel.
     * The layout will figure out how to remove and revalidate the panel successfully.
     * You should typically implement additional removeComponent() methods.
     *
     * @param component the component to remove from the panel
     */
    default void removeComponent(Component component) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * Recalculates the bounds of all components currently managed by the layout.
     */
    default void revalidateComponents() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * Sets the CyderPanel for the LayoutManager to add to and manage the components of.
     *
     * @param panel the panel for the LayoutManager to manaqge the components of
     */
    default void setAssociatedPanel(CyderPanel panel) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * Returns all components managed by this layout.
     *
     * @return all components managed by this layout
     */
    default ArrayList<Component> getLayoutComponents() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * Calculates and returns the minimum necessary size to fit all components
     * on its panel.
     *
     * @return the minimum size necessary to allow all components to be visible
     */
    default Dimension calculateMinSize() {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
