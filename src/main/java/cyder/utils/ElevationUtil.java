package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.elevation.ElevationData;
import cyder.strings.CyderStrings;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

/**
 * A utility class for elevation queries.
 */
public final class ElevationUtil {
    /**
     * Suppress default constructor.
     */
    private ElevationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The base string for queries.
     */
    private static final String BASE = "https://nationalmap.gov/epqs/pqs.php?";

    /**
     * The units tag for a url.
     */
    private static final String UNITS_TAG = "&units=";

    /**
     * Returns the elevation of the provided point.
     *
     * @param latLonPoint the lat/lon point
     * @param unit        whether the elevation should be in feet or meters
     * @return the elevation in meters or feet depending on the requested unit
     * This return value should be checked to ensure it is not {@link Double#MIN_VALUE}
     * which indicates and invalid return
     */
    public static Optional<Double> getElevation(Point latLonPoint, LengthUnit unit) {
        Preconditions.checkNotNull(latLonPoint);
        Preconditions.checkNotNull(unit);

        double lat = latLonPoint.getY();
        double lon = latLonPoint.getX();

        String queryString = BASE + "output=json&x=" + lon + "&y=" + lat + UNITS_TAG + unit.getName();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(queryString).openStream()))) {
            ElevationData elevationData = SerializationUtil.fromJson(reader, ElevationData.class);
            return Optional.of(Double.parseDouble(elevationData.uepqs.elevationQuery.elevation));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }

    /**
     * The standard length units.
     */
    public enum LengthUnit {
        /**
         * The SI unit for length.
         */
        METERS("METERS"),
        /*
         * The English unit for length.
         */
        FEET("FEET");

        /**
         * The name of the length unit
         */
        private final String name;

        /**
         * Constructs a new length UnitEnum
         *
         * @param name the name of the length unit
         */
        LengthUnit(String name) {
            this.name = name;
        }

        /**
         * Returns the name of this length unit.
         *
         * @return the name of this length unit
         */
        public String getName() {
            return name;
        }
    }
}
