package cyder.handler;

import cyder.utilities.StringUtil;

import javax.swing.*;


public class InputHandler {
    //todo this class will be associated with a specific instance of ConsoleFrame to handle all input
    // and direct the resulting flow and output
    // console frame (has-a) input handler (has-a) string util and context engine


    //todo youtube thumbnail be able to say add to backgrounds

    //todo implement cyder frame adding buttons to specific indexes for drag label and refresh
    //todo implement right title for cyder frame and then swap buttons to other side

    //todo on startup, look through error files, if there is nothing in one, delete it

    //todo when starting youtube thread, calculate how long until it finishes (lol) and notify on frame

    //todo make enter-animation work and utilize

    //todo boolean for rounded corners
    // setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
    // make rounded border for frames and make sure drag label components aren't too close to edge

    //todo user logs and throws should eventually be converted to binary data so that you can only access it through the program
    //todo add reading and writing calls to/from userdata to the log
    //todo suggestion will be in a log summary for a log, logs stored in logs dir

    //todo show full file name for user files scroll "music/EpicDubstep.mp3"

    //todo scrollbar on dir messes up when selecting element fix by making not opqeue or not fading away?

    //todo test signatures against files and make a widget for this

    //todo file space for overall program when cypherlenovo, deletes folder if not cypherlenovo

    //todo userfiles aside from music and backgrounds called files

    //todo use displayText = "<html><div style='text-align: center;'>" + displayText + "</div></html>";
    // in Jlabels

    //todo background image changing doesn't move just the background

    //todo make input field like the input field for login (actually a password field with the natche@cyder~$)

    //todo if cyder hasn't started in a while, say welcome back $username, did you miss me?
    // or some variation of that (last start/exit time in userdata)

    //todo implement preferences sliders and use them combined with cyderlabels in a jtextpane for user prefs

    //todo logout should end all executors and threads so only thing happening is login frame
    // all exeuctors should be spun once when logged in so inside of init console frame
    // all threads that continue should be able to be found and killed when logging out

    //todo fix bug: log in as different user when already logged in as nathan and then delete this new account you logged into
    // will say the first user was corrupted and say sorry + "first user name"

    //todo use gradient for weather.png and use sunrise and sunseticons
    //todo load weather stats in separate thread so window pops up quickly

    //todo flipping console is broken
    //todo fipping console resets output area position

    private JTextPane outputArea;
    private StringUtil stringUtil;

    public InputHandler(JTextPane outputArea) {
        this.outputArea = outputArea;
        stringUtil = new StringUtil(outputArea);
    }

    public void handle(JTextPane outputArea, String operation) {
        String firstWord = StringUtil.firstWord(operation);


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
