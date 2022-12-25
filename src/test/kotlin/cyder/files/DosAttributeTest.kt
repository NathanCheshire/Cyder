package cyder.files

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File

/**
 * Tests for [DosAttribute]s of a file.
 */
class DosAttributeTest {
    /**
     * Tests for DOS attributes of a file.
     */
    @Test
    fun testDosAttributes() {
        assertThrows(NullPointerException::class.java) { DosAttribute.getAttribute(null, null) }
        assertThrows(NullPointerException::class.java) { DosAttribute.getAttribute(File("."), null) }
        assertThrows(IllegalArgumentException::class.java) {
            DosAttribute.getAttribute(File("file_that_does_not_exist.txt"), null)
        }

        DosAttribute.values().iterator().forEach {
            assertDoesNotThrow {
                DosAttribute.getAttribute(File("."), it)
            }
        }
    }
}