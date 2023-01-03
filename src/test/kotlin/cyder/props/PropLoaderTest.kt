package cyder.props

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import cyder.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * Tests for the [PropLoader].
 */
class PropLoaderTest {
    /**
     * Tests for the discover prop files method.
     */
    @Test
    fun testDiscoverPropFiles() {
        assertThrows(NullPointerException::class.java) {
            PropLoader.discoverPropFiles(null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PropLoader.discoverPropFiles(File("directory_that_does_not_exist"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            PropLoader.discoverPropFiles(File(".gitignore"))
        }

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val tmpPropFile1 = File("tmp/props.ini")
        tmpPropFile1.createNewFile()
        assertTrue(tmpPropFile1.exists())

        val tmpPropFile2 = File("tmp/props_more.ini")
        tmpPropFile2.createNewFile()
        assertTrue(tmpPropFile2.exists())

        assertEquals(ImmutableList.of(tmpPropFile1, tmpPropFile2), PropLoader.discoverPropFiles(tmpDir))

        assertTrue(OsUtil.deleteFile(tmpDir))
    }

    /**
     * Tests for the extract props from directory method.
     * This also transitively tests the extract props from file method.
     */
    @Test
    fun testExtractPropsFromDirectory() {
        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val tmpPropFile1 = File("tmp/props.ini")
        tmpPropFile1.createNewFile()
        assertTrue(tmpPropFile1.exists())

        BufferedWriter(FileWriter(tmpPropFile1)).use {
            it.write("key1:value1")
            it.newLine()
            it.write("key2:value2")
            it.newLine()
        }

        val tmpPropFile2 = File("tmp/props_more.ini")
        tmpPropFile2.createNewFile()
        assertTrue(tmpPropFile2.exists())

        BufferedWriter(FileWriter(tmpPropFile2)).use {
            it.write("key3:value3")
            it.newLine()
            it.write("key4:value4")
            it.newLine()
            it.write("key_with_colon\\:end:weird_value")
            it.newLine()
        }

        val result = PropLoader.extractPropsFromDirectory(tmpDir)
        assertEquals(ImmutableMap.of(
                "key1", "value1",
                "key2", "value2",
                "key3", "value3",
                "key4", "value4",
                "key_with_colon:end", "weird_value"), result)

        assertTrue(OsUtil.deleteFile(tmpDir))
    }

    /**
     * Test for the is comment method.
     */
    @Test
    fun testIsComment() {
        assertThrows(NullPointerException::class.java) { PropLoader.isComment(null) }

        assertFalse { PropLoader.isComment("") }
        assertFalse { PropLoader.isComment("asdf") }
        assertFalse { PropLoader.isComment("prop:value") }

        assertTrue { PropLoader.isComment("#") }
        assertTrue { PropLoader.isComment("    #     ") }
        assertTrue { PropLoader.isComment("    #     comment") }
    }

    /**
     * Test for the is no log annotation.
     */
    @Test
    fun testIsNoLogAnnotation() {
        assertThrows(NullPointerException::class.java) { PropLoader.isNoLogAnnotation(null) }

        assertFalse { PropLoader.isNoLogAnnotation("") }
        assertFalse { PropLoader.isNoLogAnnotation("line") }
        assertFalse { PropLoader.isNoLogAnnotation("@annotation") }

        assertTrue { PropLoader.isNoLogAnnotation("@no_log") }
        assertTrue { PropLoader.isNoLogAnnotation("    @no_log") }
        assertTrue { PropLoader.isNoLogAnnotation("\t@no_log") }
    }

    /**
     * Tests for the extract prop from line.
     */
    @Test
    fun testExtractPropFromLine() {
        assertThrows(NullPointerException::class.java) { PropLoader.extractPropFromLine(null) }
        assertThrows(IllegalArgumentException::class.java) { PropLoader.extractPropFromLine("") }
        assertThrows(IllegalArgumentException::class.java) { PropLoader.extractPropFromLine("key,value") }

        var prop = PropLoader.extractPropFromLine("Left:Right")
        assertEquals("Left", prop.left)
        assertEquals("Right", prop.right)

        prop = PropLoader.extractPropFromLine("Key:Value")
        assertEquals("Key", prop.left)
        assertEquals("Value", prop.right)

        prop = PropLoader.extractPropFromLine("key:value")
        assertEquals("key", prop.left)
        assertEquals("value", prop.right)

        prop = PropLoader.extractPropFromLine("key\\:with_colon:val")
        assertEquals("key:with_colon", prop.left)
        assertEquals("val", prop.right)

        prop = PropLoader.extractPropFromLine("key\\:with_colon:and:other:colons:val")
        assertEquals("key:with_colon", prop.left)
        assertEquals("and:other:colons:val", prop.right)
    }
}
