package cyder.ui.frame;

/** The screen positions for a {@link CyderFrame} to be placed at. */
public enum ScreenPosition {
    /** The true top left of the monitor. */
    TRUE_TOP_LEFT,

    /** The top left of the monitor, accounting for the possible taskbar. */
    TOP_LEFT,

    /** The true top right of the monitor. */
    TRUE_TOP_RIGHT,

    /** The top right of the monitor, accounting for the possible taskbar. */
    TOP_RIGHT,

    /** The true bottom left of the monitor. */
    TRUE_BOTTOM_LEFT,

    /** The bottom left of the monitor, accounting for the possible taskbar. */
    BOTTOM_LEFT,

    /** The true bottom right of the monitor. */
    TRUE_BOTTOM_RIGHT,

    /** The bottom right of the monitor, accounting for the possible taskbar. */
    BOTTOM_RIGHT,

    /** The true center of the monitor. */
    TRUE_CENTER,

    /** The center of the monitor, accounting for the taskbar position. */
    CENTER,
}
