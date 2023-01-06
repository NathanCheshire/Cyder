package cyder.subroutines

import cyder.strings.LevenshteinUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for [Subroutine]s.
 */
class SubroutineTest {
    /**
     * Tests for creation of subroutines.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { Subroutine(null, null, null) }
        assertThrows(NullPointerException::class.java) { Subroutine({ false }, null, null) }
        assertThrows(IllegalArgumentException::class.java) { Subroutine({ false }, "", null) }
        assertThrows(NullPointerException::class.java) { Subroutine({ false }, "thread", null) }
        assertThrows(IllegalArgumentException::class.java) { Subroutine({ false }, "thread", "") }

        assertDoesNotThrow { Subroutine({ true }, "thread name", "failed") }
    }

    /**
     * Tests for the getter methods.
     */
    @Test
    fun testGetters() {
        val subroutine = Subroutine({ true }, "thread name", "failed")

        assertEquals(subroutine.threadName, "thread name")
        assertEquals(subroutine.onFailureMessage, "failed")
    }

    /**
     * Tests for the equals method.
     */
    @Test
    fun testEquals() {
        val supplier = { true }

        val subroutine = Subroutine(supplier, "thread name", "failed")
        val subroutine2 = Subroutine(supplier, "thread name", "failed")
        val subroutine3 = Subroutine(supplier, "other thread name", "failed")

        assertEquals(subroutine, subroutine2)
        assertEquals(subroutine2, subroutine)
        assertNotEquals(subroutine, subroutine3)
        assertNotEquals(subroutine2, subroutine3)
    }

    /**
     * Tests for the to string method.
     */
    @Test
    fun testToString() {
        val supplier = { true }

        val subroutine = Subroutine(supplier, "thread name", "failed")
        val subroutine2 = Subroutine(supplier, "thread name", "failed")
        val subroutine3 = Subroutine(supplier, "other thread name", "failed")

        assertEquals(subroutine.hashCode(), subroutine2.hashCode())
        assertEquals(subroutine2.hashCode(), subroutine.hashCode())
        assertNotEquals(subroutine.hashCode(), subroutine3.hashCode())
        assertNotEquals(subroutine2.hashCode(), subroutine3.hashCode())
    }

    /**
     * Tests for the hash code method.
     */
    @Test
    fun testHashCode() {
        val supplier = { true }

        val subroutine = Subroutine(supplier, "thread name", "failed")
        val subroutine2 = Subroutine(supplier, "thread name", "failed")
        val subroutine3 = Subroutine(supplier, "other thread name", "failed")

        assertTrue(LevenshteinUtil.computeLevenshteinDistance("Subroutine{routine=cyder.subroutines."
                + "SubroutineTest\$\$Lambda\$356/0x0000000800cab600@6b419da, threadName=\"thread name\", "
                + "onFailureMessage=failed}", subroutine.toString()) <= "0x0000000800cab600@6b419da".length)
        assertTrue(LevenshteinUtil.computeLevenshteinDistance("Subroutine{routine=cyder.subroutines."
                + "SubroutineTest\$\$Lambda\$356/0x0000000800cab600@6b419da, threadName=\"thread name\", "
                + "onFailureMessage=failed}", subroutine2.toString()) <= "0x0000000800cab600@6b419da".length)
        assertTrue(LevenshteinUtil.computeLevenshteinDistance("Subroutine{routine=cyder.subroutines."
                + "SubroutineTest\$\$Lambda\$356/0x0000000800cab600@6b419da, threadName=\"other thread name\", "
                + "onFailureMessage=failed}", subroutine3.toString()) <= "0x0000000800cab600@6b419da".length)
    }
}