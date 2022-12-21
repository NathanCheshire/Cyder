package cyder.audio;

import main.java.cyder.audio.AudioIcons;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link main.java.cyder.audio.AudioIcons}s
 */
public class AudioIconsTest {
    /**
     * Default constructor for JUnit.
     */
    public AudioIconsTest() {}

    /**
     * Tests for loading audio icons.
     */
    @Test
    void testAudioIcons() {
        assertNotNull(AudioIcons.lastIcon);
        assertEquals(30, AudioIcons.lastIcon.getIconWidth());
        assertEquals(30, AudioIcons.lastIcon.getIconHeight());

        assertNotNull(AudioIcons.lastIconHover);
        assertEquals(30, AudioIcons.lastIconHover.getIconWidth());
        assertEquals(30, AudioIcons.lastIconHover.getIconHeight());

        assertNotNull(AudioIcons.nextIcon);
        assertEquals(30, AudioIcons.nextIcon.getIconWidth());
        assertEquals(30, AudioIcons.nextIcon.getIconHeight());

        assertNotNull(AudioIcons.nextIconHover);
        assertEquals(30, AudioIcons.nextIconHover.getIconWidth());
        assertEquals(30, AudioIcons.nextIconHover.getIconHeight());

        assertNotNull(AudioIcons.pauseIcon);
        assertEquals(30, AudioIcons.pauseIcon.getIconWidth());
        assertEquals(30, AudioIcons.pauseIcon.getIconHeight());

        assertNotNull(AudioIcons.pauseIconHover);
        assertEquals(30, AudioIcons.pauseIconHover.getIconWidth());
        assertEquals(30, AudioIcons.pauseIconHover.getIconHeight());

        assertNotNull(AudioIcons.playIcon);
        assertEquals(30, AudioIcons.playIcon.getIconWidth());
        assertEquals(30, AudioIcons.playIcon.getIconHeight());

        assertNotNull(AudioIcons.playIconHover);
        assertEquals(30, AudioIcons.playIconHover.getIconWidth());
        assertEquals(30, AudioIcons.playIconHover.getIconHeight());

        assertNotNull(AudioIcons.shuffleIcon);
        assertEquals(30, AudioIcons.shuffleIcon.getIconWidth());
        assertEquals(30, AudioIcons.shuffleIcon.getIconHeight());

        assertNotNull(AudioIcons.shuffleIconHover);
        assertEquals(30, AudioIcons.shuffleIconHover.getIconWidth());
        assertEquals(30, AudioIcons.shuffleIconHover.getIconHeight());

        assertNotNull(AudioIcons.repeatIcon);
        assertEquals(30, AudioIcons.repeatIcon.getIconWidth());
        assertEquals(30, AudioIcons.repeatIcon.getIconHeight());

        assertNotNull(AudioIcons.repeatIconHover);
        assertEquals(30, AudioIcons.repeatIconHover.getIconWidth());
        assertEquals(30, AudioIcons.repeatIconHover.getIconHeight());
    }
}
