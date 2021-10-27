package cyder.widgets;

import cyder.handler.ErrorHandler;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CyderMessager {

    public static class Server {
        public static void main(String[] args) throws IOException {
            int port = 31415;
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                try {
                    System.out.println("waiting... listening on port: " + port);
                    Socket socket = serverSocket.accept();
                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    int fileNameLength = dis.readInt();

                    if (fileNameLength > 0) {
                        byte[] filenameBytes = new byte[fileNameLength];
                        dis.readFully(filenameBytes, 0 , fileNameLength);
                        String filename = new String(filenameBytes);

                        int contentLength = dis.readInt();

                        if (contentLength > 0) {
                            byte[] fileBytes = new byte[contentLength];
                            dis.readFully(fileBytes, 0, contentLength);

                            File downloadedContent = new File(filename);
                            FileOutputStream fis = new FileOutputStream(downloadedContent);
                            fis.write(fileBytes);
                            fis.close();
                            System.out.println("File received and downloaded");
                            break;
                        }
                    }
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }
        }
    }
}
