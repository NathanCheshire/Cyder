package main.java.cyder.parsers.remote.elevation;

import com.google.gson.annotations.SerializedName;

/**
 * A json serialization class for an elevation data query.
 */
public class ElevationData {
    @SerializedName("USGS_Elevation_Point_Query_Service")
    public Uepqs uepqs;
}
