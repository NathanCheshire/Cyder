package cyder.session;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utilities related to the singular instance socket and API calls which use the instance socket.
 */
public final class InstanceSocketUtil {
    /**
     * The key for obtaining whether localhost shutdown requests
     * should be completed without a valid password from the props.
     */
    private static final String AUTO_COMPLY_TO_LOCALHOST_SHUTDOWN_REQUESTS =
            "auto_comply_to_localhost_shutdown_requests";

    /**
     * The key for obtaining the localhost shutdown request password from the props.
     */
    private static final String LOCALHOST_SHUTDOWN_REQUEST_PASSWORD = "localhost_shutdown_request_password";

    /**
     * The local host string.
     */
    private static final String LOCALHOST = "localhost";

    /**
     * The range a port must fall into.
     */
    private static final Range<Integer> portRange = Range.closed(1024, 65535);

    /**
     * The default instance socket port.
     */
    private static final int DEFAULT_INSTANCE_SOCKET_PORT = 8888;

    /**
     * The key to get the instance socket port from the props.
     */
    private static final String INSTANCE_SOCKET_PORT = "instance_socket_port";

    /**
     * The port to start the instance socket on.
     */
    private static final int instanceSocketPort;

    static {
        boolean propPresent = PropLoader.propExists(INSTANCE_SOCKET_PORT);

        if (propPresent) {
            int requestedPort = PropLoader.getInteger(INSTANCE_SOCKET_PORT);

            if (localPortAvailable(requestedPort)) {
                instanceSocketPort = requestedPort;
            } else {
                instanceSocketPort = DEFAULT_INSTANCE_SOCKET_PORT;
            }
        } else {
            instanceSocketPort = DEFAULT_INSTANCE_SOCKET_PORT;
        }
    }

    /**
     * Suppress default constructor.
     */
    private InstanceSocketUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the instance socket port this instance is using.
     *
     * @return the instance socket port this instance is using
     */
    public static int getInstanceSocketPort() {
        return instanceSocketPort;
    }

    // todo network util

    /**
     * Returns whether the local port is available for binding.
     *
     * @param port the local port
     * @return whether the local port is available for binding
     */
    public static boolean localPortAvailable(int port) {
        Preconditions.checkArgument(portRange.contains(port));

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(LOCALHOST), port), 1);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Returns whether the instance socket port is currently available.
     *
     * @return whether the instance socket port is currently available
     */
    public static boolean instanceSocketPortAvailable() {
        return localPortAvailable(instanceSocketPort);
    }

    /**
     * Whether the instance socket bind was attempted.
     */
    private static final AtomicBoolean instanceSocketBindAttempted = new AtomicBoolean(false);

    /**
     * The instance socket.
     */
    private static ServerSocket instanceSocket;

    /**
     * Binds the instance socket to the instance socket port and starts listening for a connection.
     *
     * @throws cyder.exceptions.FatalException if an exception occurs
     */
    public static void startListening() {
        Preconditions.checkState(instanceSocketPortAvailable());
        Preconditions.checkState(!instanceSocketBindAttempted.get());

        instanceSocketBindAttempted.set(true);

        CyderThreadRunner.submit(() -> {
            try {
                instanceSocket = new ServerSocket(instanceSocketPort, 1);

                while (true) {
                    try {
                        Socket client = instanceSocket.accept();

                        PrintWriter responseWriter = new PrintWriter(client.getOutputStream(), true);
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                        CyderCommunicationMessage receivedMessage = readInputMessage(inputReader);

                        onInstanceSocketMessageReceived(receivedMessage, responseWriter);
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }
            } catch (Exception e) {
                throw new FatalException(e);
            }
        }, IgnoreThread.InstanceSocket.getName());
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

        throw new FatalException("Failed to read communication message from: " + inputReader);
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

        // todo will be instanceof ideally if we can figure it out
        if (messageType.equals(CyderRemoteShutdownMessage.MESSAGE)) {
            onInstanceSocketCyderRemoteShutdownMessageReceived(message, responseWriter);
        } else {
            throw new FatalException("Unknown CyderCommunicationMessage: " + messageType);
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

        boolean shouldComply = true;
        if (PropLoader.propExists(AUTO_COMPLY_TO_LOCALHOST_SHUTDOWN_REQUESTS)) {
            if (!PropLoader.getBoolean(AUTO_COMPLY_TO_LOCALHOST_SHUTDOWN_REQUESTS)) {
                boolean passwordExists = PropLoader.propExists(LOCALHOST_SHUTDOWN_REQUEST_PASSWORD);

                if (passwordExists) {
                    String remoteShutdownPassword = PropLoader.getString(LOCALHOST_SHUTDOWN_REQUEST_PASSWORD);
                    String hashedShutdownRequestPassword = SecurityUtil.toHexString(
                            SecurityUtil.getSha256(remoteShutdownPassword.toCharArray()));
                    String receivedHash = message.getContent();

                    shouldComply = receivedHash.equals(hashedShutdownRequestPassword);
                }
            }
        }

        if (shouldComply) {
            try {
                Logger.log(LogTag.DEBUG, "Shutdown requested from instance: " + message.getSessionId());
                instanceSocket.close();
                sendCommunicationMessage("Remote shutdown response", "Shutting down", responseWriter);
                responseWriter.close();
            } catch (Exception ignored) {} finally {
                OsUtil.exit(ExitCondition.RemoteShutdown);
            }
        } else {
            Logger.log(LogTag.DEBUG, "Shutdown request from instance "
                    + message.getSessionId() + " failed to provide the appropriate password");
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
                message, content, SessionManager.getSessionId());

        System.out.println("Sending: " + responseShutdownMessage);

        String sendHash = SecurityUtil.generateUuid();
        responseWriter.println(sendHash);
        responseWriter.println(responseShutdownMessage);
        responseWriter.println(sendHash);
    }

    public static void main(String[] args) throws Exception {
        int port = 8888;
        Future<CyderCommunicationMessage> futureResponse =
                sendRemoteShutdownRequest(LOCALHOST, port, "doesn't matter");
        while (!futureResponse.isDone()) Thread.onSpinWait();
        CyderCommunicationMessage response = futureResponse.get();
        System.out.println(response);

        while (!localPortAvailable(port)) Thread.onSpinWait();
        System.out.println("Continue Session normally");

        // todo now wait for instance socket to be free, have max allowable time to wait of course
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
    public static Future<CyderCommunicationMessage> sendRemoteShutdownRequest(String host,
                                                                              int port,
                                                                              String shutdownPassword) {
        Preconditions.checkNotNull(host);
        Preconditions.checkArgument(!host.isEmpty());
        Preconditions.checkArgument(portRange.contains(port));
        Preconditions.checkNotNull(shutdownPassword);
        Preconditions.checkArgument(!shutdownPassword.isEmpty());

        String executorName = "Remote Shutdown Request, host: " + host + ", port: " + port;
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(executorName)).submit(() -> {
            try {
                Socket clientSocket = new Socket(host, port);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String hash = SecurityUtil.toHexString(SecurityUtil.getSha256(shutdownPassword.toCharArray()));
                sendCommunicationMessage(CyderRemoteShutdownMessage.MESSAGE, hash, out);

                return readInputMessage(in);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            throw new FatalException("Failed to perform remote shutdown request");
        });
    }
}
