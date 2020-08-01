package com.cyder.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

public class Notification extends JPanel {

    private int strokeThickness = 5;
    private int padding = strokeThickness / 2;
    private int arrowSize = 6;
    private int radius = 10;
    private Color c = new Color(80, 150, 180);
    private int width;
    private int height;

    public void setStrokeThickness(int strokeThickness) {
        this.strokeThickness= strokeThickness;
    }

    public void setArrowSize(int arrowSize) {
        this.arrowSize = arrowSize;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setColor(Color c) {
        this.c = c;
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public void setHeight(int h) {
        this.height = h;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D graphics2D = (Graphics2D) g;
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        graphics2D.setRenderingHints(qualityHints);
        graphics2D.setPaint(c);

        int width = getWidth();
        int height = getHeight();

        //todo make square of any height and width with rounded corners and arrow on any center of any side
        //todo add as much text as you want

        GeneralPath path = new GeneralPath();

        path.moveTo(5, 10);

        path.curveTo(5, 10, 7, 5, 0, 0);
        path.curveTo(0, 0, 12, 0, 12, 5);
        path.curveTo(12, 5, 12, 0, 20, 0);

        path.lineTo(this.width - 10, 0);
        path.curveTo(width - 10, 0, this.width, 0, this.width, 10);

        path.lineTo(this.width, this.width - 10);
        path.curveTo(this.width, this.width - 10, this.width, this.height, this.width - 10, this.height);

        path.lineTo(15, this.height);
        path.curveTo(15, this.height, 5, this.height, 5, this.height - 10);

        path.lineTo(5, 15);

        path.closePath();
        graphics2D.fill(path);
    }
}
