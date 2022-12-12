package cyder.youtube;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.audio.AudioUtil;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderUrls;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.exceptions.YoutubeException;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.ui.button.CyderButton;
import cyder.user.UserFile;
import cyder.utils.ArrayUtil;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;

import static cyder.strings.CyderStrings.*;
import static cyder.youtube.YoutubeConstants.*;

/**
 * Utility methods related to YouTube videos.
 */
public final class YoutubeUtil {
    /**
     * Suppress default constructor.
     */
    private YoutubeUtil() {
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

        if (AudioUtil.ffmpegInstalled() && AudioUtil.youtubeDlInstalled()) {
            YoutubeDownload youtubeDownload = new YoutubeDownload(url, DownloadType.AUDIO);
            youtubeDownload.setInputHandler(baseInputHandler);
            youtubeDownload.download();
        } else {
            onNoFfmpegOrYoutubeDlInstalled();
        }
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
                return;
            }

            try {
                String link = YOUTUBE_API_V3_PLAYLIST_ITEMS
                        + "part="
                        + "snippet%2C+id"
                        + "&playlistId="
                        + playlistID
                        + "&key="
                        + Props.youtubeApi3key.getValue();

                String jsonResponse = NetworkUtil.readUrl(link);

                Matcher m = CyderRegexPatterns.youtubeApiV3UuidPattern.matcher(jsonResponse);
                ArrayList<String> uuids = new ArrayList<>();

                while (m.find()) {
                    uuids.add(m.group(1));
                }

                uuids.forEach(uuid -> downloadYouTubeAudio(buildVideoUrl(uuid), baseInputHandler));
            } catch (Exception e) {
                ExceptionHandler.handle(e);

                if (baseInputHandler != null) {
                    baseInputHandler.println("An exception occurred while downloading playlist: " + playlistID);
                }
            }
        } else {
            onNoFfmpegOrYoutubeDlInstalled();
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
     * The unknown title string if a title cannot be extracted from a url.
     */
    private static final String UNKNOWN_TITLE = "Unknown_title";

    /**
     * Returns the name to save the YouTube video's audio/thumbnail as.
     *
     * @param url the url
     * @return the save name
     */
    public static String getDownloadSaveName(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        Optional<String> optionalUrlTitle = NetworkUtil.getUrlTitle(url);
        String urlTitle = optionalUrlTitle.orElse(UNKNOWN_TITLE);

        String parsedSaveName = StringUtil.removeNonAscii(urlTitle)
                .replace(YOUTUBE_VIDEO_URL_TITLE_SUFFIX, "")
                .replaceAll(CyderRegexPatterns.windowsInvalidFilenameChars.pattern(), "").trim();

        while (parsedSaveName.endsWith(".")) {
            parsedSaveName = (parsedSaveName.substring(0, parsedSaveName.length() - 1));
        }

        if (parsedSaveName.isEmpty()) {
            parsedSaveName = SecurityUtil.generateUuid();
        }

        return parsedSaveName;
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

        Optional<BufferedImage> optionalBi = getThumbnail(url, dimension);
        if (optionalBi.isEmpty()) {
            throw new YoutubeException("Could not get raw thumbnail");
        }

        String saveDownloadName = getDownloadSaveName(url);

        File albumArtDir = Dynamic.buildDynamic(
                Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName(),
                UserFile.ALBUM_ART);

        if (!albumArtDir.exists()) {
            if (!albumArtDir.mkdirs()) {
                throw new YoutubeException("Could not create album art directory");
            }
        }

        File saveAlbumArt = OsUtil.buildFile(albumArtDir.getAbsolutePath(),
                saveDownloadName + Extension.PNG.getExtension());

        try {
            boolean written = ImageIO.write(optionalBi.get(), Extension.PNG.getExtensionWithoutPeriod(), saveAlbumArt);
            if (!written) throw new IOException("Failed to write album art");
        } catch (IOException e) {
            throw new YoutubeException("Could not write thumbnail to: " + saveAlbumArt.getAbsolutePath());
        }
    }

    /**
     * Retrieves the first valid UUID for the provided query using web scraping.
     *
     * @param youtubeQuery the user friendly query on YouTube. Example: "Gryffin Digital Mirage"
     * @return the first UUID obtained from the raw html page YouTube returns corresponding to the desired query
     */
    public static String getFirstUuid(String youtubeQuery) {
        Preconditions.checkNotNull(youtubeQuery);
        Preconditions.checkArgument(!youtubeQuery.isEmpty());

        String ret = null;

        String query = YOUTUBE_QUERY_BASE + youtubeQuery
                .replaceAll(CyderRegexPatterns.whiteSpaceRegex, "+");
        String jsonString = NetworkUtil.readUrl(query);

        String videoIdIdentifier = quote + VIDEO_ID + quote + colon + quote;
        if (jsonString.contains(videoIdIdentifier)) {
            String[] parts = jsonString.split(videoIdIdentifier);
            String firstUuidAndAfter = parts[1];
            ret = firstUuidAndAfter.substring(0, UUID_LENGTH);
        }

        return ret;
    }

    /**
     * Outputs instructions to the console due to youtube-dl or ffmpeg not being installed.
     */
    private static void onNoFfmpegOrYoutubeDlInstalled() {
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

        String uuid = extractUuid(url);

        BufferedImage save = null;

        try {
            save = ImageUtil.read(buildMaxResolutionThumbnailUrl(uuid));
        } catch (Exception ignored) {
            try {
                save = ImageUtil.read(buildStandardDefinitionThumbnailUrl(uuid));
            } catch (Exception ignored2) {}
        }

        if (save == null) return Optional.empty();

        int width = save.getWidth();
        int height = save.getHeight();

        if (width > dimension.getWidth()) {
            int cropWidthStart = (int) ((width - dimension.getWidth()) / 2.0);
            save = save.getSubimage(cropWidthStart, 0, (int) dimension.getWidth(), height);
            width = save.getWidth();
        }
        if (height > dimension.getHeight()) {
            int cropHeightStart = (int) ((height - dimension.getHeight()) / 2);
            save = save.getSubimage(0, cropHeightStart, width, (int) dimension.getHeight());
        }

        return Optional.of(save);
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
        Preconditions.checkArgument(YoutubeConstants.UUID_PATTERN.matcher(uuid).matches());

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
        Preconditions.checkArgument(YoutubeConstants.UUID_PATTERN.matcher(uuid).matches());

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
        Preconditions.checkArgument(YoutubeConstants.UUID_PATTERN.matcher(uuid).matches());

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
        ImmutableList<String> queryWords = ArrayUtil.toList(query.split(CyderRegexPatterns.whiteSpaceRegex));

        ArrayList<String> legalCharsQueryWords = new ArrayList<>();
        queryWords.forEach(part -> legalCharsQueryWords.add(
                part.replaceAll(CyderRegexPatterns.illegalUrlCharsRegex, "")));

        String builtQuery = StringUtil.joinParts(legalCharsQueryWords, NetworkUtil.URL_SPACE);

        return YOUTUBE_API_V3_SEARCH_BASE
                + YoutubeConstants.MAX_RESULTS_PARAMETER + numResults
                + queryParameter + builtQuery
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
}
