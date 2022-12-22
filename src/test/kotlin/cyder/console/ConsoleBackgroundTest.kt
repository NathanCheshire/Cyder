package cyder.console

import main.java.cyder.console.ConsoleBackground
import main.java.cyder.utils.ImageUtil
import main.java.cyder.utils.StaticUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Tests for [ConsoleBackground]s.
 */
class ConsoleBackgroundTest {
    /**
     * Tests for creating console backgrounds.
     */
    @Test
    fun testCreation() {
        Assertions.assertThrows(NullPointerException::class.java) { ConsoleBackground(null) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { ConsoleBackground(File("")) }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            ConsoleBackground(StaticUtil.getStaticResource("Gliders.json"))
        }

        Assertions.assertDoesNotThrow {
            ConsoleBackground(StaticUtil.getStaticResource("CyderIcon.png"))
        }
    }

    /**
     * Tests for generating buffered images/image icons from the console background's encapsulated file.
     */
    @Test
    fun testGeneration() {
        val file = StaticUtil.getStaticResource("CyderIcon.png")
        val background = ConsoleBackground(file)

        Assertions.assertEquals(background.referenceFile, file)
        Assertions.assertDoesNotThrow { background.generateBufferedImage() }
        Assertions.assertDoesNotThrow { background.generateImageIcon() }

        val bi = background.generateBufferedImage()
        Assertions.assertTrue(ImageUtil.areImagesEqual(bi, background.generateBufferedImage()))

        val imageIcon = background.generateImageIcon()
        Assertions.assertTrue(ImageUtil.areImagesEqual(imageIcon, background.generateImageIcon()))
    }

    /**
     * Tests for console background toString methods.
     */
    @Test
    fun testToString() {
        val cyderIconBackground = ConsoleBackground(StaticUtil.getStaticResource("CyderIcon.png"))
        Assertions.assertEquals("ConsoleBackground{referenceFile="
                + "C:\\Users\\Nathan\\Documents\\IntelliJava\\cyder\\static\\"
                + "pictures\\CyderIcon.png}", cyderIconBackground.toString())

        val defaultBackground = ConsoleBackground(StaticUtil.getStaticResource("Default.png"))
        Assertions.assertEquals("ConsoleBackground{referenceFile="
                + "C:\\Users\\Nathan\\Documents\\IntelliJava\\cyder\\static\\"
                + "pictures\\audio\\Default.png}", defaultBackground.toString())
    }

    /**
     * Tests for console background equals methods.
     */
    @Test
    fun testEquals() {
        val cyderIconBackground = ConsoleBackground(StaticUtil.getStaticResource("CyderIcon.png"))

        val defaultBackground = ConsoleBackground(StaticUtil.getStaticResource("Default.png"))

        Assertions.assertNotEquals(cyderIconBackground, defaultBackground)
        Assertions.assertEquals(cyderIconBackground, cyderIconBackground)
        Assertions.assertEquals(defaultBackground, defaultBackground)

    }

    /**
     * Tests for console background hashcode methods.
     */
    @Test
    fun testHashcode() {
        val cyderIconBackground = ConsoleBackground(StaticUtil.getStaticResource("CyderIcon.png"))

        val defaultBackground = ConsoleBackground(StaticUtil.getStaticResource("Default.png"))

        Assertions.assertNotEquals(cyderIconBackground.hashCode(), defaultBackground.hashCode())
        Assertions.assertEquals(cyderIconBackground.hashCode(), cyderIconBackground.hashCode())
        Assertions.assertEquals(defaultBackground.hashCode(), defaultBackground.hashCode())
    }
}