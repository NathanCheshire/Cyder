package cyder.handlers.input;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import cyder.annotations.GuiTest;
import cyder.annotations.Handle;
import cyder.annotations.Widget;
import cyder.enumerations.Dynamic;
import cyder.enumerations.Extension;
import cyder.enumerations.SystemPropertyKey;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.utils.OsUtil;
import cyder.utils.ReflectionUtil;
import cyder.utils.StatUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.Future;

/**
 * A handler for finding and printing statistics.
 */
public class StatHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private StatHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"debug", "count logs", "computer properties", "system properties", "tests",
            "network addresses", "filesizes", "badwords", "widgets", "analyze code", "java properties",
            "threads", "daemon threads"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("debug")) {
            CyderThreadRunner.submit(() -> {
                try {
                    getInputHandler().println("Querying computer memory...");
                    ImmutableList<String> memory = StatUtil.getComputerMemorySpaces();
                    getInputHandler().println("Computer memory:");
                    for (String prop : memory) {
                        getInputHandler().println(prop);
                    }

                    getInputHandler().println("Java properties:");
                    Arrays.stream(SystemPropertyKey.values()).forEach(propertyKey
                            -> getInputHandler().println(propertyKey.getProperty()));

                    getInputHandler().println("System properties:");
                    for (String prop : StatUtil.getSystemProperties()) {
                        getInputHandler().println(prop);
                    }

                    Future<StatUtil.DebugStats> futureStats = StatUtil.getDebugProps();
                    while (!futureStats.isDone()) Thread.onSpinWait();
                    StatUtil.DebugStats stats = futureStats.get();

                    getInputHandler().println("Debug stats:");
                    for (String line : stats.lines()) {
                        getInputHandler().println(line);
                    }

                    getInputHandler().println("Country flag:");
                    getInputHandler().println(stats.countryFlag());
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Debug Stat Finder");
        } else if (getInputHandler().inputIgnoringSpacesMatches("computerproperties")) {
            getInputHandler().println("This may take a second since this feature counts your PC's free memory");

            CyderThreadRunner.submit(() -> {
                for (String prop : StatUtil.getComputerMemorySpaces()) {
                    getInputHandler().println(prop);
                }
            }, "Computer Memory Computer");
        } else if (getInputHandler().inputIgnoringSpacesMatches("systemproperties")) {
            for (String prop : StatUtil.getSystemProperties()) {
                getInputHandler().println(prop);
            }
        } else if (getInputHandler().commandIs("countlogs")) {
            File[] logDirs = Dynamic.buildDynamic(Dynamic.LOGS.getFileName()).listFiles();
            int count = 0;
            int days = 0;

            if (logDirs != null && logDirs.length > 0) {
                for (File logDir : logDirs) {
                    days++;

                    File[] logDirFiles = logDir.listFiles();

                    if (logDirFiles != null && logDirFiles.length > 0) {
                        for (File log : logDirFiles) {
                            if (FileUtil.getExtension(log).equals(Extension.LOG.getExtension())
                                    && !logDir.equals(Logger.getCurrentLogFile())) {
                                count++;
                            }
                        }
                    }
                }
            }

            getInputHandler().println("Number of log dirs: " + days);
            getInputHandler().println("Number of logs: " + count);
        } else if (getInputHandler().commandIs("tests")) {
            getInputHandler().println("Valid GUI tests to call:");
            getInputHandler().printlns(getGuiTestTriggers());
        } else if (getInputHandler().inputIgnoringSpacesMatches("networkaddresses")) {
            try {
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

                for (NetworkInterface netInterface : Collections.list(nets)) {
                    getInputHandler().println("Display name: " + netInterface.getDisplayName());
                    getInputHandler().println("Name: " + netInterface.getName());

                    Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
                    for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                        getInputHandler().println("InetAddress: " + inetAddress);
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        } else if (getInputHandler().inputIgnoringSpacesMatches("filesizes")) {
            for (StatUtil.FileSize fileSize : StatUtil.fileSizes()) {
                getInputHandler().println(fileSize.name() + ": " + OsUtil.formatBytes(fileSize.size()));
            }
        } else if (getInputHandler().commandIs("widgets")) {
            ArrayList<WidgetDescription> descriptions = getWidgetDescriptions();

            getInputHandler().println("Found " + descriptions.size() + " widgets:");
            getInputHandler().println("-------------------------------------");

            for (WidgetDescription description : descriptions) {
                StringBuilder triggers = new StringBuilder();

                for (int i = 0 ; i < description.triggers().length ; i++) {
                    triggers.append(description.triggers()[i]);

                    if (i != description.triggers().length - 1)
                        triggers.append(", ");
                }

                getInputHandler().println("Name: " + description.name());
                getInputHandler().println("Description: " + description.description() + "\nTriggers: ["
                        + triggers.toString().trim() + CyderStrings.closingBracket);
                getInputHandler().println("-------------------------------------");
            }
        } else if (getInputHandler().inputIgnoringSpacesMatches("analyzecode")) {
            if (OsUtil.JAR_MODE) {
                getInputHandler().println("Code analyzing is not available when in Jar mode");
            } else {
                if (getInputHandler().checkArgsLength(0)
                        || getInputHandler().checkArgsLength(1)) {
                    File startDir = new File("src");

                    if (getInputHandler().checkArgsLength(1)) {
                        startDir = new File(getInputHandler().getArg(0));

                        if (!startDir.exists()) {
                            getInputHandler().println("Invalid root directory");
                            startDir = new File("src/main/java/cyder");
                        }
                    }

                    File finalStartDir = startDir;

                    CyderThreadRunner.submit(() -> {
                        int codeLines = StatUtil.totalJavaLines(finalStartDir);
                        int commentLines = StatUtil.totalComments(finalStartDir);

                        getInputHandler().println("Total lines: " + StatUtil.totalLines(finalStartDir));
                        getInputHandler().println("Code lines: " + codeLines);
                        getInputHandler().println("Blank lines: " + StatUtil.totalBlankLines(finalStartDir));
                        getInputHandler().println("Comment lines: " + commentLines);
                        getInputHandler().println("Classes: " + ReflectionUtil.getCyderClasses().size());

                        float ratio = ((float) codeLines / (float) commentLines);
                        getInputHandler().println("Code to comment ratio: "
                                + new DecimalFormat("#0.00").format(ratio));
                    }, "Code Analyzer");
                } else {
                    getInputHandler().println("analyzecode usage: analyzecode [path/to/the/root/directory] " +
                            "(leave path blank to analyze Cyder)");
                }
            }
        } else if (getInputHandler().inputIgnoringSpacesMatches("javaproperties")) {
            Arrays.stream(SystemPropertyKey.values()).forEach(propertyKey
                    -> getInputHandler().println(propertyKey.getProperty()));
        } else if (getInputHandler().commandIs("threads")) {
            getInputHandler().printlns(ThreadUtil.getThreadNames());
        } else if (getInputHandler().inputIgnoringSpacesMatches("daemonthreads")) {
            getInputHandler().printlns(ThreadUtil.getDaemonThreadNames());
        } else {
            ret = false;
        }

        return ret;
    }

    /**
     * A widget and its properties.
     */
    private record WidgetDescription(String name, String description, String[] triggers) {}

    /**
     * Returns a list of names, descriptions, and triggers of all the widgets found within Cyder.
     *
     * @return a list of descriptions of all the widgets found within Cyder
     */
    private static ArrayList<WidgetDescription> getWidgetDescriptions() {
        ArrayList<WidgetDescription> ret = new ArrayList<>();

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(Widget.class)) {
                    String[] triggers = method.getAnnotation(Widget.class).triggers();
                    String description = method.getAnnotation(Widget.class).description();
                    ret.add(new WidgetDescription(clazz.getName(), description, triggers));
                }
            }
        }

        return ret;
    }

    /**
     * Returns a list of valid gui triggers exposed in Cyder.
     *
     * @return a list of triggers for gui tests
     */
    private static ImmutableList<String> getGuiTestTriggers() {
        ArrayList<String> ret = new ArrayList<>();

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(GuiTest.class)) {
                    String trigger = m.getAnnotation(GuiTest.class).value();
                    ret.add(trigger);
                }
            }
        }

        return ImmutableList.copyOf(ret);
    }
}
