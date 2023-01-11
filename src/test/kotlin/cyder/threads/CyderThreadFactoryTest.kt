package cyder.threads

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for the [CyderThreadFactory].
 */
class CyderThreadFactoryTest {
    /**
     * Tests for the creation of CyderThreadFactories.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { CyderThreadFactory(null) }
        assertThrows(IllegalArgumentException::class.java) { CyderThreadFactory("") }

        assertDoesNotThrow { CyderThreadFactory("thread factory") }
    }

    /**
     * Tests for the accessor methods.
     */
    @Test
    fun testAccessor() {
        val factory = CyderThreadFactory("thread factory")
        assertEquals("thread factory", factory.name)
    }

    /**
     * Tests for the new thread method.
     */
    @Test
    fun testNewThread() {
        val factory = CyderThreadFactory("thread factory")
        assertDoesNotThrow { factory.newThread {} }
        assertDoesNotThrow { factory.newThread { println() } }
    }

    /**
     * Tests for the to string method.
     */
    @Test
    fun testToString() {
        val factory = CyderThreadFactory("thread factory")
        assertEquals("CyderThreadFactory{name=\"thread factory\"}", factory.toString())

        val service = CyderThreadFactory("thread service")
        assertEquals("CyderThreadFactory{name=\"thread service\"}", service.toString())
    }
}