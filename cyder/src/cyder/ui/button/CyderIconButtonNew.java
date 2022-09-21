package cyder.ui.button;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import javax.swing.*;
import java.awt.event.*;

/**
 * A button with an image icon.
 */
public class CyderIconButtonNew extends JButton {
    /**
     * The builder this cyder icon button was constructed from.
     */
    private final Builder builder;

    /**
     * Whether this button is toggled on if the button is a toggle icon button.
     */
    private boolean toggledOn;

    /**
     * Constructs a new cyder icon button.
     *
     * @param builder the builder to construct the button from
     */
    private CyderIconButtonNew(Builder builder) {
        Preconditions.checkNotNull(builder);

        this.builder = builder;

        ImageIcon icon = builder.getDefaultIcon();
        setIcon(icon);
        setSize(icon.getIconWidth(), icon.getIconHeight());

        setToolTipText(builder.getTooltip());
        setFocusPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setVisible(true);

        addMouseListener();
        addFocusListener();

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Adds the builder's focus listener or generates and adds the default focus listener if not set.
     */
    @ForReadability
    private void addFocusListener() {
        if (builder.getFocusListener() == null) {
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (!builder.isToggleButton()) {
                        setIcon(builder.getHoverAndFocusIcon());
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (!builder.isToggleButton()) {
                        setIcon(builder.getDefaultIcon());
                    }
                }
            });
        } else {
            addFocusListener(builder.getFocusListener());
        }
    }

    /**
     * Adds the builder's mouse listener or generates and adds the default mouse listener if not set.
     */
    @ForReadability
    private void addMouseListener() {
        if (builder.getMouseListener() == null) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Runnable runnable = builder.getClickAction();
                    if (runnable != null) runnable.run();

                    if (builder.isToggleButton()) {
                        toggledOn = !toggledOn;

                        if (toggledOn) {
                            setIcon(builder.getDefaultIcon());
                        } else {
                            setIcon(builder.getHoverAndFocusIcon());
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (builder.isToggleButton()) {

                        if (toggledOn) {
                            setIcon(builder.getDefaultIcon());
                        } else {
                            setIcon(builder.getHoverAndFocusIcon());
                        }
                    } else {
                        setIcon(builder.getHoverAndFocusIcon());
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (builder.isToggleButton()) {
                        if (toggledOn) {
                            setIcon(builder.getHoverAndFocusIcon());
                        } else {
                            setIcon(builder.getDefaultIcon());
                        }
                    } else {
                        setIcon(builder.getDefaultIcon());
                    }
                }
            });
        } else {
            addMouseListener(builder.getMouseListener());
        }
    }

    /**
     * Resets the state/icon of this cyder icon button.
     */
    public void reset() {
        toggledOn = false;
        setIcon(builder.defaultIcon);
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
     * Flashes the icon button between the regular icon and
     * hoverAndFocus icon for the provided number of iterations with
     * the provided delay.
     *
     * @param iterations the number of iterations to flash the icon button for
     * @param msDelay    the delay in milliseconds between button flash calls
     */
    public void flash(int iterations, int msDelay) {
        CyderThreadRunner.submit(() -> {
            Icon originalIcon = getIcon();
            ImageIcon hoverAndFocusIcon = builder.getHoverAndFocusIcon();
            ImageIcon defaultIcon = builder.getDefaultIcon();

            for (int i = 0 ; i < iterations ; i++) {
                setIcon(hoverAndFocusIcon);
                ThreadUtil.sleep(msDelay);
                setIcon(defaultIcon);
                ThreadUtil.sleep(msDelay);
            }

            setIcon(originalIcon);
        }, FLASH_THREAD_NAME + ", iterations=" + iterations + ", delay=" + msDelay + "ms");
    }

    private static final String FLASH_THREAD_NAME = "CyderIconButton Flash Thread";

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
        private final ImageIcon defaultIcon;

        /**
         * The hover and focus icon for the button.
         */
        private final ImageIcon hoverAndFocusIcon;

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
         * Whether clicking the button toggles the state, and thus the icon.
         */
        private boolean toggleButton;

        /**
         * Constructs a new CyderIconButton builder.
         *
         * @param tooltip the tooltip for the button
         */
        public Builder(String tooltip, ImageIcon defaultIcon, ImageIcon hoverAndFocusIcon) {
            Preconditions.checkNotNull(tooltip);
            Preconditions.checkArgument(!tooltip.isEmpty());

            Preconditions.checkNotNull(defaultIcon);
            Preconditions.checkNotNull(hoverAndFocusIcon);
            Preconditions.checkArgument(defaultIcon.getIconWidth() == hoverAndFocusIcon.getIconWidth());
            Preconditions.checkArgument(defaultIcon.getIconHeight() == hoverAndFocusIcon.getIconHeight());

            this.tooltip = tooltip;
            this.defaultIcon = defaultIcon;
            this.hoverAndFocusIcon = hoverAndFocusIcon;
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
         * Returns the hover and focus icon for this cyder icon button.
         *
         * @return the hover and focus icon for this cyder icon button
         */
        public ImageIcon getHoverAndFocusIcon() {
            return hoverAndFocusIcon;
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

        /**
         * Returns whether this cyder icon button should be a toggle button.
         *
         * @return whether this cyder icon button should be a toggle button
         */
        public boolean isToggleButton() {
            return toggleButton;
        }

        /**
         * Sets whether this cyder icon button should be a toggle button.
         *
         * @param toggleButton whether this cyder icon button should be a toggle button
         * @return this builder
         */
        public Builder setToggleButton(boolean toggleButton) {
            this.toggleButton = toggleButton;
            return this;
        }

        /**
         * Builds and returns a new cyder icon button using this builder.
         *
         * @return a new cyder icon button
         */
        public CyderIconButtonNew build() {
            return new CyderIconButtonNew(this);
        }
    }
}
