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

    }

    /**
     * Tests for the point in rectangle method.
     */
    @Test
    fun testPointInRectangle() {

    }

    @Test
    fun testPointOnRectangle() {

    }

    /**
     * Tests for the point on top of rectangle method.
     */
    @Test
    fun testPointOnTopOfRectangle() {

    }

    /**
     * Tests for the point on left of rectangle method.
     */
    @Test
    fun testPointOnLeftOfRectangle() {

    }

    /**
     * Tests for the point on right of rectangle method.
     */
    @Test
    fun testPointOnRightOfRectangle() {

    }

    /**
     * Tests for the point on bottom of rectangle method.
     */
    @Test
    fun testPointOnBottomOfRectangle() {

    }
}