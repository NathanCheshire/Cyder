package cyder.getter

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Tests for [GetInputBuilder]s
 */
class GetInputBuilderTest {
    /**
     * Tests for creation of get input builders.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { GetInputBuilder(null, null) }
        assertThrows(IllegalArgumentException::class.java) { GetInputBuilder("", "") }
        assertThrows(NullPointerException::class.java) { GetInputBuilder("", null) }
        assertThrows(IllegalArgumentException::class.java) { GetInputBuilder("Title", "") }
        assertDoesNotThrow { GetInputBuilder("Title", "Label") }
    }

    /**
     * Tests for accessors and mutators.
     */
    @Test
    fun testGetterSetter() {
        val builder = GetInputBuilder("Title", "Label")

        assertDoesNotThrow { }
    }
}