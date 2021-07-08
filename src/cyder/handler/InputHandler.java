package cyder.handler;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import cyder.genesis.GenesisShare;
import cyder.obj.Preference;
import cyder.threads.BletchyThread;
import cyder.threads.MasterYoutube;
import cyder.ui.ConsoleFrame;
import cyder.utilities.IOUtil;
import cyder.utilities.SecurityUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.LinkedList;


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
    private String operation;

    private InputHandler() {} //no instantiation without a valid outputArea to use

    public InputHandler(JTextPane outputArea) {
        this.outputArea = outputArea;
        masterYoutube = new MasterYoutube(outputArea);
        bletchyThread = new BletchyThread(outputArea);
    }

    //checks to see if a preference id was entered and if so, toggles it
    private boolean preferenceCheck(String op) {
        boolean ret = false;

        for (Preference pref : GenesisShare.getPrefs()) {
            if (op.toLowerCase().contains(pref.getID().toLowerCase())) {
                if (op.contains("1") || op.toLowerCase().contains("true")) {
                    IOUtil.writeUserData(pref.getID(), "1");
                } else if (op.contains("0") || op.toLowerCase().contains("false")) {
                    IOUtil.writeUserData(pref.getID(), "0");
                } else {
                    IOUtil.writeUserData(pref.getID(), (IOUtil.getUserData(pref.getID()).equals("1") ? "0" : "1"));
                }

                //todo console frame method refreshPrefs();
                ret = true;
            }
        }

        return ret;
    }

    //handle methods ----------------------------------------------

    public void handle(String op) {
        if (outputArea == null)
            throw new IllegalArgumentException("Output area not set");

        this.operation = op;

        String firstWord = StringUtil.firstWord(operation);

        //pre-process checks --------------------------------------
        if (StringUtil.filterLanguage(operation)) {
            println("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but that language is prohibited.");
        }

        //printing strings-----------------------------------------
        //printing components--------------------------------------
        //printing imageicons--------------------------------------
        //widgets--------------------------------------------------
        //calculations---------------------------------------------
        //ui and settings------------------------------------------
        //console commands (bin hex dumps)-------------------------
        //[other ones here]----------------------------------------
        //un-categorized-------------------------------------------

        //final attempt at unknown input---------------------------
        else {
            //try context engine validation linked to this (instace of InputHandler)

            if (handleMath(operation))
                return;

            if (evaluateExpression(operation))
                return;

            if (preferenceCheck(operation))
                return;

            unknownInput();
        }
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

    private boolean handleMath(String userInput) {
        int firstParen = userInput.indexOf("(");
        int comma = userInput.indexOf(",");
        int lastParen = userInput.indexOf(")");

        String mathop;
        double param1 = 0.0;
        double param2 = 0.0;

        try {
            if (firstParen != -1) {
                mathop = userInput.substring(0, firstParen);

                if (comma != -1) {
                    param1 = Double.parseDouble(userInput.substring(firstParen + 1, comma));

                    if (lastParen != -1) {
                        param2 = Double.parseDouble(userInput.substring(comma + 1, lastParen));
                    }
                } else if (lastParen != -1) {
                    param1 = Double.parseDouble(userInput.substring(firstParen + 1, lastParen));
                }

                if (mathop.equalsIgnoreCase("abs")) {
                    println(Math.abs(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("ceil")) {
                    println(Math.ceil(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("floor")) {
                    println(Math.floor(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("log")) {
                    println(Math.log(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("log10")) {
                    println(Math.log10(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("max")) {
                    println(Math.max(param1, param2));
                    return true;
                } else if (mathop.equalsIgnoreCase("min")) {
                    println(Math.min(param1, param2));
                    return true;
                } else if (mathop.equalsIgnoreCase("pow")) {
                    println(Math.pow(param1, param2));
                    return true;
                } else if (mathop.equalsIgnoreCase("round")) {
                    println(Math.round(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("sqrt")) {
                    println(Math.sqrt(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("convert2")) {
                    println(Integer.toBinaryString((int) (param1)));
                    return true;
                }
            }
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }

        return false;
    }

    private void unknownInput() {
        println("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but I don't recognize that command." +
                " You can make a suggestion by clicking the \"Suggest something\" button.");

        new Thread(() -> {
            try {
                ImageIcon blinkIcon = new ImageIcon("sys/pictures/icons/suggestion2.png");
                ImageIcon regularIcon = new ImageIcon("sys/pictures/icons/suggestion1.png");

                for (int i = 0 ; i < 4 ; i++) {
                    //todo suggestionButton.setIcon(blinkIcon);
                    Thread.sleep(300);
                    //todo suggestionButton.setIcon(regularIcon);
                    Thread.sleep(300);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Suggestion Button Flash").start();
    }

    //input handler
    private boolean evaluateExpression(String userInput) {
        try {
            println(new DoubleEvaluator().evaluate(StringUtil.firstCharToLowerCase(userInput.trim())));
            return true;
        } catch (Exception ignored) {}

        return false;
    }

    //random methods find a category for --------------------------

    /**
     * Prints a suggestion as to what the user should do
     */
    public void help() {
        //todo example trigger annotation for input handler that code looks through, gatheres, and outputs random ones
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

    //printing queue methods and logic ----------------------------

    //don't ever add to these lists, call the respective print functions and let them
    // handle adding them to the lists
    private LinkedList<Object> consolePrintingList = new LinkedList<>();
    private LinkedList<Object> consolePriorityPrintingList = new LinkedList<>();

    private boolean started = false;

    //console printing animation currently turned off do to concurrency issues such as
    // bletchy, youtube thread, and drawing pictures and such, maybe we just throw everything no matter
    // what into a custom OutputQueue and from there determine how to store it and print it?
    public void startConsolePrintingAnimation() {
        if (started)
            return;

        started = true;

        consolePrintingList.clear();
        consolePriorityPrintingList.clear();

        int charTimeout = 20;
        int lineTimeout = 200;

        new Thread(() -> {
            try {
                while (!ConsoleFrame.getConsoleFrame().isClosed()) {
                    //priority simply appends to the console
                    if (consolePriorityPrintingList.size() > 0) {
                        Object line = consolePriorityPrintingList.removeFirst();

                        if (line instanceof String) {
                            StyledDocument document = (StyledDocument) outputArea.getDocument();
                            document.insertString(document.getLength(), (String) line, null);
                            outputArea.setCaretPosition(outputArea.getDocument().getLength());
                        } else if (line instanceof JComponent) {
                            String componentUUID = SecurityUtil.generateUUID();
                            Style cs = outputArea.getStyledDocument().addStyle(componentUUID, null);
                            StyleConstants.setComponent(cs, (Component) line);
                            outputArea.getStyledDocument().insertString(outputArea.getStyledDocument().getLength(), componentUUID, cs);
                        } else if (line instanceof ImageIcon) {
                            outputArea.insertIcon((ImageIcon) line);
                        } else {
                            println("[UNKNOWN OBJECT]: " + line);
                        }
                    }
                    //regular will perform a typing animation on strings if no method
                    // is currently running, such as RY or Bletchy, that would cause
                    // concurrency issues
                    else if (consolePrintingList.size() > 0){
                        Object line = consolePrintingList.removeFirst();

                        if (line instanceof String) {
                            for (char c : ((String) line).toCharArray()) {
                                innerConsolePrint(c);
                                Thread.sleep(charTimeout);
                            }
                        } else if (line instanceof JComponent) {
                            String componentUUID = SecurityUtil.generateUUID();
                            Style cs = outputArea.getStyledDocument().addStyle(componentUUID, null);
                            StyleConstants.setComponent(cs, (Component) line);
                            outputArea.getStyledDocument().insertString(outputArea.getStyledDocument().getLength(), componentUUID, cs);
                        } else if (line instanceof ImageIcon) {
                            outputArea.insertIcon((ImageIcon) line);
                        } else {
                            println("[UNKNOWN OBJECT]: " + line);
                        }
                    }

                    Thread.sleep(lineTimeout);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Console Printing Animation").start();
    }

    //todo use a semaphore in genesis share for printing to help with
    // bletchy and remove last line and such concurrency issues

    private void innerConsolePrint(char c) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(c), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private boolean concurrencyIssues() {
        return MasterYoutube.isActive() || BletchyThread.isActive();
    }

    private void consleAppendChar(char c) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(c), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
    
    public void printlnImage(ImageIcon icon) {
        consolePrintingList.add(icon);
        consolePrintingList.add("\n");

        if (MasterYoutube.isActive() || BletchyThread.isActive())
            consolePrintingList.add("\n");
    }
    
    public void printImage(ImageIcon icon) {
        consolePrintingList.add(icon);
    }
    
    public void printlnImage(String filename) {
        consolePrintingList.add(new ImageIcon(filename));
        consolePrintingList.add("\n");
    }
    
    public void printImage(String filename) {
        consolePrintingList.add(new ImageIcon(filename));
    }

    public void printlnComponent(Component c) {
        consolePrintingList.add(c);
        consolePrintingList.add("\n");
    }

    public void printComponent(Component c) {
        consolePrintingList.add(c);
    }
    
    public void print(String usage) {
        if (concurrencyIssues())
            consolePriorityPrintingList.add(usage);
        else
            consolePrintingList.add(usage);
    }
    
    public void print(int usage) {
        if (concurrencyIssues())
            consolePriorityPrintingList.add(Integer.toString(usage));
        else
            consolePrintingList.add(Integer.toString(usage));
    }
    
    public void print(double usage) {
        if (concurrencyIssues())
            consolePriorityPrintingList.add(Double.toString(usage));
        else
            consolePrintingList.add(Double.toString(usage));
    }

    public void print(boolean usage) {
        if (concurrencyIssues())
            consolePriorityPrintingList.add(Boolean.toString(usage));
        else
            consolePrintingList.add(Boolean.toString(usage));
    }
    
    public void print(float usage) {
        if (concurrencyIssues())
            consolePriorityPrintingList.add(Float.toString(usage));
        else
            consolePrintingList.add(Float.toString(usage));
    }

    public void print(long usage) {
        if (concurrencyIssues())
            consolePriorityPrintingList.add(Long.toString(usage));
        else
            consolePrintingList.add(Long.toString(usage));
    }
    
    public void print(char usage) {
        if (concurrencyIssues())
            consolePriorityPrintingList.add(String.valueOf(usage));
        else
            consolePrintingList.add(String.valueOf(usage));
    }
    
    public void print(Object usage) {
        if (concurrencyIssues())
            consolePriorityPrintingList.add(usage.toString());
        else
            consolePrintingList.add(usage.toString());
    }

    public void println(String usage) {
        print(usage + "\n");
    }

    public void println(int usage) {
        print(usage + "\n");
    }
    
    public void println(double usage) {
        print(usage + "\n");
    }

    public void println(boolean usage) {
        print(usage + "\n");
    }

    public void println(float usage) {
        print(usage + "\n");
    }
    
    public void println(long usage) {
        print(usage + "\n");
    }
    
    public void println(char usage) {
        print(usage + "\n");
    }
    
    public void println(Object usage) {
        print(usage + "\n");
    }
    
    private boolean eic(String eic) {
        return operation.equalsIgnoreCase(eic);
    }
    
    private boolean has(String compare) {
        return operation.toLowerCase().contains(compare.toLowerCase());
    }

    private boolean hasWord(String compare) {
        if (operation.equalsIgnoreCase(compare) ||
                operation.toLowerCase().contains(' ' + compare.toLowerCase() + ' ') ||
                operation.toLowerCase().contains(' ' + compare.toLowerCase()) ||
                operation.toLowerCase().contains(compare.toLowerCase() + ' '))
            return true;
        else return operation.toLowerCase().contains(compare.toLowerCase() + ' ');
    }

    //direct JTextPane manipulation methods -----------------------

    private void clc() {
        outputArea.setText("");
    }
}