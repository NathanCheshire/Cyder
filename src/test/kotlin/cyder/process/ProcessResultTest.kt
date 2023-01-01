package cyder.process

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Tests for [ProcessResult]s.
 */
class ProcessResultTest {
    /**
     * Tests for creation of process results.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { ProcessResult(null, null) }
        assertThrows(NullPointerException::class.java) { ProcessResult(ArrayList(), null) }

        assertDoesNotThrow { ProcessResult(ArrayList(), ArrayList()) }
    }

    /**
     * Tests for the accessors and mutators.
     */
    @Test
    fun testAccessorMutator() {
        val emptyProcessResult = ProcessResult(ImmutableList.of(), ImmutableList.of())
        assertTrue(emptyProcessResult.standardOutput.isEmpty())
        assertTrue(emptyProcessResult.errorOutput.isEmpty())
        assertFalse(emptyProcessResult.hasErrors())

        val filledProcessResult = ProcessResult(ImmutableList.of("Alpha", "Beta", "Gamma"),
                ImmutableList.of("Delta", "Epsilon", "Zeta"))
        assertEquals(3, filledProcessResult.standardOutput.size)
        assertEquals(ImmutableList.of("Alpha", "Beta", "Gamma"), filledProcessResult.standardOutput)
        assertEquals(3, filledProcessResult.errorOutput.size)
        assertEquals(ImmutableList.of("Delta", "Epsilon", "Zeta"), filledProcessResult.errorOutput)
        assertTrue(filledProcessResult.hasErrors())
    }

    /**
     * Tests for the equals method.
     */
    @Test
    fun testEquals() {
        val first = ProcessResult(ImmutableList.of("First"), ImmutableList.of("Second"))
        val second = ProcessResult(ImmutableList.of("First"), ImmutableList.of("Second"))
        val third = ProcessResult(ImmutableList.of("First"), ImmutableList.of("Not Second"))

        assertEquals(first, second)
        assertEquals(second, first)
        assertNotEquals(first, third)
        assertNotEquals(second, third)
    }

    /**
     * Tests for the hash code method.
     */
    @Test
    fun testHashCode() {
        val first = ProcessResult(ImmutableList.of("First"), ImmutableList.of("Second"))
        val second = ProcessResult(ImmutableList.of("First"), ImmutableList.of("Second"))
        val third = ProcessResult(ImmutableList.of("First"), ImmutableList.of("Not Second"))

        assertEquals(first.hashCode(), second.hashCode())
        assertEquals(second.hashCode(), first.hashCode())
        assertNotEquals(first.hashCode(), third.hashCode())
        assertNotEquals(second.hashCode(), third.hashCode())
    }

    /**
     * Tests for the to string method.
     */
    @Test
    fun testToString() {
        val first = ProcessResult(ImmutableList.of("First"), ImmutableList.of("Second"))
        val second = ProcessResult(ImmutableList.of("First"), ImmutableList.of("Second"))
        val third = ProcessResult(ImmutableList.of("First"), ImmutableList.of("Not Second"))

        assertEquals("ProcessResult{standardOutput=[First], errorOutput=[Second]}", first.toString())
        assertEquals("ProcessResult{standardOutput=[First], errorOutput=[Second]}", second.toString())
        assertEquals("ProcessResult{standardOutput=[First], errorOutput=[Not Second]}", third.toString())
    }
}