package cyder.audio

import main.java.cyder.audio.AudioIcons
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests for [AudioIcons]s.
 */
class AudioIconsTest {
    /**
     * Tests for loading audio icons.
     */
    @Test
    fun testAudioIcons() {
        Assertions.assertNotNull(AudioIcons.lastIcon)
        Assertions.assertEquals(30, AudioIcons.lastIcon.iconWidth)
        Assertions.assertEquals(30, AudioIcons.lastIcon.iconHeight)

        Assertions.assertNotNull(AudioIcons.lastIconHover)
        Assertions.assertEquals(30, AudioIcons.lastIconHover.iconWidth)
        Assertions.assertEquals(30, AudioIcons.lastIconHover.iconHeight)

        Assertions.assertNotNull(AudioIcons.nextIcon)
        Assertions.assertEquals(30, AudioIcons.nextIcon.iconWidth)
        Assertions.assertEquals(30, AudioIcons.nextIcon.iconHeight)

        Assertions.assertNotNull(AudioIcons.nextIconHover)
        Assertions.assertEquals(30, AudioIcons.nextIconHover.iconWidth)
        Assertions.assertEquals(30, AudioIcons.nextIconHover.iconHeight)

        Assertions.assertNotNull(AudioIcons.pauseIcon)
        Assertions.assertEquals(30, AudioIcons.pauseIcon.iconWidth)
        Assertions.assertEquals(30, AudioIcons.pauseIcon.iconHeight)

        Assertions.assertNotNull(AudioIcons.pauseIconHover)
        Assertions.assertEquals(30, AudioIcons.pauseIconHover.iconWidth)
        Assertions.assertEquals(30, AudioIcons.pauseIconHover.iconHeight)

        Assertions.assertNotNull(AudioIcons.playIcon)
        Assertions.assertEquals(30, AudioIcons.playIcon.iconWidth)
        Assertions.assertEquals(30, AudioIcons.playIcon.iconHeight)

        Assertions.assertNotNull(AudioIcons.playIconHover)
        Assertions.assertEquals(30, AudioIcons.playIconHover.iconWidth)
        Assertions.assertEquals(30, AudioIcons.playIconHover.iconHeight)

        Assertions.assertNotNull(AudioIcons.shuffleIcon)
        Assertions.assertEquals(30, AudioIcons.shuffleIcon.iconWidth)
        Assertions.assertEquals(30, AudioIcons.shuffleIcon.iconHeight)

        Assertions.assertNotNull(AudioIcons.shuffleIconHover)
        Assertions.assertEquals(30, AudioIcons.shuffleIconHover.iconWidth)
        Assertions.assertEquals(30, AudioIcons.shuffleIconHover.iconHeight)

        Assertions.assertNotNull(AudioIcons.repeatIcon)
        Assertions.assertEquals(30, AudioIcons.repeatIcon.iconWidth)
        Assertions.assertEquals(30, AudioIcons.repeatIcon.iconHeight)

        Assertions.assertNotNull(AudioIcons.repeatIconHover)
        Assertions.assertEquals(30, AudioIcons.repeatIconHover.iconWidth)
        Assertions.assertEquals(30, AudioIcons.repeatIconHover.iconHeight)
    }
}