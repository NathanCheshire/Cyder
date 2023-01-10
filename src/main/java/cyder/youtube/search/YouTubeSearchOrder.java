package cyder.youtube.search;

/**
 * The order for the result of a {@link SearchQuery}.
 */
public enum YouTubeSearchOrder {
    DATE("date"),
    RATING("rating"),
    RELEVANCE("relevance"),
    TITLE("title"),
    VIDEO_COUNT("videoCount"),
    VIEW_COUNT("viewCount");

    /**
     * The url parameter for this search order.
     */
    private final String urlParameter;

    YouTubeSearchOrder(String urlParameter) {
        this.urlParameter = urlParameter;
    }

    /**
     * Returns the url parameter for this search order.
     *
     * @return the url parameter for this search order
     */
    public String getUrlParameter() {
        return urlParameter;
    }
}
