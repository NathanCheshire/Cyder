package cyder.strings

import com.google.common.collect.ImmutableList
import cyder.ui.button.CyderButton
import cyder.ui.pane.CyderOutputPane
import cyder.utils.SecurityUtil
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
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        val componentId = SecurityUtil.generateUuid()
        val name = "Test Button"
        assertDoesNotThrow { stringUtilInstance.printComponent(CyderButton(), name, componentId) }
        assertEquals(36, pane.styledDocument.length)
        assertEquals(componentId, stringUtilInstance.lastTextLine)
    }

    /**
     * Tests for the println component method.
     */
    @Test
    fun testPrintlnComponent() {
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        val componentId = SecurityUtil.generateUuid()
        val name = "Test Button"
        assertDoesNotThrow { stringUtilInstance.printlnComponent(CyderButton(), name, componentId) }
        assertEquals(37, pane.styledDocument.length)
        assertEquals(componentId, stringUtilInstance.lastTextLine.replace("\\s+".toRegex(), ""))
    }

    /**
     * Tests for the print method.
     */
    @Test
    fun testPrint() {
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        stringUtilInstance.print("Test String")
        assertEquals("Test String", stringUtilInstance.lastTextLine)

        stringUtilInstance.print(0)
        assertEquals("Test String0", stringUtilInstance.lastTextLine)

        stringUtilInstance.print("World")
        assertEquals("Test String0World", stringUtilInstance.lastTextLine)

        stringUtilInstance.print(0.0)
        assertEquals("Test String0World0.0", stringUtilInstance.lastTextLine)

        stringUtilInstance.print(0.0f)
        assertEquals("Test String0World0.00.0", stringUtilInstance.lastTextLine)

        stringUtilInstance.print(0.toShort())
        assertEquals("Test String0World0.00.00", stringUtilInstance.lastTextLine)

        stringUtilInstance.print(27000L)
        assertEquals("Test String0World0.00.0027000", stringUtilInstance.lastTextLine)

        stringUtilInstance.print('a')
        assertEquals("Test String0World0.00.0027000a", stringUtilInstance.lastTextLine)

        stringUtilInstance.print(0b1111)
        assertEquals("Test String0World0.00.0027000a15", stringUtilInstance.lastTextLine)

        stringUtilInstance.println("")
        val button = CyderButton()
        stringUtilInstance.print(button)
        assertEquals(button.toString(), stringUtilInstance.lastTextLine)
    }

    /**
     * Tests for the println method.
     */
    @Test
    fun testPrintln() {
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        stringUtilInstance.println("Test String")
        assertEquals("Test String", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))

        stringUtilInstance.println(0)
        assertEquals("0", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))

        stringUtilInstance.println("World")
        assertEquals("World", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))

        stringUtilInstance.println(0.0)
        assertEquals("0.0", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))

        stringUtilInstance.println(0.0f)
        assertEquals("0.0", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))

        stringUtilInstance.println(0.toShort())
        assertEquals("0", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))

        stringUtilInstance.println(27000L)
        assertEquals("27000", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))

        stringUtilInstance.println('a')
        assertEquals("a", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))

        stringUtilInstance.println(0b1111)
        assertEquals("15", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))

        val button = CyderButton()
        stringUtilInstance.println(button)
        assertEquals(button.toString(), stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))
    }

    /**
     * Tests for the print lines method.
     */
    @Test
    fun testPrintLines() {
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        val nullList: ImmutableList<String>? = null
        assertThrows(NullPointerException::class.java) { stringUtilInstance.printLines(nullList) }

        assertDoesNotThrow { stringUtilInstance.printLines(ImmutableList.of("Hello", "World")) }
        assertEquals("World", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))

        stringUtilInstance.removeLastElement()
        stringUtilInstance.removeLastElement()
        assertEquals("Hello", stringUtilInstance.lastTextLine.replace("[\\r\\n]+".toRegex(), ""))
    }

    /**
     * Tests for the newline method.
     */
    @Test
    fun testNewline() {
        val pane = JTextPane()
        val stringUtilInstance = StringUtil(CyderOutputPane(pane))

        stringUtilInstance.newline()
        assertEquals("\r", stringUtilInstance.lastTextLine)

        stringUtilInstance.print("Hello")
        assertEquals("Hello", stringUtilInstance.lastTextLine)

        stringUtilInstance.newline(true)
        assertEquals("Hello\r", stringUtilInstance.lastTextLine)

        stringUtilInstance.print("World")
        assertEquals("World", stringUtilInstance.lastTextLine)

        stringUtilInstance.newline(false)
        assertEquals("World", stringUtilInstance.lastTextLine)
    }
}
