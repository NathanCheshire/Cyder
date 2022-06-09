package cyder.handlers;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;

/**
 * A console taskbar icon.
 */
public class TaskbarIcon {
    private final CyderFrame referenceFrame;
    private boolean isFocused;
    private boolean isCompact;
    private Color borderColor;
    private ImageIcon customIcon;
    private Runnable runnable;

    /**
     * The actual icon used for the console taskbar.
     */
    private JLabel innerTaskbarIcon;

    public TaskbarIcon(CyderFrame referenceFrame, boolean isCompact, boolean isFocused,
                       Color borderColor, ImageIcon customIcon, Runnable runnable) {
        Preconditions.checkNotNull(referenceFrame);
        Preconditions.checkNotNull(borderColor);

        this.referenceFrame = referenceFrame;
        this.isCompact = isCompact;
        this.isFocused = isFocused;
        this.borderColor = borderColor;
        this.customIcon = customIcon;
        this.runnable = runnable;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    public void revalidateIcon() {
        // todo this is what will call FrameUtil methods to construct the label
        //  based on the props, a singular method should accept like everything here
        //  and use those methods to handle mapped exes and default ones
    }

    /**
     * Returns the taskbar icon for the inner CyderFrame.
     *
     * @return the taskbar icon for the inner CyderFrame
     */
    public JLabel getTaskbarIcon() {
        return innerTaskbarIcon;
    }

    // ---------------
    // builder pattern
    // ---------------

    public final class TaskbarIconBuilder {
        private CyderFrame referenceFrame;
        private boolean compact;
        private boolean focused;
        private Color borderColor;
        private ImageIcon customIcon;
        private Runnable runnable;

        public TaskbarIconBuilder(CyderFrame referenceFrame) {
            this.referenceFrame = referenceFrame;
        }

        public TaskbarIconBuilder setCompact(boolean compact) {
            this.compact = compact;
            return this;
        }

        public TaskbarIconBuilder setFocused(boolean focused) {
            this.focused = focused;
            return this;
        }

        public TaskbarIconBuilder setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        public TaskbarIconBuilder setCustomIcon(ImageIcon customIcon) {
            this.customIcon = customIcon;
            return this;
        }

        public TaskbarIconBuilder setRunnable(Runnable runnable) {
            this.runnable = runnable;
            return this;
        }

        public TaskbarIcon build() {
            return new TaskbarIcon(referenceFrame, compact, focused, borderColor, customIcon, runnable);
        }
    }
}
