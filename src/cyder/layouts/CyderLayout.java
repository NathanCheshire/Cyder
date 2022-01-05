package cyder.layouts;

import java.awt.*;

/**
 * This method shouldn't be directly used. For custom CyderLayouts simply extend the CyderBaseLayout
 */
public interface CyderLayout {
    //methods essential for all CyderLayouts to have

    //NOTE: ALL LAYOUTS SHOULD OVERRIDE THE PAINT METHOD AND CALCULATE HOW TO
    // POSITION THE COMPONENTS THERE

    //of course there will be more complex add and remove component methods but
    // this is the base one that should always exist
    boolean addComponent(Component component);
    boolean removeComponent(Component component);

    //to establish the bounds of the layout and the actual drawing/positioning
    // of components
    void setSize(int width, int height);
    void paint(Graphics g);
}
