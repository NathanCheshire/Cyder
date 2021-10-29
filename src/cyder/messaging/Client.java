package cyder.messaging;

import cyder.handler.ErrorHandler;
import cyder.utilities.IPUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

//a client instance may exist without a connection
public class Client {
    public static final int TOR_PORT = 8118;

    //server socket to receive a single socket to connect to
    // this should never close unless Cyder exits
    private ServerSocket serverSocket;

    //our socket
    private Socket ourSocket;

    //io for client
    private BufferedReader br;
    private BufferedWriter bw;

    //our name and uuid
    private String clientUUID;
    private String clientName;

    //connected to socket via the server socket
    private Socket connectedClient;

    //connected uuid and name
    private String connectedClientUUID;
    private String connectedClientName;

    //sets up readers and writers for client IO, starts up server socket to attempt to get a connection
    public Client(String clientUUID, String clientName) {
        try {
            //setups
            this.clientName = clientName;
            this.clientUUID = clientUUID;

            //init our socket, and IO objects since now we are a client trying to connect to a server
            ourSocket = new Socket(IPUtil.getIpdata().getIp(), TOR_PORT);
            this.bw = new BufferedWriter(new OutputStreamWriter(ourSocket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(ourSocket.getInputStream()));

            //start listning for connection requests
            startServer();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //used to accept a socket to connect to (connect to a client)
    private void startServer() {
        try {
            serverSocket = new ServerSocket(TOR_PORT);

            //exiting this while loop means that Cyder has exited so we don't need
            // to worry about that conditions
            while (!serverSocket.isClosed()) {
                //wrong condition here, we should always accept but then set it as connected client if it
                // completed the handshake for us
                if (connectedClient == null) {
                    //accept the connection
                    connectedClient = serverSocket.accept();
                    //todo how do you even connect in the first place to server? does this work?

                    //get name and uuid
                    connectedClientUUID = br.readLine();
                    connectedClientName = br.readLine();

                    //start listening for messages
                    listenToClient();

                    //todo there's some massive error here think about logic and going to and from server
                    // might need two socket instances?

                    //also the handshake should be sending some random sha256 hash over and they need to hash it and send it back
                    // if a client has that hash hashed, then we can accept it here so we know it's who we connected to and now they're trying to connect to us
                    // this is where the process stops
                    // we're given an ip, we connect to their server, and then they connect to our server right here

                    //now they're connected to us, we need to connect to them, this method
                    // wont' run if we're already connected to them so no that condition can be ignored
                    connect(String.valueOf(connectedClient.getLocalAddress()));
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //writes a message to the writer and flushes it, the other client will pick it up
    public void sendMessage(String message) {
        try {
            bw.write(message);
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //maybe sent a jtextpane here to append to?
    public void listenToClient() {
        new Thread(() -> {
            String receivedMessage;

            while (ourSocket.isConnected()) {
                try {
                    receivedMessage = br.readLine();
                    System.out.println("[" + connectedClientName + "]: " + receivedMessage);
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }
        },clientName + " DM Listener").start();
    }

    //connect to a client (connect to it's server)
    public void connect(String ip) {
        try {
            //initialize connection to server via provided ip
            connectedClient = new Socket(ip, TOR_PORT);

            //send handshake data
            sendMessage(clientUUID);
            sendMessage(clientName);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //ends session with currently connected client
    public void terminateConnection() {
        try {
            //if there's a connection
            if (connectedClient != null) {
                sendMessage(clientName + " has disconnected");
                connectedClient.close();
                connectedClient = null;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public Socket getOurSocket() {
        return ourSocket;
    }

    public BufferedReader getBr() {
        return br;
    }

    public BufferedWriter getBw() {
        return bw;
    }

    public String getClientUUID() {
        return clientUUID;
    }

    public String getClientName() {
        return clientName;
    }
}
