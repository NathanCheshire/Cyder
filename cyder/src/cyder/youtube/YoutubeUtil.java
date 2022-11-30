package cyder.youtube;

import com.google.common.base.Preconditions;
import cyder.audio.AudioUtil;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.exceptions.YoutubeException;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.props.Props;
import cyder.ui.button.CyderButton;
import cyder.user.UserFile;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;
import cyder.utils.StringUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.regex.Matcher;

import static cyder.constants.CyderStrings.*;
import static cyder.youtube.YoutubeConstants.*;

/** Utility methods related to YouTube videos. */
public final class YoutubeUtil {
    /** Suppress default constructor. */
    private YoutubeUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /** A list of YouTube videos currently being downloaded. */
    private static final LinkedList<YoutubeDownload> activeDownloads = new LinkedList<>();

    /**
     * Downloads the YouTube video with the provided url.
     *
     * @param url the url of the video to download
     */
    public static void downloadYouTubeAudio(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        if (AudioUtil.ffmpegInstalled() && AudioUtil.youtubeDlInstalled()) {
            YoutubeDownload youtubeDownload = new YoutubeDownload(url, DownloadType.AUDIO);
            activeDownloads.add(youtubeDownload);
            youtubeDownload.download();
        } else {
            noFfmpegOrYoutubeDl();
        }
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

        if (AudioUtil.ffmpegInstalled() && AudioUtil.youtubeDlInstalled()) {
            YoutubeDownload youtubeDownload = new YoutubeDownload(url, DownloadType.AUDIO);
            youtubeDownload.setInputHandler(baseInputHandler);
            youtubeDownload.download();
        } else {
            noFfmpegOrYoutubeDl();
        }
    }

    /**
     * Removes the provided YouTube download from the active downloads list.
     *
     * @param youtubeDownload the YouTube download to remove from the active downloads list
     */
    static void removeActiveDownload(YoutubeDownload youtubeDownload) {
        Preconditions.checkNotNull(youtubeDownload);

        activeDownloads.remove(youtubeDownload);
    }

    /**
     * Adds the provided YouTube download to the downloads list.
     *
     * @param youtubeDownload the youtube download to add to the list
     */
    static void addActiveDownload(YoutubeDownload youtubeDownload) {
        Preconditions.checkNotNull(youtubeDownload);

        activeDownloads.add(youtubeDownload);
    }

    /** Cancels all active youtube downloads. */
    public static void cancelAllActiveDownloads() {
        activeDownloads.forEach(YoutubeDownload::cancel);
    }

    /**
     * Downloads the YouTube playlist provided the playlist exists.
     *
     * @param playlist the url of the playlist to download
     */
    public static void downloadPlaylist(String playlist) {
        Preconditions.checkNotNull(playlist);
        Preconditions.checkArgument(!playlist.isEmpty());

        downloadPlaylist(playlist, null);
    }

    /**
     * Downloads the YouTube playlist provided the playlist exists.
     *
     * @param playlist         the url of the playlist to download
     * @param baseInputHandler the input handler to print updates to
     */
    public static void downloadPlaylist(String playlist, BaseInputHandler baseInputHandler) {
        Preconditions.checkNotNull(playlist);
        Preconditions.checkArgument(!playlist.isEmpty());

        if (AudioUtil.ffmpegInstalled() && AudioUtil.youtubeDlInstalled()) {
            String playlistID = extractPlaylistId(playlist);

            if (StringUtil.isNullOrEmpty(Props.youtubeApi3key.getValue()) && baseInputHandler != null) {
                baseInputHandler.println(KEY_NOT_SET_ERROR_MESSAGE);
            } else {
                try {
                    String link = YOUTUBE_API_V3_PLAYLIST_ITEMS
                            + "part=snippet%2C+id&playlistId=" + playlistID
                            + "&key=" + Props.youtubeApi3key.getValue();

                    String jsonResponse = NetworkUtil.readUrl(link);

                    Matcher m = CyderRegexPatterns.youtubeApiV3UuidPattern.matcher(jsonResponse);
                    ArrayList<String> uuids = new ArrayList<>();

                    while (m.find()) {
                        uuids.add(m.group(1));
                    }

                    uuids.forEach(uuid -> downloadYouTubeAudio(buildVideoUrl(uuid), baseInputHandler));
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);

                    if (baseInputHandler != null) {
                        baseInputHandler.println("An exception occurred while downloading playlist: " + playlistID);
                    }
                }
            }
        } else {
            noFfmpegOrYoutubeDl();
        }
    }

    /**
     * Downloads the YouTube video's thumbnail with the provided
     * url to the current user's album art directory.
     *
     * @param url the url of the YouTube video to download
     * @throws YoutubeException if an exception occurred while downloading or processing the thumbnail
     */
    public static void downloadThumbnail(String url) throws YoutubeException {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        downloadThumbnail(url, DEFAULT_THUMBNAIL_DIMENSION);
    }

    /**
     * Downloads the YouTube video's thumbnail with the provided
     * url to the current user's album aart directory.
     *
     * @param url       the url of the YouTube video to download
     * @param dimension the dimensions to crop the image to
     * @throws YoutubeException if an error downloading or processing the thumbnail occurred
     */
    public static void downloadThumbnail(String url, Dimension dimension) throws YoutubeException {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        Preconditions.checkNotNull(dimension);
        Preconditions.checkNotNull(Console.INSTANCE.getUuid());

        // get thumbnail url and file name to save it as
        Optional<BufferedImage> optionalBi = getThumbnail(url, dimension);

        // could not download thumbnail for some reason
        if (optionalBi.isEmpty()) {
            throw new YoutubeException("Could not get raw thumbnail");
        }

        Optional<String> optionalUrlTitle = NetworkUtil.getUrlTitle(url);
        String urlTitle = "unknown_title";
        if (optionalUrlTitle.isPresent()) urlTitle = optionalUrlTitle.get();
        String parsedAsciiSaveName = StringUtil.removeNonAscii(urlTitle)
                .replace("- YouTube", "")
                .replaceAll(CyderRegexPatterns.windowsInvalidFilenameChars.pattern(), "").trim();

        // Remove trailing periods if present
        while (parsedAsciiSaveName.endsWith(".")) {
            parsedAsciiSaveName = parsedAsciiSaveName
                    .substring(0, parsedAsciiSaveName.length() - 1);
        }

        // if for some reason title was only periods and all were removed, assign a random title
        if (parsedAsciiSaveName.isEmpty()) {
            parsedAsciiSaveName = SecurityUtil.generateUuid();
        }

        File albumArtDir = Dynamic.buildDynamic(
                Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName(),
                UserFile.ALBUM_ART);

        if (!albumArtDir.exists()) {
            if (!albumArtDir.mkdirs()) {
                throw new YoutubeException("Could not create album art directory");
            }
        }

        File saveAlbumArt = OsUtil.buildFile(albumArtDir.getAbsolutePath(),
                parsedAsciiSaveName + Extension.PNG.getExtension());

        try {
            boolean written = ImageIO.write(optionalBi.get(), Extension.PNG.getExtensionWithoutPeriod(), saveAlbumArt);
            if (!written) throw new IOException("Failed to write album art");
        } catch (IOException e) {
            throw new YoutubeException("Could not write thumbnail to: " + saveAlbumArt.getAbsolutePath());
        }
    }

    /**
     * Retrieves the first valid UUID for the provided query (if one exists)
     *
     * @param youtubeQuery the user friendly query on YouTube. Example: "Gryffin Digital Mirage"
     * @return the first UUID obtained from the raw html page YouTube returns corresponding to the desired query
     */
    public static String getFirstUuid(String youtubeQuery) {
        Preconditions.checkNotNull(youtubeQuery);
        Preconditions.checkArgument(!youtubeQuery.isEmpty());

        String ret = null;

        String query = YOUTUBE_QUERY_BASE + youtubeQuery.replaceAll(
                CyderRegexPatterns.whiteSpaceRegex, "+");
        String jsonString = NetworkUtil.readUrl(query);

        String videoIdIdentifier = quote + VIDEO_ID + quote + colon + quote;
        if (jsonString.contains(videoIdIdentifier)) {
            String[] parts = jsonString.split(videoIdIdentifier);
            // Safe to access second index since we are checking for contains above
            ret = parts[1].substring(0, UUID_LENGTH);
        }

        return ret;
    }

    /** Outputs instructions to the console due to youtube-dl or ffmpeg not being installed. */
    private static void noFfmpegOrYoutubeDl() {
        Console.INSTANCE.getInputHandler().println("Sorry, but ffmpeg and/or youtube-dl "
                + "couldn't be located. Please make sure they are both installed and added to your PATH Windows "
                + "variable. Remember to also set the path to your youtube-dl executable in the user editor");

        CyderButton environmentVariableHelp = new CyderButton("Learn how to add environment variables");
        environmentVariableHelp.addActionListener(e -> NetworkUtil.openUrl(environmentVariables));
        Console.INSTANCE.getInputHandler().println(environmentVariableHelp);

        CyderButton downloadFFMPEG = new CyderButton("Learn how to download ffmpeg");
        downloadFFMPEG.addActionListener(e -> NetworkUtil.openUrl(FFMPEG_INSTALLATION));
        Console.INSTANCE.getInputHandler().println(downloadFFMPEG);

        CyderButton downloadYoutubeDL = new CyderButton("Learn how to download youtube-dl");
        downloadYoutubeDL.addActionListener(e -> NetworkUtil.openUrl(YOUTUBE_DL_INSTALLATION));
        Console.INSTANCE.getInputHandler().println(downloadYoutubeDL);
    }

    /**
     * Attempts to set the console background to the provided YouTube video's thumbnail
     *
     * @param url the url of the youtube video
     * @throws YoutubeException if an exception occurred while downloading/processing the thumbnail
     */
    public static void setAsConsoleBackground(String url) throws YoutubeException {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(NetworkUtil.isValidUrl(url));

        Dimension consoleDimension = Console.INSTANCE.getConsoleCyderFrame().getSize();

        Optional<BufferedImage> maxThumbnailOptional = getMaxResolutionThumbnail(getUuid(url));

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

        Optional<String> optionalUrlTitle = NetworkUtil.getUrlTitle(url);
        String urlTitle = "Unknown_title";
        if (optionalUrlTitle.isPresent()) urlTitle = optionalUrlTitle.get();

        File fullSaveFile = Dynamic.buildDynamic(
                Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(),
                UserFile.BACKGROUNDS.getName(),
                urlTitle + Extension.PNG.getExtension());

        try {
            ImageIO.write(maxThumbnail, Extension.PNG.getExtensionWithoutPeriod(), fullSaveFile);
            Console.INSTANCE.setBackgroundFile(fullSaveFile);
        } catch (IOException e) {
            ExceptionHandler.handle(e);
            throw new YoutubeException("Failed to write image to user background directory as: "
                    + fullSaveFile.getAbsolutePath());
        }
    }

    /**
     * Returns a BufferedImage of the provided YouTube video's thumbnail.
     *
     * @param url       the url of the YouTube video to query
     * @param dimension the dimension of the image to return. If the raw image is not big enough
     *                  the maximum square size image will be returned
     * @return a squared off version of the thumbnail if possible and. Empty optional else
     */
    public static Optional<BufferedImage> getThumbnail(String url, Dimension dimension) {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(dimension);
        Preconditions.checkArgument(!isPlaylistUrl(url));

        String uuid = getUuid(url);

        BufferedImage save = null;

        try {
            save = ImageUtil.read(buildMaxResThumbnailUrl(uuid));
        } catch (Exception e) {
            // exception here means no max res default was found
            try {
                save = ImageUtil.read(buildSdDefThumbnailUrl(uuid));
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        // Murphy's law so return failsafe
        if (save == null) {
            return Optional.empty();
        }

        // initialize size
        int w = save.getWidth();
        int h = save.getHeight();

        // if width is greater than requested width, crop to middle
        if (w > dimension.getWidth()) {
            int cropWidthStart = (int) ((w - dimension.getWidth()) / 2.0);
            save = save.getSubimage(cropWidthStart, 0, (int) dimension.getWidth(), h);
            w = save.getWidth();
        }

        // if height is greater than requested height, crop to middle
        if (h > dimension.getHeight()) {
            int cropHeightStart = (int) ((h - dimension.getHeight()) / 2);
            save = save.getSubimage(0, cropHeightStart, w, (int) dimension.getHeight());
        }

        // now width and height are guaranteed to be less than or equal the provided dimension
        // We can't really increase the resolution of the image from what was provided.

        return Optional.of(save);
    }

    /**
     * Returns whether the provided url is a playlist url.
     *
     * @param url the url
     * @return whether the provided url references a YouTube playlist
     */
    public static boolean isPlaylistUrl(String url) {
        return Preconditions.checkNotNull(url).startsWith(YOUTUBE_PLAYLIST_HEADER);
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
     * @param url the url of the playlist
     * @return the YouTube playlist id
     */
    public static String extractPlaylistId(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(isPlaylistUrl(url));

        return url.replace(YOUTUBE_PLAYLIST_HEADER, "").trim();
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
        Preconditions.checkArgument(YoutubeConstants.UUID_PATTERN.matcher(uuid).matches());

        return CyderUrls.YOUTUBE_VIDEO_HEADER + uuid;
    }

    /**
     * Returns a URL for the maximum resolution version of the YouTube video's thumbnail.
     *
     * @param uuid the uuid of the video
     * @return a URL for the maximum resolution version of the YouTube video's thumbnail
     */
    public static String buildMaxResThumbnailUrl(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(YoutubeConstants.UUID_PATTERN.matcher(uuid).matches());

        return YOUTUBE_THUMBNAIL_BASE + uuid + forwardSlash + MAX_RES_DEFAULT;
    }

    /**
     * Returns a url for the default thumbnail of a YouTube video.
     *
     * @param uuid the uuid of the video
     * @return a url for the default YouTube video's thumbnail
     */
    public static String buildSdDefThumbnailUrl(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(YoutubeConstants.UUID_PATTERN.matcher(uuid).matches());

        return YOUTUBE_THUMBNAIL_BASE + uuid + CyderStrings.forwardSlash + SD_DEFAULT;
    }

    /**
     * Extracts the uuid for the YouTube video from the url
     *
     * @param url the YouTube url to extract the uuid from
     * @return the extracted uuid
     */
    public static String getUuid(String url) {
        Preconditions.checkNotNull(url);
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
    @SuppressWarnings("ConstantConditions") /* Unit test asserts throws for query of null */
    public static String buildYouTubeApiV3SearchQuery(int numResults, String query) {
        Preconditions.checkArgument(SEARCH_QUERY_RESULTS_RANGE.contains(numResults));
        Preconditions.checkNotNull(query);
        Preconditions.checkArgument(!query.isEmpty());
        Preconditions.checkArgument(Props.youtubeApi3key.valuePresent());

        String youtubeKey = Props.youtubeApi3key.getValue();
        String[] queryWords = query.split(CyderRegexPatterns.whiteSpaceRegex);

        StringBuilder queryBuilder = new StringBuilder();
        for (int i = 0 ; i < queryWords.length ; i++) {
            String append = queryWords[i].replaceAll(CyderRegexPatterns.illegalUrlCharsRegex, "");
            queryBuilder.append(append.trim());

            if (i != queryWords.length - 1 && !append.isEmpty()) {
                queryBuilder.append(NetworkUtil.URL_SPACE);
            }
        }

        return YOUTUBE_API_V3_SEARCH_BASE
                + YoutubeConstants.MAX_RESULTS_PARAMETER + numResults
                + queryParameter + queryBuilder
                + videoTypeParameter + video
                + keyParameter + youtubeKey;
    }

    /**
     * Returns the maximum resolution thumbnail for the YouTube video with the provided uuid.
     *
     * @param uuid the uuid of the video
     * @return the maximum resolution thumbnail for the YouTube video
     */
    public static Optional<BufferedImage> getMaxResolutionThumbnail(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(YoutubeConstants.UUID_PATTERN.matcher(uuid).matches());

        String thumbnailUrl = buildMaxResThumbnailUrl(uuid);

        try {
            return Optional.of(ImageUtil.read(thumbnailUrl));
        } catch (Exception ignored) {
            try {
                thumbnailUrl = buildSdDefThumbnailUrl(uuid);
                return Optional.of(ImageUtil.read(thumbnailUrl));
            } catch (Exception ignored2) {
                return Optional.empty();
            }
        }
    }
}
