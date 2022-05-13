package cyder.genesis.objects;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;

import javax.swing.*;
import java.awt.*;

public class HarmonicRectangle extends JLabel {
    private int staticWidth;
    private int staticHeight;

    /**
     * The amount to increase or decrease the animation direction by.
     */
    private int animationInc = 1;
    private int currentWidth;
    private int currentHeight;

    private int delay = 50;

    private Color backgroundColor = CyderColors.vanila;

    private HarmonicDirection harmonicDirection = HarmonicDirection.VERTICAL;

    public enum HarmonicDirection {
        VERTICAL, HORIZONTAL
    }

    public HarmonicRectangle(int width, int height) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        staticWidth = width;
        staticHeight = height;
    }

    @Override
    public int getWidth() {
        return staticWidth;
    }

    public void setStaticWidth(int staticWidth) {
        Preconditions.checkArgument(staticWidth > 0);
        this.staticWidth = staticWidth;
    }

    @Override
    public int getHeight() {
        return staticHeight;
    }

    public void setStaticHeight(int staticHeight) {
        Preconditions.checkArgument(staticHeight > 0);
        this.staticHeight = staticHeight;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        Preconditions.checkArgument(delay > 0);
        this.delay = delay;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public HarmonicDirection getAlternationDirection() {
        return harmonicDirection;
    }

    public void setAlternationDirection(HarmonicDirection harmonicDirection) {
        this.harmonicDirection = harmonicDirection;
    }

    public int getAnimationInc() {
        return animationInc;
    }

    public void setAnimationInc(int animationInc) {
        Preconditions.checkArgument(animationInc > 0);
        this.animationInc = animationInc;
    }

    private boolean animate;

    public void startAnimation() {
        if (!animate) {
            animate = true;

            // todo extract lamba runnable
            CyderThreadRunner.submit(() -> {
                while (animate) {
                    try {
                        // todo add delta to thing
                        switch (harmonicDirection) {
                            case VERTICAL:


                                break;
                            case HORIZONTAL:
                                // todo need a way to figure out if going down or up currently

                                break;
                            default:
                                throw new IllegalStateException("Invalid harmonic direction: " + harmonicDirection);
                        }

                        Thread.sleep(delay);
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }
            }, "Harmonic Rectangle Animator");
        }
    }

    public void stopAnimation() {
        animate = false;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(backgroundColor);
        // todo this implies the anchor point is static, will need to be updated if
        //  animating the left or top sides are desired and not just bottom and right
        g2d.fillRect(getX(), getY(), currentWidth, currentHeight);


        g2d.dispose(); //todo is this a resource leak throughout Cyder where dispose calls are not used?
    }
}
