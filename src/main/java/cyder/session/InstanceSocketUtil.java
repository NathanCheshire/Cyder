package cyder.session;

import com.google.common.base.Preconditions;
import cyder.enumerations.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.meta.CyderSplash;
import cyder.network.NetworkUtil;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utilities related to the singular instance socket and API calls which use the instance socket.
 */
public final class InstanceSocketUtil {
    /**
     * The port to start the instance socket on.
     */
    private static final int instanceSocketPort = Props.instanceSocketPort.getValue();

    /**
     * The number of clients which can be waiting for the instance socket to free up and connect.
     */
    private static final int instanceSocketBacklog = 1;

    /**
     * Whether the instance socket bind was attempted.
     */
    private static final AtomicBoolean instanceSocketBindAttempted = new AtomicBoolean(false);

    /**
     * The maximum amount of time to wait for the instance socket to free up after a remote shutdown request succeeds.
     */
    private static final Duration maximumWaitForInstancePortToFree = Duration.ofSeconds(5);

    /**
     * The timeout between instance socket polls for incoming messages.
     */
    private static final Duration instanceSocketPollTimeout = Duration.ofMillis(100);

    /**
     * The instance socket.
     */
    private static ServerSocket instanceSocket;

    /**
     * Suppress default constructor.
     */
    private InstanceSocketUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Attempts to bind to the set instance socket. If unavailable, and localhost remote shutdown requests
     * are enabled, a communication message is sent through the socket in an attempt to shutdown another
     * instance of Cyder already bound to the instance port.
     */
    public static void bindToInstanceSocket() {
        CyderSplash.INSTANCE.setLoadingMessage("Ensuring singular instance");
        Logger.log(LogTag.NETWORK, "Attempting to bind to instance socket port: "
                + Props.instanceSocketPort.getValue());
        SocketBindAttemptResult result = attemptToBindToInstanceSocked();
        Logger.log(LogTag.NETWORK, "Instance socket bind attempt result: " + result.getMessage());
        if (result.isSuccessful()) {
            InstanceSocketUtil.startListening();
        } else {
            throw new FatalException("Failed to bind to socket instance; " + result.getMessage());
        }
    }

    /**
     * Attempts to bind to the set instance socket. If unavailable, and localhost remote shutdown requests
     * are enabled, a communication message is sent through the socket in an attempt to shutdown another
     * instance of Cyder already bound to the instance port.
     *
     * @return the result of attempting to bind to the instance socket
     */
    private static SocketBindAttemptResult attemptToBindToInstanceSocked() {
        if (InstanceSocketUtil.instanceSocketPortAvailable()) return SocketBindAttemptResult.PORT_AVAILABLE;
        boolean shutdownRequestsEnabled = Props.localhostShutdownRequestsEnabled.getValue();
        if (!shutdownRequestsEnabled) return SocketBindAttemptResult.REMOTE_SHUTDOWN_REQUESTS_DISABLED;
        int port = Props.instanceSocketPort.getValue();
        if (!NetworkUtil.portRange.contains(port)) return SocketBindAttemptResult.INVALID_PORT;
        String password = Props.localhostShutdownRequestPassword.getValue();
        if (StringUtil.isNullOrEmpty(password)) return SocketBindAttemptResult.PASSWORD_NOT_SET;

        try {
            Future<CyderCommunicationMessage> futureMessage =
                    InstanceSocketUtil.sendRemoteShutdownRequest(NetworkUtil.LOCALHOST, port, password);
            while (!futureMessage.isDone()) Thread.onSpinWait();
            CyderCommunicationMessage message = futureMessage.get();
            String content = message.getContent();
            RemoteShutdownRequestResult result =
                    RemoteShutdownRequestResult.fromMessage(content);
            if (!result.isShouldComply()) return SocketBindAttemptResult.REMOTE_SHUTDOWN_REQUEST_DENIED;
            long startedWaitingTime = System.currentTimeMillis();
            while (!NetworkUtil.localPortAvailable(port)) {
                if (System.currentTimeMillis() - startedWaitingTime >= maximumWaitForInstancePortToFree.toMillis()) {
                    return SocketBindAttemptResult.TIMED_OUT_AFTER_SUCCESSFUL_REMOTE_SHUTDOWN;
                }
                Thread.onSpinWait();
            }
            return SocketBindAttemptResult.SUCCESS_AFTER_REMOTE_SHUTDOWN;
        } catch (InterruptedException | ExecutionException e) {
            ExceptionHandler.handle(e);
            return SocketBindAttemptResult.FAILURE_WHILE_ATTEMPTING_REMOTE_SHUTDOWN;
        } catch (FatalException e) {
            ExceptionHandler.handle(e);
            return SocketBindAttemptResult.PORT_UNAVAILABLE;
        }
    }

    /**
     * Returns the instance socket port this instance is using.
     *
     * @return the instance socket port this instance is using
     */
    public static int getInstanceSocketPort() {
        return instanceSocketPort;
    }

    /**
     * Returns whether the instance socket port is currently available.
     *
     * @return whether the instance socket port is currently available
     */
    public static boolean instanceSocketPortAvailable() {
        return NetworkUtil.localPortAvailable(instanceSocketPort);
    }

    /**
     * Binds the instance socket to the instance socket port and starts listening for a connection.
     *
     * @throws cyder.exceptions.FatalException if an exception occurs
     */
    public static void startListening() {
        Preconditions.checkState(!instanceSocketBindAttempted.get());

        instanceSocketBindAttempted.set(true);

        CyderThreadRunner.submit(() -> {
            try {
                instanceSocket = new ServerSocket(instanceSocketPort, instanceSocketBacklog);

                while (true) {
                    try {
                        Socket client = instanceSocket.accept();

                        PrintWriter responseWriter = new PrintWriter(client.getOutputStream(), true);
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                        CyderCommunicationMessage receivedMessage = readInputMessage(inputReader);

                        onInstanceSocketMessageReceived(receivedMessage, responseWriter);
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    } finally {
                        ThreadUtil.sleep(instanceSocketPollTimeout.toMillis());
                    }
                }
            } catch (Exception e) {
                throw new FatalException(e);
            }
        }, IgnoreThread.InstanceSocket.getName());
    }

    /**
     * Sends a remote shutdown request to the Cyder instance using the provided host and port.
     *
     * @param host             the host of the remote Cyder instance
     * @param port             the port of the remote Cyder instance
     * @param shutdownPassword the password to prove this instance has the
     *                         authority to request the remote instance to perform a shutdown
     * @return the response message from the remote instance
     */
    public static Future<CyderCommunicationMessage> sendRemoteShutdownRequest(String host, int port,
                                                                              String shutdownPassword) {
        Preconditions.checkNotNull(host);
        Preconditions.checkArgument(!host.isEmpty());
        Preconditions.checkArgument(NetworkUtil.portRange.contains(port));
        Preconditions.checkNotNull(shutdownPassword);
        Preconditions.checkArgument(!shutdownPassword.isEmpty());

        String executorName = "Remote Shutdown Request, host: " + host + ", port: " + port;
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(executorName)).submit(() -> {
            try {
                Socket clientSocket = new Socket(host, port);
                PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                sendCommunicationMessage(CyderRemoteShutdownMessage.MESSAGE,
                        SecurityUtil.hashAndHex(shutdownPassword), outputWriter);

                return readInputMessage(inputReader);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            throw new FatalException("Failed to send remote shutdown request");
        });
    }

    /**
     * Reads a Cyder communication message from the provided buffered reader.
     *
     * @param inputReader the buffered reader
     * @return the communication message
     */
    private static CyderCommunicationMessage readInputMessage(BufferedReader inputReader) {
        Preconditions.checkNotNull(inputReader);

        try {
            String startAndEndHash = inputReader.readLine();
            StringBuilder inputBuilder = new StringBuilder();

            String line;
            while ((line = inputReader.readLine()) != null && !line.equals(startAndEndHash)) {
                inputBuilder.append(line);
            }

            return CyderCommunicationMessage.fromJson(inputBuilder.toString());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new FatalException("Failed to read communication message from reader: " + inputReader);
    }

    /**
     * The actions to invoke when a message is received in the instance socket.
     *
     * @param message        the message received
     * @param responseWriter the writer to use to send a response message
     */
    private static void onInstanceSocketMessageReceived(CyderCommunicationMessage message,
                                                        PrintWriter responseWriter) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(responseWriter);

        String messageType = message.getMessage();

        if (messageType.equals(CyderRemoteShutdownMessage.MESSAGE)) {
            onInstanceSocketCyderRemoteShutdownMessageReceived(message, responseWriter);
        } else {
            throw new FatalException("Unknown CyderCommunicationMessage: " + messageType);
        }
    }

    /**
     * Returns whether the shutdown request should be denied or complied to and the reasoning for the decision.
     *
     * @param receivedHash the hash received from the shutdown request message
     * @return whether the shutdown request should be denied or complied to and the reasoning
     */
    private static RemoteShutdownRequestResult determineRemoteShutdownRequestResult(String receivedHash) {
        Preconditions.checkNotNull(receivedHash);
        Preconditions.checkArgument(!receivedHash.isEmpty());

        if (Props.autoComplyToLocalhostShutdownRequests.getValue()) {
            return RemoteShutdownRequestResult.AUTO_COMPLIANCE_ENABLED;
        } else {
            boolean passwordExists = Props.localhostShutdownRequestPassword.valuePresent();

            if (passwordExists) {
                String remoteShutdownPassword = Props.localhostShutdownRequestPassword.getValue();
                String hashedShutdownRequestPassword = SecurityUtil.toHexString(
                        SecurityUtil.getSha256(remoteShutdownPassword.toCharArray()));

                return receivedHash.equals(hashedShutdownRequestPassword)
                        ? RemoteShutdownRequestResult.PASSWORD_CORRECT
                        : RemoteShutdownRequestResult.PASSWORD_INCORRECT;
            } else {
                return RemoteShutdownRequestResult.PASSWORD_NOT_FOUND;
            }
        }
    }

    /**
     * The actions to invoke when a Cyder remote shutdown message is received.
     *
     * @param message        the message received
     * @param responseWriter the print writer to send a response to the sending client
     */
    private static void onInstanceSocketCyderRemoteShutdownMessageReceived(CyderCommunicationMessage message,
                                                                           PrintWriter responseWriter) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(responseWriter);

        Logger.log(LogTag.DEBUG, "Shutdown requested from instance: " + message.getSessionId());
        RemoteShutdownRequestResult result = determineRemoteShutdownRequestResult(message.getContent());
        Logger.log(LogTag.DEBUG, result.getMessage());

        sendCommunicationMessage("Remote shutdown response", result.getMessage(), responseWriter);

        if (result.isShouldComply()) {
            try {
                instanceSocket.close();
                responseWriter.close();
            } catch (Exception ignored) {} finally {
                OsUtil.exit(ExitCondition.RemoteShutdown);
            }
        }
    }

    /**
     * Sends a Cyder communication message using the provided print writer.
     *
     * @param message        the response message string for the {@link CyderCommunicationMessage}s content field.
     * @param responseWriter the print writer to use to send the response
     */
    private static void sendCommunicationMessage(String message, String content, PrintWriter responseWriter) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(content);
        Preconditions.checkNotNull(responseWriter);
        Preconditions.checkArgument(!message.isEmpty());
        Preconditions.checkArgument(!content.isEmpty());

        CyderCommunicationMessage responseShutdownMessage = new CyderCommunicationMessage(
                message, content, SessionManager.INSTANCE.getSessionId());

        String sendHash = SecurityUtil.generateUuid();

        responseWriter.println(sendHash);
        responseWriter.println(responseShutdownMessage);
        responseWriter.println(sendHash);
    }
}
