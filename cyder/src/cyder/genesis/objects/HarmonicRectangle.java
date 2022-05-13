package cyder.genesis.objects;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;

import javax.swing.*;
import java.awt.*;

public class HarmonicRectangle extends JLabel {
    private int currentWidth;
    private int currentHeight;

    private final int staticMaxWidth;
    private final int staticMaxHeight;

    private final int staticMinWidth;
    private final int staticMinHeight;

    /**
     * The amount to increase or decrease the animation direction by.
     */
    private int animationInc = 1;

    private int delay = 50;

    private Color backgroundColor = CyderColors.vanila;

    private HarmonicDirection harmonicDirection = HarmonicDirection.VERTICAL;

    public enum HarmonicDirection {
        VERTICAL, HORIZONTAL
    }

    private DeltaDirection deltaDirection = DeltaDirection.INCREASING;

    public enum DeltaDirection {
        INCREASING, DECREASING
    }

    public HarmonicRectangle(int staticMinWidth, int staticMinHeight, int staticMaxWidth, int staticMaxHeight) {
        Preconditions.checkArgument(staticMaxWidth > 0);
        Preconditions.checkArgument(staticMaxHeight > 0);

        Preconditions.checkArgument(staticMinWidth > 0);
        Preconditions.checkArgument(staticMinHeight > 0);

        Preconditions.checkArgument(staticMinWidth < staticMaxWidth);
        Preconditions.checkArgument(staticMinHeight < staticMaxHeight);

        this.staticMaxWidth = staticMaxWidth;
        this.staticMaxHeight = staticMaxHeight;
        this.staticMinWidth = staticMinWidth;
        this.staticMinHeight = staticMinHeight;
    }

    @Override
    public int getWidth() {
        return currentWidth;
    }

    @Override
    public int getHeight() {
        return currentHeight;
    }

    @Override
    public void setSize(int width, int height) {
        throw new IllegalMethodException("Illegal method; if invoking inside of class, use super modifier");
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        throw new IllegalMethodException("Illegal method; if invoking inside of class," +
                " use super modifier. If attempting to set anchor point, invoke setSize()");
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

    private boolean shouldAnimate;

    public void startAnimation() {
        if (!shouldAnimate) {
            shouldAnimate = true;

            // todo extract lamba runnable
            CyderThreadRunner.submit(() -> {
                while (shouldAnimate) {
                    try {
                        // todo add delta to thing
                        switch (harmonicDirection) {
                            case VERTICAL:
                                switch (deltaDirection) {
                                    case INCREASING:
                                        if (currentHeight + animationInc < staticMaxHeight) {
                                            currentHeight += animationInc;
                                        } else {
                                            currentHeight = staticMaxHeight;
                                            deltaDirection = DeltaDirection.DECREASING;
                                        }

                                        break;
                                    case DECREASING:
                                        if (currentHeight - animationInc > 0) {
                                            currentHeight -= animationInc;
                                        } else {
                                            currentHeight = 0;
                                        }

                                        break;
                                    default:
                                        throw new IllegalStateException("Invalid delta direction: " + deltaDirection);
                                }

                                break;
                            case HORIZONTAL:

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
        shouldAnimate = false;
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
