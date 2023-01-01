package cyder.weather.parsers;

import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * The rain object for {@link WeatherData} objects.
 */
public class Rain {
    /**
     * The one hour field.
     */
    @SerializedName("1h")
    private float oneHour;

    /**
     * Constructs a new rain object.
     */
    public Rain() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the one hour field.
     *
     * @return the one hour field
     */
    public float getOneHour() {
        return oneHour;
    }

    /**
     * Sets the one hour field.
     *
     * @param oneHour the one hour field
     */
    public void setOneHour(float oneHour) {
        this.oneHour = oneHour;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Rain)) {
            return false;
        }

        Rain other = (Rain) o;
        return other.oneHour == oneHour;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Float.hashCode(oneHour);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Rain{oneHour=" + oneHour + "}";
    }
}
