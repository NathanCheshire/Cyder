package cyder.audio.player;

import com.google.common.base.Preconditions;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A class to control the visibility of the audio volume level label and perform animations.
 */
public final class AudioVolumeLabelAnimator {
    /**
     * The thread name for the thread which waits for the proper time to pass before invoking the animate out method.
     */
    private static final String audioVolumeLabelAnimatorAnimateOutWaiterThreadName =
            "Audio Volume Label Animator Animate Out Waiter";

    /**
     * The thread name for the animate in thread.
     */
    private static final String animateInAudioVolumeLabelThreadName = "Animate In Audio Volume Label";

    /**
     * The thread name for the animate out thread.
     */
    private static final String animateOutAudioVolumeLabelThreadName = "Animate Out Audio Volume Label";

    /**
     * The minimum size a font can be during an animation.
     * For the animate in animation, this is the starting font size,
     * for the animate out animation, this is the ending font size.
     */
    private static final int minFontSize = 0;

    /**
     * The animation step for changing the font size, both increasing and decreasing.
     */
    private static final int animationStep = 2;

    /**
     * The time between animation increments.
     */
    private static final Duration animationSleepTime = Duration.ofMillis(10);

    /**
     * The time between condition checks for the audio volume label animator waiter thread.
     */
    private static final Duration waitToAnimateOutSleepTime = Duration.ofMillis(50);

    /**
     * The maximum time the audio volume percent label can remain visible for before an animate out call.
     */
    private static final Duration maxVisibleTime = Duration.ofSeconds(3);

    /**
     * Whether the animate in animation is currently underway.
     */
    private final AtomicBoolean animatingIn = new AtomicBoolean();

    /**
     * Whether the animate out animation is currently underway.
     */
    private final AtomicBoolean animatingOut = new AtomicBoolean();

    /**
     * Whether the animate out waiter thread is currently running.
     */
    private final AtomicBoolean animateOutWaiterRunning = new AtomicBoolean();

    /**
     * The time remaining before the label is animated out.
     */
    private final AtomicLong millisUntilAnimateOut = new AtomicLong(maxVisibleTime.toMillis());

    /**
     * The label to display the audio progress on when needed.
     */
    private final JLabel audioVolumePercentLabel;

    /**
     * Whether this audio volume label animator has been killed.
     */
    private boolean killed;

    /**
     * Constructs a new AudioVolumeLabelAnimator.
     *
     * @param audioVolumePercentLabel the label to animate
     */
    public AudioVolumeLabelAnimator(JLabel audioVolumePercentLabel) {
        Preconditions.checkNotNull(audioVolumePercentLabel);

        this.audioVolumePercentLabel = audioVolumePercentLabel;
    }

    /**
     * The actions to invoke when the slider is changed.
     */
    public void onValueChanged() {
        if (!audioVolumePercentLabel.isVisible()) {
            if (animatingIn.get()) return;
            animateIn();
        } else {
            millisUntilAnimateOut.set(maxVisibleTime.toMillis());
            if (!animateOutWaiterRunning.get()) {
                animateOutWaiterRunning.set(true);
                CyderThreadRunner.submit(() -> {
                    while (millisUntilAnimateOut.get() > 0) {
                        millisUntilAnimateOut.getAndAdd(-waitToAnimateOutSleepTime.toMillis());
                        ThreadUtil.sleep(waitToAnimateOutSleepTime.toMillis());
                    }
                    animateOut();
                }, audioVolumeLabelAnimatorAnimateOutWaiterThreadName);
            }
        }
    }

    /**
     * Kills this object.
     */
    public void kill() {
        killed = true;
        animatingIn.set(false);
        animatingOut.set(false);
    }

    /**
     * Performs the animate in animation.
     */
    private void animateIn() {
        if (animatingIn.get() || killed) return;
        animatingIn.set(true);

        CyderThreadRunner.submit(() -> {
            audioVolumePercentLabel.setVisible(true);
            Font font = audioVolumePercentLabel.getFont();
            int animateToFontSize = font.getSize();
            for (int fontSize = minFontSize ; fontSize <= animateToFontSize ; fontSize += animationStep) {
                if (!animatingIn.get() || killed) return;
                audioVolumePercentLabel.setFont(font.deriveFont((float) fontSize));
                ThreadUtil.sleep(animationSleepTime.toMillis());
            }

            audioVolumePercentLabel.setFont(font);
            animatingIn.set(false);
            millisUntilAnimateOut.set(maxVisibleTime.toMillis());
        }, animateInAudioVolumeLabelThreadName);
    }

    /**
     * Performs the animate out animation.
     */
    private void animateOut() {
        if (animatingOut.get() || killed) return;
        animatingOut.set(true);

        CyderThreadRunner.submit(() -> {
            Font font = audioVolumePercentLabel.getFont();
            for (int fontSize = font.getSize() ; fontSize >= minFontSize ; fontSize -= animationStep) {
                if (!animatingOut.get() || killed) return;
                audioVolumePercentLabel.setFont(font.deriveFont((float) fontSize));
                ThreadUtil.sleep(animationSleepTime.toMillis());
            }

            audioVolumePercentLabel.setVisible(false);
            audioVolumePercentLabel.setFont(font);
            animatingOut.set(false);
            animateOutWaiterRunning.set(false);
        }, animateOutAudioVolumeLabelThreadName);
    }
}
