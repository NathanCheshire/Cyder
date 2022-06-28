package cyder.genesis.subroutines;

/**
 * A priority level for a startup subroutine.
 */
public enum SubroutinePriority {
    /**
     * A subroutine which must complete successfully.
     */
    NECESSARY,

    /**
     * A subroutine which should complete successfully but is not imperative.
     */
    SUFFICIENT
}
