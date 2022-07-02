package cyder.handlers.input;

import cyder.console.ConsoleFrame;

/**
 * A base of protected static utility methods for handles.
 */
public abstract class InputHandler {
    /**
     * Returns the ConsoleFrame's input handler.
     *
     * @return the ConsoleFrame's input handler
     */
    protected static BaseInputHandler getInputHandler() {
        return ConsoleFrame.INSTANCE.getInputHandler();
    }
}
