package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.process.Program;
import cyder.threads.CyderThreadFactory;
import cyder.threads.ThreadUtil;
import cyder.user.UserFile;
import cyder.utils.FileUtil;
import cyder.utils.OsUtil;
import cyder.utils.StaticUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

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
     * The primary name for the ffprobe binary.
     */
    public static final String FFPROBE = "ffprobe";

    /**
     * The primary name for the ffplay binary.
     */
    public static final String FFPLAY = "ffplay";

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
            String builtPath = new File(OsUtil.buildPath(
                    Dynamic.PATH,
                    "tmp", FileUtil.getFilename(mp3File) + Extension.WAV.getExtension())).getAbsolutePath();
            String safePath = "\"" + builtPath + "\"";

            File outputFile = new File(builtPath);
            ProcessBuilder pb = new ProcessBuilder(getFfmpegCommand(), INPUT_FLAG,
                    "\"" + mp3File.getAbsolutePath() + "\"", safePath);
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
    @SuppressWarnings("ResultOfMethodCallIgnored") public static Future<Optional<File>> wavToMp3(File wavFile) {
        Preconditions.checkNotNull(wavFile);
        Preconditions.checkArgument(FileUtil.validateExtension(wavFile, Extension.WAV.getExtension()));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Wav to mp3 converter")).submit(() -> {

            String builtPath = new File(OsUtil.buildPath(
                    Dynamic.PATH,
                    "tmp", FileUtil.getFilename(wavFile) + Extension.MP3.getExtension())).getAbsolutePath();
            String safePath = "\"" + builtPath + "\"";

            File outputFile = new File(builtPath);
            ProcessBuilder pb = new ProcessBuilder(getFfmpegCommand(), INPUT_FLAG,
                    "\"" + wavFile.getAbsolutePath() + "\"", safePath);
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
     * The dreamified file suffix to append to music files after dreamifying them.
     */
    public static final String DREAMY_SUFFIX = "_Dreamy";

    /**
     * The highpass value for dreamifying an audio file.
     */
    public static final int HIGHPASS = 2;

    /**
     * The lowpass value for dreamifying an audio file.
     */
    public static final int LOWPASS = 300;

    /**
     * The audio dreamifier thread name prefix.
     */
    private static final String AUDIO_DREAMIFIER = "Audio Dreamifier: ";

    /**
     * An escaped quote character.
     */
    private static final String ESCAPED_QUOTE = "\"";

    /**
     * The -filter:a flag for setting high and low pass data.
     */
    private static final String FILTER_DASH_A = "-filter:a";

    /**
     * The high and low pass argument string.
     */
    private static final String HIGHPASS_LOWPASS_ARGS = "\"highpass=f=" + HIGHPASS + ", lowpass=f=" + LOWPASS + "\"";

    /**
     * The delay between polling milliseconds when dreamifying an audio.
     */
    private static final int pollMillisDelay = 500;

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

        String executorThreadName = AUDIO_DREAMIFIER + FileUtil.getFilename(wavOrMp3File);

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(executorThreadName)).submit(() -> {

            // in case the audio wav name contains spaces, surround with quotes
            String safeFilename = ESCAPED_QUOTE + wavOrMp3File.getAbsolutePath() + ESCAPED_QUOTE;

            File outputFile = OsUtil.buildFile(Dynamic.PATH, Dynamic.TEMP.getDirectoryName(),
                    FileUtil.getFilename(wavOrMp3File) + DREAMY_SUFFIX + Extension.MP3.getExtension());
            String safeOutputFilename = ESCAPED_QUOTE + outputFile.getAbsolutePath() + ESCAPED_QUOTE;

            String[] command = {
                    getFfmpegCommand(),
                    INPUT_FLAG,
                    safeFilename,
                    FILTER_DASH_A,
                    HIGHPASS_LOWPASS_ARGS,
                    safeOutputFilename};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            Future<Integer> originalFileMillis = getMillis(wavOrMp3File);
            while (!originalFileMillis.isDone()) {
                Thread.onSpinWait();
            }

            while (!outputFile.exists()) {
                Thread.onSpinWait();
            }

            while (true) {
                Future<Integer> updatedLen = getMillis(outputFile);

                while (!updatedLen.isDone()) {
                    Thread.onSpinWait();
                }

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
     * The pattern used to find the duration of an audio file from ffprobe.
     */
    private static final Pattern durationPattern = Pattern.compile("\\s*duration=.*\\s*");

    /**
     * Uses ffprobe to get the length of the audio file in milliseconds.
     *
     * @param audioFile the audio file to find the length of in milliseconds
     * @return the length of the audio file in milliseconds
     */
    private static Future<Integer> getMillis(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        Preconditions.checkArgument(FileUtil.validateExtension(audioFile, Extension.WAV.getExtension())
                || FileUtil.validateExtension(audioFile, Extension.MP3.getExtension()));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Audio Length Finder: "
                        + FileUtil.getFilename(audioFile))).submit(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(getFfprobeCommand(), INPUT_FLAG,
                        "\"" + audioFile.getAbsolutePath() + "\"", "-show_format");
                Process p = pb.start();

                // another precaution to ensure process is completed before file is returned
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (durationPattern.matcher(line).matches()) {
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
        if (OsUtil.isBinaryInstalled(FFMPEG)) {
            return true;
        }

        // finally check dynamic/exes to see if an ffmpeg binary exists there
        return OsUtil.isBinaryInExes(FFMPEG + Extension.EXE.getExtension());
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
        if (OsUtil.isBinaryInstalled(YOUTUBE_DL)) {
            return true;
        }

        // finally check dynamic/exes to see if a youtube-dl binary exists there
        return OsUtil.isBinaryInExes(YOUTUBE_DL + Extension.EXE.getExtension());
    }

    /**
     * Returns whether ffprobe is installed.
     *
     * @return whether ffprobe is installed
     */
    public static boolean ffprobeInstalled() {
        return OsUtil.isBinaryInstalled(FFPROBE) || OsUtil.isBinaryInExes(FFPROBE + Extension.EXE.getExtension());
    }

    /**
     * Returns the command to invoke ffmpeg provided the
     * binary exists and can be found.
     *
     * @return the ffmpeg command
     */
    public static String getFfmpegCommand() {
        Preconditions.checkArgument(ffmpegInstalled());

        return OsUtil.isBinaryInstalled(FFMPEG) ? FFMPEG : OsUtil.buildPath(Dynamic.PATH,
                Dynamic.EXES.getDirectoryName(), FFMPEG + Extension.EXE.getExtension());
    }

    /**
     * Returns the command to invoke youtube-dl provided the
     * binary exists and can be found.
     *
     * @return the youtube-dl command
     */
    public static String getYoutubeDlCommand() {
        Preconditions.checkArgument(youtubeDlInstalled());

        return OsUtil.isBinaryInstalled(YOUTUBE_DL)
                ? YOUTUBE_DL : OsUtil.buildPath(Dynamic.PATH,
                Dynamic.EXES.getDirectoryName(), YOUTUBE_DL + Extension.EXE.getExtension());
    }

    /**
     * Returns the base ffprobe command.
     *
     * @return the base ffprobe command
     */
    public static String getFfprobeCommand() {
        Preconditions.checkArgument(ffprobeInstalled());

        if (OsUtil.isBinaryInstalled(FFPROBE)) {
            return FFPROBE;
        } else {
            return OsUtil.buildPath(Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), FFPROBE + Extension.EXE.getExtension());
        }
    }

    private static final String FFMPEG_DOWNLOADER_THREAD_NAME = "FFMPEG Downloader";

    /**
     * Downloads ffmpeg, ffplay, and ffprobe to the exes dynamic
     * directory and sets the user path for ffmpeg to the one in dynamic.
     *
     * @return whether the download was successful
     */
    public static Future<Boolean> downloadFfmpegStack() {
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(FFMPEG_DOWNLOADER_THREAD_NAME)).submit(() -> {
            // an anonymous inner class to quickly link a file with a url
            class PairedFile {
                public final File file;
                public final String url;

                public PairedFile(File file, String url) {
                    this.file = file;
                    this.url = url;
                }
            }

            ArrayList<PairedFile> downloadZips = new ArrayList<>();
            downloadZips.add(new PairedFile(OsUtil.buildFile(Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), FFMPEG + Extension.ZIP.getExtension()), DOWNLOAD_RESOURCE_FFMPEG));
            downloadZips.add(new PairedFile(OsUtil.buildFile(Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), FFPROBE + Extension.ZIP.getExtension()),
                    DOWNLOAD_RESOURCE_FFPROBE));
            downloadZips.add(new PairedFile(OsUtil.buildFile(Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), FFPLAY + Extension.ZIP.getExtension()), DOWNLOAD_RESOURCE_FFPLAY));

            for (PairedFile pairedZipFile : downloadZips) {
                NetworkUtil.downloadResource(pairedZipFile.url, pairedZipFile.file);

                while (!pairedZipFile.file.exists()) {
                    Thread.onSpinWait();
                }

                File extractFolder = OsUtil.buildFile(
                        Dynamic.PATH,
                        Dynamic.EXES.getDirectoryName());

                FileUtil.unzip(pairedZipFile.file, extractFolder);
                OsUtil.deleteFile(pairedZipFile.file);
            }

            ArrayList<File> resultingFiles = new ArrayList<>();
            resultingFiles.add(OsUtil.buildFile(Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), FFMPEG + Extension.EXE.getExtension()));
            resultingFiles.add(OsUtil.buildFile(Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), FFPROBE + Extension.EXE.getExtension()));
            resultingFiles.add(OsUtil.buildFile(Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), FFPLAY + Extension.EXE.getExtension()));

            boolean ret = true;

            for (File file : resultingFiles) {
                ret = ret && file.exists();
            }

            return ret;
        });
    }

    /**
     * The name of the thread that downloads youtube-dl if missing and needed.
     */
    private static final String YOUTUBE_DL_DOWNLOADER_THREAD_NAME = "YouTubeDl Downloader";

    /**
     * Downloads the youtube-dl binary from the remote resources.
     * Returns whether the download was successful.
     *
     * @return whether youtube-dl could be downloaded from the remote resources
     */
    public static Future<Boolean> downloadYoutubeDl() {
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(YOUTUBE_DL_DOWNLOADER_THREAD_NAME)).submit(() -> {
            File downloadZip = OsUtil.buildFile(Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), YOUTUBE_DL + Extension.ZIP.getExtension());

            NetworkUtil.downloadResource(DOWNLOAD_RESOURCE_YOUTUBE_DL, downloadZip);

            while (!downloadZip.exists()) {
                Thread.onSpinWait();
            }

            File extractFolder = OsUtil.buildFile(Dynamic.PATH, Dynamic.EXES.getDirectoryName());

            FileUtil.unzip(downloadZip, extractFolder);
            OsUtil.deleteFile(downloadZip);

            return OsUtil.buildFile(Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), YOUTUBE_DL + Extension.EXE.getExtension()).exists();
        });
    }

    // todo AudioUtil could be cleaner

    // todo move these types of things to CyderStrings like spaces, opening brackets and parenthesis, etc.
    //  then use static import for files, ctrl f for instances of " " as a string
    //  quote, space, colon, brackets, parenthesis

    // todo log in TESTING MODE should be replaced with launched in dev debug vs ide debug... ?

    // todo rare case of is closed for console being false but getdominantframe failing to get the frame

    /**
     * A space character.
     */
    private static final String SPACE = " ";

    /**
     * The command for querying an audio's length from the python functions script.
     */
    private static final String AUDIO_LENGTH = "audio_length";

    /**
     * A quote character.
     */
    private static final String QUOTE = "\"";

    /**
     * The name of the python functions script.
     */
    private static final String PYTHON_FUNCTIONS_SCRIPT_NAME = "python_functions.py";

    /**
     * The prefix output by the python functions audio length function.
     */
    private static final String audioLengthProcessReturnPrefix = "Audio length: ";

    /**
     * Returns the number of milliseconds in an audio file.
     * Note this method takes an average of 200ms to complete and return.
     *
     * @param audioFile the audio file to return the duration of
     * @return the duration of the provided audio file in milliseconds
     */
    public static Future<Integer> getMillisFast(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PYTHON.getProgramName()));

        String threadName = "getMillisFast thread, audioFile = " + QUOTE + audioFile + QUOTE;
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(threadName)).submit(() -> {
            String functionsScriptPath = StaticUtil.getStaticPath(PYTHON_FUNCTIONS_SCRIPT_NAME);
            String command = Program.PYTHON.getProgramName() + SPACE + functionsScriptPath
                    + SPACE + AUDIO_LENGTH + SPACE + QUOTE + audioFile.getAbsolutePath() + QUOTE;

            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(command);
            while (!futureResult.isDone()) {
                Thread.onSpinWait();
            }

            ProcessResult result = null;
            try {
                result = futureResult.get();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            int ret = 0;

            if (result != null && result.getErrorOutput().isEmpty()) {
                ImmutableList<String> standardOutput = result.getStandardOutput();
                if (!standardOutput.isEmpty()) {
                    String firstResult = standardOutput.get(0);

                    if (firstResult.startsWith(audioLengthProcessReturnPrefix)) {
                        firstResult = firstResult.replace(audioLengthProcessReturnPrefix, "");
                        ret = (int) (Float.parseFloat(firstResult) * 1000.0f);
                    }
                }
            }

            return ret;
        });
    }

    /**
     * Returns the total bytes of the file.
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
    public static Optional<File> getMusicFileWithName(String title) {
        Preconditions.checkNotNull(title);
        Preconditions.checkArgument(!title.isEmpty());

        File[] files = OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
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
