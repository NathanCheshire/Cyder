package cyder.youtube;

import com.google.common.base.Preconditions;
import cyder.console.Console;
import cyder.handlers.input.BaseInputHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.network.NetworkUtil;
import cyder.utils.OsUtil;

/**
 * An object to download audio and thumbnails from YouTube.
 * An instance of this class can represent a video/playlist of videos.
 */
public class NewDownload {
    /**
     * The string which could be a link, id, or query.
     */
    private String providedDownloadString;

    /**
     * The download type the provided download string refers to.
     */
    private Type downloadType;

    /**
     * The type of download
     */
    private enum Type {
        VIDEO_LINK,
        PLAYLIST_LINK,
    }

    /**
     * The name to save the audio download as.
     */
    private String audioDownloadName;

    /**
     * The name to save the thumbnail download as.
     */
    private String thumbnailDownloadName;

    /**
     * Constructs a new YoutubeDownload object.
     */
    public NewDownload() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Sets the download type of this download to a video link.
     *
     * @param videoLink the video link
     */
    public void setVideoLink(String videoLink) {
        Preconditions.checkNotNull(videoLink);
        Preconditions.checkArgument(!videoLink.isEmpty());
        Preconditions.checkArgument(!NetworkUtil.readUrl(videoLink).isEmpty());

        this.providedDownloadString = videoLink;
        this.downloadType = Type.VIDEO_LINK;
    }

    /**
     * Sets the download type of this download to a playlist link.
     *
     * @param playlistLink the playlist link
     */
    public void setPlaylistLink(String playlistLink) {
        Preconditions.checkNotNull(playlistLink);
        Preconditions.checkArgument(!playlistLink.isEmpty());
        Preconditions.checkArgument(!NetworkUtil.readUrl(playlistLink).isEmpty());

        this.providedDownloadString = playlistLink;
        this.downloadType = Type.PLAYLIST_LINK;
    }

    /**
     * Sets the download type of this download to a video id.
     *
     * @param videoId the video id
     */
    public void setVideoId(String videoId) {
        Preconditions.checkNotNull(videoId);
        Preconditions.checkArgument(!videoId.isEmpty());
        Preconditions.checkArgument(videoId.length() == YoutubeConstants.UUID_LENGTH);

        String link = YoutubeUtil.buildVideoUrl(videoId);
        Preconditions.checkArgument(!NetworkUtil.readUrl(link).isEmpty());

        this.providedDownloadString = link;
        this.downloadType = Type.VIDEO_LINK;
    }

    /**
     * Sets the download type of this download to a playlist id.
     *
     * @param playlistId the playlist id
     */
    public void setPlaylistId(String playlistId) {
        Preconditions.checkNotNull(playlistId);
        Preconditions.checkArgument(!playlistId.isEmpty());

        String link = YoutubeConstants.YOUTUBE_PLAYLIST_HEADER + playlistId;
        Preconditions.checkArgument(!NetworkUtil.readUrl(link).isEmpty());

        this.providedDownloadString = link;
        this.downloadType = Type.PLAYLIST_LINK;
    }

    /**
     * Sets the download type of this download to a query.
     *
     * @param query the video link
     */
    public void setVideoQuery(String query) {
        Preconditions.checkNotNull(query);
        Preconditions.checkArgument(!query.isEmpty());

        String firstUuid = YoutubeUtil.getFirstUuid(query);
        if (firstUuid == null || firstUuid.length() != YoutubeConstants.UUID_LENGTH) {
            throw new IllegalArgumentException("Could not find video for query: " + query);
        }

        this.providedDownloadString = YoutubeUtil.buildVideoUrl(firstUuid);
        this.downloadType = Type.VIDEO_LINK;
    }

    /**
     * Sets the provided name as the name to save the .mp3 and .png audio and thumbnail downloads as.
     *
     * @param downloadNames the name to save the downloads as
     */
    public void setDownloadNames(String downloadNames) {
        setAudioDownloadName(downloadNames);
        setThumbnailDownloadName(downloadNames);
    }

    /**
     * Sets the name to save the .mp3 download as.
     *
     * @param audioDownloadName the name to save the .mp3 download as
     */
    public void setAudioDownloadName(String audioDownloadName) {
        Preconditions.checkNotNull(audioDownloadName);
        Preconditions.checkArgument(!audioDownloadName.isEmpty());
        Preconditions.checkArgument(OsUtil.isValidFilename(audioDownloadName));

        this.audioDownloadName = audioDownloadName;
    }

    /**
     * Sets the name to save the .png download as.
     *
     * @param thumbnailDownloadName the name to save the .png download as
     */
    public void setThumbnailDownloadName(String thumbnailDownloadName) {
        Preconditions.checkNotNull(thumbnailDownloadName);
        Preconditions.checkArgument(!thumbnailDownloadName.isEmpty());
        Preconditions.checkArgument(OsUtil.isValidFilename(thumbnailDownloadName));

        this.thumbnailDownloadName = thumbnailDownloadName;
    }

    private BaseInputHandler printOutputHandler;

    public void setPrintOutputToConsole(boolean printOutputToConsole) {
        if (printOutputToConsole) {
            setPrintOutputHandler(Console.INSTANCE.getInputHandler());
        } else {
            printOutputHandler = null;
        }
    }

    public void setPrintOutputHandler(BaseInputHandler inputHandler) {
        Preconditions.checkNotNull(inputHandler);

        this.printOutputHandler = inputHandler;
    }

    public void removePrintOutputHandler() {
        this.printOutputHandler = null;
    }

    // todo

    /**
     * Starts the download of the audio and thumbnail file(s).
     */
    public void downloadAudioAndThumbnail() {
        downloadAudio();
        downloadThumbnail();
    }

    /**
     * Starts the download of the audio file(s).
     */
    public void downloadAudio() {
        Preconditions.checkState(providedDownloadString != null);


    }

    /**
     * Starts the download of the thumbnail file(s).
     */
    public void downloadThumbnail() {
        Preconditions.checkState(providedDownloadString != null);


    }
}
