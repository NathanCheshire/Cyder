package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;

import javax.swing.*;
import java.net.UnknownHostException;

/**
 * Utilities for MapBox, MapQuest, OpenRouteService, etc.
 */
public final class MapUtil {
    /**
     * The map quest api url header.
     */
    private static final String mapQuestHeader = "http://www.mapquestapi.com/staticmap/v5/map?";

    /**
     * The height of the mapbox watermark.
     */
    private static final int MAP_BOX_WATERMARK_HEIGHT = 20;

    /**
     * The pipe character for requesting scalebar location.
     */
    private static final String PIPE = "|";

    /**
     * Suppress default constructor.
     */
    private MapUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Url parameters for a MapBox API request.
     */
    private enum MapBoxUrlParameter {
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

        /**
         * The map center.
         */
        CENTER,

        /**
         * The zoom level of the returned map image.
         */
        ZOOM;

        public String constructAsFirstParameter() {
            return this.name().toLowerCase() + "=";
        }

        public String construct() {
            return "&" + constructAsFirstParameter();
        }
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
         * A pure satellite map.
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
     * Returns an {@link ImageIcon} of the geographical location requested by the provided builder.
     *
     * @param builder thd builder
     * @return the geographical top-down image with the requested parameters fulfilled
     */
    public static ImageIcon getMapView(Builder builder) throws UnknownHostException {
        Preconditions.checkState(Props.mapQuestApiKey.valuePresent());
        Preconditions.checkNotNull(builder);

        StringBuilder requestUrlBuilder = new StringBuilder(mapQuestHeader);
        requestUrlBuilder.append(MapBoxUrlParameter.KEY.constructAsFirstParameter());
        requestUrlBuilder.append(builder.getKey());

        if (builder.getLat() != Integer.MIN_VALUE && builder.getLon() != Integer.MIN_VALUE) {
            requestUrlBuilder.append(MapBoxUrlParameter.CENTER.construct());
            requestUrlBuilder.append(builder.getLat());
            requestUrlBuilder.append(CyderStrings.comma);
            requestUrlBuilder.append(builder.getLon());
        } else {
            String locationString = builder.getLocationString();
            if (StringUtil.isNullOrEmpty(locationString)) {
                throw new IllegalStateException("Must provide latitude/longitude or location");
            }

            requestUrlBuilder.append(MapBoxUrlParameter.CENTER.construct());
            requestUrlBuilder.append(locationString
                    .replaceAll(CyderRegexPatterns.whiteSpaceRegex, NetworkUtil.URL_SPACE));
        }

        requestUrlBuilder.append(MapBoxUrlParameter.TYPE.construct());
        requestUrlBuilder.append(builder.getMapType().name().toLowerCase());

        boolean showScalebar = builder.isScaleBar();
        requestUrlBuilder.append(MapBoxUrlParameter.SCALEBAR.construct());
        requestUrlBuilder.append(showScalebar);
        if (showScalebar) {
            requestUrlBuilder.append(PIPE);
            requestUrlBuilder.append(builder.getScaleBarLocation().name().toLowerCase());
        }

        requestUrlBuilder.append(MapBoxUrlParameter.ZOOM.construct());
        requestUrlBuilder.append(builder.getZoomLevel());

        int height = builder.getHeight();
        if (builder.isFilterWaterMark()) {
            height += MAP_BOX_WATERMARK_HEIGHT;
        }

        requestUrlBuilder.append(MapBoxUrlParameter.SIZE.construct());
        requestUrlBuilder.append(builder.getWidth());
        requestUrlBuilder.append(CyderStrings.comma);
        requestUrlBuilder.append(height);

        ImageIcon returnedInitialImage = null;
        try {
            returnedInitialImage = ImageUtil.toImageIcon(ImageUtil.read(requestUrlBuilder.toString()));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (returnedInitialImage == null) {
            throw new UnknownHostException("Could not get resource for provided builder: " + builder);
        }

        ImageIcon ret = returnedInitialImage;

        if (builder.isFilterWaterMark()) {
            ret = ImageUtil.cropImage(ret, 0, 0, builder.getWidth(), builder.getHeight());
        }

        return ret;
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
         * The location string to use if latitude and longitude are not provided.
         */
        private String locationString;

        /**
         * The latitude of the map image.
         */
        private double lat = Integer.MIN_VALUE;

        /**
         * The longitude of the map image.
         */
        private double lon = Integer.MIN_VALUE;

        /**
         * The width of the map image.
         */
        private final int width;

        /**
         * The height of the map image.
         */
        private final int height;

        /**
         * The api key.
         */
        private final String key;

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
         * @param width  the width of the final image
         * @param height the height of the final image
         */
        public Builder(int width, int height, String key) {
            Preconditions.checkArgument(widthRange.contains(width));
            Preconditions.checkArgument(heightRange.contains(height));
            Preconditions.checkNotNull(key);
            Preconditions.checkArgument(!key.isEmpty());

            this.width = width;
            this.height = height;
            this.key = key;
        }

        /**
         * returns the location string for this builder.
         *
         * @param locationString the location string for this builder
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setLocationString(String locationString) {
            Preconditions.checkNotNull(locationString);
            Preconditions.checkArgument(!locationString.isEmpty());

            this.locationString = locationString;
            return this;
        }

        /**
         * Sets the latitude of this builder.
         *
         * @param lat the latitude of this builder
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setLat(double lat) {
            Preconditions.checkArgument(latitudeRange.contains(lat));
            this.lat = lat;
            return this;
        }

        /**
         * Sets the longitude of this builder.
         *
         * @param lon the longitude of this builder
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setLon(double lon) {
            Preconditions.checkArgument(longitudeRange.contains(lon));
            this.lon = lon;
            return this;
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
            this.scaleBar = true;
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

        /**
         * Returns the location string for this builder.
         *
         * @return the location string for this builder
         */
        public String getLocationString() {
            return locationString;
        }

        /**
         * Returns the key for this builder.
         *
         * @return the key for this builder
         */
        public String getKey() {
            return key;
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof Builder)) {
                return false;
            }

            Builder other = (Builder) o;
            return other.locationString.equals(locationString)
                    && other.lat == lat
                    && other.lon == lon
                    && other.width == width
                    && other.height == height
                    && other.key.equals(key)
                    && other.filterWaterMark == filterWaterMark
                    && other.scaleBar == scaleBar
                    && other.scaleBarLocation.equals(scaleBarLocation)
                    && other.mapType.equals(mapType)
                    && other.zoomLevel == zoomLevel;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int ret = locationString.hashCode();
            ret = 31 * ret + Double.hashCode(lat);
            ret = 31 * ret + Double.hashCode(lon);
            ret = 31 * ret + Integer.hashCode(width);
            ret = 31 * ret + Integer.hashCode(height);
            ret = 31 * ret + key.hashCode();
            ret = 31 * ret + Boolean.hashCode(filterWaterMark);
            ret = 31 * ret + Boolean.hashCode(scaleBar);
            ret = 31 * ret + scaleBarLocation.hashCode();
            ret = 31 * ret + mapType.hashCode();
            ret = 31 * ret + Integer.hashCode(zoomLevel);
            return ret;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            String keyRep = "HIDDEN";
            if (key != null && key.length() > 5) {
                keyRep = key.substring(0, 6) + "...";
            }

            return "Builder{"
                    + "locationString=\"" + locationString + "\""
                    + ", lat=" + lat
                    + ", lon=" + lon
                    + ", width=" + width
                    + ", height=" + height
                    + ", key=\"" + keyRep + "\""
                    + ", filterWaterMark=" + filterWaterMark
                    + ", scaleBar=" + scaleBar
                    + ", scaleBarLocation=" + scaleBarLocation
                    + ", mapType=" + mapType
                    + ", zoomLevel=" + zoomLevel
                    + '}';
        }
    }
}
