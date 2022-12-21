package cyder.audio

import main.java.cyder.audio.WaveFile
import main.java.cyder.utils.StaticUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File

/**
 * Tests for [WaveFile]s.
 */
class WavFileTest {
    /**
     * Tests for creation of a WavFile.
     */
    @Test
    fun testCreation() {
        Assertions.assertThrows(NullPointerException::class.java) { WaveFile(null) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { WaveFile(File("")) }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            WaveFile(StaticUtil.getStaticResource("223.mp3"))
        }
    }

    /**
     * Tests for the toString method of WavFiles.
     */
    @Test
    fun testToString() {
        var starsWavFile: WaveFile? = null
        assertDoesNotThrow { starsWavFile = WaveFile(StaticUtil.getStaticResource("allthestars.wav")) }
        Assertions.assertEquals("WaveFile{numChannels=2, dataLength=44221200, isPlayable=true,"
                + " audioFormat=PCM_SIGNED 48000.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian,"
                + " clip=null, sampleSize=2, numFrames=11055300, sampleRate=48000,"
                + " wavFile=static\\audio\\allthestars.wav}", starsWavFile.toString())

        var commandoWavFile: WaveFile? = null
        assertDoesNotThrow { commandoWavFile = WaveFile(StaticUtil.getStaticResource("commando.wav")) }
        Assertions.assertEquals("WaveFile{numChannels=2, dataLength=37384192, isPlayable=true,"
                + " audioFormat=PCM_SIGNED 44100.0 Hz, 16 bit, stereo, 4 bytes/frame, little-endian,"
                + " clip=null, sampleSize=2, numFrames=9346048, sampleRate=44100,"
                + " wavFile=static\\audio\\commando.wav}", commandoWavFile.toString())
    }

    /**
     * Tests for the hashCode method of WavFiles.
     */
    @Test
    fun testHashCode() {
        var starsWavFile: WaveFile? = null
        assertDoesNotThrow { starsWavFile = WaveFile(StaticUtil.getStaticResource("allthestars.wav")) }
        Assertions.assertEquals(1235034749, starsWavFile.hashCode())

        var commandoWavFile: WaveFile? = null
        assertDoesNotThrow { commandoWavFile = WaveFile(StaticUtil.getStaticResource("commando.wav")) }
        Assertions.assertEquals(-160793697, commandoWavFile.hashCode())

        Assertions.assertNotEquals(starsWavFile.hashCode(), commandoWavFile.hashCode())
    }

    /**
     * Tests for the equals method of WavFiles.
     */
    @Test
    fun testEquals() {
        var starsWavFile: WaveFile? = null
        assertDoesNotThrow { starsWavFile = WaveFile(StaticUtil.getStaticResource("allthestars.wav")) }
        Assertions.assertEquals(starsWavFile, starsWavFile)

        var commandoWavFile: WaveFile? = null
        assertDoesNotThrow { commandoWavFile = WaveFile(StaticUtil.getStaticResource("commando.wav")) }
        Assertions.assertEquals(commandoWavFile, commandoWavFile)

        Assertions.assertNotEquals(starsWavFile, commandoWavFile)
    }
}