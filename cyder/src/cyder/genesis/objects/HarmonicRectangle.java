package cyder.genesis.objects;

import cyder.constants.CyderColors;

import javax.swing.*;
import java.awt.*;

public class HarmonicRectangle extends JLabel {
    private int staticWidth;
    private int staticHeight;

    /**
     * The amount to increase or decrease the animation direction by.
     */
    private int animateDelta;
    private int currentWidth;
    private int currentHeight;

    private int delay;

    private Color backgroundColor = CyderColors.vanila;

    private AlternationDirection alternationDirection = AlternationDirection.VERTICAL;

    public enum AlternationDirection {
        VERTICAL, HORIZONTAL
    }

    public HarmonicRectangle(int width, int height) {
        this.staticWidth = width;
        this.staticHeight = height;
    }

    @Override
    public int getWidth() {
        return staticWidth;
    }

    public void setStaticWidth(int staticWidth) {
        this.staticWidth = staticWidth;
    }

    @Override
    public int getHeight() {
        return staticHeight;
    }

    public void setStaticHeight(int staticHeight) {
        this.staticHeight = staticHeight;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public AlternationDirection getAlternationDirection() {
        return alternationDirection;
    }

    public void setAlternationDirection(AlternationDirection alternationDirection) {
        this.alternationDirection = alternationDirection;
    }

    public int getAnimateDelta() {
        return animateDelta;
    }

    public void setAnimateDelta(int animateDelta) {
        this.animateDelta = animateDelta;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, currentWidth, currentHeight);
    }
}
