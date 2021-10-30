package cyder.utilities;

import cyder.handler.ErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;

public class SystemUtil {

    private SystemUtil () {}

    private static ImageIcon cyderIcon = new ImageIcon("static/pictures/CyderIcon.png");
    private static ImageIcon cyderIconBlink = new ImageIcon("static/pictures/CyderIconBlink.png");
    private ImageIcon scaledCyderIcon = new ImageIcon(new ImageIcon("static/pictures/CyderIcon.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));
    private ImageIcon scaledCyderIconBlink = new ImageIcon(new ImageIcon("static/pictures/CyderIconBlink.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));

    public static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public static int getScreenWidth() {
        return getScreenSize().width;
    }

    public static int getScreenHeight() {
        return getScreenSize().height;
    }

    public static ImageIcon getCyderIcon() {
        return cyderIcon;
    }

    public static ImageIcon getCyderIconBlink() {
        return cyderIconBlink;
    }

    private static ImageIcon currentCyderIcon = cyderIcon;

    public static ImageIcon getCurrentCyderIcon() {
        return currentCyderIcon;
    }

    public static void setCurrentCyderIcon(ImageIcon currentCyderIcon) {
        SystemUtil.currentCyderIcon = currentCyderIcon;
    }

    public ImageIcon getScaledCyderIcon() {return this.scaledCyderIcon;}

    public ImageIcon getScaledCyderIconBlink() {return this.scaledCyderIconBlink;}

    public static String getWindowsUsername() {
        return System.getProperty("user.name");
    }

    /**
     * @param property the key of the property you want. Ex: user.name
     * @return returns the system property for the passed requested property if it is valid
     */

    public static String getSystemProperty(String property) {
        return System.getProperty(property);
    }

    public static String getOS() {
        return System.getProperty("os.name");
    }

    public static String getComputerName() {
        String name = null;

        try {
            InetAddress Add = InetAddress.getLocalHost();
            name = Add.getHostName();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
        return name;
    }

    public static void resetMouse() {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int centerX = screenSize.width / 2;
            int centerY = screenSize.height / 2;
            Robot Rob = new Robot();
            Rob.mouseMove(centerX, centerY);
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    public static void runVBS(String[] vbsScript) {
        IOUtil.createAndOpenTmpFile(SecurityUtil.generateUUID(),".vbs",vbsScript);
    }

    public static void closeCD(String drive) {
        runVBS(new String[]{"Set wmp = CreateObject(\"WMPlayer.OCX\")",
                "Set cd = wmp.cdromCollection.getByDriveSpecifier(\""
                + drive + "\")",
                "cd.Eject",
                "cd.Eject"});
    }

    public static void openCD(String drive) {
        runVBS(new String[]{"Set wmp = CreateObject(\"WMPlayer.OCX\")",
                "Set cd = wmp.cdromCollection.getByDriveSpecifier(\""
                + drive + "\")",
                "cd.Eject"});
    }

    private static boolean threadsKilled = false;

    public static void killThreads() {
        threadsKilled = true;
    }

    //I think this is made for my keybaord? Logictech gpro? see if you can figure out the keyboard and drivers?
    public static void disco(int iterations) {
        Thread DiscoThread = new Thread(() -> {
            try {
                Robot Rob = new Robot();
                boolean Fixed = false;
                boolean CapsOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
                boolean ScrollOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_SCROLL_LOCK);

                for (int i = 1; i < iterations ; i++) {
                    if (threadsKilled)
                        return;

                    if (!Fixed) {
                        Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_CAPS_LOCK, false);
                        Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_SCROLL_LOCK, false);

                        Fixed = true;
                    }

                    Rob.keyPress(KeyEvent.VK_CAPS_LOCK);
                    Rob.keyRelease(KeyEvent.VK_CAPS_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_SCROLL_LOCK);
                    Rob.keyRelease(KeyEvent.VK_SCROLL_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_CAPS_LOCK);
                    Rob.keyRelease(KeyEvent.VK_CAPS_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_SCROLL_LOCK);
                    Rob.keyRelease(KeyEvent.VK_SCROLL_LOCK);

                    Thread.sleep(170);
                }

                Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_CAPS_LOCK, CapsOn);
                Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_SCROLL_LOCK, ScrollOn);

                threadsKilled = false;
            }

            catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        },"keyboard disco thread");

        DiscoThread.start();
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                }

                else {
                    f.delete();
                }
            }
        }

        folder.delete();
    }

    /**
     * Returns a list of all files contained within the startDir and sub directories
     * that have the specified extension
     * @param startDir the starting directory
     * @param extension the specified extension. Ex. ".java"
     * @return an ArrayList of all files with the given extension found within the startDir and
     * sub directories
     */
    public static ArrayList<File> getFiles(File startDir, String extension) {
        //init return set
        ArrayList<File> ret = new ArrayList<>();

        //should be directory but test anyway
        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret.addAll(getFiles(f, extension));

            //if it's a file with the proper extension, return it
        } else if (startDir.getName().endsWith(extension)) {
            ret.add(startDir);
        }

        return ret;
    }

    /**
     * Checks the OS and if it is an OSX build, notifies why we are going to exit
     * and exits the program. Cyder is built for Windows 10 (as it plainly says),
     * and also for linux distros. OS X causes Cyder to result in unintended behavior.
     */
    public static boolean osxSystem() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("mac os x");
    }

    /**
     * Confirms whether or not the command is valid. e.g. pass "python" or "ffmpeg" and if the returned value is false,
     * ffmpeg/python are not installed.
     * @param command the command to run in the terminal
     * @return whether or not the command is valid
     */
    public static boolean shellCommandValid(String command) {
        boolean ret = true;

        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);
        } catch (Exception e) {
            ret = false;
            ErrorHandler.silentHandle(e);
        } finally {
            return ret;
        }
    }
}
