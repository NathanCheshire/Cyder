package cyder.threads

import com.google.common.base.Stopwatch
import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Tests for the [ThreadUtil]s.
 */
class ThreadUtilTest {
    /**
     * Tests for the get daemon thread count method.
     */
    @Test
    fun testGetDaemonThreadCount() {
        assertDoesNotThrow { ThreadUtil.getDaemonThreadCount() }
    }

    /**
     * Tests for the get thread count method.
     */
    @Test
    fun testGetThreadCount() {
        assertDoesNotThrow { ThreadUtil.getThreadCount() }
    }

    /**
     * Tests for the get daemon threads method.
     */
    @Test
    fun testGetDaemonThreads() {
        var list: ImmutableList<String>? = null
        assertDoesNotThrow { list = ThreadUtil.getDaemonThreadNames() }

        println("Daemon Threads---")

        for (item in list!!) {
            assertNotNull(item)
            println(item)
        }
    }

    /**
     * Tests for the get threads method.
     */
    @Test
    fun testGetThreads() {
        var list: ImmutableList<String>? = null
        assertDoesNotThrow { list = ThreadUtil.getThreadNames() }

        println("Threads---")

        for (item in list!!) {
            assertNotNull(item)
            println(item)
        }
    }

    /**
     * Tests for the sleep methods.
     */
    @Test
    fun testSleep() {
        assertThrows(IllegalArgumentException::class.java) { ThreadUtil.sleep(-1) }

        assertDoesNotThrow { ThreadUtil.sleep(1) }
        assertDoesNotThrow { ThreadUtil.sleep(10) }
        assertDoesNotThrow { ThreadUtil.sleep(100) }

        val maxAcceptableVariance = 15f
        val runs = 500
        val sleepTime = 100
        var totalSleepTime = 0L

        for (i in 0 until runs) {
            val stopwatch = Stopwatch.createStarted()
            ThreadUtil.sleep(sleepTime.toLong())
            stopwatch.stop()
            totalSleepTime += stopwatch.elapsed().toMillis()
        }

        val averageVariance = totalSleepTime / runs

        assertTrue(averageVariance < sleepTime + maxAcceptableVariance)
    }

    /**
     * Tests for the sleep seconds methods.
     */
    @Test
    fun testSleepSeconds() {
        assertThrows(IllegalArgumentException::class.java) { ThreadUtil.sleepSeconds(-1) }
        assertThrows(IllegalArgumentException::class.java) { ThreadUtil.sleepSeconds(0) }

        assertDoesNotThrow { ThreadUtil.sleepSeconds(1) }
        assertDoesNotThrow { ThreadUtil.sleepSeconds(2) }
        assertDoesNotThrow { ThreadUtil.sleepSeconds(3) }

        val maxAcceptableVarianceMillis = 15f
        val runs = 100
        val sleepTimeSeconds = 1
        var totalSleepTimeMillis = 0L

        for (i in 0 until runs) {
            val stopwatch = Stopwatch.createStarted()
            ThreadUtil.sleepSeconds(sleepTimeSeconds.toLong())
            stopwatch.stop()
            totalSleepTimeMillis += stopwatch.elapsed().toMillis()
        }

        val averageVarianceMillis = totalSleepTimeMillis / runs

        assertTrue(averageVarianceMillis < sleepTimeSeconds * 1000L + maxAcceptableVarianceMillis)
    }

    /**
     * Tests for the sleep with checks method.
     */
    @Test
    fun testSleepWithChecks() {
        assertThrows(IllegalArgumentException::class.java) {
            ThreadUtil.sleepWithChecks(0L, 0L, AtomicBoolean())
        }
        assertThrows(IllegalArgumentException::class.java) {
            ThreadUtil.sleepWithChecks(1L, 0L, AtomicBoolean())
        }
        assertThrows(IllegalArgumentException::class.java) {
            ThreadUtil.sleepWithChecks(1L, 1L, AtomicBoolean())
        }

        assertDoesNotThrow { ThreadUtil.sleepWithChecks(1000L, 1L, AtomicBoolean()) }
    }

    /**
     * Tests for the get threads method.
     */
    @Test
    fun testGetCurrentThreads() {
        assertDoesNotThrow { ThreadUtil.getCurrentThreads() }

        for (item in ThreadUtil.getCurrentThreads()) {
            assertNotNull(item)
            println(item)
        }
    }
}