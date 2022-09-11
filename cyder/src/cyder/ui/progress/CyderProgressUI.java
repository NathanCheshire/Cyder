package cyder.ui.progress;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.handlers.internal.Logger;
import cyder.utils.MathUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * A progress bar ui with a color animation.
 */
public class CyderProgressUI extends BasicProgressBarUI {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void installDefaults() {
        super.installDefaults();

        progressBar.setOpaque(false);
        progressBar.setBorder(null);
    }

    /**
     * Constructs a new progress bar ui.
     */
    public CyderProgressUI() {
        startAnimationTimer();

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopAnimationTimer() {
        super.stopAnimationTimer();
    }

    /**
     * The number of frames per second
     */
    private int framesPerSecond = 100;

    /**
     * Sets the frames per second for this progress ui.
     *
     * @param numFrame the frames per second for this progress ui
     */
    public void setFramesPerSecond(int numFrame) {
        framesPerSecond = numFrame;
    }

    /**
     * Returns the frames per second for this progress ui.
     *
     * @return the frames per second for this progress ui
     */
    public int getFramesPerSecond() {
        return framesPerSecond;
    }

    private Color primaryAnimationColor;
    private Color secondaryAnimationColor;

    /**
     * Sets the animation colors of the progress ui.
     *
     * @param primaryColor   the colors for the progress ui
     * @param secondaryColor the second color for the progress ui
     */
    public void setAnimationColors(Color primaryColor, Color secondaryColor) {
        Preconditions.checkNotNull(primaryColor);
        Preconditions.checkNotNull(secondaryColor);

        this.primaryAnimationColor = primaryColor;
        this.secondaryAnimationColor = secondaryColor;
    }

    /**
     * Returns the primary animation color.
     *
     * @return the primary animation color
     */
    public Color getPrimaryAnimationColor() {
        return primaryAnimationColor;
    }

    /**
     * Returns the secondary animation color.
     *
     * @return the secondary animation color
     */
    public Color getSecondaryAnimationColor() {
        return secondaryAnimationColor;
    }

    /**
     * Creates a rippled image between the two provided colors.
     *
     * @param darkColor  the dark color for the image
     * @param lightColor the light color for the image
     * @param width      the width of the image
     * @param height     the height of the image
     * @return a rippled image between the two provided colors
     */
    private static BufferedImage createRippleImageHorizontal(Color darkColor, Color lightColor, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        GradientPaint darkToLight = new GradientPaint(new Point2D.Double(0, 0),
                darkColor, new Point2D.Double(width / 2.0, 0), lightColor);
        GradientPaint lightToDark = new GradientPaint(new Point2D.Double(width / 2.0, 0),
                lightColor, new Point2D.Double(width, 0), darkColor);
        g2.setPaint(darkToLight);
        g2.fillRect(0, 0, width / 2, height);
        g2.setPaint(lightToDark);
        g2.fillRect(width / 2, 0, width / 2, height);

        return image;
    }

    /**
     * Creates a rippled image between the two provided colors.
     *
     * @param darkColor  the dark color for the image
     * @param lightColor the light color for the image
     * @param width      the width of the image
     * @param height     the height of the image
     * @return a rippled image between the two provided colors
     */
    private static BufferedImage createRippleImageVertical(Color darkColor, Color lightColor, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        GradientPaint darkToLight = new GradientPaint(new Point2D.Double(0, 0),
                darkColor, new Point2D.Double(0, height / 2.0), lightColor);
        GradientPaint lightToDark = new GradientPaint(new Point2D.Double(0, height / 2.0),
                lightColor, new Point2D.Double(0, height), darkColor);
        g2.setPaint(darkToLight);
        g2.fillRect(0, 0, width, height / 2);
        g2.setPaint(lightToDark);
        g2.fillRect(0, height / 2, width, height / 2);

        return image;
    }

    private AnimationDirection animationDirection = AnimationDirection.LEFT_TO_RIGHT;

    /**
     * Returns the animation direction.
     *
     * @return the animation direction
     */
    public AnimationDirection getAnimationDirection() {
        return animationDirection;
    }

    /**
     * Sets the animation direction.
     *
     * @param direction the animation direction
     */
    public void setAnimationDirection(AnimationDirection direction) {
        this.animationDirection = direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void incrementAnimationIndex() {
        if (progressBar == null)
            return;

        int val = getAnimationIndex() + 1;
        setAnimationIndex(val < framesPerSecond ? val : 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        c.setBackground(CyderColors.vanilla);

        BufferedImage barImage;
        if (progressBar.getOrientation() == JProgressBar.VERTICAL) {
            barImage = createRippleImageVertical(primaryAnimationColor, secondaryAnimationColor,
                    c.getWidth(), c.getHeight());

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
            if (animationDirection == AnimationDirection.TOP_TO_BOTTOM) {
                offset = (int) (MathUtil.rangeMap(getAnimationIndex(), 0,
                        framesPerSecond, 0, barImage.getHeight()));
            } else {
                offset = (int) (MathUtil.rangeMap(getAnimationIndex(), 0,
                        framesPerSecond, barImage.getHeight(), 0));
            }

            int numRepetitions = (progressBar.getHeight() / barImage.getHeight()) + 2;

            for (int i = 0 ; i < numRepetitions ; i++) {
                g.drawImage(barImage, 0, (i - 1) * barImage.getHeight() + offset, null);
            }
        } else {
            //get the image with the colors of proper width and height
            barImage = createRippleImageHorizontal(primaryAnimationColor, secondaryAnimationColor,
                    c.getWidth() * 2, c.getHeight());

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
            if (animationDirection == AnimationDirection.RIGHT_TO_LEFT) {
                offset = (int) (MathUtil.rangeMap(getAnimationIndex(), 0,
                        framesPerSecond, barImage.getWidth(), 0));
            } else {
                offset = (int) (MathUtil.rangeMap(getAnimationIndex(), 0,
                        framesPerSecond, 0, barImage.getWidth()));
            }

            int numRepetitions = (progressBar.getWidth() / barImage.getWidth()) + 2;

            for (int i = 0 ; i < numRepetitions ; i++) {
                g.drawImage(barImage, (i - 1) * barImage.getWidth() + offset, 0, null);
            }
        }
    }

    /**
     * The direction to animate the colors in.
     */
    public enum AnimationDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        TOP_TO_BOTTOM,
        BOTTOM_TO_TOP
    }
}