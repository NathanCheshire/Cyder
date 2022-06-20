package cyder.ui;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;
import cyder.layouts.CyderLayout;
import cyder.utils.ReflectionUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * CyderPanels are what hold and manage where components go on them.
 * They basically are a wrapper for layouts that extends {@link CyderLayout}.
 */
public class CyderPanel extends JLabel {
    /*
     * This class extends JLabel to allow it to be a content pane for a CyderFrame.
     */

    /**
     * Restict class instnatiation without a valid cyder layout.
     */
    private CyderPanel() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Constrcuts a new cyder panel with the provided layout.
     *
     * @param cyderLayout the layout that manages components
     */
    public CyderPanel(CyderLayout cyderLayout) {
        this.cyderLayout = cyderLayout;
        cyderLayout.setAssociatedPanel(this);
        revalidateComponents();

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * The layout which we are wrapping with this panel.
     */
    private final CyderLayout cyderLayout;

    /**
     * Sets the layout manager to null due to this being a CyderPanel.
     * Use a custom {@link CyderLayout} class, add that to a CyderPanel,
     * and then set the panel to a {@link CyderFrame}'s content pane to use layouts with Cyder.
     */
    @Override
    public void setLayout(LayoutManager lay) {
        super.setLayout(null);
    }

    /**
     * Whether the content pane should be repainted.
     */
    private boolean disableContentRepainting;

    /**
     * Returns whether content pane painting is disabled.
     *
     * @return whether content pane painting is disabled
     */
    public boolean contentRepaintingDisabled() {
        return disableContentRepainting;
    }

    /**
     * Sets the state of disable content repainting.
     *
     * @param disableContentRepainting whether the content pane should be repainted.
     */
    public void setDisableContentRepainting(boolean disableContentRepainting) {
        this.disableContentRepainting = disableContentRepainting;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void repaint() {
        //as long as we should repaint, repaint it
        if (!disableContentRepainting) {
            super.repaint();
            revalidateComponents();
        }
    }

    /**
     * Revalidates the components managed by the linked layout.
     */
    public void revalidateComponents() {
        if (cyderLayout != null) {
            cyderLayout.revalidateComponents();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    /**
     * Returns the components managed by the layout.
     *
     * @return the components managed by the layout
     */
    public ArrayList<Component> getLayoutComponents() {
        return cyderLayout.getLayoutComponents();
    }
}
