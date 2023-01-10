package cyder.youtube.search;

/**
 * A search type for a {@link YouTubeSearchQuery}.
 */
public enum YouTubeSearchType {
    VIDEO("video"),
    CHANNEL("channel"),
    PLAYLIST("playlist");

    /**
     * The url parameter for this YouTube search type.
     */
    private final String urlParameter;

    YouTubeSearchType(String urlParameter) {
        this.urlParameter = urlParameter;
    }

    /**
     * Returns the url parameter for this YouTube search type.
     *
     * @return the url parameter for this YouTube search type
     */
    public String getUrlParameter() {
        return urlParameter;
    }
}
