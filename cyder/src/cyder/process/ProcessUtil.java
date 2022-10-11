package cyder.process;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;
import cyder.utils.OsUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities related to processes.
 */
public final class ProcessUtil {
    /**
     * Suppress default constructor.
     */
    private ProcessUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the output as a result of the running the provided command using a {@link Process}.
     *
     * @param command the command to run
     * @return the process result
     */
    @CanIgnoreReturnValue
    public static Future<ProcessResult> getProcessOutput(String command) {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!command.isEmpty());

        String threadName = "getProcessOutput thread, command = \"" + command + "\"";
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(threadName)).submit(() -> {
            ArrayList<String> standardOutput = new ArrayList<>();
            ArrayList<String> errorOutput = new ArrayList<>();

            try {
                Process process = Runtime.getRuntime().exec(command);
                process.getOutputStream().close();

                String outputLine;
                BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((outputLine = outReader.readLine()) != null) standardOutput.add(outputLine);
                outReader.close();

                String errorLine;
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((errorLine = errorReader.readLine()) != null) errorOutput.add(errorLine);
                errorReader.close();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            return new ProcessResult(standardOutput, errorOutput);
        });
    }

    /**
     * Installs the provided python pip dependency.
     *
     * @param packageName the pip dependency to install.
     */
    public static void installPipDependency(String packageName) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkArgument(!packageName.isEmpty());
        Preconditions.checkArgument(OsUtil.isBinaryInstalled("python"));
        Preconditions.checkArgument(OsUtil.isBinaryInstalled("pip"));

        getProcessOutput("pip install " + packageName);
    }
}
