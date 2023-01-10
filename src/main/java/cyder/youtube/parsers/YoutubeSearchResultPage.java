package cyder.youtube.parsers;

import java.util.LinkedList;

/**
 * The master serialization class for a youtube api 3 search query returned result.
 */
public class YoutubeSearchResultPage {
    private String kind;
    private String etag;
    private String nextPageToken;
    private String regionCode;
    private PageInfo pageInfo;
    private LinkedList<YoutubeVideo> items;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public LinkedList<YoutubeVideo> getItems() {
        return items;
    }

    public void setItems(LinkedList<YoutubeVideo> items) {
        this.items = items;
    }
}
