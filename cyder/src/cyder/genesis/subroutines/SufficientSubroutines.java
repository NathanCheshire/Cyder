package cyder.genesis.subroutines;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.Cyder;
import cyder.genesis.CyderSplash;
import cyder.threads.CyderThreadRunner;
import cyder.utils.IOUtil;

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
        private final Runnable routine;
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
         * @param routine the routine to execute
         * @param threadName the name of the thread to execute the routine using if the routine is not sequential but instead parallel
         */
        public SufficientSubroutine(Runnable routine, String, threadName) {
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
     * The sequential subroutines to execute meaning that the first routine is invoked and upon completion, the second one is invoked and so forth.
    */    
    private static final ImmutableList<SufficientSubroutine> sequentialSufficientSubroutines = ImmutableList.of(
        () -> {
            CyderSplash.INSTANCE.setLoadingMessage("Logging JVM args");
            IOUtil.logArgs(Cyder.getJvmArguments());
        }
    );

    private static final ImmutableList<SufficientSubroutine> parallelSufficientSubroutines = ImmutableList.of(

    );

    /**
     * The name for the thread which executes the sequential subroutines.
     */
    private static final String SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME = "Sufficient Subroutine Executor";

    /**
     * Executes the sufficient subroutines in a separate thread.
     */
    public static void execute() {
        CyderThreadRunner.submit(() -> {
            CyderThreadRunner.submit(() -> {
                // todo for each parallel subroutine start in sep thread
            }, "Name me");

            sequentialSufficientSubroutines.forEach(subroutine -> CyderThreadRunner.submit(subroutine.getRoutine(), subroutine.getThreadName()));
        }, SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME);
    }
}
