package cyder.temperature

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for [TemperatureUtil]s.
 */
class TemperatureUtilTest {
    /**
     *  Tests for the fahrenheit to kelvin method.
     */
    @Test
    fun testFahrenheitToKelvin() {
        assertEquals(1366.4833333333336, TemperatureUtil.fahrenheitToKelvin(2000.0))
        assertEquals(810.9277777777778, TemperatureUtil.fahrenheitToKelvin(1000.0))
        assertEquals(539.8166666666666, TemperatureUtil.fahrenheitToKelvin(512.0))
        assertEquals(397.59444444444443, TemperatureUtil.fahrenheitToKelvin(256.0))
        assertEquals(326.4833333333333, TemperatureUtil.fahrenheitToKelvin(128.0))
        assertEquals(290.92777777777775, TemperatureUtil.fahrenheitToKelvin(64.0))
        assertEquals(273.15, TemperatureUtil.fahrenheitToKelvin(32.0))
        assertEquals(264.26111111111106, TemperatureUtil.fahrenheitToKelvin(16.0))
        assertEquals(255.3722222222222, TemperatureUtil.fahrenheitToKelvin(0.0))
        assertEquals(246.48333333333332, TemperatureUtil.fahrenheitToKelvin(-16.0))
        assertEquals(237.59444444444443, TemperatureUtil.fahrenheitToKelvin(-32.0))
        assertEquals(219.81666666666663, TemperatureUtil.fahrenheitToKelvin(-64.0))
        assertEquals(184.2611111111111, TemperatureUtil.fahrenheitToKelvin(-128.0))
        assertEquals(113.14999999999998, TemperatureUtil.fahrenheitToKelvin(-256.0))
        assertEquals(103.6222222222222, TemperatureUtil.fahrenheitToKelvin(-273.15))
        assertEquals(0.0, TemperatureUtil.fahrenheitToKelvin(-459.67), 0.0000000001)
    }

    /**
     * Tests for the celsius to kelvin method.
     */
    @Test
    fun testCelsiusToKelvin() {
        assertEquals(2273.15, TemperatureUtil.celsiusToKelvin(2000.0))
        assertEquals(1273.15, TemperatureUtil.celsiusToKelvin(1000.0))
        assertEquals(785.15, TemperatureUtil.celsiusToKelvin(512.0))
        assertEquals(529.15, TemperatureUtil.celsiusToKelvin(256.0))
        assertEquals(401.15, TemperatureUtil.celsiusToKelvin(128.0))
        assertEquals(337.15, TemperatureUtil.celsiusToKelvin(64.0))
        assertEquals(305.15, TemperatureUtil.celsiusToKelvin(32.0))
        assertEquals(289.15, TemperatureUtil.celsiusToKelvin(16.0))
        assertEquals(273.15, TemperatureUtil.celsiusToKelvin(0.0))
        assertEquals(257.15, TemperatureUtil.celsiusToKelvin(-16.0))
        assertEquals(241.14999999999998, TemperatureUtil.celsiusToKelvin(-32.0))
        assertEquals(209.14999999999998, TemperatureUtil.celsiusToKelvin(-64.0))
        assertEquals(145.14999999999998, TemperatureUtil.celsiusToKelvin(-128.0))
        assertEquals(17.149999999999977, TemperatureUtil.celsiusToKelvin(-256.0))
        assertEquals(0.0, TemperatureUtil.celsiusToKelvin(-273.15))
    }

    /**
     * Tests for the fahrenheit to celsius method.
     */
    @Test
    fun testFahrenheitToCelsius() {
        assertEquals(1093.3333333333335, TemperatureUtil.fahrenheitToCelsius(2000.0))
        assertEquals(537.7777777777778, TemperatureUtil.fahrenheitToCelsius(1000.0))
        assertEquals(266.6666666666667, TemperatureUtil.fahrenheitToCelsius(512.0))
        assertEquals(124.44444444444446, TemperatureUtil.fahrenheitToCelsius(256.0))
        assertEquals(53.333333333333336, TemperatureUtil.fahrenheitToCelsius(128.0))
        assertEquals(17.77777777777778, TemperatureUtil.fahrenheitToCelsius(64.0))
        assertEquals(0.0, TemperatureUtil.fahrenheitToCelsius(32.0))
        assertEquals(-8.88888888888889, TemperatureUtil.fahrenheitToCelsius(16.0))
        assertEquals(-17.77777777777778, TemperatureUtil.fahrenheitToCelsius(0.0))
        assertEquals(-26.666666666666668, TemperatureUtil.fahrenheitToCelsius(-16.0))
        assertEquals(-35.55555555555556, TemperatureUtil.fahrenheitToCelsius(-32.0))
        assertEquals(-53.333333333333336, TemperatureUtil.fahrenheitToCelsius(-64.0))
        assertEquals(-88.88888888888889, TemperatureUtil.fahrenheitToCelsius(-128.0))
        assertEquals(-160.0, TemperatureUtil.fahrenheitToCelsius(-256.0))
        assertEquals(-169.52777777777777, TemperatureUtil.fahrenheitToCelsius(-273.15))
        assertEquals(-273.15000000000003, TemperatureUtil.fahrenheitToCelsius(-459.67))
    }

    /**
     * Tests for the kelvin to celsius method.
     */
    @Test
    fun testKelvinToCelsius() {
        assertEquals(1726.85, TemperatureUtil.kelvinToCelsius(2000.0))
        assertEquals(726.85, TemperatureUtil.kelvinToCelsius(1000.0))
        assertEquals(238.85000000000002, TemperatureUtil.kelvinToCelsius(512.0))
        assertEquals(-17.149999999999977, TemperatureUtil.kelvinToCelsius(256.0))
        assertEquals(-145.14999999999998, TemperatureUtil.kelvinToCelsius(128.0))
        assertEquals(-209.14999999999998, TemperatureUtil.kelvinToCelsius(64.0))
        assertEquals(0.0, TemperatureUtil.fahrenheitToCelsius(32.0))
        assertEquals(-257.15, TemperatureUtil.kelvinToCelsius(16.0))
        assertEquals(-273.15, TemperatureUtil.kelvinToCelsius(0.0))
    }

    /**
     * Tests for the celsius to fahrenheit method.
     */
    @Test
    fun testCelsiusToFahrenheit() {
        assertEquals(3632.0, TemperatureUtil.celsiusToFahrenheit(2000.0))
        assertEquals(1832.0, TemperatureUtil.celsiusToFahrenheit(1000.0))
        assertEquals(953.6, TemperatureUtil.celsiusToFahrenheit(512.0))
        assertEquals(492.8, TemperatureUtil.celsiusToFahrenheit(256.0))
        assertEquals(262.4, TemperatureUtil.celsiusToFahrenheit(128.0))
        assertEquals(147.2, TemperatureUtil.celsiusToFahrenheit(64.0))
        assertEquals(89.6, TemperatureUtil.celsiusToFahrenheit(32.0))
        assertEquals(60.8, TemperatureUtil.celsiusToFahrenheit(16.0))
        assertEquals(32.0, TemperatureUtil.celsiusToFahrenheit(0.0))
        assertEquals(3.1999999999999993, TemperatureUtil.celsiusToFahrenheit(-16.0))
        assertEquals(-25.6, TemperatureUtil.celsiusToFahrenheit(-32.0))
        assertEquals(-83.2, TemperatureUtil.celsiusToFahrenheit(-64.0))
        assertEquals(-198.4, TemperatureUtil.celsiusToFahrenheit(-128.0))
        assertEquals(-428.8, TemperatureUtil.celsiusToFahrenheit(-256.0))
        assertEquals(-459.66999999999996, TemperatureUtil.celsiusToFahrenheit(-273.15))
        assertEquals(-795.4060000000001, TemperatureUtil.celsiusToFahrenheit(-459.67))
    }

    /**
     * Tests for the kelvin to fahrenheit method.
     */
    @Test
    fun testKelvinToFahrenheit() {
        assertEquals(3140.33, TemperatureUtil.kelvinToFahrenheit(2000.0))
        assertEquals(1340.3300000000002, TemperatureUtil.kelvinToFahrenheit(1000.0))
        assertEquals(461.93000000000006, TemperatureUtil.kelvinToFahrenheit(512.0))
        assertEquals(1.1300000000000416, TemperatureUtil.kelvinToFahrenheit(256.0))
        assertEquals(-229.26999999999998, TemperatureUtil.kelvinToFahrenheit(128.0))
        assertEquals(-344.46999999999997, TemperatureUtil.kelvinToFahrenheit(64.0))
        assertEquals(-402.07, TemperatureUtil.kelvinToFahrenheit(32.0))
        assertEquals(-430.86999999999995, TemperatureUtil.kelvinToFahrenheit(16.0))
        assertEquals(-459.66999999999996, TemperatureUtil.kelvinToFahrenheit(0.0))
    }
}