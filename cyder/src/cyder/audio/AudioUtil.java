package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.AudioLengthResponse;
import cyder.threads.CyderThreadFactory;
import cyder.user.UserFile;
import cyder.utils.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        Preconditions.checkArgument(FileUtil.validateExtension(mp3File, ".mp3"));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Mp3 to wav converter")).submit(() -> {
            String builtPath = new File(OSUtil.buildPath(
                    Dynamic.PATH,
                    "tmp", FileUtil.getFilename(mp3File) + ".wav")).getAbsolutePath();
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
        Preconditions.checkArgument(FileUtil.validateExtension(wavFile, ".wav"));

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Wav to mp3 converter")).submit(() -> {

            String builtPath = new File(OSUtil.buildPath(
                    Dynamic.PATH,
                    "tmp", FileUtil.getFilename(wavFile) + ".mp3")).getAbsolutePath();
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
    public static final int highpass = 2;

    /**
     * The lowpass value for dreamifying an audio file.
     */
    public static final int lowpass = 300;

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

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Audio Dreamifier: "
                        + FileUtil.getFilename(wavOrMp3File))).submit(() -> {

            // in case the audio wav name contains spaces, surround with quotes
            String safeFilename = "\"" + wavOrMp3File.getAbsolutePath() + "\"";

            File outputFile = OSUtil.buildFile(Dynamic.PATH,
                    "tmp", FileUtil.getFilename(wavOrMp3File) + DREAMY_SUFFIX + ".mp3");

            ProcessBuilder pb = new ProcessBuilder(
                    getFfmpegCommand(),
                    INPUT_FLAG,
                    safeFilename,
                    "-filter:a",
                    "\"highpass=f=" + highpass + ", lowpass=f=" + lowpass + "\"",
                    "\"" + outputFile.getAbsolutePath() + "\"");
            pb.start();

            // get original time of wav (after process started to save time)
            Future<Integer> startingMillis = getMillis(wavOrMp3File);
            while (!startingMillis.isDone()) {
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

                if (updatedLen.get().equals(startingMillis.get())) {
                    break;
                }

                Thread.sleep(500);
            }

            // return dreamified
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
                || OSUtil.isBinaryInExes("ffprobe.exe");
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
                : OSUtil.buildPath(Dynamic.PATH,
                Dynamic.EXES.getDirectoryName(), FFMPEG + ".exe");
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
                : OSUtil.buildPath(Dynamic.PATH,
                Dynamic.EXES.getDirectoryName(), YOUTUBE_DL + ".exe");
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
                    Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(),
                    "ffprobe.exe");
        }
    }

    /**
     * Downloads ffmpeg, ffplay, and ffprobe to the exes dynamic
     * directory and sets the user path for ffmpeg to the one in dynamic.
     *
     * @return whether the download was successful
     */
    public static Future<Boolean> downloadFfmpegStack() {
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Ffmpeg Downloader")).submit(() -> {
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
            downloadZips.add(new PairedFile(OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(),
                    FFMPEG + ".zip"), CyderUrls.DOWNLOAD_RESOURCE_FFMPEG));
            downloadZips.add(new PairedFile(OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(),
                    FFPROBE + ".zip"), CyderUrls.DOWNLOAD_RESOURCE_FFPROBE));
            downloadZips.add(new PairedFile(OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(),
                    FFPLAY + ".zip"), CyderUrls.DOWNLOAD_RESOURCE_FFPLAY));

            for (PairedFile pairedZipFile : downloadZips) {
                NetworkUtil.downloadResource(pairedZipFile.url, pairedZipFile.file);

                while (!pairedZipFile.file.exists()) {
                    Thread.onSpinWait();
                }

                File extractFolder = OSUtil.buildFile(
                        Dynamic.PATH,
                        Dynamic.EXES.getDirectoryName());

                FileUtil.unzip(pairedZipFile.file, extractFolder);
                OSUtil.deleteFile(pairedZipFile.file);
            }

            ArrayList<File> resultingFiles = new ArrayList<>();
            resultingFiles.add(OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), FFMPEG + ".exe"));
            resultingFiles.add(OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), FFPROBE + ".exe"));
            resultingFiles.add(OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), FFPLAY + ".exe"));

            boolean ret = true;

            for (File file : resultingFiles) {
                ret = ret && file.exists();
            }

            return ret;
        });
    }

    /**
     * Downloads the youtube-dl binary from the remote resources.
     * Returns whether the download was successful.
     *
     * @return whether youtube-dl could be downloaded from the remote resources.
     */
    public static Future<Boolean> downloadYoutubeDl() {
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("YouTubeDl Downloader")).submit(() -> {
            File downloadZip = OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(),
                    YOUTUBE_DL + ".zip");

            NetworkUtil.downloadResource(CyderUrls.DOWNLOAD_RESOURCE_YOUTUBE_DL, downloadZip);

            while (!downloadZip.exists()) {
                Thread.onSpinWait();
            }

            File extractFolder = OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName());

            FileUtil.unzip(downloadZip, extractFolder);
            OSUtil.deleteFile(downloadZip);

            return OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.EXES.getDirectoryName(), YOUTUBE_DL + ".exe").exists();
        });
    }

    /**
     * Returns a pretty representation of the provided number of seconds
     * using hours, minutes, and seconds.
     * <p>
     * Example: 3661 would return "1h 1m 1s".
     *
     * @param seconds the amount of seconds
     * @return the pretty representation
     */
    public static String formatSeconds(int seconds) {
        StringBuilder sb = new StringBuilder();

        int minutes;
        int hours;

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

    /**
     * The location to post for an audio location post.
     */
    private static final String AUDIO_LENGTH_PATH = BackendUtil.constructPath("audio", "length");

    /**
     * The encoding used for a post.
     */
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * The gson object used to serialize audio length posts.
     */
    private static final Gson gson = new Gson();

    /**
     * Returns the number of milliseconds in an audio file.
     *
     * @param audioFile the audio file to return the duration of
     * @return the duration of the provided audio file in milliseconds
     */
    public static int getMillisFast(File audioFile) {
        try {
            URL url = new URL(AUDIO_LENGTH_PATH);
            String path = audioFile.getAbsolutePath().replace("\\", "\\\\");
            String data = "{\"audio_path\":\"" + path + "\"}";

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = data.getBytes();
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    con.getInputStream(), ENCODING))) {
                StringBuilder response = new StringBuilder();
                String responseLine;

                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                AudioLengthResponse audioLengthResponse = gson.fromJson(response.toString(), AudioLengthResponse.class);

                return Math.round(audioLengthResponse.getLength() * 1000);
            }

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return 0;
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
            FileInputStream fis = new FileInputStream(file);
            return fis.available();
        } catch (Exception ignored) {}

        return 0L;
    }

    /**
     * Returns a reference to the current user's music file with the provided name if found, empty optional else.
     *
     * @param title the title of the music file to search for
     * @return an optional reference to the requested music file
     */
    public static Optional<File> getMusicFileWithName(String title) {
        File[] files = OSUtil.buildFile(
                Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName()).listFiles();

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
