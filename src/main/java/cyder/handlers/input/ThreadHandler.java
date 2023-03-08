package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.audio.GeneralAudioPlayer;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.threads.YoutubeUuidCheckerManager;

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
            YoutubeUuidCheckerManager.INSTANCE.start(1);
        } else if (getInputHandler().inputIgnoringSpacesMatches("stopscript")) {
            YoutubeUuidCheckerManager.INSTANCE.killAll();
            getInputHandler().println("YouTube scripts have been killed.");
        } else if (getInputHandler().inputIgnoringSpacesMatches("stopmusic")) {
            GeneralAudioPlayer.stopGeneralAudio();
        } else {
            ret = false;
        }

        return ret;
    }
}
