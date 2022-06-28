package cyder.genesis.subroutines;

/**
 * An interface for startup subroutines.
 */
public interface StartupSubroutine {
    /**
     * Ensure the conditions for this subroutine are met and if not, take the appropriate actions
     * depending on this subroutine's {@link SubroutinePriority}.
     *
     * @return whether the subroutine completed successfully
     */
    boolean ensure();

    /**
     * Returns this subroutines priority. That of necessary or sufficient.
     *
     * @return this subroutines priority
     */
    SubroutinePriority getPriority();

    /**
     * Returns the error message for this subroutine should it fail.
     *
     * @return the error message for this subroutine should it fail
     */
    String getErrorMessage();

    /**
     * Performs an exit if this subroutine was a {@link SubroutinePriority#NECESSARY} subroutine.
     */
    void exit();
}
