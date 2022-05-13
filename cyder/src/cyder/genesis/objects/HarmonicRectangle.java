package cyder.genesis.objects;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;

import javax.swing.*;
import java.awt.*;

public class HarmonicRectangle extends JLabel {
    private int currentWidth;
    private int currentHeight;

    private int staticMaxWidth;
    private int staticMaxHeight;
    private int staticMinWidth;
    private int staticMinHeight;

    /**
     * The amount to increase or decrease the animation direction by.
     */
    private int animationInc = 1;

    private int animationDelay = 50;

    private Color backgroundColor = CyderColors.vanila;

    private HarmonicDirection harmonicDirection = HarmonicDirection.VERTICAL;

    public enum HarmonicDirection {
        VERTICAL, HORIZONTAL
    }

    private DeltaDirection deltaDirection = DeltaDirection.INCREASING;

    public enum DeltaDirection {
        INCREASING, DECREASING
    }

    /**
     * Suppress default constructor.
     */
    private HarmonicRectangle() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    public HarmonicRectangle(int staticMinWidth, int staticMinHeight, int staticMaxWidth, int staticMaxHeight) {
        Preconditions.checkArgument(staticMaxWidth > 0);
        Preconditions.checkArgument(staticMaxHeight > 0);
        Preconditions.checkArgument(staticMinWidth > 0);
        Preconditions.checkArgument(staticMinHeight > 0);

        Preconditions.checkArgument(staticMinWidth <= staticMaxWidth);
        Preconditions.checkArgument(staticMinHeight <= staticMaxHeight);

        this.staticMaxWidth = staticMaxWidth;
        this.staticMaxHeight = staticMaxHeight;
        this.staticMinWidth = staticMinWidth;
        this.staticMinHeight = staticMinHeight;

        super.setSize(staticMinWidth, staticMinHeight);
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
        currentWidth = width;
        currentHeight = height;

        super.setSize(width, height);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        currentWidth = width;
        currentHeight = height;

        super.setBounds(x, y, width, height);
    }

    public int getAnimationDelay() {
        return animationDelay;
    }

    public void setAnimationDelay(int animationDelay) {
        Preconditions.checkArgument(animationDelay > 0);
        this.animationDelay = animationDelay;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public HarmonicDirection getHarmonicDirection() {
        return harmonicDirection;
    }

    public void setHarmonicDirection(HarmonicDirection harmonicDirection) {
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

            CyderThreadRunner.submit(animationRunnable, "Harmonic Rectangle Animator");
        }
    }

    private final Runnable animationRunnable = () -> {
        while (shouldAnimate) {
            try {
                switch (harmonicDirection) {
                    case HORIZONTAL:
                        switch (deltaDirection) {
                            case INCREASING:
                                if (currentWidth + animationInc < staticMaxWidth) {
                                    currentWidth += animationInc;
                                } else {
                                    currentWidth = staticMaxWidth;
                                    deltaDirection = DeltaDirection.DECREASING;
                                }

                                break;
                            case DECREASING:
                                if (currentWidth - animationInc > staticMinWidth) {
                                    currentWidth -= animationInc;
                                } else {
                                    currentWidth = staticMinWidth;
                                    deltaDirection = DeltaDirection.INCREASING;
                                }

                                break;
                            default:
                                throw new IllegalStateException("Invalid delta direction: " + deltaDirection);
                        }

                        break;
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
                                if (currentHeight - animationInc > staticMinHeight) {
                                    currentHeight -= animationInc;
                                } else {
                                    currentHeight = staticMinHeight;
                                    deltaDirection = DeltaDirection.INCREASING;
                                }

                                break;
                            default:
                                throw new IllegalStateException("Invalid delta direction: " + deltaDirection);
                        }

                        break;
                    default:
                        throw new IllegalStateException("Invalid harmonic direction: " + harmonicDirection);
                }

                setSize(currentWidth, currentHeight);
                revalidate();
                repaint();
                Thread.sleep(animationDelay);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    };

    public void stopAnimation() {
        shouldAnimate = false;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, currentWidth, currentHeight);
    }
}
