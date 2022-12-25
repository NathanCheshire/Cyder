package cyder.snakes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.process.Program;
import cyder.process.PythonPackage;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.utils.OsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utility functions for Python.
 */
public final class PythonUtil {
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
     * The install keyword for pip installations.
     */
    private static final String INSTALL = "install";

    /**
     * The version command line argument.
     */
    private static final String VERSION_ARGUMENT = "--version";

    /**
     * The Python version command result prefix.
     */
    private static final String pythonVersionResultPrefix = "Python" + CyderStrings.space;

    /**
     * Suppress default constructor.
     */
    private PythonUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns a list of required Python packages which were not found to be installed.
     *
     * @return a list of required Python packages which were not found to be installed
     */
    public static ImmutableList<PythonPackage> getMissingRequiredPythonPackages() {
        ArrayList<PythonPackage> missingPackages = new ArrayList<>();

        Arrays.stream(PythonPackage.values()).forEach(pythonPackage -> {
            String threadName = "Python Package Installed Ensurer, package"
                    + CyderStrings.colon
                    + CyderStrings.space
                    + pythonPackage.getPackageName();

            CyderThreadRunner.submit(() -> {
                Future<Boolean> futureInstalled = pythonPackage.isInstalled();
                while (!futureInstalled.isDone()) Thread.onSpinWait();

                try {
                    boolean installed = futureInstalled.get();
                    if (!installed) missingPackages.add(pythonPackage);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, threadName);
        });

        return ImmutableList.copyOf(missingPackages);
    }

    /**
     * Returns the python version installed if present. Empty optional else.
     *
     * @return the python version installed if present
     */
    public static Optional<String> isPython3Installed() {
        Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(
                Program.PYTHON.getProgramName()
                        + CyderStrings.space
                        + VERSION_ARGUMENT);

        while (!futureResult.isDone()) Thread.onSpinWait();

        try {
            ProcessResult result = futureResult.get();
            if (result.hasErrors()) return Optional.empty();

            ImmutableList<String> output = result.getStandardOutput();
            if (output.isEmpty()) return Optional.empty();

            String line = output.get(0);
            if (!line.contains(pythonVersionResultPrefix)) return Optional.empty();

            return Optional.of(line.substring(pythonVersionResultPrefix.length()).trim());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }

    /**
     * Installs the provided python pip dependency.
     *
     * @param pythonPackage the python package to install
     */
    public static void installPipDependency(PythonPackage pythonPackage) {
        Preconditions.checkNotNull(pythonPackage);
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PYTHON.getProgramName()));
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PIP.getProgramName()));

        ProcessUtil.getProcessOutput(
                Program.PIP.getProgramName()
                        + CyderStrings.space
                        + INSTALL
                        + CyderStrings.space
                        + pythonPackage.getPackageName());
    }

    /**
     * Returns whether the provided python pip dependency is present.
     *
     * @param pythonPackage pythonPackage python package to install
     * @return whether the provided python pip dependency is present
     */
    public static Future<Boolean> isPipDependencyPresent(PythonPackage pythonPackage) {
        Preconditions.checkNotNull(pythonPackage);
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PYTHON.getProgramName()));
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PIP.getProgramName()));

        String threadName = "isPipDependencyPresent thread, packageName"
                + CyderStrings.colon
                + CyderStrings.space
                + CyderStrings.quote
                + pythonPackage.getPackageName()
                + CyderStrings.quote;

        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(
                    Program.PIP.getProgramName()
                            + CyderStrings.space
                            + SHOW
                            + CyderStrings.space
                            + pythonPackage.getPackageName());
            while (!futureResult.isDone()) Thread.onSpinWait();

            try {
                ProcessResult result = futureResult.get();
                return result.getStandardOutput().stream()
                        .anyMatch(line -> line.startsWith(namePrefix)
                                && line.substring(namePrefix.length())
                                .equalsIgnoreCase(pythonPackage.getPackageName()));
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            return false;
        });
    }

    /**
     * Returns the provided python pip dependency version if present. Empty optional else.
     *
     * @param pythonPackage the python package
     * @return the dependency version
     */
    public static Future<Optional<String>> getPipDependencyVersion(PythonPackage pythonPackage) {
        Preconditions.checkNotNull(pythonPackage);
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PYTHON.getProgramName()));
        Preconditions.checkArgument(OsUtil.isBinaryInstalled(Program.PIP.getProgramName()));

        String threadName = "getPipDependencyVersion thread, packageName"
                + CyderStrings.colon
                + CyderStrings.space
                + CyderStrings.quote
                + pythonPackage.getPackageName()
                + CyderStrings.quote;

        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(
                    Program.PIP.getProgramName()
                            + CyderStrings.space
                            + SHOW
                            + CyderStrings.space
                            + pythonPackage.getPackageName());

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
