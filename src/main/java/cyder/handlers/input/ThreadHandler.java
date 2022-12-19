package main.java.cyder.handlers.input;

import main.java.cyder.annotations.Handle;
import main.java.cyder.audio.GeneralAndSystemAudioPlayer;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.threads.MasterYoutubeThread;

/**
 * A handler to handle things related to thread ops.
 */
public class ThreadHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private ThreadHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"random youtube", "stop script", "stop music"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputIgnoringSpacesMatches("randomyoutube")) {
            MasterYoutubeThread.start(1);
        } else if (getInputHandler().inputIgnoringSpacesMatches("stopscript")) {
            MasterYoutubeThread.killAll();
            getInputHandler().println("YouTube scripts have been killed.");
        } else if (getInputHandler().inputIgnoringSpacesMatches("stopmusic")) {
            GeneralAndSystemAudioPlayer.stopGeneralAudio();
        } else {
            ret = false;
        }

        return ret;
    }
}
