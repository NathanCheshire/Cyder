package cyder.messaging;

import com.google.common.base.Preconditions;
import cyder.audio.WaveFile;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import javax.imageio.ImageIO;
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
    private static final int DEFAULT_IMAGE_WIDTH = 1800;

    /**
     * The default image height.
     */
    private static final int DEAULT_IMAGE_HEIGHT = 280;

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

    // todo I want a bass boost feature for an mp3 and wav file

    public static void main (String[] args) {
        BufferedImage ret = new BufferedImage(DEFAULT_IMAGE_WIDTH, DEAULT_IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = ret.createGraphics();

        // samples is width minus padding on each side of resulting image
        int samples = DEFAULT_IMAGE_WIDTH - 5 * 2;

        File tmpWav = new File("C:/users/nathan/Downloads/SquidGames.wav");

        if (tmpWav.exists()) {
            WaveFile wav = new WaveFile(tmpWav);

            int numSamples = DEFAULT_IMAGE_WIDTH;
            int numFrames = (int) wav.getNumFrames();
            int[] foundSamples = new int[numSamples];

            if (numSamples > numFrames) {
                System.out.println("Error");
            } else {
                int sampleLocInc = (int) Math.ceil(numFrames / (double) numSamples);
                int currentSampleInc = 0;
                int currentSampleIndex = 0;

                int max = 0;

                for (int i = 0; i < wav.getNumFrames(); i++) {
                    max = Math.max(max, wav.getSampleInt(i));

                    if (i == currentSampleInc) {
                        foundSamples[currentSampleIndex] = wav.getSampleInt(i);

                        currentSampleInc += sampleLocInc;
                        currentSampleIndex++;
                    }
                }

                g2d.setPaint(Color.WHITE);
                g2d.fillRect(0,0, DEFAULT_IMAGE_WIDTH, DEAULT_IMAGE_HEIGHT);

                g2d.setColor(CyderColors.navy);

                double[] normalizedValues = new double[numSamples];

                for (int i = 0 ; i < numSamples ; i++) {
                    int normalized = (int) ((foundSamples[i] / (double) max) * DEAULT_IMAGE_HEIGHT);

                    // todo need to linearly interpolate these values with slight variation
                    if (normalized > DEAULT_IMAGE_HEIGHT / 2)
                        continue;

                    g2d.drawLine(i, DEAULT_IMAGE_HEIGHT / 2, i,
                            DEAULT_IMAGE_HEIGHT / 2 + normalized);

                    g2d.drawLine(i, DEAULT_IMAGE_HEIGHT / 2 - normalized,
                             i, DEAULT_IMAGE_HEIGHT / 2);
                }

                try {
                   ImageIO.write(ret, "png", new File("c:/users/nathan/downloads/out.png"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = ret.createGraphics();

        // todo copy from above when testing complete

        return ret;
    }
}
