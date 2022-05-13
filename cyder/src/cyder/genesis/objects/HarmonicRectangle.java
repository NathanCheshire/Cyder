package cyder.genesis.objects;

import cyder.constants.CyderColors;

import javax.swing.*;
import java.awt.*;

public class HarmonicRectangle extends JLabel {
    private int width;
    private int height;

    /**
     * The amount to increase or decrease the animation direction by.
     */
    private int animateDelta;

    private int delay;

    private Color backgroundColor = CyderColors.vanila;

    private AlternationDirection alternationDirection = AlternationDirection.VERTICAL;

    public enum AlternationDirection {
        VERTICAL, HORIZONTAL
    }

    public HarmonicRectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
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

    }
}
