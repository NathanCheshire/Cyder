package cyder.weather.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * The wind weather object.
 */
public class Wind {
    /**
     * The wind speed.
     */
    private float speed;

    /**
     * The wind bearing in degrees.
     */
    private int deg;

    /**
     * The wind gusts speed.
     */
    private float gust;

    /**
     * Constructs a new wind object.
     */
    public Wind() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the wind speed.
     *
     * @return the wind speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Sets the wind speed.
     *
     * @param speed the wind speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Returns the wind bearing in degrees.
     *
     * @return the wind bearing in degrees
     */
    public int getDeg() {
        return deg;
    }

    /**
     * Sets the wind bearing in degrees.
     *
     * @param deg the wind bearing in degrees
     */
    public void setDeg(int deg) {
        this.deg = deg;
    }

    /**
     * Returns the wind gust speed.
     *
     * @return the wind gust speed
     */
    public float getGust() {
        return gust;
    }

    /**
     * Sets the wind gust speed.
     *
     * @param gust the wind gust speed
     */
    public void setGust(float gust) {
        this.gust = gust;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Wind)) {
            return false;
        }

        Wind other = (Wind) o;
        return speed == other.speed
                && deg == other.deg
                && gust == other.gust;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Float.hashCode(speed);
        ret = 31 * ret + Float.hashCode(deg);
        ret = 31 * ret + Float.hashCode(gust);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Wind{"
                + "speed=" + speed
                + ", deg=" + deg
                + ", gust=" + gust
                + "}";
    }
}
