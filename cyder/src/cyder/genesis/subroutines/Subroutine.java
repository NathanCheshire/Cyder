package cyder.genesis.subroutines;

import com.google.common.base.Preconditions;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

class Subroutine {
    /**
     * The routine to invoke.
     */
    private final Runnable routine;

    /**
     * The name of the thread to invoke the routine inside of if this is a parallel subroutine.
     */
    private final String threadName;

    /**
     * Suppress default constructor.
     */
    private Subroutine() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs a new sufficient subroutine.
     *
     * @param routine    the routine to execute
     * @param threadName the name of the thread to execute the routine using
     *                   if the routine is not sequential but instead parallel
     */
    public Subroutine(Runnable routine, String threadName) {
        this.routine = Preconditions.checkNotNull(routine);
        this.threadName = Preconditions.checkNotNull(threadName);

        Preconditions.checkArgument(!threadName.isEmpty());
    }

    /**
     * Returns the routine for this sufficient subroutine to execute.
     */
    public Runnable getRoutine() {
        return routine;
    }

    /**
     * Returns the thread name for this sufficient subroutine to use if the thread
     * should be executed in parallel instead of sequentially.
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = routine.hashCode();
        ret = 31 * ret + threadName.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Subroutine)) {
            return false;
        }

        Subroutine other = (Subroutine) o;
        return getRoutine().equals(other.getRoutine())
                && getThreadName().equals(other.getThreadName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Subroutine{"
                + "routine=" + routine
                + ", threadName="
                + CyderStrings.quote
                + threadName
                + CyderStrings.quote
                + "}";
    }
}
