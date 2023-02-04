package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class to control the visibility of the audio volume level label.
 */
public class AudioVolumeLabelAnimator {
    /**
     * The total sleep time before setting the audio volume label to invisible.
     */
    private static final Duration maxLabelVisibleTime = Duration.ofSeconds(3);

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

    /**
     * The actions to invoke when the slider is changed.
     */
    public void onValueChanged() {
        if (!audioVolumePercentLabel.isVisible()) {
            animatingOut.set(false);
            animateIn();
        } else {
            if (waitToAnimateOutFuture != null) {
                waitToAnimateOutFuture.cancel(true);
            }

            waitToAnimateOutFuture = Futures.submit(() -> {
                ThreadUtil.sleep(maxLabelVisibleTime.toMillis());
                animateOut();
            }, Executors.newSingleThreadExecutor(new CyderThreadFactory("name")));
        }
    }

    /**
     * Kills this object.
     */
    public void kill() {
        animatingIn.set(false);
        animatingOut.set(false);
        if (waitToAnimateOutFuture != null) waitToAnimateOutFuture.cancel(true);
    }

    private final AtomicBoolean animatingIn = new AtomicBoolean();
    private final AtomicBoolean animatingOut = new AtomicBoolean();
    private ListenableFuture<Void> waitToAnimateOutFuture;

    private void animateIn() {
        if (animatingIn.get()) return;
        animatingIn.set(true);

        CyderThreadRunner.submit(() -> {
            audioVolumePercentLabel.setVisible(true);
            Font font = audioVolumePercentLabel.getFont();
            int animateToFontSize = font.getSize();
            for (int i = 0 ; i <= animateToFontSize ; i += 2) {
                if (!animatingIn.get()) return;
                audioVolumePercentLabel.setFont(font.deriveFont((float) i));
                ThreadUtil.sleep(10);
            }

            audioVolumePercentLabel.setFont(font);
        }, "Animate In Audio Volume Label");
    }

    private void animateOut() {
        if (animatingOut.get()) return;
        animatingOut.set(true);

        CyderThreadRunner.submit(() -> {
            //            Font font = audioVolumePercentLabel.getFont();
            //            for (int i = font.getSize() ; i >= 0 ; i--) {
            //                if (!animatingOut.get()) return;
            //                audioVolumePercentLabel.setFont(font.deriveFont((float) i));
            //                ThreadUtil.sleep(50);
            //            }
            //
            //            audioVolumePercentLabel.setVisible(false);
            //            audioVolumePercentLabel.setFont(font);
        }, "Animate Out Audio Volume Label");
    }
}
