package cyder.files

import com.google.common.collect.ImmutableList
import main.java.cyder.enums.Extension
import main.java.cyder.files.FileUtil
import main.java.cyder.utils.OsUtil
import main.java.cyder.utils.StaticUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * Tests for [FileUtil]s.
 */
class FileUtilTest {
    /**
     * Tests for the is supported image extension method.
     */
    @Test
    fun testIsSupportedImageExtension() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.isSupportedImageExtension(null) }
        Assertions.assertFalse(FileUtil.isSupportedImageExtension(File("file")))
        Assertions.assertFalse(FileUtil.isSupportedImageExtension(File("c:\\users")))
        Assertions.assertFalse(FileUtil.isSupportedImageExtension(File("File.mp3")))

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.isSupportedImageExtension(File("File.png"))
        }

        Assertions.assertTrue(FileUtil.isSupportedImageExtension(StaticUtil.getStaticResource("Default.png")))
        Assertions.assertTrue(FileUtil.isSupportedImageExtension(StaticUtil.getStaticResource("x.png")))
    }

    /**
     * Tests for the is supported audio extension method.
     */
    @Test
    fun testIsSupportedAudioExtension() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.isSupportedAudioExtension(null) }
        Assertions.assertFalse(FileUtil.isSupportedAudioExtension(File("file")))
        Assertions.assertFalse(FileUtil.isSupportedAudioExtension(File("c:\\users")))
        Assertions.assertFalse(FileUtil.isSupportedAudioExtension(File("File.png")))

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.isSupportedAudioExtension(File("File.mp3"))
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.isSupportedAudioExtension(File("File.wav"))
        }

        Assertions.assertTrue(FileUtil.isSupportedAudioExtension(
                StaticUtil.getStaticResource("223.mp3")))
        Assertions.assertTrue(FileUtil.isSupportedAudioExtension(
                StaticUtil.getStaticResource("allthestars.mp3")))
        Assertions.assertTrue(FileUtil.isSupportedAudioExtension(
                StaticUtil.getStaticResource("allthestars.wav")))
        Assertions.assertTrue(FileUtil.isSupportedAudioExtension(
                StaticUtil.getStaticResource("commando.wav")))
    }

    /**
     * Tests for the is supported font extension method.
     */
    @Test
    fun testIsSupportedFontExtension() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.isSupportedFontExtension(null) }
        Assertions.assertFalse(FileUtil.isSupportedFontExtension(File("file")))
        Assertions.assertFalse(FileUtil.isSupportedFontExtension(File("c:\\users")))
        Assertions.assertFalse(FileUtil.isSupportedFontExtension(File("File.png")))

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.isSupportedFontExtension(File("File.ttf"))
        }

        Assertions.assertFalse(FileUtil.isSupportedFontExtension(StaticUtil.getStaticResource("x.png")))
        Assertions.assertTrue(FileUtil.isSupportedFontExtension(StaticUtil.getStaticResource("tahoma.ttf")))
    }

    /**
     * Tests for the file matches signature method.
     */
    @Test
    fun testFileMatchesSignature() {
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.fileMatchesSignature(null, null)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.fileMatchesSignature(File(""), null)
        }
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.fileMatchesSignature(StaticUtil.getStaticResource("x.png"), null)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.fileMatchesSignature(StaticUtil.getStaticResource("x.png"), ImmutableList.of())
        }

        Assertions.assertFalse(FileUtil.fileMatchesSignature(StaticUtil.getStaticResource("x.png"),
                ImmutableList.of(0xFA)))
        Assertions.assertFalse(FileUtil.fileMatchesSignature(StaticUtil.getStaticResource("x.png"),
                FileUtil.PNG_SIGNATURE))
    }

    /**
     * Tests for the get filename method.
     */
    @Test
    fun testGetFilename() {
        Assertions.assertThrows(IllegalArgumentException::class.java) { FileUtil.getFilename("") }
        Assertions.assertEquals("", FileUtil.getFilename(File("")))
        Assertions.assertEquals("MyFile", FileUtil.getFilename(File("MyFile.png")))
        Assertions.assertEquals("My File", FileUtil.getFilename(File("My File.png")))
        Assertions.assertEquals("My_File", FileUtil.getFilename(File("My_File.png")))
        Assertions.assertEquals("My_File_second", FileUtil.getFilename(File("My_File_second.png")))
        Assertions.assertEquals("yyyy.mm.dd", FileUtil.getFilename(File("yyyy.mm.dd.png")))

        Assertions.assertThrows(IllegalArgumentException::class.java) { FileUtil.getFilename("") }
        Assertions.assertEquals("MyFile", FileUtil.getFilename("MyFile.png"))
        Assertions.assertEquals("My File", FileUtil.getFilename("My File.png"))
        Assertions.assertEquals("My_File", FileUtil.getFilename("My_File.png"))
        Assertions.assertEquals("My_File_second", FileUtil.getFilename("My_File_second.png"))
        Assertions.assertEquals("yyyy.mm.dd", FileUtil.getFilename("yyyy.mm.dd.png"))
    }

    /**
     * Tests for the get extension method.
     */
    @Test
    fun testGetExtension() {
        Assertions.assertThrows(IllegalArgumentException::class.java) { FileUtil.getExtension("") }
        Assertions.assertEquals("", FileUtil.getExtension(File("")))
        Assertions.assertEquals("", FileUtil.getExtension(File("MyFile")))
        Assertions.assertEquals("", FileUtil.getExtension(File("My File")))
        Assertions.assertEquals("", FileUtil.getExtension(File("My_File")))
        Assertions.assertEquals(".png", FileUtil.getExtension(File(".png")))
        Assertions.assertEquals(".png", FileUtil.getExtension(File("My_File.png")))
        Assertions.assertEquals(".png", FileUtil.getExtension(File("My File.png")))
        Assertions.assertEquals(".png", FileUtil.getExtension(File("mm.dd.yyyy.png")))
    }

    /**
     * Tests for the get extension without period method.
     */
    @Test
    fun testGetExtensionWithoutPeriod() {
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.getExtensionWithoutPeriod(null)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.getExtensionWithoutPeriod(File(""))
        }

        Assertions.assertEquals("png", FileUtil.getExtensionWithoutPeriod(File("MyFile.png")))
        Assertions.assertEquals("pdf", FileUtil.getExtensionWithoutPeriod(File("mm.dd.yyyy.pdf")))
        Assertions.assertEquals("", FileUtil.getExtensionWithoutPeriod(File("mm")))
    }

    /**
     * Tests for the validate extension method.
     */
    @Test
    fun testValidateExtension() {
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.validateExtension(null, "")
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.validateExtension(File(""), "")
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.validateExtension(File(""), ImmutableList.of())
        }

        Assertions.assertTrue(FileUtil.validateExtension(File("File.png"), ".png"))
        Assertions.assertTrue(FileUtil.validateExtension(File("File.jpg"),
                ".png", ".jpg"))
        Assertions.assertTrue(FileUtil.validateExtension(File("File.pdf"),
                ImmutableList.of(".png", ".jpg", ".pdf")))
    }

    /**
     * Tests for the validate filename method.
     */
    @Test
    fun testValidateFilename() {
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.validateFileName(null, "")
        }
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.validateFileName(File(""), null)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.validateFileName(File(""), "")
        }
        Assertions.assertTrue(FileUtil.validateFileName(File("My File"), "My File"))
        Assertions.assertTrue(FileUtil.validateFileName(File("My File.png"), "My File"))
        Assertions.assertTrue(FileUtil.validateFileName(File("My File.pdf"), "My File"))
    }

    /**
     * Tests for the file contents equal method.
     */
    @Test
    fun testFileContentsEqual() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.fileContentsEqual(null, null) }
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.fileContentsEqual(File(""), null) }
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.fileContentsEqual(null, File("")) }

        Assertions.assertFalse(FileUtil.fileContentsEqual(File(""), File("")))
        Assertions.assertTrue(FileUtil.fileContentsEqual(StaticUtil.getStaticResource("x.png"),
                StaticUtil.getStaticResource("x.png")))
        Assertions.assertFalse(FileUtil.fileContentsEqual(StaticUtil.getStaticResource("x.png"),
                StaticUtil.getStaticResource("Default.png")))
    }

    /**
     * Tests for zipping a file.
     */
    @Test
    fun testZip() {
        Assertions.assertThrows(java.lang.NullPointerException::class.java) {
            FileUtil.zip(null, "")
        }
        Assertions.assertThrows(java.lang.NullPointerException::class.java) {
            FileUtil.zip("test", null)
        }
        Assertions.assertThrows(java.lang.IllegalArgumentException::class.java) {
            FileUtil.zip("", "destination")
        }
        Assertions.assertThrows(java.lang.IllegalArgumentException::class.java) {
            FileUtil.zip("source", "")
        }

        val tmpDirectoryName = "tmp"
        val testFileName = "test_zip_file"

        val tmpFile = File(tmpDirectoryName)
        tmpFile.mkdir()

        val zipFile = File("$tmpDirectoryName/$testFileName${Extension.TXT.extension}")
        val zipToPath = "$tmpDirectoryName/$testFileName${Extension.ZIP.extension}"
        zipFile.createNewFile()

        BufferedWriter(FileWriter(zipFile)).use {
            it.write("Test String")
            it.newLine()
            it.write("Final String")
            it.newLine()
        }

        Assertions.assertDoesNotThrow { FileUtil.zip(zipFile.absolutePath, zipToPath) }

        val zippedFile = File(zipToPath)

        Assertions.assertTrue(zippedFile.exists())
        Assertions.assertEquals(855347585024L, zippedFile.totalSpace)
        Assertions.assertTrue(OsUtil.deleteFile(File(tmpDirectoryName)))
    }
}