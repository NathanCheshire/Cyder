package cyder.files


import main.java.cyder.files.DirectoryWatcher
import main.java.cyder.files.WatchDirectoryEvent
import main.java.cyder.files.WatchDirectorySubscriber
import main.java.cyder.threads.ThreadUtil
import main.java.cyder.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Tests for the [DirectoryWatcher].
 */
class DirectoryWatcherTest {
    /**
     * Tests for creation of directory watchers.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { DirectoryWatcher(null, 0) }
        assertThrows(IllegalArgumentException::class.java) {
            DirectoryWatcher(File("directory_that_does_not_exist"), 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            DirectoryWatcher(File(".gitignore"), 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            DirectoryWatcher(File("."), 0)
        }
        assertDoesNotThrow { DirectoryWatcher(File("."), 1000) }
    }

    /**
     * Tests for setters and getters of directory watchers.
     */
    @Test
    fun testAccessorsMutators() {
        val directoryWatcher = DirectoryWatcher(File("."), 1000)
        assertEquals(File("."), directoryWatcher.watchDirectory)
        assertEquals(1000, directoryWatcher.pollTimeout)
    }

    /**
     * Tests for starting/stopping watching.
     */
    @Test
    fun testStartStopWatching() {
        val directoryWatcher = DirectoryWatcher(File("."), 1000)

        assertFalse(directoryWatcher.isWatching)
        directoryWatcher.startWatching()
        assertTrue(directoryWatcher.isWatching)
        directoryWatcher.stopWatching()
        assertFalse(directoryWatcher.isWatching)
    }

    /**
     * Tests for adding and removing subscribers.
     */
    @Test
    fun testAddAndRemoveSubscribers() {
        val directoryWatcher = DirectoryWatcher(File("."), 1000)

        assertThrows(NullPointerException::class.java) { directoryWatcher.addSubscriber(null) }
        assertThrows(NullPointerException::class.java) { directoryWatcher.removeSubscriber(null) }

        val subscriber = object : WatchDirectorySubscriber() {
            override fun onEvent(broker: DirectoryWatcher?, event: WatchDirectoryEvent?, eventFile: File?) {
                // Empty subscriber
            }
        }

        assertDoesNotThrow { directoryWatcher.addSubscriber(subscriber) }
        assertThrows(IllegalStateException::class.java) { directoryWatcher.addSubscriber(subscriber) }

        assertDoesNotThrow { directoryWatcher.removeSubscriber(subscriber) }
        assertThrows(IllegalStateException::class.java) { directoryWatcher.removeSubscriber(subscriber) }
    }

    /**
     * Tests for each [WatchDirectoryEvent] a [DirectoryWatcher] can push to subscribers.
     */
    @Test
    fun testDirectoryWatchEventPushing() {
        val pollTimeout = 5
        val pollMagnitudeTestingDelays = 8

        val tmpDirectory = File("tmp")
        assertTrue(tmpDirectory.mkdir())

        val directoryWatcher = DirectoryWatcher(tmpDirectory, pollTimeout)
        directoryWatcher.startWatching()

        val fileAddedEventHandled = AtomicBoolean()
        val fileAddedSubscriber = object : WatchDirectorySubscriber() {
            override fun onEvent(broker: DirectoryWatcher?, event: WatchDirectoryEvent?, eventFile: File?) {
                fileAddedEventHandled.set(true)
            }
        }
        fileAddedSubscriber.subscribeTo(WatchDirectoryEvent.FILE_ADDED)
        directoryWatcher.addSubscriber(fileAddedSubscriber)
        val tmpFile = File("tmp/temp.txt")
        assertTrue(tmpFile.createNewFile())
        ThreadUtil.sleep(pollMagnitudeTestingDelays * pollTimeout.toLong())
        assertTrue(fileAddedEventHandled.get())

        val fileDeletedEventHandled = AtomicBoolean()
        val fileDeletedSubscriber = object : WatchDirectorySubscriber() {
            override fun onEvent(broker: DirectoryWatcher?, event: WatchDirectoryEvent?, eventFile: File?) {
                fileDeletedEventHandled.set(true)
            }
        }
        fileDeletedSubscriber.subscribeTo(WatchDirectoryEvent.FILE_DELETED)
        directoryWatcher.addSubscriber(fileDeletedSubscriber)
        assertTrue(tmpFile.delete())
        ThreadUtil.sleep(pollMagnitudeTestingDelays * pollTimeout.toLong())
        assertTrue(fileDeletedEventHandled.get())



        assertDoesNotThrow { directoryWatcher.stopWatching() }
        assertTrue(OsUtil.deleteFile(tmpDirectory, false))
    }
}