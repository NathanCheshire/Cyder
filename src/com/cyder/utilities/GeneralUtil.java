
package com.cyder.utilities;

import com.cyder.exception.FatalException;
import com.cyder.handler.ErrorHandler;
import com.cyder.ui.ConsoleFrame;
import com.cyder.widgets.GenericInform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;

public class GeneralUtil {

    private static int currentBackgroundIndex;
    private static File[] validBackgroundPaths;

    //backbround vars
    private static int backgroundWidth;
    private static int backgroundHeight;

    //move to consoleframe
    public static boolean onLastBackground() {
        return (validBackgroundPaths.length == currentBackgroundIndex + 1);
    }

    public static File[] getBackgrounds() {
        refreshBackgrounds();
        return validBackgroundPaths;
    }

    //console frame
    public static void setCurrentBackgroundIndex(int i) {
        currentBackgroundIndex = i;
    }

    public static int getCurrentBackgroundIndex() {
        return currentBackgroundIndex;
    }

    public static File getCurrentBackground() {
        return validBackgroundPaths[currentBackgroundIndex];
    }

    //console frame
    public static int getBackgroundWidth() {
        return backgroundWidth;
    }

    public static int getBackgroundHeight() {
        return backgroundHeight;
    }

    public static void setBackgroundWidth(int x) {
        backgroundWidth = x;
    }

    public static void setBackgroundHeight(int y) {
        backgroundHeight = y;
    }

    //console frame
    public static void refreshBackgrounds() {
        try {
            File dir = new File("src/users/" + ConsoleFrame.getUUID() + "/Backgrounds");
            FilenameFilter PNGFilter = (dir1, filename) -> filename.endsWith(".png");
            validBackgroundPaths = dir.listFiles(PNGFilter);

            if (validBackgroundPaths.length == 0)
                validBackgroundPaths = new File[]{new File("src/com/cyder/sys/pictures/Bobby.png")};
        }

        catch (ArrayIndexOutOfBoundsException ex) {
            ErrorHandler.handle(ex);
            ErrorHandler.handle(new FatalException(ex.getMessage()));
        }
    }

    //console frame
    public static boolean canSwitchBackground() {
        return (validBackgroundPaths.length > currentBackgroundIndex + 1 && validBackgroundPaths.length > 1);
    }

    //console frame
    public static void resizeUserBackgrounds() {
        try {
            File[] backgrounds = getBackgrounds();

            for (int i = 0; i < backgrounds.length ; i++) {
                File currentFile = backgrounds[i];

                BufferedImage currentImage = ImageIO.read(currentFile);

                backgroundWidth = currentImage.getWidth();
                backgroundHeight = currentImage.getHeight();

                double aspectRatio = ImageUtil.getAspectRatio(currentImage);
                int imageType = currentImage.getType();

                if (backgroundWidth > new SystemUtil().getScreenWidth() || backgroundHeight > new SystemUtil().getScreenHeight()) {
                    GenericInform.inform("Resized the background image \"" + currentFile.getName() + "\" since it was too big " +
                            "(That's what she said ahahahahah hahaha ha ha so funny).","System Action", 700, 200);
                }

                //resizing smaller
                while (backgroundWidth > new SystemUtil().getScreenWidth() || backgroundHeight > new SystemUtil().getScreenHeight()) {
                    currentImage = ImageIO.read(currentFile);

                    int width = (int) (currentImage.getWidth() / aspectRatio);
                    int height = (int) (currentImage.getHeight() / aspectRatio);

                    BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, width, height);

                    ImageIO.write(saveImage, "png", currentFile);

                    backgroundWidth = saveImage.getWidth();
                    backgroundHeight = saveImage.getHeight();
                }

                if (backgroundWidth < 600 || backgroundHeight < 600) {
                    GenericInform.inform("Resized the background image \"" + getBackgrounds()[i].getName() + "\" since it was too small.","System Action", 700, 200);
                }

                //resizing bigger
                while (backgroundWidth < 600 || backgroundHeight < 600) {
                    currentImage = ImageIO.read(currentFile);

                    int width = (int) (currentImage.getWidth() * aspectRatio);
                    int height = (int) (currentImage.getHeight() * aspectRatio);

                    BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, width, height);

                    ImageIO.write(saveImage, "png", currentFile);

                    backgroundWidth = saveImage.getWidth();
                    backgroundHeight = saveImage.getHeight();
                }

                if (NumberUtil.isPrime(backgroundWidth)) {
                    currentImage = ImageIO.read(currentFile);

                    int width = currentImage.getWidth() + 1;
                    int height = currentImage.getHeight() + 1;

                    BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, width, height);

                    ImageIO.write(saveImage, "png", currentFile);

                    backgroundWidth = saveImage.getWidth();
                    backgroundHeight = saveImage.getHeight();
                }
            }

            getBackgrounds();
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }
}