package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.PropLoader;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;

/**
 * A handler for utilities related to props.
 */
public class PropHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private PropHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle("reload props")
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputIgnoringSpacesMatches("reloadprops")) {
            if (!Props.propsReloadable.getValue()) {
                getInputHandler().println("Reloading props is currently disabled"
                        + " during runtime, check your props file");
            } else {
                Logger.log(LogTag.PROPS_ACTION, "Reloading props");
                PropLoader.reloadProps();
                Logger.log(LogTag.PROPS_ACTION, "Props reloaded");
                int size = PropLoader.getPropsSize();
                getInputHandler().println("Reloaded " + size + " "
                        + StringUtil.getWordFormBasedOnNumber(size, "prop"));
            }
        } else ret = false;

        return ret;
    }
}
