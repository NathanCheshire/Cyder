package cyder.video;

import com.google.common.base.Preconditions;
import cyder.audio.AudioUtil;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;
import cyder.utils.IoUtil;
import cyder.utils.StaticUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utilities related to playing mp4 files in a Cyder frame.
 */
public final class VideoUtil {
    /**
     * Suppress default constructor.
     */
    private VideoUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @SuppressWarnings("UnusedAssignment") /* Optimizations */
    public static void test() {
        File audioFile = StaticUtil.getStaticResource("badapple.mp3");
        int milliSeconds = AudioUtil.getMillisFast(audioFile);

        int numFrames = 7777;
        int milliSecondsPerFrame = milliSeconds / numFrames;

        int width = 640;
        int height = 480;
        CyderFrame cyderFrame = new CyderFrame(width, height);
        cyderFrame.setTitle("Bad Apple");
        cyderFrame.finalizeAndShow();

        IoUtil.playGeneralAudio(audioFile);

        long starTime = System.currentTimeMillis();

        for (int i = 1 ; i <= numFrames ; i++) {
            File frameFile = new File("C:\\users\\nathan\\Downloads\\Frames\\"
                    + String.format("%04d", i) + ".png");

            BufferedImage image;
            try {
                image = ImageIO.read(frameFile);
            } catch (Exception ignored) {
                System.out.println("Failed to load image");
                return;
            }
            cyderFrame.setBackground(image);

            frameFile = null;
            System.out.println("fps: " + (System.currentTimeMillis() - starTime) / (float) i);
            ThreadUtil.sleep(milliSecondsPerFrame);
        }
    }

    /**
     * Plays the provided mp4 file.
     */
    public static void playMp4(File mp4File) {
        Preconditions.checkNotNull(mp4File);
        Preconditions.checkArgument(mp4File.exists());

        // todo
    }
}
