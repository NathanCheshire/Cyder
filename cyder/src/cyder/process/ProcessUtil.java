package cyder.process;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.utils.OsUtil;
import cyder.utils.StringUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities related to processes and the Java {@link Process} API.
 */
public final class ProcessUtil {
    /**
     * The version command line argument.
     */
    private static final String VERSION_ARGUMENT = "--version";

    /**
     * The Python version command result prefix.
     */
    private static final String pythonVersionResultPrefix = "Python" + CyderStrings.space;

    /**
     * The install keyword for pip installations.
     */
    private static final String INSTALL = "install";

    /**
     * The show keyword for getting a pip package version.
     */
    private static final String SHOW = "show";

    /**
     * The prefix of the pip show output for the package name.
     */
    private static final String namePrefix = "Name" + CyderStrings.colon + CyderStrings.space;

    /**
     * The prefix of the pip show output for the version.
     */
    private static final String versionPrefix = "Version" + CyderStrings.colon + CyderStrings.space;

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
        Preconditions.checkArgument(command.length == 0);

        String threadName = "getProcessOutput thread, command: "
                + CyderStrings.quote + StringUtil.joinParts(command, CyderStrings.space) + CyderStrings.quote;
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
     * Installs the provided python pip dependency.
     *
     * @param packageName the pip dependency to install.
     */
    public static void installPipDependency(String packageName) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkArgument(!packageName.isEmpty());
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PYTHON.getProgramName()));
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PIP.getProgramName()));

        getProcessOutput(Program.PIP.getProgramName()
                + CyderStrings.space + INSTALL + CyderStrings.space + packageName);
    }

    /**
     * Returns whether the provided python pip dependency is present.
     *
     * @param packageName the python pip dependency
     * @return whether the provided python pip dependency is present
     */
    public static Future<Boolean> isPipDependencyPresent(String packageName) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkArgument(!packageName.isEmpty());
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PYTHON.getProgramName()));
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PIP.getProgramName()));

        String threadName = "isPipDependencyPresent thread, packageName: "
                + CyderStrings.quote + packageName + CyderStrings.quote;
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
            Future<ProcessResult> futureResult = getProcessOutput(Program.PIP.getProgramName()
                    + CyderStrings.space + SHOW + CyderStrings.space + packageName);
            while (!futureResult.isDone()) Thread.onSpinWait();

            try {
                ProcessResult result = futureResult.get();
                return result.getStandardOutput().stream().anyMatch(line -> line.startsWith(namePrefix)
                        && line.substring(namePrefix.length()).equalsIgnoreCase(packageName));
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            return false;
        });
    }

    /**
     * Returns the provided python pip dependency version if present. Empty optional else.
     *
     * @param packageName the dependency name
     * @return the dependency version
     */
    public static Future<Optional<String>> getPipDependencyVersion(String packageName) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkArgument(!packageName.isEmpty());
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PYTHON.getProgramName()));
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PIP.getProgramName()));

        String threadName = "getPipDependencyVersion thread, packageName: "
                + CyderStrings.quote + packageName + CyderStrings.quote;
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
            Future<ProcessResult> futureResult = getProcessOutput(Program.PIP.getProgramName()
                    + CyderStrings.space + SHOW + CyderStrings.space + packageName);
            while (!futureResult.isDone()) Thread.onSpinWait();

            try {
                ProcessResult result = futureResult.get();
                for (String line : result.getStandardOutput()) {
                    if (line.startsWith(versionPrefix)) {
                        return Optional.of(line.substring(versionPrefix.length()));
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            return Optional.empty();
        });
    }

    /**
     * Returns the python version installed if present. Empty optional else.
     *
     * @return the python version installed if present
     */
    public static Optional<String> python3Installed() {
        Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(
                Program.PYTHON.getProgramName() + CyderStrings.space + VERSION_ARGUMENT);
        while (!futureResult.isDone()) Thread.onSpinWait();

        try {
            ProcessResult result = futureResult.get();
            if (!result.hasErrors()) {
                if (!result.getStandardOutput().isEmpty()) {
                    String line = result.getStandardOutput().get(0);
                    if (line.contains(pythonVersionResultPrefix)) {
                        String version = line.substring(pythonVersionResultPrefix.length()).trim();
                        return Optional.of(version);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
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
}
