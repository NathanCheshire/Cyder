package cyder.handlers.external.audio;

import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.AudioUtil;
import cyder.utilities.FileUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The class to update the audio location label and progress bar.
 */
public class AudioLocationUpdater {
    /**
     * Whether this AudioLocationUpdater has been killed.
     */
    private boolean killed;

    /**
     * The label this AudioLocationUpdater should update.
     */
    private final JLabel effectLabel;

    /**
     * The total seconds of the audio file this location updater was given.
     */
    private int totalSeconds;

    /**
     * The number of seconds in to the audio file.
     */
    private int secondsIn;

    /**
     * Constructs a new audio location label to update for the provided progress bar.
     *
     * @param effectLabel      the label to update
     * @param currentFrameView the audio player's atomic reference to the current frame view
     */
    public AudioLocationUpdater(JLabel effectLabel,
                                AtomicReference<FrameView> currentFrameView, AtomicReference<File> currentAudioFile) {
        checkNotNull(effectLabel);
        checkNotNull(currentFrameView);

        this.effectLabel = effectLabel;

        if (currentFrameView.get() == FrameView.MINI) {
            return;
        }

        try {
            CyderThreadRunner.submit(() -> {
                // maybe there could be some placeholder text while ffprobe is getting the correct length
                effectLabel.setText("");

                Future<Integer> totalMillisFuture = AudioUtil.getMillis(currentAudioFile.get());

                File localAudioFile = currentAudioFile.get();

                while (!totalMillisFuture.isDone()) {
                    Thread.onSpinWait();
                }

                // if not the same file as when the future began, return
                if (localAudioFile != currentAudioFile.get()) {
                    return;
                }

                int totalMillis = 0;

                try {
                    totalMillis = totalMillisFuture.get();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                if (totalMillis == 0) {
                    return;
                }

                this.totalSeconds = Math.round(totalMillis / 1000.0f);

                while (!killed) {
                    if (!timerPaused) {
                        secondsIn++;
                        updateEffectLabel();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                    }
                }
            }, FileUtil.getFilename(currentAudioFile.get()) + " Progress Label Thread");
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        }
    }

    /**
     * Updates the encapsulated label with the time in to the current audio file.
     */
    private void updateEffectLabel() {
        int secondsLeft = totalSeconds - secondsIn;

        if (UserUtil.getCyderUser().getAudiolength().equals("1")) {
            effectLabel.setText(AudioUtil.formatSeconds(secondsIn)
                    + " played, " + AudioUtil.formatSeconds(totalSeconds) + " remaining");
        } else {
            effectLabel.setText(AudioUtil.formatSeconds(secondsIn)
                    + " played, " + AudioUtil.formatSeconds(secondsLeft) + " remaining");
        }
    }

    // todo need to add pausing and resuming from audio player
    // should also update every 50 ms

    /**
     * Whether the seconds in value should be updated.
     */
    private boolean timerPaused;

    /**
     * Ends the updation of the label text.
     */
    public void kill() {
        killed = true;
    }

    /**
     * Stops incrementing the seconds in value.
     */
    public void pauseTimer() {
        timerPaused = true;
    }

    /**
     * Starts incrementing the seconds in value.
     */
    public void resumeTimer() {
        timerPaused = false;
    }
}
