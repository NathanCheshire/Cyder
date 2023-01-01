package cyder.github.parsers;

import com.google.gson.annotations.SerializedName;

/**
 * A json serialization class for a GitHub reaction.
 */
@SuppressWarnings("unused")
public class Reaction {
    public String url;
    public int total_count;

    @SerializedName("+1")
    public int plusOne;

    @SerializedName("-1")
    public int minusOne;

    public int laugh;
    public int hooray;
    public int confused;
    public int heart;
    public int rocket;
    public int eyes;

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
                && other.total_count == total_count
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
        ret = 31 * ret + Integer.hashCode(total_count);
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
                + ", total_count=" + total_count
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
