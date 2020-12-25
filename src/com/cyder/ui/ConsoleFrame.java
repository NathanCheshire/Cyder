package com.cyder.ui;

import com.cyder.enums.ConsoleDirection;
import com.cyder.enums.Direction;
import com.cyder.handler.ErrorHandler;
import com.cyder.utilities.*;
import com.cyder.widgets.GenericInform;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;

public class ConsoleFrame extends CyderFrame {

    public ConsoleFrame(int width, int height) {
        super(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //todo lots more stuff here
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

    private static int fontMetric = Font.BOLD;

    public static void setFontBold() {
        fontMetric = Font.BOLD;
    }

    public static void setFontItalic() {
        fontMetric = Font.ITALIC;
    }

    public static void setFontPlain() {
        fontMetric = Font.PLAIN;
    }

    public static void setFontStyle(int combStyle) {
        //you can do bold and italics with Font.BOLD + Font.ITALIC
        // so this is what this function allows you to do

        fontMetric = combStyle;
    }

    private static int fontSize = 30;

    public static void setFontSize(int size) {
        fontSize = size;
    }

    public static Font getUserFont() {
        return new Font(IOUtil.getUserData("Font"),fontMetric, fontSize);
    }

    public static Color getUserColor() {
        return ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground"));
    }

    public static Color getUserBackgroundColor() {
        return ColorUtil.hextorgbColor(IOUtil.getUserData("Background"));
    }

    public static void resizeBackgrounds() {
        try {
            LinkedList<File> backgrounds = getBackgrounds();

            for (int i = 0; i < backgrounds.size() ; i++) {
                File currentFile = backgrounds.get(i);

                BufferedImage currentImage = ImageIO.read(currentFile);

                int backgroundWidth = currentImage.getWidth();
                int backgroundHeight = currentImage.getHeight();

                double aspectRatio = ImageUtil.getAspectRatio(currentImage);
                int imageType = currentImage.getType();

                if (backgroundWidth > new SystemUtil().getScreenWidth() || backgroundHeight > new SystemUtil().getScreenHeight())
                    GenericInform.inform("Resized the background image \"" + currentFile.getName() + "\" since it was too big " +
                            "(That's what she said ahahahahah hahaha ha ha so funny).","System Action", 700, 200);

                int screenWidth = SystemUtil.getScreenWidth();
                int screenHeight = SystemUtil.getScreenHeight();

                //resizing smaller
                while (backgroundWidth > screenWidth || backgroundHeight > screenHeight) {
                    backgroundWidth = (int) (currentImage.getWidth() / aspectRatio);
                    backgroundHeight = (int) (currentImage.getHeight() / aspectRatio);
                }

                if (backgroundWidth < 600 || backgroundHeight < 600)
                    GenericInform.inform("Resized the background image \"" + getBackgrounds().get(i).getName()
                            + "\" since it was too small.","System Action", 700, 200);

                if (aspectRatio < 1)
                    aspectRatio = 1 / aspectRatio;

                //resizing bigger
                while (backgroundWidth < 800 || backgroundHeight < 800) {
                    backgroundWidth = (int) (currentImage.getWidth() * aspectRatio);
                    backgroundHeight = (int) (currentImage.getHeight() * aspectRatio);
                }

                //prime checker
                if (NumberUtil.isPrime(backgroundWidth)) {
                    backgroundWidth = currentImage.getWidth() + ((int) aspectRatio * 5);
                    backgroundHeight = currentImage.getHeight() + ((int) aspectRatio * 5);
                }

                BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, backgroundWidth, backgroundHeight);
                ImageIO.write(saveImage, "png", currentFile);
            }

            initBackgrounds();
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    public static void initBackgrounds() {
        try {
            File dir = new File("src/users/" + getUUID() + "/Backgrounds");
            FilenameFilter PNGFilter = (dir1, filename) -> filename.endsWith(".png");

            backgroundFiles = new LinkedList<>(Arrays.asList(dir.listFiles(PNGFilter)));

            if (backgroundFiles.size() == 0) {
                backgroundFiles = new LinkedList<>();
                backgroundFiles.add(new File("src/com/cyder/sys/pictures/Bobby.png"));
            }
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    private static LinkedList<File> backgroundFiles;

    public static LinkedList<File> getBackgrounds() {
        initBackgrounds();
        return backgroundFiles;
    }

    private static int backgroundIndex;

    public static int getBackgroundIndex() {
        return backgroundIndex;
    }

    public static void setBackgroundIndex(int i) {
        backgroundIndex = i;
    }

    public static void incBackgroundIndex() {
        backgroundIndex += 1;
    }

    public static void decBackgroundIndex() {
        backgroundIndex -= 1;
    }

    private static File backgroundFile;

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

    private static Direction lastSlideDirection = Direction.TOP;

    public static void switchBackground() {
        if (!(backgroundFiles.size() > backgroundIndex + 1 && backgroundFiles.size() > 1))
            return;

        switch (lastSlideDirection) {
            case LEFT:

                //todo slide top

                lastSlideDirection = Direction.TOP;
                break;

            case TOP:

                //todo slide right

                lastSlideDirection = Direction.RIGHT;
                break;

            case RIGHT:

                //todo slide down

                lastSlideDirection = Direction.BOTTOM;
                break;

            case BOTTOM:

                //todo slide left

                lastSlideDirection = Direction.LEFT;
                break;
        }
    }

    public static int getBackgroundWidth() {
        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
            return (int) SystemUtil.getScreenSize().getWidth();
        else
            return getCurrentBackgroundImageIcon().getIconWidth();
    }

    public static int getBackgroundHeight() {
        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
            return (int) SystemUtil.getScreenSize().getHeight();
        else
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

    private static void rotateConsole() {
        //todo if setting console dir is different than old value,
        // call this method and roll the image that way then set like old way
    }

    private static boolean fullscreen = false;

    public static void setFullscreen(Boolean enable) {
        fullscreen = enable;
    }

    public static boolean isFullscreen() {
        return fullscreen;
    }

    public void barrelRoll() {

    }

    private static int scrollingDowns;

    public static int getScrollingDowns() {
        return scrollingDowns;
    }

    public static void setScrollingDowns(int downs) {
        scrollingDowns = downs;
    }

    public static void incScrollingDowns() {
        scrollingDowns += 1;
    }

    public static void decScrollingDowns() {
        scrollingDowns -= 1;
    }

    public static boolean onLastBackground() {
        initBackgrounds();
        return backgroundFiles.size() == backgroundIndex + 1;
    }

    public static boolean canSwitchBackground() {
        return backgroundFiles.size() > backgroundIndex + 1;
    }
}
