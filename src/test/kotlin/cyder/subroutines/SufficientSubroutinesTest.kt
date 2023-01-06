package cyder.subroutines

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests for the [SufficientSubroutines]s.
 */
class SufficientSubroutinesTest {
    /**
     * Tests that the sufficient subroutines all complete successfully.
     */
    @Test
    fun testSubroutinesCompleteSuccessfully() {
        SufficientSubroutines.subroutines.forEach { it.routine.get()?.let { it1 -> Assertions.assertTrue(it1) } }
    }
}