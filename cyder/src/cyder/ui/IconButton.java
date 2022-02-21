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
    private IconButton() {
        super();
    }

    private ImageIcon defaultIcon;
    private ImageIcon hoverAndFocusIcon;

    public IconButton(String tooltipText, ImageIcon defaultIcon, ImageIcon hoverAndFocusIcon) {
        this(tooltipText, defaultIcon, hoverAndFocusIcon, null, null);
    }

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

    public ImageIcon getDefaultIcon() {
        return defaultIcon;
    }

    public void setDefaultIcon(ImageIcon defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    public ImageIcon getHoverAndFocusIcon() {
        return hoverAndFocusIcon;
    }

    public void setHoverAndFocusIcon(ImageIcon hoverAndFocusIcon) {
        this.hoverAndFocusIcon = hoverAndFocusIcon;
    }
}
