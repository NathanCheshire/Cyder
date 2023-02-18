package cyder.ui.frame;

/**
 * The possible positions for a {@link CyderFrame}'s title label.
 */
public enum TitlePosition {
    /**
     * Left; if any buttons are on the left, the title position will be forced to the center.
     */
    LEFT,

    /**
     * Center, this will hold as drag label buttons cannot be set to the center.
     * Therefore, if a title label is set to CENTER, it will remain.
     */
    CENTER,

    /**
     * Right; if any buttons are on the right, the title position will be forced to the center.
     */
    RIGHT,
}
