
package com.cyder.utilities;

import com.cyder.enums.ConsoleDirection;
import com.cyder.exception.FatalException;
import com.cyder.handler.ErrorHandler;
import com.cyder.ui.CyderButton;
import com.cyder.widgets.GenericInform;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import static com.cyder.enums.ConsoleDirection.*;

public class GeneralUtil {

    //uservars
    private static String userUUID;
    private static String username;
    private static Color usercolor;
    private static Font userfont;
    private static String os;
    private static int currentBackgroundIndex;
    private static File[] validBackgroundPaths;
    private boolean consoleClock;

    private ConsoleDirection consoleDirection = UP;
    //put these in consoleframe
    public BufferedImage getRotatedImage(String name) {
        switch(consoleDirection) {
            case UP:
                return ImageUtil.getBi(name);
            case RIGHT:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(name),90);
            case DOWN:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(name),180);
            case LEFT:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(name),-90);
        }

        return null;
    }

    //boolean vars
    private boolean handledMath;
    private boolean hideOnClose;
    private boolean oneMathPrint;
    private boolean alwaysOnTop;

    //scrolling var
    private int currentDowns;

    //drawing vars
    private JFrame pictureFrame;
    private CyderButton closeDraw;

    //backbround vars
    private int backgroundX;
    private int backgroundY;



    //Sys.log with title and other stuff
    public boolean released() {
        return false;
    }

    //io util, put in user data
    public String getIPKey() {
        try {
            BufferedReader keyReader = new BufferedReader(new FileReader("src/com/cyder/sys/text/keys.txt"));
            String line = "";

            while ((line = keyReader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts[0].equals("ip")) {
                    return parts[1];
                }
            }
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }

        return null;
    }

    //io util, put in user data
    public String getWeatherKey() {
        try {
            BufferedReader keyReader = new BufferedReader(new FileReader("src/com/cyder/sys/text/keys.txt"));
            String line = "";

            while ((line = keyReader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts[0].equals("weather")) {
                    return parts[1];
                }

            }
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }

        return null;
    }

    //console frame
    public int getCurrentDowns() {
        return this.currentDowns;
    }
    public void setCurrentDowns(int num) {
        this.currentDowns = num;
    }
    public boolean getHandledMath() {
        return this.handledMath;
    }
    public void setHandledMath(boolean b) {
        this.handledMath = b;
    }

    //user utils
    public static String getUserUUID() {
        return userUUID;
    }
    public static void setUserUUID(String s) {
        userUUID = s;
    }

    //user utils
    public static void setUsername(String name) {
        username = name;
    }
    public static String getUsername() {
        return username;
    }

    //user utils
    public static void setUsercolor(Color c) {
        usercolor = c;
    }
    public static Color getUsercolor() {
        return usercolor;
    }

    //user utils
    public static void setUserfont(Font f) {
        userfont = f;
    }
    public static Font getUserfont() {
        return userfont;
    }

    //move to consoleframe
    public boolean OnLastBackground() {
        return (validBackgroundPaths.length == currentBackgroundIndex + 1);
    }
    public File[] getValidBackgroundPaths() {
        initBackgrounds();
        return this.validBackgroundPaths;
    }

    //console frame
    public void setCurrentBackgroundIndex(int i) {
        this.currentBackgroundIndex = i;
    }
    public int getCurrentBackgroundIndex() {
        return this.currentBackgroundIndex;
    }
    public File getCurrentBackground() {
        return validBackgroundPaths[currentBackgroundIndex];
    }
    public void getBackgroundSize() {
        ImageIcon Size = new ImageIcon(getCurrentBackground().toString());

        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
            backgroundX = new SystemUtil().getScreenWidth();
            backgroundY = new SystemUtil().getScreenHeight();
        }

        else {
            backgroundX = Size.getIconWidth();
            backgroundY = Size.getIconHeight();
        }
    }

    //consoleframe
    public int getBackgroundX() {
        return this.backgroundX;
    }
    public int getBackgroundY() {
        return this.backgroundY;
    }
    public void setBackgroundX(int x) {
        this.backgroundX = x;
    }
    public void setBackgroundY(int y) {
        this.backgroundY = y;
    }

    //console frame
    public void initBackgrounds() {
        try {
            File dir = new File("src/users/" + getUserUUID() + "/Backgrounds");
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
    public boolean canSwitchBackground() {
        return (validBackgroundPaths.length > currentBackgroundIndex + 1 && validBackgroundPaths.length > 1);
    }

    //console frame
    public void setConsoleClock(boolean b) {
        this.consoleClock = b;
    }
    public boolean getConsoleClock() {
        return this.consoleClock;
    }

    //console frame
    public ConsoleDirection getConsoleDirection() {
        return this.consoleDirection;
    }
    public void setConsoleDirection(ConsoleDirection d) {
        this.consoleDirection = d;
    }

    //console frame
    public void resizeImages() {
        try {
            for (int i = 0 ; i < getValidBackgroundPaths().length ; i++) {
                File UneditedImage = getValidBackgroundPaths()[i];

                BufferedImage currentImage = ImageIO.read(new File(getValidBackgroundPaths()[i].toString()));

                setBackgroundX(currentImage.getWidth());
                setBackgroundY(currentImage.getHeight());

                double aspectRatio = ImageUtil.getAspectRatio(currentImage);
                int imageType = currentImage.getType();

                if (getBackgroundX() > new SystemUtil().getScreenWidth() || getBackgroundY() > new SystemUtil().getScreenHeight()) {
                    GenericInform.inform("Resized the background image \"" + getValidBackgroundPaths()[i].getName() + "\" since it was too big " +
                            "(That's what she said ahahahahah hahaha ha ha so funny).","System Action", 700, 200);
                }

                while (getBackgroundX() > new SystemUtil().getScreenWidth() || getBackgroundY() > new SystemUtil().getScreenHeight()) {
                    currentImage = ImageIO.read(new File(getValidBackgroundPaths()[i].toString()));

                    int width = (int) (currentImage.getWidth() / aspectRatio);
                    int height = (int) (currentImage.getHeight() / aspectRatio);

                    BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, width, height);

                    ImageIO.write(saveImage, "png", new File(getValidBackgroundPaths()[i].toString()));

                    setBackgroundX(saveImage.getWidth());
                    setBackgroundY(saveImage.getHeight());
                    getValidBackgroundPaths();
                }

                if (getBackgroundX() < 600 || getBackgroundY() < 600) {
                    GenericInform.inform("Resized the background image \"" + getValidBackgroundPaths()[i].getName() + "\" since it was too small.","System Action", 700, 200);
                }

                while (getBackgroundX() < 600 || getBackgroundY() < 600) {
                    currentImage = ImageIO.read(new File(getValidBackgroundPaths()[i].toString()));

                    int width = (int) (currentImage.getWidth() * aspectRatio);
                    int height = (int) (currentImage.getHeight() * aspectRatio);

                    BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, width, height);

                    ImageIO.write(saveImage, "png", new File(getValidBackgroundPaths()[i].toString()));

                    setBackgroundX(saveImage.getWidth());
                    setBackgroundY(saveImage.getHeight());
                    getValidBackgroundPaths();
                }

                if (NumberUtil.isPrime(getBackgroundX())) {
                    currentImage = ImageIO.read(new File(getValidBackgroundPaths()[i].toString()));

                    int width = currentImage.getWidth() + 1;
                    int height = currentImage.getHeight() + 1;

                    BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, width, height);

                    ImageIO.write(saveImage, "png", new File(getValidBackgroundPaths()[i].toString()));

                    setBackgroundX(saveImage.getWidth());
                    setBackgroundY(saveImage.getHeight());
                    getValidBackgroundPaths();
                }
            }

            getValidBackgroundPaths();
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }
}