package cyder.logging

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for [LoggingUtil] methods.
 */
class LoggingUtilTest {
    /**
     * Tests for the are log lines equivalent method.
     */
    @Test
    fun testAreLogLinesEquivalent() {
        assertThrows(NullPointerException::class.java) { LoggingUtil.areLogLinesEquivalent(null, null) }
        assertThrows(NullPointerException::class.java) { LoggingUtil.areLogLinesEquivalent("", null) }

        assertTrue(LoggingUtil.areLogLinesEquivalent("", ""))
        assertTrue(LoggingUtil.areLogLinesEquivalent("\t\t", "\t\t"))
        assertTrue(LoggingUtil.areLogLinesEquivalent("\n", "\n"))
        assertTrue(LoggingUtil.areLogLinesEquivalent("\n\n", "\n\n"))

        assertTrue(LoggingUtil.areLogLinesEquivalent("random line", "random line"))
        assertFalse(LoggingUtil.areLogLinesEquivalent("random line", "random line two"))

        assertTrue(LoggingUtil.areLogLinesEquivalent("[24-24-24.111]", "[25-25-25.345]"))
        assertTrue(LoggingUtil.areLogLinesEquivalent("[24-44-22.111]", "[25-26-48.345]"))
        assertTrue(LoggingUtil.areLogLinesEquivalent("[24-24-24.111] [tag1]", "[25-25-25.345] [tag1]"))
        assertTrue(LoggingUtil.areLogLinesEquivalent("[24-24-24.111] [tag1] [tag2]:",
                "[25-25-25.345] [tag1] [tag2]:"))
        assertTrue(LoggingUtil.areLogLinesEquivalent("[24-24-24.111] [tag1] [tag2]: content",
                "[25-25-25.345] [tag1] [tag2]: content"))

        assertFalse(LoggingUtil.areLogLinesEquivalent("[24-24-24.111] [tag1] [tag2]:",
                "[25-25-25.345] [tag1] [tag2]: content"))
        assertFalse(LoggingUtil.areLogLinesEquivalent("[24-24-24.111] [tag1] [tag2]: content",
                "[25-25-25.345] [tag1] [tag2]: more content"))
    }

    /**
     * Tests for the matches standard log line method.
     */
    @Test
    fun testMatchesStandardLogLine() {
        assertThrows(NullPointerException::class.java) { LoggingUtil.matchesStandardLogLine(null) }
        assertFalse(LoggingUtil.matchesStandardLogLine(""))
        assertFalse(LoggingUtil.matchesStandardLogLine("content"))
        assertFalse(LoggingUtil.matchesStandardLogLine("[asdf-asdf-asdf.asdf] [tag]: content"))

        assertTrue(LoggingUtil.matchesStandardLogLine("[24-24-24.222]"))
        assertTrue(LoggingUtil.matchesStandardLogLine("[24-24-24.222]: "))
        assertTrue(LoggingUtil.matchesStandardLogLine("[24-24-24.222]: content"))
        assertTrue(LoggingUtil.matchesStandardLogLine("[24-24-24.222]: more content"))
        assertTrue(LoggingUtil.matchesStandardLogLine("[24-24-24.222] [tag1]: content"))
        assertTrue(LoggingUtil.matchesStandardLogLine("[24-24-24.222] [tag1] [tag2]: content"))
    }

    /**
     * Tests for the check log line length method.
     */
    @Test
    fun testCheckLogLineLength() {
        assertThrows(NullPointerException::class.java) { LoggingUtil.checkLogLineLength(null) }

        assertEquals(ImmutableList.of("line"), LoggingUtil.checkLogLineLength("line"))
        assertEquals(ImmutableList.of("longer line with spaces and such"),
                LoggingUtil.checkLogLineLength("longer line with spaces and such"))
        assertEquals(ImmutableList.of("longer line with spaces and such and much more length of course to try and"
                + " break it up, longer line with spaces and such", " and much more"
                + " length of course to try and break it up"),
                LoggingUtil.checkLogLineLength("longer line with spaces and such and much more length of course"
                        + " to try and break it up, longer line with spaces and such and"
                        + " much more length of course to try and break it up"))
    }
}