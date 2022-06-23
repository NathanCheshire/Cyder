package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.internal.ExceptionHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Utilities with MapBox, MapQuest, OpenRouteService, etc.
 */
public class MapUtil {
    /**
     * Suppress default constructor.
     */
    private MapUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

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

        String string = "http://www.mapquestapi.com/staticmap/v5/map?key="
                + PropLoader.getString("map_quest_api_key") + "&type=map&size="
                + width + "," + height
                + "&locations=" + lat + "," + lon + "%7Cmarker-sm-50318A-1"
                + "&scalebar=true&zoom=15&rand=286585877";

        try {
            return ImageUtil.toImageIcon(ImageIO.read(new URL(string)));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new UnknownHostException("Could not get resource for provided lat = " + lat + ", lon = " + lon);
    }
}
