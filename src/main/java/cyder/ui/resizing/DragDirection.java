package cyder.ui.resizing;

import java.awt.*;
import java.util.Arrays;

/**
 * Possibles directions of drag for resize events.
 */
enum DragDirection {
    NO_DRAG(0, Cursor.DEFAULT_CURSOR),
    NORTH(1, Cursor.N_RESIZE_CURSOR),
    WEST(2, Cursor.W_RESIZE_CURSOR),
    NORTH_WEST(3, Cursor.NW_RESIZE_CURSOR),
    SOUTH(4, Cursor.S_RESIZE_CURSOR),
    SOUTH_WEST(6, Cursor.SW_RESIZE_CURSOR),
    EAST(8, Cursor.E_RESIZE_CURSOR),
    NORTH_EAST(9, Cursor.NE_RESIZE_CURSOR),
    SOUTH_EAST(12, Cursor.SE_RESIZE_CURSOR);

    /**
     * The drag ordinal for this drag direction.
     */
    private final int dragOrdinal;

    /**
     * The cursor ordinal for this drag direction.
     */
    private final int cursorOrdinal;

    DragDirection(int dragOrdinal, int cursorOrdinal) {
        this.dragOrdinal = dragOrdinal;
        this.cursorOrdinal = cursorOrdinal;
    }

    /**
     * Returns the drag ordinal for this drag direction.
     *
     * @return the drag ordinal for this drag direction
     */
    public int getDragOrdinal() {
        return dragOrdinal;
    }

    /**
     * Returns the cursor ordinal for this drag direction.
     *
     * @return the cursor ordinal for this drag direction
     */
    public int getCursorOrdinal() {
        return cursorOrdinal;
    }

    /**
     * Returns the {@link DragDirection} with the provided drag ordinal.
     *
     * @param dragOrdinal the ordinal
     * @return the drag direction with the provided ordinal if found
     * @throws IllegalArgumentException if a drag direction with the provided ordinal cannot be found
     */
    public static DragDirection getFromDragOrdinal(int dragOrdinal) {
        return Arrays.stream(values())
                .filter(dragDirection -> dragDirection.getDragOrdinal() == dragOrdinal)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Could not find DragDirection with ordinal: " + dragOrdinal));
    }

    /**
     * Returns the cursor for this drag direction.
     *
     * @return the cursor for this drag direction
     */
    @SuppressWarnings("MagicConstant") /* Safe cursor types */
    public Cursor getPredefinedCursor() {
        return Cursor.getPredefinedCursor(DragDirection.getFromDragOrdinal(dragOrdinal).getCursorOrdinal());
    }
}
