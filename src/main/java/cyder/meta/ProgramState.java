package cyder.meta;

/**
 * The program states of Cyder.
 */
public enum ProgramState {
    /**
     * The default state of Cyder, where the AWT event queue 0 thread is reasonably accessible.
     */
    NORMAL(true),

    /**
     * The active frames of Cyder are performing a dancing routine.
     * This causes a freeze on the AWT event queue 0 thread but it is tolerable
     * as the user can still stop a dance event.
     */
    DANCING(false);

    /**
     * Whether the watchdog counter should be incremented for this current state.
     */
    private final boolean shouldIncrementWatchdog;

    ProgramState(boolean shouldIncrementWatchdog) {
        this.shouldIncrementWatchdog = shouldIncrementWatchdog;
    }

    /**
     * Returns whether the watchdog counter should be incremented for this current state.
     *
     * @return whether the watchdog counter should be incremented for this current state
     */
    public boolean isShouldIncrementWatchdog() {
        return shouldIncrementWatchdog;
    }
}
