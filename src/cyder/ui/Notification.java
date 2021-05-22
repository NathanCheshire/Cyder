package cyder.ui;

import cyder.enums.ArrowDirection;
import cyder.enums.StartDirection;
import cyder.enums.VanishDirection;
import cyder.handler.ErrorHandler;
import cyder.utilities.AnimationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

public class Notification extends JLabel {

    private int strokeThickness = 5;
    private int padding = strokeThickness / 2;
    private int arrowSize = 6;
    private int radius = 10;

    private int timeout;

    private Color fillColor = new Color(236,64,122);

    private int width = 300;
    private int height = 300;

    private ArrowDirection ArrowType = ArrowDirection.TOP;

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

    public void setArrow(ArrowDirection type) {
        this.ArrowType = type;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D graphics2D = (Graphics2D) g;

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        graphics2D.setPaint(fillColor.darker().darker());

        GeneralPath outlinePath = new GeneralPath();

        outlinePath.moveTo(8, 8 + 2);

        outlinePath.curveTo(8, 8 + 2,10,6 + 2, 12, 4 + 2);
        outlinePath.lineTo(this.width + 14 + 2, 4 + 2);

        outlinePath.curveTo(this.width + 14 + 2, 4 + 2, this.width + 16 + 2, 6 + 2, this.width + 18 + 2, 8 + 2);
        outlinePath.lineTo(this.width + 18 + 2, this.height + 10 + 2 + 2);

        outlinePath.curveTo(this.width + 18 + 2, this.height + 10 + 2 + 2, this.width + 16 + 2, this.height + 12 + 2  + 2, this.width + 14 + 2, this.height + 14 + 2  + 2);
        outlinePath.lineTo(12, this.height + 14 + 2 + 2);

        outlinePath.curveTo(12, this.height + 14 + 2 + 2, 10, this.height + 12 + 2 + 2, 8, this.height + 10 + 2 + 2);
        outlinePath.lineTo( 8, 8 + 2);

        switch (ArrowType) {
            case TOP:
                outlinePath.moveTo(6 + this.width / 2, 6 + 2);
                outlinePath.lineTo(14 + this.width / 2,-2 + 2);
                outlinePath.lineTo(22 + this.width / 2,6 + 2);
                outlinePath.lineTo(6 + this.width / 2, 6 + 2);

                outlinePath.closePath();
                graphics2D.fill(outlinePath);

                break;
            case LEFT:
                outlinePath.moveTo(8, 2 + height/2 + 2);
                outlinePath.lineTo(2, 10 + height/2 + 2);
                outlinePath.lineTo(8, 18 + height/2 + 2);
                outlinePath.lineTo(8, 2 + height/2 + 2);

                outlinePath.closePath();
                graphics2D.fill(outlinePath);

                break;
            case RIGHT:
                outlinePath.moveTo(18 + this.width, 2 + height/2 + 2);
                outlinePath.lineTo(26 + this.width, 10 + height/2 + 2);
                outlinePath.lineTo(18 + this.width, 18 + height/2 + 2);
                outlinePath.lineTo(18 + this.width, 2 + height/2 + 2);

                outlinePath.closePath();
                graphics2D.fill(outlinePath);

                break;
            case BOTTOM:
                outlinePath.moveTo(8 + width/2, 16 + height + 2);
                outlinePath.lineTo(14 + width/2, 22 + height + 2);
                outlinePath.lineTo(20 + width/2, 16 + height + 2);
                outlinePath.lineTo(8 + width/2, 16 + height + 2);

                outlinePath.closePath();
                graphics2D.fill(outlinePath);

                break;
        }

        graphics2D.setPaint(fillColor);

        GeneralPath fillPath = new GeneralPath();

        fillPath.moveTo(10, 10 + 2);

        fillPath.curveTo(10, 10 + 2,12,8 + 2, 14, 6 + 2);
        fillPath.lineTo(this.width + 14, 6 + 2);

        fillPath.curveTo(this.width + 14, 6 + 2, this.width + 16, 8 + 2, this.width + 18, 10 + 2);
        fillPath.lineTo(this.width + 18, this.height + 10 + 2);

        fillPath.curveTo(this.width + 18, this.height + 10 + 2, this.width + 16, this.height + 12 + 2, this.width + 14, this.height + 14 + 2);
        fillPath.lineTo(14, this.height + 14 + 2);

        fillPath.curveTo(14, this.height + 14 + 2, 12, this.height + 12 + 2, 10, this.height + 10 + 2);
        fillPath.lineTo( 10, 10 + 2);

        fillPath.closePath();
        graphics2D.fill(fillPath);

        switch (ArrowType) {
            case TOP:
                fillPath.moveTo(8 + this.width / 2, 6 + 2);
                fillPath.lineTo(14 + this.width / 2, 2);
                fillPath.lineTo(20 + this.width / 2,6 + 2);
                fillPath.lineTo(8 + this.width / 2, 6 + 2);

                fillPath.closePath();
                graphics2D.fill(fillPath);

                break;
            case LEFT:
                fillPath.moveTo(10, 4 + height/2 + 2);
                fillPath.lineTo(4, 10 + height/2 + 2);
                fillPath.lineTo(10, 16 + height/2 + 2);
                fillPath.lineTo(10, 4 + height/2 + 2);

                fillPath.closePath();
                graphics2D.fill(fillPath);

                break;
            case RIGHT:
                fillPath.moveTo(18 + this.width, 4 + height/2 + 2);
                fillPath.lineTo(24 + this.width, 10 + height/2 + 2);
                fillPath.lineTo(18 + this.width, 16 + height/2 + 2);
                fillPath.lineTo(18 + this.width, 4 + height/2 + 2);

                fillPath.closePath();
                graphics2D.fill(fillPath);

                break;
            case BOTTOM:
                fillPath.moveTo(8 + width/2, 14 + height + 2);
                fillPath.lineTo(14 + width/2, 20 + height + 2);
                fillPath.lineTo(20 + width/2, 14 + height + 2);
                fillPath.lineTo(8 + width/2, 14 + height + 2);

                fillPath.closePath();
                graphics2D.fill(fillPath);

                break;
        }
    }

    /**
     * This method to be used in combination with an already visible notification.
     * @param vanishDir - the direction to exit to.
     * @param parent - the component the notification is on. Used for bounds calculations.
     * @param delay - the delay before vanish.
     */
    public void vanish(VanishDirection vanishDir, Component parent, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                switch(vanishDir) {
                    case TOP:
                        AnimationUtil.jLabelYUp(this.getY(), - this.getHeight(), 10, 4, this);
                        Thread.sleep(10 * (this.getHeight() + this.getY())/ 4);
                        break;
                    case BOTTOM:
                        AnimationUtil.jLabelYDown(this.getY(), parent.getHeight(), 10, 4, this);
                        Thread.sleep(10 * (parent.getHeight() - this.getY())/ 4);
                        break;
                    case RIGHT:
                        AnimationUtil.jLabelXRight(this.getX(), parent.getWidth(), 10, 4, this);
                        Thread.sleep(10 * (parent.getWidth() -  this.getX())/ 4);
                        break;

                    case LEFT:
                        AnimationUtil.jLabelXLeft(this.getX(), - this.getWidth(), 10, 4, this);
                        Thread.sleep(10 * (this.getWidth() + this.getX())/ 4);
                        break;
                }

                this.setVisible(false);
            }

            catch (Exception e) {
               ErrorHandler.handle(e);
            }
        }).start();
    }

    /**
     * This method to be used with an already initialized component.
     * @param startDir - the direction for the notification to enter from.
     * @param parent - the component the notification is placed on. Used for bounds calculations.
     */
    public void appear(StartDirection startDir, Component parent) {
        new Thread(() -> {
            try {
                setVisible(true);

                int parentWidth = parent.getWidth();
                int notificationWidth = getWidth() / 2;

                switch(startDir) {
                    case TOP:
                        setBounds(getX(), - getHeight(), getWidth(), getHeight());
                        AnimationUtil.jLabelYDown(getY(), 30, 10, 4, this);
                        Thread.sleep(10 * (getHeight())/ 4);
                        break;
                    case RIGHT:
                        setBounds(parentWidth, getY(),getWidth(),getHeight());

                        for (int i = parentWidth ; i > parentWidth - notificationWidth - 35 ; i -= 4) {
                            setBounds(i,getY(),getWidth(),getHeight());
                            Thread.sleep(10);
                        }

                        break;

                    case LEFT:
                        setBounds(- getWidth(), getY(), getWidth(), getHeight());
                        AnimationUtil.jLabelXRight(this.getX(), 0, 10, 4, this);
                        Thread.sleep(10 * (notificationWidth)/ 4);
                        break;

                    case BOTTOM:
                        setBounds(getX(), parent.getHeight(), getWidth(), getHeight());
                        AnimationUtil.jLabelYUp(parent.getHeight(), parent.getHeight() - getHeight() , 10, 4, this);
                        Thread.sleep(10 * (getHeight())/ 4);
                        break;
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }).start();
    }

    public void kill() {
        this.setVisible(false);
    }
}