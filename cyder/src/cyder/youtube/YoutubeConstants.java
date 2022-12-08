package cyder.youtube;

import com.google.common.collect.Range;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

import java.awt.*;
import java.util.regex.Pattern;

/**
 * Constants used throughout YouTube utility classes.
 */
public final class YoutubeConstants {
    /**
     * The length of YouTube UUIDs.
     */
    public static final int UUID_LENGTH = 11;

    /**
     * The youtube query base url.
     */
    static final String YOUTUBE_QUERY_BASE = "https://www.youtube.com/results?search_query=";

    /**
     * The youtube video base url.
     */
    static final String YOUTUBE_VIDEO_BASE = "https://www.youtube.com/watch?v=";

    /**
     * A link to set environment variables for Windows.
     */
    static final String environmentVariables
            = "https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10/";

    /**
     * The header that all youtube playlists start with.
     */
    static final String YOUTUBE_PLAYLIST_HEADER = "https://www.youtube.com/playlist?list=";

    /**
     * The header used for obtaining a youtube video's highest resolution thumbnail.
     */
    static final String YOUTUBE_THUMBNAIL_BASE = "https://img.youtube.com/vi/";

    /**
     * The google youtube api v3 for getting a playlist's items
     */
    static final String YOUTUBE_API_V3_PLAYLIST_ITEMS = "https://www.googleapis.com/youtube/v3/playlistItems?";

    /**
     * The base for youtube api v3 search queries.
     */
    static final String YOUTUBE_API_V3_SEARCH_BASE = "https://www.googleapis.com/youtube/v3/search?part=snippet";

    /**
     * The key to get the youtube api v3 key from the props.
     */
    static final String YOUTUBE_API_3_KEY = "youtube_api_3_key";

    /**
     * A link for how to install ffmpeg.
     */
    static final String FFMPEG_INSTALLATION = "https://www.wikihow.com/Install-FFmpeg-on-Windows";

    /**
     * A link for how to install youtube-dl.
     */
    static final String YOUTUBE_DL_INSTALLATION = "https://github.com/ytdl-org/youtube-dl#installation";

    /**
     * The error message printed to the console if the YouTube api v3 key is not set.
     */
    static final String KEY_NOT_SET_ERROR_MESSAGE = "Sorry, your YouTubeAPI3 key has not been set. "
            + "Visit the user editor to learn how to set this in order to download whole playlists. "
            + "In order to download individual videos, simply use the same play "
            + "command followed by a video URL or query";

    /**
     * The default resolution of thumbnails to download when the play command is invoked.
     */
    static final Dimension DEFAULT_THUMBNAIL_DIMENSION = new Dimension(720, 720);

    /**
     * The maximum number of chars that can be used for a filename from a youtube video's title.
     */
    static final int MAX_THUMBNAIL_CHARS = 20;

    /**
     * The range of valid values for the number of results a youtube api 3 search query.
     */
    static final Range<Integer> SEARCH_QUERY_RESULTS_RANGE = Range.closed(1, MAX_THUMBNAIL_CHARS);

    /**
     * The key used for a max resolution thumbnail.
     */
    static final String MAX_RES_DEFAULT = "maxresdefault.jpg";

    /**
     * The key used for a standard definition thumbnail.
     */
    static final String SD_DEFAULT = "sddefault.jpg";

    /**
     * The id to look for in the document returned by a url query.
     */
    static final String VIDEO_ID = "videoId";

    /**
     * The pattern to identify a valid YouTube UUID.
     */
    static final Pattern UUID_PATTERN = Pattern.compile("[-_A-Za-z0-9]{" + UUID_LENGTH + "}");

    /**
     * The delay between download button updates.
     */
    public static final int DOWNLOAD_UPDATE_DELAY = 1000;

    /**
     * The max results parameter for searching youtube.
     */
    static final String MAX_RESULTS_PARAMETER = "&maxResults=";

    /**
     * The value to indicate a download has not yet finished.
     */
    static final int DOWNLOAD_NOT_FINISHED = Integer.MIN_VALUE;

    /**
     * The regular exit code for a {@link Process}.
     */
    static final int SUCCESSFUL_EXIT_CODE = 0;

    /**
     * The key to obtain the audio output format for ffmpeg from the props.
     */
    static final String FFMPEG_AUDIO_OUTPUT_FORMAT = "ffmpeg_audio_output_format";

    /**
     * The suffix appended to all YouTube video titles in the URL of a webpage.
     */
    static final String YOUTUBE_VIDEO_URL_TITLE_SUFFIX = "- YouTube";

    /**
     * The cancel text.
     */
    static final String CANCEL = "Cancel";

    /**
     * The canceled text.
     */
    static final String CANCELED = "Canceled";

    /**
     * The minimum value of the download progress bar.
     */
    static final int downloadProgressMin = 0;

    /**
     * The maximum value of the download progress bar.
     */
    static final int downloadProgressMax = 10000;

    /**
     * The downloaded text for the button.
     */
    static final String DOWNLOADED = "Downloaded";

    /**
     * The failed text for the button.
     */
    static final String FAILED = "Failed";

    /**
     * The index of the matcher group the progress of the youtube download lies at.
     */
    static final int progressIndex = 1;

    /**
     * The index of the matcher group the size of the youtube download lies at.
     */
    static final int sizeIndex = 2;

    /**
     * The index of the matcher group the rate of the youtube download lies at.
     */
    static final int rateIndex = 3;

    /**
     * The index of the matcher group the eta of the youtube download lies at.
     */
    static final int etaIndex = 4;

    /**
     * The url query parameter.
     */
    static final String queryParameter = "&q=";

    /**
     * The youtube video type url parameter.
     */
    static final String videoTypeParameter = "&type=";

    /**
     * The video type parameter for constructed youtube search query urls.
     */
    static final String video = "video";

    /**
     * The key parameter for constructed youtube search query urls.
     */
    static final String keyParameter = "&key=";

    /**
     * Suppress default constructor.
     */
    private YoutubeConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
