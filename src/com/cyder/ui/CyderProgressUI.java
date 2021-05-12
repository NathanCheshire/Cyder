package com.cyder.ui;

import com.cyder.constants.CyderColors;
import com.cyder.enums.AnimationDirection;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;


//I adapted code from this guy on stackoverflow for this program and removed a bunch of comments
/*@author https://github.com/I82Much*/
public class CyderProgressUI extends BasicProgressBarUI {

    private int numFrames = 100;

    public void setNumFrames(int numFrame) {
        numFrames = numFrame;
    }

    public int getNumFrames() {
        return this.numFrames;
    }

    private Color[] colors = {CyderColors.selectionColor, CyderColors.vanila};

    private void setColors(Color[] color) {
        if (color.length != 2)
            return;

        colors = color;
    }

    private Color[] getColors() {
        return this.colors;
    }

    private BufferedImage barImage = createRippleImage(colors[0],colors[1],100,100);

    public CyderProgressUI() {
        startAnimationTimer();
    }

    protected BufferedImage createRippleImage(Color darkColor, Color lightColor, int width, int height) {

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        GradientPaint darkToLight = new GradientPaint(new Point2D.Double(0,0), darkColor, new Point2D.Double(width/2, 0), lightColor);
        GradientPaint lightToDark = new GradientPaint(new Point2D.Double(width/2,0), lightColor, new Point2D.Double(width, 0), darkColor);
        g2.setPaint(darkToLight);
        g2.fillRect(0, 0, width/2, height);
        g2.setPaint(lightToDark);
        g2.fillRect(width/2, 0, width/2, height);
        return image;
    }

    private AnimationDirection direction = AnimationDirection.LEFT_TO_RIGHT;

    public AnimationDirection getDirection() {
        return direction;
    }

    public void setDirection(AnimationDirection direction) {
        this.direction = direction;
    }

    @Override
    protected void incrementAnimationIndex() {
        int newValue = getAnimationIndex() + 1;

        if (newValue < numFrames) {
            setAnimationIndex(newValue);
        } else {
            setAnimationIndex(0);
        }
    }

    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        if (progressBar.getOrientation() != JProgressBar.HORIZONTAL) {
            super.paintDeterminate(g, c);
            return;
        }

        barImage = createRippleImage(colors[0],colors[1],c.getWidth(),c.getHeight());

        Insets b = progressBar.getInsets();
        int barRectWidth = progressBar.getWidth() - (b.right + b.left);
        int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0)
            return;

        int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

        g.setClip(b.left, b.top, amountFull, barRectHeight);

        int offset = 0;

        if (direction == AnimationDirection.RIGHT_TO_LEFT)
            offset = (int) (rangeMap(getAnimationIndex(), 0, numFrames, barImage.getWidth(), 0));

        else
            offset = (int) (rangeMap(getAnimationIndex(), 0, numFrames, 0, barImage.getWidth()));

        int numRepetitions = progressBar.getWidth() / barImage.getWidth();

        numRepetitions += 2;

        for (int i = 0; i < numRepetitions; i++)
            g.drawImage(barImage, (i - 1) * barImage.getWidth() + offset, 0, null);
    }

    public static double rangeMap(double value, double low1, double high1, double low2, double high2) {
        return linearInterpolate(low2, high2, (value - low1) / (high1 - low1));
    }

    public static double linearInterpolate(double value1, double value2, double amt) {
        return ((value2 - value1) * amt) + value1;
    }
}