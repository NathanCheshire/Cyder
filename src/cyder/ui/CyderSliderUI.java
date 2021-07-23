package cyder.ui;

import cyder.enums.SliderShape;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class CyderSliderUI extends BasicSliderUI {

    private BasicStroke stroke = new BasicStroke(3.0f);
    private BasicStroke thumbStroke = new BasicStroke(3.0f);

    private Color oldValColor;
    private Color newValColor;
    private Color fillColor;
    private Color outlineColor;
    private int thumbDiameter = 10;

    private JSlider slider;

    private SliderShape sliderShape = SliderShape.RECT;

    public void setThumbDiameter(int radius) {
        if (radius <= 0)
            throw new IllegalArgumentException("Thumb radius must be greater than 0");
        this.thumbDiameter = radius;
    }

    public int getThumbDiameter() {
        return this.thumbDiameter;
    }

    public void setSliderShape(SliderShape shape) {
        this.sliderShape = shape;
    }

    private transient boolean upperDragging;

    public void setOldValColor(Color c) {
        this.oldValColor = c;
    }

    public void setNewValColor(Color c) {
        this.newValColor = c;
    }

    public void setFillColor(Color c) {
        this.fillColor = c;
    }

    public void setOutlineColor(Color c) {
        this.outlineColor = c;
    }

    public void setTrackStroke(BasicStroke s) {
        this.stroke = s;
    }

    public void setThumbStroke(BasicStroke s) {
        this.thumbStroke = s;
    }

    public CyderSliderUI(JSlider b) {
        super(b);
        this.slider = b;
    }

    @Override
    protected void calculateThumbSize() {
        super.calculateThumbSize();
        thumbRect.setSize(thumbRect.width, thumbRect.height);
    }

    /**
     * Creates a listener to handle track events in the specified slider.
     */
    @Override
    protected TrackListener createTrackListener(JSlider slider) {
        return new RangeTrackListener();
    }

    @Override
    protected void calculateThumbLocation() {
        // Call superclass method for lower thumb location.
        super.calculateThumbLocation();

        // Adjust upper value to snap to ticks if necessary.
        if (slider.getSnapToTicks()) {
            int upperValue = slider.getValue() + slider.getExtent();
            int snappedValue = upperValue;
            int majorTickSpacing = slider.getMajorTickSpacing();
            int minorTickSpacing = slider.getMinorTickSpacing();
            int tickSpacing = 0;

            if (minorTickSpacing > 0) {
                tickSpacing = minorTickSpacing;
            } else if (majorTickSpacing > 0) {
                tickSpacing = majorTickSpacing;
            }

            if (tickSpacing != 0) {
                // If it's not on a tick, change the value
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

        // Calculate upper thumb location.  The thumb is centered over its
        // value on the track.
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

    @Override
    protected Dimension getThumbSize() {
        return super.getThumbSize();
    }

    private Shape createThumbShape(int width, int height) {
        return new Rectangle2D.Double(0, 0, width, height);
    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke old = g2d.getStroke();
        g2d.setStroke(stroke);
        g2d.setPaint(newValColor);
        Rectangle trackBounds = trackRect;
        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            g2d.drawLine(trackRect.x, trackRect.y + trackRect.height / 2,
                    trackRect.x + trackRect.width, trackRect.y + trackRect.height / 2);
            int lowerX = thumbRect.width / 2;
            int upperX = thumbRect.x + (thumbRect.width / 2);
            int cy = (trackBounds.height / 2) - 2;
            g2d.translate(trackBounds.x, trackBounds.y + cy);
            g2d.setColor(oldValColor);
            //-10 is so that we can't see the line change in the middle if it's hollow
            g2d.drawLine(lowerX - trackBounds.x, 2, upperX - trackBounds.x -
                    (sliderShape == SliderShape.HOLLOW_CIRCLE ? 10 : 0), 2);
            g2d.translate(-trackBounds.x, -(trackBounds.y + cy));
        }
        g2d.setStroke(old);
    }

    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if (sliderShape == SliderShape.CIRCLE) {
            g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle t = thumbRect;
            g2d.setColor(fillColor);
            System.out.println(slider.getValue());
            int x = (int) (trackRect.getX() + trackRect.getWidth() * slider.getValue() / 100.0 - (thumbDiameter / 4));
            int y = (int) (trackRect.getY() + trackRect.getHeight() / 2 - (thumbDiameter / 4));
            g.fillOval(x, y,thumbDiameter / 2, thumbDiameter / 2);
            g2d.dispose();
        } else if (sliderShape == SliderShape.RECT){
            Rectangle knobBounds = thumbRect;
            int w = knobBounds.width;
            int h = knobBounds.height;
            g2d = (Graphics2D) g.create();
            Shape thumbShape = createThumbShape(w - 1, h - 1);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.translate(knobBounds.x, knobBounds.y);
            g2d.setColor(fillColor);
            g2d.fill(thumbShape);

            g2d.setColor(outlineColor);
            g2d.draw(thumbShape);
            g2d.dispose();
        } else if (sliderShape == SliderShape.HOLLOW_CIRCLE) {
            g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(thumbStroke);
            Rectangle t = thumbRect;
            g2d.setColor(fillColor);
            g2d.drawOval(t.x - 5, t.y, 20, 20);
            g2d.dispose();
        } else if (sliderShape == SliderShape.NONE) {
            //no paint
        }
    }

    private BufferedImage customThumb = null;

    public void setCustomThumb(BufferedImage customThumb) {
        this.customThumb = customThumb;
    }

    public class RangeTrackListener extends TrackListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }
            currentMouseX -= thumbRect.width / 2;
            moveUpperThumb();
        }

        public void mousePressed(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (slider.isRequestFocusEnabled()) {
                slider.requestFocus();
            }

            boolean upperPressed = false;

            if (slider.getMinimum() == slider.getValue()) {
                if (thumbRect.contains(currentMouseX, currentMouseY)) {
                    upperPressed = true;
                }
            }

            else {
                if (thumbRect.contains(currentMouseX, currentMouseY)) {
                    upperPressed = true;
                }
            }

            if (upperPressed) {
                switch (slider.getOrientation()) {
                    case JSlider.VERTICAL:
                        offset = currentMouseY - thumbRect.y;
                        break;
                    case JSlider.HORIZONTAL:
                        offset = currentMouseX - thumbRect.x;
                        break;
                }

                upperDragging = true;
                return;
            }

            upperDragging = false;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            upperDragging = false;
            slider.setValueIsAdjusting(false);
            super.mouseReleased(e);
        }

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

        @Override
        public boolean shouldScroll(int direction) {
            return false;
        }

        public void moveUpperThumb() {
            int thumbMiddle;
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int halfThumbWidth = thumbRect.width / 2;
                int thumbLeft = currentMouseX - offset;
                int trackLeft = trackRect.x;
                int trackRight = trackRect.x + (trackRect.width - 1);
                int hMax = xPositionForValue(slider.getMaximum() -
                        slider.getExtent());

                if (drawInverted()) {
                    trackLeft = hMax;
                } else {
                    trackRight = hMax;
                }
                thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                setThumbLocation(thumbLeft, thumbRect.y); //setThumbLocation

                thumbMiddle = thumbLeft + halfThumbWidth;
                slider.setValue(valueForXPosition(thumbMiddle));
            }
        }
    }

    @Override
    public String toString() {
        return "CyderSliderUI object, hash=" + this.hashCode();
    }
}