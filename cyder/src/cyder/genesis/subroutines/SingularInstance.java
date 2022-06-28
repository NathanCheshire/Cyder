package cyder.genesis.subroutines;

import cyder.constants.CyderNumbers;
import cyder.enums.ExitCondition;
import cyder.enums.IgnoreThread;
import cyder.exceptions.FatalException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;

import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A startup subroutine to ensure only one instance of Cyder exists.
 */
public class SingularInstance implements StartupSubroutine {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ensure() {
        AtomicBoolean singularInstance = new AtomicBoolean(true);

        CyderThreadRunner.submit(() -> {
            try {
                // Blocking method
                new ServerSocket(CyderNumbers.INSTANCE_SOCKET_PORT).accept();
            } catch (Exception ignored) {
                singularInstance.set(false);
            }
        }, IgnoreThread.SingularInstanceEnsurer.getName());

        try {
            // started blocking method in above thread but need to wait for it to either bind or fail
            Thread.sleep(CyderNumbers.singleInstanceEnsurerTimeout);
        } catch (InterruptedException e) {
            ExceptionHandler.handle(e);
        }

        return singularInstance.get();
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
        return "Multiple instances of Cyder are not allowed. Terminate other instances before launching a new one.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exit() {
        ExceptionHandler.exceptionExit(getErrorMessage(), "Instance Exception",
                ExitCondition.MultipleInstancesExit);
        throw new FatalException(getErrorMessage());
    }
}
