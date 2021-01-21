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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConsoleFrame extends CyderFrame {

    //assuming uuid has been set, this will launch the whole of the program
    // main now is used for user auth then called ConsoleFrame
    public ConsoleFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //this depends on if loginframe is open or not
        //opening/closing loginframe should also change this value
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

    /** Sets the OutputArea and InputField font for the current user
     * @param combStyle use Font.BOLD and Font.Italic to set the user
     *                  font style. You may pass combinations of font
     *                  styling using the addition operator
     */
    public static void setFontStyle(int combStyle) {
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
                double incremeter = 1.0;

                if (aspectRatio == 1.0)
                    incremeter = 1.1;
                int imageType = currentImage.getType();

                if (backgroundWidth > SystemUtil.getScreenWidth() || backgroundHeight > SystemUtil.getScreenHeight())
                    GenericInform.inform("Resized the background image \"" + currentFile.getName() + "\" since it was too big " +
                            "(That's what she said ahahahahah hahaha ha ha so funny).","System Action");

                int screenWidth = SystemUtil.getScreenWidth();
                int screenHeight = SystemUtil.getScreenHeight();

                //resizing smaller
                while (backgroundWidth > screenWidth || backgroundHeight > screenHeight) {
                    backgroundWidth = (int) (currentImage.getWidth() / (aspectRatio * incremeter));
                    backgroundHeight = (int) (currentImage.getHeight() / (aspectRatio * incremeter));
                }

                if (backgroundWidth < 600 && backgroundHeight < 600)
                    GenericInform.inform("Resized the background image \"" + getBackgrounds().get(i).getName()
                            + "\" since it was too small.","System Action");

                if (aspectRatio < 1)
                    aspectRatio = 1 / aspectRatio;

                if (aspectRatio == 1.0)
                    incremeter = 1.1;

                //resizing bigger
                while (backgroundWidth < 800 && backgroundHeight < 800) {
                    backgroundWidth = (int) (currentImage.getWidth() * (aspectRatio * incremeter));
                    backgroundHeight = (int) (currentImage.getHeight() * (aspectRatio * incremeter));
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
                backgroundFiles.add(new File("src/com/cyder/sys/pictures/DefaultUserBackground.png"));
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

    //todo make the frame and drag label stay when switching backgrounds and the image be separate (inside of consoleframe class)
    // you kind of did this in login with the sliding text, then notification will not go over it and only the background will slide
    // to do this, just have a backgroundLabel that you can slide in and out

    //todo make changing background animation no more than one second
    // make it work in full screen too and remtain full screen
    // make it also retain a flip direction
    public static void switchBackground() {
        if (!(backgroundFiles.size() > backgroundIndex + 1 && backgroundFiles.size() > 1))
            return;

        int width = 0, height = 0;

        boolean fullscreen = IOUtil.getUserData("FullScreen").equalsIgnoreCase("1");
        ConsoleDirection direction = getConsoleDirection();

        if (fullscreen) {
            width = SystemUtil.getScreenWidth();
            height = SystemUtil.getScreenHeight();
        }

        else {
            //set width and height to the dimensions next image

            if (direction == ConsoleDirection.LEFT || direction == ConsoleDirection.RIGHT) {
                width = width + height;
                height = width - height;
                width = width - height;
            }
        }

        //todo based on dimensions get background images of those dimensions

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
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            },0, 1, TimeUnit.SECONDS);
        }

        else {
            //set visible false
            //end executor task if running
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

        //todo use this for barrel roll, askew, and ctrl + direction
    }

    private static boolean fullscreen = false;

    public static void setFullscreen(Boolean enable) {
        fullscreen = enable;
    }

    public static boolean isFullscreen() {
        return fullscreen;
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
