package cyder.messaging;

import cyder.genesis.GenesisShare;
import cyder.ui.CyderFrame;
import cyder.ui.CyderPasswordField;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;

//todo consolidate lines if they're duplicates except for timestamp, override timestamp with current one and add x2,x3... etc.

public class ClientView {
    //dm view similar to ConsoleFrame, should support images, files, and text
    //this class is not static due to being able to DM multiple users at once in different frames
    private CyderFrame messagingFrame;
    private CyderPasswordField inputField;
    private JTextPane outputArea;

    private boolean opened = false;

    private Socket sendingSocket;
    private String connectedUUID;
    private String connectedName;

    public ClientView(Socket sendingSocket, String connectedUUID, String connectedName) {
        this.sendingSocket = sendingSocket;
        this.connectedUUID = connectedUUID;
        this.connectedName = connectedName;
    }

    /**
     * Volatile method to open the window
     */
    public void showGUI() {
        if (opened)
            throw new IllegalArgumentException("Chat view already opened");
        else if (sendingSocket == null)
            throw new IllegalArgumentException("Sending socket not set");
        else if (connectedName.length() == 0 || connectedUUID.length() == 0)
            throw new IllegalArgumentException("Connected client uuid and name not set");
        opened = true;

        messagingFrame = new CyderFrame(1000, 600) {
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x,y,width,height);
                //todo set bounds of input and output
            }
        };
        messagingFrame.stealConsoleBackground();
        messagingFrame.setTitle("Chat Session: " + connectedName.trim());
        messagingFrame.initializeResizing();
        messagingFrame.setResizable(true);
        messagingFrame.setMinimumSize(new Dimension(600,600));
        messagingFrame.setMaximumSize(new Dimension(1000,600));

        //output area

        //input field

        messagingFrame.setVisible(true);
        messagingFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }
}
