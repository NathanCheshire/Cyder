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
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities related to processes.
 */
public final class ProcessUtil {

    /**
     * A space character.
     */
    private static final String SPACE = " ";

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
    private static final String namePrefix = "Name: ";

    /**
     * The prefix of the pip show output for the version.
     */
    private static final String versionPrefix = "Version: ";

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
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PYTHON.getProgramName()));
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PIP.getProgramName()));

        getProcessOutput(Program.PIP.getProgramName() + SPACE + INSTALL + SPACE + packageName);
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

        String threadName = "isPipDependencyPresent thread, packageName = \"" + packageName + "\"";
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(threadName)).submit(() -> {
            Future<ProcessResult> futureResult = getProcessOutput(Program.PIP.getProgramName()
                    + SPACE + SHOW + SPACE + packageName);
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

        String threadName = "getPipDependencyVersion thread, packageName = \"" + packageName + "\"";
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(threadName)).submit(() -> {
            Future<ProcessResult> futureResult = getProcessOutput(Program.PIP.getProgramName()
                    + SPACE + SHOW + SPACE + packageName);
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
}
