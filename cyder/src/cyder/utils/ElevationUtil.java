package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * A utility class for elevation queries.
 */
public class ElevationUtil {
    /**
     * Suppress default constructor.
     */
    private ElevationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The gson object used to serialize data from <a href="https://nationalmap.gov/">NationalMap</a>.
     */
    private static final Gson gson = new Gson();

    /**
     * The base string for queries.
     */
    private static final String base = "https://nationalmap.gov/epqs/pqs.php?";

    /**
     * Returns the elevation of the provided point.
     *
     * @param latLonPoint the lat/lon point
     * @param unit        whether the elevation should be in feet or meters
     * @return the elevation in meters or feet
     */
    @SuppressWarnings("UnnecessaryDefault")
    public static double getElevation(Point latLonPoint, LengthUnit unit) {
        Preconditions.checkNotNull(latLonPoint);

        double lat = latLonPoint.getY();
        double lon = latLonPoint.getX();

        String queryString = switch (unit) {
            case FEET -> base + "output=json&x=" + lon + "&y=" + lat + "&units=Feet";
            case METERS -> base + "output=json&x=" + lon + "&y=" + lat + "&units=METERS";
            default -> throw new IllegalArgumentException("Invalid requested distance unit: " + unit);
        };

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(queryString).openStream()))) {
            ElevationData elevationData = gson.fromJson(reader, ElevationData.class);
            return Double.parseDouble(elevationData.uepqs.elevationQuery.elevation);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return -Double.MAX_VALUE;
    }

    /**
     * The standard length units.
     */
    public enum LengthUnit {
        /**
         * The SI unit for length.
         */
        METERS,
        /*
         * The English unit for length.
         */
        FEET
    }

    /**
     * A class to serialize the elevation data from <a href="https://nationalmap.gov/">NationalMap</a>.
     */
    private static class ElevationData {
        @SerializedName("USGS_Elevation_Point_Query_Service")
        public UEPQS uepqs;

        public static class UEPQS {
            @SerializedName("Elevation_Query")
            public ElevationQuery elevationQuery;

            @SuppressWarnings("unused")
            public static class ElevationQuery {
                public double x;
                public double y;

                @SerializedName("Data_source")
                public String dataSource;

                @SerializedName("Elevation")
                public String elevation;

                @SerializedName("Units")
                public String units;
            }
        }
    }
}
