package cyder.parsers.weather;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * The "main" object for {@link WeatherData} objects.
 * I have no idea why this is called main; LocalAttributes would be a much better name.
 */
public class Main {
    /**
     * The local temperature.
     */
    private float temp;

    /**
     * The local feels-like temperature.
     */
    private float feels_like;

    /**
     * The minimum temperature.
     */
    private float temp_min;

    /**
     * The maximum temperature.
     */
    private float temp_max;

    /**
     * The atmospheric pressure.
     */
    private float pressure;

    /**
     * The local humidity.
     */
    private float humidity;

    /**
     * The sea level on the WSG84 ellipse.
     */
    private float sea_level;

    /**
     * The ground level of the location.
     */
    private float grnd_level;

    /**
     * Constructs a new main object.
     */
    Main() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the local temperature.
     *
     * @return the local temperature
     */
    public float getTemp() {
        return temp;
    }

    /**
     * Sets the local temperature.
     *
     * @param temp the local temperature
     */
    public void setTemp(float temp) {
        this.temp = temp;
    }

    /**
     * Returns the local feels-like temperature.
     *
     * @return the local feels-like temperature
     */
    public float getFeels_like() {
        return feels_like;
    }

    /**
     * Sets the local feels-like temperature.
     *
     * @param feels_like the local feels-like temperature
     */
    public void setFeels_like(float feels_like) {
        this.feels_like = feels_like;
    }

    /**
     * Returns the minimum temperature.
     *
     * @return the minimum temperature
     */
    public float getTemp_min() {
        return temp_min;
    }

    /**
     * Sets the minimum temperature.
     *
     * @param temp_min the minimum temperature
     */
    public void setTemp_min(float temp_min) {
        this.temp_min = temp_min;
    }

    /**
     * Returns the maximum temperature.
     *
     * @return the maximum temperature
     */
    public float getTemp_max() {
        return temp_max;
    }

    /**
     * Sets the maximum temperature.
     *
     * @param temp_max the maximum temperature
     */
    public void setTemp_max(float temp_max) {
        this.temp_max = temp_max;
    }

    /**
     * Returns the atmospheric pressure.
     *
     * @return the atmospheric pressure
     */
    public float getPressure() {
        return pressure;
    }

    /**
     * Sets the atmospheric pressure.
     *
     * @param pressure the atmospheric pressure
     */
    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    /**
     * Returns the humidity.
     *
     * @return the humidity
     */
    public float getHumidity() {
        return humidity;
    }

    /**
     * Sets the humidity.
     *
     * @param humidity the humidity
     */
    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    /**
     * Returns the sea level on the WSG84 ellipse.
     *
     * @return the sea level on the WSG84 ellipse
     */
    public float getSea_level() {
        return sea_level;
    }

    /**
     * Sets the sea level on the WSG84 ellipse.
     *
     * @param sea_level the sea level on the WSG84 ellipse
     */
    public void setSea_level(float sea_level) {
        this.sea_level = sea_level;
    }

    /**
     * Returns the ground level of the location.
     *
     * @return the ground level of the location
     */
    public float getGrnd_level() {
        return grnd_level;
    }

    /**
     * Sets the ground level of the location.
     *
     * @param grnd_level the ground level of the location
     */
    public void setGrnd_level(float grnd_level) {
        this.grnd_level = grnd_level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Main)) {
            return false;
        }

        Main other = (Main) o;

        return other.temp == temp
                && other.feels_like == feels_like
                && other.temp_min == temp_min
                && other.temp_max == temp_max
                && other.pressure == pressure
                && other.humidity == humidity
                && other.sea_level == sea_level
                && other.grnd_level == grnd_level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Float.hashCode(temp);

        ret = 31 * ret + Float.hashCode(temp_min);
        ret = 31 * ret + Float.hashCode(temp_max);
        ret = 31 * ret + Float.hashCode(pressure);
        ret = 31 * ret + Float.hashCode(humidity);
        ret = 31 * ret + Float.hashCode(sea_level);
        ret = 31 * ret + Float.hashCode(grnd_level);

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Main{"
                + "temp=" + temp
                + ", feels_like=" + feels_like
                + ", temp_min=" + temp_min
                + ", temp_max=" + temp_max
                + ", pressure=" + pressure
                + ", humidity=" + humidity
                + ", sea_level=" + sea_level
                + ", grnd_level=" + grnd_level
                + "}";
    }
}
