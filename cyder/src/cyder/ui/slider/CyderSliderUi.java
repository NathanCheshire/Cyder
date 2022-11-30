package cyder.ui.slider;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicInteger;

/** A ui layer for {@link JSlider}s utilized by Cyder. */
public class CyderSliderUi extends BasicSliderUI {
    /** The default stroke width for the slider and thumb strokes. */
    public static final float DEFAULT_STROKE_WIDTH = 3.0f;

    /** The stroke for painting the slider track. */
    private BasicStroke sliderStroke = new BasicStroke(DEFAULT_STROKE_WIDTH);

    /** The stroke for painting the thumb. */
    private BasicStroke thumbStroke = new BasicStroke(DEFAULT_STROKE_WIDTH);

    /** The track color left of the thumb. */
    private Color leftThumbColor;

    /** The track color right of the thumb. */
    private Color rightThumbColor;

    /** The color to fill the thumb with. */
    private Color thumbFillColor;

    /** The outline color for the thumb. */
    private Color thumbOutlineColor;

    /** The thumb radius when the slider shape is an ellipse. */
    private int thumbRadius = 10;

    /** The slider this ui manager is controlling. */
    private final JSlider slider;

    /** The shape of the thumb. */
    private ThumbShape thumbShape = ThumbShape.RECTANGLE;

    /**
     * Sets the radius of the thumb.
     *
     * @param radius the radius of the thumb
     */
    public void setThumbRadius(int radius) {
        Preconditions.checkArgument(radius > 0);
        thumbRadius = radius;
    }

    /**
     * Returns the radius of the thumb.
     *
     * @return the radius of the thumb
     */
    public int getThumbRadius() {
        return thumbRadius;
    }

    /**
     * Sets the shape of the slider thumb.
     *
     * @param shape the shape of the slider thumb
     */
    public void setThumbShape(ThumbShape shape) {
        thumbShape = shape;
    }

    /**
     * Sets the track color to the left of the thumb
     *
     * @param color the track color to the left of the thumb
     */
    public void setLeftThumbColor(Color color) {
        leftThumbColor = color;
    }

    /**
     * Sets the track color to the right of the thumb.
     *
     * @param color the track color to the right of the thumb
     */
    public void setRightThumbColor(Color color) {
        rightThumbColor = color;
    }

    /**
     * Sets the fill color of the thumb.
     *
     * @param color the fill color of the thumb
     */
    public void setThumbFillColor(Color color) {
        thumbFillColor = color;
    }

    /**
     * Sets the outline color of the thumb.
     *
     * @param color the outline color of the thumb
     */
    public void setThumbOutlineColor(Color color) {
        thumbOutlineColor = color;
    }

    /**
     * Sets the stroke of the track.
     *
     * @param stroke the stroke of the track
     */
    public void setTrackStroke(BasicStroke stroke) {
        sliderStroke = stroke;
    }

    /**
     * Sets the stroke of the thumb.
     *
     * @param stroke the stroke of the thumb
     */
    public void setThumbStroke(BasicStroke stroke) {
        thumbStroke = stroke;
    }

    /**
     * Creates a new CyderSliderUi object.
     *
     * @param slider the slider this ui is controlling and styling
     */
    public CyderSliderUi(JSlider slider) {
        super(slider);
        this.slider = slider;

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /** {@inheritDoc} */
    @Override
    protected void calculateThumbSize() {
        super.calculateThumbSize();
        thumbRect.setSize(thumbRect.width, thumbRect.height);
    }

    /** {@inheritDoc} */
    @Override
    protected BasicSliderUI.TrackListener createTrackListener(JSlider slider) {
        return new RangeTrackListener();
    }

    /** {@inheritDoc} */
    @Override
    protected void calculateThumbLocation() {
        super.calculateThumbLocation();

        // snap to ticks
        if (slider.getSnapToTicks()) {
            int upperValue = slider.getValue() + slider.getExtent();
            int majorTickSpacing = slider.getMajorTickSpacing();
            int minorTickSpacing = slider.getMinorTickSpacing();
            int tickSpacing = 0;

            if (minorTickSpacing > 0) {
                tickSpacing = minorTickSpacing;
            } else if (majorTickSpacing > 0) {
                tickSpacing = majorTickSpacing;
            }

            int snappedValue = upperValue;

            if (tickSpacing != 0) {
                if ((upperValue - slider.getMinimum()) % tickSpacing != 0) {
                    float temp = (float) (upperValue - slider.getMinimum()) / (float) tickSpacing;
                    int whichTick = Math.round(temp);
                    snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
                }

                if (snappedValue != upperValue) {
                    slider.setExtent(snappedValue - slider.getValue());
                }
            }
        }

        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int upperPosition = xPositionForValue(slider.getValue() + slider.getExtent());
            thumbRect.x = upperPosition - (thumbRect.width / 2);
            thumbRect.y = trackRect.y;

        } else {
            int upperPosition = yPositionForValue(slider.getValue() + slider.getExtent());
            thumbRect.x = trackRect.x;
            thumbRect.y = upperPosition - (thumbRect.height / 2);
        }

        slider.repaint();
    }

    /** {@inheritDoc} */
    @Override
    protected Dimension getThumbSize() {
        return super.getThumbSize();
    }

    /**
     * Creates a rectangular thumb shape with the provided dimensions.
     *
     * @param width  the width of the thumb
     * @param height the height of the thumb
     * @return a rectangular thumb shape with the provided dimensions
     */
    private Shape createRectangularThumbShape(int width, int height) {
        return new Rectangle2D.Double(0, 0, width, height);
    }

    /** The atomic holder for the current x value start of the animated color line to be drawn if enabled. */
    private final AtomicInteger animationStart = new AtomicInteger(Integer.MIN_VALUE);

    /** The length of the animation bar to draw if enabled. */
    private int animationLen = 0;

    /** Whether the animation bar should be drawn and animated. */
    private boolean animationEnabled;

    /**
     * Returns the length of the animation bar.
     *
     * @return the length of the animation bar
     */
    public int getAnimationLen() {
        return animationLen;
    }

    /**
     * Sets the length of the animation bar.
     *
     * @param animationLen the length of the animation bar
     */
    public void setAnimationLen(int animationLen) {
        this.animationLen = animationLen;
    }

    /**
     * Returns whether the animation is enabled.
     *
     * @return whether the animation is enabled
     */
    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    /**
     * Sets whether the animation is enabled.
     *
     * @param animationEnabled whether the animation is enabled
     */
    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    /** The color used for the animation segment. */
    private Color animationColor = CyderColors.regularPink;

    /**
     * Returns the color used for the animation if enabled.
     *
     * @return the color used for the animation if enabled
     */
    public Color getAnimationColor() {
        return animationColor;
    }

    /**
     * Sets the color used for the animation if enabled.
     *
     * @param animationColor the color used for the animation if enabled
     */
    public void setAnimationColor(Color animationColor) {
        this.animationColor = animationColor;
    }

    /** {@inheritDoc} */
    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setStroke(sliderStroke);
        g2d.setPaint(rightThumbColor);

        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            if (animationStart.get() == Integer.MIN_VALUE) {
                animationStart.set(trackRect.x - animationLen);
            }

            int leftX = trackRect.x;
            int rightX = trackRect.x + trackRect.width;
            int centeringY = trackRect.y + trackRect.height / 2;

            // Fill whole track with old color first
            g2d.setColor(rightThumbColor);
            g2d.drawLine(leftX, centeringY, rightX, centeringY);

            // Fill left with left track color
            g2d.setColor(leftThumbColor);
            g2d.drawLine(leftX, centeringY, getThumbCenterX(), centeringY);

            // Draw animation if enabled and possible
            if (animationEnabled) {
                int animationStartX = animationStart.get();
                int animationEndX = Math.min(getThumbCenterX(), animationStartX + animationLen);
                int leftToThumbLen = (int) (trackRect.width * ((float) slider.getValue() / slider.getMaximum()));

                // If the animation has enough room
                if (animationLen <= leftToThumbLen) {
                    g2d.setColor(animationColor);
                    g2d.drawLine(animationStartX, centeringY, animationEndX, centeringY);
                }
            }
        }
    }

    /**
     * Returns the relative x value of the thumb's center x value.
     *
     * @return the relative x value of the thumb's center x value
     */
    public int getThumbCenterX() {
        return thumbRect.x + (thumbRect.width / 2) - trackRect.x - (thumbShape == ThumbShape.HOLLOW_CIRCLE ? 10 : 0);
    }

    /**
     * Increments the animation start value. If the value exceeds the bounds of the left color,
     * the value is wrapped around to the starting value.
     */
    public void incrementAnimation() {
        Preconditions.checkArgument(animationEnabled);

        if (thumbRect == null) {
            return;
        }

        if (animationStart.get() == Integer.MIN_VALUE) {
            animationStart.set(trackRect.x - animationLen);
        }

        if (animationStart.get() + 1 > getThumbCenterX()) {
            animationStart.set(trackRect.x - animationLen);
        } else {
            animationStart.getAndIncrement();
        }
    }

    /** Resets the animation start value to the reset value. */
    public void resetAnimation() {
        animationStart.set(trackRect.x - animationLen);
    }

    /**
     * Returns the location of the center of the thumb relative to the slider's bounds.
     *
     * @return the location of the center of the thumb relative to the slider's bounds
     */
    public Point getThumbCenter() {
        return new Point((int) (trackRect.getX() + trackRect.getWidth()
                * ((float) slider.getValue() / slider.getMaximum())),
                (int) (trackRect.getY() + trackRect.getHeight()
                        * ((float) slider.getValue() / slider.getMaximum())));
    }

    /** {@inheritDoc} */
    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d;

        switch (thumbShape) {
            case CIRCLE -> {
                g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(thumbFillColor);
                int x = (int) (trackRect.getX() + trackRect.getWidth() * slider.getValue() / slider.getMaximum() -
                        (thumbRadius / 4));
                int y = (int) (trackRect.getY() + trackRect.getHeight() / 2 - (thumbRadius / 4));
                g.fillOval(x, y, thumbRadius / 2, thumbRadius / 2);
                g2d.dispose();
            }
            case RECTANGLE -> {
                Rectangle knobBounds = thumbRect;
                int w = knobBounds.width;
                int h = knobBounds.height;
                g2d = (Graphics2D) g.create();
                Shape thumbShape = createRectangularThumbShape(w - 1, h - 1);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.translate(knobBounds.x, knobBounds.y);
                g2d.setColor(thumbFillColor);
                g2d.fill(thumbShape);

                g2d.setColor(thumbOutlineColor);
                g2d.draw(thumbShape);
                g2d.dispose();
            }
            case HOLLOW_CIRCLE -> {
                g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setStroke(thumbStroke);
                Rectangle t = thumbRect;
                g2d.setColor(thumbFillColor);
                g2d.drawOval(t.x - 5, t.y, 20, 20);
                g2d.dispose();
            }
            case NONE -> {}
            default -> throw new IllegalArgumentException("Invalid slider shape: " + thumbShape);
        }
    }

    /** Whether the slider is currently under a drag event. */
    private boolean upperDragging;

    /** A custom range track listener. */
    private class RangeTrackListener extends BasicSliderUI.TrackListener {
        /** {@inheritDoc} */
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }
            currentMouseX -= thumbRect.width / 2;
            moveUpperThumb();
        }

        /** {@inheritDoc} */
        @Override
        public void mousePressed(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (slider.isRequestFocusEnabled()) {
                slider.requestFocus();
            }

            if (thumbRect.contains(currentMouseX, currentMouseY)) {
                switch (slider.getOrientation()) {
                    case JSlider.VERTICAL -> offset = currentMouseY - thumbRect.y;
                    case JSlider.HORIZONTAL -> offset = currentMouseX - thumbRect.x;
                }

                upperDragging = true;
                return;
            }

            upperDragging = false;
        }

        /** {@inheritDoc} */
        @Override
        public void mouseReleased(MouseEvent e) {
            upperDragging = false;
            slider.setValueIsAdjusting(false);
            super.mouseReleased(e);
        }

        /** {@inheritDoc} */
        @Override
        public void mouseDragged(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (upperDragging) {
                slider.setValueIsAdjusting(true);
                moveUpperThumb();

            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean shouldScroll(int direction) {
            return false;
        }

        /** Moves the painted thumb to the necessary location based on the current slider value. */
        public void moveUpperThumb() {
            int thumbMiddle;

            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int thumbLeft = currentMouseX - offset;
                int trackLeft = trackRect.x;
                int trackRight = trackRect.x + (trackRect.width - 1);
                int hMax = xPositionForValue(slider.getMaximum() - slider.getExtent());

                if (drawInverted()) {
                    trackLeft = hMax;
                } else {
                    trackRight = hMax;
                }

                thumbLeft = Math.max(thumbLeft, trackLeft - thumbRect.width / 2);
                thumbLeft = Math.min(thumbLeft, trackRight - thumbRect.width / 2);

                setThumbLocation(thumbLeft, thumbRect.y);

                thumbMiddle = thumbLeft + (thumbRect.width / 2);

                slider.setValue(valueForXPosition(thumbMiddle));
            }
        }
    }
}