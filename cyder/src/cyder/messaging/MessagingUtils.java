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

    // todo some stuff should ideally go to audio util like an mp3 to wav and vice versa methods

    // todo I want a bass boost feature for an mp3 and wav file

    public static void main (String[] args) {
        BufferedImage ret = new BufferedImage(DEFAULT_IMAGE_WIDTH, DEAULT_IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = ret.createGraphics();

        File tmpWav = new File("C:/users/nathan/Downloads/SquidGames.wav");

        if (tmpWav.exists()) {
            WaveFile wav = new WaveFile(tmpWav);

            int numSamples = DEFAULT_IMAGE_WIDTH;
            int numFrames = (int) wav.getNumFrames();
            int[] foundSamples = new int[numSamples];

            if (numSamples > numFrames) {
                System.out.println("Error"); //todo
            } else {
                int sampleLocInc = (int) Math.ceil(numFrames / (double) numSamples);
                int currentSampleInc = 0;
                int currentSampleIndex = 0;

                int max = 0;

                // find the max and add to the samples
                for (int i = 0; i < wav.getNumFrames(); i++) {
                    max = Math.max(max, wav.getSampleInt(i));

                    if (i == currentSampleInc) {
                        foundSamples[currentSampleIndex] = wav.getSampleInt(i);

                        currentSampleInc += sampleLocInc;
                        currentSampleIndex++;
                    }
                }

                // paint background of image
                g2d.setPaint(Color.WHITE);
                g2d.fillRect(0,0, DEFAULT_IMAGE_WIDTH, DEAULT_IMAGE_HEIGHT);

                // set to line color
                g2d.setColor(CyderColors.navy);

                // actual y values for painting
                int[] normalizedValues = new int[numSamples];

                for (int i = 0 ; i < numSamples ; i++) {
                    int normalizedValue = (int) ((foundSamples[i] / (double) max) * DEAULT_IMAGE_HEIGHT);

                    // if extending beyond bounds of our image, paint as zero and don't interpolate
                    if (normalizedValue > DEAULT_IMAGE_HEIGHT / 2)
                        normalizedValue = -69;

                    normalizedValues[i] = normalizedValue;
                }

                // interpolate between surrounding values where the amplitude is 0
                for (int i = 0 ; i < normalizedValues.length ; i++) {
                    // if a true zero amplitude don't paint it
                    if (normalizedValues[i] == 0)
                        continue;

                    // if we are at an amplitude of 0 that we skipped
                    else if (normalizedValues[i] == -69) {
                        // get the first value after this one that is not a 0
                        int nextNonZeroIndex = 0;

                        for (int j = i ; j < normalizedValues.length ; j++) {
                            if (normalizedValues[j] != 0 && normalizedValues[j] != -69) {
                                nextNonZeroIndex = j;
                                break;
                            }
                        }

                        int lastNonZeroIndex = 0;

                        for (int j = i ; j >= 0 ; j--) {
                            if (normalizedValues[j] != 0 && normalizedValues[j] != -69) {
                                lastNonZeroIndex = j;
                                break;
                            }
                        }

                        // average surrounding non zero values
                        int avg = (normalizedValues[nextNonZeroIndex] + normalizedValues[lastNonZeroIndex]) / 2;
                        // update current value
                        normalizedValues[i] = avg;
                    }
                }

                // paint the amplitude wave
                for (int i = 0 ; i < normalizedValues.length ; i++) {
                    // from the center line extending downwards
                    g2d.drawLine(i, DEAULT_IMAGE_HEIGHT / 2, i,
                        DEAULT_IMAGE_HEIGHT / 2 + normalizedValues[i]);

                    // from the center line extending upwards
                    g2d.drawLine(i, DEAULT_IMAGE_HEIGHT / 2 - normalizedValues[i],
                        i, DEAULT_IMAGE_HEIGHT / 2);
                }

                try {
                    // output the image todo in the future we'll return ret
                   ImageIO.write(ret, "png", new File("c:/users/nathan/downloads/out.png"));
                } catch (Exception e) {
                    // ExceptionHandler.handle(e);
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
