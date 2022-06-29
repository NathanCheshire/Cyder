package cyder.genesis.subroutines;

import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.utils.OSUtil;

/**
 * A subroutine for ensuring the proper operating system is being used to run Cyder.
 */
public class SupportedOs implements StartupSubroutine {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ensure() {
        return !OSUtil.isOSX();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubroutinePriority getPriority() {
        return SubroutinePriority.NECESSARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return "System OS not intended for Cyder use. You should install a dual boot or a VM or something :/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exit() {
        ExceptionHandler.exceptionExit(getErrorMessage(), "OS Exception", ExitCondition.CorruptedSystemFiles);
        throw new FatalException(getErrorMessage());
    }
}
