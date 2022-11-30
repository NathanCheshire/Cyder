package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utilities related to playing mp4 files in a Cyder frame.
 * Video killed the radio star.
 */
public final class VideoUtil {
    /** Suppress default constructor. */
    private VideoUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    // ffmpeg -i BadApple.mp4 "%04d.png"

    @SuppressWarnings("UnusedAssignment") /* Optimizations */
    public static void test() {
        File audioFile = new File("c:\\users\\nathan\\downloads\\BadApple.mp3");
        //int milliSeconds = AudioUtil.getMillisFast(audioFile);
        int milliSeconds = 300000;

        int numFrames = 6572;
        int milliSecondsPerFrame = milliSeconds / numFrames;

        int width = 960;
        int height = 720;
        CyderFrame cyderFrame = new CyderFrame(width, height);
        cyderFrame.setTitle("Bad Apple");
        cyderFrame.finalizeAndShow();

        IoUtil.playGeneralAudio(audioFile);

        long absoluteStart = System.currentTimeMillis();

        for (int i = 1 ; i <= numFrames ; i++) {
            if (cyderFrame.isDisposed()) return;

            File frameFile = new File("C:\\users\\nathan\\Downloads\\Frames\\"
                    + String.format("%04d", i) + Extension.PNG.getExtension());

            BufferedImage image;
            try {
                image = ImageUtil.read(frameFile);
            } catch (Exception ignored) {
                return;
            }
            cyderFrame.setBackground(image);

            frameFile = null;
            image = null;
            long totalTimeTaken = (System.currentTimeMillis() - absoluteStart);
            long timeThatShouldHaveElapsed = (long) milliSecondsPerFrame * i;
            long sleepTime = timeThatShouldHaveElapsed - totalTimeTaken;
            if (sleepTime >= 0) {
                ThreadUtil.sleep(sleepTime);
            } else {
                // ("Need to skip frames, behind: " + Math.abs(sleepTime) + "ms");
            }
        }
    }

    /** Plays the provided mp4 file. */
    public static void playMp4(File mp4File) {
        Preconditions.checkNotNull(mp4File);
        Preconditions.checkArgument(mp4File.exists());

        // todo
    }
}
