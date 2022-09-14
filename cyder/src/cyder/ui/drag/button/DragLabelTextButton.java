package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.Logger;
import cyder.ui.drag.CyderDragLabel;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A text button for a {@link CyderDragLabel}.
 */
public class DragLabelTextButton extends JLabel {
    /**
     * Suppress default constructor.
     */
    private DragLabelTextButton() {
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Constructs and returns a new drag label text button from the contents of the provided builder.
     *
     * @param builder the builder to use for construction of the text button.
     * @return the drag label text button
     */
    public static DragLabelTextButton generateTextButton(Builder builder) {
        Preconditions.checkNotNull(builder);

        DragLabelTextButton ret = new DragLabelTextButton();
        ret.setText(StringUtil.getTrimmedText(builder.getText()));
        ret.setForeground(builder.getDefaultColor());
        ret.setFont(builder.getFont());

        String tooltip = builder.getTooltip();
        if (tooltip != null) ret.setToolTipText(tooltip);

        ret.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Runnable action = builder.getClickAction();
                if (action != null) action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                Runnable action = builder.getMouseEnterAction();
                if (action != null) action.run();

                ret.setForeground(builder.hoverColor);
                ret.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Runnable action = builder.getMouseExitAction();
                if (action != null) action.run();

                ret.setForeground(builder.defaultColor);
                ret.repaint();
            }
        });

        return ret;
    }

    /**
     * A builder for a drag label text button.
     */
    public static class Builder {
        /**
         * The default font for a drag label text button.
         */
        public static final Font DEFAULT_FONT = CyderFonts.DEFAULT_FONT_SMALL;

        /**
         * The default foreground of a drag label text button.
         */
        public static final Color DEFAULT_FOREGROUND = CyderColors.vanilla;

        /**
         * The default hover color of a drag label text button.
         */
        public static final Color DEFAULT_HOVER_COLOR = CyderColors.regularRed;

        /**
         * The text contents of a drag label text button.
         */
        private final String text;

        /**
         * The action to invoke on a click event.
         */
        private Runnable clickAction;

        /**
         * The action to invoke on a mouse enter event.
         */
        private Runnable mouseEnterAction;

        /**
         * The action to invoke on a mouse exit event.
         */
        private Runnable mouseExitAction;

        /**
         * The font for this button. Changing this is highly discouraged.
         */
        private Font font = DEFAULT_FONT;

        /**
         * The default color for this button. Changing this is highly discouraged.
         */
        private Color defaultColor = DEFAULT_FOREGROUND;

        /**
         * The hover color for this button. Changing this is highly discouraged.
         */
        private Color hoverColor = DEFAULT_HOVER_COLOR;

        /**
         * The text for the tooltip of the button.
         */
        private String tooltip;

        /**
         * Constructs a new builder.
         *
         * @param text the text of the drag label button.
         */
        public Builder(String text) {
            Preconditions.checkNotNull(text);
            text = text.trim();
            Preconditions.checkArgument(!text.isEmpty());

            this.text = text;
        }

        @CanIgnoreReturnValue
        public Builder setClickAction(Runnable clickAction) {
            this.clickAction = Preconditions.checkNotNull(clickAction);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder setMouseEnterAction(Runnable mouseEnterAction) {
            this.mouseEnterAction = Preconditions.checkNotNull(mouseEnterAction);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder setMouseExitAction(Runnable mouseExitAction) {
            this.mouseExitAction = Preconditions.checkNotNull(mouseExitAction);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder setFont(Font font) {
            this.font = Preconditions.checkNotNull(font);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder setDefaultColor(Color color) {
            this.defaultColor = Preconditions.checkNotNull(color);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder setHoverColor(Color color) {
            this.hoverColor = Preconditions.checkNotNull(color);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder setTooltip(String tooltip) {
            Preconditions.checkNotNull(tooltip);
            Preconditions.checkArgument(!tooltip.isEmpty());
            this.tooltip = tooltip;
            return this;
        }

        public String getText() {
            return text;
        }

        public Runnable getClickAction() {
            return clickAction;
        }

        public Runnable getMouseEnterAction() {
            return mouseEnterAction;
        }

        public Runnable getMouseExitAction() {
            return mouseExitAction;
        }

        public Font getFont() {
            return font;
        }

        public Color getDefaultColor() {
            return defaultColor;
        }

        public Color getHoverColor() {
            return hoverColor;
        }

        public String getTooltip() {
            return tooltip;
        }
    }
}
