package cyder.messaging;

import com.google.common.base.Preconditions;
import cyder.audio.WaveFile;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;
import cyder.ui.CyderButton;
import cyder.utilities.AudioUtil;
import cyder.utilities.FileUtil;
import cyder.utilities.ImageUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
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
     * The default large width for waveform image generation.
     */
    public static final int DEFAULT_LARGE_WAVEFORM_WIDTH = 1800;

    /**
     * The default large height for waveform image generation.
     */
    public static final int DEFAULT_LARGE_WAVEFORM_HEIGHT = 280;

    /**
     * The default small width for waveform image generation.
     */
    public static final int DEFAULT_SMALL_WAVEFORM_WIDTH = 140;

    /**
     * The default small height for waveform image generation.
     * "Gave 'em 44, now here's 44 more."
     */
    public static final int DEFAULT_SMALL_WAVEFORM_HEIGHT = 44;

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
     * The dimensions used are DEFAULT_LARGE_WAVEFORM_WIDTH x DEFAULT_LARGE_WAVEFORM_HEIGHT.
     *
     * @param wavOrMp3File the mp3 opr wav file
     * @return the generated image
     */
    public static Future<BufferedImage> generateLargeWaveform(File wavOrMp3File) {
        Preconditions.checkArgument(FileUtil.validateExtension(wavOrMp3File, ".mp3")
                || FileUtil.validateExtension(wavOrMp3File, ".wav"));

       return generateWaveform(wavOrMp3File, DEFAULT_LARGE_WAVEFORM_WIDTH, DEFAULT_LARGE_WAVEFORM_HEIGHT);
    }

    /**
     * Generates a png depicting the waveform of the provided mp3/wav file.
     * The dimensions used are DEFAULT_SMALL_WAVEFORM_WIDTH x DEFAULT_SMALL_WAVEFORM_HEIGHT.
     *
     * @param wavOrMp3File the mp3 opr wav file
     * @return the generated image
     */
    public static Future<BufferedImage> generateSmallWaveform(File wavOrMp3File) {
        Preconditions.checkArgument(FileUtil.validateExtension(wavOrMp3File, ".mp3")
                || FileUtil.validateExtension(wavOrMp3File, ".wav"));

        return generateWaveform(wavOrMp3File, DEFAULT_SMALL_WAVEFORM_WIDTH, DEFAULT_SMALL_WAVEFORM_HEIGHT);
    }

    private static Future<BufferedImage> generateWaveform(File wavOrMp3File, int width, int height) {
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

            return generateWaveform(usageWav, width, height,
                    DEFAULT_BACKGROUND_COLOR, DEFAULT_WAVE_COLOR);

        });
    }

    /**
     * Generates a png depicting the waveform of the provided wav file.
     *
     * @param wavFile the wav file
     * @param width the width of the requested image
     * @param height the height of the requested image
     * @param backgroundColor the background color of the image
     * @param waveColor the color of the waveform
     * @return the generated image
     */
    public static BufferedImage generateWaveform(File wavFile, int width, int height,
                                                 Color backgroundColor, Color waveColor) {
        Preconditions.checkNotNull(wavFile);
        Preconditions.checkArgument(wavFile.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(wavFile, ".wav"));

        Preconditions.checkArgument(width >= DEFAULT_SMALL_WAVEFORM_WIDTH);
        Preconditions.checkArgument(height >= DEFAULT_SMALL_WAVEFORM_HEIGHT);

        Preconditions.checkNotNull(backgroundColor);
        Preconditions.checkNotNull(waveColor);

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = ret.createGraphics();

        WaveFile wav = new WaveFile(wavFile);

        int numFrames = (int) wav.getNumFrames();
        int[] nonNormalizedSamples = new int[width];

        if (width > numFrames) {
            throw new IllegalStateException("Samples to take is greater than num frames: "
                    + "samples = " + width + ", frames = " + numFrames
                    + "\n" + CyderStrings.europeanToymaker);
        }

        int sampleLocIncrementer = (int) Math.ceil(numFrames / (double) width);
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
        g2d.fillRect(0,0, width, height);

        // set to line color
        g2d.setColor(CyderColors.navy);

        // actual y values for painting
        int[] normalizedSamples = new int[width];

        // get raw samples from file
        for (int i = 0; i < width; i++) {
            int normalizedValue = (int) ((nonNormalizedSamples[i] / (double) maxAmp) * height);

            // if extending beyond bounds of our image, paint as zero and don't interpolate
            if (normalizedValue > height / 2)
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
            g2d.drawLine(i, height / 2, i, height / 2);
        }

        // paint the amplitude wave
        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            // from the center line extending downwards
            g2d.drawLine(i, height / 2, i,
                    height / 2 + normalizedSamples[i]);

            // from the center line extending upwards
            g2d.drawLine(i, height / 2 - normalizedSamples[i],
                    i, height / 2);
        }

        return ret;
    }

    // todo maybe an audio player that scrolls and turns the navy
    // to red as it aligns with the song percentage would be cool

    // todo I want a bass boost feature for an mp3 or wav file
    // todo use this in the new audio player widget which should handle mp3s and wavs

    // todo officially support mp3 and wav, will need updated code in a lot of places
    // and an method like images to check if valid

    // todo for updating image to red from navy, method inside of audio player
    // to set navy pixels/foreground color pixels to red, image should still be long
    // enough to buffer to most parts of a 3-5 minute audio withou ~5 pixels

    // todo invoking below
//     CyderThreadRunner.submit(() -> {
//        try {
//            Future<JLabel> label = MessagingUtils.generateAudioPreviewLabel(
//                    new File("c:/users/nathan/downloads/I love you.wav"), () -> System.out.println("here"));
//
//            while (!label.isDone()) {
//                Thread.onSpinWait();
//            }
//
//            JLabel theLabel = label.get();
//
//            ConsoleFrame.INSTANCE.getInputHandler().printlnComponent(theLabel);
//        } catch (Exception e) {
//            ExceptionHandler.handle(e);
//        }
//    }, "Manual Tester");

    /**
     * Generates and returns a file preview for the provided audio file.
     *
     * @param mp3OrWavFile the wav or mp3 file
     * @param onSaveRunnable the runnable to invoke when the save button is pressed
     * @return the label with the waveform preview and save button
     */
    public static Future<JLabel> generateAudioPreviewLabel(File mp3OrWavFile, Runnable onSaveRunnable) {
        Preconditions.checkNotNull(mp3OrWavFile);
        Preconditions.checkArgument(mp3OrWavFile.exists());
        Preconditions.checkNotNull(onSaveRunnable);

        Preconditions.checkArgument(FileUtil.validateExtension(mp3OrWavFile, ".mp3")
                || FileUtil.validateExtension(mp3OrWavFile, ".wav"));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Audio file preview generator")).submit(() -> {
            try {
                Future<BufferedImage> image = generateSmallWaveform(mp3OrWavFile);

                while (!image.isDone()) {
                    Thread.onSpinWait();
                }

                int borderLen = 5;
                int buttonHeight = 40;

                int containerWidth = 150;
                int containerHeight = DEFAULT_SMALL_WAVEFORM_HEIGHT + buttonHeight + 2 * borderLen;

                JLabel containerLabel = new JLabel("<html> <br/> <br/> <br/>" +
                        " <br/> <br/> <br/></html>") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        g.setColor(CyderColors.nullus);
                        g.fillRect(0,0, containerWidth, containerHeight);
                        super.paintComponent(g);
                    }
                };
                containerLabel.setSize(containerWidth, containerHeight);

                JLabel imageLabel = new JLabel();
                imageLabel.setBounds(borderLen,borderLen, 140, DEFAULT_SMALL_WAVEFORM_HEIGHT);
                imageLabel.setIcon(ImageUtil.toImageIcon(image.get()));

                JLabel imageContainerLabel = new JLabel();
                imageContainerLabel.setBorder(new LineBorder(CyderColors.navy, borderLen));
                imageContainerLabel.setBounds(0, 0, 150, DEFAULT_SMALL_WAVEFORM_HEIGHT + 5);
                imageContainerLabel.add(imageLabel);
                containerLabel.add(imageContainerLabel);

                CyderButton saveButton = new CyderButton("Save");
                saveButton.setBounds(0, DEFAULT_SMALL_WAVEFORM_HEIGHT + borderLen,
                        150, buttonHeight);
                containerLabel.add(saveButton);
                saveButton.addActionListener(e -> onSaveRunnable.run());

                BufferedImage preview = new BufferedImage(
                        containerLabel.getWidth(),
                        containerLabel.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );

                containerLabel.paint(preview.getGraphics());

                return containerLabel;
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            return null;
        });
    }
}
