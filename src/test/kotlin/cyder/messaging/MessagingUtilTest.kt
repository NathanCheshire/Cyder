package cyder.messaging

import cyder.constants.CyderColors
import cyder.enums.Dynamic
import cyder.enums.Extension
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
            MessagingUtil.generateWaveform(null, 0, 0, null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateWaveform(File("file_that_does_not_exist.mp3"),
                    0, 0, null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MessagingUtil.generateWaveform(File("."), 0, 0, null, null)
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
        assertTrue(savedImageBi.width == 500)
        assertTrue(savedImageBi.height == 250)

        assertTrue(OsUtil.deleteFile(tmpDir, false))
        OsUtil.deleteFile(Dynamic.TEMP.pointerFile, false)

        assertFalse(Dynamic.TEMP.pointerFile.exists())
    }

    /**
     * Tests for the generate audio preview label method.
     */
    @Test
    fun testGenerateAudioPreviewLabel() {

    }

    /**
     * Tests for the generate image preview label.
     */
    @Test
    fun testGenerateImagePreviewLabel() {

    }
}