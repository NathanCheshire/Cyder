package cyder.time

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
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
        assertThrows(NullPointerException::class.java) { TimeUtil.getTime("") }
    }
}