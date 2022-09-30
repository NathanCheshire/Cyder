package cyder.utils;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import java.lang.management.ManagementFactory;

/**
 * Utilities related to the JVM.
 */
public final class JvmUtil {
    /**
     * Suppress default constructor.
     */
    private JvmUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The string to look for when analyzing the runtime mx bean for whether the current JVM
     * has been launched in "debug mode" and may be suspended.
     */
    private static final String IN_DEBUG_MODE_KEY_PHRASE = "-agentlib:jdwp";

    /**
     * Returns whether the current jvm session is in debug mode meaning
     * thread could be externally suspended.
     *
     * @return whether the current jvm session is in debug mode meaning
     * thread could be externally suspended
     */
    public static boolean currentInstanceLaunchedWithDebug() {
        //System.out.println(ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        return ManagementFactory.getRuntimeMXBean()
                .getInputArguments().toString().contains(IN_DEBUG_MODE_KEY_PHRASE);
    }
}
