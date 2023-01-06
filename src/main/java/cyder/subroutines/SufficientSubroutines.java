package cyder.subroutines;

import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.process.PythonPackage;
import cyder.snakes.PythonUtil;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;

import java.util.Optional;
import java.util.concurrent.Future;

/**
 * A subroutine for completing startup subroutines which are not necessary for Cyder to run properly.
 */
public final class SufficientSubroutines {
    /**
     * The minimum acceptable Python major version.
     */
    private static final int MIN_PYTHON_MAJOR_VERSION = 3;

    /**
     * The name of the sufficient subroutine to ensure the needed Python dependencies defined in
     * {@link cyder.process.PythonPackage} are installed.
     */
    private static final String PYTHON_PACKAGES_INSTALLED_ENSURER = "Python Packages Installed Ensurer";

    /**
     * The name of the sufficient subroutine to check for Python 3 being installed.
     */
    private static final String PYTHON_3_INSTALLED_ENSURER = "Python 3 Installed Ensurer";

    /**
     * The name for the thread which executes the sequential subroutines.
     */
    private static final String SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME = "Sufficient Subroutine Executor";

    /**
     * Suppress default constructor.
     */
    private SufficientSubroutines() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The subroutines to execute.
     */
    public static final ImmutableList<Subroutine> subroutines = ImmutableList.of(
            new Subroutine(() -> {
                Future<ImmutableList<PythonPackage>> futureMissingPackages =
                        PythonUtil.getMissingRequiredPythonPackages();

                while (!futureMissingPackages.isDone()) Thread.onSpinWait();

                try {
                    ImmutableList<PythonPackage> missingPackages = futureMissingPackages.get();

                    for (PythonPackage missingPackage : missingPackages) {
                        Logger.log(LogTag.PYTHON, "Missing required Python package: "
                                + missingPackage.getPackageName());
                    }

                    return missingPackages.isEmpty();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                return false;
            }, PYTHON_PACKAGES_INSTALLED_ENSURER),

            new Subroutine(() -> {
                Future<Optional<String>> futureOptionalVersion = PythonUtil.getPythonVersion();
                while (!futureOptionalVersion.isDone()) Thread.onSpinWait();

                Optional<String> optionalVersion = Optional.empty();
                try {
                    optionalVersion = futureOptionalVersion.get();
                } catch (Exception ignored) {}

                if (optionalVersion.isEmpty()) {
                    Logger.log(LogTag.PYTHON, "Failed to find installed Python version");
                    return false;
                }

                String versionString = optionalVersion.get();

                int version = -1;
                try {
                    version = Integer.parseInt(String.valueOf(versionString.charAt(0)));
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                if (version == -1) {
                    Logger.log(LogTag.PYTHON, "Could not find Python version number");
                    return false;
                }

                if (version >= MIN_PYTHON_MAJOR_VERSION) {
                    Logger.log(LogTag.PYTHON, "Found Python version " + version);
                    return true;
                }

                String message = "Installed Python does not meet minimum standards, version"
                        + CyderStrings.colon
                        + CyderStrings.space
                        + version
                        + CyderStrings.comma
                        + CyderStrings.space
                        + "min acceptable version"
                        + CyderStrings.colon
                        + CyderStrings.space
                        + MIN_PYTHON_MAJOR_VERSION;

                Logger.log(LogTag.PYTHON, message);
                Console.INSTANCE.getInputHandler().println(message);
                return false;
            }, PYTHON_3_INSTALLED_ENSURER)
    );

    /**
     * Executes the sufficient subroutines in a separate thread.
     */
    public static void executeSubroutines() {
        CyderThreadRunner.submit(() -> {
            for (Subroutine sufficientSubroutine : subroutines) {
                CyderThreadRunner.submitSupplier(sufficientSubroutine.getRoutine(),
                        sufficientSubroutine.getThreadName());
            }
        }, SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME);
    }
}
