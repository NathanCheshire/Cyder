package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import cyder.enumerations.SystemPropertyKey;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.meta.Cyder;
import cyder.network.ScrapingUtil;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Optional;

import static cyder.strings.CyderStrings.*;

/**
 * Utilities related to the JVM.
 */
public final class JvmUtil {
    /**
     * The bin string.
     */
    private static final String BIN = "bin";

    /**
     * The thread name for the jvm args logger.
     */
    private static final String JVM_ARGS_LOGGER_THREAD_NAME = "JVM Args Logger";

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
     * The JVM args passed to the main method.
     */
    private static ImmutableList<String> jvmMainMethodArgs;

    /**
     * The arguments parsed from {@link #jvmMainMethodArgs}.
     */
    private static ImmutableMap<String, String> parsedArgs = ImmutableMap.of();

    /**
     * Suppress default constructor.
     */
    private JvmUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Sets the main method JVM args and parses them into a map.
     *
     * @param jvmMainMethodArgs the main method JVM args
     */
    public static void setAndParseJvmMainMethodArgs(ImmutableList<String> jvmMainMethodArgs) {
        Preconditions.checkNotNull(jvmMainMethodArgs);
        Preconditions.checkState(JvmUtil.jvmMainMethodArgs == null);

        JvmUtil.jvmMainMethodArgs = ImmutableList.copyOf(jvmMainMethodArgs);

        parseArgsMapFromArgs();
    }

    /**
     * Parses the {@link #parsedArgs} from the {@link #jvmMainMethodArgs}.
     */
    private static void parseArgsMapFromArgs() {
        ImmutableMap.Builder<String, String> args = new ImmutableMap.Builder<>();

        String currentArgument = null;
        for (String argument : jvmMainMethodArgs) {
            if (isArgument(argument)) {
                if (currentArgument != null) throw new FatalException("Failed to parse at argument: " + argument);
                currentArgument = removeLeadingDashes(argument);
                continue;
            }

            if (currentArgument == null) throw new FatalException("Failed to parse at argument: " + argument);
            args.put(currentArgument, argument);
            currentArgument = null;
        }

        parsedArgs = args.build();
    }

    /**
     * Returns whether the provided string is an argument as opposed to a parameter.
     *
     * @param string the string
     * @return whether the provided string is an argument as opposed to a parameter
     */
    private static boolean isArgument(String string) {
        Preconditions.checkNotNull(string);
        Preconditions.checkArgument(!string.isEmpty());

        return string.startsWith("--") || string.startsWith("-");
    }

    /**
     * Removes all leading dashes from the provided string.
     *
     * @param string the string
     * @return the string with all leading dashes removed
     */
    private static String removeLeadingDashes(String string) {
        Preconditions.checkNotNull(string);
        Preconditions.checkArgument(!string.isEmpty());

        return string.replaceAll("^-+", "");
    }

    /**
     * Returns the parameter corresponding to the provided argument if present. Empty optional else.
     *
     * @param argument the argument
     * @return the parameter corresponding to the provided argument if present. Empty optional else
     */
    public static Optional<String> getArgumentParam(String argument) {
        Preconditions.checkNotNull(argument);
        Preconditions.checkArgument(!argument.isEmpty());

        if (parsedArgs.containsKey(argument)) {
            return Optional.ofNullable(parsedArgs.get(argument));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the main method JVM args.
     *
     * @return the main method JVM args
     */
    public static ImmutableList<String> getJvmMainMethodArgs() {
        Preconditions.checkNotNull(jvmMainMethodArgs);

        return jvmMainMethodArgs;
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
            throw new FatalException("Jar file could not be found, path returned: " + jarFile.getAbsolutePath());
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
        getJvmMainMethodArgs().forEach(arg -> mainMethodArgumentsBuilder
                .append(quote)
                .append(arg)
                .append(quote)
                .append(CyderStrings.space));
        String mainMethodArgs = mainMethodArgumentsBuilder.toString().trim();

        String executablePath = StringUtil.escapeQuotes(getCurrentJavaExe().getAbsolutePath());
        String classpath = StringUtil.escapeQuotes(getClassPath());
        String sunJavaCommand = StringUtil.escapeQuotes(SystemPropertyKey.SUN_JAVA_COMMAND.getProperty());

        return quote + executablePath + quote + CyderStrings.space
                + safeInputArguments + CyderStrings.space
                + CLASSPATH_ARGUMENT + CyderStrings.space
                + quote + classpath + quote + CyderStrings.space
                + quote + sunJavaCommand + quote + CyderStrings.space
                + mainMethodArgs;
    }

    /**
     * Logs any main method command line arguments passed in to Cyder upon starting.
     * {@link ScrapingUtil#getIspAndNetworkDetails()} is queried and the details appended
     * to the resulting log statement if {@link Props#autocypher} is {@code false}.
     * This is queried in a separate thread so invoking this method on the UI thread is safe.
     *
     * @param cyderArgs the main method command line arguments passed in
     */
    public static void logMainMethodArgs(ImmutableList<String> cyderArgs) {
        Preconditions.checkNotNull(cyderArgs);

        if (Props.autocypher.getValue()) {
            Logger.log(LogTag.JVM_ARGS, "JVM main method args obfuscated due"
                    + " to AutoCypher, args length: " + cyderArgs.size());
            return;
        }

        CyderThreadRunner.submit(() -> {
            try {
                StringBuilder argBuilder = new StringBuilder(StringUtil.joinParts(cyderArgs, comma));

                ScrapingUtil.IspQueryResult result = ScrapingUtil.getIspAndNetworkDetails();

                argBuilder.append("city")
                        .append(colon).append(space).append(result.city())
                        .append(comma).append(space).append("state")
                        .append(colon).append(space).append(result.state())
                        .append(comma).append(space).append("country")
                        .append(colon).append(space).append(result.country())
                        .append(comma).append(space).append("ip")
                        .append(colon).append(space).append(result.ip())
                        .append(comma).append(space).append("isp")
                        .append(colon).append(space).append(result.isp())
                        .append(comma).append(space).append("hostname")
                        .append(colon).append(space).append(result.hostname());

                Logger.log(LogTag.JVM_ARGS, argBuilder);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, JVM_ARGS_LOGGER_THREAD_NAME);
    }

    /**
     * Calculates the run time of the JVM running Cyder.
     *
     * @return the run time of Cyder starting from JVM entry
     */
    public static long getRuntime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }
}
