package punch;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.subroutines.NecessarySubroutines;

/**
 * A time card time logging program.
 */
public final class Punch {
    /**
     * Suppress default constructor.
     */
    private Punch() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The list of jvm arguments supplied to the punch program.
     */
    private static ImmutableList<String> jvmArguments;

    /**
     * The main entry point for the punch program.
     *
     * @param args the jvm arguments
     */
    public static void main(String[] args) {
        jvmArguments = ImmutableList.copyOf(args);

        NecessarySubroutines.executePunch();

        // todo
    }
}
