package cyder.snakes;

import cyder.constants.CyderStrings;

/** The arguments accepted by argparse for the python_functions script. */
public enum PythonArgument {
    COMMAND("c", "command", true),
    INPUT("i", "input", false),
    RADIUS("r", "radius", false);

    /** The shorthand argument for this argument. */
    private final String shorthandArgument;

    /** The full argument for this argument. */
    private final String fullArgument;

    /** Whether this argument is required. */
    private final boolean required;

    PythonArgument(String shorthandArgument, String fullArgument, boolean required) {
        this.shorthandArgument = shorthandArgument;
        this.fullArgument = fullArgument;
        this.required = required;
    }

    /**
     * Returns the shorthand argument for this argument.
     *
     * @return the shorthand argument for this argument
     */
    public String getShorthandArgument() {
        return CyderStrings.dash + shorthandArgument;
    }

    /**
     * Returns the full argument for this argument.
     *
     * @return full argument for this argument
     */
    public String getFullArgument() {
        return CyderStrings.dash + CyderStrings.dash + fullArgument;
    }

    /**
     * Returns whether this argument is required.
     *
     * @return whether this argument is required
     */
    public boolean isRequired() {
        return required;
    }
}
