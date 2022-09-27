package cyder.utils;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

/**
 * Utility methods related to temperature conversions.
 */
public final class TemperatureUtil {
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
        return (fahrenheit - 32.0) * (5.0 / 9.0) + 273.15;
    }

    /**
     * Converts the provided celsius value to kelvin.
     *
     * @param celsius the value
     * @return the provided celsius value in kelvin
     */
    public static double celsiusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    /**
     * Converts the provided fahrenheit value to celsius.
     *
     * @param fahrenheit the value
     * @return the provided fahrenheit value in celsius
     */
    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * (5.0 / 9.0);
    }

    /**
     * Converts the provided kelvin value to celsius.
     *
     * @param kelvin the value
     * @return the provided kelvin value in celsius
     */
    public static double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }

    /**
     * Converts the provided celsius value to fahrenheit.
     *
     * @param celsius the value
     * @return the provided celsius value in fahrenheit
     */
    public static double celsiusToFahrenheit(double celsius) {
        return celsius * 1.8 + 32.0;
    }

    /**
     * Converts the provided kelvin value to fahrenheit.
     *
     * @param kelvin the value
     * @return the provided kelvin value in fahrenheit
     */
    public static double kelvinToFahrenheit(double kelvin) {
        return 1.8 * (kelvin - 273.15) + 32.0;
    }
}
