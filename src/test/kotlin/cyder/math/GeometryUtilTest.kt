package cyder.math

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.awt.Point
import java.awt.Rectangle

/**
 * Tests for the [GeometryUtil].
 */
class GeometryUtilTest {
    /**
     * Tests for the rotate point method.
     */
    @Test
    fun testRotatePoint() {
        assertThrows(NullPointerException::class.java) { GeometryUtil.rotatePoint(null, 0.0) }
        assertEquals(Point(0, 0), GeometryUtil.rotatePoint(Point(0, 0), 0.0))
        assertEquals(Point(0, 0), GeometryUtil.rotatePoint(Point(0, 0), 90.0))
        assertEquals(Point(0, 0), GeometryUtil.rotatePoint(Point(0, 0), 180.0))
        assertEquals(Point(0, 0), GeometryUtil.rotatePoint(Point(0, 0), 270.0))
        assertEquals(Point(0, 0), GeometryUtil.rotatePoint(Point(0, 0), 360.0))

        assertEquals(Point(20, 20), GeometryUtil.rotatePoint(Point(20, 20), 0.0))
        assertEquals(Point(0, 28), GeometryUtil.rotatePoint(Point(20, 20), 45.0))
        assertEquals(Point(-20, 20), GeometryUtil.rotatePoint(Point(20, 20), 90.0))
        assertEquals(Point(-20, -19), GeometryUtil.rotatePoint(Point(20, 20), 180.0))
        assertEquals(Point(19, -20), GeometryUtil.rotatePoint(Point(20, 20), 270.0))
        assertEquals(Point(20, 20), GeometryUtil.rotatePoint(Point(20, 20), 360.0))
    }

    /**
     * Tests for the rectangles overlap method.
     */
    @Test
    fun testRectanglesOverlap() {
        assertThrows(NullPointerException::class.java) { GeometryUtil.rectanglesOverlap(null, null) }
        assertThrows(NullPointerException::class.java) { GeometryUtil.rectanglesOverlap(Rectangle(), null) }

        assertTrue(GeometryUtil.rectanglesOverlap(Rectangle(0, 0, 20, 20),
                Rectangle(0, 0, 20, 20)))
        assertTrue(GeometryUtil.rectanglesOverlap(Rectangle(0, 0, 20, 20),
                Rectangle(19, 19, 20, 20)))
        assertFalse(GeometryUtil.rectanglesOverlap(Rectangle(0, 0, 20, 20),
                Rectangle(20, 20, 20, 20)))

        assertTrue(GeometryUtil.rectanglesOverlap(Rectangle(0, 0, 20, 20),
                Rectangle(0, 19, 20, 20)))
        assertFalse(GeometryUtil.rectanglesOverlap(Rectangle(0, 0, 20, 20),
                Rectangle(0, 20, 20, 20)))

        assertTrue(GeometryUtil.rectanglesOverlap(Rectangle(0, 0, 20, 20),
                Rectangle(-19, 0, 20, 20)))
        assertFalse(GeometryUtil.rectanglesOverlap(Rectangle(0, 0, 20, 20),
                Rectangle(-20, 0, 20, 20)))
    }

    /**
     * Tests for the point in or on rectangle method.
     */
    @Test
    fun testPointInOrOnRectangle() {
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointInOrOnRectangle(null, Rectangle()) }
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointInOrOnRectangle(Point(), null) }

        assertTrue(GeometryUtil.pointInOrOnRectangle(Point(), Rectangle()))
        assertTrue(GeometryUtil.pointInOrOnRectangle(Point(0, 0), Rectangle(-5, -5, 10, 10)))
        assertTrue(GeometryUtil.pointInOrOnRectangle(Point(5, 5), Rectangle(-5, -5, 10, 10)))
        assertTrue(GeometryUtil.pointInOrOnRectangle(Point(-5, 5), Rectangle(-5, -5, 10, 10)))
        assertTrue(GeometryUtil.pointInOrOnRectangle(Point(5, -5), Rectangle(-5, -5, 10, 10)))
        assertTrue(GeometryUtil.pointInOrOnRectangle(Point(-5, -5), Rectangle(-5, -5, 10, 10)))


        assertFalse(GeometryUtil.pointInOrOnRectangle(Point(-6, -6), Rectangle(-5, -5, 10, 10)))
        assertFalse(GeometryUtil.pointInOrOnRectangle(Point(-6, 6), Rectangle(-5, -5, 10, 10)))
        assertFalse(GeometryUtil.pointInOrOnRectangle(Point(6, -6), Rectangle(-5, -5, 10, 10)))
        assertFalse(GeometryUtil.pointInOrOnRectangle(Point(6, 6), Rectangle(-5, -5, 10, 10)))
    }

    /**
     * Tests for the point in rectangle method.
     */
    @Test
    fun testPointInRectangle() {
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointInRectangle(null, Rectangle()) }
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointInRectangle(Point(), null) }

        assertFalse { GeometryUtil.pointInRectangle(Point(), Rectangle()) }
        assertTrue { GeometryUtil.pointInRectangle(Point(0, 0), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointInRectangle(Point(-4, -4), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointInRectangle(Point(-4, 4), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointInRectangle(Point(4, -4), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointInRectangle(Point(4, 4), Rectangle(-5, -5, 10, 10)) }

        assertFalse { GeometryUtil.pointInRectangle(Point(-5, -5), Rectangle(-5, -5, 10, 10)) }
        assertFalse { GeometryUtil.pointInRectangle(Point(-5, 5), Rectangle(-5, -5, 10, 10)) }
        assertFalse { GeometryUtil.pointInRectangle(Point(5, -5), Rectangle(-5, -5, 10, 10)) }
        assertFalse { GeometryUtil.pointInRectangle(Point(5, 5), Rectangle(-5, -5, 10, 10)) }
    }

    @Test
    fun testPointOnRectangle() {
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointOnRectangle(null, Rectangle()) }
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointOnRectangle(Point(), null) }

        assertFalse { GeometryUtil.pointInRectangle(Point(), Rectangle()) }
        assertFalse { GeometryUtil.pointOnRectangle(Point(0, 0), Rectangle(-5, -5, 10, 10)) }
        assertFalse { GeometryUtil.pointOnRectangle(Point(-4, -4), Rectangle(-5, -5, 10, 10)) }
        assertFalse { GeometryUtil.pointOnRectangle(Point(-4, 4), Rectangle(-5, -5, 10, 10)) }
        assertFalse { GeometryUtil.pointOnRectangle(Point(4, -4), Rectangle(-5, -5, 10, 10)) }
        assertFalse { GeometryUtil.pointOnRectangle(Point(4, 4), Rectangle(-5, -5, 10, 10)) }

        assertTrue { GeometryUtil.pointOnRectangle(Point(-5, -5), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnRectangle(Point(-5, 5), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnRectangle(Point(5, -5), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnRectangle(Point(5, 5), Rectangle(-5, -5, 10, 10)) }

        assertFalse { GeometryUtil.pointOnRectangle(Point(-6, -6), Rectangle(-5, -5, 10, 10)) }
        assertFalse { GeometryUtil.pointOnRectangle(Point(-6, 6), Rectangle(-5, -5, 10, 10)) }
        assertFalse { GeometryUtil.pointOnRectangle(Point(6, -6), Rectangle(-5, -5, 10, 10)) }
        assertFalse { GeometryUtil.pointOnRectangle(Point(6, 6), Rectangle(-5, -5, 10, 10)) }
    }

    /**
     * Tests for the point on top of rectangle method.
     */
    @Test
    fun testPointOnTopOfRectangle() {
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointOnTopOfRectangle(null, Rectangle()) }
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointOnTopOfRectangle(Point(), null) }

        assertTrue { GeometryUtil.pointOnTopOfRectangle(Point(), Rectangle()) }
        assertFalse { GeometryUtil.pointOnTopOfRectangle(Point(0, 0), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnTopOfRectangle(Point(-5, -5), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnTopOfRectangle(Point(5, -5), Rectangle(-5, -5, 10, 10)) }

        assertFalse(GeometryUtil.pointOnTopOfRectangle(Point(-5, 5), Rectangle(-5, -5, 10, 10)))
        assertFalse(GeometryUtil.pointOnTopOfRectangle(Point(5, 5), Rectangle(-5, -5, 10, 10)))
    }

    /**
     * Tests for the point on left of rectangle method.
     */
    @Test
    fun testPointOnLeftOfRectangle() {
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointOnLeftOfRectangle(null, Rectangle()) }
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointOnLeftOfRectangle(Point(), null) }

        assertTrue { GeometryUtil.pointOnLeftOfRectangle(Point(), Rectangle()) }
        assertFalse { GeometryUtil.pointOnLeftOfRectangle(Point(0, 0), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnLeftOfRectangle(Point(-5, -5), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnLeftOfRectangle(Point(-5, 0), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnLeftOfRectangle(Point(-5, 5), Rectangle(-5, -5, 10, 10)) }

        assertFalse(GeometryUtil.pointOnLeftOfRectangle(Point(5, 5), Rectangle(-5, -5, 10, 10)))
        assertFalse(GeometryUtil.pointOnLeftOfRectangle(Point(5, -5), Rectangle(-5, -5, 10, 10)))
    }

    /**
     * Tests for the point on right of rectangle method.
     */
    @Test
    fun testPointOnRightOfRectangle() {
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointOnRightOfRectangle(null, Rectangle()) }
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointOnRightOfRectangle(Point(), null) }

        assertTrue { GeometryUtil.pointOnRightOfRectangle(Point(), Rectangle()) }
        assertFalse { GeometryUtil.pointOnRightOfRectangle(Point(0, 0), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnRightOfRectangle(Point(5, -5), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnRightOfRectangle(Point(5, 0), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnRightOfRectangle(Point(5, 5), Rectangle(-5, -5, 10, 10)) }

        assertFalse(GeometryUtil.pointOnRightOfRectangle(Point(-5, 5), Rectangle(-5, -5, 10, 10)))
        assertFalse(GeometryUtil.pointOnRightOfRectangle(Point(-5, -5), Rectangle(-5, -5, 10, 10)))
    }

    /**
     * Tests for the point on bottom of rectangle method.
     */
    @Test
    fun testPointOnBottomOfRectangle() {
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointOnBottomOfRectangle(null, Rectangle()) }
        assertThrows(NullPointerException::class.java) { GeometryUtil.pointOnBottomOfRectangle(Point(), null) }

        assertTrue { GeometryUtil.pointOnBottomOfRectangle(Point(), Rectangle()) }
        assertFalse { GeometryUtil.pointOnBottomOfRectangle(Point(0, 0), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnBottomOfRectangle(Point(-5, -5), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnBottomOfRectangle(Point(0, -5), Rectangle(-5, -5, 10, 10)) }
        assertTrue { GeometryUtil.pointOnBottomOfRectangle(Point(5, -5), Rectangle(-5, -5, 10, 10)) }

        assertFalse(GeometryUtil.pointOnBottomOfRectangle(Point(-5, 5), Rectangle(-5, -5, 10, 10)))
        assertFalse(GeometryUtil.pointOnBottomOfRectangle(Point(5, 5), Rectangle(-5, -5, 10, 10)))
    }
}