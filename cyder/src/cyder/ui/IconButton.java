package cyder.ui;

import cyder.utilities.StringUtil;

import javax.swing.*;
import java.awt.event.*;
import java.util.Objects;

/**
 * A button with an ImageIcon as the clickable component.
 * Mainly used for DragLabel buttons.
 */
public class IconButton extends JButton {
    /**
     * Supress default JButton constructor.
     */
    private IconButton() {
        super();
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
     * @param tooltipText the tool tip text of the button
     * @param defaultIcon the default icon
     * @param hoverAndFocusIcon the hover icon
     */
    public IconButton(String tooltipText, ImageIcon defaultIcon, ImageIcon hoverAndFocusIcon) {
        this(tooltipText, defaultIcon, hoverAndFocusIcon, null, null);
    }

    /**
     * Constructs a new icon button with the following parameters, and no focus listener.
     *
     * @param tooltipText the tool tip text of the button
     * @param defaultIcon the default icon
     * @param hoverAndFocusIcon the hover icon
     * @param mouseListener the custom mouse listener for when a mouse enters/exits the icon button's area
     */
    public IconButton(String tooltipText, ImageIcon defaultIcon, ImageIcon hoverAndFocusIcon, MouseListener mouseListener) {
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
     * @param tooltipText the tooltip text of the button
     * @param defaultIcon the default icon
     * @param hoverAndFocusIcon the hover icon
     * @param mouseListener the custom mouse listener for when a mouse enters/exits the icon button's area
     * @param focusListener the focus listener for when the button gains/loses focus
     */
    public IconButton(String tooltipText, ImageIcon defaultIcon, ImageIcon hoverAndFocusIcon,
                      MouseListener mouseListener, FocusListener focusListener) {
        if (StringUtil.isNull(tooltipText))
            throw new IllegalArgumentException("Tooltip text is null");
        if (defaultIcon != null && defaultIcon == hoverAndFocusIcon)
            throw new IllegalArgumentException("Provided hover image is the same as the default icon");
        if (defaultIcon != null && hoverAndFocusIcon != null && (defaultIcon.getIconWidth() != hoverAndFocusIcon.getIconWidth()
                                || defaultIcon.getIconHeight() != hoverAndFocusIcon.getIconHeight()))
            throw new IllegalArgumentException("Provided icons are not equal in size");

        if (defaultIcon != null)
            this.defaultIcon = defaultIcon;
        if (hoverAndFocusIcon != null)
            this.hoverAndFocusIcon = hoverAndFocusIcon;

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

        if (defaultIcon != null)
            setIcon(defaultIcon);

        setFocusPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setVisible(true);
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
}
