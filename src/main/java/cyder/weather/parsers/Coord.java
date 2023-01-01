package cyder.weather.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * The coords object for {@link WeatherData} objects.
 */
public class Coord {
    /**
     * The longitude of the coord object.
     */
    private double lon;

    /**
     * The latitude of the coord object.
     */
    private double lat;

    /**
     * Constructs a new coord object.
     */
    public Coord() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the longitude attribute.
     *
     * @return the longitude attribute
     */
    public double getLon() {
        return lon;
    }

    /**
     * Sets the longitude attribute.
     *
     * @param lon the longitude attribute
     */
    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     * Returns the latitude attribute.
     *
     * @return the latitude attribute
     */
    public double getLat() {
        return lat;
    }

    /**
     * Sets the latitude attribute.
     *
     * @param lat the latitude attribute
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Coord)) {
            return false;
        }

        Coord other = (Coord) o;
        return other.lat == lat
                && other.lon == lon;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Double.hashCode(lat);
        ret = 31 * ret + Double.hashCode(lon);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Coord{lat=" + lat + ", lon=" + lon + "}";
    }
}
