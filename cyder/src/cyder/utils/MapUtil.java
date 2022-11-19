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
     * The map quest api url header.
     */
    private static final String mapQuestHeader = "http://www.mapquestapi.com/staticmap/v5/map?";

    /**
     * The map quest api key.
     */
    private static final String MAP_QUEST_API_KEY = "map_quest_api_key";

    /**
     * The height of the mapbox watermark.
     */
    private static final int MAP_BOX_WATERMARK_HEIGHT = 25;

    /**
     * Url parameters for a MapBox API request.
     */
    private enum mapboxUrlParameter {
        /**
         * The API key.
         */
        KEY,

        /**
         * The map type.
         */
        TYPE,

        /**
         * The map size, comma separated.
         */
        SIZE,

        /**
         * Whether the scalebar should be displayed and if so, a possible location
         * with a pipe separating the true and the scalebar location.
         */
        SCALEBAR,

        /**
         * The map locations, comma separated.
         */
        LOCATIONS,
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

    /**
     * The possible map types accepted by the MapQuest API.
     */
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
        int requestHeight = height;
        if (filterWatermark) requestHeight += MAP_BOX_WATERMARK_HEIGHT;

        String string = mapQuestHeader + PropLoader.getString(MAP_QUEST_API_KEY)
                + TYPE_PARAMETER
                + SIZE_PARAMETER + width + CyderStrings.comma + requestHeight
                + LOCATIONS_PARAMETER + lat + CyderStrings.comma + lon
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
     * Returns an {@link ImageIcon} of the geographical location requested by the provided builder.
     *
     * @param builder thd builder
     * @return the geographical top-down image
     */
    public static ImageIcon getMapView(Builder builder) {
        Preconditions.checkNotNull(builder);


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

        /**
         * The latitude of the map image.
         */
        private final double lat;

        /**
         * The longitude of the map image.
         */
        private final double lon;

        /**
         * The width of the map image.
         */
        private final int width;

        /**
         * The height of the map image.
         */
        private final int height;

        /**
         * Whether the watermark should be filtered out of the image.
         */
        private boolean filterWaterMark = true;

        /**
         * Whether the scalebar should be shown on the map.
         */
        private boolean scaleBar = true;

        /**
         * The location of the scalebar if it should be displayed.
         */
        private ScaleBarLocation scaleBarLocation = ScaleBarLocation.TOP;

        /**
         * The type of map the map image will be.
         */
        private MapType mapType = MapType.MAP;

        /**
         * The zoom level of the map image.
         */
        private int zoomLevel = 15;

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

        /**
         * Returns the latitude of the map.
         *
         * @return the latitude of the map
         */
        public double getLat() {
            return lat;
        }

        /**
         * Returns the longitude of the map.
         *
         * @return the longitude of the map
         */
        public double getLon() {
            return lon;
        }

        /**
         * Returns the width of the map.
         *
         * @return the width of the map
         */
        public int getWidth() {
            return width;
        }

        /**
         * Returns the height of the map.
         *
         * @return the height of the map
         */
        public int getHeight() {
            return height;
        }

        /**
         * Returns whether the watermark should be filtered out.
         *
         * @return whether the watermark should be filtered out
         */
        public boolean isFilterWaterMark() {
            return filterWaterMark;
        }

        /**
         * Returns whether the scalebar should be displayed.
         *
         * @return whether the scalebar should be displayed
         */
        public boolean isScaleBar() {
            return scaleBar;
        }

        /**
         * Returns the scalebar location.
         *
         * @return the scalebar location
         */
        public ScaleBarLocation getScaleBarLocation() {
            return scaleBarLocation;
        }

        /**
         * Returns the map type.
         *
         * @return the map type
         */
        public MapType getMapType() {
            return mapType;
        }

        /**
         * Returns the zoom level.
         *
         * @return the zoom level
         */
        public int getZoomLevel() {
            return zoomLevel;
        }
    }
}
