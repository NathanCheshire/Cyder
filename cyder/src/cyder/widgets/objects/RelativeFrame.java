package cyder.widgets.objects;

import cyder.ui.CyderFrame;

/**
 * A wrapper used to store it's position relative to some other component.
 */
public class RelativeFrame {
    /**
     * The frame we are keeping track of.
     */
    private CyderFrame frame;

    /**
     * The frame's x offset to the other component.
     */
    private int xOffset;

    /**
     * The frame's y offset to the other component.
     */
    private int yOffset;

    /**
     * Constructs a new RelativeFrame object.
     *
     * @param frame the frame that is relative to some other component
     * @param xOffset the x offset to the other component
     * @param yOffset the y offset to the other component
     */
    public RelativeFrame(CyderFrame frame, int xOffset, int yOffset) {
        this.frame = frame;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    /**
     * Returns the reference frame.
     *
     * @return the reference frame
     */
    public CyderFrame getFrame() {
        return frame;
    }

    /**
     * Sets the reference frame.
     *
     * @param frame the reference frame
     */
    public void setFrame(CyderFrame frame) {
        this.frame = frame;
    }

    /**
     * Returns the x offset from the reference frame to the other component.
     *
     * @return the x offset from the reference frame to the other component
     */
    public int getxOffset() {
        return xOffset;
    }

    /**
     * Sets the x offset to the other component.
     *
      * @param xOffset the x offset to the other component
     */
    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    /**
     * Returns the y offset from the reference frame to the other component.
     *
     * @return the y offset from the reference frame to the other component
     */
    public int getyOffset() {
        return yOffset;
    }

    /**
     * Sets the y offset to the other component.
     *
     * @param yOffset the y offset to the other component
     */
    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }
}
