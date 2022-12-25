package cyder.bounds

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests for [BoundsString]s.
 */
class BoundsStringTest {
    /**
     * Tests for creation of BoundsStrings.
     */
    @Test
    fun testCreation() {
        Assertions.assertThrows(NullPointerException::class.java) { BoundsString(null, 0, 0) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { BoundsString("", -1, 0) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { BoundsString("", 0, -1) }
        Assertions.assertDoesNotThrow { BoundsString("", 0, 0) }
    }

    /**
     * Tests for the toString method of BoundsStrings.
     */
    @Test
    fun testToString() {
        val first = BoundsString("first", 10, 10)
        val second = BoundsString("second", 12, 12)
        val third = BoundsString("second", 12, 12)

        Assertions.assertEquals("BoundsString{text=\"first\", width=10\", height=10}", first.toString())
        Assertions.assertEquals("BoundsString{text=\"second\", width=12\", height=12}", second.toString())
        Assertions.assertEquals("BoundsString{text=\"second\", width=12\", height=12}", third.toString())

        Assertions.assertNotEquals(first.toString(), second.toString())
        Assertions.assertEquals(second.toString(), third.toString())
    }

    /**
     * Tests for the hashCode method of BoundsStrings.
     */
    @Test
    fun testHashCode() {
        val first = BoundsString("first", 10, 10)
        val second = BoundsString("second", 12, 12)
        val third = BoundsString("second", 12, 12)

        Assertions.assertEquals(-849025040, first.hashCode())
        Assertions.assertEquals(943454452, second.hashCode())
        Assertions.assertEquals(943454452, third.hashCode())

        Assertions.assertNotEquals(first.hashCode(), second.hashCode())
        Assertions.assertEquals(second.hashCode(), third.hashCode())
    }

    /**
     * Tests for the equals method of BoundsStrings.
     */
    @Test
    fun testEquals() {
        val first = BoundsString("first", 10, 10)
        val second = BoundsString("second", 12, 12)
        val third = BoundsString("second", 12, 12)

        Assertions.assertNotEquals(first, second)
        Assertions.assertEquals(second, third)
    }
}