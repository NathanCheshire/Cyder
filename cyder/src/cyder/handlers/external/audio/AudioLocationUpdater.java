package cyder.handlers.external.audio;

import cyder.threads.CyderThreadRunner;
import cyder.utilities.FileUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.io.File;
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
     * The label this AudioLocationUpdater should update to display the seconds in.
     */
    private final JLabel secondsInLabel;

    /**
     * The label this AudioLocationUpdater should update to display the seconds remaining.
     */
    private final JLabel secondsLeftLabel;

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
    private final long totalMilliSeconds;

    /**
     * The number of milliseconds in to the audio file.
     */
    private long milliSecondsIn;

    /**
     * The number of times to update the labels and slider per second.
     */
    private static final int UPDATES_PER_SECOND = 10;

    /**
     * The amount by which to sleep  for.
     */
    private static final int UPDATE_DELAY = 1000 / UPDATES_PER_SECOND;

    /**
     * Constructs a new audio location label to update for the provided progress bar.
     *
     * @param secondsInLabel   the label to display how many seconds of the audio has played
     * @param secondsLeftLabel the label to display how many seconds are remaining
     * @param currentFrameView the audio player's atomic reference to the current frame view
     * @param currentAudioFile the audio player's current audio file
     * @param sliderPressed    whether the provided slider is currently under a mouse pressed event
     */
    public AudioLocationUpdater(JLabel secondsInLabel, JLabel secondsLeftLabel,
                                AtomicReference<FrameView> currentFrameView,
                                AtomicReference<File> currentAudioFile, AtomicBoolean sliderPressed,
                                JSlider slider) {
        checkNotNull(secondsInLabel);
        checkNotNull(secondsLeftLabel);
        checkNotNull(currentFrameView);
        checkNotNull(sliderPressed);
        checkNotNull(slider);

        this.secondsInLabel = secondsInLabel;
        this.secondsLeftLabel = secondsLeftLabel;
        this.currentFrameView = currentFrameView;
        this.currentAudioFile = currentAudioFile;
        this.sliderPressed = sliderPressed;
        this.slider = slider;

        this.totalMilliSeconds = AudioUtil.getMillisFast(currentAudioFile.get());

        setupProps();
    }

    /**
     * Determines the audio total length and updates the label in preparation for the update thread to start.
     */
    private void setupProps() {
        secondsInLabel.setText("");
        secondsLeftLabel.setText("");
        slider.setValue(0);

        updateEffectLabel((int) (Math.floor(milliSecondsIn / 1000.0)));

        if (!sliderPressed.get()) {
            updateSlider();
        }

        startUpdateThread();
    }

    /**
     * Whether the update thread has been started yet.
     */
    private boolean started;

    /**
     * Starts the thread to update the inner label
     *
     * @throws IllegalStateException if this method has already been invoked
     */
    private void startUpdateThread() {
        if (started) {
            throw new IllegalStateException("Update thread already started");
        }

        started = true;

        CyderThreadRunner.submit(() -> {
            while (!killed) {
                try {
                    Thread.sleep(UPDATE_DELAY);
                } catch (Exception ignored) {}

                this.milliSecondsIn = AudioPlayer.getMillisecondsIn();

                if (!timerPaused && currentFrameView.get() != FrameView.MINI) {
                    if (!sliderPressed.get()) {
                        updateEffectLabel((int) (Math.floor(milliSecondsIn / 1000.0)));
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
     * Forces an update of both labels and the slider.
     */
    public void update() {
        updateEffectLabel((int) (Math.floor(milliSecondsIn / 1000.0)));
        updateSlider();
    }

    /**
     * The value passed to the updateEffectLabel method last.
     */
    private int lastSecondsIn;

    /**
     * Updates the encapsulated label with the time in to the current audio file.
     */
    private void updateEffectLabel(int secondsIn) {
        long milliSecondsLeft = totalMilliSeconds - secondsIn * 1000L;
        int secondsLeft = (int) (milliSecondsLeft / 1000);

        if (secondsLeft < 0 || secondsIn < lastSecondsIn) {
            return;
        }

        lastSecondsIn = secondsIn;

        secondsInLabel.setText(AudioUtil.formatSeconds(secondsIn));

        if (UserUtil.getCyderUser().getAudiolength().equals("1")) {
            secondsLeftLabel.setText(AudioUtil.formatSeconds((int) Math.round(totalMilliSeconds / 1000.0)));
        } else {
            secondsLeftLabel.setText(AudioUtil.formatSeconds(secondsLeft));
        }
    }

    /**
     * Updates the reference slider's value.
     */
    private void updateSlider() {
        float percentIn = (float) milliSecondsIn / totalMilliSeconds;

        // to be safe, check again
        if (!killed) {
            int value = Math.round(percentIn * slider.getMaximum());
            slider.setValue(value);
            slider.repaint();
        }
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
