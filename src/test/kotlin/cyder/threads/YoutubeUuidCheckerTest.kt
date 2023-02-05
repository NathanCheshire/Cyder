package cyder.threads

import cyder.ui.pane.CyderOutputPane
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.swing.JTextPane

/**
 * Tests for a [YoutubeUuidChecker].
 */
class YoutubeUuidCheckerTest {
    /**
     * Tests for creation of youtube uuid checkers.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { YoutubeUuidChecker(null) }

        assertDoesNotThrow { YoutubeUuidChecker(CyderOutputPane(JTextPane())) }
    }

    /**
     * Tests for the core functionality of a [YoutubeUuidChecker].
     */
    @Test
    fun testStartChecking() {
        val checker = YoutubeUuidChecker(CyderOutputPane(JTextPane()))
        assertFalse(checker.isKilled)

        checker.startChecking()
        assertFalse(checker.isKilled)

        checker.kill()
        assertTrue(checker.isKilled)
    }

    /**
     * Tests for the increment uuid function.
     */
    @Test
    @Suppress("SpellCheckingInspection")
    fun testIncrementUuid() {
        assertThrows(NullPointerException::class.java) { YoutubeUuidChecker.incrementUuid(null) }
        assertThrows(IllegalArgumentException::class.java) {
            YoutubeUuidChecker.incrementUuid("".toCharArray())
        }

        assertEquals("aaaaaaaaaab", String(YoutubeUuidChecker.incrementUuid("aaaaaaaaaaa".toCharArray())))
        assertEquals("aaaaaaaaaah", String(YoutubeUuidChecker.incrementUuid("aaaaaaaaaag".toCharArray())))
        assertEquals("aaaaaaaaaaz", String(YoutubeUuidChecker.incrementUuid("aaaaaaaaaay".toCharArray())))
        assertEquals("aaaaaaaaaaA", String(YoutubeUuidChecker.incrementUuid("aaaaaaaaaaz".toCharArray())))
        assertEquals("aaaaaaaaaba", String(YoutubeUuidChecker.incrementUuid("aaaaaaaaaa_".toCharArray())))
        assertEquals("aaaaaaaaba_", String(YoutubeUuidChecker.incrementUuid("aaaaaaaaa__".toCharArray())))
        assertEquals("aaaaaaba___", String(YoutubeUuidChecker.incrementUuid("aaaaaaa____".toCharArray())))
        assertEquals("___________", String(YoutubeUuidChecker.incrementUuid("__________-".toCharArray())))

        assertThrows(IllegalArgumentException::class.java) {
            String(YoutubeUuidChecker.incrementUuid("___________".toCharArray()))
        }
    }

    /**
     * Tests for the find char index function.
     */
    @Test
    fun testFindCharIndex() {
        assertEquals(0, YoutubeUuidChecker.findCharIndex('a'))
        assertEquals(25, YoutubeUuidChecker.findCharIndex('z'))
        assertEquals(26, YoutubeUuidChecker.findCharIndex('A'))
        assertEquals(51, YoutubeUuidChecker.findCharIndex('Z'))
        assertEquals(52, YoutubeUuidChecker.findCharIndex('0'))
        assertEquals(61, YoutubeUuidChecker.findCharIndex('9'))
        assertEquals(62, YoutubeUuidChecker.findCharIndex('-'))
        assertEquals(63, YoutubeUuidChecker.findCharIndex('_'))
        assertEquals(-1, YoutubeUuidChecker.findCharIndex('$'))
    }

    /**
     * Tests for the construct thumbnail url method.
     */
    @Test
    fun testConstructThumbnailUrl() {
        val checker = YoutubeUuidChecker(CyderOutputPane(JTextPane()))
        checker.startChecking()
        ThreadUtil.sleepSeconds(5)
        checker.kill()

        checker.checkedUuids.stream().forEach {
            assertEquals("https://img.youtube.com/vi/${it}/hqdefault.jpg",
                    YoutubeUuidChecker.constructThumbnailUrl(it))
        }
    }

    /**
     * Tests for the data accessor methods.
     */
    @Test
    fun testDataAccessors() {
        val sleepTime = 5L
        val checker = YoutubeUuidChecker(CyderOutputPane(JTextPane()))
        checker.startChecking()
        ThreadUtil.sleepSeconds(sleepTime)
        println("Checks: " + checker.checkedUuids.size)
        println("Rate: " + checker.currentChecksPerSecond)
        checker.kill()

        assertTrue(checker.checkedUuids.size * sleepTime >= checker.currentChecksPerSecond)
    }
}