package cyder.layouts;

import cyder.ui.CyderPanel;

import java.awt.*;

/**
 * This interface shouldn't be directly used. For custom CyderLayouts simply extend the CyderBaseLayout
 * which also forces the class to implement this interface
 */
public interface CyderLayout {
    //NOTE: ALL LAYOUTS SHOULD OVERRIDE THE PAINT METHOD AND CALCULATE HOW TO
    // POSITION THE COMPONENTS THERE

    //of course there will be more complex add and remove component methods but
    // these are the base ones that should always exist
    boolean addComponent(Component component);
    boolean removeComponent(Component component);

    //method to recalculate bounds of components on the panel
    void revalidateComponents();

    //so that the layout can manage components directly on the panel and not itself
    void setAssociatedPanel(CyderPanel panel);
    //repaint should always be overridden to revalidate the bounds of components
}
