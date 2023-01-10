package cyder.youtube.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * The page information object of a {@link YouTubeSearchResultPage}.
 */
public class PageInfo {
    private long totalResults;
    private int resultsPerPage;

    /**
     * Constructs a new PageInfo object.
     */
    public PageInfo() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }
}
