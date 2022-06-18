package cyder.handlers.external.audio;

import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.FileUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
    private long totalMilliSeconds;

    /**
     * The number of milliseconds in to the audio file.
     */
    private long milliSecondsIn;

    /**
     * The number of times a second to update the audio progress label.
     */
    private static final int UPDATES_PER_SECOND = 40;

    /**
     * The amount by which to sleep and increment for.
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

        setupProps();
    }

    /**
     * Determines the audio total length and updates the label in preparation for the update thread to start.
     */
    private void setupProps() {
        CyderThreadRunner.submit(() -> {
            secondsInLabel.setText("");
            secondsLeftLabel.setText("");
            slider.setValue(0);

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

            updateEffectLabel((int) (Math.floor(milliSecondsIn / 1000.0)));

            if (!sliderPressed.get()) {
                updateSlider();
            }

            startUpdateThread();
        }, FileUtil.getFilename(currentAudioFile.get()) + " Progress Label Thread");
    }

    /**
     * Whether the update thread has been started yet.
     */
    private boolean started;

    /**
     * The frequency to sync the slider and audio location labels with
     * the audio player's inner audio player object's value.
     */
    private int AUDIO_PLAYER_SYNC_FREQUENCY = 100;

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

        AtomicInteger accumulator = new AtomicInteger();

        CyderThreadRunner.submit(() -> {
            while (!killed) {
                try {
                    Thread.sleep(UPDATE_DELAY);

                    if (!timerPaused) {
                        milliSecondsIn += UPDATE_DELAY;
                    }
                } catch (Exception ignored) {}

                accumulator.getAndIncrement();

                // sync with inner audio player's value
                if (accumulator.get() % AUDIO_PLAYER_SYNC_FREQUENCY == 0) {
                    accumulator.set(0);
                    long newMillisIn = AudioPlayer.getMilliSecondsIn();

                    if (newMillisIn != AudioPlayer.INVALID_SECONDS_IN) {
                        milliSecondsIn = newMillisIn;
                    }
                }

                // todo make sure this is always in sync with the inner audio player's audio,
                //  maybe this should be bundled with a player object
                // only updates ui layer, millisecond increments are still happening above

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
        slider.setValue(Math.round(percentIn * slider.getMaximum()));
        slider.repaint();
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
