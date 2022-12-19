package main.java.cyder.ui.frame;

/**
 * The possible title positions for a {@link CyderFrame} title label.
 */
public enum TitlePosition {
    /**
     * Left; if any buttons are on the left the title position will be forced to the center.
     */
    LEFT,

    /**
     * Center, this will hold as drag label buttons cannot be set to the center.
     */
    CENTER,

    /**
     * Right, if buttons are on the right the title position will be forced to the center.
     */
    RIGHT,
}
