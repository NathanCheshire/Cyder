package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A moder button for use throughout Cyder, similar to {@link CyderCheckbox}.
 */
public class CyderModernButton extends JLabel {
    private String text;
    private static final String DEFAULT_TEXT = "Modern Button";

    private Font font = CyderFonts.DEFAULT_FONT_SMALL;

    private Color foregroundColor = CyderColors.regularPink;
    private Color backgroundColor = CyderColors.navy;
    private Color borderColor = Color.black;
    private Color hoverColor = backgroundColor.darker();
    private Color pressedColor = hoverColor.darker();
    private Color disabledForeground = Color.black;
    private Color disabledBackground = CyderColors.vanilla;

    private boolean roundedCorners = true;
    private boolean threadsKilled = false;
    private boolean disabled = false;
    private boolean isFlashing;

    /**
     * The radius to use when {@link #roundedCorners} is enabled.
     */
    private static final int CORNER_RADIUS = 20;
    private int borderRadius = 3;

    private int width = 150;
    private int height = 40;

    private JLabel innerTextLabel;

    private final AtomicBoolean mouseInside = new AtomicBoolean();

    public CyderModernButton() {
        this(DEFAULT_TEXT);
    }

    public CyderModernButton(String text) {
        this.text = text;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseInside.set(true);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseInside.set(false);
                repaint();
            }
        });

        installInnerTextLabel();
    }

    /**
     * Sets up and adds the inner text label to this component.
     */
    private void installInnerTextLabel() {
        innerTextLabel = new JLabel();

        refreshInnerTextLabel();

        add(innerTextLabel);
    }

    /**
     * Refreshes the text, bounds, font, foreground, and alignment of the inner text label.
     */
    private void refreshInnerTextLabel() {
        innerTextLabel.setText(text);
        innerTextLabel.setBounds(0, 0, width, height);
        innerTextLabel.setFont(font);
        innerTextLabel.setForeground(foregroundColor);

        refreshTextAlignment();

        innerTextLabel.repaint();
    }

    /**
     * The alignment of the text of the modern button.
     */
    public enum TextAlignment {
        TOP_LEFT, TOP, TOP_RIGHT,
        LEFT, CENTER, RIGHT,
        BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
    }

    /**
     * The text alignment of the button text.
     */
    private TextAlignment textAlignment = TextAlignment.CENTER;

    /**
     * Returns the text alignment of the button text.
     *
     * @return the text alignment of the button text
     */
    public TextAlignment getTextAlignment() {
        return textAlignment;
    }

    /**
     * Sets the text alignment of the button text.
     *
     * @param textAlignment the text alignment of the button text
     */
    public void setTextAlignment(TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
    }

    /**
     * Refreshes the text alignment of the inner text label.
     */
    private void refreshTextAlignment() {
        switch (textAlignment) {
            case TOP_LEFT -> {
                innerTextLabel.setHorizontalAlignment(JLabel.LEFT);
                innerTextLabel.setVerticalAlignment(JLabel.TOP);
            }
            case TOP -> {
                innerTextLabel.setHorizontalAlignment(JLabel.CENTER);
                innerTextLabel.setVerticalAlignment(JLabel.TOP);
            }
            case TOP_RIGHT -> {
                innerTextLabel.setHorizontalAlignment(JLabel.RIGHT);
                innerTextLabel.setVerticalAlignment(JLabel.TOP);
            }
            case LEFT -> {
                innerTextLabel.setHorizontalAlignment(JLabel.LEFT);
                innerTextLabel.setVerticalAlignment(JLabel.CENTER);
            }
            case CENTER -> {
                innerTextLabel.setHorizontalAlignment(JLabel.CENTER);
                innerTextLabel.setVerticalAlignment(JLabel.CENTER);
            }
            case RIGHT -> {
                innerTextLabel.setHorizontalAlignment(JLabel.RIGHT);
                innerTextLabel.setVerticalAlignment(JLabel.CENTER);
            }
            case BOTTOM_LEFT -> {
                innerTextLabel.setHorizontalAlignment(JLabel.LEFT);
                innerTextLabel.setVerticalAlignment(JLabel.BOTTOM);
            }
            case BOTTOM -> {
                innerTextLabel.setHorizontalAlignment(JLabel.CENTER);
                innerTextLabel.setVerticalAlignment(JLabel.BOTTOM);
            }
            case BOTTOM_RIGHT -> {
                innerTextLabel.setHorizontalAlignment(JLabel.RIGHT);
                innerTextLabel.setVerticalAlignment(JLabel.BOTTOM);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        graphics2D.setPaint(borderColor);
        graphics2D.setStroke(new BasicStroke(1.0f));

        if (roundedCorners) {
            graphics2D.fill(new RoundRectangle2D.Double(0, 0, width, height, CORNER_RADIUS, CORNER_RADIUS));
            graphics2D.setPaint(mouseInside.get() ? hoverColor : backgroundColor);
            graphics2D.fill(new RoundRectangle2D.Double(borderRadius, borderRadius,
                    width - 2 * borderRadius, height - 2 * borderRadius, CORNER_RADIUS, CORNER_RADIUS));
        } else {
            graphics2D.fillRect(0, 0, width, height);
            graphics2D.setPaint(mouseInside.get() ? hoverColor : backgroundColor);
            graphics2D.fillRect(borderRadius, borderRadius,
                    width - 2 * borderRadius, height - 2 * borderRadius);
        }


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        this.width = width;
        this.height = height;

        if (innerTextLabel != null) {
            innerTextLabel.setSize(width, height);
            innerTextLabel.repaint();
        }

        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        this.width = width;
        this.height = height;

        if (innerTextLabel != null) {
            innerTextLabel.setSize(width, height);
            innerTextLabel.repaint();
        }

        repaint();
    }

    /**
     * Expands the button's width or height as necessary to fit all of the button text on the button.
     */
    public void expandToMinSize() {
        expandToMinSize(false);
    }

    /**
     * Expands the button's width or height as necessary to fit all of the button text on the button.
     * The center of the button is kept in the same place if maintainRelativeCenter is passed as true.
     *
     * @param maintainRelativeCenter whether to maintain the center of the button
     */
    public void expandToMinSize(boolean maintainRelativeCenter) {
        int necessaryWidth = StringUtil.getMinWidth(text, font);
        int necessaryHeight = StringUtil.getMinHeight(text, font);

        int newWidth = Math.max(width, necessaryWidth);
        int newHeight = Math.max(height, necessaryHeight);

        if (newWidth != width || newHeight != height) {
            if (maintainRelativeCenter) {
                Point center = getCenter();
                setBounds((int) (center.getX() - newWidth / 2), (int) (center.getY() - newHeight / 2),
                        newWidth, newHeight);
            } else {
                setSize(newWidth, newHeight);
            }
        }
    }

    /**
     * Sets the center of the button on the parent component to the specified point.
     *
     * @param point the point to set as the button's center
     */
    public void setCenter(Point point) {
        setCenter(point.x, point.y);
    }

    /**
     * Sets the center of the button on the parent component to the specified point.
     *
     * @param x the x value of the point
     * @param y the y value of the point
     */
    public void setCenter(int x, int y) {
        setLocation(x - this.getWidth() / 2, y - this.getHeight() / 2);
    }

    /**
     * Returns the center point of the button on the parent component.
     *
     * @return the center point of the button on the parent component
     */
    public Point getCenter() {
        return new Point(this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2);
    }

    /**
     * The default delay between alert iterations.
     */
    private static final int DEFAULT_ALERT_DELAY = 250;

    /**
     * The default number of alert iterations.
     */
    private static final int DEFAULT_ALERT_ITERATIONS = 10;

    /**
     * Alerts the button for {@link #DEFAULT_ALERT_ITERATIONS} iterations.
     */
    public void alert() {
        alert(DEFAULT_ALERT_ITERATIONS);
    }

    /**
     * Alerts the button for the provided number of iterations.
     *
     * @param iterations the number of iterations to alert for
     */
    public void alert(int iterations) {
        String buttonName = "Modern Button, hash = " + hashCode();

        Color startingColor = backgroundColor;
        Color endingColor = backgroundColor.darker();

        CyderThreadRunner.submit(() -> {
            try {
                for (int i = 0 ; i < iterations ; i++) {
                    // todo
                    repaint();
                    ThreadUtil.sleep(DEFAULT_ALERT_DELAY);
                    // todo
                    repaint();
                    ThreadUtil.sleep(DEFAULT_ALERT_DELAY);
                }

                // todo
                repaint();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, buttonName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = text.hashCode();
        ret += 31 * foregroundColor.hashCode();
        ret += 31 * backgroundColor.hashCode();
        ret += 31 * hoverColor.hashCode();
        ret += 31 * pressedColor.hashCode();
        ret += 31 * borderColor.hashCode();
        ret += 31 * Integer.hashCode(width);
        ret += 31 * Integer.hashCode(height);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Cyder Modern Button {" + this.getX() + ", " + this.getY()
                + ", " + width + "x" + height + "}, text=\"" + text + "\"";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof CyderModernButton)) {
            return false;
        }

        CyderModernButton other = (CyderModernButton) o;
        // todo compare all things used in hashcode
        return false;
    }
}
