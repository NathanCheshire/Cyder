package cyder.messaging;

import com.google.common.base.Preconditions;
import cyder.audio.WaveFile;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.CyderThreadFactory;
import cyder.utilities.AudioUtil;
import cyder.utilities.FileUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities related to the messaging client.
 */
public class MessagingUtils {
    /**
     * Suppress default constructor.
     */
    private MessagingUtils() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The default image width.
     */
    private static final int DEFAULT_IMAGE_WIDTH = 1800;

    /**
     * The default image height.
     */
    private static final int DEAULT_IMAGE_HEIGHT = 280;

    // todo test these for visibility
    public static final int MINIMUM_IMAGE_WIDTH = 280;
    public static final int MINIMUM_IMAGE_HEIGHT = 44;

    /**
     * The default background color.
     */
    private static final Color DEFAULT_BACKGROUND_COLOR = CyderColors.vanila;

    /**
     * The default wave color.
     */
    private static final Color DEFAULT_WAVE_COLOR = CyderColors.navy;

    /**
     * Generates a png depicting the waveform of the provided mp3/wav file.
     *
     * @param wavOrMp3File the mp3 opr wav file
     * @return the generated image
     */
    public static Future<BufferedImage> generateWaveForm(File wavOrMp3File) {
        Preconditions.checkArgument(FileUtil.validateExtension(wavOrMp3File, ".mp3")
                || FileUtil.validateExtension(wavOrMp3File, ".wav"));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Waveform generator")).submit(() -> {

            // init effectively final var for usage
            File usageWav = wavOrMp3File;

            // if it's an mp3, convert o wav before passing off
            if (FileUtil.validateExtension(usageWav, ".mp3")) {
                Future<Optional<File>> waitFor = AudioUtil.mp3ToWav(usageWav);

                // wait for conversion
                while (!waitFor.isDone()) {
                    Thread.onSpinWait();
                }

                if (waitFor.get().isPresent()) {
                    usageWav = waitFor.get().get();
                }
            }

            return generateWaveForm(usageWav, DEFAULT_IMAGE_WIDTH, DEAULT_IMAGE_HEIGHT,
                    DEFAULT_BACKGROUND_COLOR, DEFAULT_WAVE_COLOR);

        });
    }

    // todo I want a bass boost feature for an mp3 or wav file
    // todo ensure meets valid min width and height

    /**
     * Generates a png depicting the waveform of the provided wav file.
     *
     * @param wavFile the wav file
     * @param width the width of the requested image
     * @param height the height of the requested image
     * @param backgroundColor the background color of the iamge
     * @param waveColor the color of the waveform
     * @return the generated image
     */
    public static BufferedImage generateWaveForm(File wavFile, int width, int height,
                                                 Color backgroundColor, Color waveColor) {
        Preconditions.checkNotNull(wavFile);
        Preconditions.checkArgument(wavFile.exists());
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(backgroundColor);
        Preconditions.checkNotNull(waveColor);
        Preconditions.checkArgument(FileUtil.validateExtension(wavFile, ".wav"));

        System.out.println("Received file: " + wavFile.getAbsolutePath());

        // todo ensure meets min dimensions

        BufferedImage ret = new BufferedImage(DEFAULT_IMAGE_WIDTH, DEAULT_IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = ret.createGraphics();

        WaveFile wav = new WaveFile(wavFile);

        int samplesToTake = DEFAULT_IMAGE_WIDTH;
        int numFrames = (int) wav.getNumFrames();
        int[] nonNormalizedSamples = new int[samplesToTake];

        if (samplesToTake > numFrames) {
            throw new IllegalStateException("Samples to take is greater than num frames: "
                    + "samples = " + samplesToTake + ", frames = " + numFrames
                    + "\n" + CyderStrings.europeanToymaker);
        }

        int sampleLocIncrementer = (int) Math.ceil(numFrames / (double) samplesToTake);
        int currentSampleLoc = 0;
        int currentSampleIndex = 0;

        int maxAmp = 0;

        // find the max and add to the samples
        for (int i = 0; i < wav.getNumFrames(); i++) {
            maxAmp = Math.max(maxAmp, wav.getSampleInt(i));

            if (i == currentSampleLoc) {
                nonNormalizedSamples[currentSampleIndex] = wav.getSampleInt(i);

                currentSampleLoc += sampleLocIncrementer;
                currentSampleIndex++;
            }
        }

        // paint background of image
        g2d.setPaint(Color.WHITE);
        g2d.fillRect(0,0, DEFAULT_IMAGE_WIDTH, DEAULT_IMAGE_HEIGHT);

        // set to line color
        g2d.setColor(CyderColors.navy);

        // actual y values for painting
        int[] normalizedSamples = new int[samplesToTake];

        // get raw samples from file
        for (int i = 0 ; i < samplesToTake ; i++) {
            int normalizedValue = (int) ((nonNormalizedSamples[i] / (double) maxAmp) * DEAULT_IMAGE_HEIGHT);

            // if extending beyond bounds of our image, paint as zero and don't interpolate
            if (normalizedValue > DEAULT_IMAGE_HEIGHT / 2)
                normalizedValue = -69;

            normalizedSamples[i] = normalizedValue;
        }

        // interpolate between surrounding values where the amplitude is 0
        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            // if a true zero amplitude don't paint it
            if (normalizedSamples[i] == 0)
                continue;

            // at a value that needs interpolation
            else if (normalizedSamples[i] == -69) {
                // find the next value that isn't a 0 or an amp that has yet to be interpolated
                int nextNonZeroIndex = 0;
                for (int j = i ; j < normalizedSamples.length ; j++) {
                    if (normalizedSamples[j] != 0 && normalizedSamples[j] != -69) {
                        nextNonZeroIndex = j;
                        break;
                    }
                }

                // find the previous value that isn't 0 or an amp that has yet to be interpolated
                int lastNonZeroIndex = 0;
                for (int j = i ; j >= 0 ; j--) {
                    if (normalizedSamples[j] != 0 && normalizedSamples[j] != -69) {
                        lastNonZeroIndex = j;
                        break;
                    }
                }

                // average surrounding non zero values
                int avg = (normalizedSamples[nextNonZeroIndex] + normalizedSamples[lastNonZeroIndex]) / 2;

                // update current value
                normalizedSamples[i] = avg;
            }
        }

        // draw center line to ensure every y value on
        // the image contains at least one pixel
        for (int i = 0 ; i < width ; i++) {
            // from the center line extending downwards
            g2d.drawLine(i, DEAULT_IMAGE_HEIGHT / 2, i, DEAULT_IMAGE_HEIGHT / 2);
        }

        // paint the amplitude wave
        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            // from the center line extending downwards
            g2d.drawLine(i, DEAULT_IMAGE_HEIGHT / 2, i,
                    DEAULT_IMAGE_HEIGHT / 2 + normalizedSamples[i]);

            // from the center line extending upwards
            g2d.drawLine(i, DEAULT_IMAGE_HEIGHT / 2 - normalizedSamples[i],
                    i, DEAULT_IMAGE_HEIGHT / 2);
        }

        System.out.println("returning ret");
        return ret;
    }
}
