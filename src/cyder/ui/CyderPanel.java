package cyder.ui;

import cyder.layouts.CyderLayout;

import javax.swing.*;
import java.awt.*;

//CyderPanels are just JLabels that can have an associated
// CyderLayout so that we can place layouts inside of layouts basically

/**
 * CyderPanels are what hold and manage where components go on them. They basically are a wrapper for
 * anything that implements CyderLayout and extends the CyderBaseLayout. CyderPanels are really just
 * wrappers for Layouts which means we can use them as either a master component for a CyderFrame
 * content pane or as a sub panel inside of a parent panel.
 */
public class CyderPanel extends JLabel {
    public CyderPanel(CyderLayout cyderLayout) {
        this.cyderLayout = cyderLayout;
    }

    private CyderLayout cyderLayout;

    public CyderLayout getCyderLayout() {
        return cyderLayout;
    }

    public void setCyderLayout(CyderLayout cyderLayout) {
        this.cyderLayout = cyderLayout;
    }

    @Override
    public void setLayout(LayoutManager lay) {
        super.setLayout(null);
        //layouts are always null, we use math to determine our layout on redrawing events
    }

    @Override
    public void paint(Graphics g) {
        if (cyderLayout != null) {
            cyderLayout.setSize(this.getWidth(), this.getHeight());
            cyderLayout.paint(g);
        }
    }
}
