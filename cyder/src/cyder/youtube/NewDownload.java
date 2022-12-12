package cyder.youtube;

import com.google.common.base.Preconditions;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.OsUtil;

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
        VIDEO_ID,
        PLAYLIST_ID,
        VIDEO_QUERY,
    }

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

        this.providedDownloadString = videoId;
        this.downloadType = Type.VIDEO_ID;
    }

    /**
     * Sets the download type of this download to a playlist id.
     *
     * @param playlistId the playlist id
     */
    public void setPlaylistId(String playlistId) {
        Preconditions.checkNotNull(playlistId);
        Preconditions.checkArgument(!playlistId.isEmpty());

        this.providedDownloadString = playlistId;
        this.downloadType = Type.PLAYLIST_ID;
    }

    /**
     * Sets the download type of this download to a query.
     *
     * @param query the video link
     */
    public void setVideoQuery(String query) {
        Preconditions.checkNotNull(query);
        Preconditions.checkArgument(!query.isEmpty());

        this.providedDownloadString = query;
        this.downloadType = Type.VIDEO_QUERY;
    }

    private String audioDownloadName;
    private String thumbnailDownloadName;

    public void setDownloadNames(String downloadNames) {
        setAudioDownloadName(downloadNames);
        setThumbnailDownloadName(downloadNames);
    }

    public void setAudioDownloadName(String audioDownloadName) {
        Preconditions.checkNotNull(audioDownloadName);
        Preconditions.checkArgument(!audioDownloadName.isEmpty());
        Preconditions.checkArgument(OsUtil.isValidFilename(audioDownloadName));

        this.audioDownloadName = audioDownloadName;
    }

    public void setThumbnailDownloadName(String thumbnailDownloadName) {
        Preconditions.checkNotNull(thumbnailDownloadName);
        Preconditions.checkArgument(!thumbnailDownloadName.isEmpty());
        Preconditions.checkArgument(OsUtil.isValidFilename(thumbnailDownloadName));

        this.thumbnailDownloadName = thumbnailDownloadName;
    }
}
