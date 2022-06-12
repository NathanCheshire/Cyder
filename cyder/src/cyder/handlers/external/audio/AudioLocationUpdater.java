package cyder.handlers.external.audio;

import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.AudioUtil;
import cyder.utilities.FileUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
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
     * The current frame view the audio player is in.
     */
    private final AtomicReference<FrameView> currentFrameView;

    /**
     * The current audio file of the audio player.
     */
    private final AtomicReference<File> currentAudioFile;

    /**
     * Whether the slider is currently under a mouse pressed event.
     */
    private final AtomicBoolean sliderPressed;

    /**
     * The slider value to update.
     */
    private final JSlider slider;

    /**
     * The total milliseconds of the audio file this location updater was given.
     */
    private long totalMilliSeconds;

    /**
     * The number of milliseconds in to the audio file.
     */
    private long milliSecondsIn;

    /**
     * The number of times a second to update the audio progress label.
     */
    private static final int UPDATES_PER_SECOND = 30;

    /**
     * The amount by which to sleep and increment for.
     */
    private static final int UPDATE_DELAY = 1000 / UPDATES_PER_SECOND;

    /**
     * Constructs a new audio location label to update for the provided progress bar.
     *
     * @param effectLabel      the label to update
     * @param currentFrameView the audio player's atomic reference to the current frame view
     * @param currentAudioFile the audio player's current audio file
     * @param sliderPressed    whether the provided slider is currently under a mouse pressed event
     */
    public AudioLocationUpdater(JLabel effectLabel, AtomicReference<FrameView> currentFrameView,
                                AtomicReference<File> currentAudioFile, AtomicBoolean sliderPressed,
                                JSlider slider) {
        checkNotNull(effectLabel);
        checkNotNull(currentFrameView);
        checkNotNull(sliderPressed);
        checkNotNull(slider);

        this.effectLabel = effectLabel;
        this.currentFrameView = currentFrameView;
        this.currentAudioFile = currentAudioFile;
        this.sliderPressed = sliderPressed;
        this.slider = slider;

        startUpdateThread();
    }

    /**
     * Starts the thread to update the inner label
     */
    private void startUpdateThread() {
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

            this.totalMilliSeconds = totalMillis;

            while (!killed) {
                try {
                    Thread.sleep(UPDATE_DELAY);

                    if (!timerPaused) {
                        milliSecondsIn += UPDATE_DELAY;
                    }
                } catch (Exception ignored) {
                }

                if (!timerPaused && currentFrameView.get() != FrameView.MINI) {
                    updateEffectLabel((int) (Math.floor(milliSecondsIn / 1000.0)));

                    if (!sliderPressed.get()) {
                        updateSlider();
                    }
                }
            }
        }, FileUtil.getFilename(currentAudioFile.get()) + " Progress Label Thread");
    }

    /**
     * Whether the seconds in value should be updated.
     */
    private boolean timerPaused = true;

    /**
     * Ends the updation of the label text.
     */
    public void kill() {
        killed = true;
    }

    /**
     * Stops incrementing the secondsIn value.
     */
    public void pauseTimer() {
        timerPaused = true;
    }

    /**
     * Starts incrementing the secondsIn value.
     */
    public void resumeTimer() {
        timerPaused = false;
    }

    /**
     * Updates the encapsulated label with the time in to the current audio file.
     */
    private void updateEffectLabel(int secondsIn) {
        long milliSecondsLeft = totalMilliSeconds - secondsIn * 1000L;
        int secondsLeft = (int) (milliSecondsLeft / 1000);

        if (UserUtil.getCyderUser().getAudiolength().equals("1")) {
            effectLabel.setText(AudioUtil.formatSeconds(secondsIn)
                    + " played, " + AudioUtil.formatSeconds((int) (totalMilliSeconds / 1000.0)) + " remaining");
        } else {
            effectLabel.setText(AudioUtil.formatSeconds(secondsIn)
                    + " played, " + AudioUtil.formatSeconds(secondsLeft) + " remaining");
        }
    }

    /**
     * Updates the reference slider's value.
     */
    private void updateSlider() {
        float percentIn = (float) milliSecondsIn / totalMilliSeconds;
        slider.setValue(Math.round(percentIn * slider.getMaximum()));
    }

    /**
     * Sets the percent in to the current audio.
     *
     * @param percentIn the percent in to the current audio
     */
    public void setPercentIn(float percentIn) {
        this.milliSecondsIn = (int) (totalMilliSeconds * percentIn);
    }
}
