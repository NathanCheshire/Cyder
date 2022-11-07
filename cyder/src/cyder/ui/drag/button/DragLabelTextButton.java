package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.drag.CyderDragLabel;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A text button for a {@link CyderDragLabel}.
 */
public class DragLabelTextButton extends JLabel {
    /**
     * Suppress default constructor.
     */
    private DragLabelTextButton() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * The builder for this drag label text button.
     */
    private Builder builder;

    /**
     * Sets the builder for this drag label text button.
     *
     * @param builder the builder for this drag label text button
     */
    public void setBuilder(Builder builder) {
        this.builder = builder;
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
        ret.setBuilder(builder);
        ret.setText(builder.getText());
        ret.setForeground(builder.getDefaultColor());
        ret.setFont(builder.getFont());

        String tooltip = builder.getTooltip();
        if (tooltip != null) ret.setToolTipText(tooltip);

        int width = StringUtil.getMinWidth(builder.getText(), builder.getFont());
        int height = StringUtil.getAbsoluteMinHeight(builder.getText(), builder.getFont());
        ret.setSize(width, height);

        ret.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Runnable action = builder.getClickAction();
                if (action != null) action.run();
            }

            // todo for some reason not being repainted on hover actions?

            @Override
            public void mouseEntered(MouseEvent e) {
                Runnable action = builder.getMouseEnterAction();
                if (action != null) action.run();

                if (builder.getStateSelected() != null) {
                    System.out.println(builder.getStateSelected());
                    if (builder.getStateSelected().get()) {
                        ret.setForeground(builder.defaultColor);
                    } else {
                        ret.setForeground(builder.hoverColor);
                    }
                } else {
                    ret.setForeground(builder.hoverColor);
                }

                ret.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Runnable action = builder.getMouseExitAction();
                if (action != null) action.run();

                if (builder.getStateSelected() != null) {
                    if (builder.getStateSelected().get()) {
                        ret.setForeground(builder.hoverColor);
                    } else {
                        ret.setForeground(builder.defaultColor);
                    }

                } else {
                    ret.setForeground(builder.defaultColor);
                }

                ret.repaint();
            }
        });

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void repaint() {
        if (builder == null) return;

        if (builder.getStateSelected() != null) {
            if (builder.getStateSelected().get()) {
                setForeground(builder.hoverColor);
            } else {
                setForeground(builder.defaultColor);
            }
        } else {
            setForeground(builder.defaultColor);
        }

        super.repaint();
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
         * Whether this drag label text button should be painted as selected.
         */
        private AtomicBoolean stateSelected;

        /**
         * Constructs a new builder.
         *
         * @param text the text of the drag label button.
         */
        public Builder(String text) {
            Preconditions.checkNotNull(text);
            text = StringUtil.getTrimmedText(text);
            Preconditions.checkArgument(!text.isEmpty());

            this.text = text;
        }

        /**
         * Sets the click action for this text button.
         *
         * @param clickAction the click action for this text button
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setClickAction(Runnable clickAction) {
            this.clickAction = Preconditions.checkNotNull(clickAction);
            return this;
        }

        /**
         * Sets the mouse enter action for this text button.
         *
         * @param mouseEnterAction the mouse enter action for this text button
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setMouseEnterAction(Runnable mouseEnterAction) {
            this.mouseEnterAction = Preconditions.checkNotNull(mouseEnterAction);
            return this;
        }

        /**
         * Sets the mouse exit action for this text button.
         *
         * @param mouseExitAction the mouse exit action for this text button
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setMouseExitAction(Runnable mouseExitAction) {
            this.mouseExitAction = Preconditions.checkNotNull(mouseExitAction);
            return this;
        }

        /**
         * Sets the font for this text button.
         *
         * @param font the font for this text button
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setFont(Font font) {
            this.font = Preconditions.checkNotNull(font);
            return this;
        }

        /**
         * Sets the default color for this text button.
         *
         * @param color the default color for this text button
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setDefaultColor(Color color) {
            this.defaultColor = Preconditions.checkNotNull(color);
            return this;
        }

        /**
         * Sets the hover color for this text button.
         *
         * @param color the hover color for this text button
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setHoverColor(Color color) {
            this.hoverColor = Preconditions.checkNotNull(color);
            return this;
        }

        /**
         * Sets the tooltip for this text button.
         *
         * @param tooltip the tooltip for this text button
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setTooltip(String tooltip) {
            Preconditions.checkNotNull(tooltip);
            Preconditions.checkArgument(!tooltip.isEmpty());
            this.tooltip = tooltip;
            return this;
        }

        /**
         * Sets the atomic boolean to determine whether the state of this drag label button should be selected
         * meaning it will be painted using the hover color by default.
         *
         * @param stateSelected the state selected atomic boolean
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setStateSelectedAtomicBoolean(AtomicBoolean stateSelected) {
            Preconditions.checkNotNull(stateSelected);
            this.stateSelected = stateSelected;
            return this;
        }

        /**
         * Returns the text for this text button.
         *
         * @return the text for this text button
         */
        public String getText() {
            return text;
        }

        /**
         * Returns the click action for this text button.
         *
         * @return the click action for this text button
         */
        public Runnable getClickAction() {
            return clickAction;
        }

        /**
         * Returns the mouse enter action for this text button.
         *
         * @return the mouse enter action for this text button
         */
        public Runnable getMouseEnterAction() {
            return mouseEnterAction;
        }

        /**
         * Returns the mouse exit action for this text button.
         *
         * @return the mouse exit action for this text button
         */
        public Runnable getMouseExitAction() {
            return mouseExitAction;
        }

        /**
         * Returns the font for this text button.
         *
         * @return the font for this text button
         */
        public Font getFont() {
            return font;
        }

        /**
         * Returns the default color for this text button.
         *
         * @return the default color for this text button
         */
        public Color getDefaultColor() {
            return defaultColor;
        }

        /**
         * Returns the hover color for this text button.
         *
         * @return the hover color for this text button
         */
        public Color getHoverColor() {
            return hoverColor;
        }

        /**
         * Returns the tooltip for this text button.
         *
         * @return the tooltip for this text button
         */
        public String getTooltip() {
            return tooltip;
        }

        /**
         * Returns the state selected atomic boolean.
         *
         * @return the state selected atomic boolean
         */
        public AtomicBoolean getStateSelected() {
            return stateSelected;
        }
    }
}
