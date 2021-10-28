package cyder.widgets;

import cyder.handler.ErrorHandler;
import cyder.messaging.Client;
import cyder.ui.ConsoleFrame;
import cyder.utilities.UserUtil;

import java.net.Socket;

public class MessagingWidget {
    //todo spawn off connection windows to other Cyder users provided this user knows an ip and port they're trying to reach

    public static void showGUI() {

    }

    private void connect() {
       try {
           Client client = new Client(ConsoleFrame.getConsoleFrame().getUUID(),
                   UserUtil.extractUser().getName());
       } catch (Exception e) {
           ErrorHandler.silentHandle(e);
           //inform we were unable to connect at this time
       }
    }
}
