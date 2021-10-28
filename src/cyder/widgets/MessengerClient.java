package cyder.widgets;

import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MessengerClient {

    private void listenForFile(int listeningPort) {
        try {
            ServerSocket serverSocket = new ServerSocket(listeningPort);

            while (true) {
                System.out.println("waiting... listening on port: " + listeningPort);
                Socket socket = serverSocket.accept(); //blocking method
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
                                new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/" + filename);
                        FileOutputStream fis = new FileOutputStream(downloadedContent);
                        fis.write(fileBytes);
                        fis.close();
                        System.out.println("File " + filename + " received");
                    }
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
