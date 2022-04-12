package cyder.utilities;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.enums.DynamicDirectory;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities related to audio files, typically MP3 and wav files.
 */
public class AudioUtil {
    /**
     * The ffmpeg input flag.
     */
    public static final String INPUT_FLAG = "-i";

    /**
     * The primary name for the youtube-dl binary.
     */
    public static final String YOUTUBE_DL = "youtube-dl";

    /**
     * The primary name for the ffmpeg binary.
     */
    public static final String FFMPEG = "ffmpeg";

    /**
     * Suppress default constructor.
     */
    private AudioUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Converts the mp3 file to a wav file and returns the file object.
     * Note the file is creatd in the Cyder temporary directory which is
     * removed upon proper Cyder shutdown/startup.
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
                    DynamicDirectory.DYNAMIC_PATH,
                    "tmp", FileUtil.getFilename(mp3File) + ".wav")).getAbsolutePath();
            String safePath = "\"" + builtPath + "\"";

            File outputFile = new File(builtPath);
            ProcessBuilder pb = new ProcessBuilder(getFfmpegCommand(), INPUT_FLAG,
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
                    DynamicDirectory.DYNAMIC_PATH,
                    "tmp", FileUtil.getFilename(wavFile) + ".mp3")).getAbsolutePath();
            String safePath = "\"" + builtPath + "\"";

            File outputFile = new File(builtPath);
            ProcessBuilder pb = new ProcessBuilder(getFfmpegCommand(), INPUT_FLAG,
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
                    // couldn't convert so return an empty
                    // optional, caller should check for this
                    return Optional.empty();
                }
            }

            // in case the audio wav name contains spaces, surround with quotes
            String safeFilename = "\"" + usageFile.getAbsolutePath() + "\"";

            File outputFile =  OSUtil.buildFile(DynamicDirectory.DYNAMIC_PATH,
                    "tmp", FileUtil.getFilename(usageFile) + "_Dreamy.wav");

            ProcessBuilder pb = new ProcessBuilder(
                    getFfmpegCommand(),
                    INPUT_FLAG,
                    safeFilename,
                    "-filter:a",
                    "\"highpass=f=2, lowpass=f=300\"",
                    "\"" + outputFile.getAbsolutePath() + "\"");
            Process p = pb.start();

            // get original time of wav (after process started to save time)
            Future<Integer> startingMilis = getMillis(usageFile);
            while (!startingMilis.isDone()) {
                Thread.onSpinWait();
            }

            // wait for file to be created by ffmpeg
            while (!outputFile.exists()) {
                Thread.onSpinWait();
            }

            // wait until length is equal to the original length
            while (true) {
                Future<Integer> updatedLen = getMillis(outputFile);

                while (!updatedLen.isDone()) {
                    Thread.onSpinWait();
                }

                if (updatedLen.get().equals(startingMilis.get())) {
                    break;
                }

                Thread.sleep(500);
            }

            // return dreamified wav
            return Optional.of(outputFile);
        });
    }

    /**
     * Uses ffprobe to get the length of the audio file in milliseconds.
     *
     * @param audioFile the audio file to get the length of
     * @return the length of the audio file in milliseconds
     */
    public static Future<Integer> getMillis(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        Preconditions.checkArgument(FileUtil.validateExtension(audioFile, ".wav")
                || FileUtil.validateExtension(audioFile, ".mp3"));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Audio Length Finder: "
                        + FileUtil.getFilename(audioFile))).submit(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(getFfprobeCommand(), INPUT_FLAG,
                        "\"" + audioFile.getAbsolutePath() + "\"", "-show_format");
                Process p = pb.start();

                // another precaution to ensure process is completed before file is returned
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (line.matches("\\s*duration=.*\\s*")) {
                        return (int) (Double.parseDouble(
                                line.replace("duration=", "").trim()) * 1000);
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            // return values are auto boxed.
            return -1;
        });
    }

    /**
     * Returns whether ffmpeg is installed by attempting
     * validation on the set path to the exe and attempting
     * to invoke ffmpeg in the console.
     *
     * @return whether ffmpeg is installed
     */
    public static boolean ffmpegInstalled() {
        // check for the binary first being set in the Windows PATH
        if (OSUtil.isBinaryInstalled(FFMPEG)) {
            return true;
        }

        // finally check dynamic/exes to see if an ffmpeg binary exists there
        return OSUtil.isBinaryInExes(FFMPEG + ".exe");
    }

    /**
     * Returns whether youtube-dl is installed by attempting
     * validation on the set path to the exe and attempting
     * to invoke youtube-dl in the console.
     *
     * @return whether youtube-dl is installed
     */
    public static boolean youtubeDlInstalled() {
        // check for the binary first being set in the Windows PATH
        if (OSUtil.isBinaryInstalled(YOUTUBE_DL)) {
            return true;
        }

        // finally check dynamic/exes to see if a youtube-dl binary exists there
        return OSUtil.isBinaryInExes(YOUTUBE_DL + ".exe");
    }

    /**
     * Returns whether ffprobe is installed.
     *
     * @return whether ffprobe is installed
     */
    public static boolean ffprobeInstalled() {
        return OSUtil.isBinaryInstalled("ffprobe")
                || OSUtil.isBinaryInExes("ffprobe.exe") ;
    }

    /**
     * Returns the command to invoke ffmpeg provided the
     * binary exists and can be found.
     *
     * @return the ffmpeg command
     */
    public static String getFfmpegCommand() {
        Preconditions.checkArgument(ffmpegInstalled());

        return OSUtil.isBinaryInstalled(FFMPEG) ? FFMPEG
                : OSUtil.buildPath(DynamicDirectory.DYNAMIC_PATH,
                         DynamicDirectory.EXES.getDirectoryName(), FFMPEG + ".exe");
    }

    /**
     * Returns the command to invoke youtube-dl provided the
     * binary exists and can be found.
     *
     * @return the youtube-dl command
     */
    public static String getYoutubeDlCommand() {
        Preconditions.checkArgument(youtubeDlInstalled());

        return OSUtil.isBinaryInstalled(YOUTUBE_DL) ? YOUTUBE_DL
                : OSUtil.buildPath(DynamicDirectory.DYNAMIC_PATH,
                DynamicDirectory.EXES.getDirectoryName(), YOUTUBE_DL + ".exe");
    }


    /**
     * Returns the base ffprobe command.
     *
     * @return the base ffprobe command
     */
    public static String getFfprobeCommand() {
        Preconditions.checkArgument(ffprobeInstalled());

        if (OSUtil.isBinaryInstalled("ffprobe")) {
            return "ffprobe";
        } else {
            return OSUtil.buildPath(
                    DynamicDirectory.DYNAMIC_PATH,
                    DynamicDirectory.EXES.getDirectoryName(),
                    "ffprobe.exe");
        }
    }

    /**
     * Downlaods ffmpeg, ffplay, and ffprobe to the exes dynamic
     * directory and sets the user path for ffmpeg to the one in dynamic.
     *
     * @return whether the download was successful
     */
    public static boolean downloadFfmpegStack() {
        // todo

        // get url from backend

        // download zip

        // extract zip into exes inside of exes/

        // delete old zip

        return false;
    }

    /**
     * Returns a pretty representation of the provided number of seconds
     * using hours, minutes, and seconds.
     *
     * Example: 3661 would return "1h 1m 1s".
     *
     * @param seconds the amount of seconds
     * @return the pretty representation
     */
    public static String formatSeconds(int seconds) {
        StringBuilder sb = new StringBuilder();

        int minutes = 0;
        int hours = 0;

        minutes = seconds / 60;
        seconds -= minutes * 60;

        hours = minutes / 60;
        minutes -= hours * 60;

        if (hours > 0) {
            sb.append(hours).append("h ");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }

        if (sb.toString().isEmpty() || seconds != 0) {
            sb.append(seconds).append("s ");
        }

        return StringUtil.getTrimmedText(sb.toString());
    }
}
