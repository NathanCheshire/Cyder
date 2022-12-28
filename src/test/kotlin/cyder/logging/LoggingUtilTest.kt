package cyder.logging

import com.google.common.collect.ImmutableList
import cyder.files.FileUtil
import cyder.strings.LevenshteinUtil
import cyder.time.TimeUtil
import cyder.utils.OsUtil
import cyder.utils.StaticUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

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

    /**
     * Tests for the get log recovery line method.
     */
    @Test
    fun testGetLogRecoveryLine() {
        val recoveryLine = LoggingUtil.getLogRecoveryDebugLine()
        val baseline = ("[16-29-19.873] [Debug]: Log was deleted during runtime, recreating"
                + " and restarting log at: Tuesday, 12/27/2022 04:29PM CST")

        assertTrue(LevenshteinUtil.computeLevenshteinDistance(baseline, recoveryLine)
                < TimeUtil.userFormat.toPattern().length)
    }

    /**
     * Tests for the construct log tags prepend method.
     */
    @Test
    fun testConstructLogTagsPrepend() {
        assertThrows(NullPointerException::class.java) {
            LoggingUtil.constructTagsPrepend(null, null)
        }
        assertThrows(NullPointerException::class.java) {
            LoggingUtil.constructTagsPrepend(null, null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.constructTagsPrepend("", "", null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.constructTagsPrepend("", null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.constructTagsPrepend("tag", "", null)
        }
        assertThrows(NullPointerException::class.java) {
            LoggingUtil.constructTagsPrepend("tag", null, null)
        }

        val nullString: String? = null
        assertThrows(NullPointerException::class.java) {
            LoggingUtil.constructTagsPrepend(ImmutableList.of(nullString))
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.constructTagsPrepend(ImmutableList.of(""))
        }
        assertThrows(NullPointerException::class.java) {
            LoggingUtil.constructTagsPrepend(ImmutableList.of("tag", nullString))
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.constructTagsPrepend(ImmutableList.of("tag", ""))
        }
    }

    /**
     * Tests for the count exceptions method.
     */
    @Test
    fun testCountExceptions() {
        val exceptionsToWrite = 21

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val tmpFile = File("tmp/tmp.log")
        tmpFile.createNewFile()
        assertTrue(tmpFile.exists())

        BufferedWriter(FileWriter(tmpFile, false)).use {
            for (i in 0 until exceptionsToWrite) {
                it.write(LoggingUtil.constructTagsPrepend(LogTag.EXCEPTION.logName))
                it.newLine()
            }
        }

        assertEquals(exceptionsToWrite, LoggingUtil.countExceptions(tmpFile))
        assertTrue(OsUtil.deleteFile(tmpDir, false))
    }

    /**
     * Tests for the count threads ran method.
     */
    @Test
    fun testCountThreadsRan() {
        val threadsRanToWrite = 25

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val tmpFile = File("tmp/tmp.log")
        tmpFile.createNewFile()
        assertTrue(tmpFile.exists())

        BufferedWriter(FileWriter(tmpFile, false)).use {
            for (i in 0 until threadsRanToWrite) {
                it.write(LoggingUtil.constructTagsPrepend(LogTag.THREAD_STARTED.logName))
                it.newLine()
            }
        }

        assertEquals(threadsRanToWrite, LoggingUtil.countThreadsRan(tmpFile))
        assertTrue(OsUtil.deleteFile(tmpDir, false))
    }

    /**
     * Tests for the count tags method.
     */
    @Test
    fun testCountTags() {
        val exceptionsToWrite = 20
        val threadsRanToWrite = 25

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val tmpFile = File("tmp/tmp.log")
        tmpFile.createNewFile()
        assertTrue(tmpFile.exists())

        BufferedWriter(FileWriter(tmpFile, false)).use {
            for (i in 0 until threadsRanToWrite) {
                it.write(LoggingUtil.constructTagsPrepend(LogTag.THREAD_STARTED.logName))
                it.newLine()
            }
            for (i in 0 until exceptionsToWrite) {
                it.write(LoggingUtil.constructTagsPrepend(LogTag.EXCEPTION.logName))
                it.newLine()
            }
        }

        assertEquals(threadsRanToWrite, LoggingUtil.countTags(tmpFile, LogTag.THREAD_STARTED.logName))
        assertEquals(exceptionsToWrite, LoggingUtil.countTags(tmpFile, LogTag.EXCEPTION.logName))
        assertTrue(OsUtil.deleteFile(tmpDir, false))
    }

    /**
     * Tests for the extract lines with tag method.
     */
    @Test
    fun testExtractLinesWithTag() {
        assertThrows(NullPointerException::class.java) { LoggingUtil.extractLinesWithTag(null, null) }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.extractLinesWithTag(File("."), null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.extractLinesWithTag(File(".gitignore"), null)
        }

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val tmpFile = File("tmp/log.log")
        tmpFile.createNewFile()
        assertTrue(tmpFile.exists())

        assertThrows(NullPointerException::class.java) {
            LoggingUtil.extractLinesWithTag(tmpFile, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.extractLinesWithTag(tmpFile, "")
        }

        // Remaining functionality of method is tested via above methods
    }

    /**
     * Tests for the extract tags method.
     */
    @Test
    fun testExtractTags() {
        val emptyList: ImmutableList<String> = ImmutableList.of()

        assertThrows(NullPointerException::class.java) { LoggingUtil.extractTags(null) }
        assertEquals(emptyList, LoggingUtil.extractTags(""))
        assertEquals(ImmutableList.of("[234-234-234.111]"),
                LoggingUtil.extractTags("[234-234-234.111]:"))
        assertEquals(ImmutableList.of("[234-234-234.111]"),
                LoggingUtil.extractTags("[234-234-234.111]: content"))
        assertEquals(ImmutableList.of("[234-234-234.111]"),
                LoggingUtil.extractTags("[234-234-234.111]: content [not a tag] [not a tag 2]"))
        assertEquals(ImmutableList.of("[234-234-234.111]", "[tag1]"),
                LoggingUtil.extractTags("[234-234-234.111] [tag1]: content"))

        assertEquals(ImmutableList.of("[234-234-234.111]", "[tag1]", "[tag2]"),
                LoggingUtil.extractTags("[234-234-234.111] [tag1] [tag2]: content"))
        assertEquals(ImmutableList.of("[234-234-234.111]", "[tag1]", "[tag2]", "[tag3]"),
                LoggingUtil.extractTags("[234-234-234.111] [tag1] [tag2] [tag3]: content"))
    }

    /**
     * Tests for the generate consolidation line method.
     */
    @Test
    fun testGenerateConsolidationLine() {
        assertThrows(NullPointerException::class.java) {
            LoggingUtil.generateConsolidationLine(null, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.generateConsolidationLine("", 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.generateConsolidationLine("Line", 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.generateConsolidationLine("Line", -1)
        }

        assertEquals("Line [2x]", LoggingUtil.generateConsolidationLine("Line", 2))
        assertEquals("Line [44x]", LoggingUtil.generateConsolidationLine("Line", 44))
        assertEquals("Line [" + Int.MAX_VALUE + "x]",
                LoggingUtil.generateConsolidationLine("Line", Int.MAX_VALUE))
        assertEquals("[time] [tag] [" + Int.MAX_VALUE + "x]: content",
                LoggingUtil.generateConsolidationLine("[time] [tag]: content", Int.MAX_VALUE))
    }

    /**
     * Tests for the write cyder ascii art to file.
     */
    @Test
    fun testWriteCyderAsciiArtToFile() {
        assertThrows(NullPointerException::class.java) { LoggingUtil.writeCyderAsciiArtToFile(null) }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.writeCyderAsciiArtToFile(File("file_that_does_not_exist"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.writeCyderAsciiArtToFile(File("src"))
        }

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val tmpFile = File("tmp/file.txt")
        tmpFile.createNewFile()
        assertTrue(tmpFile.exists())

        assertDoesNotThrow { LoggingUtil.writeCyderAsciiArtToFile(tmpFile) }

        val writtenFileLines: ImmutableList<String> = FileUtil.getFileLines(tmpFile)
        var truthLines: ImmutableList<String> = FileUtil.getFileLines(StaticUtil.getStaticResource("cyder.txt"))
        truthLines = ImmutableList.Builder<String>()
                .addAll(truthLines)
                .add("")
                .add("").build()

        assertEquals(truthLines, writtenFileLines)
        assertTrue(OsUtil.deleteFile(tmpDir, false))
    }

    /**
     * Tests for the count objects created from log method.
     */
    @Test
    fun testCountObjectsCreatedFromLog() {
        val objectsCreated = 69

        assertThrows(NullPointerException::class.java) { LoggingUtil.countObjectsCreatedFromLog(null) }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.countObjectsCreatedFromLog(File("file_that_does_not_exist"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.countObjectsCreatedFromLog(File("."))
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.countObjectsCreatedFromLog(File(".gitignore"))
        }

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val tmpFile = File("tmp/file.log")
        tmpFile.createNewFile()
        assertTrue(tmpFile.exists())

        val objectsCreatedToWrite = 6
        BufferedWriter(FileWriter(tmpFile, false)).use {
            for (i in 0 until objectsCreated) {
                it.write("[18-09-15.142] [Object Creation]: Objects created"
                        + " since last delta (5000ms): $objectsCreatedToWrite")
                it.newLine()
            }
        }

        assertEquals((objectsCreated * objectsCreatedToWrite).toLong(),
                LoggingUtil.countObjectsCreatedFromLog(tmpFile))
        assertTrue(OsUtil.deleteFile(tmpFile, false))
    }

    /**
     * Tests for the get runtime from log method.
     */
    @Test
    fun testGetRuntimeFromLog() {
        assertThrows(NullPointerException::class.java) { LoggingUtil.getRuntimeFromLog(null) }
        assertThrows(IllegalArgumentException::class.java) { LoggingUtil.getRuntimeFromLog(File("file_that_does_not_exist")) }
        assertThrows(IllegalArgumentException::class.java) { LoggingUtil.getRuntimeFromLog(File(".")) }
        assertThrows(IllegalArgumentException::class.java) { LoggingUtil.getRuntimeFromLog(File(".gitignore")) }

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val tmpFile = File("tmp/file.log")
        tmpFile.createNewFile()
        assertTrue(tmpFile.exists())

        BufferedWriter(FileWriter(tmpFile, false)).use {
            it.write("[18-30-30.000] [tag1]: content")
            it.newLine()
            it.write("[19-30-30.000] [tag1]: content")
            it.newLine()
        }

        assertEquals((TimeUtil.SECONDS_IN_HOUR * TimeUtil.MILLISECONDS_IN_SECOND).toLong(),
                LoggingUtil.getRuntimeFromLog(tmpFile))

        BufferedWriter(FileWriter(tmpFile, false)).use {
            it.write("[18-30-30.000] [tag1]: content")
            it.newLine()
            it.write("[20-30-30.500] [tag1]: content")
            it.newLine()
        }

        assertEquals((2 * TimeUtil.SECONDS_IN_HOUR * TimeUtil.MILLISECONDS_IN_SECOND + 500).toLong(),
                LoggingUtil.getRuntimeFromLog(tmpFile))
    }

    /**
     * Tests for the get log lines from log method.
     */
    @Test
    fun testGetLogLinesFromLog() {
        val numPrelinesToWrite = 8

        assertThrows(NullPointerException::class.java) { LoggingUtil.getLogLinesFromLog(null) }
        assertThrows(IllegalArgumentException::class.java) { LoggingUtil.getLogLinesFromLog(File(".")) }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.getLogLinesFromLog(File(".gitignore"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoggingUtil.getLogLinesFromLog(File("file_that_does_not_exist.txt"))
        }

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val tmpFile = File("tmp/file.log")
        tmpFile.createNewFile()
        assertTrue(tmpFile.exists())

        val logLines = ArrayList<String>()
        for (i in 0 until 40) {
            logLines.add("[00-00-00.000] [tag1] [tag2]: content $i")
        }

        BufferedWriter(FileWriter(tmpFile, false)).use {
            for (i in 0 until numPrelinesToWrite) {
                it.write("preline before first log line with time tag")
                it.newLine()
            }

            for (line in logLines) {
                it.write(line)
                it.newLine()
            }
        }

        assertEquals(logLines, LoggingUtil.getLogLinesFromLog(tmpFile))
        assertTrue(OsUtil.deleteFile(tmpDir, false))
    }
}