package cyder.ui;

import cyder.constants.CyderColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

public class CyderCheckBox extends JCheckBox {

    private boolean selected = false;

    public static final int sideLength = 50;

    private Color selectedColor = CyderColors.regularRed;
    private Color notSelectedColor = CyderColors.vanila;

    private Color drawColor = selected ? selectedColor : notSelectedColor;
    private Color background = new Color(21,23,24);

    public void setBackground(Color c) {
        background = c;
    }

    public Color getBackground() {
        return background;
    }

    public void setSelectedColor(Color c) {
        selectedColor = c;
    }

    public Color getSelectedColor(Color c) {
        return selectedColor;
    }

    public void setNotSelectedColor(Color c) {
        notSelectedColor = c;
    }

    public Color getNotSelectedColor() {
        return notSelectedColor;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected() {
        selected = true;
        drawColor = CyderColors.regularRed;
        repaint();
    }

    public void setNotSelected() {
        selected = false;
        drawColor = CyderColors.vanila;
        repaint();
    }

    public CyderCheckBox() {
        addMouseMotionListener(new CyderDragableComponent());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;

                if (selected)
                    drawColor = CyderColors.regularRed;
                else
                    drawColor = CyderColors.vanila;

                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                drawColor = selected ? CyderColors.vanila : CyderColors.regularRed;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                drawColor = selected ? CyderColors.regularRed : CyderColors.vanila;
                repaint();
            }
        });

        setBorder(null);
        repaint();
    }

    public CyderCheckBox(boolean initalValue) {
        addMouseMotionListener(new CyderDragableComponent());
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

        graphics2D.setPaint(background);
        GeneralPath outlinePath = new GeneralPath();
        outlinePath.moveTo(0, 0);
        outlinePath.lineTo(sideLength,0);
        outlinePath.lineTo(sideLength,sideLength);
        outlinePath.lineTo(0,50);
        outlinePath.lineTo(0,0);
        outlinePath.closePath();
        graphics2D.fill(outlinePath);

        graphics2D.setPaint(drawColor);
        GeneralPath checkPath = new GeneralPath();
        graphics2D.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.drawLine(5, 5, sideLength - 5, sideLength - 5);
        graphics2D.drawLine(sideLength - 5, 5, 5, sideLength - 5);
    }
}
