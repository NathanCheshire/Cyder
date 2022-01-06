package cyder.layouts;

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

    //to establish the bounds of the layout
    void setSize(int width, int height);
}
