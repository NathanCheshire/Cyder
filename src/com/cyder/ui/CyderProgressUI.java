package com.cyder.ui;

import com.cyder.Constants.CyderColors;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

public class CyderProgressUI extends BasicProgressBarUI {

    Color fillColor = CyderColors.vanila;
    Color outlineColor = CyderColors.navy;

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int iStrokWidth = 5;
        g2d.setStroke(new BasicStroke(iStrokWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(outlineColor);
        g2d.setBackground(CyderColors.selectionColor);

        int width = c.getWidth();
        int height = c.getHeight();

        RoundRectangle2D outline = new RoundRectangle2D.Double(iStrokWidth / 2, iStrokWidth / 2,
                width - iStrokWidth, height - iStrokWidth, height, height);

        g2d.draw(outline);

        int iInnerHeight = height - (iStrokWidth * 4);
        int iInnerWidth = width - (iStrokWidth * 4);

        int x = iStrokWidth * 2;
        int y = iStrokWidth * 2;

        Point2D start = new Point2D.Double(x, y);
        Point2D end = new Point2D.Double(x, y + iInnerHeight);

        g2d.setPaint(fillColor);

        RoundRectangle2D fill = new RoundRectangle2D.Double(iStrokWidth * 2, iStrokWidth * 2,
                ((JProgressBar) c).getValue() / 100 * width, iInnerHeight, iInnerHeight, iInnerHeight);

        g2d.fill(fill);
        g2d.dispose();
    }


}