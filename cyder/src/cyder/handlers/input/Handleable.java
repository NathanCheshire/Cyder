package cyder.handlers.input;

import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.Logger;

/**
 * A handler that takes care and handles all pieces of some Cyder operation.
 */
public interface Handleable {
    /**
     * The types of handlers.
     */
    enum Type {
        /**
         * An common input was handled properly.
         */
        PRIMARY,
        /**
         * A rarer form of input was handled properly.
         * This is usually
         */
        FINAL,
    }

    /**
     * Attempts to handle the provided input and returns whether the input was successfully processed.
     *
     * @return whether the handler successfully parsed the input
     */
    boolean handle();

    /**
     * Returns the type for this handle method.
     *
     * @return the type for this handle method
     */
    default Type getType() {
        return Type.PRIMARY;
    }

    /**
     * Returns the log message for this handler.
     *
     * @return the log message for this handler
     */
    default String getLogMessage() {
        return "Someone forgot to override the log message :/";
    }

    /**
     * Logs a successful handle action of this handler.
     */
    default void logSelfHandle() {
        Logger.log(Logger.Tag.HANDLE_METHOD, getLogMessage());
    }

    // ------------------------------------------------------------------------
    // easier than typing ConsoleFrame.INSTANCE.getInputHandler() over and over
    // ------------------------------------------------------------------------

    /**
     * Returns the console frame's input handler.
     *
     * @return the console frame's input handler
     */
    default BaseInputHandler getInputHandler() {
        return ConsoleFrame.INSTANCE.getInputHandler();
    }

    // --------------------------------------
    // command and argument accessor wrappers
    // --------------------------------------

    /**
     * Returns the current user issued command.
     *
     * @return the current user issued command
     */
    default String getCommand() {
        return getInputHandler().getCommand();
    }

    /**
     * Returns whether the provided string equals, ignoring case, the current input handler command.
     *
     * @param string the string to test for
     * @return whether the provided string equals ignore case the current input handler command
     */
    default boolean commandIs(String string) {
        return getInputHandler().commandIs(string);
    }

    /**
     * Returns whether the arguments array contains the expected number of arguments.
     *
     * @param expectedSize the expected size of the command arguments
     * @return whether the arguments array contains the expected number of arguments
     */
    default boolean checkArgsLength(int expectedSize) {
        return getInputHandler().checkArgsLength(expectedSize);
    }

    /**
     * Returns the command argument at the provided index.
     * Returns null if the index is out of bounds instead of throwing.
     *
     * @param index the index to retrieve the command argument of
     * @return the command argument at the provided index
     */
    default String getArg(int index) {
        return getInputHandler().getArg(index);
    }

    /**
     * Returns the arguments in String form separated by spaces.
     *
     * @return the arguments in String form separated by spaces
     */
    default String argsToString() {
        return getInputHandler().argsToString();
    }

    /**
     * Returns the original user input, that of command followed by the arguments.
     *
     * @return the original user input, that of command followed by the arguments
     */
    default String commandAndArgsToString() {
        return getInputHandler().commandAndArgsToString();
    }
}
