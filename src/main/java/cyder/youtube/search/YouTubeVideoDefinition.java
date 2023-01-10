package cyder.youtube.search;

/**
 * The video definition for a {@link SearchQuery}.
 */
public enum YouTubeVideoDefinition {
    ANY("any"),
    HIGH("high"),
    STANDARD("standard");

    /**
     * The url parameter for this video definition
     */
    private final String urlParameter;

    YouTubeVideoDefinition(String urlParameter) {
        this.urlParameter = urlParameter;
    }

    /**
     * Returns the url parameter for this video definition.
     *
     * @return the url parameter for this video definition
     */
    public String getUrlParameter() {
        return urlParameter;
    }
}
