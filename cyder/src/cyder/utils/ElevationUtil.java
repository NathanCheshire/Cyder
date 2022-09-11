package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CheckReturnValue;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.remote.elevation.ElevationData;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

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
     * Returns the elevation of the provided point.
     *
     * @param latLonPoint the lat/lon point
     * @param unit        whether the elevation should be in feet or meters
     * @return the elevation in meters or feet depending on the requested unit
     * This return value should be checked to ensure it is not {@link Double#MIN_VALUE}
     * which indicates and invalid return
     */
    @CheckReturnValue
    @SuppressWarnings("UnnecessaryDefault")
    public static double getElevation(Point latLonPoint, LengthUnit unit) {
        Preconditions.checkNotNull(latLonPoint);

        double lat = latLonPoint.getY();
        double lon = latLonPoint.getX();

        String queryString = switch (unit) {
            case FEET -> BASE + "output=json&x=" + lon + "&y=" + lat + "&units=" + LengthUnit.FEET.getName();
            case METERS -> BASE + "output=json&x=" + lon + "&y=" + lat + "&units=" + LengthUnit.METERS.getName();
            default -> throw new IllegalArgumentException("Invalid requested distance unit: " + unit);
        };

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(queryString).openStream()))) {
            ElevationData elevationData = SerializationUtil.serialize(reader, ElevationData.class);
            return Double.parseDouble(elevationData.uepqs.elevationQuery.elevation);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Double.MIN_VALUE;
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
