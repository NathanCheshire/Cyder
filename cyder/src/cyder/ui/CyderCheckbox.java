package cyder.ui;

import cyder.constants.CyderColors;
import cyder.handlers.internal.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

/**
 * A custom painted checkbox.
 */
public class CyderCheckbox extends JLabel {
    /**
     * The length of the border for the checkbox.
     */
    private int borderLen = 3;

    /**
     * Whether the checkbox is selected.
     */
    private boolean selected;

    /**
     * Whether the checkbox is enabled.
     */
    private boolean enabled = true;

    /**
     * The side length of checkbox.
     */
    public static final int sideLength = 50;

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
                    if (selected) {
                        setNotSelected();
                    } else {
                        setSelected();
                    }

                    repaint();
                }

                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        setBorder(null);
        repaint();

        selected = initialValue;
        repaint();

        addMouseMotionListener(new CyderDraggableComponent());
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the associated check box group.
     *
     * @return the associated check box group
     */
    @SuppressWarnings("unused")
    protected CyderCheckboxGroup getCyderCheckboxGroup() {
        return cyderCheckboxGroup;
    }

    /**
     * Sets the associated check box group.
     *
     * @param cyderCheckboxGroup the associated check box group
     */
    protected void setCyderCheckboxGroup(CyderCheckboxGroup cyderCheckboxGroup) {
        if (this.cyderCheckboxGroup != null && this.cyderCheckboxGroup != cyderCheckboxGroup)
            cyderCheckboxGroup.removeCheckbox(this);

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
     * Returns whether the checkbox is selected.
     *
     * @return whether the checkbox is selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets the checkbox to be selected.
     */
    public void setSelected() {
        selected = true;
        repaint();

        if (cyderCheckboxGroup != null)
            cyderCheckboxGroup.setSelectedCheckbox(this);
    }

    /**
     * Sets the checkbox to not be selected
     */
    public void setNotSelected() {
        selected = false;
        repaint();
    }

    /**
     * Sets whether the checkbox is selected.
     *
     * @param b whether the checkbox is selected
     */
    public void setSelected(boolean b) {
        selected = b;
        repaint();

        if (b && cyderCheckboxGroup != null) {
            cyderCheckboxGroup.setSelectedCheckbox(this);
        }
    }

    /**
     * Returns whether the checkbox has rounded corners.
     *
     * @return whether the checkbox has rounded corners
     */
    public boolean getRoundedCorners() {
        return roundedCorners;
    }

    /**
     * Sets whether the checkbox has rounded corners.
     *
     * @param b whether the checkbox has rounded corners
     */
    public void setRoundedCorners(Boolean b) {
        roundedCorners = b;
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
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;

        RenderingHints qualityHints =
                new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        if (roundedCorners) {
            if (isSelected()) {
                graphics2D.setPaint(background);
                graphics2D.setStroke(new BasicStroke(2.0f));
                graphics2D.fill(new RoundRectangle2D.Double(0, 0, sideLength, sideLength, 20, 20));

                //move enter check down
                int yTranslate = 4;

                graphics2D.setColor(checkColor);

                //thickness of line drawn
                graphics2D.setStroke(new BasicStroke(5));

                int cornerOffset = 5;
                graphics2D.drawLine(sideLength - borderLen - cornerOffset, borderLen + cornerOffset + yTranslate,
                        sideLength / 2, sideLength / 2 + yTranslate);

                //length from center to bottom most check point
                int secondaryDip = 5;
                graphics2D.drawLine(sideLength / 2, sideLength / 2 + yTranslate,
                        sideLength / 2 - secondaryDip, sideLength / 2 + secondaryDip + yTranslate);

                //length from bottom most part back up
                int lengthUp = 9;
                graphics2D.drawLine(sideLength / 2 - secondaryDip, sideLength / 2 + secondaryDip + yTranslate,
                        sideLength / 2 - secondaryDip - lengthUp, sideLength / 2 + secondaryDip - lengthUp + yTranslate);

            } else {
                graphics2D.setPaint(background);
                graphics2D.setStroke(new BasicStroke(2.0f));
                graphics2D.fill(new RoundRectangle2D.Double(0, 0, sideLength, sideLength, 20, 20));
                graphics2D.setPaint(CyderColors.vanilla);
                graphics2D.fill(new RoundRectangle2D.Double(borderLen, borderLen,
                        sideLength - 6, sideLength - 6, 20, 20));
            }
        } else {
            if (isSelected()) {
                graphics2D.setPaint(background);
                GeneralPath outlinePath = new GeneralPath();
                outlinePath.moveTo(0, 0);
                outlinePath.lineTo(sideLength, 0);
                outlinePath.lineTo(sideLength, sideLength);
                outlinePath.lineTo(0, sideLength);
                outlinePath.lineTo(0, 0);
                outlinePath.closePath();
                graphics2D.fill(outlinePath);

                //move enter check down
                int yTranslate = 4;

                graphics2D.setColor(checkColor);

                //thickness of line drawn
                graphics2D.setStroke(new BasicStroke(5));

                int cornerOffset = 5;
                graphics2D.drawLine(sideLength - borderLen - cornerOffset, borderLen + cornerOffset + yTranslate,
                        sideLength / 2, sideLength / 2 + yTranslate);

                //length from center to bottom most check point
                int secondaryDip = 5;
                graphics2D.drawLine(sideLength / 2, sideLength / 2 + yTranslate,
                        sideLength / 2 - secondaryDip, sideLength / 2 + secondaryDip + yTranslate);

                //length from bottom most part back up
                int lengthUp = 9;
                graphics2D.drawLine(sideLength / 2 - secondaryDip, sideLength / 2 + secondaryDip + yTranslate,
                        sideLength / 2 - secondaryDip - lengthUp, sideLength / 2 + secondaryDip - lengthUp + yTranslate);

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
        }

        graphics2D.dispose();
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
     * Returns the border width of the checkbox.
     *
     * @return the border width of the checkbox
     */
    public int getBorderLen() {
        return borderLen;
    }

    /**
     * Sets the border width of the checkbox.
     *
     * @param borderLen Sets the border width of the checkbox
     */
    public void setBorderLen(int borderLen) {
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
}
