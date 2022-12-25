package cyder.audio;

import com.google.common.base.Preconditions;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to control the visibility of the audio volume level label.
 */
public class AudioVolumeLabelAnimator {
    /**
     * Whether this object has been killed.
     */
    private boolean killed;

    /**
     * The time remaining before setting the visibility of the audio volume label to false.
     */
    private static final AtomicInteger audioVolumeLabelTimeout = new AtomicInteger();

    /**
     * The time in between checks when sleeping before the audio volume label is set to invisible.
     */
    private static final int AUDIO_VOLUME_LABEL_SLEEP_TIME = 50;

    /**
     * The total sleep time before setting the audio volume label to invisible.
     */
    private static final int MAX_AUDIO_VOLUME_LABEL_VISIBLE = 3000;

    /**
     * The thread name of the audio progress label animator.
     */
    private static final String AUDIO_PROGRESS_LABEL_ANIMATOR = "Audio Progress Label Animator";

    /**
     * The label to display the audio progress on when needed.
     */
    private final JLabel audioVolumePercentLabel;

    /**
     * Constructs a new AudioVolumeLabelAnimator.
     */
    public AudioVolumeLabelAnimator(JLabel audioVolumePercentLabel) {
        Preconditions.checkNotNull(audioVolumePercentLabel);

        this.audioVolumePercentLabel = audioVolumePercentLabel;

        startThread();
    }

    /**
     * Resets the timeout before the label is set to be invisible.
     */
    public void resetTimeout() {
        audioVolumeLabelTimeout.set(MAX_AUDIO_VOLUME_LABEL_VISIBLE + AUDIO_VOLUME_LABEL_SLEEP_TIME);
    }

    /**
     * Kills this object.
     */
    public void kill() {
        killed = true;
    }

    /**
     * Starts the worker thread to show/hide the audio volume percent label.
     */
    private void startThread() {
        CyderThreadRunner.submit(() -> {
            while (!killed) {
                while (audioVolumeLabelTimeout.get() > 0) {
                    audioVolumePercentLabel.setVisible(true);
                    ThreadUtil.sleep(AUDIO_VOLUME_LABEL_SLEEP_TIME);

                    if (!killed) {
                        audioVolumeLabelTimeout.getAndAdd(-AUDIO_VOLUME_LABEL_SLEEP_TIME);
                    }
                }

                audioVolumePercentLabel.setVisible(false);
            }
        }, AUDIO_PROGRESS_LABEL_ANIMATOR);
    }
}
