package cyder.handlers.input;

import cyder.handlers.internal.Logger;

/**
 * A handler that takes care and handles all pieces of some Cyder operation.
 */
public interface Handler {
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
     * @param tees          the objects necessary for handling
     * @param <T>           the type of objects provided for handling
     * @param userTriggered whether the input was triggered by a user or an artificial source
     * @return whether the input was successfully processed
     */
    <T> boolean handle(T[] tees, boolean userTriggered);

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
        return "Primary handler succeeded";
    }

    /**
     * Logs a handle action of this handler.
     */
    default void logSelfHandle() {
        Logger.log(Logger.Tag.HANDLE_METHOD, getLogMessage());
    }
}
