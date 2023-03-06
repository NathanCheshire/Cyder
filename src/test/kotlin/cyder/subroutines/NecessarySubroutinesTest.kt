package cyder.subroutines

import cyder.utils.StaticUtil
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for the [NecessarySubroutines].
 */
class NecessarySubroutinesTest {
    /**
     * Tests that the necessary subroutines all complete successfully.
     */
    @Test
    fun testSubroutinesCompleteSuccessfully() {
        StaticUtil.loadStaticResources()
        NecessarySubroutines.subroutines.forEach { it.routine.get()?.let { it1 -> assertTrue(it1) } }
    }
}