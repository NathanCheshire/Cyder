package cyder.time;

/**
 * The valid program states of Cyder.
 */
public enum ProgramState {
    /**
     * The default state of Cyder, where the AWT event queue 0 thread is reasonably accessible.
     */
    NORMAL,
    /**
     * The active frames of Cyder are performing a dancing routine.
     * This causes a freeze on the AWT event queue 0 thread but it is tolerable
     * as the user can still stop a dance event.
     */
    DANCING
}
