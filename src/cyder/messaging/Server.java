package cyder.messaging;

import cyder.handler.ErrorHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    //common TOR port
    public static final int TOR_PORT = 8118;

    //this server that will run locally that handles connecting to other Cyder users
    private static ServerSocket serverSocket;

    //private constructor for one time setup on compilation
    private Server() {
        try {
            serverSocket = new ServerSocket(TOR_PORT);
            runLocalServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //the one and only instance of the server
    private static Server server = new Server();

    //getter for the only server instance allows
    public static Server getServer() {
        return server;
    }

    //the list of all user's we are connected to
    private static LinkedList<Socket> sockets = new LinkedList<>();

    //startup the local server
    public static void runLocalServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                sockets.add(socket);
                System.out.println("DM established with: " + socket.getLocalAddress() + ":" + socket.getLocalPort());

                //thread here for client handler?
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //end the local server
    public static void terminateLocalServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
