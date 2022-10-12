package cyder.genesis.subroutines;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.Cyder;
import cyder.genesis.CyderSplash;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.process.PythonPackage;
import cyder.threads.CyderThreadRunner;
import cyder.utils.IoUtil;

import java.util.Arrays;
import java.util.concurrent.Future;

/**
 * A subroutine for completing startup subroutines which are not necessary for Cyder to run properly.
 */
public final class SufficientSubroutines {
    /**
     * Suppress default constructor.
     */
    private SufficientSubroutines() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    private static class SufficientSubroutine {
        /**
         * The routine to invoke.
         */
        private final Runnable routine;

        /**
         * The name of the thread to invoke the routine inside of if this is a parallel subroutine.
         */
        private final String threadName;

        /**
         * Suppress default constructor.
         */
        private SufficientSubroutine() {
            throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
        }

        /**
         * Constructs a new sufficient subroutine.
         *
         * @param routine    the routine to execute
         * @param threadName the name of the thread to execute the routine using
         *                   if the routine is not sequential but instead parallel
         */
        public SufficientSubroutine(Runnable routine, String threadName) {
            this.routine = Preconditions.checkNotNull(routine);
            this.threadName = Preconditions.checkNotNull(threadName);
            Preconditions.checkArgument(!threadName.isEmpty());
        }

        /**
         * Returns the routine for this sufficient subroutine to execute.
         */
        public Runnable getRoutine() {
            return routine;
        }

        /**
         * Returns the thread name for this sufficient subroutine to use if the thread
         * should be executed in parallel instead of sequentially.
         */
        public String getThreadName() {
            return threadName;
        }
    }

    /**
     * The name of the sufficient subroutine to log the JVM args.
     */
    private static final String JVM_LOGGER = "JVM Logger";

    /**
     * The name of the sufficient subroutine to ensure the needed python dependencies defined in
     * {@link cyder.process.PythonPackage} are installed.
     */
    private static final String PYTHON_PACKAGES_INSTALLED_ENSURER = "Python Packages Installed Ensurer";

    /**
     * The subroutines to execute.
     */
    private static final ImmutableList<SufficientSubroutine> parallelSufficientSubroutines = ImmutableList.of(
            new SufficientSubroutine(() -> {
                CyderSplash.INSTANCE.setLoadingMessage("Logging JVM args");
                IoUtil.logArgs(Cyder.getJvmArguments());
            }, JVM_LOGGER),
            new SufficientSubroutine(() -> Arrays.stream(PythonPackage.values()).forEach(pythonPackage -> {
                String threadName = "Python Package Installed Ensurer, package = " + pythonPackage.getPackageName();
                CyderThreadRunner.submit(() -> {
                    Future<Boolean> futureInstalled = pythonPackage.isInstalled();
                    while (!futureInstalled.isDone()) Thread.onSpinWait();
                    try {
                        boolean installed = futureInstalled.get();
                        if (installed) {
                            Logger.log(LogTag.DEBUG, "Python package "
                                    + pythonPackage.getPackageName() + " found to be installed");
                        } else {
                            Logger.log(LogTag.DEBUG, "MISSING Python package "
                                    + pythonPackage.getPackageName());
                            Console.INSTANCE.getInputHandler().println("Missing Python dependency: "
                                    + pythonPackage.getPackageName());
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }, threadName);
            }), PYTHON_PACKAGES_INSTALLED_ENSURER)
    );

    /**
     * The name for the thread which executes the sequential subroutines.
     */
    private static final String SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME = "Sufficient Subroutine Executor";

    /**
     * Executes the parallel and sequential sufficient subroutines in a separate thread.
     */
    public static void execute() {
        CyderThreadRunner.submit(() -> parallelSufficientSubroutines.forEach(sufficientSubroutine ->
                        CyderThreadRunner.submit(sufficientSubroutine.getRoutine(), sufficientSubroutine.getThreadName())),
                SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME);
    }
}
