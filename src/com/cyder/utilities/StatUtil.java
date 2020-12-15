package com.cyder.utilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
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

    public static void debugMenu(JTextPane outputArea) {
        try {
            DecimalFormat gFormater = new DecimalFormat("##.###");
            double gBytes = Double.parseDouble(gFormater.format((((double) Runtime.getRuntime().freeMemory()) / 1024 / 1024 / 1024)));
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface netIn = NetworkInterface.getByInetAddress(address);

            IPUtil ipu = new IPUtil();

            BufferedImage flag = ImageIO.read(new URL(new IPUtil().getUserFlagURL()));

            double x = flag.getWidth();
            double y = flag.getHeight();

            outputArea.insertIcon(new ImageIcon(new GeneralUtil().resizeImage(flag, 1, (int) (2 * x), (int) (2 * y))));

            NetworkUtil nu = new NetworkUtil();
            SystemUtil su = new SystemUtil();

            String[] lines = {"Time requested: " + TimeUtil.weatherTime(),
                    "ISP: " + ipu.getIsp(),
                    "IP: " + ipu.getUserIP(),
                    "Postal Code: " + ipu.getUserPostalCode(),
                    "City: " + ipu.getUserCity(),
                    "State: " + ipu.getUserState(),
                    "Country: " + ipu.getUserCountry() + " (" + ipu.getUserCountryAbr() + ")",
                    "Latitude: " + ipu.getLat() + " Degrees N",
                    "Longitude: " + ipu.getLon() + " Degrees W",
                    "latency: " + nu.latency(10000) + " ms",
                    "Google Reachable: " + nu.siteReachable("https://www.google.com"),
                    "YouTube Reachable: " + nu.siteReachable("https://www.youtube.com"),
                    "Apple Reachable: " + nu.siteReachable("https://www.apple.com"),
                    "Microsoft Reachable: " + nu.siteReachable("https://www.microsoft.com//en-us//"),
                    "User Name: " + su.getWindowsUsername(),
                    "Computer Name: " + su.getComputerName(),
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
            new GeneralUtil().handle(e);
        }
    }
}
