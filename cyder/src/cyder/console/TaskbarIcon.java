package cyder.console;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderLabel;
import cyder.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

/**
 * A {@link Console} taskbar icon.
 */
public class TaskbarIcon {
    /**
     * The length of the taskbar icons generated.
     */
    private static final int ICON_LEN = 75;

    /**
     * The border length of the taskbar icons generated.
     */
    private static final int BORDER_LEN = 5;

    /**
     * The maximum number of chars to display when compact mode for taskbar icons is active.
     */
    private static final int MAX_COMPACT_MENU_CHARS = 10;

    /**
     * The color for custom painted taskbar icon borders.
     */
    private static final Color BORDER_COLOR = Color.black;

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
    private final RescaleOp rescaleOp = new RescaleOp(DARK_FACTOR, 0, null);

    /**
     * The actual icon used for the console taskbar.
     */
    private JLabel innerTaskbarIcon;

    /**
     * The builder last used to construct the encapsulated taskbar icon.
     */
    private Builder builder;

    /**
     * Constructs and generates a new taskbar icon.
     *
     * @param builder the builder to construct the taskbar icon from
     */
    public TaskbarIcon(Builder builder) {
        Preconditions.checkNotNull(builder.name);
        Preconditions.checkArgument(!builder.name.isEmpty());

        Logger.log(Logger.Tag.OBJECT_CREATION, this);

        this.builder = builder;

        generateTaskbarIcon(builder);
    }

    /**
     * Regenerates the taskbar icon based on the current builder's properties.
     */
    public void generateTaskbarIcon() {
        generateTaskbarIcon(builder);
    }

    /**
     * Generates the taskbar icon for a CyderFrame based on the provided properties.
     *
     * @param builder the TaskbarIcon builder to construct the TaskbarIcon from
     */
    public void generateTaskbarIcon(Builder builder) {
        Preconditions.checkNotNull(builder.name);
        Preconditions.checkArgument(!builder.name.isEmpty());

        this.builder = builder;

        if (builder.compact) {
            String name = builder.name.substring(0, Math.min(MAX_COMPACT_MENU_CHARS, builder.name.length()));

            JLabel usage = new JLabel(name);
            usage.setForeground(builder.focused ? CyderColors.regularRed : CyderColors.vanilla);
            usage.setFont(CyderFonts.DEFAULT_FONT_SMALL);
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
                usage.setToolTipText(builder.name);
            }

            innerTaskbarIcon = usage;
        } else {
            JLabel newTaskbarIcon = new JLabel();

            BufferedImage paintedImage;

            if (builder.customIcon != null) {
                paintedImage = ImageUtil.resizeImage(ICON_LEN,
                        ICON_LEN, builder.customIcon);
            } else {
                paintedImage = new BufferedImage(ICON_LEN,
                        ICON_LEN, BufferedImage.TYPE_INT_RGB);
                Graphics g = paintedImage.getGraphics();

                // paint center
                g.setColor(BORDER_COLOR);
                g.fillRect(0, 0, ICON_LEN, BORDER_LEN);
                g.fillRect(0, 0, BORDER_LEN, ICON_LEN);
                g.fillRect(ICON_LEN - BORDER_LEN, 0, ICON_LEN, ICON_LEN);
                g.fillRect(0, ICON_LEN - BORDER_LEN, ICON_LEN, ICON_LEN);
            }

            // paint border color
            Graphics g = paintedImage.getGraphics();
            g.setColor(builder.customIcon == null ? builder.borderColor : BORDER_COLOR);
            g.fillRect(0, 0, BORDER_LEN, ICON_LEN);
            g.fillRect(0, 0, ICON_LEN, BORDER_LEN);
            g.fillRect(ICON_LEN - 5, 0, ICON_LEN, ICON_LEN);
            g.fillRect(0, ICON_LEN - 5, ICON_LEN, ICON_LEN);

            ImageIcon defaultIcon = new ImageIcon(paintedImage);
            ImageIcon focusIcon = new ImageIcon(rescaleOp.filter(paintedImage, null));

            // image construction done so place on label
            newTaskbarIcon.setIcon(builder.focused ? focusIcon : defaultIcon);
            newTaskbarIcon.setSize(ICON_LEN, ICON_LEN);

            // add name label and mouse listeners on top of background image
            String localName = builder.name.trim().substring(0, Math.min(4, builder.name.trim().length())).trim();
            CyderLabel titleLabel = new CyderLabel(builder.customIcon == null ? localName : "");
            titleLabel.setFont(labelFont);
            titleLabel.setForeground(CyderColors.vanilla);
            titleLabel.setBounds(0, 0, ICON_LEN, ICON_LEN);
            titleLabel.setFocusable(false);

            newTaskbarIcon.add(titleLabel);

            titleLabel.setToolTipText(builder.name);
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

            innerTaskbarIcon = newTaskbarIcon;
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
     * Returns the builder last used to construct the encapsulated taskbar icon.
     *
     * @return the builder last used to construct the encapsulated taskbar icon
     */
    public Builder getBuilder() {
        return this.builder;
    }

    /**
     * Runs the runnable associated with this taskbar icon.
     */
    public void runRunnable() {
        this.builder.runnable.run();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TaskbarIcon)) {
            return false;
        }

        TaskbarIcon other = (TaskbarIcon) o;

        return other.builder.equals(builder);
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

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Builder)) {
                return false;
            }

            Builder other = (Builder) o;

            boolean ret = other.compact == compact
                    && other.focused == focused
                    && other.borderColor.equals(borderColor);

            if (other.customIcon != null) {
                ret &= other.customIcon.equals(customIcon);
            }

            if (other.runnable != null) {
                ret &= other.runnable.equals(runnable);
            }

            if (other.name != null) {
                ret &= other.name.equals(name);
            }

            return ret;
        }
    }
}
