package cyder.layouts;

import java.awt.*;

/**
 * The default layout for CyderFrames where basic pure absolute layouts are used and components
 *  are added directly to the iconLabel of the CyderFrame. This means this layout is not scalable
 *  and should not be used for resizable frames.
 */
public class CyderNullLayout extends CyderBaseLayout implements CyderLayout {
    @Override
    public boolean addComponent(Component component) {
        super.add(component);
        return true;
    }
    //these won't be used for null layouts since we add directly using add()
    @Override
    public boolean removeComponent(Component component) {
        super.remove(component);
        return true;
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height); //nothing changes
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g); //nothing changes
    }
}
