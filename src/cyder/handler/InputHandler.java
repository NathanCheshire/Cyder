package cyder.handler;

import cyder.utilities.StringUtil;

import javax.swing.*;


public class InputHandler {
    //todo this class will be associated with a specific instance of ConsoleFrame to handle all input
    // and direct the resulting flow and output
    // console frame (has-a) input handler (has-a) string util and context engine


    //todo singular notes frames so one object

    //todo askew is broken when successive calls, making a testing widget for this

    //todo implement mapping links, you'll need to change how user data is stored
    // make an issue for this and finally switching to binary writing

    //todo custom list display that's clickable instead of name list?
    // this could solve the scroll bar issue if you just use a scrollpane with jtextpane

    //todo check signatures for correctness and against actual files
    //todo file signature widget

    //todo pixelate image widget

    //todo background image changing doesn't move just the background
    //todo flipping console is broken
    //todo fipping console resets output area position

    //todo logout should end all executors and threads so only thing happening is login frame
    // all exeuctors should be spun once when logged in so inside of init console frame
    // all threads that continue should be able to be found and killed when logging out

    private JTextPane outputArea;
    private StringUtil stringUtil;

    private InputHandler() {} //no instantiation without a valid outputArea to use

    public InputHandler(JTextPane outputArea) {
        this.outputArea = outputArea;
        stringUtil = new StringUtil(outputArea);
    }

    public void handle(JTextPane outputArea, String operation) {
        String firstWord = StringUtil.firstWord(operation);

        //todo split off into different handle sections
        // printing
        // widgets
        // calculations
        // ui settings
        // etc
    }

    public void setOutputArea(JTextPane outputArea) {
        this.outputArea = outputArea;
    }

    public JTextPane getOutputArea() {
        return this.outputArea;
    }

    @Override
    public String toString() {
        return "InputHandler object (I guess this is just the todo field though... *sad pony noises*), hash=" + this.hashCode();
    }
}