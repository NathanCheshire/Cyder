package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.internal.Logger;

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

    /**
     * The key for determining if props are runtime reloadable.
     */
    private static final String RELOAD_PROPS_KEY = "props_reloadable";

    @Handle("reload props")
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputWithoutSpacesIs("reloadprops")) {
            if (!PropLoader.propExists(RELOAD_PROPS_KEY)) {
                getInputHandler().println("Reload prop key not found");
            }
            if (!PropLoader.getBoolean(RELOAD_PROPS_KEY)) {
                getInputHandler().println("Reloading props is currently disabled"
                        + " during runtime, check your props file");
            } else {
                Logger.log(Logger.Tag.DEBUG, "Reloading props");
                PropLoader.reloadProps();
                Logger.log(Logger.Tag.DEBUG, "Props reloaded");
                getInputHandler().println("Reloaded props. Props loaded: " + PropLoader.getProps().size());
            }
        } else ret = false;

        return ret;
    }
}
