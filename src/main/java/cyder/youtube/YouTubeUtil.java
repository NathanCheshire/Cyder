package cyder.youtube;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.audio.AudioUtil;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderUrls;
import cyder.enumerations.Dynamic;
import cyder.enumerations.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.exceptions.YoutubeException;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.LevenshteinUtil;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.ui.button.CyderButton;
import cyder.user.UserFile;
import cyder.utils.ArrayUtil;
import cyder.utils.ImageUtil;
import cyder.utils.SecurityUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.stream.IntStream;

import static cyder.strings.CyderStrings.forwardSlash;
import static cyder.youtube.YouTubeConstants.*;

/**
 * Utility methods related to YouTube videos.
 */
public final class YouTubeUtil {
    /**
     * Suppress default constructor.
     */
    private YouTubeUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Downloads the YouTube video with the provided url.
     *
     * @param url              the url of the video to download
     * @param baseInputHandler the handler to use to print updates about the download to
     */
    public static void downloadYouTubeAudio(String url, BaseInputHandler baseInputHandler) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        Preconditions.checkNotNull(baseInputHandler);

        if (AudioUtil.ffmpegInstalled() && AudioUtil.youTubeDlInstalled()) {
            YouTubeAudioDownload youTubeDownload = new YouTubeAudioDownload();
            youTubeDownload.setVideoLink(url);
            youTubeDownload.setPrintOutputHandler(baseInputHandler);
            youTubeDownload.downloadAudioAndThumbnail();
        } else {
            onNoFfmpegOrYoutubeDlInstalled();
        }
    }

    /**
     * Returns the name to save the YouTube video's audio/thumbnail as.
     *
     * @param youTubeVideoUrl the url
     * @return the name to save the file as
     */
    public static String getDownloadSaveName(String youTubeVideoUrl) {
        Preconditions.checkNotNull(youTubeVideoUrl);
        Preconditions.checkArgument(!youTubeVideoUrl.isEmpty());

        String urlTitle = NetworkUtil.getUrlTitle(youTubeVideoUrl).orElse(UNKNOWN_TITLE);

        String safeName = urlTitle.replace(YOUTUBE_VIDEO_URL_TITLE_SUFFIX, "")
                .replaceAll(CyderRegexPatterns.windowsInvalidFilenameChars.pattern(), "").trim();

        while (safeName.endsWith(".")) {
            safeName = StringUtil.removeLastChar(safeName);
        }

        if (safeName.isEmpty()) {
            safeName = SecurityUtil.generateUuid();
        }

        return safeName;
    }

    /**
     * Retrieves the most likely valid video UUID for the provided YouTube query if present. Empty optional else.
     *
     * @param youTubeQuery the raw query as if the input was entered directly into the YouTube search bar
     * @return the most likely UUID for the search query if present. Empty optional else
     */
    public static Future<String> getMostLikelyUuid(String youTubeQuery) {
        Preconditions.checkNotNull(youTubeQuery);
        Preconditions.checkArgument(!youTubeQuery.isEmpty());

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("MostLikelyUuid finder, query=" + youTubeQuery)).submit(() -> {
            String query = YOUTUBE_QUERY_BASE + youTubeQuery.replaceAll(CyderRegexPatterns.whiteSpaceRegex, querySpace);
            String jsonString = NetworkUtil.readUrl(query);

            LinkedHashMap<Integer, String> levenshteinDistanceToUuids = new LinkedHashMap<>();

            if (jsonString.contains(videoIdHtmlSubstring)) {
                ArrayList<String> uuids = new ArrayList<>();

                String[] parts = jsonString.split(videoIdHtmlSubstring);
                IntStream.range(0, Math.min(parts.length, Props.maxYouTubeUuidChecksPlayCommand.getValue()))
                        .forEach(index -> {
                            String part = parts[index];

                            if (part.length() >= UUID_LENGTH) {
                                String uuid = part.substring(0, UUID_LENGTH);
                                if (UUID_PATTERN.matcher(uuid).matches() && !uuids.contains(uuid)) {
                                    uuids.add(uuid);
                                }
                            }
                        });

                uuids.forEach(uuid -> NetworkUtil.getUrlTitle(YouTubeUtil.buildVideoUrl(uuid))
                        .ifPresent(title -> levenshteinDistanceToUuids.put(
                                LevenshteinUtil.computeLevenshteinDistance(title, youTubeQuery), uuid)));
            }

            return levenshteinDistanceToUuids.get(levenshteinDistanceToUuids.keySet().stream().mapToInt(i -> i).min()
                    .orElseThrow(() -> new FatalException("Could not find YouTube uuid for query: " + youTubeQuery)));
        });
    }

    /**
     * Outputs instructions to the console due to YouTube-dl or ffmpeg not being installed.
     */
    private static void onNoFfmpegOrYoutubeDlInstalled() {
        Console.INSTANCE.getInputHandler().println("Sorry, but ffmpeg and/or YouTube-dl "
                + "couldn't be located. Please make sure they are both installed and added to your PATH Windows "
                + "variable. Remember to also set the path to your YouTube-dl executable in the user editor");

        CyderButton environmentVariableHelp = new CyderButton("Add Environment Variables");
        environmentVariableHelp.addActionListener(e -> NetworkUtil.openUrl(environmentVariables));
        Console.INSTANCE.getInputHandler().println(environmentVariableHelp);

        CyderButton downloadFFMPEG = new CyderButton("Download FFMPEG");
        downloadFFMPEG.addActionListener(e -> NetworkUtil.openUrl(CyderUrls.FFMPEG_INSTALLATION));
        Console.INSTANCE.getInputHandler().println(downloadFFMPEG);

        CyderButton downloadYoutubeDL = new CyderButton("Download YouTube-dl");
        downloadYoutubeDL.addActionListener(e -> NetworkUtil.openUrl(CyderUrls.YOUTUBE_DL_INSTALLATION));
        Console.INSTANCE.getInputHandler().println(downloadYoutubeDL);
    }

    /**
     * Attempts to set the console background to the provided YouTube video's thumbnail
     *
     * @param url the url of the YouTube video
     */
    public static void setAsConsoleBackground(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(NetworkUtil.isValidUrl(url));

        Dimension consoleDimension = Console.INSTANCE.getConsoleCyderFrame().getSize();

        Optional<BufferedImage> maxThumbnailOptional = getMaxResolutionThumbnail(extractUuid(url));

        if (maxThumbnailOptional.isEmpty()) {
            throw new YoutubeException("Could not get max resolution thumbnail");
        }

        BufferedImage maxThumbnail = maxThumbnailOptional.get();

        int newConsoleWidth = (int) consoleDimension.getWidth();
        int newConsoleHeight = (int) consoleDimension.getHeight();

        // if console is bigger than a dimension of the thumbnail, use thumbnail dimensions
        if (consoleDimension.getWidth() > maxThumbnail.getWidth()
                || consoleDimension.getHeight() > maxThumbnail.getHeight()) {
            newConsoleWidth = maxThumbnail.getWidth();
            newConsoleHeight = maxThumbnail.getHeight();
        }

        maxThumbnail = ImageUtil.resizeImage(maxThumbnail, maxThumbnail.getType(), newConsoleWidth, newConsoleHeight);

        String saveNameAndExtension = getDownloadSaveName(url) + Extension.PNG.getExtension();

        File fullSaveFile = Dynamic.buildDynamic(
                Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(),
                UserFile.BACKGROUNDS.getName(),
                saveNameAndExtension);

        try {
            ImageIO.write(maxThumbnail, Extension.PNG.getExtensionWithoutPeriod(), fullSaveFile);
            Console.INSTANCE.setBackgroundFile(fullSaveFile);
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns whether the provided url is a playlist url.
     *
     * @param url the url
     * @return whether the provided url references a YouTube playlist
     */
    public static boolean isPlaylistUrl(String url) {
        Preconditions.checkNotNull(url);

        return url.startsWith(YOUTUBE_PLAYLIST_HEADER);
    }

    /**
     * Returns whether the provided url is a video rul.
     *
     * @param url the url
     * @return whether the provided url is a video url
     */
    public static boolean isVideoUrl(String url) {
        return Preconditions.checkNotNull(url).startsWith(YOUTUBE_VIDEO_BASE);
    }

    /**
     * Extracts the YouTube playlist id from the provided playlist url.
     *
     * @param playlistUrl the url of the playlist
     * @return the YouTube playlist id
     */
    public static String extractPlaylistId(String playlistUrl) {
        Preconditions.checkNotNull(playlistUrl);
        Preconditions.checkArgument(isPlaylistUrl(playlistUrl));

        return playlistUrl.replace(YOUTUBE_PLAYLIST_HEADER, "").trim();
    }

    /**
     * Returns a url for the YouTube video with the provided uuid.
     *
     * @param uuid the uuid of the video
     * @return a url for the YouTube video with the provided uuid
     * @throws IllegalArgumentException if the provided uuid is not 11 chars long
     */
    public static String buildVideoUrl(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(YouTubeConstants.UUID_PATTERN.matcher(uuid).matches());

        return CyderUrls.YOUTUBE_VIDEO_HEADER + uuid;
    }

    /**
     * Returns a URL for the maximum resolution version of the YouTube video's thumbnail.
     *
     * @param uuid the uuid of the video
     * @return a URL for the maximum resolution version of the YouTube video's thumbnail
     */
    public static String buildMaxResolutionThumbnailUrl(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(YouTubeConstants.UUID_PATTERN.matcher(uuid).matches());

        return YOUTUBE_THUMBNAIL_BASE + uuid + forwardSlash + MAX_RES_DEFAULT;
    }

    /**
     * Returns a url for the default thumbnail of a YouTube video.
     *
     * @param uuid the uuid of the video
     * @return a url for the default YouTube video's thumbnail
     */
    public static String buildStandardDefinitionThumbnailUrl(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(YouTubeConstants.UUID_PATTERN.matcher(uuid).matches());

        return YOUTUBE_THUMBNAIL_BASE + uuid + CyderStrings.forwardSlash + SD_DEFAULT;
    }

    /**
     * Extracts the uuid for the YouTube video from the url
     *
     * @param url the YouTube url to extract the uuid from
     * @return the extracted uuid
     */
    public static String extractUuid(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        Matcher matcher = CyderRegexPatterns.extractYoutubeUuidPattern.matcher(url);

        if (matcher.find()) {
            return matcher.group();
        }

        throw new IllegalArgumentException("No uuid found from provided url: " + url);
    }

    /**
     * Constructs the url to query YouTube with a specific string for video results.
     *
     * @param numResults the number of results to return (max 20 results per page)
     * @param query      the search query such as "black parade"
     * @return the constructed url to match the provided parameters
     */
    public static String buildYouTubeApiV3SearchQuery(int numResults, String query) {
        Preconditions.checkArgument(SEARCH_QUERY_RESULTS_RANGE.contains(numResults));
        Preconditions.checkNotNull(query);
        Preconditions.checkArgument(!query.isEmpty());
        Preconditions.checkArgument(Props.youTubeApi3key.valuePresent());

        String key = Props.youTubeApi3key.getValue();
        ImmutableList<String> queryWords = ArrayUtil.toList(query.split(CyderRegexPatterns.whiteSpaceRegex));

        ArrayList<String> legalCharsQueryWords = new ArrayList<>();
        queryWords.forEach(part -> legalCharsQueryWords.add(
                part.replaceAll(CyderRegexPatterns.illegalUrlCharsRegex, "")));

        String builtQuery = StringUtil.joinParts(legalCharsQueryWords, NetworkUtil.URL_SPACE);

        return YOUTUBE_API_V3_SEARCH_BASE
                + YouTubeConstants.MAX_RESULTS_PARAMETER + numResults
                + queryParameter + builtQuery
                + videoTypeParameter + video
                + keyParameter + key;
    }

    /**
     * Returns the maximum resolution thumbnail for the YouTube video with the provided uuid.
     *
     * @param uuid the uuid of the video
     * @return the maximum resolution thumbnail for the YouTube video
     */
    public static Optional<BufferedImage> getMaxResolutionThumbnail(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(YouTubeConstants.UUID_PATTERN.matcher(uuid).matches());

        String thumbnailUrl = buildMaxResolutionThumbnailUrl(uuid);

        try {
            return Optional.of(ImageUtil.read(thumbnailUrl));
        } catch (Exception ignored) {
            try {
                thumbnailUrl = buildStandardDefinitionThumbnailUrl(uuid);
                return Optional.of(ImageUtil.read(thumbnailUrl));
            } catch (Exception ignored2) {
                return Optional.empty();
            }
        }
    }

    /**
     * Returns a list of video UUIDs contained in the YouTube playlist provided.
     *
     * @param playlistUrl the url of the YouTube playlist
     * @return the list of video UUIDs the playlist contains
     */
    public static ImmutableList<String> getPlaylistVideoUuids(String playlistUrl) {
        Preconditions.checkNotNull(playlistUrl);
        Preconditions.checkArgument(!playlistUrl.isEmpty());
        Preconditions.checkArgument(isPlaylistUrl(playlistUrl));

        String htmlContents = NetworkUtil.readUrl(playlistUrl);
        String[] parts = htmlContents.split(videoIdHtmlSubstring);

        ArrayList<String> uniqueVideoUuids = new ArrayList<>();

        for (String part : parts) {
            if (part.length() >= UUID_LENGTH) {
                String uuid = part.substring(0, UUID_LENGTH);

                // Just to be safe
                if (!UUID_PATTERN.matcher(uuid).matches()) continue;

                if (!uniqueVideoUuids.contains(uuid)) {
                    uniqueVideoUuids.add(uuid);
                }
            }
        }

        return ImmutableList.copyOf(uniqueVideoUuids);
    }

    /**
     * Returns the maximum resolution square thumbnail possible for the YouTube video with the provided uuid.
     *
     * @param uuid the uuid of the video
     * @return the maximum resolution square thumbnail
     */
    public static Optional<BufferedImage> getMaxResolutionSquareThumbnail(String uuid) {
        Optional<BufferedImage> optionalBi = YouTubeUtil.getMaxResolutionThumbnail(uuid);

        BufferedImage bi = optionalBi.orElse(null);

        if (bi == null) {
            return Optional.empty();
        }

        return Optional.of(ImageUtil.cropToMaximumSizeSquare(bi));
    }
}
