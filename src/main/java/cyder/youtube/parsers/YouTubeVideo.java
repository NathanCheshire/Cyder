package cyder.youtube.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * A YouTube video class containing information related to a YouTube video.
 * This is contained in the {@link YouTubeSearchResultPage} list.
 */
public class YouTubeVideo {
    /**
     * The kind. Typically "kind": "youtube#searchResult".
     */
    private String kind;

    /**
     * The etag.
     */
    private String etag;

    /**
     * The ID object which contains the YouTube video ID.
     */
    private Id id;

    /**
     * The snippet object containing most of the relevant data.
     */
    private Snippet snippet;

    /**
     * Constructs a new YouTubeVideo.
     */
    public YouTubeVideo() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the kind. Typically "kind": "youtube#searchResult".
     *
     * @return the kind. Typically "kind": "youtube#searchResult"
     */
    public String getKind() {
        return kind;
    }

    /**
     * Sets the kind. Typically "kind": "youtube#searchResult".
     *
     * @param kind the kind. Typically "kind": "youtube#searchResult"
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * Returns the etag.
     *
     * @return the etag
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Sets the etag.
     *
     * @param etag the etag
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * Returns the ID object.
     *
     * @return the ID object
     */
    public Id getId() {
        return id;
    }

    /**
     * Sets the ID object.
     *
     * @param id the ID object
     */
    public void setId(Id id) {
        this.id = id;
    }

    /**
     * Returns the snippet object containing most of the relevant data.
     *
     * @return the snippet object containing most of the relevant data
     */
    public Snippet getSnippet() {
        return snippet;
    }

    /**
     * Sets snippet object containing most of the relevant data.
     *
     * @param snippet snippet object containing most of the relevant data
     */
    public void setSnippet(Snippet snippet) {
        this.snippet = snippet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof YouTubeVideo)) {
            return false;
        }

        YouTubeVideo other = (YouTubeVideo) o;
        return other.kind.equals(kind)
                && other.etag.equals(etag)
                && other.id.equals(id)
                && other.snippet.equals(snippet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = kind.hashCode();
        ret = 31 * ret + etag.hashCode();
        ret = 31 * ret + id.hashCode();
        ret = 31 * ret + snippet.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "YouTubeVideo{"
                + "kind=\"" + kind + "\""
                + ", etag=\"" + etag + "\""
                + ", id=" + id
                + ", snippet=" + snippet
                + "}";
    }
}
