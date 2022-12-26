package cyder.getter

import cyder.constants.CyderColors
import cyder.constants.CyderFonts
import org.junit.jupiter.api.Assertions.*
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

        assertEquals(builder.frameTitle, "Title")
        assertEquals(builder.labelText, "Label")

        assertDoesNotThrow { builder.labelFont = CyderFonts.DEFAULT_FONT }
        assertEquals(CyderFonts.DEFAULT_FONT, builder.labelFont)

        assertDoesNotThrow { builder.labelColor = CyderColors.navy }
        assertEquals(CyderColors.navy, builder.labelColor)

        assertDoesNotThrow { builder.submitButtonText = "Text" }
        assertEquals("Text", builder.submitButtonText)

        assertDoesNotThrow { builder.submitButtonColor = CyderColors.navy }
        assertEquals(CyderColors.navy, builder.submitButtonColor)

        assertDoesNotThrow { builder.submitButtonFont = CyderFonts.DEFAULT_FONT }
        assertEquals(CyderFonts.DEFAULT_FONT, builder.submitButtonFont)

        assertDoesNotThrow { builder.initialFieldText = "Field text" }
        assertEquals("Field text", builder.initialFieldText)

        assertDoesNotThrow { builder.fieldHintText = "Hint text" }
        assertEquals("Hint text", builder.fieldHintText)

        assertDoesNotThrow { builder.fieldRegex = ".*" }
        assertEquals(".*", builder.fieldRegex)

        assertDoesNotThrow { builder.fieldFont = CyderFonts.DEFAULT_FONT }
        assertEquals(CyderFonts.DEFAULT_FONT, builder.fieldFont)

        assertDoesNotThrow { builder.fieldForeground = CyderColors.regularPink }
        assertEquals(CyderColors.regularPink, builder.fieldForeground)

        assertDoesNotThrow { builder.isDisableRelativeTo = true }
        assertTrue(builder.isDisableRelativeTo)

        assertDoesNotThrow { builder.relativeTo = null }
        assertEquals(null, builder.relativeTo)

        assertDoesNotThrow { builder.addOnDialogDisposalRunnable { println("") } }
        // assertEquals(ImmutableList.of { println("") }, builder.onDialogDisposalRunnables)
    }

    /**
     * Tests for the equals method.
     */
    @Test
    fun testEquals() {
        val first = GetInputBuilder("Title", "Label")
        val second = GetInputBuilder("Title", "Label")
        val third = GetInputBuilder("Title three", "Label")

        assertEquals(first, second)
        assertNotEquals(second, third)

        second.initialFieldText = "field text"
        assertNotEquals(first, second)

        first.initialFieldText = "field text"
        assertEquals(first, second)

        first.labelFont = CyderFonts.DEFAULT_FONT
        first.labelColor = CyderColors.regularPink
        first.submitButtonText = "button text"
        first.submitButtonFont = CyderFonts.DEFAULT_FONT_SMALL
        first.submitButtonColor = CyderColors.regularPink
        first.fieldHintText = "field hint text"
        first.fieldRegex = ".*"
        first.fieldForeground = CyderColors.regularPink
        first.isDisableRelativeTo = true
        first.relativeTo = null
        first.addOnDialogDisposalRunnable { println("") }

        assertNotEquals(first, second)
    }

    /**
     * Tests for the hash code method.
     */
    @Test
    fun testHashCode() {
        val first = GetInputBuilder("Title", "Label")
        val second = GetInputBuilder("Title", "Label")
        val third = GetInputBuilder("Title three", "Label")

        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(second.hashCode(), third.hashCode())

        second.initialFieldText = "field text"
        assertNotEquals(first.hashCode(), second.hashCode())

        first.initialFieldText = "field text"
        assertEquals(first.hashCode(), second.hashCode())

        first.labelFont = CyderFonts.DEFAULT_FONT
        first.labelColor = CyderColors.regularPink
        first.submitButtonText = "button text"
        first.submitButtonFont = CyderFonts.DEFAULT_FONT_SMALL
        first.submitButtonColor = CyderColors.regularPink
        first.fieldHintText = "field hint text"
        first.fieldRegex = ".*"
        first.fieldForeground = CyderColors.regularPink
        first.isDisableRelativeTo = true
        first.relativeTo = null
        first.addOnDialogDisposalRunnable { println("") }

        assertNotEquals(first.hashCode(), second.hashCode())
    }

    /**
     * Tests for the to string method.
     */
    @Test
    fun testToString() {
        val first = GetInputBuilder("Title", "Label")
        val second = GetInputBuilder("Title", "Label")

        assertEquals("GetInputBuilder{frameTitle=\"Title\", labelText=\"Label\","
                + " labelFont=java.awt.Font[family=Agency FB,name=Agency FB,style=bold,size=30],"
                + " labelColor=java.awt.Color[r=26,g=32,b=51], submitButtonText=\"Submit\","
                + " submitButtonFont=java.awt.Font[family=Segoe UI Black,name=Segoe UI Black,style=bold,size=20],"
                + " submitButtonColor=java.awt.Color[r=223,g=85,b=83], initialFieldText=\"null\", fieldHintText=\"null\","
                + " fieldRegex=\"null\", fieldFont=java.awt.Font[family=Segoe UI Black,name=Segoe UI"
                + " Black,style=bold,size=20], fieldForeground=java.awt.Color[r=26,g=32,b=51], disableRelativeTo=false,"
                + " relativeTo=null, onDialogDisposalRunnables=[]}", first.toString())
        assertEquals("GetInputBuilder{frameTitle=\"Title\", labelText=\"Label\","
                + " labelFont=java.awt.Font[family=Agency FB,name=Agency FB,style=bold,size=30],"
                + " labelColor=java.awt.Color[r=26,g=32,b=51], submitButtonText=\"Submit\","
                + " submitButtonFont=java.awt.Font[family=Segoe UI Black,name=Segoe UI Black,style=bold,size=20],"
                + " submitButtonColor=java.awt.Color[r=223,g=85,b=83], initialFieldText=\"null\", fieldHintText=\"null\","
                + " fieldRegex=\"null\", fieldFont=java.awt.Font[family=Segoe UI Black,name=Segoe UI"
                + " Black,style=bold,size=20], fieldForeground=java.awt.Color[r=26,g=32,b=51], disableRelativeTo=false,"
                + " relativeTo=null, onDialogDisposalRunnables=[]}", second.toString())

        first.labelFont = CyderFonts.DEFAULT_FONT
        first.labelColor = CyderColors.regularPink
        first.submitButtonText = "button text"
        first.submitButtonFont = CyderFonts.DEFAULT_FONT_SMALL
        first.submitButtonColor = CyderColors.regularPink
        first.fieldHintText = "field hint text"
        first.fieldRegex = ".*"
        first.fieldForeground = CyderColors.regularPink
        first.isDisableRelativeTo = true
        first.relativeTo = null
        first.addOnDialogDisposalRunnable { println("") }

        assertEquals("GetInputBuilder{frameTitle=\"Title\", labelText=\"Label\","
                + " labelFont=java.awt.Font[family=Agency FB,name=Agency FB,style=bold,size=30],"
                + " labelColor=java.awt.Color[r=26,g=32,b=51], submitButtonText=\"Submit\","
                + " submitButtonFont=java.awt.Font[family=Segoe UI Black,name=Segoe UI Black,style=bold,size=20],"
                + " submitButtonColor=java.awt.Color[r=223,g=85,b=83], initialFieldText=\"null\","
                + " fieldHintText=\"null\", fieldRegex=\"null\", fieldFont=java.awt.Font[family=Segoe UI"
                + " Black,name=Segoe UI Black,style=bold,size=20], fieldForeground=java.awt.Color[r=26,g=32,b=51],"
                + " disableRelativeTo=false, relativeTo=null, onDialogDisposalRunnables=[]}", second.toString())
    }
}