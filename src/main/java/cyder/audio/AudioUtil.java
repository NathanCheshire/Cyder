package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.process.Program;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadFactory;
import cyder.time.TimeUtil;
import cyder.user.UserFile;
import cyder.utils.OsUtil;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static cyder.strings.CyderStrings.*;

/**
 * Utilities related to supported audio files.
 */
public final class AudioUtil {
    /**
     * The resource link to download the ffmpeg binary.
     */
    private static final String ffmpegResourceDownload =
            "https://github.com/NathanCheshire/Cyder/raw/main/resources/ffmpeg.zip";

    /**
     * The resource link to download the ffplay binary.
     */
    private static final String ffplayResourceDownload =
            "https://github.com/NathanCheshire/Cyder/raw/main/resources/ffplay.zip";

    /**
     * The resource link to download the ffprobe binary.
     */
    private static final String ffprobeResourceDownload =
            "https://github.com/NathanCheshire/Cyder/raw/main/resources/ffprobe.zip";

    /**
     * The resource link to download the YouTube-dl binary.
     */
    private static final String youtubeDlResourceDownload =
            "https://github.com/NathanCheshire/Cyder/raw/main/resources/youtube-dl.zip";

    /**
     * The users keyword.
     */
    private static final String USERS = "users";

    /**
     * The music keyword.
     */
    private static final String MUSIC = "Music";

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
    private static final int HIGHPASS = 200;

    /**
     * The lowpass value for dreamifying an audio file.
     */
    private static final int LOWPASS = 1500;

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
     * The thread name for the ffmpeg downloader
     */
    private static final String FFMPEG_DOWNLOADER_THREAD_NAME = "FFMPEG Downloader";

    /**
     * A record to associate a destination file with a url to download the file, typically a zip archive, from.
     */
    private record PairedFile(File file, String url) {}

    /**
     * The name of the thread that downloads YouTube-dl if missing and needed.
     */
    private static final String YOUTUBE_DL_DOWNLOADER_THREAD_NAME = "YouTubeDl Downloader";

    /**
     * A cache of previously computed millisecond times from audio files.
     */
    private static final ConcurrentHashMap<File, Integer> milliTimes = new ConcurrentHashMap<>();

    /**
     * The identifying string to search for in an ffprobe show streams command to pull out the audio duration.
     */
    private static final String ffprobeDurationIdentifier = "\"duration\"";

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
            File tmpDir = Dynamic.buildDynamic(Dynamic.TEMP.getFileName());
            if (!tmpDir.exists()) {
                tmpDir.mkdir();
            }

            String builtPath = Dynamic.buildDynamic(Dynamic.TEMP.getFileName(),
                    FileUtil.getFilename(mp3File) + Extension.WAV.getExtension()).getAbsolutePath();
            String safePath = quote + builtPath + quote;

            File outputFile = new File(builtPath);
            if (outputFile.exists()) {
                if (!OsUtil.deleteFile(outputFile)) {
                    throw new FatalException("Output file already exists in temp directory");
                }
            }

            ProcessBuilder processBuilder = new ProcessBuilder(getFfmpegCommand(), INPUT_FLAG,
                    quote + mp3File.getAbsolutePath() + quote, safePath);
            processBuilder.redirectErrorStream();
            Process process = processBuilder.start();

            // another precaution to ensure process is completed before file is returned
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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
            Process process = pb.start();

            // another precaution to ensure process is completed before file is returned
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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

            /*
            Audio length might change from ffmpeg high and low pass filters.
             */
            while (!outputFile.exists()) Thread.onSpinWait();
            process.waitFor();

            int exitValue = process.exitValue();
            if (exitValue != 0) {
                return Optional.empty();
            }

            return Optional.of(outputFile);
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

        return OsUtil.isBinaryInExes(Program.FFMPEG.getProgramName() + Extension.EXE.getExtension());
    }

    /**
     * Returns whether YouTube-dl is installed by attempting
     * validation on the set path to the exe and attempting
     * to invoke YouTube-dl in the console.
     *
     * @return whether YouTube-dl is installed
     */
    public static boolean youTubeDlInstalled() {
        if (Program.YOUTUBE_DL.isInstalled()) return true;

        return OsUtil.isBinaryInExes(Program.YOUTUBE_DL.getProgramName() + Extension.EXE.getExtension());
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
     * Returns the command to invoke YouTube-dl provided the
     * binary exists and can be found.
     *
     * @return the YouTube-dl command
     */
    public static String getYoutubeDlCommand() {
        Preconditions.checkArgument(youTubeDlInstalled());

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
                                    + Extension.ZIP.getExtension()), ffmpegResourceDownload),
                    new PairedFile(Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFPROBE.getProgramName()
                                    + Extension.ZIP.getExtension()), ffprobeResourceDownload),
                    new PairedFile(Dynamic.buildDynamic(
                            Dynamic.EXES.getFileName(), Program.FFPLAY.getProgramName()
                                    + Extension.ZIP.getExtension()), ffplayResourceDownload)
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
     * Downloads the YouTube-dl binary from the remote resources.
     * Returns whether the download was successful.
     *
     * @return whether YouTube-dl could be downloaded from the remote resources
     */
    public static Future<Boolean> downloadYoutubeDl() {
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(YOUTUBE_DL_DOWNLOADER_THREAD_NAME)).submit(() -> {
            File downloadZip = Dynamic.buildDynamic(
                    Dynamic.EXES.getFileName(), Program.YOUTUBE_DL.getProgramName()
                            + Extension.ZIP.getExtension());

            NetworkUtil.downloadResource(youtubeDlResourceDownload, downloadZip);
            while (!downloadZip.exists()) Thread.onSpinWait();

            File extractFolder = Dynamic.buildDynamic(Dynamic.EXES.getFileName());

            FileUtil.unzip(downloadZip, extractFolder);
            OsUtil.deleteFile(downloadZip);

            return Dynamic.buildDynamic(Dynamic.EXES.getFileName(),
                    Program.YOUTUBE_DL.getProgramName() + Extension.EXE.getExtension()).exists();
        });
    }

    /**
     * Returns the milliseconds of the provided audio file using FFprobe's -show_format command.
     * Note, this method is blocking. Callers should surround invocation of this method in a separate thread.
     *
     * @param audioFile the audio file
     * @return the milliseconds of the provided file
     * @throws ExecutionException   if the future task does not complete properly
     * @throws FatalException       if the stream fails to find the proper element from the ffprobe output
     * @throws InterruptedException if the thread was interrupted while waiting
     */
    public static int getMillisFfprobe(File audioFile) throws ExecutionException, InterruptedException {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        if (milliTimes.containsKey(audioFile)) return milliTimes.get(audioFile);

        ImmutableList<String> command = ImmutableList.of(
                getFfprobeCommand(),
                "-v",
                "quiet",
                "-print_format",
                "json",
                "-show_streams",
                CyderStrings.quote + audioFile.getAbsolutePath() + CyderStrings.quote
        );
        Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(command);
        while (!futureResult.isDone()) Thread.onSpinWait();
        String millisLine = futureResult.get().getStandardOutput().stream()
                .filter(line -> line.contains(ffprobeDurationIdentifier)).findFirst()
                .orElseThrow(() -> new FatalException("Failed to find " + ffprobeDurationIdentifier + " in results"));

        String parsedMillisLine = millisLine.replaceAll(CyderRegexPatterns.nonNumberAndPeriodRegex, "");
        double seconds = Double.parseDouble(parsedMillisLine);
        int millis = (int) (seconds * TimeUtil.millisInSecond);
        milliTimes.put(audioFile, millis);
        return millis;
    }

    /**
     * Returns the milliseconds of the provided audio file using JLayer.
     *
     * @param audioFile the MP3 file to return the milliseconds of
     * @return the milliseconds of the provided file
     * @throws IOException        if a FileInputStream cannot be made from the provided file
     * @throws BitstreamException if a BitStream cannot be made from the FileInputStream
     *                            or if an exception occurs when reading the header/frames
     */
    public static int getMillisJLayer(File audioFile) throws IOException, BitstreamException {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        if (milliTimes.containsKey(audioFile)) return milliTimes.get(audioFile);

        FileInputStream fis = new FileInputStream(audioFile);
        Bitstream bitstream = new Bitstream(fis);
        Header header = bitstream.readFrame();
        long channelSize = fis.getChannel().size();
        int retMillis = (int) header.total_ms((int) channelSize);
        milliTimes.put(audioFile, retMillis);
        return retMillis;
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
     * @param filename the name of the music file to search for
     * @return an optional reference to the requested music file
     */
    public static Optional<File> getCurrentUserMusicFileWithName(String filename) {
        Preconditions.checkNotNull(filename);
        Preconditions.checkArgument(!filename.isEmpty());

        File[] files = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(), UserFile.MUSIC.getName()).listFiles();

        if (files != null && files.length > 0) {
            return Arrays.stream(files)
                    .filter(file -> FileUtil.getFilename(file).equalsIgnoreCase(filename)).findFirst();
        }

        return Optional.empty();
    }

    /**
     * Returns the first found MP3 file from this Windows user's music directory if present. Empty optional else.
     *
     * @return the first found MP3 file from this Windows user's music directory if present. Empty optional else
     */
    public static Optional<File> getFirstMp3FileForWindowsUser() {
        Preconditions.checkState(OsUtil.isWindows());

        File windowsUserMusicDirectory = OsUtil.buildFile(OsUtil.WINDOWS_ROOT + USERS,
                OsUtil.getOsUsername(), MUSIC);
        if (windowsUserMusicDirectory.exists()) {
            return FileUtil.getFiles(windowsUserMusicDirectory, true)
                    .stream().filter(file -> FileUtil.validateExtension(file, Extension.MP3.getExtension()))
                    .findFirst();
        }

        return Optional.empty();
    }
}
