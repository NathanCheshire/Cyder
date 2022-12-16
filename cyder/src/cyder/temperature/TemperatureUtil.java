package cyder.temperature;

import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

/**
 * Utility methods related to temperature conversions.
 */
public final class TemperatureUtil {
    /**
     * The value to add to Celsius measurements to convert them to Kelvin measurements.
     */
    private static final double kelvinAdditive = 273.15;

    /**
     * The freezing point of water on a Fahrenheit scale.
     */
    private static final double fahrenheitFreezingPoint = 32.0;

    /**
     * Suppress default constructor.
     */
    private TemperatureUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Converts the provided fahrenheit value to kelvin.
     *
     * @param fahrenheit the value
     * @return the provided fahrenheit value in kelvin
     */
    public static double fahrenheitToKelvin(double fahrenheit) {
        return (fahrenheit - fahrenheitFreezingPoint) * (5.0 / 9.0) + kelvinAdditive;
    }

    /**
     * Converts the provided celsius value to kelvin.
     *
     * @param celsius the value
     * @return the provided celsius value in kelvin
     */
    public static double celsiusToKelvin(double celsius) {
        return celsius + kelvinAdditive;
    }

    /**
     * Converts the provided fahrenheit value to celsius.
     *
     * @param fahrenheit the value
     * @return the provided fahrenheit value in celsius
     */
    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - fahrenheitFreezingPoint) * (5.0 / 9.0);
    }

    /**
     * Converts the provided kelvin value to celsius.
     *
     * @param kelvin the value
     * @return the provided kelvin value in celsius
     */
    public static double kelvinToCelsius(double kelvin) {
        return kelvin - kelvinAdditive;
    }

    /**
     * Converts the provided celsius value to fahrenheit.
     *
     * @param celsius the value
     * @return the provided celsius value in fahrenheit
     */
    public static double celsiusToFahrenheit(double celsius) {
        return celsius * 1.8 + fahrenheitFreezingPoint;
    }

    /**
     * Converts the provided kelvin value to fahrenheit.
     *
     * @param kelvin the value
     * @return the provided kelvin value in fahrenheit
     */
    public static double kelvinToFahrenheit(double kelvin) {
        return 1.8 * (kelvin - kelvinAdditive) + fahrenheitFreezingPoint;
    }
}
