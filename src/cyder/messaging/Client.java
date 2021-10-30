package cyder.messaging;

import cyder.handler.ErrorHandler;
import cyder.handler.SessionLogger;
import cyder.ui.CyderFrame;
import cyder.utilities.GetterUtil;
import cyder.utilities.SecurityUtil;
import cyder.utilities.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

//a client instance may exist without a connection
public class Client {
    //default TOR port
    public static final int TOR_PORT = 8118;

    //SERVER TO ESTABLISH CONNECTION AFTER HANDSHAKES

    //our server socket that receives connection requests
    private ServerSocket ourServerSocket;

    //the server we're connected to that we will send data to
    private Socket connectedServerSocket;
    private BufferedWriter connectedServerSocketWriter;
    private BufferedReader connectedServerSocketReader;

    //GENERAL DATA

    //our name, uuid, and frame
    private String clientUUID;
    private String clientName;
    private CyderFrame messagingWidgetFrame;

    //connected uuid and name
    private String connectedClientUUID;
    private String connectedClientName;

    //handshake data to listen for to make sure the IP we want to connect to is the one we connect to
    String handshakeHash = null;

    /**
     * Default constructor for client, sets up our socket and our writer
     * @param clientUUID our client's uuid
     * @param clientName our client's name
     * @param messagingWidgetFrame the CyderFrame this Client should be associated with to be used for the confirmation window
     */
    public Client(String clientUUID, String clientName, CyderFrame messagingWidgetFrame) {
        try {
            //setups
            this.clientName = clientName;
            this.clientUUID = clientUUID;
            this.messagingWidgetFrame = messagingWidgetFrame;

            //start our server to listen for connections
            startServer();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Starts listening for clients wanting to connect to us, this is ran as soon as a dm window is opened
     */
    private void startServer() {
        //blocking method so server is in it's own thread
        new Thread(() -> {
            try {
                //initialize our socket which uses our IP
                ourServerSocket = new ServerSocket(TOR_PORT);

                //we exit this while loop when Cyder exits
                while (!ourServerSocket.isClosed()) {
                    //accept a connection to check if it's who we want to connect to
                    Socket potentiallyConnectedSocket = ourServerSocket.accept(); //blocking method

                    //make a reader to receive data coming from this new connection
                    BufferedReader potentiallyConnectedSocketReader = new BufferedReader(
                            new InputStreamReader(potentiallyConnectedSocket.getInputStream()));

                    //make a writer to send data to this new connection
                    BufferedWriter potentiallyConnectedSocketWriter = new BufferedWriter(
                            new OutputStreamWriter(potentiallyConnectedSocket.getOutputStream()));

                    //get the handshake data
                    String receivedHashedHandshake = potentiallyConnectedSocketReader.readLine();

                    //get name and uuid from unknown client
                    String potentiallyConnectedClientUUID = potentiallyConnectedSocketReader.readLine();
                    String potentiallyConnectedClientName = potentiallyConnectedSocketReader.readLine();

                    //if the handshake is what we sent out, they're who we requested to connect to
                    //handhake hash being null means we're trying to be connected to,
                    // not null means we tried to connect to foreign server already and now are
                    // receiving a return that may or may not be them
                    if (handshakeHash != null && receivedHashedHandshake.equals(
                            SecurityUtil.toHexString(SecurityUtil.getSHA256(handshakeHash.toCharArray())))) {
                        //setup the reader so that we can receive more messages from this client
                        connectedServerSocketReader = potentiallyConnectedSocketReader;
                        //setup the writer so we can send messages to this client
                        connectedServerSocketWriter = potentiallyConnectedSocketWriter;

                        //set received name and uuid data to our global vars
                        connectedClientUUID = potentiallyConnectedClientUUID;
                        connectedClientName = potentiallyConnectedClientName;

                        //start listening for their messages
                        listenToClient();

                        //log connection
                        SessionLogger.log(SessionLogger.Tag.PRIVATE_MESSAGE,
                                "[PRIVATE MESSAGE]: [SECURED CONNECTION WITH " + clientName.toUpperCase()
                                        + "(" + clientUUID +  ")]");

                        //we requested, they accepted and returned, now both of us may start the chat window
                        //todo send signal to tell them to launch the chat window?
                        //todo load up chat window
                    }
                    //if the hash is not set or does not match, then it's someone new trying to connect
                    else {
                        String connectionMessage = StringUtil.capsFirst(potentiallyConnectedClientName) +
                                "(" + potentiallyConnectedClientUUID + ") would like to private message with you. Would you like to connect?";

                        boolean connect = new GetterUtil().getConfirmation(connectionMessage, messagingWidgetFrame);

                        if (connect) {
                            //setup socket and vars since we're choosing to connect to them officially
                            connectedServerSocket = potentiallyConnectedSocket;
                            connectedServerSocketReader = potentiallyConnectedSocketReader;
                            connectedServerSocketWriter = potentiallyConnectedSocketWriter;

                            String sendinghash = SecurityUtil.toHexString(SecurityUtil.getSHA256(handshakeHash.toCharArray()));

                            //send over data, now it's up to their server to connect to us as well
                            potentiallyConnectedSocketWriter.write(sendinghash);
                            potentiallyConnectedSocketWriter.write(clientUUID);
                            potentiallyConnectedSocketWriter.write(clientName);

                            //listen for their messages assumping their server still wants to connect
                            listenToClient();

                            SessionLogger.log(SessionLogger.Tag.PRIVATE_MESSAGE,
                                    "[PRIVATE MESSAGE]: [ATTEMPTING CLIENT CONNECTION WITH " + clientName.toUpperCase()
                                            + "(" + clientUUID +  ")]");
                        }
                    }
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, clientName + " Server Thread").start();
    }

    /**
     * Writes a string to our client's socket's buffered writer
     * @param message the string to write
     */
    public void sendMessage(String message) {
        try {
            //write message using their socket's writer that their listenToClient method will receive
            connectedServerSocketWriter.write(message);
            connectedServerSocketWriter.newLine();
            connectedServerSocketWriter.flush();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Called once when this client instance launches
     */
    public void listenToClient() {
        new Thread(() -> {
            String receivedMessage;

            //while connection to a client is established
            while (connectedServerSocket.isConnected()) {
                try {
                    //read from the connected client, this is a blocking method
                    receivedMessage = connectedServerSocketReader.readLine();

                    if (receivedMessage != null) {
                        //instead of printing, append to frame's outputarea
                        System.out.println("[" + connectedClientName + "]: " + receivedMessage);
                        SessionLogger.log(SessionLogger.Tag.PRIVATE_MESSAGE,
                                "[PRIVATE MESSAGE]: [ RECEIVED FROM " + clientName.toUpperCase()
                                        + "(" + clientUUID +  ")] " + receivedMessage);
                    }
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }
        },clientName + " DM Listener").start();
        //this method spins off after we've established a connection and completed handshakes
        // so it's safe to start the thread and name it using their clientName
    }

    /**
     * Attempts to connect to a client. We're given an IP to try and connect to
     * so we set a message to the IP with our handshake data, uuid, and our name
     * @param ip the provided ip to attempt to connect to (their server)
     */
    public void connect(String ip) {
        try {
            //initialize connection to server via provided ip
            Socket attemptingConnection = new Socket(ip, TOR_PORT);

            //buffered writer to send handshake and other needed data
            BufferedWriter attemptingConnectionWriter = new BufferedWriter(
                    new OutputStreamWriter(attemptingConnection.getOutputStream()));

            //we're trying to connect to them so give them some data
            this.handshakeHash = SecurityUtil.toHexString(SecurityUtil.getSHA256(SecurityUtil.getMACAddress().toCharArray()));

            //send handshake
            attemptingConnectionWriter.write(handshakeHash);
            attemptingConnectionWriter.newLine();
            //send our uuid and name
            attemptingConnectionWriter.write(this.clientUUID);
            attemptingConnectionWriter.newLine();
            attemptingConnectionWriter.write(this.clientName);
            attemptingConnectionWriter.newLine();

            //flush stream
            attemptingConnectionWriter.flush();

            //log
            SessionLogger.log(SessionLogger.Tag.PRIVATE_MESSAGE,
                    "[PRIVATE MESSAGE]: [ATTEMPTING SERVER CONNECTION WITH " + clientName.toUpperCase()
                            + "(" + clientUUID +  ")]");
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Terminates the connection that this Client has with another
     * Client and informs that client that we have disconnected
     */
    public void terminateConnection() {
        try {
            //if there's a connection
            if (connectedServerSocket != null) {
                sendMessage(clientName + " has disconnected");
                SessionLogger.log(SessionLogger.Tag.PRIVATE_MESSAGE,
                        "[DISCONNECT]: " + clientName + "(" + clientUUID +  ")" + " terminated session");
                connectedServerSocket.close();
                connectedServerSocket = null;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
