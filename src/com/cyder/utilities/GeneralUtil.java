
package com.cyder.utilities;

import com.cyder.exception.FatalException;
import com.cyder.ui.CyderButton;
import com.cyder.widgets.GenericInform;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.UUID;

public class GeneralUtil {

    //todo enums package

    //static strings used for test cases
    public static final String HERE = "here";

    public static final String LENGTH_ZERO = "";
    public static final String LENGTH_ONE = "0";
    public static final String LENGTH_TWO = "01";
    public static final String LENGTH_THREE = "012";
    public static final String LENGTH_FOUR = "0123";
    public static final String LENGTH_FIVE = "01234";
    public static final String LENGTH_SIX = "102345";
    public static final String LENGTH_SEVEN = "0123456";
    public static final String LENGTH_EIGHT = "01234567";
    public static final String LENGTH_NINE = "012345678";

    public static final String QUICK_BROWN_FOX = "The quick brown fox jumps over the lazy dog";

    public static final String SUPER_LONG = "pneumonoultramicroscopicsilicovolcanoconiosis," +
                                             "pneumonoultramicroscopicsilicovolcanoconiosi," +
                                             "pneumonoultramicroscopicsilicovolcanoconiosis!" +
                                             "There, I said it!";
    //integer bounds
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int NEG_INFINITY = Integer.MIN_VALUE;

    //public fonts
    public static Font weatherFontSmall = new Font("Segoe UI Black", Font.BOLD, 20);
    public static Font weatherFontBig = new Font("Segoe UI Black", Font.BOLD, 30);
    public static Font loginFont = new Font("Comic Sans MS", Font.BOLD, 30);
    public static Font defaultFontSmall = new Font("tahoma", Font.BOLD, 15);
    public static Font defaultFont = new Font("tahoma", Font.BOLD, 30);
    public static Font tahoma = new Font("tahoma", Font.BOLD, 20);

    //public colors
    public static Color selectionColor = new Color(204,153,0);
    public static Color regularGreen = new Color(60, 167, 92);
    public static Color regularBlue = new Color(38,168,255);
    public static Color calculatorOrange = new Color(255,140,0);
    public static Color regularRed = new Color(223,85,83);
    public static Color intellijPink = new Color(236,64,122);
    public static Color consoleColor = new Color(39, 40, 34);
    public static Color tooltipBorderColor = new Color(26, 32, 51);
    public static Color tooltipForegroundColor = new Color(85,85,255);
    public static Color tooltipBackgroundColor = new Color(0,0,0);
    public static Color vanila = new Color(252, 251, 227);
    public static Color defaultColor = new Color(252, 251, 227);
    public static Color tttblue = new Color(71, 81, 117);
    public static Color navy = new Color(26, 32, 51);

    //uservars

    private static String userUUID;
    private static String username;
    private static Color usercolor;
    private static Font userfont;
    private static String os;
    private static int currentBackgroundIndex = 0;
    private static File[] validBackgroundPaths;
    private boolean consoleClock;

    //console orientation var
    public static int CYDER_UP = 0;
    public static int CYDER_RIGHT = 1;
    public static int CYDER_DOWN = 2;
    public static int CYDER_LEFT = 3;
    private int consoleDirection;
    //put these in consoleframe
    public BufferedImage getRotatedImage(String name) {
        switch(this.consoleDirection) {
            case 0:
                return ImageUtil.getBi(name);
            case 1:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(name),90);
            case 2:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(name),180);
            case 3:
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

    //This was the best bodge I ever pulled off
    public File getFile() {
        try {
            File WhereItIs = new File("src/com/cyder/sys/jars/FileChooser.jar");
            Desktop.getDesktop().open(WhereItIs);
            File f = new File("File.txt");
            f.delete();
            while (!f.exists()) {
                Thread.onSpinWait();
            }

            Thread.sleep(200);

            BufferedReader waitReader = new BufferedReader(new FileReader("File.txt")); //todo move to temp dir
            File chosenFile = new File(waitReader.readLine());
            f.delete();
            waitReader.close();

            return (chosenFile.getName().equalsIgnoreCase("null") ? null : chosenFile);
        }

        catch (Exception e) {
            handle(e);
        }

        return null;
    }

    //networ kutil
    public String getMACAddress() {
        byte[] MAC = null;

        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface NI = NetworkInterface.getByInetAddress(address);
            MAC = NI.getHardwareAddress();
        } catch (Exception e) {
            handle(e);
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < MAC.length; i++) {
            sb.append(String.format("%02X%s", MAC[i], (i < MAC.length - 1) ? "-" : ""));
        }

        return sb.toString();
    }

    //todo make dir for title, released, and such
    public boolean released() {
        return false;
    }

    //change to user by user so in user util
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
            handle(ex);
        }

        return null;
    }

    //change to user by user so in user util
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
            handle(ex);
        }

        return null;
    }

    //security util
    public byte[] getSHA(char[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();

            for (char c : input)
                sb.append(c);

            return md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
        }

        catch (Exception ex) {
            handle(ex);
        }

        return null;
    }
    //security util
    public String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
    //security util
    public String getDeprecatedUUID() {
        return "VoidUser-" + generateUUID().substring(0,8);
    }
    public String generateUUID() {
        try {
            MessageDigest salt = MessageDigest.getInstance("SHA-256");
            salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            return UUID.nameUUIDFromBytes(salt.digest()).toString();
        }

        catch (Exception e) {
            handle(e);
        }

        return null;
    }
    //security util
    public boolean checkPassword(String name, String pass) {
        try {
            File[] users = new File("src/users").listFiles();
            LinkedList<File> userDataFiles = new LinkedList<>();

            for (File f : users) {
                if (!f.getName().contains("DeprecatedUser")) {
                    userDataFiles.add(new File(f.getAbsolutePath() + "/Userdata.txt"));
                }
            }

            for (int i = 0 ; i < userDataFiles.size() ; i++) {
                BufferedReader currentRead = new BufferedReader(new FileReader(userDataFiles.get(i)));

                String filename = null;
                String filepass = null;
                String Line = currentRead.readLine();

                while (Line != null) {
                    String[] parts = Line.split(":");

                    if (parts[0].equalsIgnoreCase("Name")) {
                        filename = parts[1];
                    } else if (parts[0].equalsIgnoreCase("Password")) {
                        filepass = parts[1];
                    }

                    Line = currentRead.readLine();
                }

                if (pass.equals(filepass) && name.equals(filename)) {
                    setUserUUID(users[i].getName());
                    setUsername(name);
                    return true;
                }
            }
        }

        catch (Exception e) {
            handle(e);
        }

        return false;
    }

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

    //time util
    public boolean isChristmas() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 12 && Date == 25);
    }
    //time util
    public boolean isHalloween() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 10 && Date == 31);
    }
    //time util
    public boolean isIndependenceDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 7 && Date == 4);
    }
    //time util
    public boolean isThanksgiving() {
        Calendar Checker = Calendar.getInstance();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        LocalDate RealTG = LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
        return (Month == 11 && Date == RealTG.getDayOfMonth());
    }
    //time util
    public boolean isAprilFoolsDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 4 && Date == 1);
    }

    //consoleframe
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
            handle(ex);
            handle(new FatalException(ex.getMessage()));
        }
    }

    //put in consoleframe class
    public boolean canSwitchBackground() {
        return (validBackgroundPaths.length > currentBackgroundIndex + 1 && validBackgroundPaths.length > 1);
    }

    //handle class
    public static void staticHandle(Exception e) {
        new GeneralUtil().handle(e);
    }

    //handle class
    public static void handle(Exception e) {
        try {
            File throwsDir = new File("src/users/" + getUserUUID() + "/Throws/");

            if (!throwsDir.exists())
                throwsDir.mkdir();

            String eFileString = "src/users/" + getUserUUID() + "/Throws/" + TimeUtil.errorTime() + ".error";
            File eFile = new File(eFileString);
            eFile.createNewFile();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrack = sw.toString();
            int lineNumber = e.getStackTrace()[0].getLineNumber();
            Class c = e.getClass();

            String write = "Error thrown by line: " + lineNumber + " from\n" + c +
                    "\n\nStack Trace:\n\n" + stackTrack;

            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(eFileString));
            errorWriter.write(write);
            errorWriter.newLine();
            errorWriter.flush();
            errorWriter.close();

            if (IOUtil.getUserData("SilenceErrors").equals("1"))
                return;
            IOUtil.openFile(eFileString);
        }

        catch (Exception ex) {
            if (IOUtil.getUserData("SilenceErrors") != null && IOUtil.getUserData("SilenceErrors").equals("0")) {
                System.out.println("Exception in error logger:\n\n");
                e.printStackTrace();
                //todo show popup with inform on consoleframe
            }
        }
    }

    //prefs utils?
    public void setConsoleClock(boolean b) {
        this.consoleClock = b;
    }
    public boolean getConsoleClock() {
        return this.consoleClock;
    }

    //input utils with getinput
    public boolean confirmation(String input) {
        return (input.toLowerCase().contains("yes") || input.equalsIgnoreCase("y"));
    }

    //put in consoleframe class
    public int getConsoleDirection() {
        return this.consoleDirection;
    }
    public void setConsoleDirection(int d) {
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
            handle(ex);
        }
    }
}