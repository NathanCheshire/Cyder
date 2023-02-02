package cyder.threads

import cyder.ui.pane.CyderOutputPane
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
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

    }

    /**
     * Tests for teh increment uuid function.
     */
    @Test
    fun testIncrementUuid() {

    }

    /**
     * Tests for the find char index function.
     */
    @Test
    fun testFindCharIndex() {

    }
}