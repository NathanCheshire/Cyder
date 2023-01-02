package cyder.ui;

import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

import javax.swing.*;

/**
 * Constants for Ui related applications
 */
public final class UiConstants {
    /**
     * Constants related to Ui.
     */
    private UiConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The value to indicate a frame is iconified.
     */
    public static final int FRAME_ICONIFIED = JFrame.ICONIFIED;

    /**
     * The value to indicate a frame is in a normal state.
     */
    public static final int FRAME_NORMAL = JFrame.NORMAL;
}
