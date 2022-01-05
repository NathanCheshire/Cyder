package cyder.layouts;

import java.awt.*;

public interface CyderLayout {
    //methods essential for all CyderLayouts to have

    //NOTE: ALL LAYOUTS SHOULD OVERRIDE THE PAINT METHOD AND CALCULATE HOW TO
    // POSITION THE COMPONENTS THERE
    boolean addComponent(Component component);
    boolean removeComponent(Component component);
}
