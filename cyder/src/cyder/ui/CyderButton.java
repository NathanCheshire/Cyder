package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.*;


public class CyderButton extends JButton {
    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;
    private boolean threadsKilled = false;
    private Color backgroundColor = CyderColors.buttonColor;

    public CyderButton() {
        this("NULL TEXT");
    }

    public CyderButton(String text) {
        super(text);
        super.setContentAreaFilled(false);

        addMouseMotionListener(new CyderDraggableComponent());
        addActionListener(e -> Logger.log(Logger.Tag.ACTION, this));

        setFont(CyderFonts.segoe20);
        setBackground(backgroundColor);
        setColors(CyderColors.buttonColor);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);

        setUI(new MetalButtonUI() {
            protected Color getDisabledTextColor() {
                return Color.black;
            }
        });
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
        this.setBackground(c);
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

                    if (this.getParent() == null) {
                        killThreads();
                        return;
                    }
                }
            }

            catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },this.getName() + " alert thread").start();
    }

    public void killThreads() {
        threadsKilled = true;
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}