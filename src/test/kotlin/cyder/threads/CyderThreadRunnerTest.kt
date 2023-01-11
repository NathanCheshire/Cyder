package cyder.threads

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Tests for the [CyderThreadRunner].
 */
class CyderThreadRunnerTest {
    /**
     * Tests for the get threads ran method.
     */
    @Test
    fun testGetThreadsRan() {
        assertDoesNotThrow { CyderThreadRunner.getThreadsRan() }
        val threadsRan = CyderThreadRunner.getThreadsRan()
        CyderThreadRunner.submit({}, "name")
        assertEquals(threadsRan + 1, CyderThreadRunner.getThreadsRan())
    }

    /**
     * Tests for the submit method.
     */
    @Test
    fun testSubmit() {
        assertThrows(NullPointerException::class.java) { CyderThreadRunner.submit(null, null) }
        assertThrows(NullPointerException::class.java) { CyderThreadRunner.submit({}, null) }
        assertThrows(IllegalArgumentException::class.java) { CyderThreadRunner.submit({}, "") }

        assertDoesNotThrow { CyderThreadRunner.submit({}, "name") }
    }

    /**
     * Tests for the submit supplier method.
     */
    @Test
    fun testSubmitSupplier() {
        assertThrows(NullPointerException::class.java) { CyderThreadRunner.submitSupplier({}, null) }
        assertThrows(IllegalArgumentException::class.java) { CyderThreadRunner.submitSupplier({}, "") }

        assertDoesNotThrow { CyderThreadRunner.submitSupplier({}, "name") }
    }

    /**
     * Tests for the create thread method.
     */
    @Test
    fun testCreateThread() {
        assertThrows(NullPointerException::class.java) { CyderThreadRunner.createThread(null, null) }
        assertThrows(NullPointerException::class.java) { CyderThreadRunner.createThread({}, null) }
        assertThrows(IllegalArgumentException::class.java) { CyderThreadRunner.createThread({}, "") }

        assertDoesNotThrow { CyderThreadRunner.createThread({}, "name") }

        val thread = CyderThreadRunner.createThread({}, "name")
        assertEquals("name", thread.name)
    }

    /**
     * Tests for the schedule at fixed rate method.
     */
    @Test
    fun testScheduleAtFixedRate() {
        assertThrows(NullPointerException::class.java) {
            CyderThreadRunner.scheduleAtFixedRate(null, null, null, null)
        }
        assertThrows(NullPointerException::class.java) {
            CyderThreadRunner.scheduleAtFixedRate({}, null, null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            CyderThreadRunner.scheduleAtFixedRate({}, "", null, null)
        }
        assertThrows(NullPointerException::class.java) {
            CyderThreadRunner.scheduleAtFixedRate({}, "name", null, null)
        }
        assertDoesNotThrow {
            CyderThreadRunner.scheduleAtFixedRate({}, "name", Duration.ofSeconds(5), null)
        }

        val quit = AtomicBoolean()
        val counter = AtomicInteger()

        CyderThreadRunner.scheduleAtFixedRate(
                { counter.incrementAndGet() },
                "Incrementer",
                Duration.ofSeconds(1),
                quit
        )

        assertEquals(0, counter.get())
        ThreadUtil.sleepSeconds(1)
        assertEquals(1, counter.get())
        ThreadUtil.sleepSeconds(1)
        assertEquals(2, counter.get())
        ThreadUtil.sleepSeconds(1)
        assertEquals(3, counter.get())
        ThreadUtil.sleepSeconds(1)
        assertEquals(4, counter.get())
        ThreadUtil.sleepSeconds(1)
        assertEquals(5, counter.get())
        ThreadUtil.sleepSeconds(1)
        assertEquals(6, counter.get())
        ThreadUtil.sleepSeconds(1)
        assertEquals(7, counter.get())
        ThreadUtil.sleepSeconds(1)
        assertEquals(8, counter.get())
        quit.set(true)
        ThreadUtil.sleepSeconds(5)
        assertTrue(counter.get() in arrayOf(8, 9))
        ThreadUtil.sleepSeconds(5)
        assertTrue(counter.get() in arrayOf(8, 9))
    }
}