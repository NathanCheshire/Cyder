package cyder.widgets;

import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.messaging.Client;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.ui.CyderTextField;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.awt.*;

public class MessagingWidget {
    private static CyderFrame messagingFrame;
    private static Client ourClient;

    public static void showGUI() {
        if (messagingFrame != null)
            messagingFrame.dispose();

        messagingFrame = new CyderFrame(600,600);
        messagingFrame.setTitle("Private Messaging");

        CyderLabel connectionLabel = new CyderLabel("Foreign IP", SwingConstants.CENTER);
        connectionLabel.setFont(new Font("Agency FB", Font.BOLD, 30));
        int width = CyderFrame.getMinWidth(connectionLabel.getText(), connectionLabel.getFont());
        connectionLabel.setBounds(messagingFrame.getWidth() / 2 - width / 2,40,  width, 60);
        messagingFrame.getContentPane().add(connectionLabel);

        CyderTextField ipFrame = new CyderTextField(0);
        ipFrame.setRegexMatcher("[0-9:]");

        messagingFrame.setVisible(true);
        messagingFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    private void connect(String ip) {
       try {
           ourClient = new Client(ConsoleFrame.getConsoleFrame().getUUID(),
                   UserUtil.extractUser().getName(), messagingFrame);
           ourClient.connect(ip);
       } catch (Exception e) {
           ErrorHandler.silentHandle(e);
           messagingFrame.notify("Sorry, but we were unable to connect to that Cyder user at this time");
       }
    }
}
