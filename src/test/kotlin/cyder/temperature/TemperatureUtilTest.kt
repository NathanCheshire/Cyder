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
        assertEquals(255.3722222222222, TemperatureUtil.fahrenheitToKelvin(0.0))
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

    }

    /**
     * Tests for the fahrenheit to celsius method.
     */
    @Test
    fun testFahrenheitToCelsius() {

    }

    /**
     * Tests for the kelvin to celsius method.
     */
    @Test
    fun testKelvinToCelsius() {

    }

    /**
     * Tests for the celsius to fahrenheit method.
     */
    @Test
    fun testCelsiusToFahrenheit() {

    }

    /**
     * Tests for the kelvin to fahrenheit method.
     */
    @Test
    fun testKelvinToFahrenheit() {

    }
}