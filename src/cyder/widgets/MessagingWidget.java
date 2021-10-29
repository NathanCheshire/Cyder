package cyder.widgets;

import cyder.handler.ErrorHandler;
import cyder.messaging.Client;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.UserUtil;

public class MessagingWidget {
    CyderFrame messagingFrame;
    Client ourClient;

    public static void showGUI() {

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
