package cyder.ui;

import cyder.utilities.StringUtil;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        if (StringUtil.isNull(tooltipText))
            throw new IllegalArgumentException("Tooltip text is null");
        if (defaultIcon == null || hoverAndFocusIcon == null)
            throw new IllegalArgumentException("Provided image is null");
        if (defaultIcon == hoverAndFocusIcon)
            throw new IllegalArgumentException("Provided hover image is the same as the default icon");
        if (defaultIcon.getIconWidth() != hoverAndFocusIcon.getIconWidth() || defaultIcon.getIconHeight() != hoverAndFocusIcon.getIconHeight())
            throw new IllegalArgumentException("Provided icons are not equal in size");

        this.defaultIcon = defaultIcon;
        this.hoverAndFocusIcon = hoverAndFocusIcon;
        setToolTipText(tooltipText);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setIcon(hoverAndFocusIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setIcon(defaultIcon);
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setIcon(hoverAndFocusIcon);
            }

            @Override
            public void focusLost(FocusEvent e) {
                setIcon(defaultIcon);
            }
        });

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
