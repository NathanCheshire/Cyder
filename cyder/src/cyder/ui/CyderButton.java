package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CyderButton extends JButton {
    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;
    private boolean threadsKilled;
    private Color originalColor;

    public CyderButton() {
        this("NULL TEXT");
    }

    public CyderButton(String text) {
        super(text);
        super.setContentAreaFilled(false);

        addMouseMotionListener(new CyderDraggableComponent());
        addActionListener(e -> Logger.log(Logger.Tag.UI_ACTION, this));

        setFont(CyderFonts.segoe20);
        setColors(CyderColors.buttonColor);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);

        setUI(new MetalButtonUI() {
            protected Color getDisabledTextColor() {
                return Color.black;
            }
        });

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        setFocusPainted(false);
        setBorder(new LineBorder(CyderColors.navy, 5, false));
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
        pressedBackgroundColor = color.darker().darker();
        hoverBackgroundColor = color.darker();
    }

    /**
     * Sets the colors of the pressed, hover, and background color.
     *
     * @param c the color to use for the outlined properties
     */
    public void setColors(Color c) {
        originalColor = c;

        pressedBackgroundColor = c.darker().darker();
        hoverBackgroundColor = c.darker();
        setBackground(c);
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
        CyderThreadRunner.submit(() -> {
            try {
                Color c1 = getBackground();
                Color c2 = c1.darker();

                for (int i = 0 ; i < 8 ; i++) {
                    setBackground(c2);
                    Thread.sleep(300);
                    setBackground(c1);
                    Thread.sleep(300);

                    if (getParent() == null) {
                        killThreads();
                        return;
                    }

                    if (threadsKilled)
                        return;
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, getName() + " Alerter");
    }

    public void killThreads() {
        threadsKilled = true;
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    /**
     * The default focus listener for CyderButtons to show when the button is the focus owner.
     */
    private final FocusListener defaultFocusListener = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            super.focusGained(e);

            if (isEnabled()) {
                setBackground(originalColor.darker());
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            super.focusLost(e);

            if (isEnabled()) {
                setBackground(originalColor);
            }
        }
    };

    /**
     * Adds the default focus listener to this CyderButton.
     */
    public void addDefaultFocusListener() {
        addFocusListener(defaultFocusListener);
    }
}