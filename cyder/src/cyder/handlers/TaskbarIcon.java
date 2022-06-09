package cyder.handlers;

import cyder.handlers.internal.Logger;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;

/**
 * A console taskbar icon.
 */
public class TaskbarIcon {
    /**
     * The actual icon used for the console taskbar.
     */
    private JLabel innerTaskbarIcon;

    public TaskbarIcon(Builder builder) {
        // todo preconditions on builder

        Logger.log(Logger.Tag.OBJECT_CREATION, this);

        generateTaskbarIcon(builder);
    }

    /**
     * Generates the taskbar icon for the encapsulated {@link CyderFrame} based on the provided properties.
     *
     * @param builder the TaskbarIcon builder to construct the TaskbarIcon from
     * @return the taskbar icon for the encapsulated {@link CyderFrame} based on the provided properties
     */
    public JLabel generateTaskbarIcon(Builder builder) {
        // todo this is what will call FrameUtil methods to construct the label
        //  based on the props, a singular method should accept like everything here
        //  and use those methods to handle mapped exes and default ones

        // todo no painting logic should remain in cyder frame/ console frame and should be off shipped to frame util
        //  and invoked here

        innerTaskbarIcon = null;

        return innerTaskbarIcon;
    }

    /**
     * Returns the taskbar icon for the inner CyderFrame.
     *
     * @return the taskbar icon for the inner CyderFrame
     */
    public JLabel getTaskbarIcon() {
        return innerTaskbarIcon;
    }

    /**
     * A builder pattern for a TaskbarIcon.
     */
    public static final class Builder {
        private CyderFrame referenceFrame;
        private boolean compact;
        private boolean focused;
        private Color borderColor;
        private ImageIcon customIcon;
        private Runnable runnable;
        private String name;

        public Builder setReferenceFrame(CyderFrame referenceFrame) {
            this.referenceFrame = referenceFrame;
            return this;
        }

        public Builder setCompact(boolean compact) {
            this.compact = compact;
            return this;
        }

        public Builder setFocused(boolean focused) {
            this.focused = focused;
            return this;
        }

        public Builder setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        public Builder setCustomIcon(ImageIcon customIcon) {
            this.customIcon = customIcon;
            return this;
        }

        public Builder setRunnable(Runnable runnable) {
            this.runnable = runnable;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public TaskbarIcon build() {
            return new TaskbarIcon(this);
        }
    }
}
