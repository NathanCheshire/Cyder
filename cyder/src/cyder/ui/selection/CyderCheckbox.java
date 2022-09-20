package cyder.ui.selection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderColors;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.drag.CyderDraggableComponent;
import cyder.utils.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * A custom styled checkbox for use throughout Cyder.
 */
public class CyderCheckbox extends JLabel {
    /**
     * The arc length for rounded corners.
     */
    public static final int ARC_LEN = 20;

    /**
     * The stroke width for a hollow circle check mark.
     */
    private float hollowCircleCheckStrokeWidth = 4.0f;

    /**
     * The possible shapes to indicate a checkbox is selected.
     */
    public enum CheckShape {
        /**
         * The standard check mark.
         */
        CHECK,

        /**
         * A fill circle check mark.
         */
        FILLED_CIRCLE,

        /**
         * A hollow circle check mark.
         */
        HOLLOW_CIRCLE
    }

    /**
     * The check shape of this checkbox.
     */
    private CheckShape checkShape = CheckShape.CHECK;

    /**
     * The length of the border for the checkbox.
     */
    private int borderLen = 3;

    /**
     * Whether the checkbox is checked.
     */
    private boolean isChecked;

    /**
     * Whether the checkbox is enabled.
     */
    private boolean enabled = true;

    /**
     * The side length of checkbox.
     */
    public int sideLength = 50;

    /**
     * The background color of the checkbox.
     */
    private Color background = new Color(21, 23, 24);

    /**
     * The color used for the checkbox checks.
     */
    private Color checkColor = CyderColors.regularPink;

    /**
     * Whether the checkbox has rounded corners.
     */
    private boolean roundedCorners = true;

    /**
     * The associated checkbox group.
     */
    private CyderCheckboxGroup cyderCheckboxGroup;

    /**
     * The function to invoke to update the checked state of this checkbox.
     */
    private Runnable refreshStateFunction;

    /**
     * Whether this checkbox is currently showing the check animation.
     */
    private final AtomicBoolean inCheckAnimation = new AtomicBoolean();

    /**
     * Constructs a new checkbox.
     */
    public CyderCheckbox() {
        this(false);
    }

    /**
     * Constructs a new checkbox.
     *
     * @param initialValue the initial value of the checkbox
     */
    public CyderCheckbox(boolean initialValue) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (enabled) {
                    if (isChecked) {
                        setNotChecked();
                    } else {
                        setChecked();
                    }

                    repaint();
                }

                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        setBorder(null);
        repaint();

        isChecked = initialValue;
        repaint();

        addMouseMotionListener(new CyderDraggableComponent());
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the associated check box group.
     *
     * @return the associated check box group
     */
    protected CyderCheckboxGroup getCyderCheckboxGroup() {
        return cyderCheckboxGroup;
    }

    /**
     * Sets the associated check box group.
     *
     * @param cyderCheckboxGroup the associated check box group
     */
    protected void setCyderCheckboxGroup(CyderCheckboxGroup cyderCheckboxGroup) {
        if (this.cyderCheckboxGroup != null && this.cyderCheckboxGroup != cyderCheckboxGroup) {
            cyderCheckboxGroup.removeCheckbox(this);
        }

        this.cyderCheckboxGroup = cyderCheckboxGroup;
    }

    /**
     * Sets the background color of this checkbox.
     *
     * @param c the background color of this checkbox
     */
    public void setBackground(Color c) {
        background = c;
    }

    /**
     * Returns the background color of this checkbox.
     *
     * @return the background color of this checkbox
     */
    public Color getBackground() {
        return background;
    }

    /**
     * Returns whether the checkbox is checked.
     *
     * @return whether the checkbox is checked
     */
    public boolean isChecked() {
        return isChecked;
    }

    /**
     * Sets the checkbox to be checked.
     */
    public void setChecked() {
        if (inCheckAnimation.get()) return;

        if (!isChecked) {
            animateCheck();
        }

        isChecked = true;
        repaint();

        if (cyderCheckboxGroup != null) {
            cyderCheckboxGroup.setCheckedBox(this);
        }
    }

    /**
     * Sets the checkbox to not be checked
     */
    public void setNotChecked() {
        if (inCheckAnimation.get()) return;

        isChecked = false;
        repaint();
    }

    /**
     * Sets whether the checkbox is checked.
     *
     * @param checked whether the checkbox is checked
     */
    public void setChecked(boolean checked) {
        if (checked) {
            setChecked();
        } else {
            setNotChecked();
        }
    }

    /**
     * Returns whether the checkbox has rounded corners.
     *
     * @return whether the checkbox has rounded corners
     */
    public boolean hasRoundedCorners() {
        return roundedCorners;
    }

    /**
     * Sets whether the checkbox has rounded corners.
     *
     * @param roundedCorners whether the checkbox has rounded corners
     */
    public void setRoundedCorners(boolean roundedCorners) {
        if (inCheckAnimation.get()) return;

        this.roundedCorners = roundedCorners;
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return sideLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return sideLength;
    }

    /**
     * The delay between check animation increments.
     */
    private static final int CHECK_ANIMATION_DELAY = 15;

    /**
     * The delay between background animation increments.
     */
    private static final int BACKGROUND_ANIMATION_DELAY = 2;

    /**
     * The name of the checkbox animation thread.
     */
    private static final String CHECK_ANIMATION_THREAD_NAME = "Checkbox Check Animation";

    /**
     * The current side length when in the background expanding animation.
     */
    private int expandingSideLength = sideLength;

    /**
     * Animates the filling and checking of the checkbox.
     */
    private void animateCheck() {
        inCheckAnimation.set(true);

        CyderThreadRunner.submit(() -> {
            IntStream.range(0, sideLength + 1).forEach(sideLen -> {
                expandingSideLength = sideLen;
                repaint();
                ThreadUtil.sleep(BACKGROUND_ANIMATION_DELAY);
            });

            Color originalCheckColor = checkColor;
            ImmutableList<Color> colors = ColorUtil.getFlashColors(background, originalCheckColor);

            colors.forEach(color -> {
                setCheckColor(color);
                repaint();
                ThreadUtil.sleep(CHECK_ANIMATION_DELAY);
            });

            setCheckColor(originalCheckColor);
            inCheckAnimation.set(false);
        }, CHECK_ANIMATION_THREAD_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        // Background
        if (roundedCorners) {
            graphics2D.setPaint(background);
            BasicStroke roundedCornersStroke = new BasicStroke(2.0f);
            graphics2D.setStroke(roundedCornersStroke);
            graphics2D.fill(new RoundRectangle2D.Double(0, 0, sideLength, sideLength, ARC_LEN, ARC_LEN));

            // Remove fill
            graphics2D.setPaint(CyderColors.vanilla);
            graphics2D.fill(new RoundRectangle2D.Double(borderLen, borderLen,
                    sideLength - 2 * borderLen, sideLength - 2 * borderLen, ARC_LEN, ARC_LEN));
        } else {
            graphics2D.setPaint(background);
            GeneralPath outlinePath = new GeneralPath();
            outlinePath.moveTo(0, 0);
            outlinePath.lineTo(sideLength, 0);
            outlinePath.lineTo(sideLength, sideLength);
            outlinePath.lineTo(0, sideLength);
            outlinePath.lineTo(0, 0);
            outlinePath.closePath();
            graphics2D.fill(outlinePath);

            graphics2D.setPaint(CyderColors.vanilla);
            GeneralPath fillPath = new GeneralPath();
            fillPath.moveTo(borderLen, borderLen);
            fillPath.lineTo(sideLength - borderLen, borderLen);
            fillPath.lineTo(sideLength - borderLen, sideLength - borderLen);
            fillPath.lineTo(borderLen, sideLength - borderLen);
            fillPath.lineTo(borderLen, borderLen);
            fillPath.closePath();
            graphics2D.fill(fillPath);
        }

        if (!isChecked) return;

        // Fill all of shape
        graphics2D.setPaint(background);
        graphics2D.setStroke(new BasicStroke(2.0f));
        int padding = (sideLength - expandingSideLength) / 2;
        graphics2D.fill(new RoundRectangle2D.Double(padding, padding,
                expandingSideLength, expandingSideLength, ARC_LEN, ARC_LEN));

        // Not done with filling animation
        if (expandingSideLength != sideLength) {
            return;
        }

        graphics2D.setColor(checkColor);
        switch (checkShape) {
            case CHECK -> {
                graphics2D.setColor(checkColor);
                //move enter check down
                int yTranslate = 4;

                //thickness of line drawn
                graphics2D.setStroke(new BasicStroke(5));

                // initial top right corner to center
                int cornerOffset = sideLength / 5;
                graphics2D.drawLine(
                        sideLength - borderLen - cornerOffset,
                        borderLen + cornerOffset + yTranslate,
                        sideLength / 2,
                        sideLength / 2 + yTranslate);

                // length from center to bottom most check point
                int secondaryDip = 5;
                graphics2D.drawLine(
                        sideLength / 2,
                        sideLength / 2 + yTranslate,
                        sideLength / 2 - secondaryDip,
                        sideLength / 2 + secondaryDip + yTranslate);

                // length from bottom most part back up
                int lengthUp = 8;
                graphics2D.drawLine(
                        sideLength / 2 - secondaryDip,
                        sideLength / 2 + secondaryDip + yTranslate,
                        sideLength / 2 - secondaryDip - lengthUp,
                        sideLength / 2 + secondaryDip - lengthUp + yTranslate);
            }
            case FILLED_CIRCLE -> {
                int diameter = sideLength / 2;
                int x = sideLength / 2 - diameter / 2;
                int y = sideLength / 2 - diameter / 2;
                g.fillOval(x, y, diameter, diameter);
            }
            case HOLLOW_CIRCLE -> {
                graphics2D.setStroke(new BasicStroke(hollowCircleCheckStrokeWidth));

                int diameter = sideLength / 2;
                int x = sideLength / 2 - diameter / 2;
                int y = sideLength / 2 - diameter / 2;
                g.drawOval(x, y, diameter, diameter);
            }
            default -> throw new IllegalArgumentException("Invalid check shape: " + checkShape);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the border length of the checkbox.
     *
     * @return the border length of the checkbox
     */
    public int getBorderLength() {
        return borderLen;
    }

    /**
     * Sets the border length of the checkbox.
     *
     * @param borderLen the border length of the checkbox
     */
    public void setBorderLength(int borderLen) {
        this.borderLen = borderLen;
    }

    /**
     * Returns the check color.
     *
     * @return the check color
     */
    public Color getCheckColor() {
        return checkColor;
    }

    /**
     * Sets the check color.
     *
     * @param checkColor the check color
     */
    public void setCheckColor(Color checkColor) {
        this.checkColor = checkColor;
    }

    /**
     * Returns the check shape.
     *
     * @return the check shape
     */
    public CheckShape getCheckShape() {
        return checkShape;
    }

    /**
     * Sets the check shape.
     *
     * @param checkShape the check shape
     */
    public void setCheckShape(CheckShape checkShape) {
        this.checkShape = checkShape;
    }

    /**
     * Returns the stroke width for a hollow circle check mark.
     *
     * @return the stroke width for a hollow circle check mark
     */
    public float getHollowCircleCheckStrokeWidth() {
        return hollowCircleCheckStrokeWidth;
    }

    /**
     * Sets the stroke width for a hollow circle check mark.
     *
     * @param hollowCircleCheckStrokeWidth the stroke width for a hollow circle check mark
     */
    public void setHollowCircleCheckStrokeWidth(float hollowCircleCheckStrokeWidth) {
        this.hollowCircleCheckStrokeWidth = hollowCircleCheckStrokeWidth;
    }

    /**
     * Returns the refresh state function.
     *
     * @return the refresh state function
     */
    public Runnable getRefreshStateFunction() {
        return refreshStateFunction;
    }

    /**
     * Sets the refresh state function.
     *
     * @param refreshStateFunction the refresh state function
     */
    public void setRefreshStateFunction(Runnable refreshStateFunction) {
        this.refreshStateFunction = Preconditions.checkNotNull(refreshStateFunction);
    }

    /**
     * Refreshes the state of the checkbox using the refresh state function.
     */
    public void refreshState() {
        if (inCheckAnimation.get()) return;

        Preconditions.checkState(refreshStateFunction != null);
        refreshStateFunction.run();
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override /* To ensure setLocation calls work same as set bounds */
    public void setLocation(int x, int y) {
        super.setBounds(x, y, getWidth(), getHeight());
    }

    /**
     * {@inheritDoc}
     */
    @Override /* To ensure length is not changed via get bounds */
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, sideLength, sideLength);
    }

    /**
     * {@inheritDoc}
     */
    @Override /* To ensure same len */
    public void setSize(int width, int height) {
        Preconditions.checkArgument(width == height);
        sideLength = width;
        super.setSize(width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override /* To ensure same len */
    public void setSize(Dimension dimension) {
        Preconditions.checkArgument(dimension.width == dimension.height);
        sideLength = dimension.width;
        super.setSize(dimension);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CyderCheckbox{" +
                "checkShape=" + checkShape +
                ", borderLen=" + borderLen +
                ", isChecked=" + isChecked +
                ", enabled=" + enabled +
                ", sideLength=" + sideLength +
                ", background=" + background +
                ", checkColor=" + checkColor +
                ", roundedCorners=" + roundedCorners +
                ", cyderCheckboxGroup=" + cyderCheckboxGroup +
                '}';
    }
}
