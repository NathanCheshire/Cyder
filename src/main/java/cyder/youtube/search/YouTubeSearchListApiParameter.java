package cyder.youtube.search;

/**
 * Supported YouTube API search-list parameters.
 */
enum YouTubeSearchListApiParameter {
    PART("part"),
    QUERY("q"),
    TYPE("type"),
    KEY("key"),
    SAFE_SEARCH("safeSearch");

    /**
     * The url parameter name for this parameter.
     */
    private final String urlParameterName;

    YouTubeSearchListApiParameter(String urlParameterName) {
        this.urlParameterName = urlParameterName;
    }

    /**
     * Returns the url parameter string for this url parameter.
     *
     * @return the url parameter string for this url parameter
     */
    public String getUrlParameter() {
        return getUrlParameter(false);
    }

    /**
     * Returns the url parameter string for this url parameter.
     *
     * @param firstParameter whether this is the first parameter in the url
     * @return the url parameter string for this url parameter
     */
    public String getUrlParameter(boolean firstParameter) {
        if (firstParameter) {
            return "?" + urlParameterName + "=";
        } else {
            return "&" + urlParameterName + "=";
        }
    }
}
