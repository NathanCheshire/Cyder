package cyder.handlers.input;

import cyder.handlers.ConsoleFrame;

/**
 * A base of protected static utility methods for handles.
 */
public abstract class InputHandlerBase {
    /**
     * Returns the ConsoleFrame's input handler.
     *
     * @return the ConsoleFrame's input handler
     */
    protected static BaseInputHandler getInputHandler() {
        return ConsoleFrame.INSTANCE.getInputHandler();
    }
}
