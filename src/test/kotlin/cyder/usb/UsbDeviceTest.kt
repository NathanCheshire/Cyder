package cyder.usb

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Tests for [UsbDevice]s.
 */
class UsbDeviceTest {
    /**
     * Tests for creation of usb devices.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) {
            UsbDevice(null, null, null, null)
        }
        assertThrows(NullPointerException::class.java) {
            UsbDevice("", null, null, null)
        }
        assertThrows(NullPointerException::class.java) {
            UsbDevice("", "", null, null)
        }
        assertThrows(NullPointerException::class.java) {
            UsbDevice("", "", "", null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            UsbDevice("", "", "", "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            UsbDevice("a", "", "", "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            UsbDevice("a", "a", "", "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            UsbDevice("a", "a", "a", "")
        }

        assertDoesNotThrow { UsbDevice("a", "a", "a", "a") }
    }

    /**
     * Tests for the accessor methods.
     */
    @Test
    fun testAccessors() {
        val device = UsbDevice("status", "type", "name", "id")
        assertEquals("status", device.status)
        assertEquals("type", device.type)
        assertEquals("name", device.friendlyName)
        assertEquals("id", device.instanceId)
    }

    /**
     * Tests for the to string method.
     */
    @Test
    fun testToString() {
        val device = UsbDevice("status", "type", "name", "id")
        assertEquals("UsbDevice{status=\"status\", type=\"type\","
                + " friendlyName=\"name\", instanceId=\"id\"}", device.toString())
    }

    /**
     * Tests for the equals method.
     */
    @Test
    fun testEquals() {
        val device = UsbDevice("status", "type", "name", "id")
        val equalDevice = UsbDevice("status", "type", "name", "id")
        val nonEqualDevice = UsbDevice("other status", "type", "name", "id")

        assertEquals(device, device)
        assertEquals(device, equalDevice)
        assertNotEquals(equalDevice, nonEqualDevice)
        assertNotEquals(equalDevice, Object())
    }

    /**
     * Test for the hashcode method.
     */
    @Test
    fun testHashCode() {
        val device = UsbDevice("status", "type", "name", "id")
        val otherDevice = UsbDevice("other status", "type", "name", "id")

        assertEquals(-928692613, device.hashCode())
        assertEquals(875382395, otherDevice.hashCode())
    }
}