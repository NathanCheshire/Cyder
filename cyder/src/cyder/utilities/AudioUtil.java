package cyder.utilities;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
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
     * The ffmpeg command.
     */
    public static final String FFMPEG = "ffmpeg";

    /**
     * The ffprobe command.
     */
    public static final String FFPROBE = "ffprobe";

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
                ProcessBuilder pb = new ProcessBuilder(FFPROBE, INPUT_FLAG,
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

    // todo not making email client here, too much work and wont be used
    // instead use Cyder@nathancheshire.com email address to auto email
    // message if commit not detected for day idea

    // todo link for ffmpeg, should be stored in backend so it can be updated
    //https://objects.githubusercontent.com/github-production-release-asset-2e65be/292087234/fd12d018-4fb6-459b-8855-372df98a430c?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIWNJYAX4CSVEH53A%2F20220403%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20220403T170244Z&X-Amz-Expires=300&X-Amz-Signature=78e4fa5c6b7dade2a92f2280e66431ca004f15b5aa56dafbeb3cdc8f650e5b71&X-Amz-SignedHeaders=host&actor_id=60986919&key_id=0&repo_id=292087234&response-content-disposition=attachment%3B%20filename%3Dffmpeg-master-latest-win64-gpl.zip&response-content-type=application%2Foctet-stream

    // todo fix silence errors
    // if not on, opacity slide in, 5s pause, opacity slide out,
    // should be a specific iwnodw size with regular red background, vanila text
    // if clicked, open current log
    // use borderless frame

    // todo be able to download ffmpeg and ffprobe.exe if user confirms they want to
    // todo be able to download ffmpeg.exe and ffprobe.exe, prompt user to download and setpaths automatically
    //  OR set path via user editor, place in dynamic/exes

    // todo audio player should be able to search for songs on youtube and display preview of top 10 results
    //  and click on one to download

    // todo dreamify checkbox for audio player, will need to generate wav first time in tmp and play from that
    // after conversion finished, should be seamless audio transition

    // todo officially support mp3 and wav, will need updated code in a lot of places
    // and an method like images to check if valid
}
