package cyder.youtube.search;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.props.Props;

/**
 * A class to build a search query url using a YouTube API v3 key to search youtube for a list of videos.
 */
public class SearchQuery {

    /**
     * The search base for youtube api v3.
     */
    private static final String searchHeader = "https://www.googleapis.com/youtube/v3/search";

    /**
     * The snippet string used for the part parameter.
     * This MUST be used according to the Google developer documentation.
     */
    private static final String SNIPPET = "snippet";

    /**
     * The url constructed from the provided builder's parameters.
     */
    private final String url;

    /**
     * Constructs a new search query based on the provided builder.
     *
     * @param builder the builder with set parameters for the search query
     */
    public SearchQuery(Builder builder) {
        Preconditions.checkNotNull(builder);

        url = searchHeader
                + YouTubeSearchListApiParameter.PART.getUrlParameter(true)
                + SNIPPET
                + YouTubeSearchListApiParameter.QUERY.getUrlParameter()
                + builder.getQuery()
                + YouTubeSearchListApiParameter.TYPE.getUrlParameter()
                + builder.getType().getUrlParameter()
                + YouTubeSearchListApiParameter.SAFE_SEARCH.getUrlParameter()
                + builder.getSafeSearch().getUrlParameter()
                + YouTubeSearchListApiParameter.KEY.getUrlParameter()
                + builder.getKey();
    }

    /**
     * Returns the url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    // todo method to return results of querying url

    /**
     * Builds and returns a new {@link Builder} with the default query.
     * The primary use of this is for ease of key validation.
     *
     * @return a new, default builder
     */
    public Builder buildDefaultBuilder() {
        return new Builder("Solitaires by Future", Props.youtubeApi3key.getValue())
                .setSafeSearch(YouTubeSafeSearch.NONE)
                .setType(YouTubeSearchType.VIDEO);
    }

    /**
     * A builder for a {@link SearchQuery}.
     */
    public static final class Builder {
        /**
         * The query string.
         */
        private final String query;

        /**
         * The API key.
         */
        private final String key;

        /**
         * The search type.
         */
        private YouTubeSearchType type = YouTubeSearchType.VIDEO;

        /**
         * The safe search type.
         */
        private YouTubeSafeSearch safeSearch = YouTubeSafeSearch.NONE;

        /**
         * Constructs a new builder for a YouTube search query.
         *
         * @param query the query string such as "Solitaires by Future"
         * @param key   the YouTube API v3 key
         */
        public Builder(String query, String key) {
            Preconditions.checkNotNull(query);
            Preconditions.checkArgument(!query.trim().isEmpty());
            Preconditions.checkNotNull(key);
            Preconditions.checkArgument(!key.isEmpty());

            this.query = query.trim().replaceAll("\\s+", "+");
            this.key = key;
        }

        /**
         * Returns the query string formatter for use in the url.
         *
         * @return the query string
         */
        public String getQuery() {
            return query;
        }

        /**
         * Returns the key for this search query.
         *
         * @return the key for this search query
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns the type of search.
         *
         * @return the type of search
         */
        public YouTubeSearchType getType() {
            return type;
        }

        /**
         * Sets the type of search.
         *
         * @param type the type of search
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setType(YouTubeSearchType type) {
            Preconditions.checkNotNull(type);

            this.type = type;
            return this;
        }

        /**
         * Returns the type of safe search.
         *
         * @return the type of safe search
         */
        public YouTubeSafeSearch getSafeSearch() {
            return safeSearch;
        }

        /**
         * Sets the type of safe search.
         *
         * @param safeSearch the type of safe search
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setSafeSearch(YouTubeSafeSearch safeSearch) {
            Preconditions.checkNotNull(safeSearch);

            this.safeSearch = safeSearch;
            return this;
        }

        /**
         * Builds and returns a new search query based on the set parameters.
         *
         * @return a new search query based on the set parameters
         */
        public SearchQuery build() {
            return new SearchQuery(this);
        }
    }
}
