package cyder.messaging;

import cyder.ui.CyderFrame;
import cyder.ui.CyderPasswordField;

import javax.swing.*;

//todo min/max/right now/feels like temperature bar graph on weather widget
//todo consolidate lines if they're duplicates except for timestamp, override timestamp with current one and add x2,x3... etc.

public class ClientView {
    //dm view similar to ConsoleFrame, should support images, files, and text
    //this class is not static due to being able to DM multiple users at once in different frames
    private CyderFrame messagingFrame;
    private CyderPasswordField inputField;
    private JTextPane outputArea;
}
