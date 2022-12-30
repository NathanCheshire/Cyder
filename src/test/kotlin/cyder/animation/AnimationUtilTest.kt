package cyder.animation

import cyder.enums.Direction
import cyder.threads.ThreadUtil
import cyder.ui.frame.CyderFrame
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Frame
import javax.swing.JFrame

/**
 * Tests for the [AnimationUtil] methods.
 */
class AnimationUtilTest {
    /**
     * Tests for the close animation method.
     */
    @Test
    fun testCloseAnimation() {
        val frame = Frame()
        Assertions.assertDoesNotThrow { AnimationUtil.closeAnimation(frame) }

        val jFrame = JFrame()
        Assertions.assertDoesNotThrow { AnimationUtil.closeAnimation(jFrame) }

        val cyderFrame = CyderFrame()
        Assertions.assertDoesNotThrow { AnimationUtil.closeAnimation(cyderFrame) }
    }

    /**
     * Tests for the minimize animation method.
     */
    @Test
    fun testMinimizeAnimation() {
        val jFrame = JFrame()
        jFrame.isVisible = true
        Assertions.assertDoesNotThrow { AnimationUtil.minimizeAnimation(jFrame) }
        Assertions.assertEquals(JFrame.ICONIFIED, jFrame.state)

        val cyderFrame = CyderFrame()
        cyderFrame.isVisible = true
        Assertions.assertDoesNotThrow { AnimationUtil.minimizeAnimation(cyderFrame) }
        Assertions.assertEquals(JFrame.ICONIFIED, jFrame.state)
    }

    /**
     * Tests for animating a component moving in a cardinal direction.
     */
    @Test
    fun testAnimateComponentMovement() {
        Assertions.assertThrows(NullPointerException::class.java) {
            AnimationUtil.animateComponentMovement(null,
                    0, 0, 0, 0, CyderFrame())
        }

        Assertions.assertThrows(NullPointerException::class.java) {
            AnimationUtil.animateComponentMovement(Direction.LEFT,
                    0, 0, 0, 0, null)
        }

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            AnimationUtil.animateComponentMovement(Direction.LEFT,
                    0, 0, 0, 0, CyderFrame())
        }

        val increment = 2
        val delay = 2
        val upFrame = CyderFrame()
        upFrame.setLocation(0, 100)
        Assertions.assertDoesNotThrow {
            AnimationUtil.animateComponentMovement(Direction.TOP,
                    100, 0, delay, increment, upFrame)
        }
        ThreadUtil.sleep((delay * 200).toLong())
        Assertions.assertEquals(0, upFrame.y)

        val downFrame = CyderFrame()
        downFrame.setLocation(0, 0)
        Assertions.assertDoesNotThrow {
            AnimationUtil.animateComponentMovement(Direction.BOTTOM,
                    0, 100, delay, increment, downFrame)
        }
        ThreadUtil.sleep((delay * 200).toLong())
        Assertions.assertEquals(100, downFrame.y)

        val leftFrame = CyderFrame()
        leftFrame.setLocation(100, 0)
        Assertions.assertDoesNotThrow {
            AnimationUtil.animateComponentMovement(Direction.LEFT,
                    100, 0, delay, increment, leftFrame)
        }
        ThreadUtil.sleep((delay * 200).toLong())
        Assertions.assertEquals(0, leftFrame.x)

        val rightFrame = CyderFrame()
        rightFrame.setLocation(0, 0)
        Assertions.assertDoesNotThrow {
            AnimationUtil.animateComponentMovement(Direction.RIGHT,
                    0, 100, delay, increment, rightFrame)
        }
        ThreadUtil.sleep((delay * 200).toLong())
        Assertions.assertEquals(100, rightFrame.x)
    }
}