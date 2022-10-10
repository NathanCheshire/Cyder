package cyder.youtube;

/**
 * The types of downloads available by {@link YoutubeDownload}.
 */
public enum DownloadType {
    /**
     * The download is downloading the video's audio.
     */
    AUDIO("audio"),
    /**
     * The download is downloading the video.
     */
    VIDEO("video"),
    /**
     * The download is downloading the video with teh audio separate.
     */
    AUDIO_AND_VIDEO_SEPARATE("audio and video separately");

    /**
     * The representation of this download type
     */
    private final String representation;

    DownloadType(String representation) {
        this.representation = representation;
    }

    /**
     * Returns the representation of this download type.
     *
     * @return the representation of this download type
     */
    public String getRepresentation() {
        return representation;
    }
}
