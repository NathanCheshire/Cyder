package main.java.cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import main.java.cyder.console.Console;
import main.java.cyder.enums.Dynamic;
import main.java.cyder.enums.Extension;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.files.FileUtil;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.network.NetworkUtil;
import main.java.cyder.process.Program;
import main.java.cyder.snakes.PythonArgument;
import main.java.cyder.snakes.PythonCommand;
import main.java.cyder.snakes.PythonFunctionsWrapper;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.threads.CyderThreadFactory;
import main.java.cyder.threads.ThreadUtil;
import main.java.cyder.time.TimeUtil;
import main.java.cyder.user.UserFile;
import main.java.cyder.utils.OsUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static main.java.cyder.strings.CyderStrings.*;

/**
 * Utilities related to audio files, typically mp3 and wav files.
 */
public final class AudioUtil {
    /**
     * The resource link to download the ffmpeg binary.
     */
    public static final String DOWNLOAD_RESOURCE_FFMPEG
            = "https://github.com/NathanCheshire/Cyder/raw/main/resources/ffmpeg.zip";

    /**
     * The resource link to download the ffplay binary.
     */
    public static final String DOWNLOAD_RESOURCE_FFPLAY
            = "https://github.com/NathanCheshire/Cyder/raw/main/resources/ffplay.zip";

    /**
     * The resource link to download the ffprobe binary.
     */
    public static final String DOWNLOAD_RESOURCE_FFPROBE
            = "https://github.com/NathanCheshire/Cyder/raw/main/resources/ffprobe.zip";

    /**
     * The resource link to download the youtube-dl binary.
     */
    public static final String DOWNLOAD_RESOURCE_YOUTUBE_DL
            = "https://github.com/NathanCheshire/Cyder/raw/main/resources/youtube-dl.zip";

    /**
     * The ffmpeg input flag.
     */
    private static final String INPUT_FLAG = "-i";

    /**
     * The dreamified file suffix to append to music files after dreamifying them.
     */
    public static final String DREAMY_SUFFIX = "_Dreamy";

    /**
     * The highpass value for dreamifying an audio file.
     */
    private static final int HIGHPASS = 2;

    /**
     * The lowpass value for dreamifying an audio file.
     */
    private static final int LOWPASS = 300;

    /**
     * The audio dreamifier thread name prefix.
     */
    private static final String audioDreamifierThreadNamePrefix = "Audio Dreamifier: ";

    /**
     * The -filter:a flag for setting high and low pass data.
     */
    private static final String FILTER_DASH_A = "-filter:a";

    /**
     * The high and low pass argument string.
     */
    private static final String HIGHPASS_LOWPASS_ARGS = quote + "highpass=f="
            + HIGHPASS + comma + space + "lowpass=f=" + LOWPASS + quote;

    /**
     * The delay between polling milliseconds when dreamifying an audio.
     */
    private static final int pollMillisDelay = 500;

    /**
     * The pattern used to find the duration of an audio file from ffprobe.
     */
    private static final Pattern durationPattern = Pattern.compile("\\s*duration=.*\\s*");

    /**
     * The thread name for the ffmpeg downloader
     */
    private static final String FFMPEG_DOWNLOADER_THREAD_NAME = "FFMPEG Downloader";

    /**
     * A record to associate a destination file with a url to download the file, typically a zip archive, from.
     */
    private record PairedFile(File file, String url) {}

    /**
     * The name of the thread that downloads youtube-dl if missing and needed.
     */
    private static final String YOUTUBE_DL_DOWNLOADER_THREAD_NAME = "YouTubeDl Downloader";

    /**
     * Suppress default constructor.
     */
    private AudioUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Converts the mp3 file to a wav file and returns the file object.
     * Note the file is created in the Cyder temporary directory which is
     * removed upon proper Cyder shutdown/startup.
     *
     * @param mp3File the mp3 file to convert to wav
     * @return the mp3 file converted to wav
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static Future<Optional<File>> mp3ToWav(File mp3File) {
        Preconditions.checkNotNull(mp3File);
        Preconditions.checkArgument(FileUtil.validateExtension(mp3File, Extension.MP3.getExtension()));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Mp3 to wav converter")).submit(() -> {
            String builtPath = Dynamic.buildDynamic(Dynamic.TEMP.getFileName(),
                    FileUtil.getFilename(mp3File) + Extension.WAV.getExtension()).getAbsolutePath();
            String safePath = quote + builtPath + quote;

            File outputFile = new File(builtPath);
            ProcessBuilder pb = new ProcessBuilder(getFfmpegCommand(), INPUT_FLAG,
                    quote + mp3File.getAbsolutePath() + quote, safePath);
            pb.redirectErrorStream();
            Process p = pb.start();

            // another precaution to ensure process is completed before file is returned
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (reader.readLine() != null) {
                Thread.onSpinWait();
            }

            // wait for file to be created by ffmpeg
            while (!outputFile.exists()) {
                Thread.onSpinWait();
            }

            return Optional.of(outputFile);
        });
    }

    /**
     * Converts the wav file to an mp3 file and returns the file object.
     * Note the file is created in the Cyder temporary directory which is
     * removed upon proper Cyder shutdown/startup.
     *
     * @param wavFile the wav file to convert to mp3
     * @return the wav file converted to mp3
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Future<Optional<File>> wavToMp3(File wavFile) {
        Preconditions.checkNotNull(wavFile);
        Preconditions.checkArgument(FileUtil.validateExtension(wavFile, Extension.WAV.getExtension()));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Wav to mp3 converter")).submit(() -> {

            String builtPath = Dynamic.buildDynamic(Dynamic.TEMP.getFileName(),
                    FileUtil.getFilename(wavFile) + Extension.MP3.getExtension()).getAbsolutePath();
            String safePath = quote + builtPath + quote;

            File outputFile = new File(builtPath);
            ProcessBuilder pb = new ProcessBuilder(getFfmpegCommand(), INPUT_FLAG,
                    quote + wavFile.getAbsolutePath() + quote, safePath);
            pb.redirectErrorStream();
            Process p = pb.start();

            // another precaution to ensure process is completed before file is returned
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (reader.readLine() != null) {
                Thread.onSpinWait();
            }

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
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(wavOrMp3File));

        String executorThreadName = audioDreamifierThreadNamePrefix + FileUtil.getFilename(wavOrMp3File);

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(executorThreadName)).submit(() -> {

            // in case the audio wav name contains spaces, surround with quotes
            String safeFilename = quote + wavOrMp3File.getAbsolutePath() + quote;

            File outputFile = Dynamic.buildDynamic(Dynamic.TEMP.getFileName(),
                    FileUtil.getFilename(wavOrMp3File) + DREAMY_SUFFIX + Extension.MP3.getExtension());
            String safeOutputFilename = quote + outputFile.getAbsolutePath() + quote;

            String[] command = {
                    getFfmpegCommand(),
                    INPUT_FLAG,
                    safeFilename,
                    FILTER_DASH_A,
                    HIGHPASS_LOWPASS_ARGS,
                    safeOutputFilename};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            Future<Integer> originalFileMillis = getMillisFfprobe(wavOrMp3File);
            while (!originalFileMillis.isDone()) Thread.onSpinWait();

            while (!outputFile.exists()) Thread.onSpinWait();

            while (true) {
                Future<Integer> updatedLen = getMillisFfprobe(outputFile);
                while (!updatedLen.isDone()) Thread.onSpinWait();
                if (updatedLen.get().equals(originalFileMillis.get())) break;
                ThreadUtil.sleep(pollMillisDelay);
            }

            int exitValue = process.exitValue();
            if (exitValue != 0) {
                return Optional.empty();
            }

            return Optional.of(outputFile);
        });
    }

    /**
     * Uses ffprobe to get the length of the audio file in milliseconds.
     * If the duration cannot be found, -1 is returned.
     *
     * @param audioFile the audio file to find the length of in milliseconds
     * @return the length of the audio file in milliseconds
     */
    private static Future<Integer> getMillisFfprobe(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("getMillisFfprobe, file"
                        + colon
                        + space
                        + quote + FileUtil.getFilename(audioFile) + quote)).submit(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(getFfprobeCommand(),
                        INPUT_FLAG,
                        quote + audioFile.getAbsolutePath() + quote,
                        "-show_format");

                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (durationPattern.matcher(line).matches()) {
                        line = line.replace("duration=", "").trim();
                        return (int) (Double.parseDouble(line) * 1000);
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
        if (Program.FFMPEG.isInstalled()) return true;

        return OsUtil.isBinaryInExes(Program.FFMPEG.getProgramName()
                + Extension.EXE.getExtension());
    }

    /**
     * Returns whether youtube-dl is installed by attempting
     * validation on the set path to the exe and attempting
     * to invoke youtube-dl in the console.
     *
     * @return whether youtube-dl is installed
     */
    public static boolean youtubeDlInstalled() {
        if (Program.YOUTUBE_DL.isInstalled()) return true;

        return OsUtil.isBinaryInExes(Program.YOUTUBE_DL.getProgramName()
                + Extension.EXE.getExtension());
    }

    /**
     * Returns whether ffprobe is installed.
     *
     * @return whether ffprobe is installed
     */
    public static boolean ffprobeInstalled() {
        return Program.FFPROBE.isInstalled() || OsUtil.isBinaryInExes(Program.FFPROBE.getFilename());
    }

    /**
     * Returns the command to invoke ffmpeg provided the
     * binary exists and can be found.
     *
     * @return the ffmpeg command
     */
    public static String getFfmpegCommand() {
        Preconditions.checkArgument(ffmpegInstalled());

        return Program.FFMPEG.isInstalled()
                ? Program.FFMPEG.getProgramName()
                : Dynamic.buildDynamic(Dynamic.EXES.getFileName(), Program.FFPROBE.getFilename()).getAbsolutePath();

    }

    /**
     * Returns the command to invoke youtube-dl provided the
     * binary exists and can be found.
     *
     * @return the youtube-dl command
     */
    public static String getYoutubeDlCommand() {
        Preconditions.checkArgument(youtubeDlInstalled());

        return Program.YOUTUBE_DL.isInstalled()
                ? Program.YOUTUBE_DL.getProgramName()
                : Dynamic.buildDynamic(Dynamic.EXES.getFileName(), Program.YOUTUBE_DL.getFilename()).getAbsolutePath();
    }

    /**
     * Returns the base ffprobe command.
     *
     * @return the base ffprobe command
     */
    public static String getFfprobeCommand() {
        Preconditions.checkArgument(ffprobeInstalled());

        if (Program.FFPROBE.isInstalled()) return Program.FFPROBE.getProgramName();

        return Dynamic.buildDynamic(Dynamic.EXES.getFileName(),
                Program.FFPROBE.getProgramName() + Extension.EXE.getExtension()).getAbsolutePath();
    }

    /**
     * Downloads ffmpeg, ffplay, and ffprobe to the exes dynamic
     * directory and sets the user path for ffmpeg to the one in dynamic.
     *
     * @return whether the download was successful
     */
    public static Future<Boolean> downloadFfmpegStack() {
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(FFMPEG_DOWNLOADER_THREAD_NAME)).submit(() -> {
            ImmutableList<PairedFile> downloadZips = ImmutableList.of(
                    new PairedFile(Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFMPEG.getProgramName()
                                    + Extension.ZIP.getExtension()), DOWNLOAD_RESOURCE_FFMPEG),
                    new PairedFile(Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFPROBE.getProgramName()
                                    + Extension.ZIP.getExtension()), DOWNLOAD_RESOURCE_FFPROBE),
                    new PairedFile(Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFPLAY.getProgramName()
                                    + Extension.ZIP.getExtension()), DOWNLOAD_RESOURCE_FFPLAY)
            );

            for (PairedFile pairedZipFile : downloadZips) {
                NetworkUtil.downloadResource(pairedZipFile.url(), pairedZipFile.file());

                while (!pairedZipFile.file().exists()) {
                    Thread.onSpinWait();
                }

                File extractFolder = Dynamic.buildDynamic(Dynamic.EXES.getFileName());
                FileUtil.unzip(pairedZipFile.file(), extractFolder);
                OsUtil.deleteFile(pairedZipFile.file());
            }

            ImmutableList<File> resultingFiles = ImmutableList.of(
                    Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFMPEG.getFilename()),
                    Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFPROBE.getFilename()),
                    Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFPLAY.getFilename())
            );

            return resultingFiles.stream().filter(File::exists).count() == downloadZips.size();
        });
    }

    /**
     * Downloads the youtube-dl binary from the remote resources.
     * Returns whether the download was successful.
     *
     * @return whether youtube-dl could be downloaded from the remote resources
     */
    public static Future<Boolean> downloadYoutubeDl() {
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(YOUTUBE_DL_DOWNLOADER_THREAD_NAME)).submit(() -> {
            File downloadZip = Dynamic.buildDynamic(
                    Dynamic.EXES.getFileName(), Program.YOUTUBE_DL.getProgramName()
                            + Extension.ZIP.getExtension());

            NetworkUtil.downloadResource(DOWNLOAD_RESOURCE_YOUTUBE_DL, downloadZip);

            while (!downloadZip.exists()) {
                Thread.onSpinWait();
            }

            File extractFolder = Dynamic.buildDynamic(Dynamic.EXES.getFileName());

            FileUtil.unzip(downloadZip, extractFolder);
            OsUtil.deleteFile(downloadZip);

            return Dynamic.buildDynamic(Dynamic.EXES.getFileName(),
                    Program.YOUTUBE_DL.getProgramName() + Extension.EXE.getExtension()).exists();
        });
    }

    /**
     * A cache of previously computed millisecond times from audio files.
     */
    private static final ConcurrentHashMap<File, Integer> milliTimes = new ConcurrentHashMap<>();

    /**
     * Returns the number of milliseconds in an audio file using the python dependency Mutagen.
     *
     * @param audioFile the audio file to return the duration of
     * @return the duration of the provided audio file in milliseconds
     */
    public static Future<Integer> getMillisMutagen(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PYTHON.getProgramName()));

        if (milliTimes.containsKey(audioFile)) {
            return Futures.immediateFuture(milliTimes.get(audioFile));
        }

        String threadName = "getMillisMutagen, file" + colon + space + quote + audioFile + quote;
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
            String command = PythonArgument.COMMAND.getFullArgument()
                    + CyderStrings.space + PythonCommand.AUDIO_LENGTH.getCommand()
                    + CyderStrings.space + PythonArgument.INPUT.getFullArgument()
                    + CyderStrings.space + quote + audioFile.getAbsolutePath() + quote;

            Future<String> futureResult = PythonFunctionsWrapper.invokeCommand(command);
            while (!futureResult.isDone()) Thread.onSpinWait();

            String result = futureResult.get();
            String parsedResult = PythonCommand.AUDIO_LENGTH.parseResponse(result);

            int millis = (int) (Float.parseFloat(parsedResult) * TimeUtil.MILLISECONDS_IN_SECOND);
            milliTimes.put(audioFile, millis);

            return millis;
        });
    }

    /**
     * Returns the total bytes of the file if found. Zero else.
     *
     * @param file the file to find the total bytes of
     * @return the total bytes of the file
     */
    public static long getTotalBytes(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        try {
            return new FileInputStream(file).available();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return 0L;
    }

    /**
     * Returns a reference to the current user's music file with the provided name if found, empty optional else.
     *
     * @param title the title of the music file to search for
     * @return an optional reference to the requested music file
     */
    public static Optional<File> getCurrentUserMusicFileWithName(String title) {
        Preconditions.checkNotNull(title);
        Preconditions.checkArgument(!title.isEmpty());

        File[] files = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(), UserFile.MUSIC.getName()).listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (FileUtil.getFilename(file).equalsIgnoreCase(title)) {
                    return Optional.of(file);
                }
            }
        }

        return Optional.empty();
    }
}
