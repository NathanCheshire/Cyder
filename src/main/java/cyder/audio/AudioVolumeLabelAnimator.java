package cyder.audio;

import com.google.common.base.Preconditions;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A class to control the visibility of the audio volume level label.
 */
public class AudioVolumeLabelAnimator {
    /**
     * The label to display the audio progress on when needed.
     */
    private final JLabel audioVolumePercentLabel;

    /**
     * Constructs a new AudioVolumeLabelAnimator.
     *
     * @param audioVolumePercentLabel the label to animate
     */
    public AudioVolumeLabelAnimator(JLabel audioVolumePercentLabel) {
        Preconditions.checkNotNull(audioVolumePercentLabel);

        this.audioVolumePercentLabel = audioVolumePercentLabel;
    }

    private final int maxVisibleTime = 3000;
    private final AtomicLong millisUntilAnimateOut = new AtomicLong(maxVisibleTime);
    private final int waitToAnimateOutSleepTime = 50;

    /**
     * The actions to invoke when the slider is changed.
     */
    public void onValueChanged() {
        if (!audioVolumePercentLabel.isVisible()) {
            if (animatingIn.get()) return;
            animateIn();
        } else {
            millisUntilAnimateOut.set(maxVisibleTime);
            if (!animateOutWaiterRunning.get()) {
                animateOutWaiterRunning.set(true);
                CyderThreadRunner.submit(() -> {
                    while (millisUntilAnimateOut.get() > 0) {
                        millisUntilAnimateOut.getAndAdd(-waitToAnimateOutSleepTime);
                        ThreadUtil.sleep(waitToAnimateOutSleepTime);
                    }
                    animateOut();
                }, "Audio Volume Label Animator Animate Out Waiter");
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

    private boolean killed;
    private final AtomicBoolean animatingIn = new AtomicBoolean();
    private final AtomicBoolean animatingOut = new AtomicBoolean();
    private final AtomicBoolean animateOutWaiterRunning = new AtomicBoolean();
    private static final int animationStep = 2;
    private static final Duration animationSleepTime = Duration.ofMillis(10);

    private synchronized void animateIn() {
        if (animatingIn.get() || killed) return;
        animatingIn.set(true);

        CyderThreadRunner.submit(() -> {
            audioVolumePercentLabel.setVisible(true);
            Font font = audioVolumePercentLabel.getFont();
            int animateToFontSize = font.getSize();
            for (int fontSize = 0 ; fontSize <= animateToFontSize ; fontSize += animationStep) {
                if (!animatingIn.get() || killed) return;
                audioVolumePercentLabel.setFont(font.deriveFont((float) fontSize));
                ThreadUtil.sleep(animationSleepTime.toMillis());
            }

            audioVolumePercentLabel.setFont(font);
            animatingIn.set(false);
            millisUntilAnimateOut.set(maxVisibleTime);
        }, "Animate In Audio Volume Label");
    }

    private synchronized void animateOut() {
        if (animatingOut.get() || killed) return;
        animatingOut.set(true);

        CyderThreadRunner.submit(() -> {
            Font font = audioVolumePercentLabel.getFont();
            for (int fontSize = font.getSize() ; fontSize >= 0 ; fontSize -= animationStep) {
                if (!animatingOut.get() || killed) return;
                audioVolumePercentLabel.setFont(font.deriveFont((float) fontSize));
                ThreadUtil.sleep(animationSleepTime.toMillis());
            }

            audioVolumePercentLabel.setVisible(false);
            audioVolumePercentLabel.setFont(font);
            animatingOut.set(false);
            animateOutWaiterRunning.set(false);
        }, "Animate Out Audio Volume Label");
    }
}
