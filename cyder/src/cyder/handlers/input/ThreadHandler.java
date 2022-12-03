package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.audio.GeneralAndSystemAudioPlayer;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.MasterYoutubeThread;

/** A handler to handle things related to thread ops. */
public class ThreadHandler extends InputHandler {
    /** Suppress default constructor. */
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
