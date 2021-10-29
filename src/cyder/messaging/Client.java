package cyder.messaging;

import cyder.handler.ErrorHandler;
import cyder.utilities.IPUtil;
import cyder.utilities.SecurityUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

//a client instance may exist without a connection
public class Client {
    public static final int TOR_PORT = 8118;

    //our server socket that receives messages
    private ServerSocket ourServerSocket;

    //our client socket that we use to send messages
    private Socket ourClientSocket;

    //io for client
    private BufferedReader ourClientReader;
    private BufferedWriter ourClientWriter;

    //our name and uuid
    private String clientUUID;
    private String clientName;

    //the server we're connected that will receive our messages
    private Socket connectedServerSocket;

    //connected uuid and name
    private String connectedClientUUID;
    private String connectedClientName;

    //handshake data to listen for to make sure the IP we want to connect to is the one we connect to
    String handshakeHash = null;

    public Client(String clientUUID, String clientName) {
        try {
            //setups
            this.clientName = clientName;
            this.clientUUID = clientUUID;

            //init our socket, and IO objects since now we are a client ready to connect to a server but also
            // ready to listen for a connection attempt
            ourClientSocket = new Socket(IPUtil.getIpdata().getIp(), TOR_PORT);
            this.ourClientWriter = new BufferedWriter(new OutputStreamWriter(ourClientSocket.getOutputStream()));
            this.ourClientReader = new BufferedReader(new InputStreamReader(ourClientSocket.getInputStream()));

            //start our server to listen for connections
            startServer();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //used to accept a socket to connect to (connect to a client)
    private void startServer() {
        try {
            //initialize our socket which uses our IP
            ourServerSocket = new ServerSocket(TOR_PORT);

            //we exit this while loop when Cyder exits
            while (!ourServerSocket.isClosed()) {
                //accept a connection to check if it's who we want to connect to
                Socket potentiallyConnectedSocket = ourServerSocket.accept();

                //make a reader to receive data coming from this new connection
                BufferedReader potentiallyConnectedSocketReader = new BufferedReader(
                        new InputStreamReader(potentiallyConnectedSocket.getInputStream()));

                //get the handshake data
                String receivedHashedHandshake = potentiallyConnectedSocketReader.readLine();

                //if the handshake is what we sent out, they're who we requested to connect to
                //handhake hash being null means we're trying to be connected to,
                // not null means we tried to connect to foreign server already and now are
                // receiving a return that may or may not be them
                if (handshakeHash != null && receivedHashedHandshake.equals(
                        SecurityUtil.toHexString(SecurityUtil.getSHA256(handshakeHash.toCharArray())))) {
                    //get name and uuid from
                    connectedClientUUID = potentiallyConnectedSocketReader.readLine();
                    connectedClientName = potentiallyConnectedSocketReader.readLine();
                } else {
                    //they're some random other person trying to connect to us, should we allow it?
                    // who are they too?
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Writes a string to our client's socket's buffered writer
     * @param message the string to write
     */
    public void sendMessage(String message) {
        try {
            //write message using our socket's writer that their server will pickup
            ourClientWriter.write(message);
            ourClientWriter.newLine();
            ourClientWriter.flush();
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

            while (ourClientSocket.isConnected()) {
                try {
                    //wrong reader here?
                    receivedMessage = ourClientReader.readLine();
                    System.out.println("[" + connectedClientName + "]: " + receivedMessage);
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }
        },clientName + " DM Listener").start();
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
            ourClientWriter.write(handshakeHash);
            ourClientWriter.newLine();
            //send our uuid and name
            ourClientWriter.write(this.clientUUID);
            ourClientWriter.newLine();
            ourClientWriter.write(this.clientName);
            ourClientWriter.newLine();

            //flush stream
            ourClientWriter.flush();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //ends session with currently connected client
    public void terminateConnection() {
        try {
            //if there's a connection
            if (connectedServerSocket != null) {
                sendMessage(clientName + " has disconnected");
                connectedServerSocket.close();
                connectedServerSocket = null;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
