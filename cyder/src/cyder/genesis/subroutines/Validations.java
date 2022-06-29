package cyder.genesis.subroutines;

import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.genesis.CyderSplash;
import cyder.handlers.internal.ExceptionHandler;
import cyder.utils.OSUtil;
import cyder.utils.ReflectionUtil;
import cyder.utils.UserUtil;

/**
 * A subroutine for performing necessary validations such as ensuring files are created
 * and annotations are used properly.
 */
public class Validations implements StartupSubroutine {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ensure() {
        try {
            CyderSplash.INSTANCE.setLoadingMessage("Creating dynamics");
            OSUtil.ensureDynamicsCreated();

            CyderSplash.INSTANCE.setLoadingMessage("Validating users");
            UserUtil.validateUsers();

            CyderSplash.INSTANCE.setLoadingMessage("Cleaning users");
            UserUtil.cleanUsers();

            CyderSplash.INSTANCE.setLoadingMessage("Validating props");
            ReflectionUtil.validateProps();

            CyderSplash.INSTANCE.setLoadingMessage("Validating Widgets");
            ReflectionUtil.validateWidgets();

            CyderSplash.INSTANCE.setLoadingMessage("Validating Test");
            ReflectionUtil.validateTests();

            CyderSplash.INSTANCE.setLoadingMessage("Validating Vanilla");
            ReflectionUtil.validateVanillaWidgets();

            CyderSplash.INSTANCE.setLoadingMessage("Validating Handles");
            ReflectionUtil.validateHandles();

            return true;
        } catch (Exception e) {
            errorMessage += e.getMessage();
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubroutinePriority getPriority() {
        return SubroutinePriority.NECESSARY;
    }

    private String errorMessage = "Exception thrown from necessary subroutine runner, message = ";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exit() {
        ExceptionHandler.exceptionExit(getErrorMessage(),
                "Subroutine Exception", ExitCondition.SubroutineException);
        throw new FatalException(getErrorMessage());
    }
}
