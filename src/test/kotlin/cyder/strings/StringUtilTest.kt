@file:Suppress("SpellCheckingInspection")

package cyder.strings

import com.google.common.collect.ImmutableList
import cyder.bounds.HtmlString
import cyder.bounds.PlainString
import cyder.constants.CyderFonts
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

    /**
     * Tests for the get apostrophe suffix method.
     */
    @Test
    fun testGetApostropheSuffix() {
        assertThrows(NullPointerException::class.java) { StringUtil.getApostropheSuffix(null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.getApostropheSuffix("") }

        assertEquals("'s", StringUtil.getApostropheSuffix("Nathan"))
        assertEquals("'", StringUtil.getApostropheSuffix("Brookes"))
    }

    /**
     * Tests for the get plural method.
     */
    @Test
    fun testGetPlural() {
        assertThrows(NullPointerException::class.java) { StringUtil.getPlural(null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.getPlural("") }

        assertEquals("Houses", StringUtil.getPlural("House"))
        assertEquals("geese", StringUtil.getPlural("Goose"))
        assertEquals("Persons", StringUtil.getPlural("Person"))
        assertEquals("Apples", StringUtil.getPlural("Apple"))
        assertEquals("Sheep", StringUtil.getPlural("Sheep"))
    }

    @Test
    fun testGetWordFormBasedOnNumber() {
        assertThrows(IllegalArgumentException::class.java) { StringUtil.getWordFormBasedOnNumber(0, null) }
        assertThrows(NullPointerException::class.java) { StringUtil.getWordFormBasedOnNumber(1, null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.getWordFormBasedOnNumber(1, "") }

        assertEquals("Issue", StringUtil.getWordFormBasedOnNumber(1, "Issue"))
        assertEquals("Issues", StringUtil.getWordFormBasedOnNumber(2, "Issue"))

        assertEquals("Moss", StringUtil.getWordFormBasedOnNumber(1, "Moss"))
        assertEquals("Mosses", StringUtil.getWordFormBasedOnNumber(2, "Moss"))
    }

    /**
     * Tests for the fill string method.
     */
    @Test
    fun testFillString() {
        assertThrows(IllegalArgumentException::class.java) { StringUtil.fillString(-1, "") }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.fillString(0, "") }
        assertThrows(NullPointerException::class.java) { StringUtil.fillString(1, null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.fillString(1, "") }

        assertEquals(" ", StringUtil.fillString(1, " "))
        assertEquals("  ", StringUtil.fillString(2, " "))
        assertEquals("aaaa", StringUtil.fillString(2, "aa"))
    }

    /**
     * Tests for the is palindrome method.
     */
    @Test
    fun testIsPalindrome() {
        assertThrows(NullPointerException::class.java) { StringUtil.isPalindrome(null) }

        assertTrue(StringUtil.isPalindrome(""))
        assertTrue(StringUtil.isPalindrome("pop"))
        assertTrue(StringUtil.isPalindrome("ogopogo"))
        assertTrue(StringUtil.isPalindrome("racecar"))
        assertTrue(StringUtil.isPalindrome("noon"))
        assertTrue(StringUtil.isPalindrome("civic"))
        assertTrue(StringUtil.isPalindrome("repaper"))
        assertTrue(StringUtil.isPalindrome("rotator"))
        assertTrue(StringUtil.isPalindrome("kayak"))

        assertFalse(StringUtil.isPalindrome("take"))
        assertFalse(StringUtil.isPalindrome("my"))
        assertFalse(StringUtil.isPalindrome("hand"))
        assertFalse(StringUtil.isPalindrome("we'll"))
        assertFalse(StringUtil.isPalindrome("make"))
        assertFalse(StringUtil.isPalindrome("it"))
        assertTrue(StringUtil.isPalindrome("I"))
        assertFalse(StringUtil.isPalindrome("swear"))
    }

    /**
     * Tests for the caps first words method.
     */
    @Test
    fun testCapsFirstWords() {
        assertThrows(NullPointerException::class.java) { StringUtil.capsFirstWords(null) }

        assertEquals("", StringUtil.capsFirstWords(""))
        assertEquals("Word", StringUtil.capsFirstWords("word"))
        assertEquals("Word", StringUtil.capsFirstWords("Word"))
        assertEquals("Sentence Of Words And Other Things",
                StringUtil.capsFirstWords("Sentence of words and other things"))
        assertEquals("Sentence Of Words And Other Things 1weird Word \$word$",
                StringUtil.capsFirstWords("Sentence of words and other things 1weird word \$word$"))
    }

    /**
     * Tests for the caps first word method.
     */
    @Test
    fun testCapsFirstWord() {
        assertThrows(NullPointerException::class.java) { StringUtil.capsFirstWord(null) }

        assertEquals("", StringUtil.capsFirstWord(""))
        assertEquals("L", StringUtil.capsFirstWord("L"))
        assertEquals("L", StringUtil.capsFirstWord("l"))
        assertEquals("Word", StringUtil.capsFirstWord("word"))
        assertEquals("Word", StringUtil.capsFirstWord("Word"))
        assertEquals("1word", StringUtil.capsFirstWord("1word"))
    }

    /**
     * Tests for the filter leet method.
     */
    @Test
    fun testFilterLeet() {
        assertThrows(NullPointerException::class.java) { StringUtil.filterLeet(null) }

        assertEquals("", StringUtil.filterLeet(""))
        assertEquals("Hello", StringUtil.filterLeet("Hello"))
        assertEquals("shittt", StringUtil.filterLeet("$#!ttt"))
        assertEquals("cunters", StringUtil.filterLeet("(u~+ers"))
    }

    /**
     * Tests for the has word method.
     */
    @Test
    fun testHasWord() {
        assertThrows(NullPointerException::class.java) { StringUtil.hasWord(null, null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.hasWord("", null) }
        assertThrows(NullPointerException::class.java) { StringUtil.hasWord("input", null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.hasWord("input", "") }

        assertFalse(StringUtil.hasWord("user input", "potato", true))
        assertFalse(StringUtil.hasWord("user input", "spuds", true))
        assertFalse(StringUtil.hasWord("user input", "userinput", true))
        assertFalse(StringUtil.hasWord("user input", "users", true))

        assertTrue(StringUtil.hasWord("user input", "user", true))
        assertTrue(StringUtil.hasWord("user input", "USER", true))
        assertTrue(StringUtil.hasWord("user input", "input", true))
        assertTrue(StringUtil.hasWord("user input", "inPut", true))
    }

    /**
     * Tests for the contains blocked words method.
     */
    @Test
    fun testContainsBlockedWords() {
        assertThrows(NullPointerException::class.java) { StringUtil.containsBlockedWords(null, true) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.containsBlockedWords("", true) }

        assertTrue { StringUtil.containsBlockedWords("$#!t", true).failed }
        assertTrue { StringUtil.containsBlockedWords("(u~+", true).failed }

        assertFalse { StringUtil.containsBlockedWords("hello world", true).failed }
        assertFalse { StringUtil.containsBlockedWords("hello (unt", false).failed }
    }

    /**
     * Test for the first char to lower case method.
     */
    @Test
    fun testFirstCharToLowerCase() {
        assertThrows(NullPointerException::class.java) { StringUtil.firstCharToLowerCase(null) }

        assertEquals("", StringUtil.firstCharToLowerCase(""))
        assertEquals("hello world", StringUtil.firstCharToLowerCase("Hello world"))
        assertEquals("hello world", StringUtil.firstCharToLowerCase("hello world"))
        assertEquals("pretty", StringUtil.firstCharToLowerCase("Pretty"))
        assertEquals("toxic", StringUtil.firstCharToLowerCase("Toxic"))
        assertEquals("revolver", StringUtil.firstCharToLowerCase("Revolver"))
    }

    /**
     * Tests for the count words method.
     */
    @Test
    fun testCountWords() {
        assertEquals(0, StringUtil.countWords(null))
        assertEquals(0, StringUtil.countWords(""))
        assertEquals(1, StringUtil.countWords("Hello"))
        assertEquals(2, StringUtil.countWords("Hello world"))
        assertEquals(9, StringUtil.countWords(CyderStrings.QUICK_BROWN_FOX))
    }

    /**
     * Tests for the format commas method.
     */
    @Test
    fun testFormatCommas() {
        assertThrows(NullPointerException::class.java) { StringUtil.formatCommas(null) }

        assertEquals("", StringUtil.formatCommas(""))
        assertEquals("Something, ", StringUtil.formatCommas("Something,"))
        assertEquals("Something, just", StringUtil.formatCommas("Something,just"))
        assertEquals("Something, just, like, this",
                StringUtil.formatCommas("Something,just,like,this"))
        assertEquals("Something, just, like, this, ",
                StringUtil.formatCommas("Something,just,like,this,"))
    }

    /**
     * Tests for the get definition method.
     */
    @Test
    fun testGetDefinition() {
        assertThrows(NullPointerException::class.java) { StringUtil.getDefinition(null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.getDefinition("") }

        var optionalDefinition = StringUtil.getDefinition("Guitar")
        assertTrue(optionalDefinition.isPresent)
        assertEquals("a stringed musical instrument with a long, fretted neck, a flat,"
                + " somewhat violinlike body, and typically six strings, which are plucked with "
                + "the fingers or with a plectrum.", optionalDefinition.get())

        optionalDefinition = StringUtil.getDefinition("Genesis")
        assertTrue(optionalDefinition.isPresent)
        assertEquals("an origin, creation, or beginning.", optionalDefinition.get())

        optionalDefinition = StringUtil.getDefinition("asdfasdfasdfasdf")
        assertTrue(optionalDefinition.isEmpty)
    }

    /**
     * Tests for the get wikipedia summary method.
     */
    @Test
    fun testGetWikipediaSummary() {
        assertThrows(NullPointerException::class.java) { StringUtil.getWikipediaSummary(null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.getWikipediaSummary("") }

        var optionalSummary = StringUtil.getWikipediaSummary("MGK")
        assertTrue(optionalSummary.isPresent)
        assertEquals("Colson Baker (born April 22, 1990), known professionally as Machine"
                + " Gun Kelly (MGK), is an American rapper, singer, songwriter, and actor. He is noted"
                + " for his genre duality across alternative rock with hip hop.\\nMachine Gun Kelly"
                + " released four mixtapes between 2007 and 2010 before signing with Bad Boy Records."
                + " He released his debut studio album, Lace Up, in 2012, which peaked at number four"
                + " on the US Billboard 200 and contained his breakout single \\\"Wild Boy\\\""
                + " (featuring Waka Flocka Flame). His second and third albums, General Admission"
                + " (2015) and Bloom (2017), achieved similar commercial success; the latter included"
                + " the single \\\"Bad Things\\\" (with Camila Cabello), which peaked at number 4 on"
                + " the Billboard Hot 100. His fourth album, Hotel Diablo (2019), included rap rock.\\nMachine"
                + " Gun Kelly released his fifth album, Tickets to My Downfall, in 2020; it marked a complete"
                + " departure from hip hop and entry into pop punk. It debuted at number one on the Billboard"
                + " 200, the only rock album to do so that year, and contained the single \\\"My Ex's Best Friend\\\","
                + " which reached number 20 on the Hot 100. He achieved similar commercial success with its follow"
                + " up Mainstream Sellout (2022).\\nMachine Gun Kelly had his first starring role in the romantic"
                + " drama Beyond the Lights (2014), and since appeared in the techno-thriller Nerve (2016), the horror"
                + " Bird Box (2018), the comedy Big Time Adolescence and portrayed Tommy Lee in the biopic"
                + " The Dirt (both 2019).", optionalSummary.get())

        optionalSummary = StringUtil.getWikipediaSummary("Rust")
        assertTrue(optionalSummary.isPresent)
        assertEquals("Rust is an iron oxide, a usually reddish-brown oxide formed by the reaction of"
                + " iron and oxygen in the catalytic presence of water or air moisture. Rust consists of hydrous"
                + " iron(III) oxides (Fe2O3\\u00b7nH2O) and iron(III) oxide-hydroxide (FeO(OH), Fe(OH)3), and is"
                + " typically associated with the corrosion of refined iron.\\nGiven sufficient time, any iron mass,"
                + " in the presence of water and oxygen, could eventually convert entirely to rust. Surface rust is"
                + " commonly flaky and friable, and provides no passivational protection to the underlying iron,"
                +
                " unlike the formation of patina on copper surfaces. Rusting is the common term for corrosion of"
                + " elemental iron and its alloys such as steel. Many other metals undergo similar corrosion, but the"
                + " resulting oxides are not commonly called \\\"rust\\\".Several forms of rust are distinguishable"
                + " both visually and by spectroscopy, and form under different circumstances. Other forms of rust"
                + " include the result of reactions between iron and chloride in an environment deprived of oxygen."
                + " Rebar used in underwater concrete pillars, which generates green rust, is an example. Although"
                + " rusting is generally a negative aspect of iron, a particular form of rusting, known as stable"
                + " rust, causes the object to have a thin coating of rust over the top. If kept in low relative"
                + " humidity, it makes the \\\"stable\\\" layer protective to the iron below, but not to the extent"
                + " of other oxides such as aluminium oxide on aluminium.", optionalSummary.get())

        optionalSummary = StringUtil.getWikipediaSummary("asdfasdf")
        assertTrue(optionalSummary.isEmpty)
    }

    /**
     * Tests for the are anagrams method.
     */
    @Test
    fun testAreAnagrams() {
        assertThrows(NullPointerException::class.java) { StringUtil.areAnagrams(null, null) }
        assertThrows(NullPointerException::class.java) { StringUtil.areAnagrams("", null) }

        assertTrue(StringUtil.areAnagrams("", ""))
        assertTrue(StringUtil.areAnagrams("something", "thingeosm"))
        assertFalse(StringUtil.areAnagrams("something", "somethings"))
    }

    /**
     * Tests for the split to html tags and content method.
     */
    @Test
    fun testSplitToHtmlTagsAndContent() {
        assertThrows(NullPointerException::class.java) { StringUtil.splitToHtmlTagsAndContent(null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.splitToHtmlTagsAndContent("") }

        assertEquals(ImmutableList.of(PlainString("Hello")),
                StringUtil.splitToHtmlTagsAndContent("Hello"))

        assertEquals(ImmutableList.of(HtmlString("<Hello>")),
                StringUtil.splitToHtmlTagsAndContent("<Hello>"))

        assertEquals(ImmutableList.of(HtmlString("<html>"),
                PlainString("content"), HtmlString("</html>")),
                StringUtil.splitToHtmlTagsAndContent("<html>content</html>"))

        assertEquals(ImmutableList.of(
                HtmlString("<html>"),
                HtmlString("<b>"),
                PlainString("bold-content"),
                HtmlString("</b>"),
                PlainString("non-bold content"),
                HtmlString("</html>")),
                StringUtil.splitToHtmlTagsAndContent("<html><b>bold-content</b>non-bold content</html>"))
    }

    /**
     * Tests for the is null or empty method.
     */
    @Test
    fun testIsNullOrEmpty() {
        assertTrue(StringUtil.isNullOrEmpty(null))
        assertTrue(StringUtil.isNullOrEmpty(""))
        assertTrue(StringUtil.isNullOrEmpty("\t"))
        assertTrue(StringUtil.isNullOrEmpty("\t\t"))
        assertTrue(StringUtil.isNullOrEmpty("\t\t\t"))
        assertTrue(StringUtil.isNullOrEmpty("\n"))
        assertTrue(StringUtil.isNullOrEmpty("\n\n"))
        assertTrue(StringUtil.isNullOrEmpty("\n\n\n"))
        assertTrue(StringUtil.isNullOrEmpty("      "))

        assertFalse(StringUtil.isNullOrEmpty("\u1040"))
        assertFalse(StringUtil.isNullOrEmpty("u"))
        assertFalse(StringUtil.isNullOrEmpty("\\"))
        assertFalse(StringUtil.isNullOrEmpty("\""))
        assertFalse(StringUtil.isNullOrEmpty("Hello world"))
    }

    /**
     * Tests for the get text length ignoring html tags.
     */
    @Test
    fun testGetTextLengthIgnoringHtmlTags() {
        assertThrows(NullPointerException::class.java) { StringUtil.getTextLengthIgnoringHtmlTags(null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.getTextLengthIgnoringHtmlTags("") }

        assertEquals(0, StringUtil.getTextLengthIgnoringHtmlTags("<html>"))
        assertEquals(0, StringUtil.getTextLengthIgnoringHtmlTags("</html>"))
        assertEquals(0, StringUtil.getTextLengthIgnoringHtmlTags("<b>"))
        assertEquals(0, StringUtil.getTextLengthIgnoringHtmlTags("</b>"))
        assertEquals(5, StringUtil.getTextLengthIgnoringHtmlTags("Hello"))
        assertEquals(5, StringUtil.getTextLengthIgnoringHtmlTags("World"))
        assertEquals(0, StringUtil.getTextLengthIgnoringHtmlTags("<html></html>"))
        assertEquals(0, StringUtil.getTextLengthIgnoringHtmlTags("<b></b>"))
        assertEquals(11, StringUtil.getTextLengthIgnoringHtmlTags("<b>Hello World</b>"))
    }

    /**
     * Tests for the get trimmed text method.
     */
    @Test
    fun testGetTrimmedText() {
        assertThrows(NullPointerException::class.java) { StringUtil.getTrimmedText(null) }

        assertEquals("", StringUtil.getTrimmedText(""))
        assertEquals("hello world", StringUtil.getTrimmedText("hello world"))
        assertEquals("hello world", StringUtil.getTrimmedText("hello\tworld"))
        assertEquals("hello world", StringUtil.getTrimmedText("hello\t\tworld"))
        assertEquals("hello world", StringUtil.getTrimmedText("hello\t\tworld\t"))
        assertEquals("hello world", StringUtil.getTrimmedText("\thello\t\tworld"))
        assertEquals("hello world", StringUtil.getTrimmedText("\thello\t\tworld\t"))
        assertEquals("hello world", StringUtil.getTrimmedText("   hello world"))
        assertEquals("hello world", StringUtil.getTrimmedText("   hello world    "))
        assertEquals("hello world", StringUtil.getTrimmedText("hello world    "))
        assertEquals("hello world", StringUtil.getTrimmedText("    hello     world    "))
    }

    /**
     * Tests for the in method.
     */
    @Test
    fun testIn() {
        assertThrows(NullPointerException::class.java) { StringUtil.`in`(null, null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.`in`("", null) }
        assertThrows(NullPointerException::class.java) { StringUtil.`in`("string", null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.`in`("string", "") }
        assertThrows(NullPointerException::class.java) { StringUtil.`in`("string", "string", null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.`in`("string", "string", "") }

        assertThrows(NullPointerException::class.java) {
            StringUtil.`in`(null, false, ImmutableList.of())
        }
        assertThrows(IllegalArgumentException::class.java) {
            StringUtil.`in`("", false, ImmutableList.of())
        }
        assertThrows(IllegalArgumentException::class.java) {
            StringUtil.`in`("word", false, ImmutableList.of())
        }

        val nullString: String? = null

        assertThrows(NullPointerException::class.java) {
            StringUtil.`in`("word", false, ImmutableList.of(nullString))
        }
        assertThrows(IllegalArgumentException::class.java) {
            StringUtil.`in`("word", false, ImmutableList.of(""))
        }
        assertThrows(NullPointerException::class.java) {
            StringUtil.`in`("word", false, ImmutableList.of("word", nullString))
        }
        assertThrows(IllegalArgumentException::class.java) {
            StringUtil.`in`("word", false, ImmutableList.of("word", ""))
        }

        assertFalse(StringUtil.`in`("word", false, "wOrD"))
        assertTrue(StringUtil.`in`("word", true, "wOrD"))
        assertTrue(StringUtil.`in`("word", false, "word"))
        assertTrue(StringUtil.`in`("word", true, "word"))

        assertTrue(StringUtil.`in`("word", false, "wOrD", "word"))
        assertTrue(StringUtil.`in`("STUFF", true, "wOrD", "stuff"))
        assertFalse(StringUtil.`in`("Word", false, "word", "Stuffs"))
        assertTrue(StringUtil.`in`("word", true, "word", "Stuffs"))

        assertTrue(StringUtil.`in`("word", true, ImmutableList.of("word")))
        assertTrue(StringUtil.`in`("word", true, ImmutableList.of("word", "words")))
        assertFalse(StringUtil.`in`("word", false, ImmutableList.of("wOrd", "words")))
    }

    /**
     * Tests for the get min width and get absolute min width methods.
     */
    @Test
    fun testGetMinAndGetAbsoluteMinWidth() {
        assertThrows(NullPointerException::class.java) { StringUtil.getMinWidth(null, null) }
        assertThrows(NullPointerException::class.java) { StringUtil.getMinWidth("", null) }
        assertThrows(NullPointerException::class.java) { StringUtil.getAbsoluteMinWidth(null, null) }
        assertThrows(NullPointerException::class.java) { StringUtil.getAbsoluteMinWidth("", null) }

        var font = CyderFonts.DEFAULT_FONT_SMALL

        assertEquals(StringUtil.SIZE_ADDITIVE, StringUtil.getMinWidth("", font))
        assertEquals(0, StringUtil.getAbsoluteMinWidth("", font))

        assertEquals(57, StringUtil.getMinWidth("Pretty", font))
        assertEquals(57 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Pretty", font))

        assertEquals(48, StringUtil.getMinWidth("Toxic", font))
        assertEquals(48 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Toxic", font))

        assertEquals(55, StringUtil.getMinWidth("Heavy", font))
        assertEquals(55 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Heavy", font))

        assertEquals(95, StringUtil.getMinWidth("Conscience", font))
        assertEquals(95 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Conscience", font))

        font = CyderFonts.DEFAULT_FONT

        assertEquals(StringUtil.SIZE_ADDITIVE, StringUtil.getMinWidth("", font))
        assertEquals(0, StringUtil.getAbsoluteMinWidth("", font))

        assertEquals(74, StringUtil.getMinWidth("Pretty", font))
        assertEquals(74 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Pretty", font))

        assertEquals(63, StringUtil.getMinWidth("Toxic", font))
        assertEquals(63 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Toxic", font))

        assertEquals(71, StringUtil.getMinWidth("Heavy", font))
        assertEquals(71 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Heavy", font))

        assertEquals(126, StringUtil.getMinWidth("Conscience", font))
        assertEquals(126 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Conscience", font))

        font = CyderFonts.DEFAULT_FONT_LARGE

        assertEquals(StringUtil.SIZE_ADDITIVE, StringUtil.getMinWidth("", font))
        assertEquals(0, StringUtil.getAbsoluteMinWidth("", font))

        assertEquals(85, StringUtil.getMinWidth("Pretty", font))
        assertEquals(85 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Pretty", font))

        assertEquals(71, StringUtil.getMinWidth("Toxic", font))
        assertEquals(71 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Toxic", font))

        assertEquals(81, StringUtil.getMinWidth("Heavy", font))
        assertEquals(81 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Heavy", font))

        assertEquals(145, StringUtil.getMinWidth("Conscience", font))
        assertEquals(145 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinWidth("Conscience", font))
    }

    /**
     * Tests for the get min height and get absolute min height methods.
     */
    @Test
    fun testGetMinAndGetAbsoluteMinHeight() {
        assertThrows(NullPointerException::class.java) { StringUtil.getMinHeight(null, null) }
        assertThrows(NullPointerException::class.java) { StringUtil.getMinHeight("", null) }
        assertThrows(NullPointerException::class.java) { StringUtil.getAbsoluteMinHeight(null, null) }
        assertThrows(NullPointerException::class.java) { StringUtil.getAbsoluteMinHeight("", null) }

        var font = CyderFonts.DEFAULT_FONT_SMALL

        assertEquals(36, StringUtil.getMinHeight("", font))
        assertEquals(36 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("", font))

        assertEquals(36, StringUtil.getMinHeight("Pretty", font))
        assertEquals(36 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Pretty", font))

        assertEquals(36, StringUtil.getMinHeight("Toxic", font))
        assertEquals(36 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Toxic", font))

        assertEquals(36, StringUtil.getMinHeight("Heavy", font))
        assertEquals(36 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Heavy", font))

        assertEquals(36, StringUtil.getMinHeight("Conscience", font))
        assertEquals(36 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Conscience", font))

        font = CyderFonts.DEFAULT_FONT

        assertEquals(45, StringUtil.getMinHeight("", font))
        assertEquals(45 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("", font))

        assertEquals(45, StringUtil.getMinHeight("Pretty", font))
        assertEquals(45 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Pretty", font))

        assertEquals(45, StringUtil.getMinHeight("Toxic", font))
        assertEquals(45 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Toxic", font))

        assertEquals(45, StringUtil.getMinHeight("Heavy", font))
        assertEquals(45 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Heavy", font))

        assertEquals(45, StringUtil.getMinHeight("Conscience", font))
        assertEquals(45 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Conscience", font))

        font = CyderFonts.DEFAULT_FONT_LARGE

        assertEquals(51, StringUtil.getMinHeight("", font))
        assertEquals(0, StringUtil.getAbsoluteMinWidth("", font))

        assertEquals(51, StringUtil.getMinHeight("Pretty", font))
        assertEquals(51 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Pretty", font))

        assertEquals(51, StringUtil.getMinHeight("Toxic", font))
        assertEquals(51 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Toxic", font))

        assertEquals(81, StringUtil.getMinWidth("Heavy", font))
        assertEquals(41, StringUtil.getAbsoluteMinHeight("Heavy", font))

        assertEquals(145, StringUtil.getMinWidth("Conscience", font))
        assertEquals(51 - StringUtil.SIZE_ADDITIVE, StringUtil.getAbsoluteMinHeight("Conscience", font))
    }

    /**
     * Tests for the remove non-ascii method.
     */
    @Test
    fun testRemoveNonAscii() {
        assertThrows(NullPointerException::class.java) { StringUtil.removeNonAscii(null) }

        assertEquals("Hello World", StringUtil.removeNonAscii("Hello World"))
        assertEquals("Hello World", StringUtil.removeNonAscii("Hello🔥😏World"))
        assertEquals("", StringUtil.removeNonAscii("😂❤️"))
    }

    /**
     * Tests for the contains non ascii method.
     */
    @Test
    fun testContainsNonAscii() {
        assertThrows(NullPointerException::class.java) { StringUtil.containsNonAscii(null) }

        assertFalse(StringUtil.containsNonAscii("Hello World"))
        assertTrue(StringUtil.containsNonAscii("Hello中國的World"))
        assertTrue(StringUtil.containsNonAscii("😂❤️"))

        assertFalse(StringUtil.containsNonAscii("Hello world"))
        assertFalse(StringUtil.containsNonAscii("1234567890"))
        assertFalse(StringUtil.containsNonAscii("!@#$%^&*()_+-="))
    }

    /**
     * Tests for the generate spaces method.
     */
    @Test
    fun testGenerateSpaces() {
        assertThrows(IllegalArgumentException::class.java) { StringUtil.generateSpaces(-1) }

        assertEquals("", StringUtil.generateSpaces(0))
        assertEquals(" ", StringUtil.generateSpaces(1))
        assertEquals("  ", StringUtil.generateSpaces(2))
        assertEquals("   ", StringUtil.generateSpaces(3))
        assertEquals("    ", StringUtil.generateSpaces(4))
        assertEquals("     ", StringUtil.generateSpaces(5))
    }

    /**
     * Tests for the strip new lines and trim method.
     */
    @Test
    fun testStripNewLinesAndTrim() {
        assertThrows(NullPointerException::class.java) { StringUtil.stripNewLinesAndTrim(null) }

        assertEquals("", StringUtil.stripNewLinesAndTrim(""))
        assertEquals("", StringUtil.stripNewLinesAndTrim("\t\t"))
        assertEquals("", StringUtil.stripNewLinesAndTrim("\n\n"))
        assertEquals("", StringUtil.stripNewLinesAndTrim("\n\n\t\t"))
        assertEquals("", StringUtil.stripNewLinesAndTrim(" \t\t  "))
        assertEquals("", StringUtil.stripNewLinesAndTrim("\n\t    "))

        assertEquals("hello world", StringUtil.stripNewLinesAndTrim("\thello world\t"))
        assertEquals("hello world", StringUtil.stripNewLinesAndTrim("\thello world\t"))
    }

    /**
     * Tests for the contains letter method.
     */
    @Test
    fun testContainsLetter() {
        assertThrows(NullPointerException::class.java) { StringUtil.containsLetter(null) }

        assertTrue(StringUtil.containsLetter("1234a".toCharArray()))
        assertFalse(StringUtil.containsLetter("1234567890".toCharArray()))
        assertFalse(StringUtil.containsLetter("".toCharArray()))
        assertFalse(StringUtil.containsLetter("7".toCharArray()))
    }

    /**
     * Tests for the contains number method.
     */
    @Test
    fun testContainsNumber() {
        assertThrows(NullPointerException::class.java) { StringUtil.containsNumber(null) }

        assertTrue(StringUtil.containsNumber("aaaa2".toCharArray()))
        assertFalse(StringUtil.containsNumber("asdfasdf".toCharArray()))
        assertFalse(StringUtil.containsNumber("".toCharArray()))
        assertFalse(StringUtil.containsNumber("a".toCharArray()))
    }

    /**
     * Tests for the escape quotes method.
     */
    @Test
    fun testEscapeQuotes() {
        assertThrows(NullPointerException::class.java) { StringUtil.escapeQuotes(null) }

        assertEquals("", StringUtil.escapeQuotes(""))
        assertEquals("hello", StringUtil.escapeQuotes("hello"))
        assertEquals("hello\\\"", StringUtil.escapeQuotes("hello\""))
        assertEquals("\\\"hello\\\"", StringUtil.escapeQuotes("\"hello\""))
    }

    /**
     * Tests for the join parts method.
     */
    @Test
    fun testJoinParts() {
        assertThrows(NullPointerException::class.java) { StringUtil.joinParts(null, null) }
        assertThrows(NullPointerException::class.java) { StringUtil.joinParts(ImmutableList.of(), null) }

        assertEquals("", StringUtil.joinParts(ImmutableList.of("", ""), ""))
        assertEquals("aa", StringUtil.joinParts(ImmutableList.of("a", "a"), ""))
        assertEquals("a a", StringUtil.joinParts(ImmutableList.of("a", "a"), " "))
        assertEquals("a a b", StringUtil.joinParts(ImmutableList.of("a", "a", "b"), " "))
    }

    /**
     * Tests for the remove last char method.
     */
    @Test
    fun testRemoveLastChar() {
        assertThrows(NullPointerException::class.java) { StringUtil.removeLastChar(null) }
        assertThrows(IllegalArgumentException::class.java) { StringUtil.removeLastChar("") }

        assertEquals("", StringUtil.removeLastChar("a"))
        assertEquals("a", StringUtil.removeLastChar("ab"))
        assertEquals("Hell", StringUtil.removeLastChar("Hello"))
        assertEquals("Hello", StringUtil.removeLastChar("Hello1"))
    }
}
