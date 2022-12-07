package cyder.snakes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.process.Program;
import cyder.threads.CyderThreadFactory;
import cyder.utils.StaticUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static cyder.constants.CyderStrings.space;

/**
 * A wrapper for the python_functions.py utility functions script.
 */
public final class PythonFunctionsWrapper {
    /**
     * The name of the python functions script.
     */
    private static final String PYTHON_FUNCTIONS_SCRIPT_NAME = "python_functions.py";

    /**
     * The absolute path to the python functions script.
     */
    private static final String functionsScriptPath =
            StaticUtil.getStaticResource(PYTHON_FUNCTIONS_SCRIPT_NAME).getAbsolutePath();

    /**
     * Suppress default constructor.
     */
    private PythonFunctionsWrapper() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Invokes the provided command with the python functions script.
     * For example, "--command audio_length --input "path/to/my/file.mp3".
     *
     * @param command the python command
     * @return the result of invoking the python command using the input file
     */
    public static Future<String> invokeCommand(String command) {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!command.isEmpty());

        String threadName = "PythonFunctions, command: " + command;
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(threadName)).submit(() -> {
            String pythonCommand = Program.PYTHON.getProgramName() + space + functionsScriptPath + space + command;
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(pythonCommand);
            while (!futureResult.isDone()) Thread.onSpinWait();

            ProcessResult result = futureResult.get();
            if (result.hasErrors()) {
                return result.getErrorOutput().get(0);
            }

            ImmutableList<String> output = result.getStandardOutput();
            return output.get(0);
        });
    }
}
