package cyder.managers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for the [CyderVersionManager].
 */
class CyderVersionManagerTest {
    /**
     * Tests for the necessary getters of the cyder version manager.
     */
    @Test
    fun testGetters() {
        assertEquals("Cyder", CyderVersionManager.INSTANCE.programName)
    }
}