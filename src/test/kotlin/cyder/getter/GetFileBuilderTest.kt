package cyder.getter

import com.google.common.collect.ImmutableList
import cyder.constants.CyderColors
import cyder.constants.CyderFonts
import cyder.strings.LevenshteinUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File

/**
 * Tests for [GetFileBuilder]s.
 */
class GetFileBuilderTest {
    /**
     * The length of a memory address in Kotlin. Used to ensure the Levenshtein distance between two strings
     * is of acceptable form (not off by more than different memory addresses for the runnables).
     */
    private val lambdaMemoryAddressLength: Int = 16

    /**
     * Tests for creation of get file builders.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { GetFileBuilder(null, null) }
        assertThrows(IllegalArgumentException::class.java) { GetFileBuilder("", null) }
        assertThrows(NullPointerException::class.java) { GetFileBuilder("Title", null) }
        assertThrows(IllegalArgumentException::class.java) {
            GetFileBuilder("Title", File("directory_that_does_not_exist"))
        }

        assertDoesNotThrow { GetFileBuilder("Title", File(".")) }
    }

    /**
     * Tests for accessors and mutators.
     */
    @Test
    fun testGettersSetters() {
        val builder = GetFileBuilder("Title", File("."))

        assertDoesNotThrow { builder.initialFieldText = "Field text" }
        assertEquals("Field text", builder.initialFieldText)

        assertDoesNotThrow { builder.fieldForeground = CyderColors.navy }
        assertEquals(CyderColors.navy, builder.fieldForeground)

        assertDoesNotThrow { builder.fieldFont = CyderFonts.DEFAULT_FONT }
        assertEquals(CyderFonts.DEFAULT_FONT, builder.fieldFont)

        assertDoesNotThrow { builder.isAllowFileSubmission = false }
        assertEquals(false, builder.isAllowFileSubmission)

        assertDoesNotThrow { builder.isAllowFolderSubmission = false }
        assertEquals(false, builder.isAllowFolderSubmission)

        assertDoesNotThrow { builder.submitButtonText = "Button text" }
        assertEquals("Button text", builder.submitButtonText)

        assertDoesNotThrow { builder.submitButtonColor = CyderColors.navy }
        assertEquals(CyderColors.navy, builder.submitButtonColor)

        assertDoesNotThrow { builder.submitButtonFont = CyderFonts.DEFAULT_FONT }
        assertEquals(CyderFonts.DEFAULT_FONT, builder.submitButtonFont)

        assertDoesNotThrow { builder.relativeTo = null }
        assertEquals(null, builder.relativeTo)

        assertDoesNotThrow { builder.isDisableRelativeTo = true }
        assertEquals(true, builder.isDisableRelativeTo)

        assertDoesNotThrow { builder.addOnDialogDisposalRunnable { println("") } }
        // assertEquals(ImmutableList.of { println("") }, builder.onDialogDisposalRunnables)

        assertDoesNotThrow { builder.allowableFileExtensions = ImmutableList.of("png") }
        assertEquals(ImmutableList.of("png"), builder.allowableFileExtensions)
    }

    /**
     * Tests for the equals method.
     */
    @Test
    fun testEquals() {
        val first = GetFileBuilder("Title first", File("."))
        val second = GetFileBuilder("Title", File("."))
        val third = GetFileBuilder("Title", File("."))

        assertNotEquals(first, second)
        assertEquals(second, third)

        second.initialFieldText = "Field text"
        second.fieldForeground = CyderColors.navy
        second.fieldFont = CyderFonts.DEFAULT_FONT
        second.isAllowFileSubmission = false
        second.isAllowFolderSubmission = false
        second.submitButtonText = "Button text"
        second.submitButtonColor = CyderColors.navy
        second.submitButtonFont = CyderFonts.DEFAULT_FONT
        second.relativeTo = null
        second.isDisableRelativeTo = true
        second.addOnDialogDisposalRunnable { println("") }
        second.allowableFileExtensions = ImmutableList.of("png")

        assertNotEquals(second, third)
    }

    /**
     * Tests for the hash code method.
     */
    @Test
    fun testHashCode() {
        val first = GetFileBuilder("Title first", File("."))
        val second = GetFileBuilder("Title", File("."))
        val third = GetFileBuilder("Title", File("."))

        assertNotEquals(first.hashCode(), second.hashCode())
        assertEquals(second.hashCode(), third.hashCode())

        second.initialFieldText = "Field text"
        second.fieldForeground = CyderColors.navy
        second.fieldFont = CyderFonts.DEFAULT_FONT
        second.isAllowFileSubmission = false
        second.isAllowFolderSubmission = false
        second.submitButtonText = "Button text"
        second.submitButtonColor = CyderColors.navy
        second.submitButtonFont = CyderFonts.DEFAULT_FONT
        second.relativeTo = null
        second.isDisableRelativeTo = true
        second.addOnDialogDisposalRunnable { println("") }
        second.allowableFileExtensions = ImmutableList.of("png")

        assertNotEquals(second.hashCode(), third.hashCode())
    }

    /**
     * Tests for the toString method.
     */
    @Test
    fun testToString() {
        val first = GetFileBuilder("Title first", File("."))
        val second = GetFileBuilder("Title", File("."))
        val third = GetFileBuilder("Title", File("."))

        assertEquals("GetFileBuilder{frameTitle=\"Title first\", initialDirectory=.,"
                + " initialFieldText=\"null\", fieldForeground=java.awt.Color[r=26,g=32,b=51],"
                + " fieldFont=java.awt.Font[family=Segoe UI Black,name=Segoe UI Black,style=bold,size=20],"
                + " allowFileSubmission=true, allowFolderSubmission=false, submitButtonText=\"Submit\","
                + " submitButtonFont=java.awt.Font[family=Segoe UI Black,name=Segoe UI Black,style=bold,size=20],"
                + " submitButtonColor=java.awt.Color[r=236,g=64,b=122], relativeTo=null, disableRelativeTo=false,"
                + " onDialogDisposalRunnables=[], allowableFileExtensions=[]}", first.toString())
        assertEquals("GetFileBuilder{frameTitle=\"Title\", initialDirectory=., initialFieldText=\"null\","
                + " fieldForeground=java.awt.Color[r=26,g=32,b=51], fieldFont=java.awt.Font[family=Segoe UI"
                + " Black,name=Segoe UI Black,style=bold,size=20], allowFileSubmission=true, allowFolderSubmission=false,"
                + " submitButtonText=\"Submit\", submitButtonFont=java.awt.Font[family=Segoe UI"
                + " Black,name=Segoe UI Black,style=bold,size=20], submitButtonColor=java.awt.Color[r=236,g=64,b=122],"
                + " relativeTo=null, disableRelativeTo=false, onDialogDisposalRunnables=[],"
                + " allowableFileExtensions=[]}", second.toString())
        assertEquals("GetFileBuilder{frameTitle=\"Title\", initialDirectory=., initialFieldText=\"null\","
                + " fieldForeground=java.awt.Color[r=26,g=32,b=51], fieldFont=java.awt.Font[family=Segoe UI"
                + " Black,name=Segoe UI Black,style=bold,size=20], allowFileSubmission=true, allowFolderSubmission=false,"
                + " submitButtonText=\"Submit\", submitButtonFont=java.awt.Font[family=Segoe UI Black,name=Segoe UI"
                + " Black,style=bold,size=20], submitButtonColor=java.awt.Color[r=236,g=64,b=122], relativeTo=null,"
                + " disableRelativeTo=false, onDialogDisposalRunnables=[], allowableFileExtensions=[]}", third.toString())

        second.initialFieldText = "Field text"
        second.fieldForeground = CyderColors.navy
        second.fieldFont = CyderFonts.DEFAULT_FONT
        second.isAllowFileSubmission = false
        second.isAllowFolderSubmission = false
        second.submitButtonText = "Button text"
        second.submitButtonColor = CyderColors.navy
        second.submitButtonFont = CyderFonts.DEFAULT_FONT
        second.relativeTo = null
        second.isDisableRelativeTo = true
        second.addOnDialogDisposalRunnable { println("") }
        second.allowableFileExtensions = ImmutableList.of("png")

        assertTrue(LevenshteinUtil.computeLevenshteinDistance(second.toString(),
                "GetFileBuilder{frameTitle=\"Title\", initialDirectory=., initialFieldText=\"Field text\","
                        + " fieldForeground=java.awt.Color[r=26,g=32,b=51], fieldFont=java.awt.Font[family=Agency"
                        + " FB,name=Agency FB,style=bold,size=30], allowFileSubmission=false, allowFolderSubmission=false,"
                        + " submitButtonText=\"Button text\", submitButtonFont=java.awt.Font[family=Agency FB,name=Agency"
                        + " FB,style=bold,size=30], submitButtonColor=java.awt.Color[r=26,g=32,b=51], relativeTo=null,"
                        + " disableRelativeTo=true, onDialogDisposalRunnables=[cyder.getter.GetFileBuilderTest\$\$Lambda\$363"
                        + "/0x0000000800cc8f70@c267ef4], allowableFileExtensions=[png]}") < lambdaMemoryAddressLength)
    }
}