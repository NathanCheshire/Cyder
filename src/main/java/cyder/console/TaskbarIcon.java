package main.java.cyder.console;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import main.java.cyder.constants.CyderColors;
import main.java.cyder.constants.CyderFonts;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.ui.label.CyderLabel;
import main.java.cyder.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Objects;

/**
 * A {@link Console} taskbar icon for the console menu.
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
    private static final RescaleOp rescaleOp = new RescaleOp(DARK_FACTOR, 0, null);

    /**
     * The actual icon used for the console taskbar.
     */
    private JLabel innerTaskbarIcon;

    /**
     * The builder last used to construct the encapsulated taskbar icon.
     */
    private final Builder builder;

    /**
     * Suppress default constructor.
     */
    private TaskbarIcon() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs and generates a new taskbar icon.
     *
     * @param builder the builder to construct the taskbar icon from
     */
    private TaskbarIcon(Builder builder) {
        Preconditions.checkNotNull(builder.getName());
        Preconditions.checkArgument(!builder.getName().isEmpty());

        Logger.log(LogTag.OBJECT_CREATION, this);

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
    private void generateTaskbarIcon(Builder builder) {
        Preconditions.checkNotNull(builder.getName());
        Preconditions.checkArgument(!builder.getName().isEmpty());

        if (builder.isCompact()) {
            String name = builder.getName().substring(0, Math.min(MAX_COMPACT_MENU_CHARS, builder.getName().length()));

            JLabel usage = new JLabel(name);
            usage.setForeground(builder.isFocused() ? CyderColors.regularRed : CyderColors.vanilla);
            usage.setFont(CyderFonts.DEFAULT_FONT_SMALL);
            usage.setVerticalAlignment(SwingConstants.CENTER);

            usage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    builder.getRunnable().run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    usage.setForeground(builder.isFocused() ? CyderColors.vanilla : CyderColors.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    usage.setForeground(builder.isFocused() ? CyderColors.regularRed : CyderColors.vanilla);
                }
            });

            // if had to cut off text, make tooltip show full
            if (!builder.getName().equalsIgnoreCase(name)) {
                usage.setToolTipText(builder.getName());
            }

            innerTaskbarIcon = usage;
        } else {
            JLabel newTaskbarIcon = new JLabel();

            BufferedImage paintedImage;

            if (builder.getCustomIcon() != null) {
                paintedImage = ImageUtil.resizeImage(ICON_LEN,
                        ICON_LEN, builder.getCustomIcon());
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
            g.setColor(builder.getCustomIcon() == null ? builder.getBorderColor() : BORDER_COLOR);
            g.fillRect(0, 0, BORDER_LEN, ICON_LEN);
            g.fillRect(0, 0, ICON_LEN, BORDER_LEN);
            g.fillRect(ICON_LEN - 5, 0, ICON_LEN, ICON_LEN);
            g.fillRect(0, ICON_LEN - 5, ICON_LEN, ICON_LEN);

            ImageIcon defaultIcon = new ImageIcon(paintedImage);
            ImageIcon focusIcon = new ImageIcon(rescaleOp.filter(paintedImage, null));

            // image construction done so place on label
            newTaskbarIcon.setIcon(builder.isFocused() ? focusIcon : defaultIcon);
            newTaskbarIcon.setSize(ICON_LEN, ICON_LEN);

            // add name label and mouse listeners on top of background image
            String localName =
                    builder.getName().trim().substring(0, Math.min(4, builder.getName().trim().length())).trim();
            CyderLabel titleLabel = new CyderLabel(builder.getCustomIcon() == null ? localName : "");
            titleLabel.setFont(labelFont);
            titleLabel.setForeground(CyderColors.vanilla);
            titleLabel.setBounds(0, 0, ICON_LEN, ICON_LEN);
            titleLabel.setFocusable(false);

            newTaskbarIcon.add(titleLabel);

            titleLabel.setToolTipText(builder.getName());
            titleLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (builder.getRunnable() != null) {
                        builder.getRunnable().run();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    newTaskbarIcon.setIcon(builder.isFocused() ? defaultIcon : focusIcon);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    newTaskbarIcon.setIcon(builder.isFocused() ? focusIcon : defaultIcon);
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
     * Sets whether this taskbar icon is focused.
     *
     * @param focused whether this taskbar icon is focused
     */
    public void setFocused(boolean focused) {
        this.builder.setFocused(focused);
    }

    /**
     * Runs the runnable associated with this taskbar icon.
     */
    public void runRunnable() {
        this.builder.getRunnable().run();
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

        return builder.equals(other.builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return builder.hashCode();
    }

    /**
     * A builder for a {@link TaskbarIcon}.
     */
    public static final class Builder {
        /**
         * Whether this icon should be compact.
         */
        private boolean compact;

        /**
         * Whether this icon is focused.
         */
        private boolean focused;

        /**
         * The border color.
         */
        private Color borderColor;

        /**
         * A possible custom icon.
         */
        private ImageIcon customIcon;

        /**
         * The runnable to invoke upon a click action.
         */
        private Runnable runnable;

        /**
         * The name of the icon.
         */
        private final String name;

        /**
         * Constructs a new builder for a taskbar icon.
         *
         * @param name the name/tooltip for the taskbar icon
         */
        public Builder(String name) {
            Preconditions.checkNotNull(name);
            Preconditions.checkArgument(!name.isEmpty());

            this.name = name;
        }

        /**
         * Sets whether this taskbar icon should be painted in compact mode.
         *
         * @param compact whether this taskbar icon should be painted in compact mode
         * @return this Builder
         */
        @CanIgnoreReturnValue
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
        @CanIgnoreReturnValue
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
        @CanIgnoreReturnValue
        public Builder setBorderColor(Color borderColor) {
            Preconditions.checkNotNull(borderColor);

            this.borderColor = borderColor;
            return this;
        }

        /**
         * Sets the customIcon for this taskbar icon.
         *
         * @param customIcon the name for this taskbar icon
         * @return this Builder
         */
        @CanIgnoreReturnValue
        public Builder setCustomIcon(ImageIcon customIcon) {
            Preconditions.checkNotNull(customIcon);

            this.customIcon = customIcon;
            return this;
        }

        /**
         * Sets the runnable for this taskbar icon.
         *
         * @param runnable the runnable for this taskbar icon
         * @return this Builder
         */
        @CanIgnoreReturnValue
        public Builder setRunnable(Runnable runnable) {
            Preconditions.checkNotNull(runnable);

            this.runnable = runnable;
            return this;
        }

        /**
         * Returns whether this icon should be generated as compact.
         *
         * @return whether this icon should be generated as compact
         */
        public boolean isCompact() {
            return compact;
        }

        /**
         * Returns whether this icon should be generated as focused.
         *
         * @return whether this icon should be generated as focused
         */
        public boolean isFocused() {
            return focused;
        }

        /**
         * Returns the border color for this icon.
         *
         * @return the border color for this icon
         */
        public Color getBorderColor() {
            return borderColor;
        }

        /**
         * Returns the custom icon for this icon.
         *
         * @return the custom icon for this icon
         */
        public ImageIcon getCustomIcon() {
            return customIcon;
        }

        /**
         * Returns the runnable for this icon.
         *
         * @return the runnable for this icon
         */
        public Runnable getRunnable() {
            return runnable;
        }

        /**
         * Returns the name and tooltip of this icon.
         *
         * @return the name and tooltip of this icon
         */
        public String getName() {
            return name;
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

            boolean ret = other.isCompact() == compact
                    && other.isFocused() == focused;

            if (other.getBorderColor() != null) {
                ret = ret && other.getBorderColor().equals(borderColor);
            }

            if (other.getCustomIcon() != null) {
                ret = ret && other.getCustomIcon().equals(customIcon);
            }

            if (other.getRunnable() != null) {
                ret = ret && other.getRunnable().equals(runnable);
            }

            if (other.getName() != null) {
                ret = ret && other.getName().equals(name);
            }

            return ret;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int ret = Boolean.hashCode(compact);
            ret = 31 * ret + Boolean.hashCode(focused);
            ret = 31 * ret + Objects.hashCode(borderColor);
            ret = 31 * ret + Objects.hashCode(customIcon);
            ret = 31 * ret + Objects.hashCode(runnable);
            ret = 31 * ret + Objects.hashCode(name);
            return ret;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "TaskbarIconBuilder{"
                    + "compact: " + compact
                    + ", focused: " + focused
                    + ", borderColor: " + borderColor
                    + ", customIcon: " + customIcon
                    + ", runnable: " + runnable
                    + ", name: \"" + name + "\""
                    + "}";
        }
    }
}
