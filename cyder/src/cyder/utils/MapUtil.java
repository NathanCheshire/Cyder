package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.props.PropLoader;

import javax.swing.*;
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
     * The height of the mapbox watermark.
     */
    private static final int MAP_BOX_WATERMARK_HEIGHT = 25;

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

    // todo extract params
    /**
     * The map quest api footer.
     */
    private static final String MAP_QUEST_FOOTER = "%7Cmarker-sm-50318A-1&scalebar=true&zoom=15&rand=286585877";

    /**
     * The map quest api key.
     */
    private static final String MAP_QUEST_API_KEY = "map_quest_api_key";

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
        return getMapView(lat, lon, width, height, true);
    }

    /**
     * Returns an ImageIcon with the provided dimensions of an
     * aerial map view centered at the provided lat, lon.
     *
     * @param lat             the center lat point
     * @param lon             the center lon point
     * @param width           the width of the resulting image
     * @param height          the height of the resulting image
     * @param filterWatermark whether the mapbox logo and watermark should be filtered out
     * @return the requested image
     * @throws UnknownHostException if the resource cannot be located
     */
    public static ImageIcon getMapView(double lat, double lon,
                                       int width, int height,
                                       boolean filterWatermark) throws UnknownHostException {
        // todo accept builder

        int requestHeight = height;
        if (filterWatermark) requestHeight += MAP_BOX_WATERMARK_HEIGHT;

        String string = MAP_QUEST_HEADER + PropLoader.getString(MAP_QUEST_API_KEY)
                + MAP_TYPE_PARAMETER
                + MAP_SIZE_PARAMETER + width + CyderStrings.comma + requestHeight
                + MAP_LOCATIONS_PARAMETER + lat + CyderStrings.comma + lon
                + "&type=hyb" + "&scalebar=true|top";
        // todo show scalebar on weather widget, add map widget eventually?

        try {
            ImageIcon ret = ImageUtil.toImageIcon(ImageUtil.read(string));

            if (filterWatermark) {
                return ImageUtil.cropImage(ret, 0, 0, width, height);
            } else {
                return ret;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new UnknownHostException("Could not get resource for provided lat: " + lat + ", lon: " + lon);
    }

    /**
     * The scalebar locations for the map scale.
     */
    public enum ScaleBarLocation {
        /**
         * The scalebar will be located at the top of the map.
         */
        TOP,

        /**
         * The scalebar will be located at the bottom of the map.
         */
        BOTTOM
    }

    // todo use a request for when asking where they are show an image with a dot?
    public enum MapType {
        /**
         * The standard map type.
         */
        MAP,

        /**
         * A hybrid map with satellite imaging.
         */
        HYB,

        /**
         * A purely satellite map.
         */
        SAT,

        /**
         * A light mode digital map.
         */
        LIGHT,

        /**
         * A dark mode digital map.
         */
        DARK
    }

    /**
     * A builder for a MapQuestApi request.
     */
    public static final class Builder {
        /**
         * The range a latitude value must fall into.
         */
        private static final Range<Double> latitudeRange = Range.closed(-90.0, 90.0);

        /**
         * The range a longitude value must fall into.
         */
        private static final Range<Double> longitudeRange = Range.closed(-180.0, 180.0);

        /**
         * The range the width must fall into.
         */
        private static final Range<Integer> widthRange = Range.closed(170, 1920);

        /**
         * The range the height must fall into.
         */
        private static final Range<Integer> heightRange = Range.closed(30, 1920);

        /**
         * The range a zoom value must fall into.
         */
        private static final Range<Integer> zoomRange = Range.closed(0, 20);

        private final double lat;
        private final double lon;
        private final int width;
        private final int height;

        private boolean filterWaterMark = true;
        private boolean scaleBar = true;
        private ScaleBarLocation scaleBarLocation = ScaleBarLocation.TOP;
        private MapType mapType = MapType.MAP;
        private int zoomLevel = 12;

        /**
         * Constructs a new MapQuestApi request builder.
         *
         * @param lat    the latitude to center the map on
         * @param lon    the longitude to center the map on
         * @param width  the width of the final image
         * @param height the height of the final image
         */
        public Builder(double lat, double lon, int width, int height) {
            Preconditions.checkArgument(latitudeRange.contains(lat));
            Preconditions.checkArgument(longitudeRange.contains(lon));
            Preconditions.checkArgument(widthRange.contains(width));
            Preconditions.checkArgument(heightRange.contains(height));

            this.lat = lat;
            this.lon = lon;
            this.width = width;
            this.height = height;
        }

        /**
         * Sets whether the MapQuestApi watermark should be filtered out of the final image.
         *
         * @param filterWaterMark whether the MapQuestApi watermark should be filtered out of the final image
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setFilterWaterMark(boolean filterWaterMark) {
            this.filterWaterMark = filterWaterMark;
            return this;
        }

        /**
         * Sets whether a scale bar should be displayed on the final image.
         *
         * @param scaleBar whether a scale bar should be displayed on the final image
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setScaleBar(boolean scaleBar) {
            this.scaleBar = scaleBar;
            return this;
        }

        /**
         * Sets the scale bar location on the final image.
         *
         * @param scaleBarLocation the scale bar location on the final image
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setScaleBarLocation(ScaleBarLocation scaleBarLocation) {
            Preconditions.checkNotNull(scaleBarLocation);
            this.scaleBarLocation = scaleBarLocation;
            return this;
        }

        /**
         * Sets the map type of the final image.
         *
         * @param mapType the map type of the final image
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setMapType(MapType mapType) {
            Preconditions.checkNotNull(mapType);
            this.mapType = mapType;
            return this;
        }

        /**
         * Sets the zoom level of the final map image.
         *
         * @param zoomLevel the zoom level of the final map image
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setZoomLevel(int zoomLevel) {
            Preconditions.checkArgument(zoomRange.contains(zoomLevel));
            this.zoomLevel = zoomLevel;
            return this;
        }
    }
}
