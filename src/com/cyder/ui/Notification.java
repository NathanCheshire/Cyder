package com.cyder.ui;

import com.cyder.utilities.GeneralUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

public class Notification extends JLabel {

    private GeneralUtil notificationHandler = new GeneralUtil();

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

    public static final int LEFT_START = 1;
    public static final int TOP_START = 2;
    public static final int RIGHT_START = 3;

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

        graphics2D.setPaint(fillColor.darker().darker());

        GeneralPath outlinePath = new GeneralPath();

        outlinePath.moveTo(8, 8);

        outlinePath.curveTo(8, 8,10,6, 12, 4);
        outlinePath.lineTo(this.width + 14 + 2, 4);

        outlinePath.curveTo(this.width + 14 + 2, 4, this.width + 16 + 2, 6, this.width + 18 + 2, 8);
        outlinePath.lineTo(this.width + 18 + 2, this.height + 10 + 2);

        outlinePath.curveTo(this.width + 18 + 2, this.height + 10  +2, this.width + 16 + 2, this.height + 12 + 2, this.width + 14 + 2, this.height + 14  +2);
        outlinePath.lineTo(12, this.height + 14 + 2);

        outlinePath.curveTo(12, this.height + 14 + 2, 10, this.height + 12 + 2, 8, this.height + 10 + 2);
        outlinePath.lineTo( 8, 8);

        outlinePath.closePath();
        graphics2D.fill(outlinePath);
        graphics2D.setPaint(fillColor);

        GeneralPath fillPath = new GeneralPath();

        fillPath.moveTo(10, 10);

        fillPath.curveTo(10, 10,12,8, 14, 6);
        fillPath.lineTo(this.width + 14, 6);

        fillPath.curveTo(this.width + 14, 6, this.width + 16, 8, this.width + 18, 10);
        fillPath.lineTo(this.width + 18, this.height + 10);

        fillPath.curveTo(this.width + 18, this.height + 10, this.width + 16, this.height + 12, this.width + 14, this.height + 14);
        fillPath.lineTo(14, this.height + 14);

        fillPath.curveTo(14, this.height + 14, 12, this.height + 12, 10, this.height + 10);
        fillPath.lineTo( 10, 10);

        fillPath.closePath();
        graphics2D.fill(fillPath);

        //this adds the arrow on sides
        switch (type) {
            case Notification.TOP:
                fillPath.moveTo(8 + this.width / 2, 6);
                fillPath.lineTo(14 + this.width / 2,0);
                fillPath.lineTo(20 + this.width / 2,6);
                fillPath.lineTo(8 + this.width / 2, 6);

                fillPath.closePath();
                graphics2D.fill(fillPath);

                //todo add border to arrows

                break;
            case Notification.LEFT: //left
                fillPath.moveTo(10, 4 + height/2);
                fillPath.lineTo(4, 10 + height/2);
                fillPath.lineTo(10, 16 + height/2);
                fillPath.lineTo(10, 4 + height/2);

                fillPath.closePath();
                graphics2D.fill(fillPath);

                //todo add border to arrows

                break;
            case Notification.BOTTOM:
                fillPath.moveTo(18 + this.width, 4 + height/2);
                fillPath.lineTo(24 + this.width, 10 + height/2);
                fillPath.lineTo(18 + this.width, 16 + height/2);
                fillPath.lineTo(18 + this.width, 4 + height/2);

                fillPath.closePath();
                graphics2D.fill(fillPath);

                //todo add border to arrows

                break;
            case Notification.RIGHT:
                fillPath.moveTo(8 + width/2, 14+ height);
                fillPath.lineTo(14 + width/2, 20 + height);
                fillPath.lineTo(20 + width/2, 14 + height);
                fillPath.lineTo(8 + width/2, 14+ height);

                fillPath.closePath();
                graphics2D.fill(fillPath);

                //todo add border to arrows

                break;
        }
    }

    public void vanish(int vanishDir, Component parent, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                CyderAnimation ca = new CyderAnimation();

                switch(vanishDir) {
                    case Notification.TOP_VANISH:
                        ca.jLabelYUp(this.getY(), - this.getHeight(), 10, 8, this);
                        Thread.sleep(10 * (this.getHeight() + this.getY())/ 8);
                        break;
                    case Notification.BOTTOM_VANISH:
                        ca.jLabelYDown(this.getY(), parent.getHeight(), 10, 8, this);
                        Thread.sleep(10 * (parent.getHeight() - this.getY())/ 8);
                        break;
                    case Notification.RIGHT_VANISH:
                        ca.jLabelXRight(this.getX(), parent.getWidth(), 10, 8, this);
                        Thread.sleep(10 * (parent.getWidth() -  this.getX())/ 8);
                        break;

                    case Notification.LEFT_VANISH:
                        ca.jLabelXLeft(this.getX(), - this.getWidth(), 10, 8, this);
                        Thread.sleep(10 * (this.getWidth() + this.getX())/ 8);
                        break;
                }

                this.setVisible(false);
            }

            catch (Exception e) {
                notificationHandler.handle(e);
            }
        }).start();
    }

    public void kill() {
        this.setVisible(false);
    }
}