package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.enums.SystemPropertyKey;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.Cyder;

import java.io.File;
import java.lang.management.ManagementFactory;

import static cyder.constants.CyderStrings.quote;

/**
 * Utilities related to the JVM.
 */
public final class JvmUtil {
    /**
     * The bin string.
     */
    private static final String BIN = "bin";

    /**
     * The name of the javaw.exe executable. The difference between java and javaw
     * is that javaw does not open up a console window or use a console to display output on.
     */
    private static final String JAVAW = "javaw.exe";

    /**
     * The name of the java.exe executable.
     */
    private static final String JAVA = "java.exe";

    /**
     * The string to look for when analyzing the runtime mx bean for whether the current JVM
     * has been launched in "debug mode" and may be suspended. This is identifiable by looking
     * for the agent lib command line argument referencing the Java Debug Wire Protocol.
     */
    private static final String IN_DEBUG_MODE_KEY_PHRASE = "-agentlib:jdwp";

    /**
     * The classpath argument.
     * Note that not using a double dash is correct here.
     */
    private static final String CLASSPATH_ARGUMENT = "-classpath";

    /**
     * Whether the current jvm session is in debug mode meaning threads could be externally suspended.
     */
    private static final boolean JVM_LAUNCHED_IN_DEBUG_MODE = ManagementFactory.getRuntimeMXBean()
            .getInputArguments().toString().contains(IN_DEBUG_MODE_KEY_PHRASE);

    /**
     * The JVM args passed to the main method. todo rename to something to do with main to make it explicit
     */
    private static ImmutableList<String> jvmArgs;

    /**
     * Suppress default constructor.
     */
    private JvmUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Sets the Cyder JVM args.
     *
     * @param args the JVM args
     */
    public static void setJvmArgs(ImmutableList<String> args) {
        Preconditions.checkNotNull(args);
        Preconditions.checkState(jvmArgs == null);

        jvmArgs = ImmutableList.copyOf(args);
    }

    /**
     * Returns the Cyder JVM args.
     *
     * @return the Cyder JVM args
     */
    public static ImmutableList<String> getJvmArgs() {
        Preconditions.checkNotNull(jvmArgs);

        return jvmArgs;
    }

    /**
     * Returns whether the current jvm session is in debug mode meaning
     * threads could be externally suspended.
     *
     * @return whether the current jvm session is in debug mode meaning
     * threads could be externally suspended
     */
    public static boolean currentInstanceLaunchedWithDebug() {
        return JVM_LAUNCHED_IN_DEBUG_MODE;
    }

    /**
     * Returns the total number of classes that have been loaded
     * since the Java virtual machine has started execution.
     *
     * @return the total number of classes that have been loaded
     * since the Java virtual machine has started execution
     */
    public static long getCurrentTotalLoadedClassCount() {
        return ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount();
    }

    /**
     * Returns a file reference to the Cyder jar file if the current session was launched from a jar file.
     *
     * @return the file reference to the jar file
     * @throws FatalException if the located file is not a JAR or a file or
     *                        if Cyder's protected domain code source location cannot be converted to a URI
     */
    public static File getCyderJarReference() {
        Preconditions.checkState(OsUtil.JAR_MODE);

        File jarFile;
        try {
            jarFile = new File(Cyder.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getAbsoluteFile();
        } catch (Exception e) {
            throw new FatalException(e);
        }

        if (!jarFile.isFile()) {
            throw new FatalException("Jar file could not be found, path returned: "
                    + jarFile.getAbsolutePath());
        }

        return jarFile;
    }

    /**
     * Returns a file reference to the java home of the JVM which is running the current instance of Cyder.
     *
     * @return a file reference to the java home of the JVM which is running the current instance of Cyder
     * @throws FatalException if the found file does not exist or is not a directory
     */
    public static File getCurrentJavaHome() {
        String javaHomePath = SystemPropertyKey.JAVA_HOME.getProperty();
        File javaHome = new File(javaHomePath);

        if (!javaHome.exists()) {
            throw new FatalException("Found java home does not exist: " + javaHome.getAbsolutePath());
        }
        if (!javaHome.isDirectory()) {
            throw new FatalException("Found java home is not a directory: " + javaHome.getAbsolutePath());
        }

        return javaHome;
    }

    /**
     * Returns a file reference to the java home bin of the JVM which is running the current instance of Cyder.
     *
     * @return a file reference to the java home bin of the JVM which is running the current instance of Cyder
     * @throws FatalException if the found file does not exist or is not a directory
     */
    public static File getCurrentJavaBin() {
        File bin = new File(getCurrentJavaHome(), BIN);

        if (!bin.exists()) {
            throw new FatalException("Found bin does not exist: " + bin.getAbsolutePath());
        }
        if (!bin.isDirectory()) {
            throw new FatalException("Found bin is not a directory: " + bin.getAbsolutePath());
        }

        return bin;
    }

    /**
     * Returns a file reference to the java home javaw.exe of the JVM which is running the current instance of Cyder.
     *
     * @return a file reference to the java home javaw.exe of the JVM which is running the current instance of Cyder
     * @throws FatalException if the found file does not exist or is not a file
     */
    public static File getCurrentJavaWExe() {
        File javaw = new File(getCurrentJavaBin(), JAVAW);

        if (!javaw.exists()) {
            throw new FatalException("Found javaw file does not exist: " + javaw.getAbsolutePath());
        }
        if (!javaw.isFile()) {
            throw new FatalException("Found javaw file is not a file: " + javaw.getAbsolutePath());
        }

        return javaw;
    }

    /**
     * Returns a file reference to the java home java.exe of the JVM which is running the current instance of Cyder.
     *
     * @return a file reference to the java home java.exe of the JVM which is running the current instance of Cyder
     * @throws FatalException if the found file does not exist or is not a file
     */
    public static File getCurrentJavaExe() {
        File java = new File(getCurrentJavaBin(), JAVA);

        if (!java.exists()) {
            throw new FatalException("Found java file does not exist: " + java.getAbsolutePath());
        }
        if (!java.isFile()) {
            throw new FatalException("Found java file is not a file: " + java.getAbsolutePath());
        }

        return java;
    }

    /**
     * Returns an {@link ImmutableList} of the {@link ManagementFactory}'s runtime MX bean input
     * arguments to the JVM not including the arguments passed to the main method of Cyder.
     *
     * @return an immutable list of JVM arguments not including the arguments passed to the main method
     */
    public static ImmutableList<String> getNonMainInputArguments() {
        return ImmutableList.copyOf(ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    /**
     * Returns the Java class path that is used by the system class loader to search for class files
     *
     * @return the Java class path
     */
    public static String getClassPath() {
        return SystemPropertyKey.JAVA_CLASS_PATH.getProperty();
    }

    /**
     * Returns the full command used to invoke the current JVM instance.
     * This includes java.exe path, input arguments from the runtime MX bean,
     * the class path, and the main method arguments.
     *
     * @return the full command used to invoke the current JVM instance.
     */
    public static String getFullJvmInvocationCommand() {
        StringBuilder inputArgumentsBuilder = new StringBuilder();
        getNonMainInputArguments().forEach(arg -> inputArgumentsBuilder
                .append(quote)
                .append(arg)
                .append(quote)
                .append(CyderStrings.space));
        String safeInputArguments = inputArgumentsBuilder.toString().trim();

        StringBuilder mainMethodArgumentsBuilder = new StringBuilder();
        getJvmArgs().forEach(arg -> mainMethodArgumentsBuilder
                .append(quote)
                .append(arg)
                .append(quote)
                .append(CyderStrings.space));
        String mainMethodArgs = mainMethodArgumentsBuilder.toString().trim();

        // todo string util escape quotes method?
        String executablePath = getCurrentJavaExe().getAbsolutePath().replaceAll("\"", "\\\"");
        String classpath = getClassPath().replaceAll("\"", "\\\"");
        String sunJavaCommand = SystemPropertyKey.SUN_JAVA_COMMAND.getProperty().replaceAll("\"", "\\\"");

        return quote + executablePath + quote + CyderStrings.space
                + safeInputArguments + CyderStrings.space
                + CLASSPATH_ARGUMENT + CyderStrings.space
                + quote + classpath + quote + CyderStrings.space
                + quote + sunJavaCommand + quote + CyderStrings.space
                + mainMethodArgs;
    }
}
