package cyder.files


import com.google.common.collect.ImmutableList
import cyder.threads.ThreadUtil
import cyder.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
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
     * Test all directory watch event publishing for multiple poll values.
     */
    @Test
    fun testDirectoryWatchEventPublishing() {
        val pollDelays = ImmutableList.of(5, 10, 50, 75, 100, 200, 500)
        pollDelays.stream().forEach { innerTestDirectoryWatchEventPublishing(it) }
    }

    /**
     * Tests for each [WatchDirectoryEvent] a [DirectoryWatcher] can push to subscribers.
     */
    private fun innerTestDirectoryWatchEventPublishing(pollTimeout: Int = 8) {
        val pollMagnitudeTestingDelays = 8

        val tmpDirectory = File("tmp")
        tmpDirectory.mkdir()
        assertTrue(tmpDirectory.exists())

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

        val directoryAddedEventHandled = AtomicBoolean()
        val directoryAddedSubscriber = object : WatchDirectorySubscriber() {
            override fun onEvent(broker: DirectoryWatcher?, event: WatchDirectoryEvent?, eventFile: File?) {
                directoryAddedEventHandled.set(true)
            }
        }
        directoryAddedSubscriber.subscribeTo(WatchDirectoryEvent.DIRECTORY_ADDED)
        directoryWatcher.addSubscriber(directoryAddedSubscriber)
        val innerTempDirectory = File("tmp/temp_directory")
        assertTrue(innerTempDirectory.mkdir())
        ThreadUtil.sleep(pollMagnitudeTestingDelays * pollTimeout.toLong())
        assertTrue(directoryAddedEventHandled.get())

        val directoryRemovedEventHandled = AtomicBoolean()
        val directoryRemovedSubscriber = object : WatchDirectorySubscriber() {
            override fun onEvent(broker: DirectoryWatcher?, event: WatchDirectoryEvent?, eventFile: File?) {
                directoryRemovedEventHandled.set(true)
            }
        }
        directoryRemovedSubscriber.subscribeTo(WatchDirectoryEvent.DIRECTORY_DELETED)
        directoryWatcher.addSubscriber(directoryRemovedSubscriber)
        assertTrue(innerTempDirectory.delete())
        ThreadUtil.sleep(pollMagnitudeTestingDelays * pollTimeout.toLong())
        assertTrue(directoryRemovedEventHandled.get())

        val fileToModify = File("tmp/file_to_modify.txt")
        assertTrue(fileToModify.createNewFile())
        val fileModifyEventHandled = AtomicBoolean()
        val fileModifiedSubscriber = object : WatchDirectorySubscriber() {
            override fun onEvent(broker: DirectoryWatcher?, event: WatchDirectoryEvent?, eventFile: File?) {
                fileModifyEventHandled.set(true)
            }
        }
        fileModifiedSubscriber.subscribeTo(WatchDirectoryEvent.FILE_MODIFIED)
        directoryWatcher.addSubscriber(fileModifiedSubscriber)
        ThreadUtil.sleep(pollMagnitudeTestingDelays * pollTimeout.toLong())
        BufferedWriter(FileWriter(fileToModify)).use {
            it.write("Text")
            it.newLine()
        }
        ThreadUtil.sleep(pollMagnitudeTestingDelays * pollTimeout.toLong())
        assertTrue(fileModifyEventHandled.get())

        val createSubDir = File("tmp/temp_directory")
        assertTrue(createSubDir.mkdir())
        val directoryModifyEventHandled = AtomicBoolean()
        val directoryModifiedSubscriber = object : WatchDirectorySubscriber() {
            override fun onEvent(broker: DirectoryWatcher?, event: WatchDirectoryEvent?, eventFile: File?) {
                directoryModifyEventHandled.set(true)
            }
        }
        directoryModifiedSubscriber.subscribeTo(WatchDirectoryEvent.DIRECTORY_MODIFIED)
        directoryWatcher.addSubscriber(directoryModifiedSubscriber)
        ThreadUtil.sleep(pollMagnitudeTestingDelays * pollTimeout.toLong())
        val fileToAdd = File("tmp/temp_directory/add_file.txt")
        assertTrue(fileToAdd.createNewFile())
        ThreadUtil.sleep(pollMagnitudeTestingDelays * pollTimeout.toLong())
        assertTrue(directoryModifyEventHandled.get())

        assertDoesNotThrow { directoryWatcher.stopWatching() }
        assertTrue(OsUtil.deleteFile(tmpDirectory, false))
    }
}