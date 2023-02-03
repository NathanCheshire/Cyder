package cyder.time

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for [MonthDay] objects.
 */
class MonthDayTest {
    /**
     * Tests for creation of month day objects.
     */
    @Test
    fun testCreation() {
        assertThrows(IllegalArgumentException::class.java) { MonthDay(0, 5) }
        assertThrows(IllegalArgumentException::class.java) { MonthDay(13, 5) }
        assertThrows(IllegalArgumentException::class.java) { MonthDay(1, 0) }
        assertThrows(IllegalArgumentException::class.java) { MonthDay(1, 32) }

        assertDoesNotThrow { MonthDay(1, 1) }
        assertDoesNotThrow { MonthDay(1, 31) }
        assertDoesNotThrow { MonthDay(5, 31) }
        assertDoesNotThrow { MonthDay(12, 31) }
    }

    /**
     * Tests for the accessor methods.
     */
    @Test
    fun testAccessors() {
        val janFirst = MonthDay(1, 1)
        assertEquals(1, janFirst.month)
        assertEquals(1, janFirst.date)
        assertEquals("January", janFirst.monthString)
        assertEquals("1st", janFirst.dateString)
        assertEquals("January the 1st", janFirst.monthDateString)

        val julyFourth = MonthDay(7, 4)
        assertEquals(7, julyFourth.month)
        assertEquals(4, julyFourth.date)
        assertEquals("July", julyFourth.monthString)
        assertEquals("4th", julyFourth.dateString)
        assertEquals("July the 4th", julyFourth.monthDateString)
    }

    /**
     * Tests for the toString method.
     */
    @Test
    fun testToString() {
        val janFirst = MonthDay(1, 1)
        val equalToJanFirst = MonthDay(1, 1)
        val julyFourth = MonthDay(7, 4)

        assertEquals("MonthDay{month=1, date=1}", janFirst.toString())
        assertEquals("MonthDay{month=7, date=4}", julyFourth.toString())
        assertEquals(janFirst.toString(), equalToJanFirst.toString())
        assertNotEquals(janFirst.toString(), julyFourth.toString())
        assertNotEquals(janFirst.toString(), Object().toString())
    }

    /**
     * Tests for the hashcode method.
     */
    @Test
    fun testHashCode() {
        val janFirst = MonthDay(1, 1)
        val equalToJanFirst = MonthDay(1, 1)
        val julyFourth = MonthDay(7, 4)

        assertEquals(32, janFirst.hashCode())
        assertEquals(32, equalToJanFirst.hashCode())
        assertEquals(221, julyFourth.hashCode())
        assertEquals(janFirst.hashCode(), janFirst.hashCode())
        assertEquals(janFirst.hashCode(), equalToJanFirst.hashCode())
        assertNotEquals(equalToJanFirst.hashCode(), julyFourth.hashCode())
        assertNotEquals(equalToJanFirst.hashCode(), Object().hashCode())
    }

    /**
     * Tests for the equals method.
     */
    @Test
    fun testEquals() {
        val janFirst = MonthDay(1, 1)
        val equalToJanFirst = MonthDay(1, 1)
        val julyFourth = MonthDay(7, 4)

        assertEquals(janFirst, janFirst)
        assertEquals(janFirst, equalToJanFirst)
        assertNotEquals(equalToJanFirst, julyFourth)
        assertNotEquals(equalToJanFirst, Object())
    }
}