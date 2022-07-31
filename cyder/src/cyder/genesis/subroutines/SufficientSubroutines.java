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

    /**
     * Executes the sufficient subroutines in a separate thread.
     */
    public static void execute() {
        CyderThreadRunner.submit(() -> {
            CyderSplash.INSTANCE.setLoadingMessage("Logging JVM args");
            IOUtil.logArgs(Cyder.getJvmArguments());
        }, "Sufficient Subroutine Executor");
    }
}
