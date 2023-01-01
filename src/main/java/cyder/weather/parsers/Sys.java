package cyder.weather.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * The "sys" object for {@link WeatherData} objects.
 * Sys is a bad name for this class.
 */
public class Sys {
    /**
     * The OpenWeatherMap API internal type field.
     */
    private int type;

    /**
     * The OpenWeatherMap API internal id field.
     */
    private int id;

    /**
     * The country of origin.
     */
    private String country;

    /**
     * The time of sunrise.
     */
    private long sunrise;

    /**
     * The time of sunset.
     */
    private long sunset;

    /**
     * Constructs a new sys object.
     */
    public Sys() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Sets the type field.
     *
     * @param type the type field
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Sets the id field.
     *
     * @param id the id field
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the country code.
     *
     * @param country the country code
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Sets the sunrise time.
     *
     * @param sunrise the sunrise time
     */
    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    /**
     * Sets the sunset time.
     *
     * @param sunset the sunset time
     */
    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    /**
     * Returns the type field.
     *
     * @return the type field
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the id field.
     *
     * @return the id field
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the country of origin.
     *
     * @return the country of origin
     */
    public String getCountry() {
        return country;
    }

    /**
     * Returns the sunrise time.
     *
     * @return the sunrise time
     */
    public long getSunrise() {
        return sunrise;
    }

    /**
     * Returns the sunset time.
     *
     * @return the sunset time
     */
    public long getSunset() {
        return sunset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Sys)) {
            return false;
        }

        Sys other = (Sys) o;

        return other.type == type
                && other.id == id
                && other.country.equals(country)
                && other.sunrise == sunrise
                && other.sunset == sunset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(type);

        ret = 31 * ret + Integer.hashCode(id);
        ret = 31 * ret + country.hashCode();
        ret = 31 * ret + Long.hashCode(sunrise);
        ret = 31 * ret + Long.hashCode(sunset);

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Sys{"
                + "type=" + type
                + ", id=" + id
                + ", country=\"" + country + "\""
                + ", sunrise=" + sunrise
                + ", sunset=" + sunset
                + "}";
    }
}
