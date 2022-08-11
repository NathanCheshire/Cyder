package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

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
    private String text = "Modern Button";

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

    private static final int CORNER_RADIUS = 20;
    private int borderRadius = 3;

    private int width = 150;
    private int height = 40;

    private JLabel innerTextLabel;

    private final AtomicBoolean mouseIn = new AtomicBoolean();

    public CyderModernButton() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseIn.set(true);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseIn.set(false);
                repaint();
            }
        });

        innerTextLabel = new JLabel();
        innerTextLabel.setHorizontalAlignment(JLabel.CENTER);
        // todo enum for config of positioning, top left, bottom right, etc.
        innerTextLabel.setVerticalAlignment(JLabel.CENTER);
        innerTextLabel.setBounds(0, 0, width, height);
        innerTextLabel.setFont(font);
        innerTextLabel.setForeground(foregroundColor);
        innerTextLabel.setText(text);
        add(innerTextLabel);
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

        if (roundedCorners) {
            graphics2D.setPaint(borderColor);
            graphics2D.setStroke(new BasicStroke(1.0f));
            graphics2D.fill(new RoundRectangle2D.Double(0, 0, width, height, CORNER_RADIUS, CORNER_RADIUS));
            graphics2D.setPaint(mouseIn.get() ? hoverColor : backgroundColor);
            graphics2D.fill(new RoundRectangle2D.Double(borderRadius, borderRadius,
                    width - 2 * borderRadius, height - 2 * borderRadius, CORNER_RADIUS, CORNER_RADIUS));
        } else {
            // todo
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
