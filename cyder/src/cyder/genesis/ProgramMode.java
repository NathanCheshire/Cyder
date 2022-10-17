package cyder.genesis;

/**
 * Possible modes Cyder can exist in for a certain instance.
 */
public enum ProgramMode {
    NORMAL("Normal", 0),
    IDE_DEBUG("IDE Debug", 1),
    DEVELOPER_DEBUG("Developer Debug", 2);

    /**
     * The name of the program mode.
     */
    private final String name;

    /**
     * The level of priority of the program mode.
     */
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
