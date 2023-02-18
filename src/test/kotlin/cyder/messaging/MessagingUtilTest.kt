package cyder.messaging

import cyder.constants.CyderColors
import cyder.enumerations.Dynamic
import cyder.enumerations.Extension
import cyder.utils.ImageUtil
import cyder.utils.OsUtil
import cyder.utils.StaticUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

/**
 * Tests for [MessagingUtil]s
 */
class MessagingUtilTest {
    /**
     * Tests for the generate waveform method.
     */
    @Test
    fun testGenerateWaveform() {
        assertThrows(NullPointerException::class.java) {
            MessagingUtil.generateWaveform(null, 0, 0,
                    null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateWaveform(File("file_that_does_not_exist.mp3"),
                    0, 0, null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateWaveform(File("."), 0, 0,
                    null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateWaveform(File(".gitignore"),
                    0, 0, null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateWaveform(StaticUtil.getStaticResource("223.mp3"),
                    0, 0, null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateWaveform(StaticUtil.getStaticResource("223.mp3"),
                    500, 0, null, null)
        }
        assertThrows(NullPointerException::class.java) {
            MessagingUtil.generateWaveform(StaticUtil.getStaticResource("223.mp3"),
                    500, 250, null, null)
        }
        assertThrows(NullPointerException::class.java) {
            MessagingUtil.generateWaveform(StaticUtil.getStaticResource("223.mp3"),
                    500, 250, CyderColors.vanilla, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateWaveform(StaticUtil.getStaticResource("223.mp3"),
                    500, 250, CyderColors.vanilla, CyderColors.vanilla)
        }

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val futureBi = MessagingUtil.generateWaveform(StaticUtil.getStaticResource("223.mp3"),
                500, 250, CyderColors.vanilla, CyderColors.navy)
        while (!futureBi.isDone) Thread.onSpinWait()
        val bi = futureBi.get()

        val outputFile = File("tmp/223_waveform.png")

        try {
            ImageIO.write(bi, Extension.PNG.extensionWithoutPeriod, outputFile)
        } catch (ignored: Exception) {
        }

        assertTrue(outputFile.exists())

        val savedImageBi = ImageUtil.read(outputFile)
        assertEquals(500, savedImageBi.width)
        assertEquals(250, savedImageBi.height)

        assertTrue(OsUtil.deleteFile(tmpDir, false))
        OsUtil.deleteFile(Dynamic.TEMP.pointerFile, false)

        assertFalse(Dynamic.TEMP.pointerFile.exists())
    }

    /**
     * Tests for the generate audio preview label method.
     */
    @Test
    fun testGenerateAudioPreviewLabel() {
        assertThrows(NullPointerException::class.java) {
            MessagingUtil.generateAudioPreviewLabel(null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateAudioPreviewLabel(File("file_that_does_not_exist.txt"), null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateAudioPreviewLabel(File("."), null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateAudioPreviewLabel(File(".gitignore"), null)
        }
        assertThrows(NullPointerException::class.java) {
            MessagingUtil.generateAudioPreviewLabel(StaticUtil.getStaticResource("223.mp3"), null)
        }

        val futureJLabel = MessagingUtil.generateAudioPreviewLabel(StaticUtil.getStaticResource("223.mp3")) { }
        while (!futureJLabel.isDone) Thread.onSpinWait()
        val jLabel = futureJLabel.get()

        assertNotNull(jLabel)

        val bi = ImageUtil.screenshotComponent(jLabel)

        assertNotNull(jLabel)
        assertEquals(150, bi.width)
        assertEquals(94, bi.height)
    }

    /**
     * Tests for the generate image preview label.
     */
    @Test
    fun testGenerateImagePreviewLabel() {
        assertThrows(NullPointerException::class.java) {
            MessagingUtil.generateImagePreviewLabel(null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateImagePreviewLabel(File("file_that_does_not_exist.txt"), null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateImagePreviewLabel(File("."), null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateImagePreviewLabel(File(".gitignore"), null)
        }
        assertThrows(NullPointerException::class.java) {
            MessagingUtil.generateImagePreviewLabel(StaticUtil.getStaticResource("Me.png"), null)
        }

        val jLabel = MessagingUtil.generateImagePreviewLabel(StaticUtil.getStaticResource("Me.png")) {}
        assertNotNull(jLabel)

        val bi = ImageUtil.screenshotComponent(jLabel)

        assertEquals(150, bi.width)
        assertEquals(150 + 40, bi.height)
    }
}