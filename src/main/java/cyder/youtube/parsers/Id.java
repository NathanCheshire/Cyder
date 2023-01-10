package cyder.youtube.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * An ID object of a {@link YouTubeVideo}.
 */
public class Id {
    /**
     * The kind, typically "youtube#video".
     */
    private String kind;

    /**
     * The eleven digit YouTube video ID.
     */
    private String videoId;

    /**
     * Constructs a new YouTube ID.
     */
    public Id() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the kind.
     *
     * @return the kind
     */
    public String getKind() {
        return kind;
    }

    /**
     * Sets the kind.
     *
     * @param kind the kind
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * Returns the video ID.
     *
     * @return the video ID
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * Sets the video ID.
     *
     * @param videoId the video ID
     */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Id)) {
            return false;
        }

        Id other = (Id) o;
        return other.videoId.equals(videoId)
                && other.kind.equals(kind);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = kind.hashCode();
        ret = 31 * ret + videoId.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Id{"
                + "kind=\"" + kind + "\""
                + ", videoId=\"" + videoId + "\""
                + '}';
    }
}
