package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.internal.ExceptionHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Utilities for MapBox, MapQuest, OpenRouteService, etc.
 */
public final class MapUtil {
    /**
     * Suppress default constructor.
     */
    private MapUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The map quest api url header.
     */
    private static final String MAP_QUEST_HEADER = "http://www.mapquestapi.com/staticmap/v5/map?key=";

    /**
     * The map quest map type parameter.
     */
    private static final String MAP_TYPE_PARAMETER = "&type=map";

    /**
     * The map quest size parameter.
     */
    private static final String MAP_SIZE_PARAMETER = "&size=";

    /**
     * The map quest location parameter.
     */
    private static final String MAP_LOCATIONS_PARAMETER = "&locations=";

    /**
     * The map quest api footer.
     */
    private static final String MAP_QUEST_FOOTER = "%7Cmarker-sm-50318A-1&scalebar=true&zoom=15&rand=286585877";

    /**
     * The range a lat value must fall into.
     */
    private static final Range<Double> LAT_RANGE = Range.closed(-90.0, 90.0);

    /**
     * The range a lon value must fall into.
     */
    private static final Range<Double> LON_RANGE = Range.closed(-180.0, 180.0);

    /**
     * Returns an ImageIcon with the provided dimensions of an
     * aerial map view centered at the provided lat, lon.
     *
     * @param lat    the center lat point
     * @param lon    the center lon point
     * @param width  the width of the resulting image
     * @param height the height of the resulting image
     * @return the requested image
     * @throws UnknownHostException if the resource cannot be located
     */
    public static ImageIcon getMapView(double lat, double lon, int width, int height) throws UnknownHostException {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkArgument(LAT_RANGE.contains(lat));
        Preconditions.checkArgument(LON_RANGE.contains(lon));

        String string = MAP_QUEST_HEADER + PropLoader.getString("map_quest_api_key")
                + MAP_TYPE_PARAMETER
                + MAP_SIZE_PARAMETER + width + "," + height
                + MAP_LOCATIONS_PARAMETER + lat + "," + lon
                + MAP_QUEST_FOOTER;

        try {
            return ImageUtil.toImageIcon(ImageIO.read(new URL(string)));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new UnknownHostException("Could not get resource for provided lat = " + lat + ", lon = " + lon);
    }
}
