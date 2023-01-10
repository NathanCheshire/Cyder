package cyder.youtube.search;

/**
 * The video duration for a {@link YouTubeSearchQuery}.
 */
public enum YouTubeVideoDuration {
    //any,long,medium,short
    ANY("any"),
    LONG("long"),
    MEDIUM("medium"),
    SHORT("short");

    /**
     * The url parameter for this video duration.
     */
    private final String urlParameter;

    YouTubeVideoDuration(String urlParameter) {
        this.urlParameter = urlParameter;
    }

    /**
     * Returns the url parameter for this video duration.
     *
     * @return the url parameter for this video duration
     */
    public String getUrlParameter() {
        return urlParameter;
    }
}
