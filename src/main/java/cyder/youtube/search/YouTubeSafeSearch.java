package cyder.youtube.search;

/**
 * Youtube safe search options for a {@link YouTubeSearchQuery}.
 */
public enum YouTubeSafeSearch {
    MODERATE("moderate"),
    NONE("none"),
    STRICT("strict");

    /**
     * The url parameter for this YouTube safe search.
     */
    private final String urlParameter;

    YouTubeSafeSearch(String urlParameter) {
        this.urlParameter = urlParameter;
    }

    /**
     * Returns the url parameter for this YouTube safe search.
     *
     * @return the url parameter for this YouTube safe search
     */
    public String getUrlParameter() {
        return urlParameter;
    }
}
