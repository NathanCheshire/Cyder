package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.awt.event.*;
import java.util.Objects;

/**
 * A button with an ImageIcon as the clickable component.
 * Mainly used for DragLabel buttons.
 */
public class CyderIconButton extends JButton {
    /**
     * Suppress default JButton constructor.
     */
    private CyderIconButton() {
        throw new IllegalMethodException("Construction not allowed without proper parameters");
    }

    /**
     * The icon to use for the icon button.
     */
    private ImageIcon defaultIcon;

    /**
     * The hover icon to use for the icon button.
     */
    private ImageIcon hoverAndFocusIcon;

    /**
     * Constructs a new icon button with the following parameters, and default mouse and focus listeners.
     *
     * @param tooltipText       the tool tip text of the button
     * @param defaultIcon       the default icon
     * @param hoverAndFocusIcon the hover icon
     */
    public CyderIconButton(String tooltipText, ImageIcon defaultIcon, ImageIcon hoverAndFocusIcon) {
        this(tooltipText, defaultIcon, hoverAndFocusIcon, null, null);
    }

    /**
     * Constructs a new icon button with the following parameters, and no focus listener.
     *
     * @param tooltipText       the tool tip text of the button
     * @param defaultIcon       the default icon
     * @param hoverAndFocusIcon the hover icon
     * @param mouseListener     the custom mouse listener for when a mouse enters/exits the icon button's area
     */
    public CyderIconButton(String tooltipText, ImageIcon defaultIcon, ImageIcon hoverAndFocusIcon,
                           MouseListener mouseListener) {
        this(tooltipText, defaultIcon, hoverAndFocusIcon, mouseListener, new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
            }
        });
    }

    /**
     * Constructs a new icon button with the following parameters.
     *
     * @param tooltipText       the tooltip text of the button
     * @param defaultIcon       the default icon
     * @param hoverAndFocusIcon the hover icon
     * @param mouseListener     the custom mouse listener for when a mouse enters/exits the icon button's area
     * @param focusListener     the focus listener for when the button gains/loses focus
     */
    public CyderIconButton(String tooltipText, ImageIcon defaultIcon, ImageIcon hoverAndFocusIcon,
                           MouseListener mouseListener, FocusListener focusListener) {
        Preconditions.checkNotNull(tooltipText);
        Preconditions.checkArgument(!StringUtil.isNull(tooltipText));

        if (defaultIcon != null) {
            this.defaultIcon = defaultIcon;
        }

        if (hoverAndFocusIcon != null) {
            this.hoverAndFocusIcon = hoverAndFocusIcon;
        }

        Preconditions.checkArgument(!(defaultIcon != null && defaultIcon == hoverAndFocusIcon),
                "Provided hover image is the same as the default icon");
        Preconditions.checkArgument(!(defaultIcon != null
                        && hoverAndFocusIcon != null
                        && (defaultIcon.getIconWidth() != hoverAndFocusIcon.getIconWidth()
                        || defaultIcon.getIconHeight() != hoverAndFocusIcon.getIconHeight())),
                "Provided icons are not equal in size");

        setToolTipText(tooltipText);

        addMouseListener(Objects.requireNonNullElseGet(mouseListener, () -> new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setIcon(hoverAndFocusIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setIcon(defaultIcon);
            }
        }));

        addFocusListener(Objects.requireNonNullElseGet(focusListener, () -> new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setIcon(hoverAndFocusIcon);
            }

            @Override
            public void focusLost(FocusEvent e) {
                setIcon(defaultIcon);
            }
        }));

        if (defaultIcon != null) {
            setIcon(defaultIcon);
        }

        setFocusPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setVisible(true);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the default icon.
     *
     * @return the default icon
     */
    public ImageIcon getDefaultIcon() {
        return defaultIcon;
    }

    /**
     * Sets the default icon.
     *
     * @param defaultIcon the default icon to use
     */
    public void setDefaultIcon(ImageIcon defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    /**
     * Returns the hover and focus icon.
     *
     * @return the hover and focus icon
     */
    public ImageIcon getHoverAndFocusIcon() {
        return hoverAndFocusIcon;
    }

    /**
     * Sets the hover and focus icon.
     *
     * @param hoverAndFocusIcon the hover and focus icon
     */
    public void setHoverAndFocusIcon(ImageIcon hoverAndFocusIcon) {
        this.hoverAndFocusIcon = hoverAndFocusIcon;
    }

    /**
     * Flashes the icon button between the regular icon and
     * hover/focus icon for "iterations" iterations.
     *
     * @param iterations the number of iterations to flash the icon button for
     * @param msDelay    the delay in milliseconds between button flash calls
     */
    public void flash(int iterations, int msDelay) {
        Preconditions.checkArgument(hoverAndFocusIcon != null && defaultIcon != null,
                "Cannot flash when both icons are not present");

        CyderThreadRunner.submit(() -> {
            try {
                Icon originalIcon = getIcon();

                for (int i = 0 ; i < iterations ; i++) {
                    setIcon(hoverAndFocusIcon);
                    Thread.sleep(msDelay);
                    setIcon(defaultIcon);
                    Thread.sleep(msDelay);
                }

                setIcon(originalIcon);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "CyderIconButton Flash Thread");
    }
}
