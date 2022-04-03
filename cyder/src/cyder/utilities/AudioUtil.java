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

    // todo dreamify checkbox for audio player, will need to generate wav first time in tmp and play from that
    // after conversion finished, should be seamless audio transition

    // todo officially support mp3 and wav, will need updated code in a lot of places
    // and an method like images to check if valid

    /**
     * Dreamifies the provided wav or mp3 audio file.
     * The optional may be empty if the file could not
     * be converted if required and processed.
     *
     * @param wavOrMp3File the old file to dreamify
     * @return the dreamified wav or mp3 file
     */
    public static Future<Optional<File>> dreamifyAudio(File wavOrMp3File) {
        Preconditions.checkNotNull(wavOrMp3File);
        Preconditions.checkArgument(wavOrMp3File.exists());

        Preconditions.checkArgument(FileUtil.validateExtension(wavOrMp3File, ".wav")
                || FileUtil.validateExtension(wavOrMp3File, ".mp3"));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Audio Dreamifier: "
                        + FileUtil.getFilename(wavOrMp3File))).submit(() -> {
            File usageFile = wavOrMp3File;

            // if an mp3 file, convert usageFile to wav
            if (!FileUtil.validateExtension(usageFile, ".wav")) {
                Future<Optional<File>> wavFile = mp3ToWav(usageFile);

                while (!wavFile.isDone()) {
                    Thread.onSpinWait();
                }

                if (wavFile.get().isPresent()) {
                    usageFile = wavFile.get().get();
                } else {
                    // couldn't convert so return an empty optional
                    return Optional.empty();
                }
            }

            // in case the audio wav name contains spaces, surround with quotes
            String safeFilename = "\"" + usageFile.getAbsolutePath() + "\"";

            File outputFile =  OSUtil.buildFile("dynamic",
                    "tmp", FileUtil.getFilename(usageFile) + "_Dreamy.wav");

            ProcessBuilder pb = new ProcessBuilder(
                    FFMPEG,
                    INPUT_FLAG,
                    safeFilename,
                    "-filter:a",
                    "\"highpass=f=2, lowpass=f=300\"",
                    "\"" + outputFile.getAbsolutePath() + "\"");

            Process p = pb.start();

            // wait for file to be created by ffmpeg
            while (!outputFile.exists()) {
                Thread.onSpinWait();
            }

            // todo maybe use ffprobe to probe for same audio len?

            // return dreamified wav
            return Optional.of(outputFile);
        });
    }
}
