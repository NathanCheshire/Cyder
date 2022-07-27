package cyder.genesis.subroutines;

import cyder.constants.CyderNumbers;
import cyder.enums.ExitCondition;
import cyder.enums.IgnoreThread;
import cyder.exceptions.FatalException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A startup subroutine to ensure only one instance of Cyder exists.
 */
public class SingularInstance implements StartupSubroutine {
    /**
     * The socket used to ensure only one instance of Cyder ever exists.
     */
    private Socket serverSocket;

    /**
     * Returns the server socket used to ensure only one instance of Cyder exists.
     *
     * @return the server socket used to ensure only one instance of Cyder exists
     */
    public Socket getServerSocket() {
        return serverSocket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ensure() {
        AtomicBoolean singularInstance = new AtomicBoolean(true);

        CyderThreadRunner.submit(() -> {
            try {
                Logger.log(Logger.Tag.DEBUG, "Starting instance socket on port "
                        + CyderNumbers.INSTANCE_SOCKET_PORT);
                serverSocket = new ServerSocket(CyderNumbers.INSTANCE_SOCKET_PORT).accept();
            } catch (Exception ignored) {
                singularInstance.set(false);
                Logger.log(Logger.Tag.DEBUG, "Failed to start singular instance socket");
            }
        }, IgnoreThread.SingularInstanceEnsurer.getName());

        // started blocking method in above thread but need to wait for it to either bind or fail
        ThreadUtil.sleep(CyderNumbers.SINGLE_INSTANCE_ENSURER_TIMEOUT);

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
