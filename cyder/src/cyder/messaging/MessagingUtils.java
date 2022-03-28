package cyder.messaging;

import com.google.common.base.Preconditions;
import cyder.audio.WaveFile;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.utilities.FileUtil;
import cyder.utilities.OSUtil;
import cyder.utilities.StringUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utilities related to the messaging client.
 */
public class MessagingUtils {
    /**
     * The ffmpeg command
     */
    private static final String FFMPEG = "FFMPEG";

    /**
     * The ffmpeg input flag.
     */
    private static final String INPUT_FLAG = "-i";

    /**
     * Suppress default constructor.
     */
    private MessagingUtils() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The default image width.
     */
    private static final int DEFAULT_IMAGE_WIDTH = 160;

    /**
     * The default image height.
     */
    private static final int DEAULT_IMAGE_HEIGHT = 60;

    /**
     * The default background color.
     */
    private static final Color DEFAULT_BACKGROUND_COLOR = CyderColors.vanila;

    /**
     * The default wave color.
     */
    private static final Color DEFAULT_WAVE_COLOR = CyderColors.navy;

    /**
     * Generates a png depicting the waveform of the provided mp3 file.
     *
     * @param mp3File the p3 file
     * @return the generated image
     */
    public static BufferedImage generateWaveForm(File mp3File) {
        return generateWaveForm(mp3File, DEFAULT_IMAGE_WIDTH, DEAULT_IMAGE_HEIGHT,
                DEFAULT_BACKGROUND_COLOR, DEFAULT_WAVE_COLOR);
    }

    /**
     * Generates a png depicting the waveform of the provided mp3 file.
     *
     * @param mp3File the mp3 file
     * @param width the width of the requested image
     * @param height the height of the requested image
     * @param backgroundColor the background color of the iamge
     * @param waveColor the color of the waveform
     * @return the generated image
     */
    public static BufferedImage generateWaveForm(File mp3File, int width, int height,
                                                 Color backgroundColor, Color waveColor) {
        Preconditions.checkNotNull(mp3File);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(backgroundColor);
        Preconditions.checkNotNull(waveColor);

        System.out.println(mp3File.exists());

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = ret.createGraphics();

        // samples is width minus padding on each side of resulting image
        int samples = width - 5 * 2;

        File tmpWav = OSUtil.createFileInSystemSpace(
                FileUtil.getFilename(mp3File) + ".wav");

        try {
            Runtime rt = Runtime.getRuntime();
            String command = StringUtil.separate(FFMPEG, INPUT_FLAG,
                    "\"" + mp3File.getAbsolutePath() + "\"", "\""
                            + tmpWav.getAbsolutePath() + "\"");
            Process proc = rt.exec(command);
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        }

        if (tmpWav.exists()) {
            WaveFile wav = new WaveFile(tmpWav);

            int amplitudeExample = wav.getSampleInt(140);

            for (int i = 0; i < wav.getNumFrames(); i++) {
                int amplitude = wav.getSampleInt(i);
                System.out.println(amplitude);
            }
        }

        OSUtil.delete(tmpWav);

        return ret;
    }
}
