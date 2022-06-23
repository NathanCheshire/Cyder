package cyder.youtube;

import com.google.common.collect.Range;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import java.awt.*;
import java.util.regex.Pattern;

/**
 * Constants used throughout YouTube utility classes.
 */
public final class YoutubeConstants {
    /**
     * The error message printed to the console if the YouTube api v3 key is not set.
     */
    public static final String KEY_NOT_SET_ERROR_MESSAGE = "Sorry, your YouTubeAPI3 key has not been set. "
            + "Visit the user editor to learn how to set this in order to download whole playlists. "
            + "In order to download individual videos, simply use the same play "
            + "command followed by a video URL or query";

    /**
     * The default resolution of thumbnails to download when the play command is invoked.
     */
    public static final Dimension DEFAULT_THUMBNAIL_DIMENSION = new Dimension(720, 720);

    /**
     * The extract audio ffmpeg flag.
     */
    public static final String FFMPEG_EXTRACT_AUDIO_FLAG = "--extract-audio";

    /**
     * The audio format ffmpeg flag.
     */
    public static final String FFMPEG_AUDIO_FORMAT_FLAG = "--audio-format";

    /**
     * The output ffmpeg flag.
     */
    public static final String FFMPEG_OUTPUT_FLAG = "--output";

    /**
     * The range of valid values for the number of results a youtube api 3 search query.
     */
    public static final Range<Integer> SEARCH_QUERY_RESULTS_RANGE = Range.closed(1, 20);

    /**
     * The string used to represent a space in a url.
     */
    public static final String URL_SPACE = "%20";

    /**
     * The key used for a max resolution thumbnail.
     */
    public static final String MAX_RES_DEFAULT = "maxresdefault.jpg";

    /**
     * The key used for a standard definition thumbnail.
     */
    public static final String SD_DEFAULT = "sddefault.jpg";

    /**
     * The maximum number of chars that can be used for a filename from a youtube video's title.
     */
    public static final int MAX_THUMBNAIL_CHARS = 20;

    /**
     * The pattern to identify a valid YouTube UUID.
     */
    public static final Pattern uuidPattern = Pattern.compile("[A-Za-z0-9_\\-]{0,11}");

    /**
     * The delay between download button updates.
     */
    public static final int DOWNLOAD_UPDATE_DELAY = 1000;

    /**
     * Suppress default constructor.
     */
    private YoutubeConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
