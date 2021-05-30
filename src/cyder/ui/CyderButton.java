package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.handler.ErrorHandler;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class CyderButton extends JButton {

    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;

    public CyderButton() {
        this(null);
    }

    public CyderButton(String text) {
        super(text);
        super.setContentAreaFilled(false);

        addMouseMotionListener(new CyderDragableComponent());

        setFont(CyderFonts.weatherFontSmall);
        setBackground(CyderColors.regularRed);
        setColors(CyderColors.regularRed);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
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

    public void alert() {
        new Thread(() -> {
            try {
                Color c1 = this.getBackground();
                Color c2 = c1.darker();

                for (int i = 0 ; i < 8 ; i++) {
                    this.setBackground(c2);
                    Thread.sleep(300);
                    this.setBackground(c1);
                    Thread.sleep(300);
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },this.getName() + " alert thread").start();
    }
}