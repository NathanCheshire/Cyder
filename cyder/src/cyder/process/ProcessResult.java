package cyder.process;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;

/**
 * The result of a {@link ProcessUtil#getProcessOutput(String) invocation}.
 */
public class ProcessResult {
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

    /**
     * Returns whether the error output contains strings.
     *
     * @return whether the error output contains strings
     */
    public boolean hasErrors() {
        return !errorOutput.isEmpty();
    }
}
