package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
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

                        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        String endHash = inputReader.readLine();

                        StringBuilder sb = new StringBuilder();
                        String line;
                        while (!(line = inputReader.readLine()).equals(endHash)) {
                            sb.append(line);
                        }


                        System.out.println("SB: " + sb);
                        MyClass myClass = SerializationUtil.fromJson(sb.toString(), MyClass.class);
                        System.out.println(myClass);

                        out.println("ACK");

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

    private static final class MyClass {
        private String message;
        private String content;

        public MyClass(String message, String content) {
            this.message = message;
            this.content = content;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket("localhost", 8888);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        out.println("ending hash");
        out.println("{");
        out.println("\"message\":\"shutdown\",");
        out.println("\"content\":\"shutdownhash\"");
        out.println("}");
        out.println("ending hash");
        String response = in.readLine(); // while loop
        System.out.println("response: " + response);
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
