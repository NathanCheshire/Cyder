package cyder.ui.frame;

/**
 * The possible frame types for a {@link CyderFrame}.
 */
public enum FrameType {
    /**
     * The default frame type, all drag label buttons are present.
     */
    DEFAULT,

    /**
     * An input getter frame, the pin button is removed as the frame has been set to always on top.
     */
    INPUT_GETTER,

    /**
     * A popup frame, only the close button is present. The frame is always on top.
     */
    POPUP,
}
