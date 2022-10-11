package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;

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
     * The result of a {@link #getProcessOutput(String) invocation}.
     */
    public static class ProcessResult {
        /**
         * The standard output of the process.
         */
        private final ImmutableList<String> standardOutput;

        /**
         * The error output of the process.
         */
        private final ImmutableList<String> errorOutput;

        /**
         * Constructs a new process result.
         *
         * @param standardOutput the standard output
         * @param errorOutput    the error output
         */
        public ProcessResult(ArrayList<String> standardOutput, ArrayList<String> errorOutput) {
            this.standardOutput = ImmutableList.copyOf(standardOutput);
            this.errorOutput = ImmutableList.copyOf(errorOutput);
        }

        /**
         * Returns the standard output of the process.
         *
         * @return the standard output of the process
         */
        public ImmutableList<String> getStandardOutput() {
            return standardOutput;
        }

        /**
         * Returns the error output of the process.
         *
         * @return the error output of the process
         */
        public ImmutableList<String> getErrorOutput() {
            return errorOutput;
        }
    }

    /**
     * Returns the output as a result of the running the provided command using a {@link Process}.
     *
     * @param command the command to run
     * @return the process result
     */
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
}
