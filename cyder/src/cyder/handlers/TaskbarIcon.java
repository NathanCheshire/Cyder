package cyder.handlers;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderLabel;
import cyder.utilities.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

/**
 * A console taskbar icon.
 */
public class TaskbarIcon {
    /**
     * The length of the taskbar icons generated.
     */
    private static final int TASKBAR_ICON_LEN = 75;

    /**
     * The border length of the taskbar icons generated.
     */
    private static final int TASKBAR_BORDER_LEN = 5;

    /**
     * The maximum number of chars to display when compact mode for taskbar icons is active.
     */
    private static final int MAX_COMPACT_MENU_CHARS = 11;

    /**
     * The color for custom painted taskbar icon borders.
     */
    private static final Color TASKBAR_BORDER_COLOR = Color.black;

    /**
     * The font used for taskbar icon painted names.
     */
    private static final Font labelFont = new Font("Agency FB", Font.BOLD, 28);

    /**
     * The factor to darken a buffered image by for hover/focus events.
     */
    private static final float DARK_FACTOR = 0.7f;

    /**
     * The rescale operator used to darken buffered images.
     */
    private static final RescaleOp rescaleOp = new RescaleOp(DARK_FACTOR, 0, null);

    /**
     * The actual icon used for the console taskbar.
     */
    private JLabel innerTaskbarIcon;

    /**
     * Constructs and generates a new taskbar icon.
     *
     * @param builder the builder to construct the taskbar icon from
     */
    public TaskbarIcon(Builder builder) {
        Preconditions.checkNotNull(builder.name);
        Preconditions.checkArgument(!builder.name.isEmpty());

        Logger.log(Logger.Tag.OBJECT_CREATION, this);

        generateTaskbarIcon(builder);
    }

    /**
     * Generates the taskbar icon for a CyderFrame based on the provided properties.
     *
     * @param builder the TaskbarIcon builder to construct the TaskbarIcon from
     */
    public void generateTaskbarIcon(Builder builder) {
        JLabel newTaskbarIcon = new JLabel();

        if (builder.compact) {
            String name = builder.name.substring(0, Math.min(MAX_COMPACT_MENU_CHARS, builder.name.length()));

            JLabel usage = new JLabel(name);
            usage.setForeground(builder.focused ? CyderColors.regularRed : CyderColors.vanilla);
            usage.setFont(CyderFonts.defaultFontSmall);
            usage.setVerticalAlignment(SwingConstants.CENTER);

            usage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    builder.runnable.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    usage.setForeground(builder.focused ? CyderColors.vanilla : CyderColors.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    usage.setForeground(builder.focused ? CyderColors.regularRed : CyderColors.vanilla);
                }
            });

            // if had to cut off text, make tooltip show full
            if (!builder.name.equalsIgnoreCase(name)) {
                usage.setToolTipText(name);
            }
        } else {
            BufferedImage paintedImage;

            if (builder.customIcon != null) {
                paintedImage = ImageUtil.resizeImage(TASKBAR_ICON_LEN,
                        TASKBAR_ICON_LEN, builder.customIcon);
            } else {
                paintedImage = new BufferedImage(TASKBAR_ICON_LEN,
                        TASKBAR_ICON_LEN, BufferedImage.TYPE_INT_RGB);
                Graphics g = paintedImage.getGraphics();

                // paint center
                g.setColor(TASKBAR_BORDER_COLOR);
                g.fillRect(0, 0, TASKBAR_ICON_LEN, TASKBAR_BORDER_LEN);
                g.fillRect(0, 0, TASKBAR_BORDER_LEN, TASKBAR_ICON_LEN);
                g.fillRect(TASKBAR_ICON_LEN - TASKBAR_BORDER_LEN, 0, TASKBAR_ICON_LEN, TASKBAR_ICON_LEN);
                g.fillRect(0, TASKBAR_ICON_LEN - TASKBAR_BORDER_LEN, TASKBAR_ICON_LEN, TASKBAR_ICON_LEN);
            }

            // paint border color
            Graphics g = paintedImage.getGraphics();
            g.setColor(builder.borderColor);
            g.fillRect(0, 0, TASKBAR_ICON_LEN, TASKBAR_ICON_LEN);

            ImageIcon defaultIcon = new ImageIcon(paintedImage);
            ImageIcon focusIcon = new ImageIcon(rescaleOp.filter(paintedImage, null));

            // image construction done so place on label
            newTaskbarIcon.setIcon(builder.focused ? focusIcon : defaultIcon);

            // add name label and mouse listeners on top of background image
            String localName = builder.name.trim().substring(0, Math.min(4, builder.name.trim().length())).trim();
            CyderLabel titleLabel = new CyderLabel(localName);
            titleLabel.setFont(labelFont);
            titleLabel.setForeground(CyderColors.vanilla);
            titleLabel.setBounds(0, 0, TASKBAR_ICON_LEN, TASKBAR_ICON_LEN);
            titleLabel.setFocusable(false);

            newTaskbarIcon.add(titleLabel);

            titleLabel.setToolTipText(localName);
            titleLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    builder.runnable.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    newTaskbarIcon.setIcon(builder.focused ? defaultIcon : focusIcon);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    newTaskbarIcon.setIcon(builder.focused ? focusIcon : defaultIcon);
                }
            });
        }

        innerTaskbarIcon = newTaskbarIcon;
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
        private boolean compact;
        private boolean focused;
        private Color borderColor;
        private ImageIcon customIcon;
        private Runnable runnable;
        private String name;

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
