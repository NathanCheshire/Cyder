package cyder.handlers.input;

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
     * @return whether the input was successfully processed
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

    // -------------------------
    // print and utility methods
    // -------------------------

    // todo prints, commandIs, getArg, etc. these should reference base methods
}
