package cyder.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for [TimeUnit]s.
 */
class TimeUnitTest {
    /**
     * Tests for the to millis method.
     */
    @Test
    fun testToMillis() {
        assertEquals(0, TimeUnit.MILLISECONDS.toMillis(0))
        assertEquals(1000, TimeUnit.MILLISECONDS.toMillis(1000))

        assertEquals(0, TimeUnit.SECONDS.toMillis(0))
        assertEquals(1000, TimeUnit.SECONDS.toMillis(1))
        assertEquals(2000, TimeUnit.SECONDS.toMillis(2))
        assertEquals(5000, TimeUnit.SECONDS.toMillis(5))

        assertEquals(0, TimeUnit.MINUTES.toMillis(0))
        assertEquals(60000, TimeUnit.MINUTES.toMillis(1))
        assertEquals(120000, TimeUnit.MINUTES.toMillis(2))
        assertEquals(300000, TimeUnit.MINUTES.toMillis(5))

        assertEquals(0, TimeUnit.HOURS.toMillis(0))
        assertEquals(3600000, TimeUnit.HOURS.toMillis(1))
        assertEquals(7200000, TimeUnit.HOURS.toMillis(2))
        assertEquals(18000000, TimeUnit.HOURS.toMillis(5))

        assertEquals(0, TimeUnit.DAYS.toMillis(0))
        assertEquals(86400000, TimeUnit.DAYS.toMillis(1))
        assertEquals(172800000, TimeUnit.DAYS.toMillis(2))
        assertEquals(432000000, TimeUnit.DAYS.toMillis(5))

        assertEquals(0, TimeUnit.WEEKS.toMillis(0))
        assertEquals(604800000, TimeUnit.WEEKS.toMillis(1))
        assertEquals(1209600000, TimeUnit.WEEKS.toMillis(2))
        assertEquals(3024000000, TimeUnit.WEEKS.toMillis(5))

        assertEquals(0, TimeUnit.MONTHS.toMillis(0))
        assertEquals(2592000000, TimeUnit.MONTHS.toMillis(1))
        assertEquals(5184000000, TimeUnit.MONTHS.toMillis(2))
        assertEquals(12960000000, TimeUnit.MONTHS.toMillis(5))

        assertEquals(0, TimeUnit.YEARS.toMillis(0))
        assertEquals(31536000000, TimeUnit.YEARS.toMillis(1))
        assertEquals(63072000000, TimeUnit.YEARS.toMillis(2))
        assertEquals(157680000000, TimeUnit.YEARS.toMillis(5))
    }

    /**
     * Tests for the from millis method.
     */
    @Test
    fun testFromMillis() {
        assertEquals(0, TimeUnit.MILLISECONDS.fromMillis(0))
        assertEquals(1000, TimeUnit.MILLISECONDS.fromMillis(1000))

        assertEquals(0, TimeUnit.SECONDS.fromMillis(0))
        assertEquals(1, TimeUnit.SECONDS.fromMillis(1000))
        assertEquals(2, TimeUnit.SECONDS.fromMillis(2000))
        assertEquals(5, TimeUnit.SECONDS.fromMillis(5000))

        assertEquals(0, TimeUnit.MINUTES.fromMillis(0))
        assertEquals(1, TimeUnit.MINUTES.fromMillis(60000))
        assertEquals(2, TimeUnit.MINUTES.fromMillis(120000))
        assertEquals(5, TimeUnit.MINUTES.fromMillis(300000))

        assertEquals(0, TimeUnit.HOURS.fromMillis(0))
        assertEquals(1, TimeUnit.HOURS.fromMillis(3600000))
        assertEquals(2, TimeUnit.HOURS.fromMillis(7200000))
        assertEquals(5, TimeUnit.HOURS.fromMillis(18000000))

        assertEquals(0, TimeUnit.DAYS.fromMillis(0))
        assertEquals(1, TimeUnit.DAYS.fromMillis(86400000))
        assertEquals(2, TimeUnit.DAYS.fromMillis(172800000))
        assertEquals(5, TimeUnit.DAYS.fromMillis(432000000))

        assertEquals(0, TimeUnit.WEEKS.fromMillis(0))
        assertEquals(1, TimeUnit.WEEKS.fromMillis(604800000))
        assertEquals(2, TimeUnit.WEEKS.fromMillis(1209600000))
        assertEquals(5, TimeUnit.WEEKS.fromMillis(3024000000))

        assertEquals(0, TimeUnit.MONTHS.fromMillis(0))
        assertEquals(1, TimeUnit.MONTHS.fromMillis(2592000000))
        assertEquals(2, TimeUnit.MONTHS.fromMillis(5184000000))
        assertEquals(5, TimeUnit.MONTHS.fromMillis(12960000000))

        assertEquals(0, TimeUnit.YEARS.fromMillis(0))
        assertEquals(1, TimeUnit.YEARS.fromMillis(31536000000))
        assertEquals(2, TimeUnit.YEARS.fromMillis(63072000000))
        assertEquals(5, TimeUnit.YEARS.fromMillis(157680000000))
    }
}