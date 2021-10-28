package cyder.messaging;

import cyder.handler.ErrorHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

//a client instance may exist without a connection
public class Client {
    public static final int TOR_PORT = 8118;

    //client socket
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private String clientUUID;
    private String clientName;

    private Client connectedClient;

    //out socket, our uuid, and our name
    public Client(Socket socket, String clientUUID, String clientName) {
        try {
            this.socket = socket;
            this.clientName = clientName;
            this.clientUUID = clientUUID;

            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void connect(Client connectedClient) {
        this.connectedClient = connectedClient;

    }

    public void sendMessage(String message) {
        try {
            bw.write(message);
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void listenToClient() {
        new Thread(() -> {
            String receivedMessage;

            while (socket.isConnected()) {
                try {
                    receivedMessage = br.readLine();
                    System.out.println("someone said: " + receivedMessage);
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }
        },connectedClient.getConnectedClient().getClientName() + "DM Listener").start();
    }

    //getters

    public Socket getSocket() {
        return socket;
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

    public Client getConnectedClient() {
        return connectedClient;
    }
}
