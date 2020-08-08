package com.cyder.ui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JButton;
import javax.swing.border.LineBorder;

public class CyderButton extends JButton {

    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;

    public CyderButton() {
        this(null);
    }

    public CyderButton(String text) {
        super(text);
        super.setContentAreaFilled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.setFocusPainted(false);
        this.setBorder(new LineBorder(new Color(26, 32, 51),5,false));
        if (getModel().isPressed()) {
            g.setColor(pressedBackgroundColor);
        } else if (getModel().isRollover()) {
            g.setColor(hoverBackgroundColor);
        } else {
            g.setColor(getBackground());
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override
    public void setContentAreaFilled(boolean b) {
    }

    public void setColors(Color c) {
        this.pressedBackgroundColor = c.darker().darker();
        this.hoverBackgroundColor = c.darker();
    }

    public Color getHoverBackgroundColor() {
        return hoverBackgroundColor;
    }

    public void setHoverBackgroundColor(Color hoverBackgroundColor) {
        this.hoverBackgroundColor = hoverBackgroundColor;
    }

    public Color getPressedBackgroundColor() {
        return pressedBackgroundColor;
    }

    public void setPressedBackgroundColor(Color pressedBackgroundColor) {
        this.pressedBackgroundColor = pressedBackgroundColor;
    }
}