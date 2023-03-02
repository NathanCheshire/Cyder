package cyder.youtube.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.ArrayList;

/**
 * The master containing object for containing serialized data from a YouTube search list query.
 * See {@link cyder.youtube.search.YouTubeSearchQuery}.
 */
public class YouTubeSearchResultPage {
    /**
     * The kind. Typically "youtube#searchListResponse".
     */
    private String kind;

    /**
     * The etag.
     */
    private String etag;

    /**
     * The token for the next search results page.
     */
    private String nextPageToken;

    /**
     * The region code of the search results.
     */
    private String regionCode;

    /**
     * The page info object.
     */
    private PageInfo pageInfo;

    /**
     * The list of YouTube videos matching the search query.
     */
    private ArrayList<YouTubeVideo> items;

    /**
     * Constructs a new YouTube search results page.
     */
    public YouTubeSearchResultPage() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the kind. Typically "youtube#searchListResponse"
     *
     * @return the kind. Typically "youtube#searchListResponse"
     */
    public String getKind() {
        return kind;
    }

    /**
     * Sets the kind. Typically "youtube#searchListResponse"
     *
     * @param kind the kind. Typically "youtube#searchListResponse"
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
     * Returns the token for the next search results page.
     *
     * @return the token for the next search results page
     */
    public String getNextPageToken() {
        return nextPageToken;
    }

    /**
     * Sets the token for the next search results page.
     *
     * @param nextPageToken the token for the next search results page
     */
    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    /**
     * Returns the region code of the search results.
     *
     * @return the region code of the search results
     */
    public String getRegionCode() {
        return regionCode;
    }

    /**
     * Sets the region code of the search results.
     *
     * @param regionCode the region code of the search results
     */
    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    /**
     * Returns the page info object.
     *
     * @return the page info object
     */
    public PageInfo getPageInfo() {
        return pageInfo;
    }

    /**
     * Sets the page info object.
     *
     * @param pageInfo the page info object
     */
    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    /**
     * Returns the list of YouTube videos matching the search query.
     *
     * @return the list of YouTube videos matching the search query
     */
    public ArrayList<YouTubeVideo> getItems() {
        return items;
    }

    /**
     * Sets the list of YouTube videos matching the search query.
     *
     * @param items the list of YouTube videos matching the search query
     */
    public void setItems(ArrayList<YouTubeVideo> items) {
        this.items = items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof YouTubeSearchResultPage)) {
            return false;
        }

        YouTubeSearchResultPage other = (YouTubeSearchResultPage) o;
        return other.kind.equals(kind)
                && other.etag.equals(etag)
                && other.nextPageToken.equals(nextPageToken)
                && other.regionCode.equals(regionCode)
                && other.pageInfo.equals(pageInfo)
                && other.items.equals(items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = kind.hashCode();
        ret = 31 * ret + etag.hashCode();
        ret = 31 * ret + nextPageToken.hashCode();
        ret = 31 * ret + regionCode.hashCode();
        ret = 31 * ret + pageInfo.hashCode();
        ret = 31 * ret + items.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "YouTubeSearchResultPage{"
                + "kind=\"" + kind + "\""
                + ", etag=\"" + etag + "\""
                + ", nextPageToken=\"" + nextPageToken + "\""
                + ", regionCode=\"" + regionCode + "\""
                + ", pageInfo=" + pageInfo
                + ", items=" + items
                + "}";
    }
}
