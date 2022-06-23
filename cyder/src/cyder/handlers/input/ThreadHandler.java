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

    @Handle({"randomyoutube", "stopscript", "stopmusic"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("randomyoutube")) {
            MasterYoutubeThread.start(1);
        } else if (getInputHandler().commandIs("stopscript")) {
            MasterYoutubeThread.killAll();
            getInputHandler().println("YouTube scripts have been killed.");
        } else if (getInputHandler().commandIs("stopmusic")) {
            IOUtil.stopGeneralAudio();
        } else {
            ret = false;
        }

        return ret;
    }
}
