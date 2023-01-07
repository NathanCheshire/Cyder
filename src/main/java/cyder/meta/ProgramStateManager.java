package cyder.meta;

/**
 * The manager for Cyder's program state.
 */
public enum ProgramStateManager {
    /**
     * The program state manager instance.
     */
    INSTANCE;

    /**
     * The current program state.
     */
    private ProgramState currentProgramState = ProgramState.NORMAL;

    /**
     * Returns the current program state.
     *
     * @return the current program state
     */
    public ProgramState getCurrentProgramState() {
        return currentProgramState;
    }

    /**
     * Sets the current program state.
     *
     * @param currentProgramState the current program state
     */
    public void setCurrentProgramState(ProgramState currentProgramState) {
        this.currentProgramState = currentProgramState;
    }
}
