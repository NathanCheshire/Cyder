package main.java.cyder.handlers.input;

import main.java.cyder.annotations.Handle;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;
import main.java.cyder.props.PropLoader;
import main.java.cyder.props.Props;
import main.java.cyder.strings.CyderStrings;

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
                getInputHandler().println("Reloaded props. Props loaded: " + PropLoader.getNumProps());
            }
        } else ret = false;

        return ret;
    }
}
