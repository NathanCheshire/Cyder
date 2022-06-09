package cyder.handlers;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderFrame;

import javax.swing.*;

/**
 * A console taskbar icon.
 */
public class TaskbarIcon {
    /**
     * The frame this taskbar icon is controlling
     */
    private final CyderFrame referenceFrame;

    /**
     * Whether the current state of this icon is a focused/hovered state.
     */
    private boolean isFocused;

    /**
     * Whether the current state of this icon is a compact state.
     */
    private boolean isCompact;

    /**
     * The actual icon used for the console taskbar.
     */
    private JLabel innerTaskbarIcon;

    public TaskbarIcon(CyderFrame referenceFrame, boolean isCompact) {
        Preconditions.checkNotNull(referenceFrame);

        this.referenceFrame = referenceFrame;
        this.isCompact = isCompact;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the reference frame for this taskbar icon.
     *
     * @return the reference frame for this taskbar icon
     */
    public CyderFrame getReferenceFrame() {
        return referenceFrame;
    }

    /**
     * Returns whether the state of this taskbar icon is focused/hovered.
     *
     * @return whether the state of this taskbar icon is focused/hovered
     */
    public boolean isFocused() {
        return isFocused;
    }

    /**
     * Returns whether the state of this taskbar icon is compact.
     *
     * @return whether the state of this taskbar icon is compact
     */
    public boolean isCompact() {
        return isCompact;
    }

    public void setFocused(boolean focused) {
        if (focused == isFocused)
            return;

        isFocused = focused;

        revalidateIcon();
    }

    public void setCompact(boolean compact) {
        if (compact == isCompact)
            return;

        isCompact = compact;

        revalidateIcon();
    }

    public void revalidateIcon() {
        // todo
    }

    /**
     * Returns the taskbar icon for the inner CyderFrame.
     *
     * @return the taskbar icon for the inner CyderFrame
     */
    public JLabel getTaskbarIcon() {
        return innerTaskbarIcon;
    }
}
