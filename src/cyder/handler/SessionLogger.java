package cyder.handler;

import java.io.File;

public class SessionLogger {
    //todo user logs and throws should eventually be converted to binary data so that you can only access it through the program
    //todo add reading and writing calls to/from userdata to the log
    //todo suggestion will be in a log summary for a log, logs stored in logs dir
    //todo log these in chat log. Tags: [USER], [SYSTEM], [EXCEPTION] (link to exception file)

    //TODO [hh:mm:ss] [tag]: data about tag (error stack trace, actions performed,
    // methods called, what we printed to the console, what was typed, buttons clicked... so on)

    public enum Tag {
        CLIENT, SERVER, EXCEPTION, ACTION, LINK, EOL
    }

    public SessionLogger(File outputFile) {

    }

    public SessionLogger() {
        //create the file
    }
}
