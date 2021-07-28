package cyder.utilities;

import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
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
    public static void javaProperties() {
        ArrayList<String> PropertiesList = new ArrayList<>();
        Properties Props = System.getProperties();

        Enumeration<?> keys = Props.keys();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) Props.get(key);
            PropertiesList.add(key + ": " + value);
        }

        String[] lines = new String[PropertiesList.size()];

        for (int i =  0 ; i < PropertiesList.size() ; i++) {
            lines[i] = PropertiesList.get(i);
        }

        IOUtil.createAndOpenTmpFile("JavaProperties",".txt", lines);
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

        String[] lines = new String[arrayLines.size()];

        for (int i = 0 ; i < arrayLines.size() ; i++)
            lines[i] = arrayLines.get(i);

        IOUtil.createAndOpenTmpFile("SystemProperties",".txt",lines);
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

        String[] lines = new String[arrayLines.size()];

        for (int i = 0 ; i < arrayLines.size() ; i++) {
            lines[i] = arrayLines.get(i);
        }

        IOUtil.createAndOpenTmpFile("Computer Properties",".txt", lines);
    }

    public static void allStats() {
        debugMenu();
        computerProperties();
        javaProperties();
        systemProperties();
    }

    public static void debugMenu() {
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

            IOUtil.createAndOpenTmpFile("DebugProperties",".txt",lines);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static String fileByFileAnalyze(File startDir) {
        String ret = "Numbers in order represent: code lines, comment lines, and blank lines respectively\n";

        ArrayList<File> javaFiles = SystemUtil.getFiles(startDir, ".java");

        for (File f : javaFiles) {
            ret += f.getName().replace(".java","")+ ": " + totalJavaLines(f) + ","
                    + totalComments(f) + "," + totalBlankLines(f) + "\n";
        }

        return ret;
    }

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
                    if (line.trim().length() > 0)
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
                    if (line.trim().startsWith("/*")) {
                        blockComment = true;
                    } else if (line.trim().endsWith("*/")) {
                        blockComment = false;
                    }

                    if (blockComment)
                        localRet++;
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

    private static boolean isComment(String line) {
        return line.trim().startsWith("//") ||
                line.trim().startsWith("/*") ||
                line.trim().startsWith("*") ||
                line.trim().endsWith("*/") || line.matches("//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/");
    }

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

                while ((line = lineReader.readLine()) != null)
                    if (line.trim().toLowerCase().startsWith("//todo"))
                        localRet++;

                return localRet;
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }

        return ret;
    }
}