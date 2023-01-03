package cyder.snakes

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Tests for the [PythonFunctionsWrapper].
 */
class PythonFunctionsWrapperTest {
    /**
     * Tests for the invoke command method.
     */
    @Test
    fun testInvokeCommand() {
        assertThrows(NullPointerException::class.java) { PythonFunctionsWrapper.invokeCommand(null) }
        assertThrows(IllegalArgumentException::class.java) { PythonFunctionsWrapper.invokeCommand("") }

        var futureResult = PythonFunctionsWrapper.invokeCommand("command")
        while (!futureResult.isDone) Thread.onSpinWait()
        assertEquals("usage: Cyder Python Utility Functions [-h] -c COMMAND -i INPUT [-r RADIUS]",
                futureResult.get())

        futureResult = PythonFunctionsWrapper.invokeCommand("--command invalid --input invalid")
        while (!futureResult.isDone) Thread.onSpinWait()
        assertEquals("Provided input file does not exist: \"invalid\"", futureResult.get())

        futureResult = PythonFunctionsWrapper.invokeCommand("--command invalid --input .gitignore")
        while (!futureResult.isDone) Thread.onSpinWait()
        assertEquals("Unsupported command: invalid", futureResult.get())
    }
}