package cyder.github.parsers;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * A json serialization class for a GitHub issue reaction.
 */
@SuppressWarnings("unused")
public class Reaction {
    /**
     * The url to get the list of reactions for the issue referenced by this reaction.
     */
    public String url;

    /**
     * The total count of reactions for the issue referenced by this reaction.
     */
    @SerializedName("total_count")
    public int totalCount;

    /**
     * The number of plus one reactions.
     */
    @SerializedName("+1")
    public int plusOne;

    /**
     * The number of minus one reactions.
     */
    @SerializedName("-1")
    public int minusOne;

    /**
     * The number of laugh reactions.
     */
    public int laugh;

    /**
     * The number of hooray reactions.
     */
    public int hooray;

    /**
     * The number of confused reactions.
     */
    public int confused;

    /**
     * The number of heart reactions.
     */
    public int heart;

    /**
     * The number of rocket reactions.
     */
    public int rocket;

    /**
     * The number of eyes reactions.
     */
    public int eyes;

    /**
     * Creates a new Reaction for a GitHub issue.
     */
    public Reaction() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the url to get the list of reactions for the issue referenced by this reaction.
     *
     * @return the url to get the list of reactions for the issue referenced by this reaction
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url to get the list of reactions for the issue referenced by this reaction.
     *
     * @param url the url to get the list of reactions for the issue referenced by this reaction
     */
    public void setUrl(String url) {
        Preconditions.checkNotNull(url);

        this.url = url;
    }

    /**
     * Returns the total count of reactions for the issue referenced by this reaction.
     *
     * @return the total count of reactions for the issue referenced by this reaction
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Sets the total count of reactions for the issue referenced by this reaction.
     *
     * @param totalCount the total count of reactions for the issue referenced by this reaction
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * Returns the number of plus one reactions.
     *
     * @return the number of plus one reactions
     */
    public int getPlusOne() {
        return plusOne;
    }

    /**
     * Sets the number of plus one reactions.
     *
     * @param plusOne the the number of plus one reactions
     */
    public void setPlusOne(int plusOne) {
        this.plusOne = plusOne;
    }

    /**
     * Returns the number of minus one reactions.
     *
     * @return the number of minus one reactions
     */
    public int getMinusOne() {
        return minusOne;
    }

    /**
     * Sets the number of minus one reactions.
     *
     * @param minusOne the number of minus one reactions
     */
    public void setMinusOne(int minusOne) {
        this.minusOne = minusOne;
    }

    /**
     * Returns the number of laugh reactions.
     *
     * @return the number of laugh reactions
     */
    public int getLaugh() {
        return laugh;
    }

    /**
     * Sets the number of laugh reactions.
     *
     * @param laugh the number of laugh reactions
     */
    public void setLaugh(int laugh) {
        this.laugh = laugh;
    }

    /**
     * Returns the number of hooray reactions.
     *
     * @return the number of hooray reactions
     */
    public int getHooray() {
        return hooray;
    }

    /**
     * Sets the number of hooray reactions.
     *
     * @param hooray the number of hooray reactions
     */
    public void setHooray(int hooray) {
        this.hooray = hooray;
    }

    /**
     * Returns the number of confused reactions.
     *
     * @return the number of confused reactions
     */
    public int getConfused() {
        return confused;
    }

    /**
     * Sets the number of confused reactions.
     *
     * @param confused the number of confused reactions
     */
    public void setConfused(int confused) {
        this.confused = confused;
    }

    /**
     * Returns the number of heart reactions.
     *
     * @return the number of heart reactions
     */
    public int getHeart() {
        return heart;
    }

    /**
     * Sets the number of heart reactions.
     *
     * @param heart the number of heart reactions.
     */
    public void setHeart(int heart) {
        this.heart = heart;
    }

    /**
     * Returns the number of rocket reactions.
     *
     * @return the number of rocket reactions
     */
    public int getRocket() {
        return rocket;
    }

    /**
     * Sets the number of rocket reactions.
     *
     * @param rocket the number of rocket reactions
     */
    public void setRocket(int rocket) {
        this.rocket = rocket;
    }

    /**
     * Sets the number of eye reactions.
     *
     * @return the number of eye reactions
     */
    public int getEyes() {
        return eyes;
    }

    /**
     * Sets the number of eye reactions.
     *
     * @param eyes the number of eye reactions
     */
    public void setEyes(int eyes) {
        this.eyes = eyes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Reaction)) {
            return false;
        }

        Reaction other = (Reaction) o;
        return other.url.equals(url)
                && other.totalCount == totalCount
                && other.plusOne == plusOne
                && other.minusOne == minusOne
                && other.laugh == laugh
                && other.hooray == hooray
                && other.confused == confused
                && other.heart == heart
                && other.rocket == rocket
                && other.eyes == eyes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = url.hashCode();
        ret = 31 * ret + Integer.hashCode(totalCount);
        ret = 31 * ret + Integer.hashCode(plusOne);
        ret = 31 * ret + Integer.hashCode(minusOne);
        ret = 31 * ret + Integer.hashCode(laugh);
        ret = 31 * ret + Integer.hashCode(hooray);
        ret = 31 * ret + Integer.hashCode(confused);
        ret = 31 * ret + Integer.hashCode(heart);
        ret = 31 * ret + Integer.hashCode(rocket);
        ret = 31 * ret + Integer.hashCode(eyes);

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Reaction{"
                + "url=\"" + url + "\""
                + ", total_count=" + totalCount
                + ", plusOne=" + plusOne
                + ", minusOne=" + minusOne
                + ", laugh=" + laugh
                + ", hooray=" + hooray
                + ", confused=" + confused
                + ", heart=" + heart
                + ", rocket=" + rocket
                + ", eyes=" + eyes
                + "}";
    }
}
