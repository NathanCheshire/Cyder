package cyder.ui;

import cyder.consts.CyderImages;
import cyder.enums.Direction;
import cyder.handler.ErrorHandler;
import cyder.utilities.*;
import cyder.widgets.GenericInform;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;

public final class ConsoleFrame extends CyderFrame {
    //the one and only console frame method
    private static ConsoleFrame theConsoleFrame = new ConsoleFrame();

    public static ConsoleFrame getConsoleFrame() {
        return theConsoleFrame;
    }

    private ConsoleFrame() {} //no instantiation this way

    /**
     * Assuming uuid has been set, this will launch the whole of the program.
     * Main class is used for user auth then calls ConsoleFrame so under current program structure,
     * only one instance of console frame should ever exist.
     */
    private void start(String UUID) {
        resizeBackgrounds();
        initBackgrounds();

        //everything else here, calling constructor should make it open up and program start working
    }

    @Override
    public void repaint() {
        super.repaint();

        //todo reset all bounds of console elements and take into account possible
        // fullscreen set and rotation set
    }

    @Override
    public void setTitlePosition(TitlePosition position) {
        if (position == TitlePosition.LEFT || position == TitlePosition.RIGHT) {
            throw new IllegalArgumentException("Left and righttitle positions now allowed for ConsoleFrame");
        } else {
            super.setTitlePosition(position);
        }
    }

    private String UUID;

    /**
     * Set the UUID for this Cyder session. Everything else relies on this being set and not null.
     * Once set, a one time check is performed to fix any possibly corrupted userdata.
     * @param uuid - the user uuid that we will use to determine our output dir and other
     *             information specific to this instance of the console frame
     */
    public void setUUID(String uuid) {
        UUID = uuid;
        IOUtil.fixUserData();
    }

    public String getUUID() {
        return UUID;
    }

    public String getUsername() {
        String name = IOUtil.getUserData("Name");
        if (name == null || name.trim().length() < 1)
            return "Name Not Found";
        else
            return name;
    }

    private int fontMetric = Font.BOLD;

    public void setFontBold() {
        fontMetric = Font.BOLD;
    }

    public void setFontItalic() {
        fontMetric = Font.ITALIC;
    }

    public void setFontPlain() {
        fontMetric = Font.PLAIN;
    }

    /**
     * Sets the OutputArea and InputField font style for the current user
     * @param combStyle use Font.BOLD and Font.Italic to set the user
     *                  font style. You may pass combinations of font
     *                  styling using the addition operator
     */
    public void setFontStyle(int combStyle) {
        fontMetric = combStyle;
    }

    private int fontSize = 30;

    /**
     * Sets the font size for the user to be used when {@link ConsoleFrame#getUserFont()} is called.
     * @param size - the size of the font
     */
    public void setFontSize(int size) {
        fontSize = size;
    }

    /**
     * Get the desired user font in combination with the set font metric and font size.
     * @return - the font to use for the input and output areas
     */
    public Font getUserFont() {
        return new Font(IOUtil.getUserData("Font"), fontMetric, fontSize);
    }

    /**
     * Get the user's foreground color from Userdata
     * @return - a Color object representing the chosen foreground
     */
    public Color getUserForegroundColor() {
        return ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground"));
    }

    /**
     * Get the user's background color from Userdata
     * @return - a Color object representing the chosen background
     */
    public Color getUserBackgroundColor() {
        return ColorUtil.hextorgbColor(IOUtil.getUserData("Background"));
    }

    /**
     * Takes into account the dpi scaling value and checks all the backgrounds in the user's
     * directory against the current monitors resolution. If any width or height of a background file
     * exceeds the monitor's width or height. We resize until it doesn't. We also check to make sure the background
     * meets our minimum pixel dimension parameters.
     */
    public void resizeBackgrounds() {
        try {
            LinkedList<File> backgrounds = getBackgrounds();

            for (int i = 0; i < backgrounds.size(); i++) {
                File currentFile = backgrounds.get(i);
                BufferedImage currentImage = ImageIO.read(currentFile);
                int backgroundWidth = currentImage.getWidth();
                int backgroundHeight = currentImage.getHeight();
                int screenWidth = SystemUtil.getScreenWidth();
                int screenHeight = SystemUtil.getScreenHeight();
                double aspectRatio = ImageUtil.getAspectRatio(currentImage);
                int imageType = currentImage.getType();

                //inform the user we are changing the size of the image.
                if (backgroundWidth > SystemUtil.getScreenWidth() || backgroundHeight > SystemUtil.getScreenHeight())
                    GenericInform.inform("Resized the background image \"" + currentFile.getName() + "\" since it was too big.",
                            "System Action");

                //while the image dimensions are greater than the screen dimensions,
                // divide the image dimensions by the the aspect ratio if it will result in a smaller number
                // if it won't then we divide by 1/aspectRatio which will result in a smaller number if the first did not
                while (backgroundWidth > screenWidth || backgroundHeight > screenHeight) {
                    backgroundWidth = (int) (backgroundWidth / ((aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio)));
                    backgroundHeight = (int) (backgroundHeight / ((aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio)));
                }

                //inform the user we are changing the size of the image
                if (backgroundWidth < 600 && backgroundHeight < 600)
                    GenericInform.inform("Resized the background image \"" + getBackgrounds().get(i).getName()
                            + "\" since it was too small.", "System Action");

                //while the image dimensions are less than 800x800, multiply the image dimensions by the
                // aspect ratio if it will result in a bigger number, if it won't, multiply it by 1.0 / aspectRatio
                // which will result in a number greater than 1.0 if the first option failed.
                while (backgroundWidth < 800 && backgroundHeight < 800) {
                    backgroundWidth = (int) (backgroundWidth * (aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio));
                    backgroundHeight = (int) (backgroundHeight * (aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio));
                }

                //if the background width is a prime number then do some math
                if (NumberUtil.isPrime(backgroundWidth)) {
                    backgroundWidth = currentImage.getWidth() + ((int) aspectRatio * 5);
                    backgroundHeight = currentImage.getHeight() + ((int) aspectRatio * 5);
                }

                //save the modified image
                BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, backgroundWidth, backgroundHeight);
                ImageIO.write(saveImage, "png", currentFile);
            }

            //reinit backgrounds after resizing all backgrounds that needed fixing
            initBackgrounds();
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    private LinkedList<File> backgroundFiles;

    public void initBackgrounds() {
        try {
            File dir = new File("users/" + getUUID() + "/Backgrounds");
            FilenameFilter PNGFilter = (dir1, filename) -> filename.endsWith(".png");

            backgroundFiles = new LinkedList<>(Arrays.asList(dir.listFiles(PNGFilter)));

            //if no backgrounds, copy the default image icon over and recall initBackgrounds()
            if (backgroundFiles.size() == 0) {
                Image img = CyderImages.defaultBackground.getImage();

                BufferedImage bi = new BufferedImage(img.getWidth(null),
                        img.getHeight(null),BufferedImage.TYPE_INT_RGB);

                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
                ImageIO.write(bi, "png", new File("users/" + getUsername()
                        + "/Backgrounds/Default.png"));

                initBackgrounds();
            }
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    public LinkedList<File> getBackgrounds() {
        initBackgrounds();
        return backgroundFiles;
    }

    private int backgroundIndex;

    public int getBackgroundIndex() {
        return backgroundIndex;
    }

    public void setBackgroundIndex(int i) {
        backgroundIndex = i;
    }

    public void incBackgroundIndex() {
        backgroundIndex += 1;
    }

    public void decBackgroundIndex() {
        backgroundIndex -= 1;
    }

    private static File backgroundFile;

    public File getCurrentBackgroundFile() {
        backgroundFile = backgroundFiles.get(backgroundIndex);
        return backgroundFile;
    }

    private ImageIcon backgroundImageIcon;

    public ImageIcon getCurrentBackgroundImageIcon() {
        try {
            File f = getCurrentBackgroundFile();
            backgroundImageIcon = new ImageIcon(ImageIO.read(f));
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return backgroundImageIcon;
        }
    }

    public ImageIcon getNextBackgroundImageIcon() {
        ImageIcon ret = null;

        try {
            if (backgroundIndex + 1 == backgroundFiles.size() - 1) {
                ret = new ImageIcon(ImageIO.read(backgroundFiles.get(0)));
            } else {
                ret = new ImageIcon(ImageIO.read(backgroundFiles.get(backgroundFiles.size() + 1)));
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    public ImageIcon getLastBackgroundImageIcon() {
        ImageIcon ret = null;

        try {
            if (backgroundIndex - 1 >= 0) {
                ret = new ImageIcon(ImageIO.read(backgroundFiles.get(backgroundIndex - 1)));
            } else {
                ret = new ImageIcon(ImageIO.read(backgroundFiles.get(backgroundFiles.size() - 1)));
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    private Direction lastSlideDirection = Direction.TOP;

    //todo make the frame and drag label stay when switching backgrounds and the image be separate (inside of consoleframe class)
    // you kind of did this in login with the sliding text, then notification will not go over it and only the background will slide
    // to do this, just have a backgroundLabel that you can slide in and out

    //todo make changing background animation no more than one second (so redo the method to calculate step)
    // make it also retain a console orientation when transitioning (both full screen or not full screen)

    //if this returns false then we didn't switch so we should tell the user they should add more backgrounds
    public boolean switchBackground() {
        try {
            //if we only have one background we can't switch
            if (!(backgroundFiles.size() > backgroundIndex + 1 && backgroundFiles.size() > 1))
                return false;

            ImageIcon oldBack = getCurrentBackgroundImageIcon();
            ImageIcon newBack = getNextBackgroundImageIcon();

            //get the dimensions which we will flip to, the next image
            int width = newBack.getIconWidth();
            int height = newBack.getIconHeight();

            //are we full screened and are we rotated?
            boolean fullscreen = IOUtil.getUserData("FullScreen").equalsIgnoreCase("1");
            Direction direction = getConsoleDirection();

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
                if (direction == Direction.LEFT) {
                    width = width + height;
                    height = width - height;
                    width = width - height;

                    oldBack = new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.ImageIcon2BufferedImage(oldBack), -90));
                    newBack = new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.ImageIcon2BufferedImage(newBack), -90));
                }

                //not full screen and oriented right
                else if (direction == Direction.RIGHT) {
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
                    int[] delayInc = AnimationUtil.getDelayIncrement(height);
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

            //todo change tooltip for background
            //todo refresh consoleclock bounds
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return true;
        }
    }

    /**
     * @return returns the current background with using the current background ImageIcon and whether or not full screen is active
     */
    public int getBackgroundWidth() {
        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
            return (int) SystemUtil.getScreenSize().getWidth();
        else
            return getCurrentBackgroundImageIcon().getIconWidth();
    }

    /**
     * @return returns the current background height using the current background ImageIcon and whether or not full screen is active
     */
    public int getBackgroundHeight() {
        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
            return (int) SystemUtil.getScreenSize().getHeight();
        else
            return getCurrentBackgroundImageIcon().getIconHeight();
    }

    //this is an example of how the new method should look within here, remove all get user data from IOUtil
    public String getUserData(String dataName) {
        return (dataName instanceof String ? dataName : dataName.equals("1") ? "true" : "false");
    }

    private boolean consoleClockEnabled;

    public void setConsoleClock(Boolean enable) {
        IOUtil.writeUserData("ClockOnConsole", enable ? "1" : "0");
        consoleClockEnabled = enable;

        if (enable) {
            setTitle("");
            //start up executor
        } else {
            setTitle("");
            //end executor task if running
        }
    }

    public boolean consoleClockEnabled() {
        return consoleClockEnabled;
    }

    private Direction consoleDir = Direction.TOP;

    public void setConsoleDirection(Direction conDir) {
        consoleDir = conDir;
        getConsoleFrame().repaint();
    }

    public Direction getConsoleDirection() {
        return consoleDir;
    }

    /**
     * Smoothly transitions the background icon to the specified degrees.
     * Use set console direction for console flipping and not this.
     * @param deg - the degree by which to smoothly rotate
     */
    private void rotateConsole(int deg) {
        ImageIcon masterIcon = (ImageIcon) ((JLabel) getConsoleFrame().getContentPane()).getIcon();
        BufferedImage master = ImageUtil.getBi(masterIcon);

        Timer timer = null;
        Timer finalTimer = timer;
        timer = new Timer(10, new ActionListener() {
            private double angle = 0;
            private double delta = 2.0;

            BufferedImage rotated;

            @Override
            public void actionPerformed(ActionEvent e) {
                angle += delta;
                if (angle > deg) {
                    rotated = ImageUtil.rotateImageByDegrees(master, deg);
                    ((JLabel) getConsoleFrame().getContentPane()).setIcon(new ImageIcon(rotated));
                    return;
                }
                rotated = ImageUtil.rotateImageByDegrees(master, angle);
                ((JLabel) getConsoleFrame().getContentPane()).setIcon(new ImageIcon(rotated));
            }
        });
        timer.start();
    }

    private boolean fullscreen = false;

    public void setFullscreen(Boolean enable) {
        fullscreen = enable;

        //this should recalculate bounds for components depending on console direction and fullscreen mode
        repaint();
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    private int scrollingDowns;

    public int getScrollingDowns() {
        return scrollingDowns;
    }

    public void setScrollingDowns(int downs) {
        scrollingDowns = downs;
    }

    public void incScrollingDowns() {
        scrollingDowns += 1;
    }

    public void decScrollingDowns() {
        scrollingDowns -= 1;
    }

    public boolean onLastBackground() {
        initBackgrounds();
        return backgroundFiles.size() == backgroundIndex + 1;
    }

    public boolean canSwitchBackground() {
        return backgroundFiles.size() > backgroundIndex + 1;
    }
}
