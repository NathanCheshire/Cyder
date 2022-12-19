package main.java.cyder.process;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.handlers.input.BaseInputHandler;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.strings.StringUtil;
import main.java.cyder.threads.CyderThreadFactory;
import main.java.cyder.threads.CyderThreadRunner;
import main.java.cyder.utils.ArrayUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities related to processes and the Java {@link Process} API.
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
     * @param command the command list
     * @return the process result
     */
    @CanIgnoreReturnValue
    public static Future<ProcessResult> getProcessOutput(List<String> command) {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!command.isEmpty());

        String[] passThrough = new String[command.size()];
        for (int i = 0 ; i < command.size() ; i++) {
            passThrough[i] = command.get(i);
        }

        return getProcessOutput(passThrough);
    }

    /**
     * Returns the output as a result of the running the provided command using a {@link Process}.
     *
     * @param command the command string array to run
     * @return the process result
     */
    @CanIgnoreReturnValue
    public static Future<ProcessResult> getProcessOutput(String[] command) {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!ArrayUtil.isEmpty(command));

        String threadName = "getProcessOutput waiter thread, command"
                + CyderStrings.colon + CyderStrings.space + CyderStrings.quote
                + StringUtil.joinParts(ArrayUtil.toList(command), CyderStrings.space) + CyderStrings.quote;
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
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
     * Returns the output as a result of the running the provided command using a {@link Process}.
     *
     * @param command the command to run
     * @return the process result
     */
    @CanIgnoreReturnValue
    public static Future<ProcessResult> getProcessOutput(String command) {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!command.isEmpty());

        String threadName = "getProcessOutput thread, command: "
                + CyderStrings.quote + command + CyderStrings.quote;
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
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
     * Executes the provided process and prints the output to the provided input handler in real time.
     * Note that this process is executed on the current thread so callers should invoke this method
     * in a separate thread if blocking is to be avoided.
     *
     * @param pipeTo  the input handle to print the output to
     * @param builder the process builder to run
     */
    public static void runAndPrintProcess(BaseInputHandler pipeTo, ProcessBuilder builder) {
        checkNotNull(pipeTo);
        checkNotNull(builder);

        try {
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                pipeTo.println(line);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Executes the provided processes successively and prints the output to the provided input handler.
     *
     * @param pipeTo   the input handle to print the output to
     * @param builders the process builders to run
     */
    public static void runAndPrintProcessesSequential(BaseInputHandler pipeTo,
                                                      ImmutableList<ProcessBuilder> builders) {
        checkNotNull(pipeTo);
        checkNotNull(builders);
        checkArgument(!builders.isEmpty());

        String threadName = "Successive Process Runner, pipeTo: " + pipeTo + ", builders: " + builders.size();
        CyderThreadRunner.submit(() -> builders.forEach(builder -> runAndPrintProcess(pipeTo, builder)), threadName);
    }

    /**
     * Runs the provided command using the Java process API and
     * invokes {@link Process#waitFor()} after starting the process.
     *
     * @param command the command to run
     */
    public static void runAndWaitForProcess(String command) {
        checkNotNull(command);
        checkArgument(!command.isEmpty());

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
