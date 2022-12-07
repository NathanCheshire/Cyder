package cyder.ui.drag;

/**
 * The possible types of {@link CyderDragLabel}s.
 */
public enum DragLabelType {
    /**
     * The top of the frame. This is the only drag label that builds the default right button list.
     */
    TOP,

    /**
     * The bottom of the frame.
     */
    BOTTOM,

    /**
     * The left of the frame.
     */
    LEFT,

    /**
     * The right of the frame.
     */
    RIGHT,

    /**
     * The drag label takes up the full content pane or is the content pane.
     */
    FULL
}
