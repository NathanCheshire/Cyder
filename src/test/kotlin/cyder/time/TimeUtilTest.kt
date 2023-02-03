package cyder.time

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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
}