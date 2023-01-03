package cyder.strings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Tests for [LevenshteinUtil]s.
 */
class LevenshteinUtilTest {
    /**
     * Tests for the compute levenshtein distance method.
     */
    @Test
    fun testComputeLevenshteinDistance() {
        assertThrows(NullPointerException::class.java) {
            LevenshteinUtil.computeLevenshteinDistance(null, null)
        }
        assertThrows(NullPointerException::class.java) {
            LevenshteinUtil.computeLevenshteinDistance("", null)
        }

        assertEquals(4, LevenshteinUtil.computeLevenshteinDistance("alpha", "beta"))
        assertEquals(0, LevenshteinUtil.computeLevenshteinDistance("alpha", "alpha"))
        assertEquals(5, LevenshteinUtil.computeLevenshteinDistance("alpha", ""))
        assertEquals(1, LevenshteinUtil.computeLevenshteinDistance("Nathan", "nathan"))
        assertEquals(7, LevenshteinUtil.computeLevenshteinDistance("Nathan", "Cheshire"))
    }
}