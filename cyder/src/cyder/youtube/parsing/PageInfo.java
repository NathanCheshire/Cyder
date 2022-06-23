package cyder.youtube.parsing;

/**
 * Information about the YoutubeSearchResultPage.
 */
@SuppressWarnings("unused")
public class PageInfo {
    private long totalResults;
    private int resultsPerPage;

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
