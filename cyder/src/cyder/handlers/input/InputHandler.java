package cyder.handlers.input;

import cyder.console.Console;

/** A base class for InputHandlers to extend in order to enhance readability. */
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
