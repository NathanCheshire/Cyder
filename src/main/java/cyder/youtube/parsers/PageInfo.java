package cyder.youtube.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * The page information object of a {@link YouTubeSearchResultPage}.
 */
public class PageInfo {
    /**
     * The total results returned via this query. Note this many be into the thousands.
     */
    private long totalResults;

    /**
     * The results displayed per page of this query.
     */
    private int resultsPerPage;

    /**
     * Constructs a new PageInfo object.
     */
    public PageInfo() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the total results of this query.
     *
     * @return the total results of this query
     */
    public long getTotalResults() {
        return totalResults;
    }

    /**
     * Sets the total results of this query.
     *
     * @param totalResults the total results of this query
     */
    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    /**
     * Returns the results per page.
     *
     * @return the results per page
     */
    public int getResultsPerPage() {
        return resultsPerPage;
    }

    /**
     * Sets the results per page.
     *
     * @param resultsPerPage the results per page
     */
    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof PageInfo)) {
            return false;
        }

        PageInfo other = (PageInfo) o;
        return other.totalResults == totalResults
                && other.resultsPerPage == resultsPerPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Long.hashCode(totalResults);
        ret = 31 * ret + Integer.hashCode(resultsPerPage);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PageInfo{"
                + "totalResults=" + totalResults
                + ", resultsPerPage=" + resultsPerPage
                + "}";
    }
}
