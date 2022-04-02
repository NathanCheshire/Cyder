package cyder.utilities;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities related to audio files, typically MP3 and wav files.
 */
public class AudioUtil {
    /**
     * The ffmpeg command
     */
    public static final String FFMPEG = "FFMPEG";

    /**
     * The ffmpeg input flag.
     */
    public static final String INPUT_FLAG = "-i";

    /**
     * Suppress default constructor.
     */
    private AudioUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Returns the length of the audio file in milliseconds.
     *
     * @param audioFile the audio file, must be in mp3 or wav format.
     * @return the length of the audio file in milliseconds
     */
    public static int millisLength(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(FileUtil.validateExtension(audioFile, ".wav")
                || FileUtil.validateExtension(audioFile, ".mp3"));

        int milisRet = 0;

        try {
            FileInputStream fis = new FileInputStream(audioFile);
            Bitstream bitstream = new Bitstream(fis);
            Header h = bitstream.readFrame();
            long tn = fis.getChannel().size();
            milisRet = (int) h.total_ms((int) tn);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return milisRet;
    }

    /**
     * Converts the mp3 file to a wav file and returns the file object.
     * Note the file is creatd in the Cyder temporary directory which is
     * removed upon proper Cyder shutdown/startup.
     *
     *
     * @param mp3File the mp3 file to convert to wav
     * @return the mp3 file converted to wav
     */
    public static Future<Optional<File>> mp3ToWav(File mp3File) {
        Preconditions.checkNotNull(mp3File);
        Preconditions.checkArgument(FileUtil.validateExtension(mp3File, ".mp3"));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Mp3 to wav converter")).submit(() -> {
            String builtPath = new File(OSUtil.buildPath(
                    "dynamic", "tmp", FileUtil.getFilename(mp3File) + ".wav")).getAbsolutePath();
            String safePath = "\"" + builtPath + "\"";

            File outputFile = new File(builtPath);
            ProcessBuilder pb = new ProcessBuilder(FFMPEG, INPUT_FLAG,
                    "\"" + mp3File.getAbsolutePath() + "\"", safePath);
            pb.redirectErrorStream();
            Process p = pb.start();

            // another precaution to ensure process is completed before file is returned
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {}

            // wait for file to be created by ffmpeg
            while (!outputFile.exists()) {
                Thread.onSpinWait();
            }

            return Optional.of(outputFile);
        });
    }

    /**
     * Converts the wav file to an mp3 file and returns the file object.
     * Note the file is creatd in the Cyder temporary directory which is
     * removed upon proper Cyder shutdown/startup.
     *
     * @param wavFile the wav file to convert to mp3
     * @return the wav file converted to mp3
     */
    public static Future<Optional<File>> wavToMp3(File wavFile) {
        Preconditions.checkNotNull(wavFile);
        Preconditions.checkArgument(FileUtil.validateExtension(wavFile, ".wav"));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Wav to mp3 converter")).submit(() -> {

            String builtPath = new File(OSUtil.buildPath(
                    "dynamic", "tmp", FileUtil.getFilename(wavFile) + ".mp3")).getAbsolutePath();
            String safePath = "\"" + builtPath + "\"";

            File outputFile = new File(builtPath);
            ProcessBuilder pb = new ProcessBuilder(FFMPEG, INPUT_FLAG,
                    "\"" + wavFile.getAbsolutePath() + "\"", safePath);
            pb.redirectErrorStream();
            Process p = pb.start();

            // another precaution to ensure process is completed before file is returned
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {}

            // wait for file to be created by ffmpeg
            while (!outputFile.exists()) {
                Thread.onSpinWait();
            }

            return Optional.of(outputFile);
        });
    }
}
