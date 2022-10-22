package cyder.animation;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

/**
 * A simple rectangle which can grow and shrink for a simple animation.
 */
public class HarmonicRectangle extends JLabel {
    /**
     * The component's current width.
     */
    private int currentWidth;

    /**
     * The component's current height.
     */
    private int currentHeight;

    /**
     * The maximum allowable width for the component.
     */
    private final int staticMaxWidth;

    /**
     * The maximum allowable height for the component.
     */
    private final int staticMaxHeight;

    /**
     * The minimum allowable height for the component.
     */
    private final int staticMinWidth;

    /**
     * The minimum allowable width for the component.
     */
    private final int staticMinHeight;

    /**
     * The amount to increase or decrease the animation direction by.
     */
    private int animationInc = 1;

    /**
     * The delay between animation updates.
     */
    private int animationDelay = 50;

    /**
     * The background color of the drawn component.
     */
    private Color backgroundColor = CyderColors.vanilla;

    /**
     * The current direction of harmonic oscillation.
     */
    private HarmonicDirection harmonicDirection = HarmonicDirection.VERTICAL;

    /**
     * The position directions of harmonic oscillation.
     */
    public enum HarmonicDirection {
        /**
         * The rectangle will oscillate in the vertical direction.
         */
        VERTICAL,

        /**
         * The rectangle will oscillate in the horizontal direction.
         */
        HORIZONTAL
    }

    /**
     * The current state of the harmonic direction.
     */
    private DeltaDirection deltaDirection = DeltaDirection.INCREASING;

    /**
     * The possible states of the harmonic direction.
     */
    public enum DeltaDirection {
        /**
         * The rectangle is currently increasing.
         */
        INCREASING,

        /**
         * The rectangle is currently decreasing.
         */
        DECREASING
    }

    /**
     * The wave equation to use for this harmonic rectangle if synchronization to other rectangles is needed.
     * This is commonly used when multiple rectangles have different phases.
     */
    private Function<Float, Double> waveEquation;

    /**
     * Suppress default constructor.
     */
    private HarmonicRectangle() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Constructs a new harmonic rectangle.
     *
     * @param staticMinWidth  the minimum width
     * @param staticMinHeight the minimum height
     * @param staticMaxWidth  the maximum width
     * @param staticMaxHeight the maximum height
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return currentWidth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return currentHeight;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(int width, int height) {
        currentWidth = width;
        currentHeight = height;

        super.setSize(width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        currentWidth = width;
        currentHeight = height;

        super.setBounds(x, y, width, height);
    }

    /**
     * Returns the delay between animation frame updates.
     *
     * @return the delay between animation frame updates
     */
    public int getAnimationDelay() {
        return animationDelay;
    }

    /**
     * Sets the delay between animation frame updates.
     *
     * @param animationDelay the delay between animation frame updates
     */
    public void setAnimationDelay(int animationDelay) {
        Preconditions.checkArgument(animationDelay > 0);
        this.animationDelay = animationDelay;
    }

    /**
     * Returns the background color.
     *
     * @return the background color
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color.
     *
     * @param backgroundColor the background color
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = Preconditions.checkNotNull(backgroundColor);
    }

    /**
     * Returns the animation to oscillate in.
     *
     * @return the animation to oscillate in
     */
    public HarmonicDirection getHarmonicDirection() {
        return harmonicDirection;
    }

    /**
     * Sets the direction to oscillate in.
     *
     * @param harmonicDirection the direction to oscillate in
     */
    public void setHarmonicDirection(HarmonicDirection harmonicDirection) {
        this.harmonicDirection = Preconditions.checkNotNull(harmonicDirection);
    }

    /**
     * Returns the increment to increase/decrease the animation side by.
     *
     * @return the increment to increase/decrease the animation side by
     */
    public int getAnimationInc() {
        return animationInc;
    }

    /**
     * Sets the increment to increase/decrease the animation side by.
     *
     * @param animationInc the increment to increase/decrease the animation side by
     */
    public void setAnimationInc(int animationInc) {
        Preconditions.checkArgument(animationInc > 0);
        this.animationInc = animationInc;
    }

    /**
     * Whether the rectangle is animating currently.
     */
    private boolean isAnimating;

    /**
     * Returns whether the rectangle is currently in animation.
     *
     * @return whether the rectangle is currently in animation
     */
    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * The default animation thread name.
     */
    private static final String DEFAULT_ANIMATION_THREAD_NAME = "Harmonic Rectangle Animator";

    /**
     * Starts the animation.
     */
    public void startAnimation() {
        startAnimation(DEFAULT_ANIMATION_THREAD_NAME);

    }

    /**
     * Starts the animation with the provided name as the thread name.
     *
     * @param threadName the name of the thread for animation of this rectangle.
     * @throws IllegalStateException if the rectangle has already started animating
     */
    public void startAnimation(String threadName) {
        Preconditions.checkNotNull(threadName);
        Preconditions.checkArgument(!threadName.isEmpty());

        if (isAnimating) throw new IllegalStateException("Rectangle already animating");
        isAnimating = true;
        CyderThreadRunner.submit(animationRunnable, threadName);
    }

    /**
     * The animation runnable used to automatically animate the rectangle.
     */
    private final Runnable animationRunnable = () -> {
        while (isAnimating) {
            animationStep();
            ThreadUtil.sleep(animationDelay);
        }
    };

    /**
     * Takes an animation step.
     */
    private void animationStep() {
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
    }

    /**
     * Stops the animation of on-going.
     */
    public void stopAnimation() {
        isAnimating = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, currentWidth, currentHeight);
    }

    /**
     * Returns the wave equation for this harmonic rectangle
     *
     * @return the wave equation for this harmonic rectangle
     */
    public Function<Float, Double> getWaveEquation() {
        return waveEquation;
    }

    /**
     * Sets the wave equation for this harmonic rectangle.
     * The standard form of a simple wave equation is as follows:
     *
     * <pre>
     * y_value = amplitude * Math.sin(period * (x_value + phase_shift)) + vertical_shift
     * <pre/>
     *
     * @param waveEquation the wave equation for this harmonic rectangle
     */
    public void setWaveEquation(Function<Float, Double> waveEquation) {
        this.waveEquation = Preconditions.checkNotNull(waveEquation);
    }

    /**
     * Calculates the result of this harmonic rectangles wave equation at the time "t".
     *
     * @param t the time to evaluate the wave equation at
     * @return the wave equation evaluated at time t
     */
    public double calculateWaveAtPoint(float t) {
        return waveEquation.apply(t);
    }

    /**
     * Constructs and returns a function used to calculate a wave in the following form:
     *
     * <pre>
     * y_value = amplitude * Math.sin(period * (x_value + phase_shift)) + vertical_shift
     * <pre/>
     *
     * @param amplitude the amplitude of the wave
     * @param period the period of the wave
     * @param phaseShift the phase shift (left/right) of the wave
     * @param verticalShift the vertical shift of the wave
     * @return the wave function
     */
    public static Function<Float, Double> constructWaveEquation(float amplitude, float period,
                                                                float phaseShift, float verticalShift) {
        return inputFloat -> amplitude * Math.sin(period * (inputFloat + phaseShift)) + verticalShift;
    }
}
