package cyder.ui;

import cyder.consts.CyderColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

public class CyderCheckBox extends JLabel {
    private int borderWidth = 3;

    private boolean selected = false;

    public static final int sideLength = 50;

    private Color background = new Color(21,23,24);

    private boolean roundedCorners = true;

    public void setBackground(Color c) {
        background = c;
    }

    public Color getBackground() {
        return background;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected() {
        selected = true;
        repaint();
    }

    public void setNotSelected() {
        selected = false;
        repaint();
    }

    public boolean getRoundedCorners() {
        return this.roundedCorners;
    }

    public void setRoundedCorners(Boolean b) {
        this.roundedCorners = b;
        repaint();
    }

    public CyderCheckBox() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;
                repaint();
            }
        });

        setBorder(null);
        repaint();
    }

    public CyderCheckBox(boolean initalValue) {
        selected = initalValue;
        repaint();
    }

    @Override
    public int getWidth() {
        return sideLength;
    }

    @Override
    public int getHeight() {
        return sideLength;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D graphics2D = (Graphics2D) g;

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        if (roundedCorners) {
            if (this.isSelected()) {
                graphics2D.setPaint(background);
                graphics2D.setStroke(new BasicStroke(2.0f));
                graphics2D.fill(new RoundRectangle2D.Double(0, 0, sideLength, sideLength, 20, 20));

                //move enter check down
                int yTranslate = 4;

                graphics2D.setColor(CyderColors.intellijPink);

                //thickness of line drawn
                graphics2D.setStroke(new BasicStroke(5));

                int cornerOffset = 5;
                graphics2D.drawLine(sideLength - borderWidth - cornerOffset, borderWidth + cornerOffset + yTranslate,
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
                graphics2D.setPaint(CyderColors.vanila);
                graphics2D.fill(new RoundRectangle2D.Double(3, 3, sideLength - 6, sideLength - 6, 20, 20));
            }
        } else {
            if (this.isSelected()) {
                graphics2D.setPaint(background);
                GeneralPath outlinePath = new GeneralPath();
                outlinePath.moveTo(0, 0);
                outlinePath.lineTo(sideLength,0);
                outlinePath.lineTo(sideLength,sideLength);
                outlinePath.lineTo(0,sideLength);
                outlinePath.lineTo(0,0);
                outlinePath.closePath();
                graphics2D.fill(outlinePath);

                //move enter check down
                int yTranslate = 4;

                graphics2D.setColor(CyderColors.intellijPink);

                //thickness of line drawn
                graphics2D.setStroke(new BasicStroke(5));

                int cornerOffset = 5;
                graphics2D.drawLine(sideLength - borderWidth - cornerOffset, borderWidth + cornerOffset + yTranslate,
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
                outlinePath.lineTo(sideLength,0);
                outlinePath.lineTo(sideLength,sideLength);
                outlinePath.lineTo(0,50);
                outlinePath.lineTo(0,0);
                outlinePath.closePath();
                graphics2D.fill(outlinePath);

                graphics2D.setPaint(CyderColors.vanila);
                GeneralPath fillPath = new GeneralPath();
                fillPath.moveTo(borderWidth, borderWidth);
                fillPath.lineTo(sideLength - borderWidth,borderWidth);
                fillPath.lineTo(sideLength - borderWidth,sideLength - borderWidth);
                fillPath.lineTo(borderWidth,sideLength - borderWidth);
                fillPath.lineTo(borderWidth,borderWidth);
                fillPath.closePath();
                graphics2D.fill(fillPath);
            }
        }

        graphics2D.dispose();
    }

    @Override
    public String toString() {
        return "CyderCheckBox object, hash=" + this.hashCode();
    }
}
