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
        assertDoesNotThrow { TimeUtil.userReadableTime() }
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

    /**
     * Tests for the is new years day method.
     */
    @Test
    fun testIsNewYearsDay() {
        assertDoesNotThrow { TimeUtil.isNewYearsDay() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isNewYearsDay(null) }
        assertTrue(TimeUtil.isNewYearsDay(MonthDay(1, 1)))
        assertFalse(TimeUtil.isNewYearsDay(MonthDay(1, 2)))
    }

    /**
     * Tests for the is ground hog day method.
     */
    @Test
    fun testIsGroundHogDay() {
        assertDoesNotThrow { TimeUtil.isGroundHogDay() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isGroundHogDay(null) }
        assertTrue(TimeUtil.isGroundHogDay(MonthDay(2, 2)))
        assertFalse(TimeUtil.isGroundHogDay(MonthDay(2, 3)))
    }

    /**
     * Tests for the is Mardi Grass day method.
     */
    @Test
    fun testIsMardiGrass() {
        assertDoesNotThrow { TimeUtil.isMardiGrassDay() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isMardiGrassDay(null) }

        assertEquals(MonthDay(3, 1), TimeUtil.getMardiGrassDay(2022))
        assertEquals(MonthDay(2, 21), TimeUtil.getMardiGrassDay(2023))
        assertEquals(MonthDay(2, 13), TimeUtil.getMardiGrassDay(2024))
        assertEquals(MonthDay(3, 4), TimeUtil.getMardiGrassDay(2025))
        assertEquals(MonthDay(2, 17), TimeUtil.getMardiGrassDay(2026))
        assertEquals(MonthDay(2, 9), TimeUtil.getMardiGrassDay(2027))
        assertEquals(MonthDay(2, 29), TimeUtil.getMardiGrassDay(2028))
        assertEquals(MonthDay(2, 13), TimeUtil.getMardiGrassDay(2029))
        assertEquals(MonthDay(3, 5), TimeUtil.getMardiGrassDay(2030))
    }

    /**
     * Tests for the is labor day method.
     */
    @Test
    fun testIsLaborDay() {
        assertDoesNotThrow { TimeUtil.isLaborDay() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isLaborDay(null) }

        // Labor day can fall on Sep 1-7
        IntStream.range(8, 32).forEach { assertFalse(TimeUtil.isLaborDay(MonthDay(8, it))) }

        assertEquals(MonthDay(9, 4), TimeUtil.getLaborDay(2023))
        assertEquals(MonthDay(9, 2), TimeUtil.getLaborDay(2024))
        assertEquals(MonthDay(9, 1), TimeUtil.getLaborDay(2025))
        assertEquals(MonthDay(9, 7), TimeUtil.getLaborDay(2026))
        assertEquals(MonthDay(9, 6), TimeUtil.getLaborDay(2027))
        assertEquals(MonthDay(9, 4), TimeUtil.getLaborDay(2028))
        assertEquals(MonthDay(9, 3), TimeUtil.getLaborDay(2029))
        assertEquals(MonthDay(9, 2), TimeUtil.getLaborDay(2030))
    }

    /**
     * Test for the is memorial day method.
     */
    @Test
    fun testIsMemorialDay() {
        assertDoesNotThrow { TimeUtil.isMemorialDay() }
        assertThrows(NullPointerException::class.java) { TimeUtil.isMemorialDay(null) }

        // Memorial day can fall on May 25 - 31
        IntStream.range(1, 25).forEach { assertFalse(TimeUtil.isMemorialDay(MonthDay(5, it))) }

        assertEquals(MonthDay(5, 29), TimeUtil.getMemorialDay(2023))
        assertEquals(MonthDay(5, 27), TimeUtil.getMemorialDay(2024))
        assertEquals(MonthDay(5, 26), TimeUtil.getMemorialDay(2025))
        assertEquals(MonthDay(5, 25), TimeUtil.getMemorialDay(2026))
        assertEquals(MonthDay(5, 31), TimeUtil.getMemorialDay(2027))
        assertEquals(MonthDay(5, 29), TimeUtil.getMemorialDay(2028))
        assertEquals(MonthDay(5, 28), TimeUtil.getMemorialDay(2029))
        assertEquals(MonthDay(5, 27), TimeUtil.getMemorialDay(2030))
    }

    /**
     * Tests for the get current year method.
     */
    @Test
    fun testGetCurrentYear() {
        assertDoesNotThrow { TimeUtil.getCurrentYear() }
        assertEquals(2023, TimeUtil.getCurrentYear())
    }

    /**
     * Tests for the get easter sunday string method.
     */
    @Test
    fun testGetEasterSundayString() {
        assertDoesNotThrow { TimeUtil.getEasterSundayString() }
        assertEquals("April 9th", TimeUtil.getEasterSundayString())
    }

    /**
     * Tests for the format number suffix method.
     */
    @Test
    fun testFormatNumberSuffix() {
        assertEquals("-1000th", TimeUtil.formatNumberSuffix(-1000))
        assertEquals("-500th", TimeUtil.formatNumberSuffix(-500))
        assertEquals("-515th", TimeUtil.formatNumberSuffix(-515))
        assertEquals("-100th", TimeUtil.formatNumberSuffix(-100))
        assertEquals("-55th", TimeUtil.formatNumberSuffix(-55))
        assertEquals("-1st", TimeUtil.formatNumberSuffix(-1))
        assertEquals("0th", TimeUtil.formatNumberSuffix(0))
        assertEquals("1st", TimeUtil.formatNumberSuffix(1))
        assertEquals("2nd", TimeUtil.formatNumberSuffix(2))
        assertEquals("3rd", TimeUtil.formatNumberSuffix(3))
        assertEquals("4th", TimeUtil.formatNumberSuffix(4))
        assertEquals("5th", TimeUtil.formatNumberSuffix(5))
        assertEquals("6th", TimeUtil.formatNumberSuffix(6))
        assertEquals("7th", TimeUtil.formatNumberSuffix(7))
        assertEquals("8th", TimeUtil.formatNumberSuffix(8))
        assertEquals("9th", TimeUtil.formatNumberSuffix(9))
        assertEquals("10th", TimeUtil.formatNumberSuffix(10))
        assertEquals("11th", TimeUtil.formatNumberSuffix(11))
        assertEquals("12th", TimeUtil.formatNumberSuffix(12))
        assertEquals("13th", TimeUtil.formatNumberSuffix(13))
        assertEquals("14th", TimeUtil.formatNumberSuffix(14))
        assertEquals("15th", TimeUtil.formatNumberSuffix(15))
        assertEquals("16th", TimeUtil.formatNumberSuffix(16))
        assertEquals("17th", TimeUtil.formatNumberSuffix(17))
        assertEquals("18th", TimeUtil.formatNumberSuffix(18))
        assertEquals("19th", TimeUtil.formatNumberSuffix(19))
        assertEquals("20th", TimeUtil.formatNumberSuffix(20))
        assertEquals("21st", TimeUtil.formatNumberSuffix(21))
        assertEquals("22nd", TimeUtil.formatNumberSuffix(22))
        assertEquals("23rd", TimeUtil.formatNumberSuffix(23))
    }

    /**
     * Tests for the format millis method.
     */
    @Test
    fun testFormatMillis() {
        assertThrows(IllegalArgumentException::class.java) { TimeUtil.formatMillis(-100) }
        assertThrows(IllegalArgumentException::class.java) { TimeUtil.formatMillis(-1) }

        assertEquals("0ms", TimeUtil.formatMillis(0))
        assertEquals("1ms", TimeUtil.formatMillis(1))
        assertEquals("1s", TimeUtil.formatMillis(1000))
        assertEquals("1s 101ms", TimeUtil.formatMillis(1101))
        assertEquals("1s 11ms", TimeUtil.formatMillis(1011))
        assertEquals("1s 500ms", TimeUtil.formatMillis(1500))
        assertEquals("11s 567ms", TimeUtil.formatMillis(11567))
        assertEquals("2m 45s 534ms", TimeUtil.formatMillis(165534))
        assertEquals("2m 42s 324ms", TimeUtil.formatMillis(162324))
        assertEquals("9m 52s 929ms", TimeUtil.formatMillis(592929))
        assertEquals("13m 12s 929ms", TimeUtil.formatMillis(792929))
        assertEquals("2h 12m 9s 290ms", TimeUtil.formatMillis(7929290))
        assertEquals("2h 12m 9s 294ms", TimeUtil.formatMillis(7929294))
        assertEquals("22h 1m 32s 900ms", TimeUtil.formatMillis(79292900))
        assertEquals("9d 4h 15m 29s 1ms", TimeUtil.formatMillis(792929001))
        assertEquals("3mo 1d 18h 34m 50s 19ms", TimeUtil.formatMillis(7929290019))
        assertEquals("2y 6mo 17d 17h 48m 20s 196ms", TimeUtil.formatMillis(79292900196))
        assertEquals("25y 5mo 27d 10h 3m 21s 960ms", TimeUtil.formatMillis(792929001960))
    }

    /**
     * Tests for the millis to seconds method.
     */
    @Test
    fun testMillisToSeconds() {
        assertEquals(0.0, TimeUtil.millisToSeconds(0))
        assertEquals(0.001, TimeUtil.millisToSeconds(1))
        assertEquals(0.002, TimeUtil.millisToSeconds(2))
        assertEquals(0.005, TimeUtil.millisToSeconds(5))
        assertEquals(0.5, TimeUtil.millisToSeconds(500))
        assertEquals(1.0, TimeUtil.millisToSeconds(1000))
        assertEquals(1.5, TimeUtil.millisToSeconds(1500))
        assertEquals(5.0, TimeUtil.millisToSeconds(5000))
        assertEquals(10.0, TimeUtil.millisToSeconds(10000))
        assertEquals(50.642, TimeUtil.millisToSeconds(50642))
        assertEquals(5034.534, TimeUtil.millisToSeconds(5034534))
    }
}