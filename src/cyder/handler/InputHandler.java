package cyder.handler;

import cyder.utilities.StringUtil;

import javax.swing.*;


public class InputHandler {
    //todo this class will be associated with a specific instance of ConsoleFrame to handle all input
    // and direct the resulting flow and output
    // console frame (has-a) input handler (has-a) string util and context engine

    //todo animations seem slow?
    // start animation bodge with checking for relative to null and then using enterAnimation works,
    // on start up apps but not once already started?

    //todo boolean for rounded corners for program as a whole
    // setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
    // make rounded border for frames and make sure drag label components aren't too close to edge
    // make sure refreshing works still with everything added to drag label

    //todo user logs and throws should eventually be converted to binary data so that you can only access it through the program
    //todo add reading and writing calls to/from userdata to the log
    //todo suggestion will be in a log summary for a log, logs stored in logs dir
    //todo log these in chat log. Tags: [USER], [SYSTEM], [EXCEPTION] (link to exception file)

    //todo custom list display that's clickable instead of name list?
    // this could solve the scroll bar issue if you just use a scrollpane with jtextpane

    //todo check signatures for correctness and against actual files
    //todo file signature widget

    //todo pixelate image widget

    //todo background image changing doesn't move just the background

    //todo make input field like the input field for login (actually a password field with the natche@cyder~$)

    //todo logout should end all executors and threads so only thing happening is login frame
    // all exeuctors should be spun once when logged in so inside of init console frame
    // all threads that continue should be able to be found and killed when logging out

    //todo flipping console is broken
    //todo fipping console resets output area position

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
