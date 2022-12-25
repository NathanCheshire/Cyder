package cyder.files

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Tests for [WatchDirectorySubscriber]s.
 */
class WatchDirectorySubscriberTest {
    /**
     * Tests for creating subscribers.
     */
    @Test
    fun testCreation() {
        assertDoesNotThrow {
            object : WatchDirectorySubscriber() {
                override fun onEvent(broker: DirectoryWatcher?, event: WatchDirectoryEvent?, eventFile: File?) {
                    // Empty
                }
            }
        }
    }

    /**
     * Tests for subscribing to events.
     */
    @Test
    fun testSubscribeTo() {
        val subscriber = object : WatchDirectorySubscriber() {
            override fun onEvent(broker: DirectoryWatcher?, event: WatchDirectoryEvent?, eventFile: File?) {
                // Empty
            }
        }

        assertThrows(NullPointerException::class.java) { subscriber.subscribeTo(null) }
        assertThrows(NullPointerException::class.java) {
            subscriber.subscribeTo(WatchDirectoryEvent.DIRECTORY_MODIFIED, null)
        }
        assertThrows(NullPointerException::class.java) {
            subscriber.subscribeTo(WatchDirectoryEvent.FILE_MODIFIED, null, null)
        }
        assertThrows(IllegalStateException::class.java) {
            subscriber.subscribeTo(WatchDirectoryEvent.FILE_MODIFIED)
        }
        assertEquals(ImmutableList.of(WatchDirectoryEvent.DIRECTORY_MODIFIED,
                WatchDirectoryEvent.FILE_MODIFIED), subscriber.subscriptions)
    }

    /**
     * Tests for the regex setters of watch directory subscribers.
     */
    @Test
    fun testSetters() {
        val subscriber = object : WatchDirectorySubscriber() {
            override fun onEvent(broker: DirectoryWatcher?, event: WatchDirectoryEvent?, eventFile: File?) {
                // Empty
            }
        }

        assertThrows(NullPointerException::class.java) { subscriber.setFileNameRegex(null) }
        assertThrows(IllegalArgumentException::class.java) { subscriber.setFileNameRegex("") }
        assertDoesNotThrow { subscriber.setFileNameRegex(".*") }
        assertEquals(".*", subscriber.fileNameRegex.pattern())

        assertThrows(NullPointerException::class.java) { subscriber.setFileExtensionRegex(null) }
        assertThrows(IllegalArgumentException::class.java) { subscriber.setFileExtensionRegex("") }
        assertDoesNotThrow { subscriber.setFileExtensionRegex(".*") }
        assertEquals(".*", subscriber.fileExtensionRegex.pattern())

        assertThrows(NullPointerException::class.java) { subscriber.setFileRegex(null) }
        assertThrows(IllegalArgumentException::class.java) { subscriber.setFileRegex("") }
        assertDoesNotThrow { subscriber.setFileRegex(".*") }
        assertEquals(".*", subscriber.fileRegex.pattern())

        assertThrows(NullPointerException::class.java) { subscriber.setDirectoryRegex(null) }
        assertThrows(IllegalArgumentException::class.java) { subscriber.setDirectoryRegex("") }
        assertDoesNotThrow { subscriber.setDirectoryRegex(".*") }
        assertEquals(".*", subscriber.directoryRegex.pattern())
    }
}