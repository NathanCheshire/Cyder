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
     * The delay between update cycles for the audio location text.
     */
    private static final int AUDIO_LOCATION_TEXT_UPDATE_DELAY = 1000;

    /**
     * Whether this AudioLocationUpdater has been killed.
     */
    private boolean killed;

    /**
     * The label this AudioLocationUpdater should update.
     */
    private final JLabel effectLabel;

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

                int totalSeconds = Math.round(totalMillis / 1000.0f);

                String formattedTotal = AudioUtil.formatSeconds(totalSeconds);

                while (!killed) {
                    updateEffectLabel(totalSeconds, formattedTotal);

                    try {
                        Thread.sleep(AUDIO_LOCATION_TEXT_UPDATE_DELAY);
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
     *
     * @param totalSeconds   the total seconds of the audio file
     * @param formattedTotal the formatted string of the total seconds
     */
    private void updateEffectLabel(int totalSeconds, String formattedTotal) {
        float percentIn = 0;
        // todo figure this out and how to smoothly move up by a second at a time

        int secondsIn = (int) Math.ceil(percentIn * totalSeconds);
        int secondsLeft = totalSeconds - secondsIn;

        if (UserUtil.getCyderUser().getAudiolength().equals("1")) {
            effectLabel.setText(AudioUtil.formatSeconds(secondsIn)
                    + " played, " + formattedTotal + " remaining");
        } else {
            effectLabel.setText(AudioUtil.formatSeconds(secondsIn)
                    + " played, " + AudioUtil.formatSeconds(secondsLeft) + " remaining");
        }
    }

    /**
     * Ends the updation of the label text.
     */
    public void kill() {
        killed = true;
    }
}
