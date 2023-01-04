package cyder.strings

import cyder.ui.button.CyderButton
import cyder.ui.pane.CyderOutputPane
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.swing.JTextPane

/**
 * Tests for [StringUtil]s.
 */
class StringUtilTest {
    /**
     * Test for creation of string util instances.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { StringUtil(null) }

        val pane = JTextPane()
        var stringUtilInstance: StringUtil? = null
        assertDoesNotThrow { stringUtilInstance = StringUtil(CyderOutputPane(pane)) }
        assertNotNull(stringUtilInstance)
        assertEquals(pane, stringUtilInstance!!.linkedCyderPane.jTextPane)
    }

    /**
     * Tests for the remove first method.
     */
    @Test
    fun testRemoveFirst() {
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        assertEquals(2, pane.document.rootElements.size)
        assertEquals("", pane.getText(0, pane.styledDocument.length))

        stringUtilInstance.println("Hello")
        assertEquals(2, pane.document.rootElements.size)
        assertEquals("Hello\n", pane.getText(0, pane.styledDocument.length))

        stringUtilInstance.println("World")
        assertEquals(2, pane.document.rootElements.size)
        assertEquals("Hello\nWorld\n", pane.getText(0, pane.styledDocument.length))

        stringUtilInstance.removeFirst()
        assertEquals(2, pane.document.rootElements.size)
        assertEquals("World\n", pane.getText(0, pane.styledDocument.length))

        stringUtilInstance.removeFirst()
        assertEquals(2, pane.document.rootElements.size)
        assertEquals("", pane.getText(0, pane.styledDocument.length))
    }

    /**
     * Tests for the get last text line method.
     */
    @Test
    fun testGetLastTextLine() {
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        assertEquals("", stringUtilInstance.lastTextLine)

        stringUtilInstance.println("Hello")
        assertEquals("Hello\r", stringUtilInstance.lastTextLine)

        stringUtilInstance.println("World")
        assertEquals("World\r", stringUtilInstance.lastTextLine)
    }

    /**
     * Tests for the remove last element method.
     */
    @Test
    fun testRemoveLastElement() {
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        stringUtilInstance.println("Hello")
        stringUtilInstance.println("World")

        assertEquals("Hello\nWorld\n", pane.getText(0, pane.styledDocument.length))
        stringUtilInstance.removeLastElement()
        assertEquals("Hello\nWorld", pane.getText(0, pane.styledDocument.length))
        stringUtilInstance.removeLastElement()
        assertEquals("Hello", pane.getText(0, pane.styledDocument.length))
        stringUtilInstance.removeLastElement()
        assertEquals("", pane.getText(0, pane.styledDocument.length))
    }

    /**
     * Tests for the count document elements method.
     */
    @Test
    fun testCountDocumentElements() {
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        assertEquals(3, stringUtilInstance.countDocumentElements())
        stringUtilInstance.println("Hello")
        assertEquals(5, stringUtilInstance.countDocumentElements())
        stringUtilInstance.println("World")
        assertEquals(7, stringUtilInstance.countDocumentElements())
        stringUtilInstance.printlnComponent(CyderButton())
        assertEquals(10, stringUtilInstance.countDocumentElements())
    }

    /**
     * Tests for the document contains more than default elements method.
     */
    @Test
    fun testDocumentContainsMoreThanDefaultElements() {
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        assertFalse(stringUtilInstance.documentContainsMoreThanDefaultElements())

        stringUtilInstance.println("Hello")
        assertTrue(stringUtilInstance.documentContainsMoreThanDefaultElements())
        stringUtilInstance.println("World")
        assertTrue(stringUtilInstance.documentContainsMoreThanDefaultElements())
        stringUtilInstance.removeLastElement() // Remove \n
        assertTrue(stringUtilInstance.documentContainsMoreThanDefaultElements())
        stringUtilInstance.removeLastElement() // Remove \nWorld
        assertTrue(stringUtilInstance.documentContainsMoreThanDefaultElements())
        stringUtilInstance.removeLastElement() // Remove Hello
        assertFalse(stringUtilInstance.documentContainsMoreThanDefaultElements())
    }

    /**
     * Tests for the print component method.
     */
    @Test
    fun testPrintComponent() {

    }
}
