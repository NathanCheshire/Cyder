package cyder.threads

import cyder.strings.CyderStrings
import cyder.ui.pane.CyderOutputPane
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import javax.swing.JTextPane

/**
 * Tests for the [BletchyAnimationManager].
 */
class BletchyAnimationManagerTest {
    /**
     * Tests for the initialize method of the bletchy animation manager.
     */
    @Test
    fun testInitialize() {
        BletchyAnimationManager.INSTANCE.deconstruct()

        assertThrows(IllegalStateException::class.java) {
            BletchyAnimationManager.INSTANCE.bletchy(null, false, 0, false)
        }

        assertThrows(NullPointerException::class.java) {
            BletchyAnimationManager.INSTANCE.initialize(null)
        }

        val outputPane = CyderOutputPane(JTextPane())
        assertDoesNotThrow { BletchyAnimationManager.INSTANCE.initialize(outputPane) }
        assertThrows(IllegalStateException::class.java) { BletchyAnimationManager.INSTANCE.initialize(outputPane) }
    }

    /**
     * Tests for the bletchy method.
     */
    @Test
    fun testBletchy() {
        BletchyAnimationManager.INSTANCE.deconstruct()

        assertThrows(IllegalStateException::class.java) {
            BletchyAnimationManager.INSTANCE.bletchy(null, false, 0, false)
        }

        val outputPane = CyderOutputPane(JTextPane())
        BletchyAnimationManager.INSTANCE.initialize(outputPane)

        assertThrows(NullPointerException::class.java) {
            BletchyAnimationManager.INSTANCE.bletchy(null, false, 0, false)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BletchyAnimationManager.INSTANCE.bletchy("", false, 0, false)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BletchyAnimationManager.INSTANCE.bletchy("string",
                    false, -1, false)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BletchyAnimationManager.INSTANCE.bletchy("string",
                    false, 0, false)
        }

        assertDoesNotThrow {
            BletchyAnimationManager.INSTANCE.bletchy("string",
                    false, 10, false)
        }
    }

    /**
     * Tests for the is active and kill methods.
     */
    @Test
    fun testIsActive() {
        BletchyAnimationManager.INSTANCE.deconstruct()

        val outputPane = CyderOutputPane(JTextPane())
        BletchyAnimationManager.INSTANCE.initialize(outputPane)

        assertFalse(BletchyAnimationManager.INSTANCE.isActive)
        BletchyAnimationManager.INSTANCE.bletchy(CyderStrings.EUROPEAN_TOY_MAKER, false, 1, false)
        ThreadUtil.sleepSeconds(2)
        assertTrue(BletchyAnimationManager.INSTANCE.isActive)
        BletchyAnimationManager.INSTANCE.kill()
        assertFalse(BletchyAnimationManager.INSTANCE.isActive)
    }
}