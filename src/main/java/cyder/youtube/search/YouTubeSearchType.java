package cyder.youtube.search;

/**
 * A youtube search type.
 */
public enum YouTubeSearchType {
    VIDEO("video"),
    CHANNEL("channel"),
    PLAYLIST("playlist");

    /**
     * The url parameter for this youtube search type.
     */
    private final String urlParameter;

    YouTubeSearchType(String urlParameter) {
        this.urlParameter = urlParameter;
    }

    /**
     * Returns the url parameter for this youtube search type.
     *
     * @return the url parameter for this youtube search type
     */
    public String getUrlParameter() {
        return urlParameter;
    }
}
