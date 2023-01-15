package cyder.subroutines;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.errorprone.annotations.Immutable;
import cyder.strings.CyderStrings;

/**
 * A subroutine to execute on Cyder startup.
 */
@Immutable
public final class Subroutine {
    /**
     * The subroutine failed string.
     */
    private static final String SUBROUTINE_FAILED = "Subroutine failed";

    /**
     * The routine to invoke.
     */
    private final Supplier<Boolean> routine;

    /**
     * The name of the thread to invoke the routine inside of if this is a parallel subroutine.
     */
    private final String threadName;

    /**
     * The failure message to use in the case of the supplier returning false.
     */
    private final String onFailureMessage;

    /**
     * Constructs a new sufficient subroutine.
     *
     * @param routine    the routine to execute
     * @param threadName the name of the thread to execute the routine using
     *                   if the routine is not sequential but instead parallel
     */
    public Subroutine(Supplier<Boolean> routine, String threadName) {
        this(routine, threadName, SUBROUTINE_FAILED);
    }

    /**
     * Constructs a new sufficient subroutine.
     *
     * @param routine          the routine to execute
     * @param threadName       the name of the thread to execute the routine using
     *                         if the routine is not sequential but instead parallel
     * @param onFailureMessage the failure message to use in the case of the supplier returning false
     */
    public Subroutine(Supplier<Boolean> routine, String threadName, String onFailureMessage) {
        Preconditions.checkNotNull(routine);
        Preconditions.checkNotNull(threadName);
        Preconditions.checkArgument(!threadName.isEmpty());
        Preconditions.checkNotNull(onFailureMessage);
        Preconditions.checkArgument(!onFailureMessage.isEmpty());

        this.routine = routine;
        this.threadName = threadName;
        this.onFailureMessage = onFailureMessage;
    }

    /**
     * Returns the routine for this sufficient subroutine to execute.
     *
     * @return the routine for this subroutine
     */
    public Supplier<Boolean> getRoutine() {
        return routine;
    }

    /**
     * Returns the thread name for this sufficient subroutine to use if the thread
     * should be executed in parallel instead of sequentially.
     *
     * @return the thread name for this subroutine
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Returns the on failure message for this subroutine.
     *
     * @return the on failure message for this subroutine
     */
    public String getOnFailureMessage() {
        return onFailureMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = threadName.hashCode();
        ret = 31 * ret + onFailureMessage.hashCode();
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
        return getThreadName().equals(other.getThreadName())
                && getOnFailureMessage().equals(other.getOnFailureMessage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Subroutine{"
                + "routine="
                + routine
                + ", threadName="
                + CyderStrings.quote
                + threadName
                + CyderStrings.quote
                + ", onFailureMessage="
                + onFailureMessage
                + "}";
    }
}
