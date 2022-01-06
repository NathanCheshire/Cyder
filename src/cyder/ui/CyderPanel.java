package cyder.ui;

import cyder.layouts.CyderBaseLayout;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import java.awt.*;

/**
 * CyderPanels are what hold and manage where components go on them. They basically are a wrapper for
 * anything that implements CyderLayout and extends the CyderBaseLayout. CyderPanels are really just
 * wrappers for Layouts which means we can use them as either a master component for a CyderFrame
 * content pane or as a sub panel inside of a parent panel.
 */
public class CyderPanel extends JLabel {
    //CyderPanel cannot exist without a CyderLayout
    private CyderPanel() {}

    public CyderPanel(CyderBaseLayout cyderLayout) {
        this.cyderLayout = cyderLayout;
        cyderLayout.setAssociatedPanel(this);
        //now the layout can access 'this' directly when it needs to recalculate bounds
    }

    private CyderBaseLayout cyderLayout;

    @Override
    public void setLayout(LayoutManager lay) {
        super.setLayout(null);
        //layouts are always null, we use math to determine our layout on redrawing events
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);

        if (cyderLayout != null)
            cyderLayout.revalidateComponents();
    }

    //disabling repainting of the content pane for optimization purposes
    private boolean disableContentRepainting = false;

    public boolean isDisableContentRepainting() {
        return disableContentRepainting;
    }

    public void setDisableContentRepainting(boolean disableContentRepainting) {
        this.disableContentRepainting = disableContentRepainting;
    }

    @Override
    public void repaint() {
        //as long as we should repaint, repaint it
        if (!disableContentRepainting) {
            super.repaint();

            if (cyderLayout != null)
                cyderLayout.revalidateComponents();
        }
    }

    //standard
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}
