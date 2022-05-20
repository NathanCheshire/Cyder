package cyder.handlers.input;

import cyder.exceptions.IllegalMethodException;
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
     * @return whether the input was successfully processed
     */
    default <T> boolean handle(T[] tees) {
        throw new IllegalMethodException("Handle method not implemented");
    }

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
