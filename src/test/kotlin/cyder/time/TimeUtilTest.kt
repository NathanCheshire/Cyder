package cyder.time

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.stream.IntStream

/**
 * Tests for the utilities exposed by [TimeUtil].
 */
class TimeUtilTest {
    /**
     * Tests for the time getter formatter methods.
     */
    @Test
    fun testTimeGetters() {
        assertDoesNotThrow { TimeUtil.weatherTime() }
        assertDoesNotThrow { TimeUtil.logSubDirTime() }
        assertDoesNotThrow { TimeUtil.logTime() }
        assertDoesNotThrow { TimeUtil.getLogLineTime() }
        assertDoesNotThrow { TimeUtil.notificationTime() }
        assertDoesNotThrow { TimeUtil.userTime() }
        assertDoesNotThrow { TimeUtil.consoleSecondTime() }
        assertDoesNotThrow { TimeUtil.consoleNoSecondTime() }

        assertThrows(NullPointerException::class.java) { TimeUtil.getTime(null) }
        assertThrows(IllegalArgumentException::class.java) { TimeUtil.getTime("") }
    }

    /**
     * Tests for the is special day method.
     */
    @Test
    fun testIsSpecialDay() {
        assertThrows(NullPointerException::class.java) { TimeUtil.isSpecialDay(null, null) }
        assertThrows(NullPointerException::class.java) { TimeUtil.isSpecialDay(MonthDay.TODAY, null) }

        assertTrue(MonthDay(1, 1).isSpecialDay(SpecialDay.NEW_YEARS_DAY))
        assertFalse(MonthDay(1, 2).isSpecialDay(SpecialDay.NEW_YEARS_DAY))

        assertTrue(MonthDay(2, 2).isSpecialDay(SpecialDay.GROUND_HOG_DAY))
        assertFalse(MonthDay(2, 3).isSpecialDay(SpecialDay.GROUND_HOG_DAY))

        assertTrue(MonthDay(7, 4).isSpecialDay(SpecialDay.INDEPENDENCE_DAY))
        assertFalse(MonthDay(7, 3).isSpecialDay(SpecialDay.INDEPENDENCE_DAY))

        assertTrue(MonthDay(10, 31).isSpecialDay(SpecialDay.HALLOWEEN))
        assertFalse(MonthDay(10, 30).isSpecialDay(SpecialDay.HALLOWEEN))

        assertTrue(MonthDay(12, 25).isSpecialDay(SpecialDay.CHRISTMAS))
        assertFalse(MonthDay(12, 26).isSpecialDay(SpecialDay.CHRISTMAS))
    }

    /**
     * Tests for the is christmas method.
     */
    @Test
    fun testIsChristmas() {
        assertDoesNotThrow { TimeUtil.isChristmas() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isChristmas(null) }
        assertTrue(TimeUtil.isChristmas(MonthDay(12, 25)))
        assertFalse(TimeUtil.isChristmas(MonthDay(12, 26)))
    }

    /**
     * Tests for the is halloween method.
     */
    @Test
    fun testIsHalloween() {
        assertDoesNotThrow { TimeUtil.isHalloween() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isHalloween(null) }
        assertTrue(TimeUtil.isHalloween(MonthDay(10, 31)))
        assertFalse(TimeUtil.isHalloween(MonthDay(10, 30)))
    }

    /**
     * Tests for the is independence day.
     */
    @Test
    fun testIsIndependenceDay() {
        assertDoesNotThrow { TimeUtil.isIndependenceDay() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isIndependenceDay(null) }
        assertTrue(TimeUtil.isIndependenceDay(MonthDay(7, 4)))
        assertFalse(TimeUtil.isIndependenceDay(MonthDay(7, 5)))
    }

    /**
     * Tests for the is valentines day.
     */
    @Test
    fun testIsValentinesDay() {
        assertDoesNotThrow { TimeUtil.isValentinesDay() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isValentinesDay(null) }
        assertTrue(TimeUtil.isValentinesDay(MonthDay(2, 14)))
        assertFalse(TimeUtil.isValentinesDay(MonthDay(2, 15)))
    }

    /**
     * Tests for the is thanksgiving method.
     */
    @Test
    fun testIsThanksgiving() {
        assertDoesNotThrow { TimeUtil.isThanksgiving() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isThanksgiving(null) }

        // Earliest possible thanksgiving is Nov 22, latest is Nov 28
        IntStream.range(1, 22).forEach { assertFalse(TimeUtil.isThanksgiving(MonthDay(11, it))) }
        assertFalse(TimeUtil.isThanksgiving(MonthDay(11, 29)))
        assertFalse(TimeUtil.isThanksgiving(MonthDay(11, 30)))

        assertEquals(MonthDay(11, 23), TimeUtil.getThanksgiving(2023))
        assertEquals(MonthDay(11, 28), TimeUtil.getThanksgiving(2024))
        assertEquals(MonthDay(11, 27), TimeUtil.getThanksgiving(2025))
        assertEquals(MonthDay(11, 26), TimeUtil.getThanksgiving(2026))
        assertEquals(MonthDay(11, 25), TimeUtil.getThanksgiving(2027))
        assertEquals(MonthDay(11, 23), TimeUtil.getThanksgiving(2028))
        assertEquals(MonthDay(11, 22), TimeUtil.getThanksgiving(2029))
        assertEquals(MonthDay(11, 28), TimeUtil.getThanksgiving(2030))
    }

    /**
     * Tests for the is April fools method.
     */
    @Test
    fun testIsAprilFools() {
        assertDoesNotThrow { TimeUtil.isAprilFoolsDay() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isAprilFoolsDay(null) }
        assertTrue(TimeUtil.isAprilFoolsDay(MonthDay(4, 1)))
        assertFalse(TimeUtil.isAprilFoolsDay(MonthDay(4, 2)))
    }

    /**
     * Tests for the is April fools method.
     */
    @Test
    fun testIsPiDay() {
        assertDoesNotThrow { TimeUtil.isPiDay() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isPiDay(null) }
        assertTrue(TimeUtil.isPiDay(MonthDay(3, 14)))
        assertFalse(TimeUtil.isPiDay(MonthDay(3, 15)))
    }

    /**
     * Tests for the is Easter method.
     */
    @Test
    fun testIsEaster() {
        assertDoesNotThrow { TimeUtil.isEaster() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isEaster(null) }

        // Easter is between March 22 and April 25
        IntStream.of(1, 21).forEach { assertFalse(TimeUtil.isEaster(MonthDay(3, it))) }
        assertFalse(TimeUtil.isEaster(MonthDay(4, 26)))
        assertFalse(TimeUtil.isEaster(MonthDay(4, 27)))
        assertFalse(TimeUtil.isEaster(MonthDay(4, 28)))
        assertFalse(TimeUtil.isEaster(MonthDay(4, 29)))
        assertFalse(TimeUtil.isEaster(MonthDay(4, 30)))

        assertEquals(MonthDay(4, 9), TimeUtil.getEasterSundayDate(2023))
        assertEquals(MonthDay(3, 31), TimeUtil.getEasterSundayDate(2024))
        assertEquals(MonthDay(4, 20), TimeUtil.getEasterSundayDate(2025))
        assertEquals(MonthDay(4, 5), TimeUtil.getEasterSundayDate(2026))
        assertEquals(MonthDay(3, 28), TimeUtil.getEasterSundayDate(2027))
        assertEquals(MonthDay(4, 16), TimeUtil.getEasterSundayDate(2028))
        assertEquals(MonthDay(4, 1), TimeUtil.getEasterSundayDate(2029))
        assertEquals(MonthDay(4, 21), TimeUtil.getEasterSundayDate(2030))
    }
}