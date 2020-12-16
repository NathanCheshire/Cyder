package com.cyder.utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;

public class SystemUtil {

    private GeneralUtil gu;

    public SystemUtil() {
        gu = new GeneralUtil();
    }

    private ImageIcon cyderIcon = new ImageIcon("src/com/cyder/sys/pictures/CyderIcon.png");
    private ImageIcon cyderIconBlink = new ImageIcon("src/com/cyder/sys/pictures/CyderIconBlink.png");
    private ImageIcon scaledCyderIcon = new ImageIcon(new ImageIcon("src/com/cyder/sys/pictures/CyderIcon.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));
    private ImageIcon scaledCyderIconBlink = new ImageIcon(new ImageIcon("src/com/cyder/sys/pictures/CyderIconBlink.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));

    public Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public int getScreenWidth() {
        return this.getScreenSize().width;
    }

    public int getScreenHeight() {
        return this.getScreenSize().height;
    }

    public ImageIcon getCyderIcon() {
        return this.cyderIcon;
    }

    public ImageIcon getCyderIconBlink() {
        return this.cyderIconBlink;
    }

    public ImageIcon getScaledCyderIcon() {return this.scaledCyderIcon;}

    public ImageIcon getScaledCyderIconBlink() {return this.scaledCyderIconBlink;}

    public String getCyderVer() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/Title.txt"))) {
            return br.readLine();
        }

        catch (Exception e) {
            gu.handle(e);
        }

        return null;
    }

    public String getWindowsUsername() {
        return System.getProperty("user.name");
    }

    public static String getOS() {
        return System.getProperty("os.name");
    }

    //system util
    public String getComputerName() {
        String name = null;

        try {
            InetAddress Add = InetAddress.getLocalHost();
            name = Add.getHostName();
        } catch (Exception e) {
            gu.handle(e);
        }
        return name;
    }

    //system util
    public void resetMouse() {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int centerX = screenSize.width / 2;
            int centerY = screenSize.height / 2;
            Robot Rob = new Robot();
            Rob.mouseMove(centerX, centerY);
        } catch (Exception ex) {
            gu.handle(ex);
        }
    }

    //todo make methods to run vbs scripts

    public void closeCD(String drive) {
        String[] vbs = {"Set wmp = CreateObject(\"WMPlayer.OCX\")",
                "Set cd = wmp.cdromCollection.getByDriveSpecifier(\""
                        + drive + "\")",
                "cd.Eject",
                "cd.Eject"};

        IOUtil.createAndOpenTmpFile("CDROM-CLOSE",".vbs",vbs);
    }

    public void openCD(String drive) {
        String[] vbs = {"Set wmp = CreateObject(\"WMPlayer.OCX\")",
                "Set cd = wmp.cdromCollection.getByDriveSpecifier(\""
                        + drive + "\")",
                "cd.Eject"};

        IOUtil.createAndOpenTmpFile("CDROM-OPEN",".vbs",vbs);
    }

    public void disco(int iterations) {
        Thread DiscoThread = new Thread(() -> {
            try {
                boolean Fixed = false;
                boolean NumOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
                boolean CapsOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
                boolean ScrollOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_SCROLL_LOCK);

                for (int i = 1; i < iterations ; i++) {
                    Robot Rob = new Robot();

                    if (!Fixed) {
                        Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, false);
                        Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_CAPS_LOCK, false);
                        Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_SCROLL_LOCK, false);

                        Fixed = true;
                    }

                    Rob.keyPress(KeyEvent.VK_NUM_LOCK);
                    Rob.keyRelease(KeyEvent.VK_NUM_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_CAPS_LOCK);
                    Rob.keyRelease(KeyEvent.VK_CAPS_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_SCROLL_LOCK);
                    Rob.keyRelease(KeyEvent.VK_SCROLL_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_NUM_LOCK);
                    Rob.keyRelease(KeyEvent.VK_NUM_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_CAPS_LOCK);
                    Rob.keyRelease(KeyEvent.VK_CAPS_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_SCROLL_LOCK);
                    Rob.keyRelease(KeyEvent.VK_SCROLL_LOCK);

                    Thread.sleep(170);
                }

                Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, NumOn);
                Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_CAPS_LOCK, CapsOn);
                Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_SCROLL_LOCK, ScrollOn);
            }

            catch (Exception ex) {
                gu.handle(ex);
            }
        });

        DiscoThread.start();
    }

    public void deleteFolder(File folder) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File f: files) {
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
}
