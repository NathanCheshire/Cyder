package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;

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

    private Font labelFont = CyderFonts.DEFAULT_FONT_SMALL;

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
    private int alertDelay = 250;
    private int alertIterations = 10;
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
        innerTextLabel.setVerticalAlignment(JLabel.CENTER);
        innerTextLabel.setBounds(0, 0, 150, 40);
        innerTextLabel.setFont(labelFont);
        innerTextLabel.setForeground(foregroundColor);
        innerTextLabel.setText(text);
        add(innerTextLabel);
    }

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
        }
    }
}
