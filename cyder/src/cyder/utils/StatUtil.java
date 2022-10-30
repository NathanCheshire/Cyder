package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.enums.Extension;
import cyder.enums.SystemPropertyKey;
import cyder.exceptions.IllegalMethodException;
import cyder.file.FileUtil;
import cyder.genesis.ProgramMode;
import cyder.genesis.ProgramModeManager;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.IpUtil;
import cyder.network.NetworkUtil;
import cyder.threads.CyderThreadFactory;
import cyder.time.TimeUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utilities for general statistics related to Cyder.
 */
public final class StatUtil {
    /**
     * Suppress default constructor.
     */
    private StatUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns an immutable list detailing the java system properties of the current JVM.
     *
     * @return an immutable list detailing the java system properties of the current JVM
     */
    public static ImmutableList<String> getSystemProperties() {
        LinkedList<String> ret = new LinkedList<>();

        Arrays.stream(SystemPropertyKey.values()).forEach(systemPropertyKey ->
                ret.add(systemPropertyKey.getDescription() + ", key: " + systemPropertyKey.getKey()
                        + ", value: " + systemPropertyKey.getProperty()));

        return ImmutableList.copyOf(ret);
    }

    /**
     * If I hit it one time ima pipe her.
     */
    private static final String NO_LIMIT = "no limit";

    /**
     * Returns an immutable list detailing the found computer memory spaces.
     * <p>
     * Note: invocation of this method should be done in a separate thread
     * since computation of free memory may take some time.
     *
     * @return an immutable list detailing the found computer properties
     */
    public static ImmutableList<String> getComputerMemorySpaces() {
        LinkedList<String> ret = new LinkedList<>();

        ret.add("Available processors (cores): " + Runtime.getRuntime().availableProcessors());
        ret.add("Free memory: " + OsUtil.formatBytes(Runtime.getRuntime().freeMemory()));

        long maxMemory = Runtime.getRuntime().maxMemory();

        ret.add("Maximum memory: " +
                (maxMemory == Long.MAX_VALUE ? NO_LIMIT : OsUtil.formatBytes(maxMemory)));
        ret.add("Total memory available to JVM: " + OsUtil.formatBytes(Runtime.getRuntime().totalMemory()));

        Arrays.stream(File.listRoots()).forEach(root -> {
            ret.add("File system root: " + root.getAbsolutePath());
            ret.add("Total space (root): " + OsUtil.formatBytes(root.getTotalSpace()));
            ret.add("Free space (root): " + OsUtil.formatBytes(root.getFreeSpace()));
            ret.add("Usable space (root): " + OsUtil.formatBytes(root.getUsableSpace()));
        });

        return ImmutableList.copyOf(ret);
    }

    /**
     * A record type to hold the stats returned by {@link StatUtil#getDebugProps()}.
     */
    public record DebugStats(ImmutableList<String> lines, ImageIcon countryFlag) {}

    /**
     * The name of the executor service which gets the debug props.
     */
    private static final String DEBUG_PROPS_EXECUTOR_THREAD_NAME = "Debug Props Getter";

    /**
     * Returns a debug object containing the found user flag and some common debug details.
     *
     * @return a debug object containing the found user flag and some common debug details
     */
    public static Future<DebugStats> getDebugProps() {
        Preconditions.checkArgument(!NetworkUtil.isHighLatency());

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(DEBUG_PROPS_EXECUTOR_THREAD_NAME)).submit(() -> {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface netIn = NetworkInterface.getByInetAddress(address);

            BufferedImage flag = ImageIO.read(new URL(IpUtil.getIpData().getFlag()));

            int x = 2 * flag.getWidth();
            int y = 2 * flag.getHeight();
            int type = flag.getType();

            ImageIcon resized = new ImageIcon(ImageUtil.resizeImage(flag, type, x, y));

            return new DebugStats(
                    ImmutableList.of(
                            "Time requested: " + TimeUtil.weatherTime(),
                            "ISP: " + IpUtil.getIpData().getAsn().getName(),
                            "IP: " + IpUtil.getIpData().getIp(),
                            "Postal Code: " + IpUtil.getIpData().getPostal(),
                            "City: " + IpUtil.getIpData().getCity(),
                            "State: " + IpUtil.getIpData().getRegion(),
                            "Country: " + IpUtil.getIpData().getCountry_name()
                                    + " (" + IpUtil.getIpData().getCountry_code() + CyderStrings.closingParenthesis,
                            "Latitude: " + IpUtil.getIpData().getLatitude() + " Degrees N",
                            "Longitude: " + IpUtil.getIpData().getLongitude() + " Degrees W",
                            "latency: " + NetworkUtil.latency(10000) + " ms",
                            "Google Reachable: " + NetworkUtil.siteReachable(CyderUrls.GOOGLE),
                            "YouTube Reachable: " + NetworkUtil.siteReachable(CyderUrls.YOUTUBE),
                            "Apple Reachable: " + NetworkUtil.siteReachable(CyderUrls.APPLE),
                            "Microsoft Reachable: " + NetworkUtil.siteReachable(CyderUrls.MICROSOFT),
                            "User Name: " + OsUtil.getOsUsername(),
                            "Computer Name: " + OsUtil.getComputerName(),
                            "Available Cores: " + Runtime.getRuntime().availableProcessors(),
                            "Available Memory: " + OsUtil.formatBytes(Runtime.getRuntime().freeMemory()),
                            "Operating System: " + OsUtil.OPERATING_SYSTEM_NAME,
                            "Java Version: " + SystemPropertyKey.JAVA_VERSION.getProperty(),
                            "Network Interface Name: " + netIn.getName(),
                            "Network Interface Display Name: " + netIn.getDisplayName(),
                            "Network MTU: " + netIn.getMTU(),
                            "Host Address: " + address.getHostAddress(),
                            "Local Host Address: " + InetAddress.getLocalHost(),
                            "Loopback Address: " + InetAddress.getLoopbackAddress()), resized);
        });
    }

    /**
     * Returns a string representing statistics found about all .java files found from the starting directory such as
     * comment lines, total lines, and blank lines.
     *
     * @param startDir the directory to start from
     * @return a string representing statistics found about all .java files found from the starting directory such as
     * * comment lines, total lines, and blank lines
     */
    public static String fileByFileAnalyze(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        StringBuilder ret = new StringBuilder("Numbers in order represent: "
                + "code lines, comment lines, and blank lines respectively" + CyderStrings.newline);

        FileUtil.getFiles(startDir, Extension.JAVA.getExtension()).forEach(javaFile ->
                ret.append(javaFile.getName().replace(Extension.JAVA.getExtension(), ""))
                        .append(": ").append(totalLines(javaFile)).append(CyderStrings.comma)
                        .append(totalComments(javaFile)).append(CyderStrings.comma)
                        .append(totalBlankLines(javaFile)).append(CyderStrings.newline));

        return ret.toString();
    }

    /**
     * Finds the total number of Java lines found within .java files
     * and recursive directories within the provided starting directory
     *
     * @param startDir the directory to begin recursing from
     * @return the total number of java code lines found
     */
    public static int totalJavaLines(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        AtomicInteger ret = new AtomicInteger();

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                Arrays.stream(files).forEach(file -> ret.addAndGet(totalJavaLines(file)));
            }
        } else if (startDir.getName().endsWith(Extension.JAVA.getExtension())) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;
                int localRet = 0;

                while ((line = lineReader.readLine()) != null) {
                    if (isCodeLine(line)) {
                        localRet++;
                    }
                }

                return localRet;
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        return ret.get();
    }

    /**
     * Returns whether hte provided line is a code line meaning it is not blank or a comment line.
     *
     * @param line the line
     * @return whether the provided line is a code line
     */
    @ForReadability
    private static boolean isCodeLine(String line) {
        Preconditions.checkNotNull(line);

        return !line.trim().isEmpty() && !isComment(line.trim());
    }

    /**
     * Finds the total number of lines found within each java file provided the starting directory
     * to begin recursing from
     *
     * @param startDir the directory to begin recursing from
     * @return the total number of lines found
     */
    public static int totalLines(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret += totalLines(f);
                }
            }
        } else if (startDir.getName().endsWith(Extension.JAVA.getExtension())) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                int localRet = 0;

                while (lineReader.readLine() != null)
                    localRet++;

                return localRet;
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        return ret;
    }

    /**
     * Finds the number of java comments associated with all .java files
     * within the directory and recursively located directories provided
     *
     * @param startDir the directory to begin recursing from
     * @return the raw number of comments found
     */
    public static int totalComments(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret += totalComments(f);
                }
            }
        } else if (startDir.getName().endsWith(Extension.JAVA.getExtension())) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;
                int localRet = 0;

                boolean blockComment = false;

                while ((line = lineReader.readLine()) != null) {
                    //rare case of this happening but needed to not trigger a long block comment
                    if (line.trim().startsWith("/*") && line.trim().endsWith("*/")) {
                        localRet++;
                        continue;
                    }

                    //start of a block comment
                    if (line.trim().startsWith("/*")) {
                        blockComment = true;
                    }
                    //end of a block comment
                    else if (line.trim().endsWith("*/")) {
                        blockComment = false;
                    }

                    //if we've activated block comment or still on, increment line count
                    if (blockComment)
                        localRet++;
                        //otherwise if the line has text and is a comment inc
                    else if (!line.trim().isEmpty() && (isComment(line)))
                        localRet++;
                }

                return localRet;
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        return ret;
    }

    /**
     * Determines if the provided line is a comment line
     *
     * @param line the string in question to possibly be a comment
     * @return whether the line is a comment
     */
    public static boolean isComment(String line) {
        Preconditions.checkNotNull(line);

        return line.matches(CyderRegexPatterns.commentPattern.pattern());
    }

    /**
     * Finds the number of blank lines associated with .java files within the provided start directory
     *
     * @param startDir the directory to begin recursing from to find .java files
     * @return the number of blank lines found in the provided directory and subdirectories
     */
    public static int totalBlankLines(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    ret += totalBlankLines(f);
                }
            }
        } else if (startDir.getName().endsWith(Extension.JAVA.getExtension())) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;
                int localRet = 0;

                while ((line = lineReader.readLine()) != null)
                    if (line.trim().isEmpty())
                        localRet++;

                return localRet;
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        return ret;
    }

    /**
     * Returns an immutable list detailing all the files found
     * within the root level directory and their sizes.
     *
     * @return an immutable list detailing all the files found
     * within the root level directory and their sizes
     */
    public static ImmutableList<FileSize> fileSizes() {
        if (ProgramModeManager.INSTANCE.getProgramMode() == ProgramMode.NORMAL) {
            throw new IllegalMethodException("Method not allowed when in Jar mode");
        }

        LinkedList<FileSize> prints = innerFileSizes(OsUtil.buildFile("..", "Cyder"));
        prints.sort(new FileComparator());
        return ImmutableList.copyOf(prints);
    }

    private static LinkedList<FileSize> innerFileSizes(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        LinkedList<FileSize> ret = new LinkedList<>();

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files != null && files.length > 0) {
                Arrays.stream(files).forEach(file -> ret.addAll(innerFileSizes(file)));
            }
        } else {
            ret.add(new FileSize(startDir.getName(), startDir.length()));
        }

        return ret;
    }

    /**
     * The file comparator used for comparing files by their sizes in bytes.
     */
    private static final class FileComparator implements Comparator<FileSize> {
        public int compare(FileSize fs1, FileSize fs2) {
            if (fs1.size() < fs2.size()) {
                return 1;
            } else if (fs1.size() > fs2.size()) {
                return -1;
            }

            return 0;
        }
    }

    /**
     * A record to associate a file name with its size.
     */
    public record FileSize(String name, long size) {}
}