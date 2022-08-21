package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.MasterYoutubeThread;
import cyder.utils.IOUtil;

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

        if (getInputHandler().inputWithoutSpacesIs("randomyoutube")) {
            MasterYoutubeThread.start(1);
        } else if (getInputHandler().inputWithoutSpacesIs("stopscript")) {
            MasterYoutubeThread.killAll();
            getInputHandler().println("YouTube scripts have been killed.");
        } else if (getInputHandler().inputWithoutSpacesIs("stopmusic")) {
            IOUtil.stopGeneralAudio();
        } else {
            ret = false;
        }

        return ret;
    }
}
