package cyder.messaging;

import com.google.common.base.Preconditions;
import cyder.audio.AudioUtil;
import cyder.audio.WaveFile;
import cyder.constants.CyderColors;
import cyder.enumerations.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadFactory;
import cyder.ui.UiUtil;
import cyder.ui.button.CyderButton;
import cyder.utils.ImageUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utilities related to the messaging client.
 */
public final class MessagingUtil {
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
     * The default background color.
     */
    private static final Color DEFAULT_BACKGROUND_COLOR = CyderColors.vanilla;

    /**
     * The default wave color.
     */
    private static final Color DEFAULT_WAVE_COLOR = CyderColors.navy;

    /**
     * The text used for the audio preview label component.
     */
    private static final String AUDIO_PREVIEW_LABEL_MAGIC_TEXT = UiUtil.generateTextForCustomComponent(6);

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
    private static final int AUDIO_PREVIEW_CONTAINER_HEIGHT =
            DEFAULT_SMALL_WAVEFORM_HEIGHT + AUDIO_PREVIEW_BUTTON_HEIGHT + 2 * AUDIO_PREVIEW_BORDER_LEN;

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
    private static final String IMAGE_PREVIEW_LABEL_TEXT = UiUtil.generateTextForCustomComponent(12);

    /**
     * The save text for generated image and audio preview labels.
     */
    private static final String SAVE = "Save";

    /**
     * The number denoting a value should be interpolated.
     */
    private static final int interpolationNeededValue = -69;

    /**
     * The name of the executor service which waits for the waveform image to finish generation.
     */
    private static final String waveformGeneratorThreadName = "Audio Waveform Generator";

    /**
     * The name of the executor service which waits for the waveform image to
     * finish generation when generating an image preview label.
     */
    private static final String audioWaveformPreviewLabelGeneratorThreadName =
            "Audio waveform preview label generator";

    /**
     * Suppress default constructor.
     */
    private MessagingUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Generates a png depicting the waveform of the provided mp3/wav file.
     * The dimensions used are DEFAULT_LARGE_WAVEFORM_WIDTH x DEFAULT_LARGE_WAVEFORM_HEIGHT.
     *
     * @param wavOrMp3File the mp3 opr wav file
     * @return the generated image
     */
    public static Future<BufferedImage> generateLargeWaveform(File wavOrMp3File) {
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
        return generateWaveform(wavOrMp3File, DEFAULT_SMALL_WAVEFORM_WIDTH, DEFAULT_SMALL_WAVEFORM_HEIGHT);
    }

    /**
     * @param wavOrMp3File the wav or mp3 file to generate a waveform image for
     * @param width        the width of the waveform image
     * @param height       the height of the waveform image
     * @return the waveform image
     */
    public static Future<BufferedImage> generateWaveform(File wavOrMp3File, int width, int height) {
        return generateWaveform(wavOrMp3File, width, height, DEFAULT_BACKGROUND_COLOR, DEFAULT_WAVE_COLOR);
    }

    /**
     * Generates a png depicting the waveform of the provided wav or mp3 file.
     *
     * @param wavOrMp3File    the wav or mp3 file
     * @param width           the width of the image
     * @param height          the height of the image
     * @param backgroundColor the background color of the image
     * @param waveColor       the color of the waveform
     * @return the generated waveform image
     */
    @SuppressWarnings("UnusedAssignment") /* Freeing resource */
    public static Future<BufferedImage> generateWaveform(final File wavOrMp3File,
                                                         final int width, final int height,
                                                         final Color backgroundColor, final Color waveColor) {
        Preconditions.checkNotNull(wavOrMp3File);
        Preconditions.checkArgument(wavOrMp3File.exists());
        Preconditions.checkArgument(wavOrMp3File.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(wavOrMp3File));
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(backgroundColor);
        Preconditions.checkNotNull(waveColor);
        Preconditions.checkArgument(!backgroundColor.equals(waveColor));

        AtomicReference<File> wavOrMp3FileReference = new AtomicReference<>(wavOrMp3File);
        AtomicInteger widthReference = new AtomicInteger(width);
        AtomicInteger heightReference = new AtomicInteger(height);

        return Executors.newSingleThreadExecutor(new CyderThreadFactory(waveformGeneratorThreadName)).submit(() -> {
            try {
                if (FileUtil.validateExtension(wavOrMp3FileReference.get(), Extension.MP3.getExtension())) {
                    Future<Optional<File>> futureWav = AudioUtil.mp3ToWav(wavOrMp3FileReference.get());
                    while (!futureWav.isDone()) Thread.onSpinWait();
                    if (futureWav.get().isPresent()) wavOrMp3FileReference.set(futureWav.get().get());
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            if (FileUtil.validateExtension(wavOrMp3FileReference.get(), Extension.MP3.getExtension())) {
                throw new FatalException("Failed to convert mp3 to wav");
            }

            BufferedImage ret =
                    new BufferedImage(widthReference.get(), heightReference.get(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = ret.createGraphics();

            WaveFile wav;
            try {
                wav = new WaveFile(wavOrMp3FileReference.get());
            } catch (Exception e) {
                throw new FatalException(e.getMessage());
            }

            int numFrames = (int) wav.getNumFrames();
            if (numFrames < widthReference.get()) {
                widthReference.set(numFrames);
            }

            int[] nonNormalizedSamples = new int[widthReference.get()];

            int sampleLocationIncrement = (int) Math.ceil(numFrames / (double) widthReference.get());

            int maxAmplitude = 0;
            int nonNormalizedSamplesIndex = 0;
            for (int i = 0 ; i < numFrames ; i += sampleLocationIncrement) {
                maxAmplitude = Math.max(maxAmplitude, wav.getSample(i));

                nonNormalizedSamples[nonNormalizedSamplesIndex] = wav.getSample(i);
                nonNormalizedSamplesIndex++;
            }

            g2d.setPaint(backgroundColor);
            g2d.fillRect(0, 0, widthReference.get(), heightReference.get());
            g2d.setColor(waveColor);

            int[] normalizedSamples = new int[widthReference.get()];
            for (int i = 0 ; i < nonNormalizedSamples.length ; i++) {
                int normalizedValue = (int) ((nonNormalizedSamples[i] / (double) maxAmplitude) * heightReference.get());
                if (normalizedValue > heightReference.get() / 2) normalizedValue = interpolationNeededValue;
                normalizedSamples[i] = normalizedValue;
            }

            // Loop through samples and interpolate where needed
            for (int i = 0 ; i < normalizedSamples.length ; i++) {
                int currentSample = normalizedSamples[i];
                if (currentSample != interpolationNeededValue) continue;

                int nextValue = 0;
                for (int j = i ; j < normalizedSamples.length ; j++) {
                    int currentNextValue = normalizedSamples[j];
                    if (currentNextValue != interpolationNeededValue) {
                        nextValue = currentNextValue;
                        break;
                    }
                }

                // Last value that isn't an interpolation value
                int lastValue = 0;
                for (int j = i ; j >= 0 ; j--) {
                    int currentLastValue = normalizedSamples[j];
                    if (currentLastValue != interpolationNeededValue) {
                        lastValue = currentLastValue;
                        break;
                    }
                }

                normalizedSamples[i] = (nextValue + lastValue) / 2;
            }

            // Draw center line
            for (int i = 0 ; i < widthReference.get() ; i++) {
                g2d.drawLine(i, heightReference.get() / 2, i, heightReference.get() / 2);
            }

            // Paint wave extending upwards and downwards
            for (int i = 0 ; i < normalizedSamples.length ; i++) {
                g2d.drawLine(i, heightReference.get() / 2, i, heightReference.get() / 2 + normalizedSamples[i]);
                g2d.drawLine(i, heightReference.get() / 2 - normalizedSamples[i], i, heightReference.get() / 2);
            }

            wavOrMp3FileReference.set(null);
            wav.stop();
            wav = null;

            return ret;
        });
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
        Preconditions.checkArgument(mp3OrWavFile.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(mp3OrWavFile));
        Preconditions.checkNotNull(onSaveRunnable);

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(audioWaveformPreviewLabelGeneratorThreadName)).submit(() -> {
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
     * Generates and returns a file preview for the provided image file.
     *
     * @param imageFile      the image file
     * @param onSaveRunnable the runnable to invoke when the save button is pressed
     * @return the label with an image preview and save button
     */
    public static JLabel generateImagePreviewLabel(File imageFile, Runnable onSaveRunnable) {
        Preconditions.checkNotNull(imageFile);
        Preconditions.checkArgument(imageFile.exists());
        Preconditions.checkArgument(imageFile.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedImageExtension(imageFile));
        Preconditions.checkNotNull(onSaveRunnable);

        BufferedImage readImage;
        try {
            readImage = ImageUtil.read(imageFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            throw new FatalException("Failed to read image: " + imageFile.getAbsolutePath());
        }

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
        ret.setSize(IMAGE_PREVIEW_LEN, IMAGE_PREVIEW_BUTTON_HEIGHT + IMAGE_PREVIEW_LEN);

        return ret;
    }
}
