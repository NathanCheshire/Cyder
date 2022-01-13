package cyder.layouts;

import cyder.ui.CyderPanel;

import java.awt.*;

/**
 * This interface shouldn't be directly used. For custom CyderLayouts simply extend the CyderBaseLayout
 * which also forces the class to implement this interface
 */
public interface CyderLayout {
    /**
     * Adds the specified component to the linked CyderPanel.
     * The layout will figure out how add the component to the panel successfully.
     * You should typically add more addComponent() methods but this is the simple base one
     * that should always be implemented.
     *
     * @param component the component to add to the panel
     * @return whether or not the component was successfully added to the panel
     */
    boolean addComponent(Component component);

    /**
     * Removes the specified component from the linked CyderPanel.
     * The layout will figure out how to remove and revalidate the panel successfully.
     * You should typically add more removeComponent() methods but this is the simple base one
     * that should always be implmeneted.
     *
     * @param component the component to remove from the panel
     * @return whether or not the component was successfully removed from the panel
     */
    boolean removeComponent(Component component);

    /**
     * Recalculates the bounds of all components currently added the CyderPanel.
     */
    void revalidateComponents();

    //so that the layout can manage components directly on the panel and not itself

    /**
     * Sets the CyderPanel for the LayoutManager to manage the components of.
     *
     * @param panel the panel for the LayoutManager to manaqge the components of
     */
    void setAssociatedPanel(CyderPanel panel);
}
