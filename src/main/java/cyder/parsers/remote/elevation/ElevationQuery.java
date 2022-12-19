package main.java.cyder.parsers.remote.elevation;

import com.google.gson.annotations.SerializedName;

/**
 * A json serialization class for an elevation query.
 */
public class ElevationQuery {
    public double x;
    public double y;

    @SuppressWarnings("unused")
    @SerializedName("Data_source")
    public String dataSource;

    @SerializedName("Elevation")
    public String elevation;

    @SerializedName("Units")
    public String units;
}
