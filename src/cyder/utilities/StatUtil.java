package cyder.utilities;

import cyder.consts.CyderRegexPatterns;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.ConsoleFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

public class StatUtil {
    private StatUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static void javaProperties() {
        ArrayList<String> PropertiesList = new ArrayList<>();
        Properties Props = System.getProperties();

        Enumeration<?> keys = Props.keys();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) Props.get(key);
            PropertiesList.add(key + ": " + value);
        }

        System.out.println("Java Properties:\n------------------------");

        for (String s : PropertiesList) {
            ConsoleFrame.getConsoleFrame().getInputHandler().println(s);
        }
    }

    public static void systemProperties() {
        ArrayList<String> arrayLines = new ArrayList<>();
        arrayLines.add("File Separator: " + System.getProperty("file.separator"));
        arrayLines.add("Class Path: " + System.getProperty("java.class.path"));
        arrayLines.add("Java Home: " + System.getProperty("java.home"));
        arrayLines.add("Java Vendor: " + System.getProperty("java.vendor"));
        arrayLines.add("Java Vendor URL: " + System.getProperty("java.vendor.url"));
        arrayLines.add("Java Version: " + System.getProperty("java.version"));
        arrayLines.add("Line Separator: " + System.getProperty("line.separator"));
        arrayLines.add("OS Architecture: " + System.getProperty("os.arch"));
        arrayLines.add("OS Name: " + System.getProperty("os.name"));
        arrayLines.add("OS Version: " + System.getProperty("os.version"));
        arrayLines.add("OS Path Separator: " + System.getProperty("path.separator"));
        arrayLines.add("User Directory: " + System.getProperty("user.dir"));
        arrayLines.add("User Home: " + System.getProperty("user.home"));
        arrayLines.add("Computer Username: " + System.getProperty("user.name"));

        for (String arrayLine : arrayLines)
            ConsoleFrame.getConsoleFrame().getInputHandler().println(arrayLine);
    }

    public static void computerProperties() {
        ArrayList<String> arrayLines = new ArrayList<>();

        arrayLines.add("Available processors (cores): " + Runtime.getRuntime().availableProcessors());
        arrayLines.add("Free memory (bytes): " + Runtime.getRuntime().freeMemory());

        long maxMemory = Runtime.getRuntime().maxMemory();

        arrayLines.add("Maximum memory (bytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
        arrayLines.add("Total memory available to JVM (bytes): " + Runtime.getRuntime().totalMemory());

        File[] roots = File.listRoots();

        for (File root : roots) {
            arrayLines.add("File system root: " + root.getAbsolutePath());
            arrayLines.add("Total space (bytes): " + root.getTotalSpace());
            arrayLines.add("Free space (bytes): " + root.getFreeSpace());
            arrayLines.add("Usable space (bytes): " + root.getUsableSpace());
        }

        for (String arrayLine : arrayLines)
            ConsoleFrame.getConsoleFrame().getInputHandler().println(arrayLine);
    }

    public static void allStats() {
        debugMenu();
        computerProperties();
        javaProperties();
        systemProperties();
    }

    public static void debugMenu() {
        new Thread(() -> {
            try {
                if (GenesisShare.isQuesitonableInternet()) {
                    throw new RuntimeException("Stable connection not established");
                }

                DecimalFormat gFormater = new DecimalFormat("##.###");
                double gBytes = Double.parseDouble(gFormater.format((((double) Runtime.getRuntime().freeMemory()) / 1024 / 1024 / 1024)));
                InetAddress address = InetAddress.getLocalHost();
                NetworkInterface netIn = NetworkInterface.getByInetAddress(address);

                BufferedImage flag = ImageIO.read(new URL(IPUtil.getIpdata().getFlag()));

                double x = flag.getWidth();
                double y = flag.getHeight();

                ConsoleFrame.getConsoleFrame().getInputHandler().println("Country: " + IPUtil.getIpdata().getCountry_name() + "\nCountry Flag: ");
                ConsoleFrame.getConsoleFrame().getInputHandler().printlnImage(new ImageIcon(ImageUtil.resizeImage(flag, 1, (int) (2 * x), (int) (2 * y))));

                String[] lines = {"Time requested: " + TimeUtil.weatherTime(),
                        "ISP: " + IPUtil.getIpdata().getAsn().getName(),
                        "IP: " + IPUtil.getIpdata().getIp(),
                        "Postal Code: " + IPUtil.getIpdata().getPostal(),
                        "City: " + IPUtil.getIpdata().getCity(),
                        "State: " + IPUtil.getIpdata().getRegion(),
                        "Country: " + IPUtil.getIpdata().getCountry_name() + " (" + IPUtil.getIpdata().getCountry_code() + ")",
                        "Latitude: " + IPUtil.getIpdata().getLatitude() + " Degrees N",
                        "Longitude: " + IPUtil.getIpdata().getLongitude() + " Degrees W",
                        "latency: " + NetworkUtil.latency(10000) + " ms",
                        "Google Reachable: " + NetworkUtil.siteReachable("https://www.google.com"),
                        "YouTube Reachable: " + NetworkUtil.siteReachable("https://www.youtube.com"),
                        "Apple Reachable: " + NetworkUtil.siteReachable("https://www.apple.com"),
                        "Microsoft Reachable: " + NetworkUtil.siteReachable("https://www.microsoft.com//en-us//"),
                        "User Name: " + SystemUtil.getWindowsUsername(),
                        "Computer Name: " + SystemUtil.getComputerName(),
                        "Available Cores: " + Runtime.getRuntime().availableProcessors(),
                        "Available Memory: " + gBytes + " GigaBytes",
                        "Operating System: " + SystemUtil.getOS(),
                        "Java Version: " + System.getProperty("java.version"),
                        "Network Interface Name: " + netIn.getName(),
                        "Network Interface Display Name: " + netIn.getDisplayName(),
                        "Network MTU: " + netIn.getMTU(),
                        "Host Address: " + address.getHostAddress(),
                        "Local Host Address: " + address.getLocalHost(),
                        "Loopback Address: " + address.getLoopbackAddress()};

                if (ConsoleFrame.getConsoleFrame().isClosed()) {
                    IOUtil.createAndOpenTmpFile("DebugProperties",".txt",lines);
                } else {
                    ConsoleFrame.getConsoleFrame().getInputHandler().printlns(lines);
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Debug Stat Thread").start();
    }

    public static String fileByFileAnalyze(File startDir) {
        String ret = "Numbers in order represent: code lines, comment lines, and blank lines respectively\n";

        ArrayList<File> javaFiles = SystemUtil.getFiles(startDir, ".java");

        for (File f : javaFiles) {
            ret += f.getName().replace(".java","")+ ": " + totalLines(f) + ","
                    + totalComments(f) + "," + totalBlankLines(f) + "\n";
        }

        return ret;
    }

    /**
     * Finds the total number of Java lines found within .java files
     * and recursive directories within the provided starting directory
     * @param startDir the directory to begin recursing from
     * @return the total number of java code lines found
     */
    public static int totalJavaLines(File startDir) {
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret += totalJavaLines(f);
        } else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line = "";
                int localRet = 0;

                while ((line = lineReader.readLine()) != null)
                    //not blank and not a comment means a code line
                    if (line.trim().length() > 0 && !isComment(line.trim()))
                        localRet++;

                return localRet;
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }

        return ret;
    }

    /**
     * Finds the total number of lines found within each java file provided the starting directory
     * to begin recursing from
     * @param startDir the directory to begin recursing from
     * @return the total number of lines found
     */
    public static int totalLines(File startDir) {
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret += totalLines(f);
        } else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line = "";
                int localRet = 0;

                while ((line = lineReader.readLine()) != null)
                    localRet++;

                return localRet;
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }

        return ret;
    }

    public static int totalJavaFiles(File startDir) {
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret += totalJavaFiles(f);
        } else if (startDir.getName().endsWith(".java")) {
            return 1;
        }

        return ret;
    }

    /**
     * Finds the number of java comments associated with all .java files
     * within the directory and recurively located directories provided
     * @param startDir the directory to begin recursing from
     * @return the raw number of comments found
     */
    public static int totalComments(File startDir) {
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret += totalComments(f);
        } else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line = "";
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
                    else if (line.trim().length() > 0 && (isComment(line)))
                        localRet++;
                }

                return localRet;
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }

        return ret;
    }

    /**
     * Determines if the provided line is a comment line
     * @param line the string in question to possibly be a comment
     * @return whether or not the line is a comment
     */
    public static boolean isComment(String line) {
        return line.matches(CyderRegexPatterns.commentPattern);
    }

    /**
     * Finds the number of blank lines associated with .java files within the provided start directory
     * @param startDir the directory to begin recursing from to find .java files
     * @return the number of blank lines found in the provided directory and subdirectories
     */
    public static int totalBlankLines(File startDir) {
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret += totalBlankLines(f);
        } else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line = "";
                int localRet = 0;

                while ((line = lineReader.readLine()) != null)
                    if (line.trim().length() == 0)
                        localRet++;

                return localRet;
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }

        return ret;
    }

    /**
     * Finds each to-do within all .java files recursing from the provided start directory
     * @param startDir the directory to begin recursing from
     * @return a String representation of all todos within the discovered directories.
     *          This String may be printed directly as it includes line breaks
     */
    public static String getTodos(File startDir) {
        StringBuilder ret = new StringBuilder();

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret.append(getTodos(f));
        } else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line;

                while ((line = lineReader.readLine()) != null)
                    if (line.trim().toLowerCase().startsWith("//todo")) {
                        ret.append(startDir.getName()).append(": ").append(line.trim().substring(6)).append("\n")
                                .append("----------------------------------------\n");
                    }
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }

        return ret.toString();
    }

    /**
     * Finds the raw number of todos listed in code by counting the number
     * of lines that are a comment with t0do inside of them.
     * @param startDir the directory to start recursing from, typically src/
     * @return the number of todos found in the provided directory and sub-directories
     */
    public static int totalTodos(File startDir) {
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret += totalTodos(f);
        } else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line = "";
                int localRet = 0;

                while ((line = lineReader.readLine()) != null) {
                    if (isComment(line.trim()) && line.trim().contains("todo"))
                        localRet++;
                }

                return localRet;
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }

        return ret;
    }
}