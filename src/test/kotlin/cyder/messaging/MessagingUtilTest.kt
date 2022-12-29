package cyder.messaging

import cyder.constants.CyderColors
import cyder.utils.StaticUtil
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File

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

        // todo test for wav and mp3 succeeding and being of proper width and height
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