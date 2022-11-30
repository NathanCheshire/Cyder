package cyder.genesis;

/** Possible modes Cyder can exist in for a certain instance. */
public enum ProgramMode {
    /** The normal, user mode for Cyder, started from a JAR file. */
    NORMAL("Normal", 0),

    /** Cyder was started from an IDE and not a JAR file. */
    IDE_NORMAL("IDE Normal", 1),

    /** Cyder was started in a debug mode by an IDE. */
    IDE_DEBUG("IDE Debug", 2),

    /** Cyder was started via an AutoCypher. */
    DEVELOPER_DEBUG("Developer Debug", 3);

    /** The name of the program mode. */
    private final String name;

    /** The level of priority of the program mode. */
    private final int priorityLevel;

    ProgramMode(String name, int priorityLevel) {
        this.name = name;
        this.priorityLevel = priorityLevel;
    }

    /**
     * Returns the name of the program mode.
     *
     * @return the name of the program mode
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the priority level of the program mode.
     *
     * @return the priority level of the program mode
     */
    public int getPriorityLevel() {
        return priorityLevel;
    }
}
