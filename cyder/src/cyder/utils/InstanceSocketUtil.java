package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.comms.CyderCommunicationMessage;
import cyder.comms.CyderRemoteShutdownMessage;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utilities related to the singular instance socket and API calls which use the instance socket.
 */
public final class InstanceSocketUtil {
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
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port), 1);
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
                        PrintWriter sendWriter = new PrintWriter(client.getOutputStream(), true);
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                        String startAndEndHash = inputReader.readLine();

                        StringBuilder inputBuilder = new StringBuilder();
                        String line;
                        while (!(line = inputReader.readLine()).equals(startAndEndHash)) {
                            inputBuilder.append(line);
                        }

                        CyderCommunicationMessage receivedMessage =
                                CyderCommunicationMessage.fromJson(inputBuilder.toString());

                        CyderRemoteShutdownMessage responseMessage =
                                new CyderRemoteShutdownMessage("Shutting down instance: instance id here");

                        // todo send a response with a confirmation that we're about to shutdown
                        //  then sending client can wait for the port to be free, that's the queue to start
                        sendWriter.println(responseMessage);

                        // todo need an instance session id api now...

                        // todo now use my class to determine course of action
                        if (receivedMessage.getContent().equals("my content")) { // todo prop hash
                            // todo remote shutdown
                            Logger.log(LogTag.DEBUG, "Shutdown requested from instance: todo instance");
                            OsUtil.exit(ExitCondition.RemoteShutdown);
                        }

                        // todo need to read messages some how defined by some schema
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                throw new FatalException(e);
            }
        }, IgnoreThread.InstanceSocket.getName());
    }

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 8888;
        Socket clientSocket = new Socket(host, port);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        StringBuilder sendBuilder = new StringBuilder();
        sendBuilder.append("{");
        sendBuilder.append(CyderStrings.quote)
                .append("message")
                .append(CyderStrings.quote)
                .append(CyderStrings.colon);
        sendBuilder.append(CyderStrings.quote)
                .append("Remote shutdown")
                .append(CyderStrings.quote)
                .append(CyderStrings.comma);
        sendBuilder.append(CyderStrings.quote)
                .append("content")
                .append(CyderStrings.quote)
                .append(CyderStrings.colon);
        sendBuilder.append(CyderStrings.quote)
                .append("my content")
                .append(CyderStrings.quote)
                .append("}");

        out.println("ending hash");
        out.println(sendBuilder);
        out.println("ending hash");
        String response = in.readLine(); // while loop
        System.out.println("response: " + response); // todo ensure it was an ack or maybe an actual json message
    }

    /**
     * The actions to invoke when a message is received from the instance socket.
     *
     * @param message the received message
     */
    private static void onInstanceSocketMessageReceived(String message) {
        Preconditions.checkNotNull(message);
        Preconditions.checkArgument(!message.isEmpty());


    }
}
