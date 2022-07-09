package cyder.messaging;

import com.google.common.base.Preconditions;
import cyder.audio.AudioUtil;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;
import cyder.ui.CyderButton;
import cyder.utils.FileUtil;
import cyder.utils.ImageUtil;
import cyder.utils.StringUtil;

import javax.imageio.ImageIO;
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
     * Suppress default constructor.
     */
    private MessagingUtils() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
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
     * Generates a buffered image depicting the local waveform of the provided wav file.
     *
     * @param wav             the wav file to sample
     * @param startFrame      the starting frame of the wav file
     * @param numSamples      the number of samples to take/the resulting width of the image
     * @param height          the height of the image
     * @param backgroundColor the background color of the image
     * @param waveColor       the wave color to draw
     * @return a buffered image depicting the local waveform of the provided wav file
     */
    public static BufferedImage generate1DWaveformInRange(WaveFile wav, int startFrame,
                                                          int numSamples, int height,
                                                          Color backgroundColor, Color waveColor) {
        Preconditions.checkNotNull(wav);
        Preconditions.checkArgument(startFrame >= 0);
        Preconditions.checkArgument(startFrame < wav.getNumFrames());
        Preconditions.checkArgument(numSamples < wav.getNumFrames());
        Preconditions.checkArgument(startFrame + numSamples < wav.getNumFrames());

        Preconditions.checkNotNull(backgroundColor);
        Preconditions.checkNotNull(waveColor);

        // now standard algorithm
        int[] nonNormalizedSamples = new int[numSamples];
        int localMax = 0;

        // get local non-normalized samples and find local max
        int index = 0;
        for (int i = startFrame ; i < startFrame + numSamples ; i++) {
            int sample = wav.getSample(i);

            localMax = Math.max(sample, localMax);

            nonNormalizedSamples[index] = sample;
            index++;
        }

        int[] normalizedSamples = new int[nonNormalizedSamples.length];
        int interpolationNeededValue = -69;

        // normalize values and skip ones which exceeding tol
        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            int normalizedValue = (int) ((nonNormalizedSamples[i] / (float) localMax) * height);

            if (normalizedValue >= height * 0.9) {
                normalizedValue = interpolationNeededValue;
            }

            normalizedSamples[i] = normalizedValue;
        }

        // interpolate between surrounding values where
        // the amplitude was set to the interpolation value
        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            // if a true zero amplitude don't interpolate
            if (normalizedSamples[i] == 0) {
                continue;
            }
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

                // average the surrounding values for the interpolated value
                int avg = (normalizedSamples[nextNonZeroIndex] + normalizedSamples[lastNonZeroIndex]) / 2;

                // update current value
                normalizedSamples[i] = avg;
            }
        }

        BufferedImage ret = new BufferedImage(numSamples, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = ret.createGraphics();

        // draw background
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, numSamples, height);

        g2d.setColor(waveColor);

        // draw samples
        for (int i = 0 ; i < normalizedSamples.length ; i++) {
            g2d.drawLine(i, height, i, height - normalizedSamples[i]);
            g2d.drawLine(i, height, i, height - 1);
        }

        return ret;
    }

    /**
     * Generates a png depicting the waveform of the provided wav file.
     *
     * @param wavFile         the wav file
     * @param width           the width of the requested image
     * @param height          the height of the requested image
     * @param backgroundColor the background color of the image
     * @param waveColor       the color of the waveform
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

        Preconditions.checkArgument(width <= numFrames,
                "Samples to take is greater than num frames: "
                        + "samples = " + width + ", frames = " + numFrames
                        + "\n" + CyderStrings.EUROPEAN_TOY_MAKER);

        int sampleLocIncrementer = (int) Math.ceil(numFrames / (double) width);
        int currentSampleLoc = 0;
        int currentSampleIndex = 0;

        int maxAmp = 0;

        // find the max and add to the samples at the same time
        for (int i = 0 ; i < wav.getNumFrames() ; i++) {
            maxAmp = Math.max(maxAmp, wav.getSample(i));

            if (i == currentSampleLoc) {
                nonNormalizedSamples[currentSampleIndex] = wav.getSample(i);

                currentSampleLoc += sampleLocIncrementer;
                currentSampleIndex++;
            }
        }

        // paint background of image
        g2d.setPaint(backgroundColor);
        g2d.fillRect(0, 0, width, height);

        // set to line color
        g2d.setColor(waveColor);

        // actual y values for painting
        int[] normalizedSamples = new int[width];

        // normalize raw samples and mark values to interpolate
        for (int i = 0 ; i < width ; i++) {
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
        Preconditions.checkNotNull(onSaveRunnable);

        Preconditions.checkArgument(FileUtil.validateExtension(mp3OrWavFile, ".mp3")
                || FileUtil.validateExtension(mp3OrWavFile, ".wav"));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Audio file preview generator")).submit(() -> {
            Future<BufferedImage> image = generateSmallWaveform(mp3OrWavFile);

            while (!image.isDone()) {
                Thread.onSpinWait();
            }

            int borderLen = 5;
            int buttonHeight = 40;

            int containerWidth = 150;
            int containerHeight = DEFAULT_SMALL_WAVEFORM_HEIGHT + buttonHeight + 2 * borderLen;

            JLabel containerLabel = new JLabel(StringUtil.generateTextForCustomComponent(6)) {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(CyderColors.zero);
                    g.fillRect(0, 0, containerWidth, containerHeight);
                    super.paintComponent(g);
                }
            };
            containerLabel.setSize(containerWidth, containerHeight);

            JLabel imageLabel = new JLabel();
            imageLabel.setBounds(borderLen, borderLen, 140, DEFAULT_SMALL_WAVEFORM_HEIGHT);
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
    public static final int IMAGE_PREVIEW_LEN = 150;

    /**
     * Generates and returns a file preview for the provided image file.
     *
     * @param imageFile      the image file
     * @param onSaveRunnable the runnable to invoke when the save button is pressed.
     * @return the label with the image preview and save button.
     */
    public static JLabel generatePicturePreviewLabel(File imageFile, Runnable onSaveRunnable) {
        Preconditions.checkNotNull(imageFile);
        Preconditions.checkArgument(imageFile.exists());
        Preconditions.checkNotNull(onSaveRunnable);

        JLabel ret = null;

        try {
            Preconditions.checkArgument(FileUtil.isSupportedImageExtension(imageFile));

            ImageIcon resized = ImageUtil.resizeImage(ImageUtil.toImageIcon(ImageIO.read(imageFile)),
                    IMAGE_PREVIEW_LEN, IMAGE_PREVIEW_LEN);

            JLabel imagePreviewLabel = new JLabel();
            imagePreviewLabel.setSize(IMAGE_PREVIEW_LEN, IMAGE_PREVIEW_LEN);
            imagePreviewLabel.setIcon(resized);
            imagePreviewLabel.setBorder(new LineBorder(CyderColors.navy, 5));

            CyderButton saveButton = new CyderButton("Save");
            saveButton.setSize(IMAGE_PREVIEW_LEN, 40);
            saveButton.setBackground(CyderColors.regularPurple);
            saveButton.setForeground(CyderColors.defaultDarkModeTextColor);

            ret = new JLabel(StringUtil.generateTextForCustomComponent(12));
            imagePreviewLabel.setLocation(0, 0);
            ret.add(imagePreviewLabel);

            saveButton.setLocation(0, IMAGE_PREVIEW_LEN - 5);
            ret.add(saveButton);

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }
}
