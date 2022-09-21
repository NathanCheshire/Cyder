package cyder.genesis.subroutines;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.Cyder;
import cyder.genesis.CyderSplash;
import cyder.threads.CyderThreadRunner;
import cyder.utils.IoUtil;

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
     * The sequential subroutines to execute meaning that the first routine
     * is invoked and upon completion, the second one is invoked and so forth.
     */
    private static final ImmutableList<SufficientSubroutine> sequentialSufficientSubroutines = ImmutableList.of();

    /**
     * The parallel subroutines to execute meaning that they are all started in a separate thread.
     */
    private static final ImmutableList<SufficientSubroutine> parallelSufficientSubroutines = ImmutableList.of(
            new SufficientSubroutine(() -> {
                CyderSplash.INSTANCE.setLoadingMessage("Logging JVM args");
                IoUtil.logArgs(Cyder.getJvmArguments());
            }, "Jvm Logger")
    );

    /**
     * The name for the thread which executes the sequential subroutines.
     */
    private static final String SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME = "Sufficient Subroutine Executor";

    /**
     * The name for the thread that spins off the parallel threads into their own threads.
     */
    private static final String PARALLEL_SUFFICIENT_SUBROUTINE_THREAD_NAME = "Parallel Sufficient Subroutine Executor";

    /**
     * Executes the parallel and sequential sufficient subroutines in a separate thread.
     */
    public static void execute() {
        CyderThreadRunner.submit(() -> {
            CyderThreadRunner.submit(() -> parallelSufficientSubroutines.forEach(subroutine ->
                            CyderThreadRunner.submit(subroutine.getRoutine(), subroutine.getThreadName())),
                    PARALLEL_SUFFICIENT_SUBROUTINE_THREAD_NAME);

            sequentialSufficientSubroutines.forEach(subroutine -> subroutine.getRoutine().run());
        }, SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME);
    }
}
