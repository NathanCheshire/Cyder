package cyder.ui.button;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;

import javax.swing.*;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;

/**
 * A button with an image icon.
 */
public class CyderIconButtonNew extends JButton {
    /**
     * The builder this cyder icon button was constructed from.
     */
    private final Builder builder;

    /**
     * Constructs a new cyder icon button.
     *
     * @param builder the builder to construct the button from
     */
    private CyderIconButtonNew(Builder builder) {
        Preconditions.checkNotNull(builder);

        this.builder = builder;

        setIcon(builder.getDefaultIcon());

        setToolTipText(builder.getTooltip());
        setFocusPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setVisible(true);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the builder used to construct this cyder icon button.
     *
     * @return the builder used to construct this cyder icon button
     */
    public Builder getBuilder() {
        return builder;
    }

    /**
     * Suppress default constructor.
     */
    private CyderIconButtonNew() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * A builder pattern for constructing a Cyder icon button.
     */
    public static final class Builder {
        /**
         * The tooltip for the button.
         */
        private final String tooltip;

        /**
         * The default icon for the button.
         */
        private ImageIcon defaultIcon;

        /**
         * The hover and focus icon for the button.
         */
        private ImageIcon hoverAndFocusIcon;

        /**
         * The focus listener or adapter for the button.
         * If none is provided, a default one is generated.
         */
        private FocusListener focusListener;

        /**
         * The mouse listener or adapter for the button.
         * If none is provided, a default one is generated.
         */
        private MouseListener mouseListener;

        /**
         * The action to invoke when the mouse is pressed.
         */
        private Runnable clickAction;

        /**
         * Constructs a new CyderIconButton builder.
         *
         * @param tooltip the tooltip for the button
         */
        public Builder(String tooltip) {
            Preconditions.checkNotNull(tooltip);
            Preconditions.checkArgument(!tooltip.isEmpty());

            this.tooltip = tooltip;
        }

        /**
         * Returns the tooltip for this cyder icon button.
         *
         * @return the tooltip for this cyder icon button
         */
        public String getTooltip() {
            return tooltip;
        }

        /**
         * Returns the default icon for this cyder icon button.
         *
         * @return the default icon for this cyder icon button
         */
        public ImageIcon getDefaultIcon() {
            return defaultIcon;
        }

        /**
         * Sets the default icon for this cyder icon button.
         *
         * @param defaultIcon the default icon for this cyder icon button
         * @return this builder.
         */
        public Builder setDefaultIcon(ImageIcon defaultIcon) {
            Preconditions.checkNotNull(defaultIcon);

            if (hoverAndFocusIcon != null) {
                Preconditions.checkArgument(defaultIcon.getIconWidth() == hoverAndFocusIcon.getIconWidth());
                Preconditions.checkArgument(defaultIcon.getIconHeight() == hoverAndFocusIcon.getIconHeight());
            }

            this.defaultIcon = defaultIcon;
            return this;
        }

        /**
         * Returns the hover and focus icon for this cyder icon button.
         *
         * @return the hover and focus icon for this cyder icon button
         */
        public ImageIcon getHoverAndFocusIcon() {
            return hoverAndFocusIcon;
        }

        /**
         * Sets the hover and focus icon for this cyder icon button.
         *
         * @param hoverAndFocusIcon the hover and focus icon for this cyder icon button
         * @return this builder
         */
        public Builder setHoverAndFocusIcon(ImageIcon hoverAndFocusIcon) {
            Preconditions.checkNotNull(hoverAndFocusIcon);

            if (defaultIcon != null) {
                Preconditions.checkArgument(defaultIcon.getIconWidth() == hoverAndFocusIcon.getIconWidth());
                Preconditions.checkArgument(defaultIcon.getIconHeight() == hoverAndFocusIcon.getIconHeight());
            }

            this.hoverAndFocusIcon = hoverAndFocusIcon;
            return this;
        }

        /**
         * Returns the focus listener for this cyder icon button.
         *
         * @return the focus listener for this cyder icon button
         */
        public FocusListener getFocusListener() {
            return focusListener;
        }

        /**
         * Sets the focus listener for this cyder icon button.
         *
         * @param focusListener the focus listener for this cyder icon button
         * @return this builder
         */
        public Builder setFocusListener(FocusListener focusListener) {
            this.focusListener = Preconditions.checkNotNull(focusListener);
            return this;
        }

        /**
         * Returns the mouse listener for this cyder icon button.
         *
         * @return the mouse listener for this cyder icon button
         */
        public MouseListener getMouseListener() {
            return mouseListener;
        }

        /**
         * Sets the mouse listener for this cyder icon button.
         *
         * @param mouseListener the mouse listener for this cyder icon button
         * @return this builder
         */
        public Builder setMouseListener(MouseListener mouseListener) {
            this.mouseListener = Preconditions.checkNotNull(mouseListener);
            return this;
        }

        /**
         * Returns the click action for this cyder icon button.
         *
         * @return the click action for this cyder icon button
         */
        public Runnable getClickAction() {
            return clickAction;
        }

        /**
         * Sets the click action for this cyder icon button.
         *
         * @param clickAction the click action for this cyder icon button
         * @return this builder
         */
        public Builder setClickAction(Runnable clickAction) {
            this.clickAction = Preconditions.checkNotNull(clickAction);
            return this;
        }

        public CyderIconButtonNew build() {
            return new CyderIconButtonNew(this);
        }
    }
}
