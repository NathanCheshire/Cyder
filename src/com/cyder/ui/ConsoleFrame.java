package com.cyder.ui;

import com.cyder.enums.ConsoleDirection;
import com.cyder.enums.Direction;
import com.cyder.handler.ErrorHandler;
import com.cyder.utilities.ColorUtil;
import com.cyder.utilities.IOUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;

public class ConsoleFrame extends CyderFrame {

    public ConsoleFrame(int width, int height) {
        super(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static String UUID;

    public static void setUUID(String uuid) {
        UUID = uuid;
    }

    public static String getUUID() {
        return UUID;
    }

    public static String getUsername() {
        return IOUtil.getUserData("Name");
    }

    public static Font getUserFont() {
        //todo be able to scale font size
        return new Font(IOUtil.getUserData("Font"),Font.BOLD, 30);
    }

    public static Color getUserColor() {
        return ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground"));
    }

    public static Color getUserBackgroundColor() {
        return ColorUtil.hextorgbColor(IOUtil.getUserData("Background"));
    }

    public static void resizeBackgrounds() {

    }

    public static void initBackgrounds() {
        try {
            File dir = new File("src/users/" + getUUID() + "/Backgrounds");
            FilenameFilter PNGFilter = (dir1, filename) -> filename.endsWith(".png");

            backgroundFiles = new LinkedList<>(Arrays.asList(dir.listFiles(PNGFilter)));

            if (backgroundFiles.size() == 0)
                backgroundFiles = new LinkedList<>();
                backgroundFiles.add(new File("src/com/cyder/sys/pictures/Bobby.png"));
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    private static LinkedList<File> backgroundFiles;

    public static File[] getBackgrounds() {
        return null;
    }

    private static File backgroundFile;
    private static int backgroundIndex;

    public static File getCurrentBackgroundFile() {
        backgroundFile = backgroundFiles.get(backgroundIndex);

        return backgroundFile;
    }

    private static ImageIcon backgroundImageIcon;

    public static ImageIcon getCurrentBackgroundImageIcon() {
        try {
            File f = getCurrentBackgroundFile();

            backgroundImageIcon = new ImageIcon(ImageIO.read(f));
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return backgroundImageIcon;
    }

    private static Direction lastSlideDirection = Direction.LEFT;

    public static void switchBackground() {
        if (!(backgroundFiles.size() > backgroundIndex + 1 && backgroundFiles.size() > 1))
            return;

        switch (lastSlideDirection) {
            case LEFT:



                break;

            case RIGHT:



                break;
        }
    }

    public static int getBackgroundWidth() {
        return getCurrentBackgroundImageIcon().getIconWidth();
    }

    public static int getBackgroundHeight() {
        return getCurrentBackgroundImageIcon().getIconHeight();
    }

    private static boolean consoleClockEnabled;

    public static void setConsoleClock(Boolean enable) {
        consoleClockEnabled = enable;

        if (enable) {
            //set console clock visible
            //start executor to update console clcok
        }
    }

    public static boolean isConsoleClockEnabled() {
        return consoleClockEnabled;
    }

    private static ConsoleDirection consoleDir = ConsoleDirection.UP;

    public static void setConsoleDirection(ConsoleDirection conDir) {
        consoleDir = conDir;
    }

    public static ConsoleDirection getConsoleDirection() {
        return consoleDir;
    }

    public static void rotateConsole(ConsoleDirection dir) {

    }

    private static boolean fullscreen = false;

    public static void setFullscreen(Boolean enable) {

    }

    public static boolean isFullscreen() {
        return fullscreen;
    }

    public static void barrelRoll() {

    }
}
