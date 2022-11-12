package cyder.messaging;

import com.google.common.base.Preconditions;
import cyder.annotations.CyderTest;
import cyder.audio.AudioUtil;
import cyder.audio.WaveFile;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.ui.button.CyderButton;
import cyder.utils.ImageUtil;
import cyder.utils.StringUtil;

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
public final class MessagingUtils {
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
     */
    public static final int DEFAULT_SMALL_WAVEFORM_HEIGHT = 44;

    /**
     * Suppress default constructor.
     */
    private MessagingUtils() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The default background color.
     */
    private static final Color DEFAULT_BACKGROUND_COLOR = CyderColors.vanilla;

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
        Preconditions.checkNotNull(wavOrMp3File);
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(wavOrMp3File));

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
        Preconditions.checkNotNull(wavOrMp3File);
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(wavOrMp3File));

        return generateWaveform(wavOrMp3File, DEFAULT_SMALL_WAVEFORM_WIDTH, DEFAULT_SMALL_WAVEFORM_HEIGHT);
    }

    /**
     * The name of the executor service which waits for the waveform image to finish generation.
     */
    private static final String waveformGeneratorThreadName = "Waveform Generator Waiter";

    /**
     * @param wavOrMp3File the wav or mp3 file to generate a waveform image for
     * @param width        the width of the waveform image
     * @param height       the height of the waveform image
     * @return the waveform image
     */
    public static Future<BufferedImage> generateWaveform(File wavOrMp3File, int width, int height) {
        Preconditions.checkNotNull(wavOrMp3File);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        return Executors.newSingleThreadExecutor(new CyderThreadFactory(waveformGeneratorThreadName)).submit(() -> {
            File usageWav = wavOrMp3File;

            if (FileUtil.validateExtension(usageWav, Extension.MP3.getExtension())) {
                Future<Optional<File>> futureWav = AudioUtil.mp3ToWav(usageWav);
                while (!futureWav.isDone()) Thread.onSpinWait();

                if (futureWav.get().isPresent()) usageWav = futureWav.get().get();
            }

            return generateWaveform(usageWav, width, height, DEFAULT_BACKGROUND_COLOR, DEFAULT_WAVE_COLOR);
        });
    }

    /**
     * The number denoting a value should be interpolated.
     */
    private static final int interpolationNeededValue = -69;

    // todo when looking for these need to ensure static and void and public
    // todo also always invoke these in a separate thread

    // todo when these are invoked say what, where, and when
    // invoking testImageGeneration in MessagingUtils.java at 1:31:23am

    @CyderTest
    public static void testImageGeneration() {
        CyderThreadRunner.submit(() -> {
            try {
                File mp3 = new File("C:/users/nathan/downloads/badapple.mp3");
                long start = System.currentTimeMillis();
                Future<BufferedImage> image = generateWaveform(mp3, 1000, 150);
                while (!image.isDone()) Thread.onSpinWait();
                long end = System.currentTimeMillis();
                System.out.println("Time to generate: " + (end - start) + "ms");
                ImageUtil.drawImage(image.get()); // todo this should return a frame reference, annotate with can ignore
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "asdfasdf");
    }

    /**
     * Generates a png depicting the waveform of the provided wav file.
     *
     * @param wavFile         the wav file
     * @param width           the width of the image
     * @param height          the height of the image
     * @param backgroundColor the background color of the image
     * @param waveColor       the color of the waveform
     * @return the generated waveform image
     */
    public static BufferedImage generateWaveform(File wavFile,
                                                 int width, int height,
                                                 Color backgroundColor, Color waveColor) {
        Preconditions.checkNotNull(wavFile);
        Preconditions.checkArgument(wavFile.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(wavFile, Extension.WAV.getExtension()));
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(backgroundColor);
        Preconditions.checkNotNull(waveColor);
        Preconditions.checkArgument(!backgroundColor.equals(waveColor));

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = ret.createGraphics();

        WaveFile wav = new WaveFile(wavFile);

        int numFrames = (int) wav.getNumFrames();
        if (numFrames < width) width = numFrames;

        int[] nonNormalizedSamples = new int[width];

        int sampleLocationIncrement = (int) Math.ceil(numFrames / (double) width);
        int currentSampleLoc = 0;
        int currentSampleIndex = 0;

        int maxAmplitude = 0;

        // find the max and add to the samples at the same time
        for (int i = 0 ; i < wav.getNumFrames() ; i++) {
            maxAmplitude = Math.max(maxAmplitude, wav.getSample(i));

            if (i == currentSampleLoc) {
                nonNormalizedSamples[currentSampleIndex] = wav.getSample(i);

                currentSampleLoc += sampleLocationIncrement;
                currentSampleIndex++;
            }
        }

        g2d.setPaint(backgroundColor);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(waveColor);

        // actual y values for painting
        int[] normalizedSamples = new int[width];

        // normalize raw samples and mark values to interpolate
        for (int i = 0 ; i < width ; i++) {
            int normalizedValue = (int) ((nonNormalizedSamples[i] / (double) maxAmplitude) * height);

            // if extending beyond bounds of our image, paint as zero and don't interpolate
            if (normalizedValue > height / 2)
                normalizedValue = interpolationNeededValue;

            normalizedSamples[i] = normalizedValue;
        }

        // interpolate between surrounding values where the amplitude is 0
        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            // if a true zero amplitude don't paint it
            if (normalizedSamples[i] == 0)
                continue;

                // at a value that needs interpolation
            else if (normalizedSamples[i] == interpolationNeededValue) {
                // find the next value that isn't a 0 or an amp that has yet to be interpolated
                int nextNonZeroIndex = 0;
                for (int j = i ; j < normalizedSamples.length ; j++) {
                    if (normalizedSamples[j] != 0 && normalizedSamples[j] != interpolationNeededValue) {
                        nextNonZeroIndex = j;
                        break;
                    }
                }

                // find the previous value that isn't 0 or an amp that has yet to be interpolated
                int lastNonZeroIndex = 0;
                for (int j = i ; j >= 0 ; j--) {
                    if (normalizedSamples[j] != 0 && normalizedSamples[j] != interpolationNeededValue) {
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

        // Draw center line
        for (int i = 0 ; i < width ; i++) {
            g2d.drawLine(i, height / 2, i, height / 2);
        }

        // Paint wave extending upwards and downwards
        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            g2d.drawLine(i, height / 2, i, height / 2 + normalizedSamples[i]);
            g2d.drawLine(i, height / 2 - normalizedSamples[i], i, height / 2);
        }

        return ret;
    }

    /**
     * The text used for the audio preview label component.
     */
    private static final String AUDIO_PREVIEW_LABEL_MAGIC_TEXT = StringUtil.generateTextForCustomComponent(6);

    /**
     * The border length for generated audio preview.
     */
    private static final int AUDIO_PREVIEW_BORDER_LEN = 5;

    /**
     * The button height for generated audio previews.
     */
    private static final int AUDIO_PREVIEW_BUTTON_HEIGHT = 40;

    /**
     * The container width for generated audio previews.
     */
    private static final int AUDIO_PREVIEW_CONTAINER_WIDTH = 150;

    /**
     * The container height for generated audio previews.
     */
    private static final int AUDIO_PREVIEW_CONTAINER_HEIGHT = DEFAULT_SMALL_WAVEFORM_HEIGHT
            + AUDIO_PREVIEW_BUTTON_HEIGHT + 2 * AUDIO_PREVIEW_BORDER_LEN;

    /**
     * Generates and returns a file preview for the provided audio file.
     *
     * @param mp3OrWavFile   the wav or mp3 file
     * @param onSaveRunnable the runnable to invoke when the save button is pressed
     * @return the label with the waveform preview and save button
     */
    public static Future<JLabel> generateAudioPreviewLabel(File mp3OrWavFile, Runnable onSaveRunnable) {
        Preconditions.checkNotNull(mp3OrWavFile);
        Preconditions.checkArgument(mp3OrWavFile.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(mp3OrWavFile));
        Preconditions.checkNotNull(onSaveRunnable);

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Audio file preview generator")).submit(() -> {
            Future<BufferedImage> image = generateSmallWaveform(mp3OrWavFile);

            while (!image.isDone()) {
                Thread.onSpinWait();
            }

            JLabel containerLabel = new JLabel(AUDIO_PREVIEW_LABEL_MAGIC_TEXT) {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(CyderColors.empty);
                    g.fillRect(0, 0, AUDIO_PREVIEW_CONTAINER_WIDTH, AUDIO_PREVIEW_CONTAINER_HEIGHT);
                    super.paintComponent(g);
                }
            };
            containerLabel.setSize(AUDIO_PREVIEW_CONTAINER_WIDTH, AUDIO_PREVIEW_CONTAINER_HEIGHT);

            JLabel imageLabel = new JLabel();
            imageLabel.setBounds(AUDIO_PREVIEW_BORDER_LEN, AUDIO_PREVIEW_BORDER_LEN,
                    AUDIO_PREVIEW_CONTAINER_WIDTH - 2 * AUDIO_PREVIEW_BORDER_LEN, DEFAULT_SMALL_WAVEFORM_HEIGHT);
            imageLabel.setIcon(ImageUtil.toImageIcon(image.get()));

            JLabel imageContainerLabel = new JLabel();
            imageContainerLabel.setBorder(new LineBorder(CyderColors.navy, AUDIO_PREVIEW_BORDER_LEN));
            imageContainerLabel.setBounds(0, 0, AUDIO_PREVIEW_CONTAINER_WIDTH,
                    DEFAULT_SMALL_WAVEFORM_HEIGHT + 5);
            imageContainerLabel.add(imageLabel);
            containerLabel.add(imageContainerLabel);

            CyderButton saveButton = new CyderButton(SAVE);
            saveButton.setBounds(0, DEFAULT_SMALL_WAVEFORM_HEIGHT + AUDIO_PREVIEW_BORDER_LEN,
                    AUDIO_PREVIEW_CONTAINER_WIDTH, AUDIO_PREVIEW_BUTTON_HEIGHT);
            containerLabel.add(saveButton);
            saveButton.setBackground(CyderColors.regularPurple);
            saveButton.setForeground(CyderColors.defaultDarkModeTextColor);
            saveButton.addActionListener(e -> onSaveRunnable.run());

            BufferedImage preview = new BufferedImage(
                    containerLabel.getWidth(),
                    containerLabel.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            containerLabel.paint(preview.getGraphics());

            return containerLabel;
        });
    }

    /**
     * The length of the image for the generated image previews.
     */
    private static final int IMAGE_PREVIEW_LEN = 150;

    /**
     * The height for the image preview save button.
     */
    private static final int IMAGE_PREVIEW_BUTTON_HEIGHT = 40;

    /**
     * The text used for generated image preview labels.
     */
    private static final String IMAGE_PREVIEW_LABEL_TEXT = StringUtil.generateTextForCustomComponent(12);

    /**
     * The save text for generated image and audio preview labels.
     */
    private static final String SAVE = "Save";

    /**
     * Generates and returns a file preview for the provided image file.
     *
     * @param imageFile      the image file
     * @param onSaveRunnable the runnable to invoke when the save button is pressed
     * @return the label with an image preview and save button
     */
    public static JLabel generateImagePreviewLabel(File imageFile, Runnable onSaveRunnable) {
        Preconditions.checkNotNull(imageFile);
        Preconditions.checkArgument(imageFile.exists());
        Preconditions.checkArgument(FileUtil.isSupportedImageExtension(imageFile));
        Preconditions.checkNotNull(onSaveRunnable);

        BufferedImage readImage = null;
        try {
            readImage = ImageUtil.read(imageFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (readImage == null) throw new FatalException("Failed to read image: " + imageFile.getAbsolutePath());

        ImageIcon resized = ImageUtil.resizeImage(ImageUtil.toImageIcon(readImage),
                IMAGE_PREVIEW_LEN, IMAGE_PREVIEW_LEN);

        JLabel imagePreviewLabel = new JLabel();
        imagePreviewLabel.setSize(IMAGE_PREVIEW_LEN, IMAGE_PREVIEW_LEN);
        imagePreviewLabel.setIcon(resized);
        imagePreviewLabel.setBorder(new LineBorder(CyderColors.navy, 5));

        CyderButton saveButton = new CyderButton(SAVE);
        saveButton.setSize(IMAGE_PREVIEW_LEN, IMAGE_PREVIEW_BUTTON_HEIGHT);
        saveButton.setBackground(CyderColors.regularPurple);
        saveButton.setForeground(CyderColors.defaultDarkModeTextColor);

        JLabel ret = new JLabel(IMAGE_PREVIEW_LABEL_TEXT);
        imagePreviewLabel.setLocation(0, 0);
        ret.add(imagePreviewLabel);

        saveButton.setLocation(0, IMAGE_PREVIEW_LEN - 5);
        ret.add(saveButton);

        return ret;
    }
}
