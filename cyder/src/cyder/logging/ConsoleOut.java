package cyder.logging;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * The possible things printed/appended to the console output pane.
 */
public enum ConsoleOut {
    /**
     * A string type.
     */
    STRING("STRING"),

    /**
     * An image such as a {@link BufferedImage} or an {@link ImageIcon}.
     */
    IMAGE("IMAGE"),

    /**
     * A {@link JComponent}.
     */
    J_COMPONENT("J_COMPONENT"),

    /**
     * An unknown output type.
     */
    UNKNOWN("UNKNOWN");

    /**
     * The string representation for this console out type.
     */
    private final String stringRepresentation;

    ConsoleOut(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    /**
     * Returns the string representation for this console out type.
     *
     * @return the string representation for this console out type
     */
    public String getStringRepresentation() {
        return stringRepresentation;
    }

    /**
     * An opening bracket.
     */
    private static final String OPENING_BRACKET = "[";

    /**
     * A closing bracket.
     */
    private static final String CLOSING_BRACKET = "[";

    /**
     * The suffix for a log tag for a console out type.
     */
    private static final String COLON_SPACE = ": ";

    /**
     * Returns the log tag to use for this console out type.
     *
     * @return the log tag to use for this console out type
     */
    public String getLogTag() {
        return OPENING_BRACKET + stringRepresentation + CLOSING_BRACKET + COLON_SPACE;
    }
}
