package cyder.files

import com.google.common.collect.ImmutableList
import main.java.cyder.enums.Extension
import main.java.cyder.files.FileUtil
import main.java.cyder.utils.OsUtil
import main.java.cyder.utils.StaticUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.*

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
        Assertions.assertTrue(tmpFile.mkdir())

        val zipFile = File("$tmpDirectoryName/$testFileName${Extension.TXT.extension}")
        val zipToPath = "$tmpDirectoryName/$testFileName${Extension.ZIP.extension}"
        Assertions.assertTrue(zipFile.createNewFile())

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
        Assertions.assertTrue(OsUtil.deleteFile(File(tmpDirectoryName), false))
    }

    /**
     * Tests unzipping a file.
     */
    @Test
    fun testUnzip() {
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.unzip(null, File(""))
        }
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.unzip(File(""), null)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.unzip(File("file_that_does_not_exist.asdf"), File(""))
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.unzip(File(""), File("file_that_does_not_exist.asdf"))
        }

        val tmpDirectoryName = "tmp"
        val testFileName = "test_zip_file"

        val tmpFolder = File(tmpDirectoryName)
        tmpFolder.mkdir()
        Assertions.assertTrue(tmpFolder.exists())

        val zipFile = File("$tmpDirectoryName/$testFileName${Extension.TXT.extension}")
        val zipToPath = "$tmpDirectoryName/$testFileName${Extension.ZIP.extension}"
        zipFile.createNewFile()
        Assertions.assertTrue(zipFile.exists())

        BufferedWriter(FileWriter(zipFile, false)).use {
            it.write("Test String")
            it.newLine()
            it.write("Final String")
            it.newLine()
        }

        Assertions.assertDoesNotThrow { FileUtil.zip(zipFile.absolutePath, zipToPath) }

        val zippedFile = File(zipToPath)

        Assertions.assertTrue(zippedFile.exists())
        Assertions.assertEquals(855347585024L, zippedFile.totalSpace)

        Assertions.assertTrue(zipFile.delete())
        Assertions.assertTrue(FileUtil.unzip(zippedFile, tmpFolder))
        Assertions.assertTrue(zipFile.exists())

        val contents = FileUtil.readFileContents(zipFile)
        Assertions.assertFalse(contents.isEmpty())
        Assertions.assertEquals(contents, "Test String\r\nFinal String\r\n")

        Assertions.assertTrue(OsUtil.deleteFile(File(tmpDirectoryName), false))
    }

    /**
     * Tests for the close if not null method.
     */
    @Test
    fun testCloseIfNotNull() {
        val tmpFolder = File("tmp")
        Assertions.assertTrue(tmpFolder.mkdir())
        Assertions.assertTrue(tmpFolder.exists())

        val tmpFile = File("tmp/tmp.txt")
        Assertions.assertTrue(tmpFile.createNewFile())
        Assertions.assertTrue(tmpFile.exists())

        Assertions.assertDoesNotThrow { FileUtil.closeIfNotNull(null) }
        Assertions.assertDoesNotThrow { FileUtil.closeIfNotNull { } }
        Assertions.assertDoesNotThrow { FileUtil.closeIfNotNull(FileReader(tmpFile)) }
        Assertions.assertDoesNotThrow { FileUtil.closeIfNotNull(BufferedReader(FileReader(tmpFile))) }
        Assertions.assertDoesNotThrow { FileUtil.closeIfNotNull(FileWriter(tmpFile)) }
        Assertions.assertDoesNotThrow { FileUtil.closeIfNotNull(BufferedWriter(FileWriter(tmpFile))) }

        Assertions.assertTrue(OsUtil.deleteFile(tmpFolder, false))
    }

    /**
     * Tests for the construct unique name method.
     */
    @Test
    fun testConstructUniqueName() {
        val tmpDirectory = File("tmp")
        Assertions.assertTrue(tmpDirectory.mkdir())

        val tmpFileName = "tmp_file"
        val tmpFile = File("tmp/$tmpFileName.txt")
        Assertions.assertTrue(tmpFile.createNewFile())

        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.constructUniqueName(null, File(""))
        }
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.constructUniqueName(File(""), null)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.constructUniqueName(File("/"), File("directory_that_does_not_exist"))
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.constructUniqueName(File("/"), tmpFile)
        }
        Assertions.assertEquals(tmpFileName + "_1.txt", FileUtil.constructUniqueName(tmpFile, tmpDirectory))

        Assertions.assertTrue(OsUtil.deleteFile(tmpDirectory, false))
    }

    /**
     * Tests for the get file method.
     */
    @Test
    fun testGetFiles() {
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.getFiles(null, false, "")
        }
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.getFiles(File("."), false, null)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.getFiles(File("directory_that_does_not_exist"), false, "")
        }

        Assertions.assertEquals(7, FileUtil.getFiles(File("."), false, "").size)
        Assertions.assertTrue(FileUtil.getFiles(File("."), true, "").size > 100)
    }

    /**
     * Tests for the get folder method.
     */
    @Test
    fun testGetFolders() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.getFolders(null) }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.getFolders(File("folder_that_does_not_exist"))
        }
        Assertions.assertEquals(13, FileUtil.getFolders(File("."), false).size)
        Assertions.assertTrue(FileUtil.getFolders(File(".")).size >= 474)
    }

    /**
     * Tests for the read file contents method.
     */
    @Test
    fun readFileContents() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.readFileContents(null) }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.readFileContents(File("file_that_does_not_exist"))
        }

        Assertions.assertDoesNotThrow { FileUtil.readFileContents(File(".gitignore")) }
        Assertions.assertEquals(".idea/\r\nbin/\r\nout/\r\n*.iml\r\n*.git\r\n.gradle/*"
                + "\r\nbuild/\r\nprops/propkeys.ini\r\n__pycache__/\r\nvenv/\r\ndynamic/logs/",
                FileUtil.readFileContents(File(".gitignore")))
    }

    /**
     * Tests for the get hex string method.
     */
    @Test
    fun testGetHexString() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.getHexString(null) }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.getHexString(File("file_that_does_not_exist.bin"))
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.getHexString(File(".gitignore"))
        }
    }

    /**
     * Tests for the get binary string method.
     */
    @Test
    fun testGetBinaryString() {
        Assertions.assertThrows(NullPointerException::class.java) { FileUtil.getBinaryString(null) }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.getBinaryString(File("file_that_does_not_exist.bin"))
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.getBinaryString(File(".gitignore"))
        }
    }

    /**
     * Tests for the get file lines method.
     */
    @Test
    fun testGetFileLines() {
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.getFileLines(null)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.getFileLines(File("file_that_does_not_exist.txt"))
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.getFileLines(File("src"))
        }

        Assertions.assertEquals(ImmutableList.of(".idea/",
                "bin/",
                "out/",
                "*.iml",
                "*.git",
                ".gradle/*",
                "build/",
                "props/propkeys.ini",
                "__pycache__/",
                "venv/",
                "dynamic/logs/"), FileUtil.getFileLines(File(".gitignore")))
    }

    /**
     * Tests for the write lines to file method.
     */
    @Test
    fun testWriteLinesToFile() {
        Assertions.assertThrows(NullPointerException::class.java) {
            FileUtil.writeLinesToFile(null, null, false)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.writeLinesToFile(File("file_that_does_not_exist"), null, false)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            FileUtil.writeLinesToFile(File("."), null, false)
        }

        val tmpDir = File("tmp")
        Assertions.assertTrue(tmpDir.mkdir())

        val tmpFile = File("tmp/tmp.txt")
        Assertions.assertTrue(tmpFile.createNewFile())

        val writeLines = ImmutableList.of("one", "two", "three", "four", "five")
        Assertions.assertDoesNotThrow { FileUtil.writeLinesToFile(tmpFile, writeLines, false) }
        Assertions.assertEquals(writeLines,
                ImmutableList.copyOf(FileUtil.readFileContents(tmpFile).trim().split("\r\n")))

        Assertions.assertTrue(OsUtil.deleteFile(tmpDir, false))
    }
}