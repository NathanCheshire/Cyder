package cyder.parsers.remote.github;

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
}
