package cyder.math

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for [AngleUtil]s.
 */
class AngleUtilTest {
    /**
     * Tests for the normalize angle 360 method.
     */
    @Test
    fun testNormalizeAngle360() {
        assertEquals(0, AngleUtil.normalizeAngle360(0))
        assertEquals(90, AngleUtil.normalizeAngle360(90))
        assertEquals(180, AngleUtil.normalizeAngle360(180))
        assertEquals(270, AngleUtil.normalizeAngle360(270))
        assertEquals(359, AngleUtil.normalizeAngle360(359))
        assertEquals(0, AngleUtil.normalizeAngle360(360))
        assertEquals(1, AngleUtil.normalizeAngle360(361))
        assertEquals(359, AngleUtil.normalizeAngle360(719))
        assertEquals(0, AngleUtil.normalizeAngle360(720))
        assertEquals(1, AngleUtil.normalizeAngle360(721))

        assertEquals(0.0, AngleUtil.normalizeAngle360(0.0))
        assertEquals(90.0, AngleUtil.normalizeAngle360(90.0))
        assertEquals(180.0, AngleUtil.normalizeAngle360(180.0))
        assertEquals(270.0, AngleUtil.normalizeAngle360(270.0))
        assertEquals(359.0, AngleUtil.normalizeAngle360(359.0))
        assertEquals(0.0, AngleUtil.normalizeAngle360(360.0))
        assertEquals(1.0, AngleUtil.normalizeAngle360(361.0))
        assertEquals(359.0, AngleUtil.normalizeAngle360(719.0))
        assertEquals(0.0, AngleUtil.normalizeAngle360(720.0))
        assertEquals(1.0, AngleUtil.normalizeAngle360(721.0))
    }

    /**
     * Tests for the normalize angle 180 method.
     */
    @Test
    fun testNormalizeAngle180() {
        assertEquals(0.0, AngleUtil.normalizeAngle180(0.0))
        assertEquals(90.0, AngleUtil.normalizeAngle180(90.0))
        assertEquals(0.0, AngleUtil.normalizeAngle180(180.0))
        assertEquals(90.0, AngleUtil.normalizeAngle180(270.0))
        assertEquals(179.0, AngleUtil.normalizeAngle180(359.0))
        assertEquals(0.0, AngleUtil.normalizeAngle180(360.0))
        assertEquals(1.0, AngleUtil.normalizeAngle180(361.0))
        assertEquals(179.0, AngleUtil.normalizeAngle180(719.0))
        assertEquals(0.0, AngleUtil.normalizeAngle180(720.0))
        assertEquals(1.0, AngleUtil.normalizeAngle180(721.0))
    }

    /**
     * Tests for the angle in northern hemisphere method.
     */
    @Test
    fun testAngleInNorthernHemisphere() {
        assertTrue(AngleUtil.angleInNorthernHemisphere(1.0))
        assertTrue(AngleUtil.angleInNorthernHemisphere(90.0))
        assertTrue(AngleUtil.angleInNorthernHemisphere(170.0))
        assertTrue(AngleUtil.angleInNorthernHemisphere(179.0))

        assertFalse(AngleUtil.angleInNorthernHemisphere(0.0))
        assertFalse(AngleUtil.angleInNorthernHemisphere(180.0))
        assertFalse(AngleUtil.angleInNorthernHemisphere(270.0))
        assertFalse(AngleUtil.angleInNorthernHemisphere(360.0))

        assertTrue(AngleUtil.angleInNorthernHemisphere(361.0))
    }

    /**
     * Tests for the angle in southern hemisphere method.
     */
    @Test
    fun testAngleInSouthernHemisphere() {
        assertTrue(AngleUtil.angleInSouthernHemisphere(181.0))
        assertTrue(AngleUtil.angleInSouthernHemisphere(270.0))
        assertTrue(AngleUtil.angleInSouthernHemisphere(271.0))
        assertTrue(AngleUtil.angleInSouthernHemisphere(350.0))
        assertTrue(AngleUtil.angleInSouthernHemisphere(359.0))

        assertFalse(AngleUtil.angleInSouthernHemisphere(1.0))
        assertFalse(AngleUtil.angleInSouthernHemisphere(90.0))
        assertFalse(AngleUtil.angleInSouthernHemisphere(170.0))
        assertFalse(AngleUtil.angleInSouthernHemisphere(179.0))

        assertTrue(AngleUtil.angleInSouthernHemisphere(700.0))
    }

    /**
     * Tests for the angle is east method.
     */
    @Test
    fun testAngleIsEast() {
        assertTrue(AngleUtil.angleIsEast(0.0))
        assertTrue(AngleUtil.angleIsEast(360.0))
        assertTrue(AngleUtil.angleIsEast(720.0))

        assertFalse(AngleUtil.angleIsEast(180.0))
        assertFalse(AngleUtil.angleIsEast(500.0))
        assertFalse(AngleUtil.angleIsEast(700.0))
    }

    /**
     * Tests for the angle is west method.
     */
    @Test
    fun testAngleIsWest() {
        assertTrue(AngleUtil.angleIsWest(180.0))
        assertTrue(AngleUtil.angleIsWest(540.0))
        assertTrue(AngleUtil.angleIsWest(900.0))

        assertFalse(AngleUtil.angleIsWest(160.0))
        assertFalse(AngleUtil.angleIsWest(400.0))
        assertFalse(AngleUtil.angleIsWest(500.0))
    }
}