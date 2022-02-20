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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;


public class CyderButton extends JButton {
    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;
    private boolean threadsKilled = false;

    public CyderButton() {
        this("NULL TEXT");
    }

    public CyderButton(String text) {
        super(text);
        super.setContentAreaFilled(false);

        addMouseMotionListener(new CyderDraggableComponent());
        addActionListener(e -> Logger.log(Logger.Tag.ACTION, this));

        setFont(CyderFonts.segoe20);
        setColors(CyderColors.buttonColor);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);

        setUI(new MetalButtonUI() {
            protected Color getDisabledTextColor() {
                return Color.black;
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);

                if (isEnabled()) {
                    setBackground(hoverBackgroundColor);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);

                if (isEnabled()) {
                    setBackground(CyderColors.buttonColor);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        this.setFocusPainted(false);
        this.setBorder(new LineBorder(CyderColors.navy,5,false));
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
        // nothing since this is useless
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        this.pressedBackgroundColor = color.darker().darker();
        this.hoverBackgroundColor = color.darker();
    }

    /**
     * Sets the colors of the pressed, hover, and background color.
     *
     * @param c the color to use for the outlined properties
     */
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

                    if (threadsKilled)
                        return;
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