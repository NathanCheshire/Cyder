package cyder.logging;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;

import javax.swing.*;
import java.awt.image.BufferedImage;

/** Tags for possible types printed to the console's text pane. */
public enum ConsoleOutType {
    /** A string type. */
    STRING("STRING", ImmutableList.of(String.class)),

    /** An image such as a {@link BufferedImage} or an {@link ImageIcon}. */
    IMAGE("IMAGE", ImmutableList.of(BufferedImage.class, ImageIcon.class)),

    /** A {@link JComponent}. */
    J_COMPONENT("J_COMPONENT", ImmutableList.of(JComponent.class)),

    /** An unknown output type. */
    UNKNOWN("UNKNOWN", ImmutableList.of(Object.class));

    /** The string representation for this console out type. */
    private final String stringRepresentation;

    /** The class types this type tag is used for. */
    private final ImmutableList<?> types;

    ConsoleOutType(String stringRepresentation, ImmutableList<?> types) {
        this.stringRepresentation = stringRepresentation;
        this.types = types;
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
     * Returns the class types thi tag is used for.
     *
     * @return the class types thi tag is used for
     */
    public ImmutableList<?> getTypes() {
        return types;
    }

    /**
     * Returns the log tag to use for this console out type.
     *
     * @return the log tag to use for this console out type
     */
    public String getLogTag() {
        return CyderStrings.openingBracket + stringRepresentation + CyderStrings.closingBracket
                + CyderStrings.colon + CyderStrings.space;
    }
}
