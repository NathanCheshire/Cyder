package cyder.ui;

import cyder.constants.CyderColors;
import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.ui.enums.AnimationDirection;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class CyderProgressUI extends BasicProgressBarUI {
    @Override
    protected void installDefaults() {
        super.installDefaults();

        progressBar.setOpaque(false);
        progressBar.setBorder(null);
    }

    /**
     * Constructor that starts the animation timer to allow the progressbar to move the animation colors
     */
    public CyderProgressUI() {
        startAnimationTimer();

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    public void stopAnimationTimer() {
        super.stopAnimationTimer();
    }

    //frames for animation, more frames means slower animation

    private int numFrames = 100;

    public void setNumFrames(int numFrame) {
        numFrames = numFrame;
    }

    public int getNumFrames() {
        return numFrames;
    }

    //two colors for the buffered image used for animation

    private Color[] colors = {CyderColors.selectionColor, CyderColors.vanila};

    public void setColors(Color[] color) {
        if (color.length != 2)
            return;

        colors = color;
    }

    private Color[] getColors() {
        return colors;
    }

    //the image used for the animation

    public static BufferedImage createRippleImageHorizontal(Color darkColor, Color lightColor, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        GradientPaint darkToLight = new GradientPaint(new Point2D.Double(0,0),
                darkColor, new Point2D.Double(width / 2.0, 0), lightColor);
        GradientPaint lightToDark = new GradientPaint(new Point2D.Double(width / 2.0,0),
                lightColor, new Point2D.Double(width, 0), darkColor);
        g2.setPaint(darkToLight);
        g2.fillRect(0, 0, width/2, height);
        g2.setPaint(lightToDark);
        g2.fillRect(width/2, 0, width/2, height);

        return image;
    }

    public static BufferedImage createRippleImageVertical(Color darkColor, Color lightColor, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        GradientPaint darkToLight = new GradientPaint(new Point2D.Double(0,0),
                darkColor, new Point2D.Double(0, height / 2.0), lightColor);
        GradientPaint lightToDark = new GradientPaint(new Point2D.Double(0,height / 2.0),
                lightColor, new Point2D.Double(0, height), darkColor);
        g2.setPaint(darkToLight);
        g2.fillRect(0, 0, width, height/2);
        g2.setPaint(lightToDark);
        g2.fillRect(0, height/2, width, height/2);

        return image;
    }

    //direction of animation, L2R or R2L for horiz and T2B or B2T for vert

    private AnimationDirection direction = AnimationDirection.LEFT_TO_RIGHT;

    public AnimationDirection getAnimationDirection() {
        return direction;
    }

    public void setAnimationDirection(AnimationDirection direction) {
        this.direction = direction;
    }

    @Override
    protected void incrementAnimationIndex() {
        if (progressBar == null)
            return;

        int val = getAnimationIndex() + 1;
        setAnimationIndex(val < numFrames ? val : 0);
    }

    //UI Shape, currently only SQUARE works, rounded still being implemented
    public enum Shape {
        SQUARE, ROUNDED
    }

    private Shape shape = Shape.SQUARE;

    public void setShape(Shape s) {
        shape = s;
    }

    public Shape getShape() {
        return shape;
    }

    //NOTE: animation direction is simply the direction the bar animation moves,
    // not what direction the bar moves (top to bottom vs bottom to top or left to right vs right to left)
    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        //square uses the custom animation
        if (shape == Shape.SQUARE) {
            c.setBackground(CyderColors.vanila);

            BufferedImage barImage;
            if (progressBar.getOrientation() == JProgressBar.VERTICAL) {
                barImage = createRippleImageVertical(colors[0], colors[1], c.getWidth(), c.getHeight());

                //get proper width and height accounting for insets as well
                Insets b = progressBar.getInsets();
                int barRectWidth = progressBar.getWidth() - (b.right + b.left);
                int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

                //if it's invisible then why paint?
                if (barRectWidth <= 0 || barRectHeight <= 0)
                    return;

                //the amount the progress bar should be filled by with the proper parameters passed
                int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

                //set the clip at the point that it shouldn't extend past
                g.setClip(b.left, b.top, barRectWidth, amountFull);

                int offset;
                //image drawing offset for completed percent
                if (direction == AnimationDirection.TOP_TO_BOTTOM) {
                    offset = (int) (rangeMap(getAnimationIndex(), 0, numFrames, 0, barImage.getHeight()));
                } else {
                    offset = (int) (rangeMap(getAnimationIndex(), 0, numFrames, barImage.getHeight(), 0));
                }

                int numRepetitions = (progressBar.getHeight() / barImage.getHeight()) + 2;

                for (int i = 0; i < numRepetitions; i++) {
                    g.drawImage(barImage, 0, (i - 1) * barImage.getHeight() + offset, null);
                }
            } else {
                //get the image with the colors of proper width and height
                barImage = createRippleImageHorizontal(colors[0], colors[1], c.getWidth() * 2, c.getHeight());

                //get proper width and height accounting for insets as well
                Insets b = progressBar.getInsets();
                int barRectWidth = progressBar.getWidth() - (b.right + b.left);
                int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

                //if it's invisible then why paint?
                if (barRectWidth <= 0 || barRectHeight <= 0)
                    return;

                //the amount the progress bar should be filled by with the proper parameters passed
                int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

                //set the clip at the point that it shouldn't extend past
                g.setClip(b.left, b.top, amountFull, barRectHeight);

                int offset;
                //right to left otherwise left to right, offset for progress image drawing
                if (direction == AnimationDirection.RIGHT_TO_LEFT) {
                    offset = (int) (rangeMap(getAnimationIndex(), 0, numFrames, barImage.getWidth(), 0));
                } else {
                    offset = (int) (rangeMap(getAnimationIndex(), 0, numFrames, 0, barImage.getWidth()));
                }

                int numRepetitions = (progressBar.getWidth() / barImage.getWidth()) + 2;

                for (int i = 0; i < numRepetitions; i++) {
                    g.drawImage(barImage, (i - 1) * barImage.getWidth() + offset, 0, null);
                }
            }
        } else {
            if (progressBar.getOrientation() == JProgressBar.VERTICAL) {
                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                //fill
                int oStrokeHeight = 3;
                g2d.setStroke(new BasicStroke(oStrokeHeight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(colors[0]);

                int outerWidth = c.getWidth();
                int outerHeight = c.getHeight();

                double progressPercent = progressBar.getValue() / (double) progressBar.getMaximum();
                double prog = (outerHeight - oStrokeHeight) * progressPercent;
                int fullH = (outerHeight - oStrokeHeight);
                int drawFill = (int) Math.min(fullH, prog);

                RoundRectangle2D fill = new RoundRectangle2D.Double(oStrokeHeight / 2.0, oStrokeHeight / 2.0,
                         outerWidth - oStrokeHeight, drawFill, outerWidth, outerWidth);

                g2d.fill(fill);

                //outline over fill
                int iStrokeHeight = 3;
                g2d.setStroke(new BasicStroke(iStrokeHeight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(CyderColors.navy);
                g2d.setBackground(CyderColors.navy);

                int width = c.getWidth();
                int height = c.getHeight();

                RoundRectangle2D outline = new RoundRectangle2D.Double(iStrokeHeight / 2.0, iStrokeHeight / 2.0,
                        width - iStrokeHeight, height - iStrokeHeight, width, width);

                g2d.draw(outline);

                //clip so nothing out of outer oval
                g2d.setClip(outline);

                g2d.dispose();
            } else {
                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                //fill
                int oStrokeWidth = 3;
                g2d.setStroke(new BasicStroke(oStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(CyderColors.regularPink);

                int outerWidth = c.getWidth();
                int outerHeight = c.getHeight();

                double progressPercent = progressBar.getValue() / (double) progressBar.getMaximum();
                double prog = (outerWidth - oStrokeWidth) * progressPercent;
                int fullW = (outerWidth - oStrokeWidth);
                int drawFill = (int) Math.min(fullW, prog);

                RoundRectangle2D fill = new RoundRectangle2D.Double(oStrokeWidth / 2.0, oStrokeWidth / 2.0,
                        drawFill, outerHeight - oStrokeWidth, outerHeight, outerHeight);

                g2d.fill(fill);

                //outline over fill
                int iStrokWidth = 3;
                g2d.setStroke(new BasicStroke(iStrokWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(CyderColors.navy);
                g2d.setBackground(CyderColors.navy);

                int width = c.getWidth();
                int height = c.getHeight();

                RoundRectangle2D outline = new RoundRectangle2D.Double(iStrokWidth / 2.0, iStrokWidth / 2.0,
                        width - iStrokWidth, height - iStrokWidth, height, height);

                g2d.draw(outline);

                //clip so nothing out of outer oval
                g2d.setClip(outline);

                g2d.dispose();
            }
        }
    }

    /**
     * Maps the provided value in the original range to the second range.
     *
     * @param value the value to map
     * @param low1 the min value of the orignal range
     * @param high1 the max value of the original range
     * @param low2 the min value of the new range
     * @param high2 the max value of the new range
     * @return the mapped value
     */
    private static double rangeMap(double value, double low1, double high1, double low2, double high2) {
        return linearInterpolate(low2, high2, (value - low1) / (high1 - low1));
    }

    /**
     * Linearly interpolates between val1 and val2 where amt is the amount to interpolate between the two values.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @param amt the alpha value
     * @return the linear interpolation
     */
    private static double linearInterpolate(double value1, double value2, double amt) {
        return ((value2 - value1) * amt) + value1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}