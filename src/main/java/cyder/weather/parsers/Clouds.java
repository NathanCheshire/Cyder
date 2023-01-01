package cyder.weather.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * The clouds object for {@link WeatherData} objects.
 */
public class Clouds {
    /**
     * The all attribute of the clouds object.
     */
    private double all;

    /**
     * Constructs a new clouds object.
     */
    public Clouds() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the all attribute.
     *
     * @return the all attribute
     */
    public double getAll() {
        return all;
    }

    /**
     * Sets the all attribute.
     *
     * @param all the all attribute
     */
    public void setAll(double all) {
        this.all = all;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Clouds)) {
            return false;
        }

        Clouds other = (Clouds) o;
        return other.all == all;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Double.hashCode(all);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Clouds{all=" + all + "}";
    }
}
