package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import javax.swing.*;

/**
 * A handler for printing images.
 */
public class PrintImageHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private PrintImageHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle({"java", "msu", "nathan", "html", "css", "docker", "redis"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("java")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/duke.png"));
        } else if (getInputHandler().commandIs("msu")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/msu.png"));
        } else if (getInputHandler().commandIs("nathan")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/me.png"));
        } else if (getInputHandler().commandIs("html")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/html5.png"));
        } else if (getInputHandler().commandIs("css")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/css.png"));
        } else if (getInputHandler().commandIs("docker")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/Docker.png"));
        } else if (getInputHandler().commandIs("redis")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/Redis.png"));
        } else {
            ret = false;
        }

        return ret;
    }
}
