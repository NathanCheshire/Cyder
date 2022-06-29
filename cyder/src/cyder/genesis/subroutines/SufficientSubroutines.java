package cyder.genesis.subroutines;

import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.genesis.Cyder;
import cyder.genesis.CyderSplash;
import cyder.handlers.internal.ExceptionHandler;
import cyder.utils.IOUtil;

/**
 * A subroutine for completing startup subroutines which are not necessary for Cyder to run properly.
 */
public class SufficientSubroutines implements StartupSubroutine {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ensure() {
        try {
            CyderSplash.INSTANCE.setLoadingMessage("Logging JVM args");
            IOUtil.logArgs(Cyder.getJvmArguments());
        } catch (Exception ignored) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubroutinePriority getPriority() {
        return SubroutinePriority.SUFFICIENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return "An exception occurred while running sufficient subroutines.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exit() {
        ExceptionHandler.exceptionExit(getErrorMessage(), "OS Exception", ExitCondition.SufficientSubroutineExit);
        throw new FatalException(getErrorMessage());
    }
}
