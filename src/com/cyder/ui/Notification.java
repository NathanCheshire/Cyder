package com.cyder.ui;

import com.cyder.utilities.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

public class Notification extends JLabel {

    private Util notificationHandler = new Util();

    private int strokeThickness = 5;
    private int padding = strokeThickness / 2;
    private int arrowSize = 6;
    private int radius = 10;

    private int timeout;

    private Color fillColor = new Color(236,64,122);

    private int width = 300;
    private int height = 300;
    private int type = 1;

    public static final int TOP_ARROW = 1;
    public static final int LEFT_ARROW = 2;
    public static final int RIGHT_ARROW = 3;
    public static final int BOTTOM_ARROW = 4;

    public static final int TOP_VANISH = 1;
    public static final int LEFT_VANISH = 2;
    public static final int RIGHT_VANISH = 3;
    public static final int BOTTOM_VANISH = 4;

    public void setStrokeThickness(int strokeThickness) {
        this.strokeThickness = strokeThickness;
    }

    public void setArrowSize(int arrowSize) {
        this.arrowSize = arrowSize;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setFillColor(Color c) {
        this.fillColor = c;
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public void setHeight(int h) {
        this.height = h;
    }

    public void setArrow(int type) {
        this.type = type;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D graphics2D = (Graphics2D) g;
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        graphics2D.setRenderingHints(qualityHints);
        graphics2D.setPaint(fillColor);

        GeneralPath path = new GeneralPath();

        path.moveTo(10, 10);

        path.curveTo(10, 10,12,8, 14, 6);
        path.lineTo(this.width + 14, 6);

        path.curveTo(this.width + 14, 6, this.width + 16, 8, this.width + 18, 10);
        path.lineTo(this.width + 18, this.height + 10);

        path.curveTo(this.width + 18, this.height + 10, this.width + 16, this.height + 12, this.width + 14, this.height + 14);
        path.lineTo(14, this.height + 14);

        path.curveTo(14, this.height + 14, 12, this.height + 12, 10, this.height + 10);
        path.lineTo( 10, 10);

        path.closePath();
        graphics2D.fill(path);

        switch (type) {
            case 1:
                path.moveTo(8 + this.width / 2, 6);
                path.lineTo(14 + this.width / 2,0);
                path.lineTo(20 + this.width / 2,6);
                path.lineTo(8 + this.width / 2, 6);

                path.closePath();
                graphics2D.fill(path);
                break;
            case 2:
                path.moveTo(10, 4 + height/2);
                path.lineTo(4, 10 + height/2);
                path.lineTo(10, 16 + height/2);
                path.lineTo(10, 4 + height/2);

                path.closePath();
                graphics2D.fill(path);
                break;

            case 3:
                path.moveTo(18 + this.width, 4 + height/2);
                path.lineTo(24 + this.width, 10 + height/2);
                path.lineTo(18 + this.width, 16 + height/2);
                path.lineTo(18 + this.width, 4 + height/2);

                path.closePath();
                graphics2D.fill(path);
                break;

            case 4:
                path.moveTo(8 + width/2, 14+ height);
                path.lineTo(14 + width/2, 20 + height);
                path.lineTo(20 + width/2, 14 + height);
                path.lineTo(8 + width/2, 14+ height);

                path.closePath();
                graphics2D.fill(path);
                break;
        }
    }
}
