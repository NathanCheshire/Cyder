package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A moder button for use throughout Cyder, similar to {@link CyderCheckbox}.
 */
public class CyderModernButton extends JLabel {
    /**
     * The radius to use when {@link #roundedCorners} is enabled.
     */
    private static final int CORNER_RADIUS = 20;

    /**
     * The default text for the modern button.
     */
    private static final String DEFAULT_TEXT = "Modern Button";

    /**
     * The text this modern button holds.
     */
    private String text;

    /**
     * The font of the modern button text label.
     */
    private Font font = CyderFonts.DEFAULT_FONT_SMALL;

    /**
     * The foreground color of the label text.
     */
    private Color foregroundColor = CyderColors.regularPink;

    /**
     * The background color of the button.
     */
    private Color backgroundColor = CyderColors.navy;

    /**
     * The border color.
     */
    private Color borderColor = Color.black;

    /**
     * The color used for hover events.
     */
    private Color hoverColor = backgroundColor.darker();

    /**
     * The color used while the button is pressed.
     */
    private Color pressedColor = hoverColor.darker();

    /**
     * The foreground text color for when the button is disabled.
     */
    private Color disabledForeground = Color.black;

    /**
     * The background color for the button when disabled.
     */
    private Color disabledBackground = CyderColors.vanilla;

    /**
     * Whether the button should be painted with rounded corners.
     */
    private boolean roundedCorners = true;

    /**
     * Whether this button is disabled.
     */
    private boolean disabled = false;

    /**
     * Whether the button is currently flashing. This is invoked by {@link #alert()}.
     */
    private boolean isFlashing;

    /**
     * The length of the border painted around the button.
     */
    private int borderLength = 3;

    /**
     * The width of the button.
     */
    private int width;

    /**
     * The height of the button.
     */
    private int height;

    /**
     * The label that holds the button text.
     */
    private JLabel innerTextLabel;

    /**
     * Whether the mouse is currently inside of the modern button.
     */
    private final AtomicBoolean mouseInside = new AtomicBoolean();

    /**
     * Constructs a new modern button.
     */
    public CyderModernButton() {
        this(DEFAULT_TEXT);
    }

    /**
     * Constructs a new modern button.
     *
     * @param text the button text
     */
    public CyderModernButton(String text) {
        this(text, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    /**
     * Constructs a new modern button.
     * Note: do not use this construct to force the width and height
     * to be calculated by this class.
     *
     * @param text   the text of the button
     * @param width  the width of the button
     * @param height the height of the button
     */
    public CyderModernButton(String text, int width, int height) {
        Preconditions.checkNotNull(text);

        this.text = text;

        if (width == Integer.MIN_VALUE) {
            width = StringUtil.getMinWidth(text, font);
        }

        if (height == Integer.MIN_VALUE) {
            height = StringUtil.getMinHeight(text, font);
        }

        this.width = width;
        this.height = height;

        addListeners();
        installInnerTextLabel();
    }

    /**
     * Adds the necessary listeners to the modern button.
     */
    private void addListeners() {
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

            @Override
            public void mouseClicked(MouseEvent e) {
                invokeRunnables();
            }
        });
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
        innerTextLabel.setForeground(disabled ? disabledForeground : foregroundColor);

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
     * The runnables to invoke when the button is clicked.
     */
    private final LinkedList<Runnable> clickRunnables = new LinkedList<>();

    /**
     * Adds the runnable to the list of runnables to invoke when the button is clicked.
     *
     * @param runnable the runnable to invoke when the button is clicked
     */
    public void addClickRunnable(Runnable runnable) {
        Preconditions.checkNotNull(runnable);

        clickRunnables.add(runnable);
    }

    /**
     * Removes the runnable from the list of runnables to invoke when a button is clicked.
     *
     * @param runnable the runnable to remove from the click runnables
     */
    public void removeClickRunnable(Runnable runnable) {
        Preconditions.checkNotNull(runnable);

        clickRunnables.remove(runnable);
    }

    /**
     * Invokes all runnables in {@link #clickRunnables}.
     */
    private void invokeRunnables() {
        for (Runnable runnable : clickRunnables) {
            runnable.run();
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
            graphics2D.setPaint(disabled ? disabledBackground : (mouseInside.get() ? hoverColor : backgroundColor));
            graphics2D.fill(new RoundRectangle2D.Double(borderLength, borderLength,
                    width - 2 * borderLength, height - 2 * borderLength, CORNER_RADIUS, CORNER_RADIUS));
        } else {
            graphics2D.fillRect(0, 0, width, height);
            graphics2D.setPaint(disabled ? disabledBackground : (mouseInside.get() ? hoverColor : backgroundColor));
            graphics2D.fillRect(borderLength, borderLength,
                    width - 2 * borderLength, height - 2 * borderLength);
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
     * Sets the width of the button.
     *
     * @param width the width of the button
     */
    public void setWidth(int width) {
        this.width = width;

        if (innerTextLabel != null) {
            innerTextLabel.setSize(width, height);
            innerTextLabel.repaint();
        }

        repaint();
    }

    /**
     * Sets the height of the button.
     *
     * @param height the height of the button
     */
    public void setHeight(int height) {
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
     * Sets the text of this modern button.
     *
     * @param text the text of this modern button
     */
    public void setText(String text) {
        Preconditions.checkNotNull(text);

        this.text = text;
        refreshInnerTextLabel();
    }

    /**
     * Returns the text this modern button holds.
     *
     * @return the text this modern button holds
     */
    public String getText() {
        return this.text;
    }

    /**
     * Returns the font of this modern button.
     *
     * @return the font of this modern button
     */
    @Override
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font of this modern button.
     *
     * @param font the font of this modern button
     */
    @Override
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Returns the length of the button border.
     *
     * @return the length of the button border
     */
    public int getBorderLength() {
        return borderLength;
    }

    /**
     * Sets the length of the button border.
     *
     * @param borderLength the length of the button border
     */
    public void setBorderLength(int borderLength) {
        this.borderLength = borderLength;
    }

    /**
     * Disables this modern button.
     */
    public void setDisabled() {
        disabled = true;
    }

    /**
     * Enables this modern button.
     */
    public void setEnabled() {
        disabled = false;
    }

    /**
     * Returns whether the button is painted with rounded corners.
     *
     * @return whether the button is painted with rounded corners
     */
    public boolean isRoundedCorners() {
        return roundedCorners;
    }

    /**
     * Sets whether the button is painted with rounded corners.
     *
     * @param roundedCorners whether the button is painted with rounded corners
     */
    public void setRoundedCorners(boolean roundedCorners) {
        this.roundedCorners = roundedCorners;
    }

    /**
     * Returns the foreground color for the button text.
     *
     * @return the foreground color for the button text
     */
    public Color getForegroundColor() {
        return foregroundColor;
    }

    /**
     * Sets the foreground color for the button text.
     *
     * @param foregroundColor the foreground color for the button text
     */
    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    /**
     * Returns the background color for the button.
     *
     * @return the background color for the button
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for the button.
     *
     * @param backgroundColor the background color for the button
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Returns the color for the border.
     *
     * @return the color for the border
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the color for the border.
     *
     * @param borderColor the color for the border
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Returns the color for hover events.
     *
     * @return the color for hover events
     */
    public Color getHoverColor() {
        return hoverColor;
    }

    /**
     * Sets the color for hover events.
     *
     * @param hoverColor the color for hover events
     */
    public void setHoverColor(Color hoverColor) {
        this.hoverColor = hoverColor;
    }

    /**
     * Returns the color used for when the button model is pressed.
     *
     * @return the color used for when the button model is pressed
     */
    public Color getPressedColor() {
        return pressedColor;
    }

    /**
     * Sets the color used for when the button model is pressed.
     *
     * @param pressedColor the color used for when the button model is pressed
     */
    public void setPressedColor(Color pressedColor) {
        this.pressedColor = pressedColor;
    }

    /**
     * Returns the color used for the label text when the button is disabled.
     *
     * @return the color used for the label text when the button is disabled
     */
    public Color getDisabledForeground() {
        return disabledForeground;
    }

    /**
     * Sets the color used for the label text when the button is disabled.
     *
     * @param disabledForeground the color used for the label text when the button is disabled
     */
    public void setDisabledForeground(Color disabledForeground) {
        this.disabledForeground = disabledForeground;
    }

    /**
     * Returns the color used for when the button is disabled.
     *
     * @return the color used for when the button is disabled
     */
    public Color getDisabledBackground() {
        return disabledBackground;
    }

    /**
     * Sets the color used for when the button is disabled.
     *
     * @param disabledBackground the color used for when the button is disabled
     */
    public void setDisabledBackground(Color disabledBackground) {
        this.disabledBackground = disabledBackground;
    }

    /**
     * The focus listener to show when the button is the focus owner.
     */
    private final FocusListener defaultFocusListener = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            super.focusGained(e);

            if (!disabled) {
                setBackground(backgroundColor.darker());
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            super.focusLost(e);

            if (!disabled) {
                setBackground(backgroundColor);
            }
        }
    };

    /**
     * Adds the default focus listener to this modern button.
     */
    public void addDefaultFocusListener() {
        addFocusListener(defaultFocusListener);
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
        Color startingColor = backgroundColor;
        Color endingColor = backgroundColor.darker();

        CyderThreadRunner.submit(() -> {
            try {
                isFlashing = true;

                for (int i = 0 ; i < iterations ; i++) {
                    if (!isFlashing) {
                        break;
                    }

                    backgroundColor = startingColor;
                    repaint();
                    ThreadUtil.sleep(DEFAULT_ALERT_DELAY);
                    backgroundColor = endingColor;
                    repaint();
                    ThreadUtil.sleep(DEFAULT_ALERT_DELAY);
                }

                backgroundColor = startingColor;
                repaint();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                isFlashing = false;
            }
        }, toString());
    }

    /**
     * Kills all threads spawned by this modern button.
     * Currently this means stopping the flashing animation if on-going.
     */
    public void killThreads() {
        isFlashing = false;
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

        return text.equals(other.getText())
                && foregroundColor.equals(other.getForegroundColor())
                && backgroundColor.equals(other.getBackgroundColor())
                && hoverColor.equals(other.getHoverColor())
                && pressedColor.equals(other.getPressedColor())
                && borderColor.equals(other.getBorderColor())
                && width == other.getWidth()
                && height == other.getHeight();
    }
}
