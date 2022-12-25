package cyder.ui.drag;

/**
 * Valid sizes for an icon button.
 */
public enum DragLabelButtonSize {
    /**
     * The default size of small.
     */
    SMALL(22),

    /**
     * A slightly larger icon button size.
     */
    MEDIUM(26),

    /**
     * A larger icon button size to fill the entire height of a default {@link CyderDragLabel}.
     */
    LARGE(30),

    /**
     * The icon should be drawn to take up the most space it can of its parent.
     */
    FULL_DRAG_LABEL(Integer.MAX_VALUE);

    /**
     * The size of this icon button.
     */
    private final int size;

    DragLabelButtonSize(int size) {
        this.size = size;
    }

    /**
     * Returns the size this icon button should be drawn with.
     *
     * @return the size this icon button should be drawn with
     */
    public int getSize() {
        return size;
    }
}
