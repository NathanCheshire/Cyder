package cyder.getter

import cyder.constants.CyderColors
import cyder.constants.CyderFonts
import cyder.ui.frame.CyderFrame
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Tests for [GetConfirmationBuilder]s.
 */
class GetConfirmationBuilderTest {
    /**
     * Tests for creation of get confirmation builders.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { GetConfirmationBuilder(null, "") }
        assertThrows(NullPointerException::class.java) { GetConfirmationBuilder("title", null) }
        assertThrows(IllegalArgumentException::class.java) { GetConfirmationBuilder("title", "") }
        assertThrows(IllegalArgumentException::class.java) { GetConfirmationBuilder("", "text") }
    }

    /**
     * Tests for accessors and mutators of the get confirmation builder.
     */
    @Test
    fun testGetterSetter() {
        val builder = GetConfirmationBuilder("Title", "Label text")
        assertEquals("Title", builder.frameTitle)
        assertEquals("Label text", builder.labelText)

        assertDoesNotThrow { builder.setLabelFont(CyderFonts.DEFAULT_FONT) }
        assertEquals(CyderFonts.DEFAULT_FONT, builder.labelFont)

        assertDoesNotThrow { builder.setLabelColor(CyderColors.regularRed) }
        assertEquals(CyderColors.regularRed, builder.labelColor)

        assertDoesNotThrow { builder.setYesButtonText("Sure") }
        assertEquals("Sure", builder.yesButtonText)

        assertDoesNotThrow { builder.setYesButtonFont(CyderFonts.DEFAULT_FONT) }
        assertEquals(CyderFonts.DEFAULT_FONT, builder.yesButtonFont)

        assertDoesNotThrow { builder.setYesButtonColor(CyderColors.regularPink) }
        assertEquals(CyderColors.regularPink, builder.yesButtonColor)

        assertDoesNotThrow { builder.setNoButtonText("No thanks") }
        assertEquals("No thanks", builder.noButtonText)

        assertDoesNotThrow { builder.setNoButtonFont(CyderFonts.DEFAULT_FONT) }
        assertEquals(CyderFonts.DEFAULT_FONT, builder.noButtonFont)

        assertDoesNotThrow { builder.setNoButtonColor(CyderColors.regularPink) }
        assertEquals(CyderColors.regularPink, builder.noButtonColor)

        val relativeTo = CyderFrame(411, 611)
        assertDoesNotThrow { builder.setRelativeTo(relativeTo) }
        assertEquals(relativeTo, builder.relativeTo)

        val runnable: () -> Unit = { println() }
        assertDoesNotThrow { builder.addOnDialogDisposalRunnable(runnable) }
        // assertEquals(runnable, builder.onDialogDisposalRunnables[0])

        assertDoesNotThrow { builder.setDisableRelativeTo(true) }
        assertEquals(true, builder.isDisableRelativeTo)
    }

    /**
     * Tests for the equals method of a get confirmation builder.
     */
    @Test
    fun testEquals() {
        val first = GetConfirmationBuilder("One", "Two")
        val second = GetConfirmationBuilder("One", "Three")
        val third = GetConfirmationBuilder("One", "Two")

        assertEquals(first, third)
        assertEquals(third, first)
        assertNotEquals(first, second)
        assertNotEquals(second, first)

        first.yesButtonColor = CyderColors.regularPink
        assertNotEquals(first, third)
        third.yesButtonColor = CyderColors.regularPink
        assertEquals(first, third)
    }

    /**
     * Tests for the hash code method of a get confirmation builder.
     */
    @Test
    fun testHashCode() {
        val first = GetConfirmationBuilder("One", "Two")
        val second = GetConfirmationBuilder("One", "Three")
        val third = GetConfirmationBuilder("One", "Two")

        assertEquals(first.hashCode(), third.hashCode())
        assertEquals(third.hashCode(), first.hashCode())
        assertNotEquals(first.hashCode(), second.hashCode())
        assertNotEquals(second.hashCode(), first.hashCode())

        first.yesButtonColor = CyderColors.regularPink
        assertNotEquals(first.hashCode(), third.hashCode())
        third.yesButtonColor = CyderColors.regularPink
        assertEquals(first.hashCode(), third.hashCode())
    }

    /**
     * Tests for the to string method of a get confirmation builder.
     */
    @Test
    fun testToString() {
        val first = GetConfirmationBuilder("One", "Two")
        assertEquals("GetConfirmationBuilder{frameTitle=\"One\", labelText=\"Two\","
                + " labelFont=java.awt.Font[family=Agency FB,name=Agency FB,style=bold,size=22],"
                + " labelColor=java.awt.Color[r=26,g=32,b=51], yesButtonText=\"Yes\","
                + " yesButtonColor=java.awt.Color[r=223,g=85,b=83], yesButtonFont=java.awt.Font[family=Segoe"
                + " UI Black,name=Segoe UI Black,style=bold,size=20], noButtonText=\"No\","
                + " noButtonColor=java.awt.Color[r=223,g=85,b=83], noButtonFont=java.awt.Font[family=Segoe"
                + " UI Black,name=Segoe UI Black,style=bold,size=20], relativeTo=null,"
                + " disableRelativeTo=false, onDialogDisposalRunnables=[]}", first.toString())

        val second = GetConfirmationBuilder("One", "Two")
        second.yesButtonColor = CyderColors.regularPink
        second.yesButtonFont = CyderFonts.DEFAULT_FONT_SMALL
        second.yesButtonText = "Sure"
        second.noButtonColor = CyderColors.regularPink
        second.noButtonFont = CyderFonts.DEFAULT_FONT_SMALL
        second.noButtonText = "Sure"
        second.labelFont = CyderFonts.DEFAULT_FONT
        second.labelColor = CyderColors.regularPink
        second.isDisableRelativeTo = true
        second.relativeTo = null
        assertEquals("GetConfirmationBuilder{frameTitle=\"One\","
                + " labelText=\"Two\", labelFont=java.awt.Font[family=Agency"
                + " FB,name=Agency FB,style=bold,size=30], labelColor=java.awt.Color[r=236,g=64,b=122],"
                + " yesButtonText=\"Sure\", yesButtonColor=java.awt.Color[r=236,g=64,b=122],"
                + " yesButtonFont=java.awt.Font[family=Agency FB,name=Agency FB,style=bold,size=22],"
                + " noButtonText=\"Sure\", noButtonColor=java.awt.Color[r=236,g=64,b=122],"
                + " noButtonFont=java.awt.Font[family=Agency FB,name=Agency FB,style=bold,size=22],"
                + " relativeTo=null, disableRelativeTo=true,"
                + " onDialogDisposalRunnables=[]}", second.toString())
    }
}