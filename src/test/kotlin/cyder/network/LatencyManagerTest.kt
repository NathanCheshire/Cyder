package cyder.network

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Test for the [LatencyManager].
 */
class LatencyManagerTest {
    /**
     * Tests for the get latency method.
     */
    @Test
    fun testGetLatency() {
        assertThrows(IllegalArgumentException::class.java) { LatencyManager.INSTANCE.getLatency(0) }
        assertThrows(IllegalArgumentException::class.java) { LatencyManager.INSTANCE.getLatency(-1) }

        assertDoesNotThrow { LatencyManager.INSTANCE.getLatency(5) }
        assertDoesNotThrow { LatencyManager.INSTANCE.latency }


    }

    /**
     * Tests for the current connection has decent ping method.
     */
    @Test
    fun testCurrentConnectionHasDecentPing() {
        assertDoesNotThrow { LatencyManager.INSTANCE.currentConnectionHasDecentPing() }
        assertTrue { LatencyManager.INSTANCE.currentConnectionHasDecentPing() }
    }
}