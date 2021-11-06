package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderRegexPatterns;
import cyder.genesis.GenesisShare;
import cyder.handlers.ErrorHandler;
import cyder.messaging.Client;
import cyder.ui.*;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.awt.*;

public class MessagingWidget {
    private static CyderFrame messagingFrame;
    private static Client ourClient;

    public static void showGUI() {
        if (messagingFrame != null)
            messagingFrame.dispose();

        messagingFrame = new CyderFrame(600,350);
        messagingFrame.setTitle("Private Messaging");

        CyderLabel connectionLabel = new CyderLabel("Foreign IP", SwingConstants.CENTER);
        connectionLabel.setFont(new Font("Agency FB", Font.BOLD, 30));
        int width = CyderFrame.getMinWidth(connectionLabel.getText(), connectionLabel.getFont());
        connectionLabel.setBounds(messagingFrame.getWidth() / 2 - width / 2,40,  width, 60);
        messagingFrame.getContentPane().add(connectionLabel);

        CyderTextField ipField = new CyderTextField(0);
        ipField.setBounds(150,100,300,40);
        messagingFrame.getContentPane().add(ipField);

        JLabel validatedLabel = new JLabel("Not validated", SwingConstants.CENTER);

        CyderButton vadliateIPButton = new CyderButton("Validate");
        vadliateIPButton.setBounds(150,160,300,40);
        messagingFrame.getContentPane().add(vadliateIPButton);
        vadliateIPButton.addActionListener(e -> {
            if (ipField.getText().trim().matches(CyderRegexPatterns.ipv4Pattern)) {
                validatedLabel.setText("Valid ipv4 address");
                validatedLabel.setForeground(CyderColors.regularGreen);
            } else {
                ipField.setText("");
                validatedLabel.setText("Invalid ipv4 address");
                validatedLabel.setForeground(CyderColors.regularRed);
            }
        });

        validatedLabel.setFont(CyderFonts.weatherFontSmall);
        validatedLabel.setForeground(CyderColors.regularRed);
        validatedLabel.setBounds(150, 220, 300, 30);
        messagingFrame.getContentPane().add(validatedLabel);

        CyderButton sendRequestButton = new CyderButton("Send Request");
        sendRequestButton.setBounds(150,270,300,40);
        messagingFrame.getContentPane().add(sendRequestButton);
        sendRequestButton.addActionListener(e -> {
            String ip = ipField.getText().trim();

            if (ip.matches(CyderRegexPatterns.ipv4Pattern)) {
                //todo instead of notifying, pop into a waiting lobby frame that will exit if the other client denies it or timesout
                // this should be a completely separate chat window
                messagingFrame.notify("Sending request to: " + ip + ":" + Client.TOR_PORT);
                ourClient.connect(ip);
            } else {
                messagingFrame.notify("Sorry, but the entered ipv4 address is invalid");
            }
        });

        messagingFrame.setVisible(true);
        messagingFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());

        //startup our client so that we can receive request and send requests
        ourClient = new Client(ConsoleFrame.getConsoleFrame().getUUID(), UserUtil.extractUser().getName(), messagingFrame);
    }

    private void connect(String ip) {
       try {
           ourClient = new Client(ConsoleFrame.getConsoleFrame().getUUID(),
                   UserUtil.extractUser().getName(), messagingFrame);
           ourClient.connect(ip);
       } catch (Exception e) {
           ErrorHandler.silentHandle(e);
       }
    }
}
