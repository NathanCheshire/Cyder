package cyder.process

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for [ProcessUtil]s.
 */
class ProcessUtilTest {
    /**
     * Tests for the get process output methods.
     */
    @Test
    fun testGetProcessOutput() {
        assertThrows(IllegalArgumentException::class.java) { ProcessUtil.getProcessOutput(arrayOf<String>()) }
        assertThrows(IllegalArgumentException::class.java) { ProcessUtil.getProcessOutput(ImmutableList.of()) }
        assertThrows(IllegalArgumentException::class.java) { ProcessUtil.getProcessOutput("") }

        val futureResult = ProcessUtil.getProcessOutput("cmd /c echo hello")
        while (!futureResult.isDone) Thread.onSpinWait()
        val result = futureResult.get()

        assertFalse(result.hasErrors())
        assertEquals(ImmutableList.of("hello"), result.standardOutput)
    }

    /**
     * Tests for the run process method.
     */
    @Test
    fun testRunProcess() {
        assertThrows(NullPointerException::class.java) {
            ProcessUtil.runProcess(null)
        }

        val futureResult = ProcessUtil.runProcess(ProcessBuilder("cmd", "/c", "echo", "hello"))
        assertFalse(futureResult.isEmpty())
        assertEquals(ImmutableList.of("hello"), futureResult)
    }

    /**
     * Tests for the run processes sequential method.
     */
    @Test
    fun testRunProcesses() {
        assertThrows(NullPointerException::class.java) {
            ProcessUtil.runProcesses(null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProcessUtil.runProcesses(ImmutableList.of())
        }

        val futureResult = ProcessUtil.runProcesses(ImmutableList.of(
                ProcessBuilder("cmd", "/c", "echo", "hello"),
                ProcessBuilder("cmd", "/c", "echo", "world")))
        assertFalse(futureResult.isEmpty())
        assertEquals(ImmutableList.of("hello", "world"), futureResult)
    }

    /**
     * Tests for the run and wait for process method.
     */
    @Test
    fun testRunAndWaitForProcess() {
        assertThrows(NullPointerException::class.java) {
            ProcessUtil.runAndWaitForProcess(null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProcessUtil.runAndWaitForProcess("")
        }

        assertDoesNotThrow { ProcessUtil.runAndWaitForProcess("cmd /c echo hello world") }
    }
}