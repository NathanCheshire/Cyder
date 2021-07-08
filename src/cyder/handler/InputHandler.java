package cyder.handler;

import cyder.utilities.StringUtil;

import javax.swing.*;


public class InputHandler {
    //todo this class will be associated with a specific instance of ConsoleFrame to handle all input
    // and direct the resulting flow and output
    // console frame (has-a) input handler (change of plans, don't link to a string util)



    //todo new get delay increment method in ConsoleFrame since fullscreen takes too long

    //todo on logout close everything and stop threads and such

    //todo correcting user data doesn't work properly, it should either work and everything be there,
    // or correct it so that everything is there, or corrupted the user and wrap the data, nothing else!

    //todo background resizing in console frame has never been correct

    //todo take away all jtextfield and use CyderTextField

    //todo corrupted users aren't saved to downloads, saved to directory up, should save to same dir as src, fix

    //todo implement mapping links, you'll need to change how user data is stored
    // make an issue for this and finally switching to binary writing

    //todo custom list display that's clickable instead of name list?
    // this could solve the scroll bar issue if you just use a scrollpane with jtextpane
    // pizza is the only one in use currently that needs multiple selection support

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