package cyder.audio;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.user.UserUtil;
import cyder.utils.FileUtil;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The class to update the audio location label and progress bar.
 */
public class AudioLocationUpdater {
    /**
     * The thread for setting up the props during object construction.
     */
    private static final String SETUP_PROPS_THREAD_NAME = "AudioLocationUpdater setupProps Thread";

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
        this.secondsInLabel = Preconditions.checkNotNull(secondsInLabel);
        this.secondsLeftLabel = Preconditions.checkNotNull(secondsLeftLabel);
        this.currentFrameView = Preconditions.checkNotNull(currentFrameView);
        this.currentAudioFile = Preconditions.checkNotNull(currentAudioFile);
        this.sliderPressed = Preconditions.checkNotNull(sliderPressed);
        this.slider = Preconditions.checkNotNull(slider);

        setupProps();
    }

    /**
     * Determines the audio total length and updates the label in preparation for the update thread to start.
     */
    private void setupProps() {
        secondsInLabel.setText("");
        secondsLeftLabel.setText("");
        slider.setValue(0);

        updateEffectLabel((int) (Math.floor(milliSecondsIn / 1000.0)), false);

        if (!sliderPressed.get()) {
            updateSlider();
        }

        CyderThreadRunner.submit(() -> {
            try {
                File file = currentAudioFile.get();
                Future<Integer> futureTotalMilliSeconds = AudioUtil.getMillisMutagen(file);
                this.totalMilliSeconds = futureTotalMilliSeconds.get();
                startUpdateThread();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, SETUP_PROPS_THREAD_NAME);
    }

    /**
     * Whether the update thread has been started yet.
     */
    private boolean started;

    /**
     * The timeout between progress label and slider updates.
     */
    private static final int TIMEOUT = 100;

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
                ThreadUtil.sleep(TIMEOUT);

                milliSecondsIn = AudioPlayer.getMillisecondsIn();
                int newSecondsIn = (int) (milliSecondsIn / 1000.0);

                if (!timerPaused && currentFrameView.get() != FrameView.MINI) {
                    if (!sliderPressed.get()) {
                        updateEffectLabel(newSecondsIn, false);
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
     *
     * @param userTriggered whether this even was triggered by a user or automatically
     */
    public void update(boolean userTriggered) {
        updateEffectLabel((int) (Math.floor(milliSecondsIn / 1000.0)), userTriggered);
        updateSlider();
    }

    /**
     * The value passed to the updateEffectLabel method last.
     */
    private int lastSecondsIn;

    /**
     * Updates the encapsulated label with the time in to the current audio file.
     *
     * @param secondsIn     the seconds into the current audio file
     * @param userTriggered whether this update was invoked by a user or automatically
     */
    private void updateEffectLabel(int secondsIn, boolean userTriggered) {
        long milliSecondsLeft = totalMilliSeconds - secondsIn * 1000L;
        int secondsLeft = (int) (milliSecondsLeft / 1000);

        if (secondsLeft < 0 || (secondsIn < lastSecondsIn && !userTriggered)) {
            return;
        }

        lastSecondsIn = secondsIn;
        secondsInLabel.setText(TimeUtil.formatMillis(secondsIn * 1000L));

        boolean totalLength = UserUtil.getCyderUser().getAudioLength().equals("1");
        if (totalLength) {
            int displayMillis = Math.round(totalMilliSeconds / 1000f) * 1000;
            secondsLeftLabel.setText(TimeUtil.formatMillis(displayMillis));
        } else {
            secondsLeftLabel.setText(TimeUtil.formatMillis(secondsLeft * 1000L));
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
