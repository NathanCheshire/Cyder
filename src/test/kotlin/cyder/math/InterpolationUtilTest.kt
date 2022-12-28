package cyder.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for [InterpolationUtil]s.
 */
class InterpolationUtilTest {
    /**
     * Tests for the range map method.
     */
    @Test
    fun testRangeMap() {
        assertEquals(0.0, InterpolationUtil.rangeMap(0.0, 0.0, 1.0, 0.0, 50.0))
        assertEquals(50.0, InterpolationUtil.rangeMap(1.0, 0.0, 1.0, 0.0, 50.0))
        assertEquals(25.0, InterpolationUtil.rangeMap(0.5, 0.0, 1.0, 0.0, 50.0))
        assertEquals(40.0, InterpolationUtil.rangeMap(0.8, 0.0, 1.0, 0.0, 50.0))
        assertEquals(-50.0, InterpolationUtil.rangeMap(-1.0, 0.0, 1.0, 0.0, 50.0))

        assertEquals(-23.333333333333332, InterpolationUtil.rangeMap(0.0, 50.0, 200.0, 0.0, 70.0))
        assertEquals(-22.866666666666667, InterpolationUtil.rangeMap(1.0, 50.0, 200.0, 0.0, 70.0))
        assertEquals(-23.1, InterpolationUtil.rangeMap(0.5, 50.0, 200.0, 0.0, 70.0))
        assertEquals(-22.96, InterpolationUtil.rangeMap(0.8, 50.0, 200.0, 0.0, 70.0))
        assertEquals(-23.8, InterpolationUtil.rangeMap(-1.0, 50.0, 200.0, 0.0, 70.0))
    }
}