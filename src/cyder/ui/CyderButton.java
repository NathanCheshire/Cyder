package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handler.ErrorHandler;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;

public class CyderButton extends JButton {

    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;

    public CyderButton() {
        this(null);
    }

    public CyderButton(String text) {
        super(text);
        super.setContentAreaFilled(false);
        this.addMouseListener(new CyderMouseDraggable() {
            @Override
            public void mousePressed(MouseEvent me) {
                super.mousePressed(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                super.mouseReleased(me);
            }
        });
        setFont(CyderFonts.weatherFontSmall);
        setBackground(CyderColors.regularRed);
        setColors(CyderColors.regularRed);
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

                this.setBackground(c1);
                Thread.sleep(300);
                this.setBackground(c2);
                Thread.sleep(300);
                this.setBackground(c1);
                Thread.sleep(300);
                this.setBackground(c2);
                Thread.sleep(300);
                this.setBackground(c1);
                Thread.sleep(300);
                this.setBackground(c2);
                Thread.sleep(300);
                this.setBackground(c1);
                Thread.sleep(300);
                this.setBackground(c2);
                Thread.sleep(300);
                this.setBackground(c1);

            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },this.getName() + " alert thread").start();
    }
}