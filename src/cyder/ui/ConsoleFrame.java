package cyder.ui;

import cyder.enums.ConsoleDirection;
import cyder.enums.Direction;
import cyder.genesis.CyderMain;
import cyder.handler.ErrorHandler;
import cyder.utilities.*;
import cyder.widgets.GenericInform;

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

public final class ConsoleFrame extends CyderFrame {

    //assuming uuid has been set, this will launch the whole of the program
    // main now is used for user auth then called ConsoleFrame so only one instance of console frame should ever exist
    public ConsoleFrame() {
        resizeBackgrounds();
        initBackgrounds();
    }

    @Override
    public void repaint() {
        super.repaint();


        setDefaultCloseOperation(0);
        //now reset all the bounds and such
    }

    private boolean drawConsoleLines = false;
    private boolean consoleLinesDrawn = false;
    private Color lineColor = Color.white;

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (drawConsoleLines && !consoleLinesDrawn) {
            Graphics2D g2d = (Graphics2D) g;

            g2d.setPaint(lineColor);
            g2d.setStroke(new BasicStroke(5));

            g2d.drawLine(getWidth() / 2 - 3, 32, getWidth() / 2 - 3, getHeight() - 12);
            g2d.drawLine(10, getHeight() / 2 - 3, getWidth() - 12, getHeight() / 2 - 3);

            BufferedImage img = null;

            try {
                img = ImageIO.read(new File("src/cyder//sys/pictures/Neffex.png"));
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }

            int w = img.getWidth(null);
            int h = img.getHeight(null);

            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            g2d.drawImage(img, getWidth() / 2 - w / 2, getHeight() / 2 - h / 2, null);

            consoleLinesDrawn = true;
        }
    }

    private static String UUID;

    /**
     * @param uuid - the user uuid that we will use to determine our output dir and other
     *             information specific to this instance of the console frame
     */
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

    /**
     * Sets the OutputArea and InputField font for the current user
     *
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
        return new Font(IOUtil.getUserData("Font"), fontMetric, fontSize);
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

            for (int i = 0; i < backgrounds.size(); i++) {
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
                            "(That's what she said ahahahahah hahaha ha ha so funny).", "System Action");

                int screenWidth = SystemUtil.getScreenWidth();
                int screenHeight = SystemUtil.getScreenHeight();

                //resizing smaller
                while (backgroundWidth > screenWidth || backgroundHeight > screenHeight) {
                    backgroundWidth = (int) (currentImage.getWidth() / (aspectRatio * incremeter));
                    backgroundHeight = (int) (currentImage.getHeight() / (aspectRatio * incremeter));
                }

                if (backgroundWidth < 600 && backgroundHeight < 600)
                    GenericInform.inform("Resized the background image \"" + getBackgrounds().get(i).getName()
                            + "\" since it was too small.", "System Action");

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
        } catch (Exception ex) {
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
                backgroundFiles.add(new File("src/cyder//sys/pictures/DefaultBackground.png"));
            }
        } catch (Exception ex) {
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
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return backgroundImageIcon;
    }

    private static Direction lastSlideDirection = Direction.TOP;

    //todo make the frame and drag label stay when switching backgrounds and the image be separate (inside of consoleframe class)
    // you kind of did this in login with the sliding text, then notification will not go over it and only the background will slide
    // to do this, just have a backgroundLabel that you can slide in and out

    //todo make changing background animation no more than one second (so redo the method to calculate step)
    // make it also retain a console orientation when transitioning (both full screen or not full screen)

    public static void switchBackground() {
        try {
            //if we only have one background we can't switch
            if (!(backgroundFiles.size() > backgroundIndex + 1 && backgroundFiles.size() > 1))
                return;

            //todo replace with get next and get last image function calls
            ImageIcon oldBack = getCurrentBackgroundImageIcon();
            ImageIcon newBack = new ImageIcon(ImageIO.read(getBackgrounds()
                    .get(getBackgroundIndex() == 0 ? getBackgrounds().size() - 1 : getBackgroundIndex() - 1)));

            //get the dimensions which we will flip to, the next image
            int width = newBack.getIconWidth();
            int height = newBack.getIconHeight();

            //are we full screened and are we rotated?
            boolean fullscreen = IOUtil.getUserData("FullScreen").equalsIgnoreCase("1");
            ConsoleDirection direction = getConsoleDirection();

            //if full screen then get full screen images
            if (fullscreen) {
                width = SystemUtil.getScreenWidth();
                height = SystemUtil.getScreenHeight();

                oldBack = ImageUtil.resizeImage(oldBack, width, height);
                newBack = ImageUtil.resizeImage(newBack, width, height);
            }

            //when switching backgrounds, we ignore rotation if in full screen because it is impossible
            else {
                //not full screen and oriented left
                if (direction == ConsoleDirection.LEFT) {
                    width = width + height;
                    height = width - height;
                    width = width - height;

                    oldBack = new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.ImageIcon2BufferedImage(oldBack), -90));
                    newBack = new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.ImageIcon2BufferedImage(newBack), -90));
                }

                //not full screen and oriented right
                else if (direction == ConsoleDirection.RIGHT) {
                    width = width + height;
                    height = width - height;
                    width = width - height;

                    oldBack = new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.ImageIcon2BufferedImage(oldBack), 90));
                    newBack = new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.ImageIcon2BufferedImage(newBack), 90));
                }
            }

            //todo make sure console rotations are impossible in full screen

            //make master image to set to background and slide
            ImageIcon combinedIcon;

            //todo bug found, on logout, should reset console dir (will be fixed with cyderframe instances holding entire cyder instance essentially)
            //stop music and basically everything on close, (mp3 music continues)

            //todo before combining images, we need to make sure they're the same size, duhhhhh
            oldBack = ImageUtil.resizeImage(oldBack, width, height);
            newBack = ImageUtil.resizeImage(newBack, width, height);

            switch (lastSlideDirection) {
                case LEFT:
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.BOTTOM);
                    //todo set image bounds
                    //todo setbackground tod this new image
                    //new CyderAnimation().jLabelYUp(0, -height, 10, 10, iconLabel);
                    //todo slide up by height so init bounds are 0,height,width,height
                    //todo set actual icon to background

                    //rest all new bounds

                    lastSlideDirection = Direction.TOP;
                    break;

                case TOP:
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.LEFT);
                    //todo slide right by width so init bounds are -width,0,width,height

                    lastSlideDirection = Direction.RIGHT;
                    break;

                case RIGHT:
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.TOP);
                    //todo slide down by height so init bounds are 0,-height,width,height

                    lastSlideDirection = Direction.BOTTOM;
                    break;

                case BOTTOM:
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.RIGHT);
                    //todo slide left by width so init bounds are width,0,width,height

                    lastSlideDirection = Direction.LEFT;
                    break;
            }

//            parentLabel.setToolTipText(ConsoleFrame.getCurrentBackgroundFile().getName().replace(".png", ""));
//            consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 -
//                            CyderFrame.getTitleWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) / 2 - 13,
//                    2,CyderFrame.getTitleWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) + 26, 25);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * @return returns the current background with using the current background ImageIcon and whether or not full screen is active
     */
    public static int getBackgroundWidth() {
        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
            return (int) SystemUtil.getScreenSize().getWidth();
        else
            return getCurrentBackgroundImageIcon().getIconWidth();
    }

    /**
     * @return returns the current background height using the current background ImageIcon and whether or not full screen is active
     */
    public static int getBackgroundHeight() {
        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
            return (int) SystemUtil.getScreenSize().getHeight();
        else
            return getCurrentBackgroundImageIcon().getIconHeight();
    }

    //this is an example of how the new method should look within here, remove all get user data from IOUtil
    public static String getUserData(String dataName) {
        return (dataName instanceof String ? dataName : dataName.equals("1") ? "true" : "false");
    }

    private static boolean consoleClockEnabled;

    public static void setConsoleClock(Boolean enable) {
        consoleClockEnabled = IOUtil.getUserData("ClockOnConsole").equals("1");

        if (enable) {
            //set console clock visible
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            }, 0, 1, TimeUnit.SECONDS);
        } else {
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
        //todo also refresh direction, do nothing if in full screen of course
    }

    public static ConsoleDirection getConsoleDirection() {
        return consoleDir;
    }

    private static void rotateConsole(int deg) {
        //todo roll console to deg, image should be centered in middle, however
        // dont use this for setting console dir though, that should snap in direction
        // since we are rolling, it should be a smooth transition
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

    /**
     * if there is only one instance of a ConsoleFrame, then we will exit the program on close of this instance,
     * otherwise, we will dispose the frame and continue process execution
     */
    @Override
    public void setDefaultCloseOperation(int ignored) {
        super.setDefaultCloseOperation((CyderMain.getConsoleFrameInstances() == null || CyderMain.getConsoleFrameInstances().length < 2)
                ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
    }
}
