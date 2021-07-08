package cyder.handler;

import cyder.threads.BletchyThread;
import cyder.threads.MasterYoutube;
import cyder.utilities.StringUtil;

import javax.swing.*;


public class InputHandler {
    //todo new get delay increment method in ConsoleFrame since fullscreen takes too long

    //todo correcting user data doesn't work properly, it should either work and everything be there,
    // or correct it so that everything is there, or corrupted the user and wrap the data, nothing else!

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

    //todo logout should end all executors and threads so only thing happening is login frame
    // all exeuctors should be spun once when logged in so inside of init console frame
    // all threads that continue should be able to be found and killed when logging out

    private JTextPane outputArea;
    private MasterYoutube masterYoutube;
    private BletchyThread bletchyThread;
    private boolean userInputMode;
    private String userInputDesc;

    private InputHandler() {} //no instantiation without a valid outputArea to use

    public InputHandler(JTextPane outputArea) {
        this.outputArea = outputArea;
        masterYoutube = new MasterYoutube(outputArea);
        bletchyThread = new BletchyThread(outputArea);
    }

    //todo queues, printing methods that add to the queues

    //todo also need to make login and edit user their owns widgets

    public void handle(String operation) {
        if (outputArea == null)
            throw new IllegalArgumentException("Output area not set");

        String firstWord = StringUtil.firstWord(operation);

        //todo split off into different handle sections
        // printing strings
        // printing components
        // printing imageicons
        // widgets
        // calculations
        // ui and settings
        // console commands (bin hex dumps)
        // etc
    }

    public void handleSecond(String operation) {
        if (outputArea == null)
            throw new IllegalArgumentException("Output area not set");

        String firstWord = StringUtil.firstWord(operation);

        try {
            String desc = getUserInputDesc();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Prints a suggestion as to what the user should do
     */
    public void help() {
//        String[] helpTips = {
//                "Nathan forgot to finish this; tell him to fill out this list"
//        };
//
//        println("Try typing: " + helpTips[0]);
    }

    public void logSuggestion(String suggestion) {
        //SessionLogger.log(SessionLogger.Tag.SUGGESTION, suggestion);
    }

    public void setOutputArea(JTextPane outputArea) {
        this.outputArea = outputArea;
    }

    public JTextPane getOutputArea() {
        return this.outputArea;
    }

    public MasterYoutube getMasterYoutube() {
        return masterYoutube;
    }

    public BletchyThread getBletchyThread() {
        return bletchyThread;
    }

    /**
     * Getter for this instance's input mode
     * @return - the value of user input mode
     */
    public boolean getUserInputMode() {
        return this.userInputMode;
    }

    /**
     * Set the value of secondary input mode
     * @param b - the value of input mode
     */
    public void setUserInputMode(boolean b) {
        this.userInputMode = b;
    }

    /**
     * Returns the expected secondary input description
     * @return - the input description
     */
    public String getUserInputDesc() {
        return this.userInputDesc;
    }

    /**
     * Sets this instance's secondary input description
     * @param s - the description of the input we expect to receive next
     */
    public void setUserInputDesc(String s) {
        this.userInputDesc = s;
    }

    public void close() {
        masterYoutube.killAllYoutube();
        bletchyThread.killBletchy();
        //todo other stuff to reset this in the event of a logout and other events
    }

    @Override
    public String toString() {
        return "InputHandler object, hash=" + this.hashCode() +
                "\nLinked outputArea: " + this.outputArea + "";
    }
}