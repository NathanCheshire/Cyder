package cyder.youtube.search;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderRegexPatterns;
import cyder.handlers.internal.ExceptionHandler;
import cyder.utils.SerializationUtil;
import cyder.youtube.parsers.YouTubeSearchResultPage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

/**
 * A builder for a YouTube search query with the possibility of setting the following parameters
 * and get the serialized results:
 *
 * <ul>
 *     <li>Search query</li>
 *     <li>{@link Builder#setSafeSearch(YouTubeSafeSearch)}</li>
 *     <li>{@link Builder#setKey(String)}</li>
 *     <li>{@link Builder#setSearchOrder(YouTubeSearchOrder)}</li>
 *     <li>{@link Builder#setType(YouTubeSearchType)}</li>
 *     <li>{@link Builder#setMaxResults(int)}</li>
 *     <li>{@link Builder#setVideoDefinition(YouTubeVideoDefinition)}</li>
 *     <li>{@link Builder#setVideoDuration(YouTubeVideoDuration)}</li>
 * </ul>
 */
public final class YouTubeSearchQuery {
    /**
     * The search base for YouTube api v3.
     */
    private static final String searchHeader = "https://www.googleapis.com/youtube/v3/search";

    /**
     * The snippet string used for the part parameter.
     * This MUST be used according to the Google developer documentation.
     */
    private static final String SNIPPET = "snippet";

    /**
     * The default query string.
     */
    private static final String DEFAULT_QUERY = "Solitaires by Future";

    /**
     * The url constructed from the provided builder's parameters.
     */
    private final String url;

    /**
     * Constructs a new search query based on the provided builder.
     *
     * @param builder the builder with set parameters for the search query
     */
    public YouTubeSearchQuery(Builder builder) {
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
                + builder.getKey()
                + YouTubeSearchListApiParameter.ORDER.getUrlParameter()
                + builder.getSearchOrder()
                + YouTubeSearchListApiParameter.VIDEO_DEFINITION.getUrlParameter()
                + builder.getVideoDefinition()
                + YouTubeSearchListApiParameter.VIDEO_DURATION.getUrlParameter()
                + builder.getVideoDuration()
                + YouTubeSearchListApiParameter.MAX_RESULTS.getUrlParameter()
                + builder.getMaxResults();
    }

    /**
     * Returns the url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the serialized results of querying the url represented by this search query if possible.
     * Empty optional else.
     *
     * @return the serialized results of querying the url represented by this search query if possible.
     * * Empty optional else
     */
    public Optional<YouTubeSearchResultPage> getResults() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(url).openStream()))) {
            return Optional.of(SerializationUtil.fromJson(reader, YouTubeSearchResultPage.class));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }

    /**
     * Builds and returns a new {@link Builder} with the default query.
     * The primary use of this is for ease of key validation.
     *
     * @return a new, default builder
     */
    public static Builder buildDefaultBuilder() {
        return new Builder(DEFAULT_QUERY);
    }

    /**
     * A builder for a {@link YouTubeSearchQuery}.
     */
    public static final class Builder {
        /**
         * The range a max results value must fall within.
         */
        private static final Range<Integer> maxResultsRange = Range.closed(0, 50);

        /**
         * The default maximum results returned by a YouTube list search query.
         */
        private static final int DEFAULT_MAX_RESULTS = 5;

        /**
         * The query string.
         */
        private final String query;

        /**
         * The API key.
         */
        private String key;

        /**
         * The search type.
         */
        private YouTubeSearchType type = YouTubeSearchType.VIDEO;

        /**
         * The safe search type.
         */
        private YouTubeSafeSearch safeSearch = YouTubeSafeSearch.NONE;

        /**
         * The search order.
         */
        private YouTubeSearchOrder searchOrder = YouTubeSearchOrder.TITLE;

        /**
         * The video definition.
         */
        private YouTubeVideoDefinition videoDefinition = YouTubeVideoDefinition.ANY;

        /**
         * The video duration.
         */
        private YouTubeVideoDuration videoDuration = YouTubeVideoDuration.ANY;

        /**
         * The maximum results that may be returned by this query.
         */
        private int maxResults = DEFAULT_MAX_RESULTS;

        /**
         * Constructs a new builder for a YouTube search query.
         *
         * @param query the query string such as "Solitaires by Future"
         */
        public Builder(String query) {
            Preconditions.checkNotNull(query);
            Preconditions.checkArgument(!query.trim().isEmpty());

            this.query = query.trim().replaceAll(CyderRegexPatterns.whiteSpaceRegex, "+");
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
         * Sets the api key.
         *
         * @param key the api key
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setKey(String key) {
            Preconditions.checkNotNull(key);
            Preconditions.checkArgument(!key.isEmpty());

            this.key = key;
            return this;
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
         * Returns the maximum number of results.
         *
         * @return the maximum number of results
         */
        public int getMaxResults() {
            return maxResults;
        }

        /**
         * Sets the maximum number of results.
         *
         * @param maxResults the maximum number of results
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setMaxResults(int maxResults) {
            Preconditions.checkArgument(maxResultsRange.contains(maxResults));

            this.maxResults = maxResults;
            return this;
        }

        /**
         * Returns the search order.
         *
         * @return the search order
         */
        public YouTubeSearchOrder getSearchOrder() {
            return searchOrder;
        }

        /**
         * Sets the search order.
         *
         * @param searchOrder the search order
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setSearchOrder(YouTubeSearchOrder searchOrder) {
            Preconditions.checkNotNull(searchOrder);

            this.searchOrder = searchOrder;
            return this;
        }

        /**
         * Returns the video definition.
         *
         * @return the video definition
         */
        public YouTubeVideoDefinition getVideoDefinition() {
            return videoDefinition;
        }

        /**
         * Sets the video definition.
         *
         * @param videoDefinition the video definition
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setVideoDefinition(YouTubeVideoDefinition videoDefinition) {
            Preconditions.checkNotNull(videoDefinition);

            this.videoDefinition = videoDefinition;
            return this;
        }

        /**
         * Returns the video duration.
         *
         * @return the video duration
         */
        public YouTubeVideoDuration getVideoDuration() {
            return videoDuration;
        }

        /**
         * Sets the video duration.
         *
         * @param videoDuration the video duration
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setVideoDuration(YouTubeVideoDuration videoDuration) {
            Preconditions.checkNotNull(videoDuration);

            this.videoDuration = videoDuration;
            return this;
        }

        /**
         * Builds and returns a new search query based on the set parameters.
         *
         * @return a new search query based on the set parameters
         */
        public YouTubeSearchQuery build() {
            return new YouTubeSearchQuery(this);
        }
    }
}
