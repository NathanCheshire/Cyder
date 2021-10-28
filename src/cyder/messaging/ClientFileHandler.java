package cyder.messaging;

import cyder.handler.ErrorHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientFileHandler {
    static ServerSocket serverSocket;

    public static void listenForFile() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(Server.TOR_PORT);
                while (!serverSocket.isClosed()) {
                    System.out.println("waiting... listening on port: " + Server.TOR_PORT);
                    Socket socket = serverSocket.accept();
                    System.out.println(socket.getLocalAddress() + ":" + socket.getLocalPort());
                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    int fileNameLength = dis.readInt();

                    if (fileNameLength > 0) {
                        byte[] filenameBytes = new byte[fileNameLength];
                        dis.readFully(filenameBytes, 0, fileNameLength);
                        String filename = new String(filenameBytes);

                        int contentLength = dis.readInt();

                        if (contentLength > 0) {
                            byte[] fileBytes = new byte[contentLength];
                            dis.readFully(fileBytes, 0, contentLength);

                            File downloadedContent =
                                    new File("static/sandbox/" + filename);
                            FileOutputStream fis = new FileOutputStream(downloadedContent);
                            fis.write(fileBytes);
                            fis.close();
                            System.out.println("File " + filename + " received, saved to: " + downloadedContent.getPath());
                        }
                    }
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Clist File Receiver Thread").start();
    }

    public static void sendFile(File sendFile, String host) {
        try {
            FileInputStream fis = new FileInputStream(sendFile.getAbsolutePath());
            Socket socket = new Socket(host, Server.TOR_PORT);
            DataOutputStream dis = new DataOutputStream(socket.getOutputStream());

            String filename = sendFile.getName();
            byte[] fileNameBytes = filename.getBytes();

            byte[] fileContentBytes = new byte[(int) sendFile.length()];
            fis.read(fileContentBytes);

            dis.writeInt(fileNameBytes.length);
            dis.write(fileNameBytes);

            dis.writeInt(fileContentBytes.length);
            dis.write(fileContentBytes);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
