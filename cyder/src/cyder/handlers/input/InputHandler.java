package cyder.handlers.input;

import cyder.console.Console;

/**
 * A base of protected static utility methods for handles.
 */
public abstract class InputHandler {
    /**
     * Returns the Console's input handler.
     *
     * @return the Console's input handler
     */
    protected static BaseInputHandler getInputHandler() {
        return Console.INSTANCE.getInputHandler();
    }
}
