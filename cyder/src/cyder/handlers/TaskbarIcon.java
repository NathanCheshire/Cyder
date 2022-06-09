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
    /**
     * The length of the taskbar icons generated.
     */
    public static final int TASKBAR_ICON_LEN = 75;

    /**
     * The border length of the taskbar icons generated.
     */
    public static final int TASKBAR_BORDER_LEN = 5;

    /**
     * The maximum number of chars to display when compact mode for taskbar icons is active.
     */
    public static final int MAX_COMPACT_MENU_CHARS = 11;

    /**
     * The actual icon used for the console taskbar.
     */
    private JLabel innerTaskbarIcon;

    public TaskbarIcon(Builder builder) {
        Preconditions.checkNotNull(builder.name);
        Preconditions.checkArgument(!builder.name.isEmpty());


        Logger.log(Logger.Tag.OBJECT_CREATION, this);

        generateTaskbarIcon(builder);
    }

    /**
     * Generates the taskbar icon for the encapsulated {@link CyderFrame} based on the provided properties.
     *
     * @param builder the TaskbarIcon builder to construct the TaskbarIcon from
     */
    public void generateTaskbarIcon(Builder builder) {
        if (builder.compact) {
            // todo simpler case
        } else {
            if (builder.customIcon != null) {
                // todo slightly easier
            } else {
                // todo hardest
            }
        }
    }

    /**
     * Returns the previously generated taskbar icon.
     *
     * @return the previously generated taskbar icon
     */
    public JLabel getTaskbarIcon() {
        return innerTaskbarIcon;
    }

    /**
     * A builder for a TaskbarIcon.
     */
    public static final class Builder {
        private CyderFrame referenceFrame;
        private boolean compact;
        private boolean focused;
        private Color borderColor;
        private ImageIcon customIcon;
        private Runnable runnable;
        private String name;

        /**
         * Sets the reference frame for this taskbar icon.
         *
         * @param referenceFrame the reference frame for this taskbar icon
         * @return this Builder
         */
        public Builder setReferenceFrame(CyderFrame referenceFrame) {
            this.referenceFrame = referenceFrame;
            return this;
        }

        /**
         * Sets whether this taskbar icon should be painted in compact mode.
         *
         * @param compact whether this taskbar icon should be painted in compact mode
         * @return this Builder
         */
        public Builder setCompact(boolean compact) {
            this.compact = compact;
            return this;
        }

        /**
         * Sets whether this taskbar icon should be painted as focused.
         *
         * @param focused whether this taskbar icon should be painted as focused
         * @return this Builder
         */
        public Builder setFocused(boolean focused) {
            this.focused = focused;
            return this;
        }

        /**
         * Sets the borderColor for this taskbar icon.
         *
         * @param borderColor the name for this taskbar icon
         * @return this Builder
         */
        public Builder setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        /**
         * Sets the customIcon for this taskbar icon.
         *
         * @param customIcon the name for this taskbar icon
         * @return this Builder
         */
        public Builder setCustomIcon(ImageIcon customIcon) {
            this.customIcon = customIcon;
            return this;
        }

        /**
         * Sets the runnable for this taskbar icon.
         *
         * @param runnable the runnable for this taskbar icon
         * @return this Builder
         */
        public Builder setRunnable(Runnable runnable) {
            this.runnable = runnable;
            return this;
        }

        /**
         * Sets the name for this taskbar icon.
         *
         * @param name the name for this taskbar icon
         * @return this Builder
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Constructs a new TaskbarIcon instance using this builder's members.
         *
         * @return a new TaskbarIcon instance using this builder's members
         */
        public TaskbarIcon build() {
            return new TaskbarIcon(this);
        }
    }
}
