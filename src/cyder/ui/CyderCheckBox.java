package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderImages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;

public class CyderCheckBox extends JLabel {

    private boolean selected = false;

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

    public CyderCheckBox() {
        addMouseMotionListener(new CyderDragableComponent());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //toggle color
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //togle color
            }
        });
        repaint();
    }

    public CyderCheckBox(boolean initalValue) {
        addMouseMotionListener(new CyderDragableComponent());
        selected = initalValue;
        repaint();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D graphics2D = (Graphics2D) g;

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        graphics2D.setPaint(new Color(21,23,24));
        GeneralPath outlinePath = new GeneralPath();
        outlinePath.moveTo(0, 0);
        outlinePath.lineTo(50,0);
        outlinePath.lineTo(50,50);
        outlinePath.lineTo(0,50);
        outlinePath.lineTo(0,0);
        outlinePath.closePath();
        graphics2D.fill(outlinePath);

        graphics2D.setPaint(selected ? CyderColors.regularRed : CyderColors.vanila);
        GeneralPath checkPath = new GeneralPath();
        graphics2D.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.drawLine(5, 5, 45, 45);
        graphics2D.drawLine(45, 5, 5, 45);
    }
}
