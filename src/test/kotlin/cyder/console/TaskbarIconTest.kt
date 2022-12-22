package cyder.console

import main.java.cyder.console.TaskbarIcon
import main.java.cyder.constants.CyderColors
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests for [TaskbarIcon]s.
 */
class TaskbarIconTest {
    /**
     * Tests for creating taskbar icons.
     */
    @Test
    fun testCreation() {
        Assertions.assertThrows(NullPointerException::class.java) { TaskbarIcon.Builder(null) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { TaskbarIcon.Builder("") }

        val builder = TaskbarIcon.Builder("Something")

        Assertions.assertThrows(java.lang.NullPointerException::class.java) { builder.borderColor = null }
        Assertions.assertThrows(java.lang.NullPointerException::class.java) { builder.customIcon = null }
        Assertions.assertThrows(java.lang.NullPointerException::class.java) { builder.runnable = null }

        Assertions.assertDoesNotThrow { builder.borderColor = CyderColors.regularPurple }
        Assertions.assertDoesNotThrow {
            builder.runnable = Runnable {
                var i = 0
                i += 1
                i -= 1
            }
        }
    }

    /**
     * Tests for equality between taskbar icons.
     */
    @Test
    fun testEquals() {
        val firstBuilder = TaskbarIcon.Builder("Something")
        val secondBuilder = TaskbarIcon.Builder("Something")
        val thirdBuilder = TaskbarIcon.Builder("Something different")

        Assertions.assertEquals(firstBuilder, secondBuilder)
        Assertions.assertEquals(secondBuilder, firstBuilder)
        Assertions.assertEquals(firstBuilder, firstBuilder)
        Assertions.assertEquals(secondBuilder, secondBuilder)

        Assertions.assertNotEquals(firstBuilder, thirdBuilder)
        Assertions.assertNotEquals(secondBuilder, thirdBuilder)
    }

    /**
     * Tests for hashcode of taskbar icons
     */
    @Test
    fun testHashcode() {
        val firstBuilder = TaskbarIcon.Builder("Something")
        val secondBuilder = TaskbarIcon.Builder("Something")
        val thirdBuilder = TaskbarIcon.Builder("Something different")

        Assertions.assertEquals(firstBuilder.hashCode(), secondBuilder.hashCode())
        Assertions.assertEquals(secondBuilder.hashCode(), firstBuilder.hashCode())
        Assertions.assertEquals(firstBuilder.hashCode(), firstBuilder.hashCode())
        Assertions.assertEquals(secondBuilder.hashCode(), secondBuilder.hashCode())

        Assertions.assertNotEquals(firstBuilder.hashCode(), thirdBuilder.hashCode())
        Assertions.assertNotEquals(secondBuilder.hashCode(), thirdBuilder.hashCode())
    }

    /**
     * Tests for to string of taskbar icons
     */
    @Test
    fun testToString() {
        val firstBuilder = TaskbarIcon.Builder("Something")
        val secondBuilder = TaskbarIcon.Builder("Something")
        val thirdBuilder = TaskbarIcon.Builder("Something different").setFocused(true).setCompact(true)

        Assertions.assertEquals("TaskbarIconBuilder{compact: false, focused: false,"
                + " borderColor: null, customIcon: null, runnable: null, name: \"Something\"}",
                firstBuilder.toString())
        Assertions.assertEquals("TaskbarIconBuilder{compact: false, focused: false,"
                + " borderColor: null, customIcon: null, runnable: null, name: \"Something\"}",
                secondBuilder.toString())
        Assertions.assertEquals("TaskbarIconBuilder{compact: true, focused: true,"
                + " borderColor: null, customIcon: null, runnable: null, name: \"Something different\"}", thirdBuilder.toString())

        Assertions.assertEquals(firstBuilder.toString(), secondBuilder.toString())
    }
}