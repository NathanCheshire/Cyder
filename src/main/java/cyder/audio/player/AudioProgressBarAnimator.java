package cyder.audio.player;

import com.google.common.base.Preconditions;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.slider.CyderSliderUi;

import javax.swing.*;

/**
 * An encapsulation class for incrementing the audio progress bar's animation increment.
 */
final class AudioProgressBarAnimator {
    /**
     * The delay between update calls while the animation is in the {@link State#RUNNING} state.
     */
    private static final int animationDelay = 2;

    /**
     * The possible states for this animator.
     */
    public enum State {
        /**
         * Not running; the animation will not update.
         */
        STOPPED,

        /**
         * Running; the animation is updated every {@link #animationDelay}ms.
         */
        RUNNING,

        /**
         * The animation is paused and can be resumed from the current point.
         */
        PAUSED,
    }

    /**
     * The slider this progress bar animator controls.
     */
    private final JSlider slider;

    /**
     * The ui belonging to the slider.
     */
    private final CyderSliderUi sliderUi;

    /**
     * The current state of the animator.
     */
    private State state = State.STOPPED;

    /**
     * Constructs a new AudioProgressBarAnimator object.
     *
     * @param slider   the JSlider to animate
     * @param sliderUi the ui of the slider to update the animation
     */
    public AudioProgressBarAnimator(JSlider slider, CyderSliderUi sliderUi) {
        this.slider = Preconditions.checkNotNull(slider);
        this.sliderUi = Preconditions.checkNotNull(sliderUi);
    }

    /**
     * Returns the delay between animation updates.
     *
     * @return the delay between animation updates
     */
    public int getAnimationDelay() {
        return animationDelay;
    }

    /**
     * Returns the slider this progress bar animator controls.
     *
     * @return the slider this progress bar animator controls
     */
    public JSlider getSlider() {
        return slider;
    }

    /**
     * Returns the ui of the slider this progress bar animator controls.
     *
     * @return the ui of the slider this progress bar animator controls
     */
    public CyderSliderUi getSliderUi() {
        return sliderUi;
    }

    /**
     * Returns the current state of this progress bar animator.
     *
     * @return the current state of this progress bar animator
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the current state of this progress bar animator.
     *
     * @param state the current state of this progress bar animator
     */
    public void setState(State state) {
        Preconditions.checkNotNull(state);
        if (this.state == state) return;

        this.state = state;

        switch (state) {
            case STOPPED -> sliderUi.resetAnimation();
            case RUNNING -> startAnimation();
            case PAUSED -> {}
            default -> throw new IllegalArgumentException("Invalid Animation State: " + state);
        }
    }

    /**
     * Starts the animation of the controlled slider.
     * The animation proceeds until {@link #state} is set to a value other than {@link State#RUNNING}.
     */
    private void startAnimation() {
        String threadName = "Audio Location Slider Animation Updater, slider=" + slider + ", ui" + sliderUi;
        CyderThreadRunner.submit(() -> {
            while (state == State.RUNNING) {
                sliderUi.incrementAnimation();
                slider.repaint();
                ThreadUtil.sleep(animationDelay);
            }
        }, threadName);
    }
}
